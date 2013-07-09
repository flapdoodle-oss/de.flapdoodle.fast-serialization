package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.impl.FSTByteArrayUnsafeStructGeneration;
import de.ruedigermoeller.heapoff.structs.impl.FSTEmbeddedBinary;
import de.ruedigermoeller.heapoff.structs.impl.FSTStructGeneration;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.util.FSTUtil;
import javassist.*;
import javassist.bytecode.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import sun.misc.Unsafe;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 22.06.13
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructFactory {

    static FSTStructFactory instance;
    public static FSTStructFactory getInstance() {
        if (instance==null) {
            instance = new FSTStructFactory();
        }
        return instance;
    }

    public static final int MAX_CLASSES = 1000;
    static Unsafe unsafe = FSTUtil.unFlaggedUnsafe;
    static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    static Loader proxyLoader = new Loader(FSTStructFactory.class.getClassLoader(), ClassPool.getDefault());

    ConcurrentHashMap<Class, Class> proxyClzMap = new ConcurrentHashMap<Class, Class>();
    FSTStructGeneration structGen = new FSTByteArrayUnsafeStructGeneration();

    <T> Class<T> createStructClz( Class<T> clazz ) throws Exception {
        //FIXME: ensure FSTStruct is superclass, check protected, no private methods+fields
        if ( Modifier.isFinal(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers()) ) {
            throw new RuntimeException("Cannot add final classes to structs");
        }
//        Class<?> clazz = toPack.getClass();
        String proxyName = clazz.getName()+"_Struct";
        Class present = null;
        try {
            present = proxyLoader.loadClass(proxyName);
        } catch (ClassNotFoundException ex) {
            //
        }
        if ( present != null )
            return present;
        ClassPool pool = ClassPool.getDefault();
        CtClass newClz = pool.makeClass(proxyName);
        CtClass orig = pool.get(clazz.getName());
        newClz.setSuperclass(orig);

        final FSTClazzInfo clInfo = conf.getClassInfo(clazz);

        CtMethod[] methods = orig.getMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            final Class curClz = Class.forName( method.getDeclaringClass().getName() );
            boolean allowed = ((method.getModifiers() & AccessFlag.ABSTRACT) == 0 ) &&
                    (method.getModifiers() & AccessFlag.NATIVE) == 0 &&
                    (method.getModifiers() & AccessFlag.FINAL) == 0 &&
                    ! method.getDeclaringClass().getName().equals(FSTStruct.class.getName()) &&
                    ! method.getDeclaringClass().getName().equals(Object.class.getName());
            allowed &= method.getAnnotation(NoAssist.class) == null;
            if ( allowed && (method.getModifiers() & AccessFlag.FINAL) != 0 && ! method.getDeclaringClass().getName().equals("java.lang.Object") ) {
                throw new RuntimeException("final methods are not allowed for struct classes:"+method.getName());
            }
            if ( allowed && (method.getModifiers() & AccessFlag.PRIVATE) != 0 && ! method.getDeclaringClass().getName().equals("java.lang.Object")) {
                throw new RuntimeException("private methods are not allowed for struct classes:"+method.getName());
            }
            if ( allowed ) {
                ClassMap mp = new ClassMap();
                mp.fix(clazz.getName());
                mp.fix(clazz.getSuperclass().getName());
                method = new CtMethod(method,newClz,mp);
                String methName = method.getName();
                FSTClazzInfo.FSTFieldInfo arrayFi = clInfo.getFieldInfo(methName, null);
                FSTClazzInfo.FSTFieldInfo lenfi = methName.length() > 2 ? clInfo.getFieldInfo(methName.substring(0, methName.length() - 3), null) : null;
                FSTClazzInfo.FSTFieldInfo indexfi = methName.length()>4 ? clInfo.getFieldInfo(methName.substring(0, methName.length() - 5), null) : null;
                if ( arrayFi == null || !arrayFi.isArray() || arrayFi.getArrayType().isArray() )
                    arrayFi = null;
                if ( lenfi == null || !lenfi.isArray() || lenfi.getArrayType().isArray() )
                    lenfi = null;
                if ( indexfi == null || !indexfi.isArray() || indexfi.getArrayType().isArray() )
                    indexfi = null;
                if ( indexfi != null ) {
                    structGen.defineArrayIndex(indexfi, clInfo, method);
                    newClz.addMethod(method);
                } else
                if (  arrayFi != null ) {
                    structGen.defineArrayAccessor(arrayFi, clInfo, method);
                    newClz.addMethod(method);
                } else if ( methName.endsWith("Len") && lenfi != null )
                {
                    structGen.defineArrayLength(lenfi, clInfo, method);
                    newClz.addMethod(method);
                } else {
                    newClz.addMethod(method);
//                    System.out.println("instrument "+newClz.getName()+"#"+method.getName()+" returns "+method.getReturnType().getName());
                    method.instrument( new ExprEditor() {
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            try {
                                if ( ! f.isStatic() ) {
                                    CtClass type = null;
                                    type = f.getField().getType();
                                    FSTClazzInfo.FSTFieldInfo fieldInfo = clInfo.getFieldInfo(f.getFieldName(), curClz);
                                    if ( fieldInfo == null ) {
//                                        System.out.println("no field for "+f.getFieldName());
                                        return;
                                    }
                                    if ( f.isReader() ) {
                                        structGen.defineStructReadAccess(f, type, fieldInfo);
                                    } else if ( f.isWriter() ) {
                                        structGen.defineStructWriteAccess(f, type, fieldInfo);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }

        return (Class<T>) loadProxyClass(clazz, pool, newClz);
    }

    private <T> Class loadProxyClass(Class<T> clazz, ClassPool pool, CtClass cc) throws ClassNotFoundException {
        Class ccClz;
        proxyLoader.delegateLoadingOf(Unsafe.class.getName());
        proxyLoader.delegateLoadingOf(clazz.getName());
        proxyLoader.delegateLoadingOf(Externalizable.class.getName());
        proxyLoader.delegateLoadingOf(FSTUtil.class.getName());
        proxyLoader.delegateLoadingOf(Serializable.class.getName());
        proxyLoader.delegateLoadingOf(FSTStructFactory.class.getName());
        proxyLoader.delegateLoadingOf(FSTStruct.class.getName());
        ccClz = proxyLoader.loadClass(cc.getName());
        return ccClz;
    }

    public Class getProxyClass(Class clz) throws Exception {
        Class res = proxyClzMap.get(clz);
        if ( res == null ) {
            res = createStructClz(clz);
            proxyClzMap.put(clz,res);
        }
        return res;
    }

    public <T extends FSTStruct> T createWrapper(Class<T> onHeap, byte bytes[], int index) throws Exception {
        Class proxy = getProxyClass(onHeap);
        T res = (T) unsafe.allocateInstance(proxy);
//        T res = (T) proxy.newInstance();
        res.baseOn(bytes, FSTUtil.bufoff+index, this);
        return res;
    }

    public FSTStruct createStructWrapper(byte b[], int offset) {
        int clzId = unsafe.getInt(b,FSTUtil.bufoff+offset+4);
        return createStructPointer(b, offset, clzId);
    }

    private FSTStruct createStructPointer(byte[] b, int index, Integer clzId) {
        Class clazz = mIntToClz.get(clzId);
        if (clazz==null)
            throw new RuntimeException("unregistered class "+clzId);
        try {
            return (FSTStruct) createWrapper(clazz, b, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends FSTStruct> T toStruct(T onHeap) {
        if ( onHeap.isOffHeap() ) {
            return onHeap;
        }
        try {
            byte b[] = toByteArray(onHeap);
            return (T)createWrapper(onHeap.getClass(),b,0);
        } catch (Exception e) {
            if ( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new RuntimeException(e);
        }
    }

    ThreadLocal<Object[]> cachedWrapperMap = new ThreadLocal<Object[]>() {
        @Override
        protected Object[] initialValue() {
            return new Object[MAX_CLASSES];
        }
    };

    public void detach(int clzId) {
        cachedWrapperMap.get()[clzId] = null;
    }

    public Object getStructPointerByOffset(byte b[], long offset) {
        if ( offset < 0 ) {
            return null;
        }
        Integer clzId = unsafe.getInt(b, offset+4);
        if (clzId.intValue() == 0) {
            return null;
        }
        Object[] wrapperMap = cachedWrapperMap.get();
        Object res = wrapperMap[clzId];
        if ( res != null ) {
            ((FSTStruct)res).baseOn(b, offset, this);
            return res;
        }
        res = createStructPointer(b, (int) (offset - FSTUtil.bufoff), clzId);
        wrapperMap[clzId] = res;
        return res;
    }

    public Object getStructPointer(byte b[], int index) {
        if ( index < 0 ) {
            return null;
        }
        Integer clzId = unsafe.getInt(b, FSTUtil.bufoff + index+4);
        if (clzId.intValue()==0) {
            return null;
        }
        Object[] wrapperMap = cachedWrapperMap.get();
        Object res = wrapperMap[clzId];
        if ( res != null ) {
            ((FSTStruct)res).baseOn(b, FSTUtil.bufoff + index, this);
            return res;
        }
        res = createStructPointer(b, index, clzId);
        wrapperMap[clzId] = res;
        return res;
    }

    public int calcStructSize(Object onHeapStruct) {
        try {
            if ( onHeapStruct == null ) {
                return 0;
            }
            int siz = 8;
            FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
            FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
            for (int i = 0; i < fis.length; i++) {
                FSTClazzInfo.FSTFieldInfo fi = fis[i];
                if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                    continue;
                // FIXME: check for null refs, check for FSTStruct subclasses
                if ( fi.getType().isArray() ) {
                    if ( fi.getType().getComponentType().isArray() ) {
                        throw new RuntimeException("nested arrays not supported");
                    }
                    if ( fi.isIntegral() ) { // prim array
                        Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                        if ( objectValue == null ) {
                            throw new RuntimeException("arrays in struct templates must not be null !");
                        }
                        siz += Array.getLength(objectValue) * fi.getComponentStructSize() + fi.getStructSize();
                    } else { // object array
                        Object objectValue[] = (Object[]) clInfo.getObjectValue(onHeapStruct, fi);
                        siz += 4 + objectValue.length*4;
                        for (int j = 0; j < objectValue.length; j++) {
                            Object o = objectValue[j];
                            siz+=calcStructSize(o);
                        }
                    }
                } else if ( fi.isIntegral() ) { // && ! array
                    siz += fi.getStructSize();
                } else { // objectref
                    Object obj = clInfo.getObjectValue(onHeapStruct,fi);
                    siz += fi.getStructSize()+calcStructSize(obj);
                }
            }
            if ( onHeapStruct instanceof FSTEmbeddedBinary ) {
                siz+=((FSTEmbeddedBinary) onHeapStruct).getEmbeddedSizeAdditon(this);
            }
            return siz;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    HashMap<Integer,Class> mIntToClz = new HashMap<Integer, Class>();
    HashMap<Class,Integer> mClzToInt = new HashMap<Class,Integer>();

    int idCount = 1;
    public void registerClz(Class c) {
        int id = idCount++;
        mIntToClz.put(id,c);
        mClzToInt.put(c,id);
    }

    public int getClzId(Class c) {
        Integer integer = mClzToInt.get(c);
        return integer == null ? 0: integer;
    }

    public byte[] toByteArray(Object onHeapStruct) {
        try {
            int sz = calcStructSize(onHeapStruct);
            byte b[] = new byte[sz];
            toByteArray(onHeapStruct,b,0);
            return b;
        } catch (Exception e) {
            if ( e instanceof RuntimeException )
                throw (RuntimeException)e;
            else
                throw new RuntimeException(e);
        }
    }

    static class ForwardEntry {

        ForwardEntry(int pointerPos, Object forwardObject) {
            this.pointerPos = pointerPos;
            this.forwardObject = forwardObject;
        }

        boolean isArray =false;
        int pointerPos;
        Object forwardObject;
    }


    public int toByteArray(Object onHeapStruct, byte bytes[], int index) throws Exception {
        ArrayList<ForwardEntry> positions = new ArrayList<ForwardEntry>();
        if ( onHeapStruct == null ) {
            return index;
        }
        int initialIndex =index;
        int clzId = getClzId(onHeapStruct.getClass());
        unsafe.putInt(bytes,FSTUtil.bufoff+index+4,clzId);
        index+=8;
        FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
        FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fis[i];
            if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                continue;
            if ( fi.getType().isArray() ) {
                if ( fi.getType().getComponentType().isArray() ) {
                    throw new RuntimeException("nested arrays not supported");
                }
                if ( fi.isIntegral() ) { // prim array
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    positions.add(new ForwardEntry(index,objectValue));
                    index += fi.getStructSize();
                } else { // object array
                    Object objArr[] = (Object[]) clInfo.getObjectValue(onHeapStruct, fi);
                    unsafe.putInt(bytes,FSTUtil.bufoff+index,objArr.length);
                    index+=4;
                    for (int j = 0; j < objArr.length; j++) {
                        Object objectValue = objArr[j];
                        if ( objectValue == null ) {
                            unsafe.putInt(bytes, FSTUtil.bufoff + index, -1);
                            index+=4;
                        } else {
                            positions.add(new ForwardEntry(index,objectValue));
                            index += 4;
                        }
                    }
                }
            } else if ( fi.isIntegral() ) { // && ! array
                Class type = fi.getType();
                if ( type == boolean.class ) {
                    unsafe.putBoolean(bytes, FSTUtil.bufoff + index, clInfo.getBooleanValue(onHeapStruct, fi));
                } else
                if ( type == byte.class ) {
                    unsafe.putByte(bytes, FSTUtil.bufoff + index, (byte) clInfo.getByteValue(onHeapStruct, fi));
                } else
                if ( type == char.class ) {
                    unsafe.putChar(bytes, FSTUtil.bufoff + index, (char) clInfo.getCharValue(onHeapStruct,fi));
                } else
                if ( type == short.class ) {
                    unsafe.putShort(bytes, FSTUtil.bufoff + index, (short) clInfo.getShortValue(onHeapStruct, fi));
                } else
                if ( type == int.class ) {
                    unsafe.putInt( bytes, FSTUtil.bufoff+index, clInfo.getIntValue(onHeapStruct,fi) );
                } else
                if ( type == long.class ) {
                    unsafe.putLong( bytes, FSTUtil.bufoff+index, clInfo.getLongValue(onHeapStruct,fi) );
                } else
                if ( type == float.class ) {
                    unsafe.putFloat(bytes, FSTUtil.bufoff + index, clInfo.getFloatValue(onHeapStruct, fi));
                } else
                if ( type == double.class ) {
                    unsafe.putDouble(bytes, FSTUtil.bufoff + index, clInfo.getDoubleValue(onHeapStruct, fi));
                } else {
                    throw new RuntimeException("this is an error");
                }
                index += fi.getStructSize();
            } else { // objectref
                Object obj = clInfo.getObjectValue(onHeapStruct, fi);
                if ( obj == null ) {
                    unsafe.putInt(bytes, FSTUtil.bufoff + index, -1);
                    index+=fi.getStructSize();
                } else {
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    positions.add(new ForwardEntry(index,objectValue));
                    index += fi.getStructSize();
                }
            }
        }
        for ( int i=0; i < positions.size(); i++) {
            ForwardEntry en = positions.get(i);
            Object o = en.forwardObject;
            if ( o == null ) {
                throw new RuntimeException("this is a bug");
            }
            Class c = o.getClass();
            if (c.isArray()) {
                int siz = 0;
                if ( c == byte[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTUtil.bufoff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == boolean[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTUtil.bufoff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == char[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == short[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == int[].class ) {
                    siz = Array.getLength(o) * FSTUtil.intscal;
                    unsafe.copyMemory(o,FSTUtil.intoff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == long[].class ) {
                    siz = Array.getLength(o) * FSTUtil.longscal;
                    unsafe.copyMemory(o,FSTUtil.longoff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == float[].class ) {
                    siz = Array.getLength(o) * FSTUtil.floatscal;
                    unsafe.copyMemory(o,FSTUtil.floatoff, bytes, FSTUtil.bufoff+index, siz);
                } else if ( c == double[].class ) {
                    siz = Array.getLength(o) * FSTUtil.doublescal;
                    unsafe.copyMemory(o,FSTUtil.doubleoff, bytes, FSTUtil.bufoff+index, siz);
                } else {
                    // object array treated like a sequence of object refs
                    int newoffset = toByteArray(o, bytes, index);
                    unsafe.putInt(bytes, FSTUtil.bufoff+en.pointerPos, index );
                    index = newoffset;
                }
                unsafe.putInt(bytes, FSTUtil.bufoff+en.pointerPos, index );
                unsafe.putInt(bytes, FSTUtil.bufoff+en.pointerPos+4, Array.getLength(o) );
                index+=siz;
            } else {
                int newoffset = toByteArray(o, bytes, index);
                unsafe.putInt(bytes, FSTUtil.bufoff+en.pointerPos, index );
                index = newoffset;
            }
        }
        if ( onHeapStruct instanceof FSTEmbeddedBinary ) {
            FSTEmbeddedBinary embeddedBinary = (FSTEmbeddedBinary) onHeapStruct;
            index = embeddedBinary.insertEmbedded(this, bytes, index);
        }
        unsafe.putInt(bytes,FSTUtil.bufoff+initialIndex,index-initialIndex); // set object size
        return index;
    }

    public int getShallowStructSize(Class clz) {
        return conf.getClassInfo(clz).getStructSize();
    }

}
