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
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 20.11.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class FSTIdentity2IdMap
{
    private static final int GROFAC = 2;

    private Object  mKeys[];
    private int     mValues[];
    private int     mNumberOfElements;

    private FSTIdentity2IdMap next;

    int bloom = 0;

    public FSTIdentity2IdMap(int initialSize)
    {
        if (initialSize < 2)
        {
            initialSize = 2;
        }

        initialSize = FSTObject2IntMap.adjustSize(initialSize*2);

        mKeys = new Object[initialSize];
        mValues = new int[initialSize];
        mNumberOfElements = 0;
    }

    public int size()
    {
        return mNumberOfElements + (next != null ? next.size():0);
    }

    final public int putOrGet(Object key, int value)
    {
        int h = System.identityHashCode(key);
        int hash = ((h << 1) - (h << 8))&0x7FFFFFFF;
        return putOrGetHash(key, value, hash, this);
    }

    final int putOrGetHash(Object key, int value, int hash, FSTIdentity2IdMap parent) {
        if (mNumberOfElements*GROFAC > mKeys.length)
        {
            if ( parent != null ) {
                if ( (parent.mNumberOfElements+mNumberOfElements)*GROFAC > parent.mKeys.length ) {
                    parent.resize(parent.mKeys.length*GROFAC);
                    return parent.putOrGet(key,value);
                } else {
                    resize(mKeys.length * GROFAC);
                }
            } else {
                resize(mKeys.length * GROFAC);
            }
        }

        Object[] mKeys = this.mKeys;
        int idx = hash % mKeys.length;

        if (mKeys[idx] == null ) // new
        {
            bloom|=hash;
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
            return Integer.MIN_VALUE;
        }
        else if (mKeys[idx]==key)    // present ?
        {
            return mValues[idx];
        } else {
            return putOrGetNext(hash, key, value);
        }
    }

    final int putOrGetNext(final int hash, final Object key, final int value) {
        if ( next == null ) { // new
            int newSiz = mNumberOfElements/3;
            next = new FSTIdentity2IdMap(newSiz);
            next.putHash(key,value,hash,this);
            return Integer.MIN_VALUE;
        }
        return next.putOrGetHash(key,value,hash, this);
    }

    final public void put(Object key, int value)
    {
        int h = System.identityHashCode(key);
        int hash = ((h << 1) - (h << 8))&0x7FFFFFFF;
        putHash(key, value, hash, this);
    }

    final void putHash(Object key, int value, int hash, FSTIdentity2IdMap parent) {
        if (mNumberOfElements*GROFAC > mKeys.length)
        {
            if ( parent != null ) {
                if ( (parent.mNumberOfElements+mNumberOfElements)*GROFAC > parent.mKeys.length ) {
                    parent.resize(parent.mKeys.length*GROFAC);
                    parent.put(key,value);
                    return;
                } else {
                    resize(mKeys.length * GROFAC);
                }
            } else {
                resize(mKeys.length * GROFAC);
            }
        }

        Object[] mKeys = this.mKeys;
        int idx = hash % mKeys.length;

        if (mKeys[idx] == null ) // new
        {
            bloom|=hash;
            mNumberOfElements++;
            mValues[idx] = value;
            mKeys[idx]   = key;
        }
        else if (mKeys[idx]==key)    // overwrite
        {
            bloom|=hash;
            mValues[idx] = value;
        } else {
            putNext(hash, key, value);
        }
    }

    final void putNext(final int hash, final Object key, final int value) {
        if ( next == null ) {
            int newSiz = mNumberOfElements/3;
            next = new FSTIdentity2IdMap(newSiz);
        }
        next.putHash(key,value,hash, this);
    }

    final public int get(final Object key) {
        int h = System.identityHashCode(key);
        int hash = ((h << 1) - (h << 8))&0x7FFFFFFF;
        if ( (bloom&hash) != hash )
            return Integer.MIN_VALUE;
        //return getHash(key,hash); inline =>
        final int idx = hash % mKeys.length;

        Object[] mKeys = this.mKeys;
        final Object mapsKey = mKeys[idx];
        if (mapsKey == null ) // not found
        {
            return Integer.MIN_VALUE;
        }
        else if (mapsKey == key )  // found
        {
            return mValues[idx];
        } else {
            if ( next == null ) {
                return Integer.MIN_VALUE;
            }
            int res = next.getHash(key, hash);
            return res;
        }
        // <== inline
    }

    static int miss = 0;
    static int hit = 0;
    final int getHash(final Object key, final int hash)
    {
        final int idx = hash % mKeys.length;

        final Object mapsKey = mKeys[idx];
        if (mapsKey == null ) // not found
        {
            return Integer.MIN_VALUE;
        }
        else if (mapsKey == key)  // found
        {
            return mValues[idx];
        } else {
            if ( next == null ) {
                return Integer.MIN_VALUE;
            }
            int res = next.getHash(key, hash);
            return res;
        }
    }

    final void resize(int newSize)
    {
        newSize = FSTObject2IntMap.adjustSize(newSize);
        Object[]    oldTabKey = mKeys;
        int[] oldTabVal = mValues;

        mKeys = new Object[newSize];
        mValues           = new int[newSize];
        mNumberOfElements = 0;
        bloom=0;

        for (int n = 0; n < oldTabKey.length; n++)
        {
            if (oldTabKey[n] != null)
            {
                put(oldTabKey[n], oldTabVal[n]);
            }
        }
        if ( next != null ) {
            FSTIdentity2IdMap oldNext = next;
            next = null;
            oldNext.rePut(this);
        }
    }

    private void rePut(FSTIdentity2IdMap kfstObject2IntMap) {
        for (int i = 0; i < mKeys.length; i++) {
            Object mKey = mKeys[i];
            if ( mKey != null ) {
                kfstObject2IntMap.put(mKey, mValues[i]);
            }
        }
        if ( next != null ) {
            next.rePut(kfstObject2IntMap);
        }
    }

    private static int _hash(Object x) {
        int h = System.identityHashCode(x);
        return ((h << 1) - (h << 8))&0x7FFFFFFF;
    }

    public void clear() {
        if ( size() == 0 ) {
            return;
        }
        bloom = 0;
        FSTUtil.clear(mKeys);
        FSTUtil.clear(mValues);
        mNumberOfElements = 0;
        if ( next != null ) {
            next.clear();
        }
    }

    public static void main( String arg[] ) {
    }
}