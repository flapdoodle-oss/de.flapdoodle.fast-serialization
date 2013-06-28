package de.ruedigermoeller.heapoff.structs.structtypes;

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
 * Date: 28.06.13
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructString implements Comparable {

    int testInt = 13;
    char chars[];

    public FSTStructString(int size) {
        chars = new char[size];
    }

    public FSTStructString(String s) {
        chars = s.toCharArray();
    }

    public FSTStructString(String oha, int i) {
        this(oha);
        testInt = i;
    }

    public int getTestInt() {
        return testInt;
    }

    public void setTestInt(int testInt) {
        this.testInt = testInt;
    }

    public void chars(int i, char val) {
        chars[i] = val;
    }

    public char chars(int i) {
        return chars[i];
    }

    public int compareTo(FSTStructString str) {
        int l1 = charsLen();
        int l2 = str.charsLen();
        int max = Math.min(l1, l2);
        int i = 0;
        while (i < max) {
            char c1 = chars(i);
            char c2 = str.chars(i);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
        }
        return l1 - l2;
    }

    public int charsLen() {
        return chars.length;
    }

    public String toString() {
        // fixme: optimize this by pointing directly to underlying array
        char ch[] = new char[charsLen()];
        for ( int i=0; i < charsLen(); i++ ) {
            ch[i] = chars(i);
        }
        return new String(ch);
    }

    @Override
    public int hashCode() {
        int len = charsLen();
        if ( len > 1)
            return chars(0)+chars(1)<<16+chars(len-1)<<32+chars(len-2)<<48;
        else if ( len > 0 )
            return chars(0);
        return 97979797;
    }

    public boolean equals( Object o ) {
        if ( o instanceof FSTStructString ) {
            FSTStructString ss = (FSTStructString) o;
            if ( ss.charsLen() != charsLen() ) {
                return false;
            }
            for ( int i = 0; i < ss.charsLen(); i++ ) {
                if ( ss.chars(i) != ss.chars(i) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        if ( o instanceof FSTStructString ) {
            return compareTo((FSTStructString)o);
        }
        return -1;
    }
}
