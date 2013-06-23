package de.ruedigermoeller.heapoff;

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
import java.util.Arrays;
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

    <T> Class<T> createStructClz( T toPack ) throws Exception {
        Class<?> clazz = toPack.getClass();
        String proxyName = clazz.getName()+"_Struct";
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
                if ( clInfo.getFieldInfo(method.getName(), null) != null ) {
                    structGen.defineArrayAccessor(clInfo.getFieldInfo(method.getName(), null), clInfo, method);
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
        Loader cl = new Loader(clazz.getClassLoader(), pool);
        cl.delegateLoadingOf(Unsafe.class.getName());
        cl.delegateLoadingOf(clazz.getName());
        cl.delegateLoadingOf(Externalizable.class.getName());
        cl.delegateLoadingOf(Serializable.class.getName());
        cl.delegateLoadingOf(FSTStructFactory.class.getName());
//        cl.delegateLoadingOf(SubTestStruct.class.getName());

        ccClz = cl.loadClass(cc.getName());


        return ccClz;
    }

    public Object getStructWrapper(byte b[], int offset) {
        return null;
    }

    int calcStructSize(Object onHeapStruct) throws IllegalAccessException, NoSuchFieldException {
        if ( onHeapStruct == null ) {
            return 0;
        }
        int siz = 0;
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

    int toOffHeap(Object onHeapStruct, byte bytes[], int offset) throws IllegalAccessException, NoSuchFieldException {
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
            if ( o == null )
                continue;
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
                int newoffset = toOffHeap(o,bytes,offset);
                unsafe.putInt(bytes, FSTUtil.bufoff+pointerPositions[i], offset );
                offset = newoffset;
            }
        }
        return offset;
    }
    public static class SubTestStruct implements Serializable {
        long id = 12345;
        int legs[] = {19,18,17,16};

        public int legs(int i) { return legs[i]; }
        public void legs(int i, int val) {legs[i] = val;}
    }

    public static class TestStruct implements Serializable {
        int intVar=64;
        boolean boolVar;
        int intarray[] = new int[10];
        SubTestStruct struct = new SubTestStruct();

        public TestStruct() {
            intarray[0] = Integer.MAX_VALUE-1;
            intarray[9] = Integer.MAX_VALUE;
        }

        public int getIntVar() {
            return intVar;
        }

        public void setIntVar(int intVar) {
            this.intVar = intVar;
        }

        public boolean isBoolVar() {
            return boolVar;
        }

        public void setBoolVar(boolean boolVar) {
            this.boolVar = boolVar;
        }

        public void intarray(int i, int val) {
            intarray[i] = val;
        }

        public int intarray( int i ) {
            return intarray[i];
        }

        public SubTestStruct getStruct() {
            return struct;
        }
    }

    public int getStructSize(Class clz) {
        return conf.getClassInfo(clz).getStructSize();
    }

    public static void main(String arg[] ) throws Exception {
        FSTStructFactory fac = new FSTStructFactory();

        fac.registerClz(TestStruct.class);
        fac.registerClz(SubTestStruct.class);

        TestStruct toPack = new TestStruct();
        int size = fac.calcStructSize(toPack);
        System.out.println("Size:"+size);
        byte bytes[] = new byte[size];
        fac.toOffHeap(toPack,bytes,0);

        Class<TestStruct> tc = fac.createStructClz(toPack);
        TestStruct testStruct = tc.newInstance();
        initStructInstance(fac, bytes, tc, testStruct);

        System.out.println("iarr oheap "+testStruct.intarray(0));
        System.out.println("iarr oheap "+testStruct.intarray(9));

        System.out.println("ivar " + testStruct.getIntVar());
        testStruct.setIntVar(9999);
        System.out.println("ivar " + testStruct.getIntVar());

        System.out.println("bool " + testStruct.isBoolVar());
        testStruct.setBoolVar(true);
        System.out.println("bool " + testStruct.isBoolVar());

        testStruct.intarray(3, 4444);
        System.out.println("POK " + testStruct.intarray(3));
        testStruct.intarray(9);

        SubTestStruct sub = testStruct.getStruct();

//        testStruct.setStringVar("Pok");
    }

    private static void initStructInstance(FSTStructFactory fac, byte[] bytes, Class<TestStruct> tc, TestStruct testStruct) throws IllegalAccessException, NoSuchFieldException {
        tc.getField("___fac").set(testStruct, fac);
        tc.getField("___bytes").set(testStruct, bytes);
        tc.getField("___unsafe").set(testStruct,unsafe);
        tc.getField("___offset").set(testStruct, FSTUtil.bufoff);
    }

}
