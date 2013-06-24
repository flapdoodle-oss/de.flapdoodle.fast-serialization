package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.heapoff.structs.FSTStructFactory;

import java.io.Serializable;

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
 * Date: 24.06.13
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */
public class TestStructs {

    public static class SubTestStruct implements Serializable {
        long id = 12345;
        int legs[] = {19,18,17,16};

        public long getId() {
            return id;
        }
        public int legs(int i) { return legs[i]; }
        public void legs(int i, int val) {legs[i] = val;}
        public int legsLen() { return legs.length; }
    }

    public static class TestStruct implements Serializable {
        int intVar=64;
        boolean boolVar;
        int intarray[] = new int[50];
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

        public boolean containsInt(int i) {
            for (int j = 0; j < intarrayLen(); j++) {
                int i1 = intarray(j);
                if ( i1 == i ) {
                    return true;
                }
            }
            return false;
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

        public int intarrayLen() {
            return intarray.length;
        }

        public SubTestStruct getStruct() {
            return struct;
        }
    }

    private static void benchAccess(TestStruct[] structs) {
        int times = 4;
        long tim;
        int sum;

        tim = System.currentTimeMillis();
        sum = 0;
        for (int ii=0;ii<times;ii++)
            for ( int i=0; i<structs.length; i++ ) {
                sum += structs[i].getIntVar();
            }
        System.out.println("  read int "+(System.currentTimeMillis()-tim));

        tim = System.currentTimeMillis();
        sum = 0;
        for (int ii=0;ii<times;ii++)
            for ( int i=0; i<structs.length; i++ ) {
                sum += structs[i].intarray(3);
            }
        System.out.println("  read int array[3]"+(System.currentTimeMillis()-tim));

        tim = System.currentTimeMillis();
        for (int ii=0;ii<times;ii++)
            for ( int i=0; i<structs.length; i++ ) {
                if ( structs[i].containsInt(77) ) {
                    sum = 0;
                }
            }
        System.out.println("  iter int from this "+(System.currentTimeMillis()-tim));

        tim = System.currentTimeMillis();
        sum = 0;
        for (int ii=0;ii<times;ii++)
            for ( int i=0; i<structs.length; i++ ) {
                sum += structs[i].getStruct().getId();
            }
        System.out.println("  read substructure int "+(System.currentTimeMillis()-tim));

        tim = System.currentTimeMillis();
        sum = 0;
        for (int ii=0;ii<times;ii++)
            for ( int i=0; i<structs.length; i++ ) {
                sum += structs[i].getStruct().legs(2);
            }
        System.out.println("  read substructure int[] "+(System.currentTimeMillis()-tim));
    }

    static FSTStructFactory fac = new FSTStructFactory();
    static TestStruct structs[] = new TestStruct[3000000];

    public static void main0(String arg[] ) throws Exception {

        long tim = System.currentTimeMillis();
        for (int i = 0; i < structs.length; i++) {
            structs[i] = new TestStruct();
        }
        System.out.println("instantiation on heap "+(System.currentTimeMillis()-tim));

        benchAccess(structs);

        tim = System.currentTimeMillis();
        for (int i = 0; i < structs.length; i++) {
            structs[i] = fac.toStruct(structs[i]);
        }
        System.out.println("moving off heap "+(System.currentTimeMillis()-tim));
        benchAccess(structs);

        TestStruct template = new TestStruct();
        byte[] bytes = fac.toByteArray(template);
        tim = System.currentTimeMillis();
        for (int i = 0; i < structs.length; i++) {
            byte b[] = new byte[bytes.length];
            System.arraycopy(bytes,0,b,0,b.length);
            structs[i] = (TestStruct) fac.getStructWrapper(b,0);
        }
        System.out.println("instantiate off heap new byte[] per object "+(System.currentTimeMillis()-tim));
        benchAccess(structs);

        tim = System.currentTimeMillis();
        byte hugeArray[] = new byte[bytes.length*structs.length];
        int off = 0;
        for (int i = 0; i < structs.length; i++) {
            System.arraycopy(bytes,0,hugeArray,off,bytes.length);
            structs[i] = (TestStruct) fac.getStructWrapper(hugeArray,off);
            off += bytes.length;
        }
        System.out.println("instantiate off heap single byte array "+(System.currentTimeMillis()-tim));
        benchAccess(structs);


//
//        System.out.println("iarr oheap "+testStruct.intarray(0));
//        System.out.println("iarr oheap "+testStruct.intarray(9));
//
//        System.out.println("ivar " + testStruct.getIntVar());
//        testStruct.setIntVar(9999);
//        System.out.println("ivar " + testStruct.getIntVar());
//
//        System.out.println("bool " + testStruct.isBoolVar());
//        testStruct.setBoolVar(true);
//        System.out.println("bool " + testStruct.isBoolVar());
//
//        testStruct.intarray(3, 4444);
//        System.out.println("POK " + testStruct.intarray(3));
//        testStruct.intarray(9);
//
//        SubTestStruct sub = testStruct.getStruct();
//        System.out.println("sub.id " + sub.getId());
//        System.out.println("sub.legs0 " + sub.legs(0));

//        testStruct.setStringVar("Pok");
    }

    public static void main(String arg[] ) throws Exception {
        fac.registerClz(TestStruct.class);
        fac.registerClz(SubTestStruct.class);
        System.out.println("WarmUp");
        main0(arg);
        structs = new TestStruct[3000000]; // prevent escape analysis
        System.gc();
        System.out.println("BENCH ------------------------------");
        main0(arg);
    }

}
