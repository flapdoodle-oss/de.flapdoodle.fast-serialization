/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */

package de.ruedigermoeller.serialization.util;


import java.util.HashMap;

/**
 */
public final class FSTObject2ObjectMap<K,V> implements Cloneable, java.io.Serializable
{
    static int[] prim = FSTObject2IntMap.prim;

    public Object  mKeys[];
    public Object  mValues[];
    public int     mNumberOfElements;
    FSTObject2ObjectMap<K,V> next;
    int level;
    FSTObject2ObjectMap parent;

    public FSTObject2ObjectMap(int initialSize) {
        this(initialSize,0,null);
    }

    public FSTObject2ObjectMap(int initialSize, int level, FSTObject2ObjectMap par)
    {
        parent = par;
        this.level = level;
        if (initialSize < 2)
        {
            initialSize = 2;
        }

        initialSize = FSTObject2IntMap.adjustSize(initialSize*2);

        mKeys = new Object[initialSize];
        mValues = new Object[initialSize];
        mNumberOfElements = 0;
    }

    public int size()
    {
        return mNumberOfElements + (next != null ? next.size():0);
    }

    final public void put(K key, V value)
    {
        int hash = key.hashCode() & 0x7FFFFFFF;
        putHash(key, value, hash);
    }

    final void putHash(K key, V value, int hash) {
        if (mNumberOfElements*2 > mKeys.length)
        {
            resize(mKeys.length * 2);
        }

        int idx = hash % mKeys.length;

        if (mKeys[idx] == null ) // new
        {
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
        }
        else if (mKeys[idx].equals(key))  // overwrite
        {
            mValues[idx] = value;
        } else {
            putNext(hash, key, value);
        }
    }

    final void putNext(int hash, K key, V value) {
        if ( next == null ) {
            int newSiz = mKeys.length/12;
            next = new FSTObject2ObjectMap<K,V>(newSiz,level+1,this);
        }
        next.putHash(key,value,hash);
    }

    final public V get(K key) {
        int hash = key.hashCode() & 0x7FFFFFFF;
//        return getHash(key,hash); inline -->
        final int idx = hash % mKeys.length;

        final Object mKey = mKeys[idx];
        if (mKey == null ) // not found
        {
            return null;
        }
        else if (mKey.equals(key))  // found
        {
            return (V) mValues[idx];
        } else {
            if ( next == null ) {
                return null;
            }
            V res = next.getHash(key, hash);
            return res;
        }
        // <-- inline
    }

    static int miss = 0;
    static int hit = 0;
    final V getHash(K key, int hash)
    {
        final int idx = hash % mKeys.length;

        final Object mKey = mKeys[idx];
        if (mKey == null ) // not found
        {
//            hit++;
            return null;
        }
        else if (mKey.equals(key))  // found
        {
//            hit++;
            return (V) mValues[idx];
        } else {
            if ( next == null ) {
                return null;
            }
//            miss++;
            V res = next.getHash(key, hash);
            return res;
        }
    }

    final K removeHash(K key, int hash)
    {
        final int idx = hash % mKeys.length;

        final Object mKey = mKeys[idx];
        if (mKey == null ) // not found
        {
//            hit++;
            return null;
        }
        else if (mKey.equals(key))  // found
        {
//            hit++;
            K val = (K) mKeys[idx];
            mValues[idx] = null; mKeys[idx] = null;
            mNumberOfElements--;
            return val;
        } else {
            if ( next == null ) {
                return null;
            }
//            miss++;
            return next.removeHash(key, hash);
        }
    }

    final void resize(int newSize)
    {
        newSize = FSTObject2IntMap.adjustSize(newSize);
        Object[]    oldTabKey = mKeys;
        Object[] oldTabVal = mValues;

        mKeys = new Object[newSize];
        mValues           = new Object[newSize];
        mNumberOfElements = 0;

        for (int n = 0; n < oldTabKey.length; n++)
        {
            if (oldTabKey[n] != null)
            {
                put((K)oldTabKey[n], (V)oldTabVal[n]);
            }
        }
        if ( next != null ) {
            FSTObject2ObjectMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTObject2ObjectMap<K,V> kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if ( mKey != null ) {
                kfstObject2IntMap.put((K) mKey,(V)mValues[i]);
            }
        }
        if ( next != null ) {
            next.rePut(kfstObject2IntMap);
        }
    }

    public void clear() {
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
        mNumberOfElements = 0;
        if ( next != null ) {
            next.clear();
        }
    }

    public static void main( String arg[] ) {
        for ( int jj = 0; jj < 100; jj++ ) {
            int count = 300000; hit = miss = 0;
            FSTObject2ObjectMap<Object,Integer> map = new FSTObject2ObjectMap<Object,Integer>(count/1000);
            HashMap<Object,Integer> hm = new HashMap<Object, Integer>(count/1000);
            Object obs[] = new Object[count];

            for ( int i = 0; i < count;  i++ ) {
                obs[i] = ""+i;
            }

            System.out.println("---");
            long tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                map.put(obs[i],i);
            }
            System.out.println("fst put"+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int i = 0; i < count;  i++ ) {
                hm.put(obs[i],i);
            }
            System.out.println("hmap put"+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( map.get(obs[i]) != i ) {
                        System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("h"+hit+" m "+miss);
            System.out.println("fst read "+(System.currentTimeMillis()-tim));

            tim = System.currentTimeMillis();
            for ( int j = 0; j < 10;  j++ ) {
                for ( int i = 0; i < count;  i++ ) {
                    if ( hm.get(obs[i]) != i ) {
                        System.out.println("bug "+i);
                    }
                }
            }
            System.out.println("hmap read "+(System.currentTimeMillis()-tim));
        }
    }
}