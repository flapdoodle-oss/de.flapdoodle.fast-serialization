package de.ruedigermoeller.heapoff.structs.structtypes;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.util.Iterator;
import java.util.Map;

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
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

/**
 * a simple open adressed hashmap, which allows read access when embedded in structs. Note it is fixed size.
 * @param <K>
 * @param <V>
 */
public class StructMap<K,V> extends FSTStruct {

    Object keyVal[];
    int    size;

    /**
     * creates a new Hashtable with 'entrySize' elements allocated
     */
    public StructMap(int entrySize)
    {
        entrySize = Math.max(3, entrySize);
        keyVal    = new Object[entrySize * 2];
    }

    /**
     * creates a new Hashtable with 3 elements allocated
     */
    public StructMap()
    {
        this(3);
    }

    public StructMap(Map<K, V> toCopy)
    {
        this(toCopy.size()*2);
        for (Iterator iterator = toCopy.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry next = (Map.Entry) iterator.next();
            put((K)next.getKey(),(V)next.getValue());
        }
    }

    protected int locateIndex(Object key)
    {
        if (size >= getCapacity()-1)
        {
            throw new RuntimeException("Map is full");
        }
        int kvlen = keyValLen();
        int hpos = 2 * ((key.hashCode() & 0x7FFFFFFF) % (kvlen>>1));
        int pos = hpos;
        Object o = keyVal(pos);
        while ( o != null )
        {
            if (key.equals(o))
                break;
            pos = (pos + 2) % kvlen;
            o = keyVal(pos);
        }
        return pos / 2;
    }

    public int size()
    {
        return size;
    }

    public V get(Object key)
    {
        int    pos = locateIndex(key);
        Object res = keyAt(pos) != null ? valueAt(pos) : null;
        return (V) res;
    }

    public V put(K key, V value)
    {
        if ( key == null ) {
            throw new RuntimeException("Illegal Argument key is null");
        }
        if ( value == null ) {
            throw new RuntimeException("Illegal Argument value is null");
        }
        Object tmp = null;
        if (key != null)
        {
            int     pos    = locateIndex(key);
            if ( keyAt(pos) == null )
                size++;

            tmp = valueAt(pos);
            setKeyValue(pos, key, value);
        }
        return (V) tmp;
    }

    public K keyAt(int i)
    {
        return (K) keyVal(i << 1);
    }

    public V valueAt(int i)
    {
        return (V) keyVal((i << 1) + 1);
    }

    void setKeyValue(int i, K key, V value)
    {
        keyVal(i << 1,key);
        keyVal((i << 1) + 1, value);
    }

    public int getCapacity()
    {
        return keyValLen()/2;
    }

    public Object keyVal(int i) {
        return keyVal[i];
    }

    public void keyVal(int i, Object v) {
        keyVal[i] = v;
    }

    public int keyValIndex() {
        return -1; // will be redefined off-heap and deliver startindex of array
    }

    public int keyValLen() {
        return keyVal.length;
    }

    public static void main(String[] args)
    {
        StructMap<Integer,Integer> smt = new StructMap(8000);

        for (int ii = 0; ii < 4000; ii++)
        {
            smt.put(ii,ii);
        }

        for (int ii = 0; ii < 4000; ii++)
        {
            if ( smt.get(ii).intValue() != ii )
                System.out.println("BUG");
        }

    }

}
