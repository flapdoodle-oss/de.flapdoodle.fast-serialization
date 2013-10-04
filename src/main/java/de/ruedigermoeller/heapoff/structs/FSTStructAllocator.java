package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;

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
 * Date: 04.10.13
 * Time: 20:51
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructAllocator {
    protected int chunkSize;
    protected byte chunk[];
    protected int chunkIndex;

    protected FSTStructAllocator() {

    }

    /**
     * @param b
     * @param index
     * @return a new allocated pointer matching struct type stored in b[]
     */
    public static FSTStruct createStructPointer(byte b[], int index) {
        return FSTStructFactory.getInstance().getStructPointerByOffset(b, FSTStruct.bufoff + index).detach();
    }

    /**
     * @param onHeapTemplate
     * @param <T>
     * @return return a byte array based struct instance for given on-heap template. Allocates a new byte[] with each call
     */
    public static <T extends FSTStruct> T toStruct(T onHeapTemplate) {
        return FSTStructFactory.getInstance().toStruct(onHeapTemplate);
    }

    /**
     * @param b
     * @param index
     * @return a pointer matching struct type stored in b[] from the thread local cache
     */
    public static FSTStruct getVolatileStructPointer(byte b[], int index) {
        return (FSTStruct) FSTStructFactory.getInstance().getStructPointerByOffset(b, FSTStruct.bufoff + index);
    }

    /**
     * @param clazz
     * @param <C>
     * @return a newly allocated pointer matching. use baseOn to point it to a meaningful location
     */
    public static <C extends FSTStruct> C newPointer(Class<C> clazz) {
        try {
            return (C)FSTStructFactory.getInstance().getProxyClass(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a Structallocator with given chunk size in bytes. If allocated structs are larger than the given size, a new bytearray is
     * created for the allocation.
     * @param chunkSizeBytes
     */
    public FSTStructAllocator(int chunkSizeBytes) {
        this.chunkSize = chunkSizeBytes;
    }

    /**
     * create a new struct array of same type as template
     * @param size
     * @return
     */
    public <X extends FSTStruct> StructArray<X> newArray(int size, X templ) {
        StructArray<X> aTemplate = new StructArray<X>(size, templ);
        int siz = getFactory().calcStructSize(aTemplate);
        try {
            if ( siz < chunkSize )
                return newStruct(aTemplate);
            else {
                return getFactory().toStruct(aTemplate);
            }
        } catch (Throwable e) {
            System.out.println("tried to allocate "+siz+" bytes. StructArray of "+size+" "+templ.getClass().getName());
            throw new RuntimeException(e);
        }
    }

    /**
     * create a fixed size struct hashmap. Note it should be of fixed types for keys and values, as
     * the space for those is allocated directly. Additionally keys and values are stored 'in-place' without references.
     * (for allocation if templateless cosntructore has been used)
     *
     * @param size
     * @param keyTemplate
     * @param <K>
     * @return
     */
    public <K extends FSTStruct, V extends FSTStruct> StructMap<K,V> newMap(int size, K keyTemplate, V valueTemplate) {
        return newStruct( new StructMap<K, V>(keyTemplate,valueTemplate,size) );
    }

    /**
     * allocate a Struct instance from an arbitrary template. This is provided to avoid having to construct tons
     * of "allocator" instances.
     * @param aTemplate
     * @param <S>
     * @return
     */
    public <S extends FSTStruct> S newStruct(S aTemplate) {
        aTemplate = getFactory().toStruct(aTemplate);
        if (aTemplate.getByteSize()>=chunkSize)
            return (S)aTemplate.createCopy();
        int byteSize = aTemplate.getByteSize();
        synchronized (this) {
            if (chunk == null || chunkIndex+ byteSize > chunk.length) {
                chunk = new byte[chunkSize];
                chunkIndex = 0;
            }
            FSTStruct.unsafe.copyMemory(aTemplate.___bytes, aTemplate.___offset, chunk, FSTStruct.bufoff + chunkIndex, byteSize);
            S res = (S) getFactory().createStructWrapper(chunk, chunkIndex );
            chunkIndex+=byteSize;
            return res;
        }
    }

    protected FSTStructFactory getFactory() {
        return FSTStructFactory.getInstance();
    }


}
