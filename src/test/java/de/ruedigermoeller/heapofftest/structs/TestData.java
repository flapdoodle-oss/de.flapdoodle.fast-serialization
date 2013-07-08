package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;

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
 * Date: 07.07.13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class TestData extends FSTStruct {

    TestData nested;
    StructString string = new StructString(50);
    StructArray dataStructArray;

    byte a = 0;
    short b = 11111;
    char c = 22222;
    int d = 333333333;
    long e = 444444444444l;
    float f = 5555555555.55f;
    double g = 66666666666.66;

    public byte getA() {
        return a;
    }

    public short getB() {
        return b;
    }

    public char getC() {
        return c;
    }

    public int getD() {
        return d;
    }

    public long getE() {
        return e;
    }

    public float getF() {
        return f;
    }

    public double getG() {
        return g;
    }

    byte[] arrA = "blablablablabla".getBytes();
    short[] arrb = { 11111, 22222, 3333 };
    char[] arrc = { 22221, 22221, 22222, 1 };
    int[] arrd = { 33333331, 33333332, 33333333, 1, 1 } ;
    long[] arre = { 444444444441l, 444444444442l, 444444444443l, 1,1 ,1 };
    float[] arrf = { 5555555555.51f, 5555555555.52f, 5555555555.53f, 1,1,1,1 };
    double[] arrg = { 66666666666.61, 66666666666.62,66666666666.63,1,1,1,1,1};

    public byte arrA(int index) { return arrA[index]; }
    public short arrb(int index) { return arrb[index]; }
    public char arrc(int index) { return arrc[index]; }
    public int arrd(int index) { return arrd[index]; }
    public long arre(int index) { return arre[index]; }
    public float arrf(int index) { return arrf[index]; }
    public double arrg(int index) { return arrg[index]; }

    public void arrA(int index, byte val) { arrA[index] = val; }
    public void arrb(int index, short val ) { arrb[index] = val; }
    public void arrc(int index, char val ) { arrc[index] = val; }
    public void arrd(int index, int val) { arrd[index] = val; }
    public void arre(int index, long val) { arre[index] = val; }
    public void arrf(int index, float val) { arrf[index] = val; }
    public void arrg(int index, double val ) { arrg[index] = val; }

    public int arrALen() { return arrA.length; }
    public int arrbLen() { return arrb.length; }
    public int arrcLen() { return arrc.length; }
    public int arrdLen() { return arrd.length; }
    public int arreLen() { return arre.length; }
    public int arrfLen() { return arrf.length; }
    public int arrgLen() { return arrg.length; }

    public StructArray getDataStructArray() {
        return dataStructArray;
    }

    public StructString getString() {
        return string;
    }

    public TestData getNested() { // Workaround Bug: referencing self does not work with javaassist
        return (TestData) _getNested();
    }
    public Object _getNested() { return nested; }

    public void setNested(TestData nested) {
        this.nested = nested;
    }
}

