package de.ruedigermoeller.heapoff.structs.structtypes;

import de.ruedigermoeller.heapoff.structs.FSTArrayElementSizeCalculator;
import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.Templated;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;

import java.lang.reflect.Field;
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
public class StructMap<K,V> extends FSTStruct implements FSTArrayElementSizeCalculator {

    transient FSTStruct keyTemplate, valueTemplate;

    protected Object keys[];
    protected Object vals[];
    protected int    size;
    protected transient FSTStruct pointer;

    public StructMap( FSTStruct keyTemplate, FSTStruct valueTemplate, int numelems ) {
        this(numelems);
        this.keyTemplate = keyTemplate;
        this.valueTemplate = valueTemplate;
    }

    /**
     * creates a new Hashtable with 'entrySize' elements allocated on heap. Note that off-heaping before filling elements will result
     * in an element bucket size of zero rendering this map unusable off heap. Use the templated constructore to
     * create an empty OffHeap Map.
     */
    public StructMap(int numElems)
    {
        numElems = Math.max(3, numElems);
        keys    = new Object[numElems*2];
        vals = new Object[numElems*2];
    }

    public StructMap(Map<K, V> toCopy)
    {
        this(toCopy.size()*2);
        for (Iterator iterator = toCopy.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry next = (Map.Entry) iterator.next();
            put((K)next.getKey(),(V)next.getValue());
        }
    }

    protected int keysStructIndex() {
        return -1; // generated
    }

    protected int valsStructIndex() {
        return -1; // generated
    }

    protected int locateIndex(Object key)
    {
        if (size >= getCapacity()-1)
        {
            throw new RuntimeException("Map is full");
        }
        if ( pointer != null || isOffHeap() ) {
            long arrbase = ___offset+keysStructIndex();
            int kvlen = unsafe.getInt(___bytes,arrbase+4);
            int kelemsiz = unsafe.getInt(___bytes,arrbase+8);
            if ( pointer == null )
                pointer = ___fac.createStructPointer(___bytes,0,unsafe.getInt(___bytes,arrbase+12) );

            int pos = ((key.hashCode() & 0x7FFFFFFF) % kvlen);
            pointer.___offset = ___offset+unsafe.getInt(___bytes,arrbase)+pos*kelemsiz;
            while ( pointer.getInt() > 0 )
            {
                if (key.equals(pointer))
                    break;
                pos++;
                pointer.next(kelemsiz);
                if ( pos >= kvlen ) {
                    pos = 0;
                    pointer.___offset = ___offset+unsafe.getInt(___bytes,arrbase);
                }
            }
            return pos;
        } else {
            int kvlen = keysLen();
            int pos = ((key.hashCode() & 0x7FFFFFFF) % kvlen);
            Object o = keys(pos);
            while ( o != null )
            {
                if (key.equals(o))
                    break;
                pos++;
                if ( pos >= kvlen )
                    pos = 0;
                o = keys(pos);
            }
            return pos;
        }
    }

    public int size()
    {
        return size;
    }

    public V get(Object key)
    {
        int    pos = locateIndex(key);
        Object res = keys(pos) != null ? vals(pos) : null;
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
            if ( keys(pos) == null )
                size++;

            tmp = vals(pos);
            setKeyValue(pos, key, value);
        }
        return (V) tmp;
    }

    protected void setKeyValue(int i, K key, V value)
    {
        keys(i, key);
        vals(i, value);
    }

    public int getCapacity()
    {
        return keysLen();
    }

    public Object keys(int i) {
        return keys[i];
    }

    public Object vals(int i) {
        return vals[i];
    }

    public void keys(int i, Object v) {
        keys[i] = v;
    }

    public void vals(int i, Object v) {
        vals[i] = v;
    }

    public int keyValIndex() {
        return -1; // will be redefined off-heap and deliver startindex of array
    }

    public int keysLen() {
        return keys.length;
    }

    public int valsLen() {
        return vals.length;
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

    @Override
    public int getElementSize(Field arrayRef, FSTStructFactory fac) {
        if ( keyTemplate != null && "keys".equals(arrayRef.getName()) ) {
            return fac.calcStructSize(keyTemplate);
        }
        if ( valueTemplate != null && "vals".equals(arrayRef.getName()) ) {
            return fac.calcStructSize(valueTemplate);
        }
        return -1;
    }

    @Override
    public Class<? extends FSTStruct> getElementType(Field arrayRef, FSTStructFactory fac) {
        if ( keyTemplate != null && "keys".equals(arrayRef.getName()) ) {
            return keyTemplate.getClass();
        }
        if ( valueTemplate != null && "vals".equals(arrayRef.getName()) ) {
            return valueTemplate.getClass();
        }
        return null;
    }
}
