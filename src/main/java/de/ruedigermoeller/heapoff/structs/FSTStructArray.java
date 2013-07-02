package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.serialization.util.FSTUtil;

import java.util.Iterator;

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
 * Date: 26.06.13
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */

/**
 * An array consisting of directly packed structs. All structs have to be of same memory size.
 *
 * the struct objects are directly embedded in a row, there is no index table or similar involved.
 * Up to 2 GB of packed struct objects can be held without increasing Full GC duration
 *
 * Note that get() and iterator just deliver a pointer to a struct of the array.
 * In order to detach an object and reference it from outside you need to use 'createPointer'
 * @param <T>
 */
public class FSTStructArray<T extends FSTStruct> {

    byte b[];
    int elemSiz;
    int size;
    FSTStructFactory fac;

    transient ThreadLocal<T> wrapper;

    public FSTStructArray(FSTStructFactory fac, T template, int size) {
        if (size <= 0) {
            throw new RuntimeException("size must be > 0");
        }
        this.fac = fac;
        elemSiz = fac.calcStructSize(template);
        b = new byte[size * elemSiz];
        this.size = size;
        byte[] bytes = fac.toByteArray(template);
        //int clId = fac.getClzId(template.getClass());
        for (int i = 0; i < b.length; i += elemSiz) {
            //FSTUtil.unFlaggedUnsafe.putInt(b,FSTUtil.bufoff+i,clId);
            fac.unsafe.copyMemory(bytes, 0, b, FSTUtil.bufoff + i, elemSiz);
        }
    }

    public T get(int i) {
        if (i < 0 || i >= size)
            throw new ArrayIndexOutOfBoundsException("index: " + i + " size:" + size);
        if ( wrapper == null ) {
            wrapper = new ThreadLocal<T>() {
                @Override
                protected T initialValue() {
                    return (T) fac.getStructWrapper(b, 0);
                }
            };
        }
        T wrap = wrapper.get();
        wrap.setAbsoluteOffset(FSTUtil.bufoff + elemSiz * i);
        return wrap;
    }

    public int size() {
        return size;
    }

    public int getElemSiz() {
        return elemSiz;
    }

    public Iterator<T> iterator() {
        return new StructArrIterator<T>();
    }

    public T createPointer(int index) {
        T res = (T) fac.createStructWrapper(b, index * elemSiz);
        res.setElementSize(elemSiz);
        return res;
    }

    final class StructArrIterator<T extends FSTStruct> implements Iterator<T> {

        T current;
        final int maxPos;
        final int eSiz;

        StructArrIterator() {
            current = (T) fac.createStructWrapper(b, 0);
            maxPos = b.length + FSTUtil.bufoff;
            this.eSiz = elemSiz;
        }

        @Override
        public final boolean hasNext() {
            return current.getAbsoluteOffset() < maxPos;
        }

        @Override
        public final T next() {
            current.addOffset(elemSiz);
            return current;
        }

        @Override
        public void remove() {
            throw new RuntimeException("unsupported operation");
        }
    }
}
