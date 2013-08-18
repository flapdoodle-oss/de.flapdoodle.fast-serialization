package de.ruedigermoeller.heapoff.structs.unsafeimpl;

import de.ruedigermoeller.heapoff.structs.*;
import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.util.FSTInt2ObjectMap;
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

    public static int SIZE_ALIGN = 2;
    static FSTStructFactory instance;

    public static FSTStructFactory getInstance() {
        if (instance==null) {
            instance = new FSTStructFactory();
        }
        return instance;
    }

    public static final int MAX_CLASSES = 1000;
    static Unsafe unsafe = FSTUtil.unFlaggedUnsafe;
    static FSTConfiguration conf = FSTConfiguration.createStructConfiguration();
    static Loader proxyLoader = new Loader(FSTStructFactory.class.getClassLoader(), ClassPool.getDefault());

    ConcurrentHashMap<Class, Class> proxyClzMap = new ConcurrentHashMap<Class, Class>();
    FSTStructGeneration structGen = new FSTByteArrayUnsafeStructGeneration();
    boolean autoRegister = true;

    public FSTStructFactory() {
        registerClz(FSTStruct.class);
        registerClz(StructString.class);
        registerClz(StructArray.class);
        registerClz(StructArray.StructArrIterator.class);
        registerClz(StructMap.class);
    }

    <T> Class<T> createStructClz( Class<T> clazz ) throws Exception {
        //FIXME: ensure FSTStruct is superclass, check protected, no private methods+fields
        if ( Modifier.isFinal(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers()) ) {
            throw new RuntimeException("Cannot add final classes to structs");
        }
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
            allowed &= (method.getModifiers() & AccessFlag.STATIC) == 0;
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
                // array access:
                //      void [name](int, type)
                //      [type] [name](int)
                FSTClazzInfo.FSTFieldInfo arrayFi = checkForSpecialArrayMethod(clInfo, method, "", null, null);
                // array length:
                //      int [name]Len()
                FSTClazzInfo.FSTFieldInfo lenfi = checkForSpecialArrayMethod(clInfo, method, "Len", CtClass.intType, new CtClass[0]);
                // get byte index of array data:
                //      int [name]Index()
                FSTClazzInfo.FSTFieldInfo indexfi = checkForSpecialArrayMethod(clInfo, method, "Index", CtClass.intType, new CtClass[0]);
                // get size of array element:
                //      int [name]ElementSize()
                FSTClazzInfo.FSTFieldInfo elemlen = checkForSpecialArrayMethod(clInfo, method, "ElementSize", CtClass.intType, new CtClass[0]);
                // CREATE non volatile pointer to array[0] element:
                //      type [name]Pointer() OR type [name]Pointer(pointerToSetup) (for reuse)
                FSTClazzInfo.FSTFieldInfo pointerfi = checkForSpecialArrayMethod(clInfo, method, "Pointer", null, null);
                // get byte index to structure or array header element:
                //      type [name]StructIndex()
                FSTClazzInfo.FSTFieldInfo structIndex = checkForSpecialMethod(clInfo, method, "StructIndex", CtClass.intType, new CtClass[0], false);
                // set with CAS
                //      boolean [name]CAS(expectedValue,value)
                FSTClazzInfo.FSTFieldInfo casAcc = checkForSpecialMethod(clInfo, method, "CAS", CtClass.booleanType, null, false);

                if ( casAcc != null ) {
                    structGen.defineStructSetCAS(casAcc, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( pointerfi != null ) {
                    structGen.defineArrayPointer(pointerfi, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( structIndex != null ) {
                    structGen.defineFieldStructIndex(structIndex, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( indexfi != null ) {
                    structGen.defineArrayIndex(indexfi, clInfo, method);
                    newClz.addMethod(method);
                } else
                if ( elemlen != null ) {
                    structGen.defineArrayElementSize(elemlen, clInfo, method);
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
                    method.instrument( new ExprEditor() {
                        @Override
                        public void edit(FieldAccess f) throws CannotCompileException {
                            try {
                                if ( ! f.isStatic() ) {
                                    CtClass type = null;
                                    type = f.getField().getType();
                                    FSTClazzInfo.FSTFieldInfo fieldInfo = clInfo.getFieldInfo(f.getFieldName(), null);
                                    if ( fieldInfo == null ) {
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

    FSTClazzInfo.FSTFieldInfo checkForSpecialArrayMethod( FSTClazzInfo clzInfo, CtMethod method, String postFix, Object returnType, CtClass requiredArgs[] ) {
        return checkForSpecialMethod(clzInfo, method, postFix, returnType, requiredArgs, true);
    }

    FSTClazzInfo.FSTFieldInfo checkForSpecialMethod(FSTClazzInfo clzInfo, CtMethod method, String postFix, Object returnType, CtClass requiredArgs[], boolean array) {
        int len = postFix.length();
        String methName = method.getName();
        if ( ! methName.endsWith(postFix) ) {
            return null;
        }
        FSTClazzInfo.FSTFieldInfo res = clzInfo.getFieldInfo(methName.substring(0, methName.length() - len), null);
        if ( res == null ) {
            return null;
        }
        if ( array && res.isArray() && res.getArrayType().isArray() ) {
            throw new RuntimeException("nested arrays not supported "+res.getDesc());
        }
        if ( array && !res.isArray() ) {
            //throw new RuntimeException("expect array type for field "+res.getDesc()+" special method:"+method);
            // just ignore
            return null;
        }
        if ( res.isArray() || ! array ) {
            if ( returnType instanceof Class ) {
                try {
                    if ( ! method.getReturnType().getName().equals(((Class) returnType).getName()) ) {
                        throw new RuntimeException("expected method "+method+" to return "+returnType );
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            } else if ( returnType instanceof CtClass ) {
                try {
                    if ( ! method.getReturnType().equals(returnType) ) {
                        throw new RuntimeException("expected method "+method+" to return "+returnType );
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
            return res;
        }
        return null;
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
//        proxyLoader.delegateLoadingOf(StructArray.class.getName());
//        proxyLoader.delegateLoadingOf(StructArray.StructArrIterator.class.getName());
        ccClz = proxyLoader.loadClass(cc.getName());
        return ccClz;
    }

    public Class getProxyClass(Class clz) throws Exception {
        synchronized (this) {
            Class res = proxyClzMap.get(clz);
            if ( res == null ) {
                res = createStructClz(clz);
                proxyClzMap.put(clz,res);
            }
            return res;
        }
    }

    public <T extends FSTStruct> T createWrapper(Class<T> onHeap, byte bytes[], int index) throws Exception {
        Class proxy = getProxyClass(onHeap);
        T res = (T) unsafe.allocateInstance(proxy);
        res.baseOn(bytes, FSTStruct.bufoff+index, this);
        return res;
    }

    public FSTStruct createStructWrapper(byte b[], int index) {
        int clzId = unsafe.getInt(b, FSTStruct.bufoff + index + 4);
        return createStructPointer(b, index, clzId);
    }

    public FSTStruct createStructPointer(byte[] b, int index, int clzId) {
        synchronized (this) {
            Class clazz = mIntToClz.get(clzId);
            if (clazz==null)
                throw new RuntimeException("unregistered class "+clzId);
            try {
                return (FSTStruct) createWrapper(clazz, b, index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public FSTStruct createTypedArrayBasePointer(byte base[], long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = unsafe.getInt(base, objectBaseOffset + arrayStructIndex);
        int elemSiz = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+8);
        int len = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+4);
        int clId = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+12);
        FSTStruct structPointer = null;
        if ( clId <= 0 ) { // untyped array
            structPointer = new FSTStruct();
            structPointer.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
        } else {
            structPointer = createStructPointer(base, (int) (objectBaseOffset+arrayElementZeroindex- FSTStruct.bufoff), clId);
        }
        structPointer.___elementSize = elemSiz;
        return structPointer;
    }

    public void fillTypedArrayBasePointer(FSTStruct result, byte base[], long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = unsafe.getInt(base,objectBaseOffset+arrayStructIndex);
        int elemSiz = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+8);
//        int len = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+4);
//        int clId = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+12);
        result.baseOn(base, objectBaseOffset + arrayElementZeroindex, this);
        result.___elementSize = elemSiz;
    }

    public void fillPrimitiveArrayBasePointer(FSTStruct result, byte base[], long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = unsafe.getInt(base,objectBaseOffset+arrayStructIndex);
        result.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
    }

    public FSTStruct createPrimitiveArrayBasePointer(byte base[], long objectBaseOffset /*offset of object containing array*/, int arrayStructIndex /*position of array header in struct*/) {
        int arrayElementZeroindex = unsafe.getInt(base,objectBaseOffset+arrayStructIndex);
//        int len = unsafe.getInt(base,objectBaseOffset+arrayStructIndex+4);
        FSTStruct structPointer = new FSTStruct();
        structPointer.baseOn(base,objectBaseOffset+arrayElementZeroindex,this);
        return structPointer;
    }

    public <T extends FSTStruct> StructArray<T> toStructArray(int size, T onHeap) {
        StructArray<T> arr = new StructArray<T>(size,onHeap);
        return toStruct(arr);
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

    public void detach(FSTStruct structPointer) {
        int id = structPointer.getClzId();
        Object o = cachedWrapperMap.get()[id];
        if ( o == structPointer )
            cachedWrapperMap.get()[id] = null;
    }

    public Object getStructPointerByOffset(byte b[], long offset) {
        if ( offset < FSTStruct.bufoff ) {
            return null;
        }
        int clzId = unsafe.getInt(b, offset+4);
        int ptr = unsafe.getInt(b, offset);
        if (clzId <= 0) {
            return null;
        }
        Object[] wrapperMap = cachedWrapperMap.get();
        Object res = wrapperMap[clzId];
        if ( res != null ) {
            ((FSTStruct)res).baseOn(b, offset, this);
            return res;
        }
        res = createStructPointer(b, (int) (offset - FSTStruct.bufoff), clzId);
        wrapperMap[clzId] = res;
        return res;
    }

    public Object getStructPointer(byte b[], int index) {
        return getStructPointerByOffset(b,FSTStruct.bufoff+index);
    }

    public static int align(int val, int align) {
        while( val%align != 0 )
            val++;
        return val;
    }

    public int calcStructSize(FSTStruct onHeapStruct) {
        try {
            if ( onHeapStruct == null ) {
                return 0;
            }
            if (onHeapStruct.isOffHeap())
                return onHeapStruct.getByteSize();
            int siz = 8;
            FSTClazzInfo clInfo = conf.getClassInfo(onHeapStruct.getClass());
            FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
            for (int i = 0; i < fis.length; i++) {
                FSTClazzInfo.FSTFieldInfo fi = fis[i];
                if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                    continue;
                int modifiers = fi.getField().getModifiers();
                if ( ! Modifier.isProtected(modifiers) && ! Modifier.isPublic(modifiers) )
                    throw new RuntimeException("all fields of a structable class must be public or protected. Field:"+fi.getField().getName()+" in class "+fi.getField().getDeclaringClass().getName() );
                // FIXME: check for null refs, check for FSTStruct subclasses
                if ( fi.getType().isArray() ) {
                    if ( fi.getType().getComponentType().isArray() ) {
                        throw new RuntimeException("nested arrays not supported");
                    }
                    // if array is @aligned, add align-1 to size (overestimation), because I don't know the exact position of the array data here
                    // embedded object data, is currently not aligned, only the header position respects the @align
                    if ( fi.isIntegral() ) { // prim array
                        Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                        if ( objectValue == null ) {
                            throw new RuntimeException("arrays in struct templates must not be null !");
                        }
                        siz += Array.getLength(objectValue) * fi.getComponentStructSize() + fi.getStructSize() + fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                    } else { // object array
                        Object objectValue[] = (Object[]) clInfo.getObjectValue(onHeapStruct, fi);
                        if (objectValue==null) {
                            siz+=fi.getStructSize()+fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                        } else {
                            int elemSiz = computeElemSize(onHeapStruct,objectValue, fi);
                            siz += Array.getLength(objectValue) * elemSiz + fi.getStructSize() + fi.getAlignPad()+(fi.getAlign()>0?fi.getAlign()-1:0);
                        }
                    }
                } else if ( fi.isIntegral() ) { // && ! array
                    siz += fi.getStructSize();
                } else { // objectref
                    FSTStruct obj = (FSTStruct) clInfo.getObjectValue(onHeapStruct,fi);
                    siz += fi.getStructSize()+calcStructSize(obj)+fi.getAlignPad();
                }
            }
            if ( onHeapStruct instanceof FSTEmbeddedBinary) {
                siz+=((FSTEmbeddedBinary) onHeapStruct).getEmbeddedSizeAdditon(this);
            }
            return siz;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    protected int computeElemSize(Object container, Object[] objectValue, FSTClazzInfo.FSTFieldInfo fi) {
        if ( container instanceof FSTArrayElementSizeCalculator ) {
            int res = ((FSTArrayElementSizeCalculator) container).getElementSize(fi.getField(),this);
            if ( res >= 0 )
                return res;
        }
        Templated annotation = fi.getField().getAnnotation(Templated.class);
        if ( annotation != null ) {
            Object template = objectValue[0];
            return align(calcStructSize((FSTStruct) template),SIZE_ALIGN);
        }
        int elemSiz = 0;
        for (int j = 0; j < objectValue.length; j++) {
            Object o = objectValue[j];
            if ( o != null )
                elemSiz=Math.max( elemSiz, calcStructSize((FSTStruct) o) );
        }
        return align(elemSiz,SIZE_ALIGN);
    }

    FSTInt2ObjectMap<Class> mIntToClz = new FSTInt2ObjectMap<Class>(97);
    HashMap<Class,Integer> mClzToInt = new HashMap<Class,Integer>();

    int idCount = 1;
    public void registerClz(Class ... classes) {
        for (int i = 0; i < classes.length; i++) {
            Class c = classes[i];
            if ( mClzToInt.containsKey(c) ) {
                continue;
            }
            int id = idCount++;
            mIntToClz.put(id,c);
            mClzToInt.put(c,id);
        }
    }

    public int getClzId(Class c) {
        Integer integer = mClzToInt.get(c);
        if (autoRegister && integer == null && c != null ) {
            if ( c.getName().endsWith("_Struct") )
                return getClzId(c.getSuperclass());
            registerClz(c);
            return getClzId(c);
        }
        return integer == null ? 0: integer;
    }

    public Class getClazz(int clzId) {
        return mIntToClz.get(clzId);
    }

    public byte[] toByteArray(FSTStruct onHeapStruct) {
        try {
            int sz = align(calcStructSize(onHeapStruct),SIZE_ALIGN);
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

        ForwardEntry(int pointerPos, Object forwardObject, FSTClazzInfo.FSTFieldInfo fsfi) {
            this.pointerPos = pointerPos;
            this.forwardObject = forwardObject;
            fi = fsfi;
        }

        FSTClazzInfo.FSTFieldInfo fi;
        int pointerPos;
        Object forwardObject;
        FSTStruct template;
    }


    public int toByteArray(FSTStruct onHeapStruct, byte bytes[], int index) throws Exception {
        ArrayList<ForwardEntry> positions = new ArrayList<ForwardEntry>();
        if ( onHeapStruct == null ) {
            return index;
        }
        if (onHeapStruct.isOffHeap()) {
            unsafe.copyMemory(onHeapStruct.___bytes,onHeapStruct.___offset,bytes,FSTStruct.bufoff+index,onHeapStruct.getByteSize());
            return onHeapStruct.getByteSize();
        }
        int initialIndex =index;
        Class<? extends FSTStruct> aClass = onHeapStruct.getClass();
        int clzId = getClzId(aClass);
        unsafe.putInt(bytes,FSTStruct.bufoff+index+4,clzId);
        index+=8;
        FSTClazzInfo clInfo = conf.getClassInfo(aClass);
        FSTClazzInfo.FSTFieldInfo fis[] = clInfo.getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fis[i];
            if ( fi.getField().getDeclaringClass() == FSTStruct.class )
                continue;
            index+=fi.getAlignPad();
            if ( fi.getType().isArray() ) {
                if ( fi.getType().getComponentType().isArray() ) {
                    throw new RuntimeException("nested arrays not supported");
                }
                if ( fi.isIntegral() ) { // prim array
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    positions.add(new ForwardEntry(index,objectValue,fi));
                    index += fi.getStructSize();
                } else { // object array
                    Object objArr[] = (Object[]) clInfo.getObjectValue(onHeapStruct, fi);
                    if ( objArr == null ) {
                        unsafe.putInt(bytes, FSTStruct.bufoff + index, -1);
                        index+=fi.getStructSize();
                    } else {
                        Templated takeFirst = fi.getField().getAnnotation(Templated.class);
                        ForwardEntry fe = new ForwardEntry(index, objArr, fi);
                        if ( takeFirst != null ) {
                            fe.template = (FSTStruct) objArr[0];
                        }
                        positions.add(fe);
                        index += fi.getStructSize();
                        int elemSiz = computeElemSize(onHeapStruct,objArr,fi);
                        unsafe.putInt(bytes, FSTStruct.bufoff+index-8,elemSiz);
                    }
                }
            } else if ( fi.isIntegral() ) { // && ! array
                Class type = fi.getType();
                int structIndex = fi.getStructOffset();
                if ( index != structIndex+initialIndex ) {
                    throw new RuntimeException("internal error. please file an issue");
                }
                if ( type == boolean.class ) {
                    unsafe.putByte(bytes, FSTStruct.bufoff + index, (byte) (clInfo.getBooleanValue(onHeapStruct, fi)?1:0));
                } else
                if ( type == byte.class ) {
                    unsafe.putByte(bytes, FSTStruct.bufoff + index, (byte) clInfo.getByteValue(onHeapStruct, fi));
                } else
                if ( type == char.class ) {
                    unsafe.putChar(bytes, FSTStruct.bufoff + index, (char) clInfo.getCharValue(onHeapStruct,fi));
                } else
                if ( type == short.class ) {
                    unsafe.putShort(bytes, FSTStruct.bufoff + index, (short) clInfo.getShortValue(onHeapStruct, fi));
                } else
                if ( type == int.class ) {
                    unsafe.putInt( bytes, FSTStruct.bufoff+index, clInfo.getIntValue(onHeapStruct,fi) );
                } else
                if ( type == long.class ) {
                    unsafe.putLong( bytes, FSTStruct.bufoff+index, clInfo.getLongValue(onHeapStruct,fi) );
                } else
                if ( type == float.class ) {
                    unsafe.putFloat(bytes, FSTStruct.bufoff + index, clInfo.getFloatValue(onHeapStruct, fi));
                } else
                if ( type == double.class ) {
                    unsafe.putDouble(bytes, FSTStruct.bufoff + index, clInfo.getDoubleValue(onHeapStruct, fi));
                } else {
                    throw new RuntimeException("this is an error");
                }
                index += fi.getStructSize();
            } else { // objectref
                Object obj = clInfo.getObjectValue(onHeapStruct, fi);
                int structIndex = fi.getStructOffset();
                if ( index != structIndex+initialIndex ) {
                    throw new RuntimeException("internal error. please file an issue");
                }
                if ( obj == null ) {
                    unsafe.putInt(bytes, FSTStruct.bufoff + index, -1);
                    unsafe.putInt(bytes, FSTStruct.bufoff + index+4, -1);
                    index+=fi.getStructSize();
                } else {
                    Object objectValue = clInfo.getObjectValue(onHeapStruct, fi);
                    positions.add(new ForwardEntry(index,objectValue,fi));
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
                if ( en.fi.getAlign() > 0 ) {
                    while( (index%en.fi.getAlign()) != 0 ) {
                        index++;
                    }
                }
                long siz = 0;
                if ( c == byte[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTStruct.bufoff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == boolean[].class ) {
                    siz = Array.getLength(o);
                    unsafe.copyMemory(o,FSTStruct.bufoff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == char[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == short[].class ) {
                    siz = Array.getLength(o) * FSTUtil.chscal;
                    unsafe.copyMemory(o,FSTUtil.choff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == int[].class ) {
                    siz = Array.getLength(o) * FSTUtil.intscal;
                    unsafe.copyMemory(o,FSTUtil.intoff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == long[].class ) {
                    siz = Array.getLength(o) * FSTUtil.longscal;
                    unsafe.copyMemory(o,FSTUtil.longoff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == float[].class ) {
                    siz = Array.getLength(o) * FSTUtil.floatscal;
                    unsafe.copyMemory(o,FSTUtil.floatoff, bytes, FSTStruct.bufoff+index, siz);
                } else if ( c == double[].class ) {
                    siz = Array.getLength(o) * FSTUtil.doublescal;
                    unsafe.copyMemory(o,FSTUtil.doubleoff, bytes, FSTStruct.bufoff+index, siz);
                } else {
                    Object[] objArr = (Object[]) o;
                    int elemSiz = unsafe.getInt(bytes, FSTStruct.bufoff+en.pointerPos+8);
                    siz = Array.getLength(o) * elemSiz;
                    int tmpIndex = index;
                    byte templatearr[] = null;
                    boolean hasClzId = false;
                    if ( onHeapStruct instanceof FSTArrayElementSizeCalculator ) {
                        Class elemClz = ((FSTArrayElementSizeCalculator)onHeapStruct).getElementType(en.fi.getField(),this);
                        if ( elemClz != null ) {
                            int clid = getClzId(elemClz);
                            unsafe.putInt(bytes,FSTStruct.bufoff+en.pointerPos+12, clid );
                            hasClzId = true;
                        }
                    }
                    if (en.template != null) {
                        templatearr = toByteArray(en.template); // fixme: unnecessary alloc
                        if ( ! hasClzId ) {
                            unsafe.putInt(bytes,FSTStruct.bufoff+en.pointerPos+12, getClzId( en.template.getClass() ) );
                            hasClzId = true;
                        }
                    }
                    for (int j = 0; j < objArr.length; j++) {
                        Object objectValue = objArr[j];
                        if ( templatearr != null ) {
                            unsafe.copyMemory(templatearr,FSTStruct.bufoff,bytes,FSTStruct.bufoff+tmpIndex,templatearr.length);
                            tmpIndex+=elemSiz;
                        } else {
                            if ( objectValue == null ) {
                                unsafe.putInt(bytes, FSTStruct.bufoff + tmpIndex+4, -1);
                                tmpIndex += elemSiz;
                            } else {
                                toByteArray((FSTStruct) objectValue, bytes, tmpIndex);
                                unsafe.putInt( bytes,FSTStruct.bufoff+tmpIndex, elemSiz ); // need to patch size in case of smaller objects in obj array
                                tmpIndex += elemSiz;
                                if ( !hasClzId ) {
                                    unsafe.putInt(bytes,FSTStruct.bufoff+en.pointerPos+12, getClzId( en.fi.getArrayType() ) );
                                    hasClzId = true;
                                }
                            }
                        }
                    }
                }
                unsafe.putInt(bytes, FSTStruct.bufoff+en.pointerPos, index-initialIndex );
                unsafe.putInt(bytes, FSTStruct.bufoff+en.pointerPos+4, Array.getLength(o) );
                index+=siz;
            } else { // object ref or objarray elem
                int newoffset = toByteArray((FSTStruct) o, bytes, index);
                unsafe.putInt(bytes, FSTStruct.bufoff+en.pointerPos, index-initialIndex );
                index = newoffset;
            }
        }
        if ( onHeapStruct instanceof FSTEmbeddedBinary ) {
            FSTEmbeddedBinary embeddedBinary = (FSTEmbeddedBinary) onHeapStruct;
            index = embeddedBinary.insertEmbedded(this, bytes, index);
        }
        unsafe.putInt(bytes,FSTStruct.bufoff+initialIndex,index-initialIndex); // set object size
        return index;
    }

    public int getShallowStructSize(Class clz) {
        return conf.getClassInfo(clz).getStructSize();
    }

}
