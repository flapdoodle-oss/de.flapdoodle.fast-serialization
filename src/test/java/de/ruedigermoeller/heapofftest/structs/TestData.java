package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.Templated;
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

    protected TestData nested;
    protected StructString string = new StructString(50);
    protected StructArray dataStructArray;

    protected byte a = 0;
    protected short b = 11111;
    protected char c = 22222;
    protected int d = 333333333;
    protected long e = 444444444444l;
    protected float f = 5555555555.55f;
    protected double g = 66666666666.66;

    protected Object objArray = new Object[] { new StructString(5), new StructString(10), new StructString(20)};
    @Templated
    protected Object templatedObjArray = new Object[] { new StructString(5), null, null, null };

    protected StructString typedArray[] = new StructString[] { null, new StructString("One"), new StructString("Two"), new StructString("3", 10), new StructString("Four") };

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

    protected byte[] arrA = "blablablablabla".getBytes();
    protected short[] arrb = { 11111, 22222, 3333 };
    protected char[] arrc = { 22221, 22221, 22222, 1 };
    protected int[] arrd = { 33333331, 33333332, 33333333, 1, 1 } ;
    protected long[] arre = { 444444444441l, 444444444442l, 444444444443l, 1,1 ,1 };
    protected float[] arrf = { 5555555555.51f, 5555555555.52f, 5555555555.53f, 1,1,1,1 };
    protected double[] arrg = { 66666666666.61, 66666666666.62,66666666666.63,1,1,1,1,1};

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

    public void setString( StructString s ) {
        string = s;
    }

    public TestData getNested() {
        return nested;
    }

    public void setNested(TestData nested) {
        this.nested = nested;
    }
}

