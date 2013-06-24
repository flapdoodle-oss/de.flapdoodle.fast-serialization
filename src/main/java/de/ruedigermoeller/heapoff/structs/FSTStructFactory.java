package de.ruedigermoeller.heapoff.structs;

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
import java.util.HashMap;

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

    static Unsafe unsafe = FSTUtil.unFlaggedUnsafe;

    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    FSTStructGeneration structGen = new FSTByteArrayUnsafeStructGeneration();
    HashMap<Class, Class> proxyClzMap = new HashMap<Class, Class>();
    Loader proxyLoader = new Loader(getClass().getClassLoader(), ClassPool.getDefault());

    <T> Class<T> createStructClz( Class<T> clazz ) throws Exception {
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
        //newClz.setInterfaces(new CtClass[]{pool.get(Externalizable.class.getName()), pool.get(FGRemoteObject.class.getName())});

        final FSTClazzInfo clInfo = conf.getClassInfo(clazz);
        structGen.defineStructFields(this, pool, newClz, clInfo);

        CtMethod[] methods = orig.getMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            final Class curClz = Class.forName( method.getDeclaringClass().getName() );
            boolean allowed = ((method.getModifiers() & AccessFlag.ABSTRACT) == 0 ) &&
                    (method.getModifiers() & AccessFlag.NATIVE) == 0 &&
                    (method.getModifiers() & AccessFlag.FINAL) == 0;
            if ( allowed ) {
                method = new CtMethod(method,newClz,null);
                String methName = method.getName();
                if ( clInfo.getFieldInfo(methName, null) != null ) {
                    structGen.defineArrayAccessor(clInfo.getFieldInfo(methName, null), clInfo, method);
                    newClz.addMethod(method);
                } else if ( methName.endsWith("Len") && clInfo.getFieldInfo(methName.substring(0, methName.length() - 3), null) != null ) {
                    structGen.defineArrayLength(clInfo.getFieldInfo(methName.substring(0, methName.length() - 3), null), clInfo, method);
                    newClz.addMethod(method);
                } else {
                    newClz.addMethod(method);
                    method.instrument( new ExprEditor() {
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            try {
                                if ( ! f.isStatic() ) {
                                    CtClass type = null;
                                    type = f.getField().getType();
                                    FSTClazzInfo.FSTFieldInfo fieldInfo = clInfo.getFieldInfo(f.getFieldName(), curClz);
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
        proxyLoader.delegateLoadingOf(Serializable.class.getName());
        proxyLoader.delegateLoadingOf(FSTStructFactory.class.getName());
//        proxyLoader.delegateLoadingOf(SubTestStruct.class.getName());

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

    public <T> T createWrapper(Class<T> onHeap, byte bytes[], int offset) throws Exception {
        Class proxy = getProxyClass(onHeap);
        T res = (T) unsafe.allocateInstance(proxy);
        proxy.getField("___fac").set(res, this);
        proxy.getField("___bytes").set(res, bytes);
        proxy.getField("___unsafe").set(res,unsafe);
        proxy.getField("___offset").set(res, FSTUtil.bufoff+offset);
        return res;
    }

    public Object createStructWrapper(byte b[], int offset) {
        int clzId = unsafe.getInt(b,FSTUtil.bufoff+offset);
        Class clazz = mIntToClz.get(clzId);
        if (clazz==null)
            throw new RuntimeException("unregistered class "+clzId);
        try {
            return createWrapper(clazz, b, offset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T toStruct(T onHeap) {
        try {
            byte b[] = toByteArray(onHeap);
            return (T)createWrapper(onHeap.getClass(),b,0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getStructWrapper(byte b[], int offset) {
//        int realOff = unsafe.getInt(b, FSTUtil.bufoff + offset);
        if ( offset < 0 )
            return null;
        return createStructWrapper(b,offset);
    }

    int calcStructSize(Object onHeapStruct) throws IllegalAccessException, NoSuchFieldException {
        if ( onHeapStruct == null ) {
            return 0;
        }
        int siz = 4;
        FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
        FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fis[i];
            if ( fi.getType().isArray() ) {
                if ( fi.getType().getComponentType().isArray() ) {
                    throw new RuntimeException("nested arrays not supported");
                }
                if ( fi.isIntegral() ) { // prim array
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    siz += Array.getLength(objectValue) * fi.getComponentStructSize() + fi.getStructSize();
                } else { // object array

                }
            } else if ( fi.isIntegral() ) { // && ! array
                siz += fi.getStructSize();
            } else { // objectref
                Object obj = clInfo.getObjectValue(onHeapStruct,fi);
                siz += fi.getStructSize()+calcStructSize(obj);
            }
        }
        return siz;
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

    public byte[] toByteArray(Object onHeapStruct) throws IllegalAccessException, NoSuchFieldException {
        int sz = calcStructSize(onHeapStruct);
        byte b[] = new byte[sz];
        toByteArray(onHeapStruct,b,0);
        return b;
    }

    public int toByteArray(Object onHeapStruct, byte bytes[], int offset) throws IllegalAccessException, NoSuchFieldException {
        int pointerPositions[] = new int[30];
        Object objects[] = new Object[30];
        int pointerPos = 0;
        if ( onHeapStruct == null ) {
            return offset;
        }
        int clzId = getClzId(onHeapStruct.getClass());
        unsafe.putInt(bytes,FSTUtil.bufoff+offset,clzId);
        offset+=4;
        FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
        FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fis[i];
            if ( fi.getType().isArray() ) {
                if ( fi.getType().getComponentType().isArray() ) {
                    throw new RuntimeException("nested arrays not supported");
                }
                if ( fi.isIntegral() ) { // prim array
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    objects[pointerPos] = objectValue;
                    pointerPositions[pointerPos] = offset;
                    pointerPos++;
                    offset += fi.getStructSize();
                } else { // object array

                }
            } else if ( fi.isIntegral() ) { // && ! array
                Class type = fi.getType();
                if ( type == boolean.class ) {
                    unsafe.putBoolean(bytes, FSTUtil.bufoff + offset, clInfo.getBooleanValue(onHeapStruct, fi));
                } else
                if ( type == byte.class ) {
                    unsafe.putByte(bytes, FSTUtil.bufoff + offset, (byte) clInfo.getByteValue(onHeapStruct, fi));
                } else
                if ( type == char.class ) {
                    unsafe.putChar(bytes, FSTUtil.bufoff + offset, (char) clInfo.getCharValue(onHeapStruct,fi));
                } else
                if ( type == short.class ) {
                    unsafe.putShort(bytes, FSTUtil.bufoff + offset, (short) clInfo.getShortValue(onHeapStruct, fi));
                } else
                if ( type == int.class ) {
                    unsafe.putInt( bytes, FSTUtil.bufoff+offset, clInfo.getIntValue(onHeapStruct,fi) );
                } else
                if ( type == long.class ) {
                    unsafe.putLong( bytes, FSTUtil.bufoff+offset, clInfo.getLongValue(onHeapStruct,fi) );
                } else
                if ( type == float.class ) {
                    unsafe.putFloat(bytes, FSTUtil.bufoff + offset, clInfo.getFloatValue(onHeapStruct, fi));
                } else
                if ( type == double.class ) {
                    unsafe.putDouble(bytes, FSTUtil.bufoff + offset, clInfo.getDoubleValue(onHeapStruct, fi));
                } else {
                    throw new RuntimeException("this is an error");
                }
                offset += fi.getStructSize();
            } else { // objectref
                Object obj = clInfo.getObjectValue(onHeapStruct, fi);
                Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                objects[pointerPos] = objectValue;
                pointerPositions[pointerPos] = offset;
                pointerPos++;
                offset += fi.getStructSize();
//                siz += fi.getStructSize()+calcStructSize(obj);
            }
        }
        for ( int i=0; i < pointerPos; i++) {
            Object o = objects[i];
            if ( o == null ) {
                unsafe.putInt(bytes, FSTUtil.bufoff+pointerPositions[i], -1 );
                continue;
            }
            Class c = o.getClass();
            if (c.isArray()) {
                int siz = 0;
                if ( c == byte[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTUtil.bufoff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == boolean[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTUtil.bufoff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == char[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == short[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == int[].class ) {
                    siz = Array.getLength(o) * FSTUtil.intscal;
                    unsafe.copyMemory(o,FSTUtil.intoff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == long[].class ) {
                    siz = Array.getLength(o) * FSTUtil.longscal;
                    unsafe.copyMemory(o,FSTUtil.longoff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == float[].class ) {
                    siz = Array.getLength(o) * FSTUtil.floatscal;
                    unsafe.copyMemory(o,FSTUtil.floatoff, bytes, FSTUtil.bufoff+offset, siz);
                } else if ( c == double[].class ) {
                    siz = Array.getLength(o) * FSTUtil.doublescal;
                    unsafe.copyMemory(o,FSTUtil.doubleoff, bytes, FSTUtil.bufoff+offset, siz);
                } else {
                    throw new RuntimeException("reference type not supported");
                }
                unsafe.putInt(bytes, FSTUtil.bufoff+pointerPositions[i], offset );
                unsafe.putInt(bytes, FSTUtil.bufoff+pointerPositions[i]+4, Array.getLength(o) );
                offset+=siz;
            } else {
                int newoffset = toByteArray(o, bytes, offset);
                unsafe.putInt(bytes, FSTUtil.bufoff+pointerPositions[i], offset );
                offset = newoffset;
            }
        }
        return offset;
    }

    public int getStructSize(Class clz) {
        return conf.getClassInfo(clz).getStructSize();
    }

}
