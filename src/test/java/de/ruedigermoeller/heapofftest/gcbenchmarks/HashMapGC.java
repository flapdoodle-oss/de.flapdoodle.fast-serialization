package de.ruedigermoeller.heapofftest.gcbenchmarks;

import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;

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
 * Date: 29.06.13
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
public class HashMapGC extends BasicGCBench {

    static class MyInt {
        int val;

        public MyInt(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        @Override
        public int hashCode() {
            return val;
        }

        public boolean equals(Object o) {
            return ((MyInt)o).getVal() == val;
        }

    }

    int size = 8000000;
    HashMap map;
    StructMap structMap;

    public void benchInt() {

        map = new HashMap(size);
        for ( int i=0; i<size; i++) {
            map.put(i,i);
        }
        System.out.println("GC HashMap<int,int> "+benchFullGC());
        map = null;
        System.out.println("freed HashMap<int,int> "+benchFullGC());

        structMap = new StructMap(size*2);
        for ( int i=0; i<size; i++) {
            structMap.put(i,i);
        }
        System.out.println("GC StructMap<int,int> "+benchFullGC());

        structMap = fac.toStruct(structMap);
        System.out.println("GC off heaped StructMap<int,int> "+benchFullGC());

        structMap = null;
        System.out.println("GC freed StructMap<int,int> "+benchFullGC());

    }

    public void benchMyInt() {

        map = new HashMap(size);
        for ( int i=0; i<size; i++) {
            map.put(new MyInt(i), new MyInt(i) );
        }
        System.out.println("GC HashMap<MyInt,MyInt> "+benchFullGC());
        map = null;
        System.out.println("freed HashMap<MyInt,MyInt> "+benchFullGC());

        structMap = new StructMap(size*2);
        for ( int i=0; i<size; i++) {
            structMap.put(new MyInt(i), new MyInt(i) );
        }
        System.out.println("GC StructMap<MyInt,MyInt> "+benchFullGC());

        structMap = fac.toStruct(structMap);
        System.out.println("GC off heaped StructMap<MyInt,MyInt> "+benchFullGC());

        structMap = null;
        System.out.println("GC freed StructMap<MyInt,MyInt> "+benchFullGC());

    }

    public static void main(String arg[]) {
        HashMapGC hashMapGC = new HashMapGC();
        hashMapGC.benchInt();
        hashMapGC.benchMyInt();
    }

}
