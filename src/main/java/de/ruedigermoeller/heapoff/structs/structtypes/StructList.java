package de.ruedigermoeller.heapoff.structs.structtypes;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.impl.FSTEmbeddedBinary;
import de.ruedigermoeller.serialization.util.FSTUtil;

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
 * Date: 30.06.13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */
public class StructList<E extends FSTStruct> extends FSTStruct implements FSTEmbeddedBinary {

    transient E template; // expect to be offheap
    transient Object[] elems;
    int elemSize;
    int size;

    /**
     * initializes an inline-object array filled with null values
     * @param size
     * @param elemSize
     */
    public StructList(int size, int elemSize ) {
        this.size = size;
        elems = new Object[size];
        this.elemSize = elemSize;
    }

    /**
     * initializes with a template. When off heaped, all elements are filled with a copy of that template.
     * Warning: if this is used, this class behaves different off-heap compared to offheap. On Heap the template
     * element is ignored.
     *
     * @param size
     * @param template
     */
    public StructList(int size, E template ) {
        if ( ! template.isOffHeap() ) {
            throw new RuntimeException("template should be offheap. Use constructor including FSTStructFactory");
        }
        this.size = size;
        this.template = template;
        elems = new Object[size];
    }

    public StructList(int size, E templateOnHeap, FSTStructFactory fac ) {
        template = fac.toStruct(template);
        this.size = size;
        elems = new Object[size];
        this.template = templateOnHeap;
    }

    public E get( int i ) {
        int __siz = getSize();
        if ( i < 0 || i >= __siz ) {
            throw new ArrayIndexOutOfBoundsException("index:"+i+" size: "+__siz);
        }
        if ( isOffHeap() ) {
            return (E) ___fac.getStructPointerByOffset(___bytes, getObjectArrayOffset() + elemSize * i);
        } else {
            return (E) elems[i];
        }
    }

    public void set( int i, E value ) {
        int __siz = getSize();
        if ( i < 0 || i >= __siz ) {
            throw new ArrayIndexOutOfBoundsException("index:"+i+" size: "+__siz);
        }
        if ( isOffHeap() ) {
            if ( value.isOffHeap() ) {
                int byteSize = value.getByteSize();
                if ( byteSize > elemSize )
                    throw new RuntimeException("elemt is too large to fit");
                FSTStruct.unsafe.copyMemory(___bytes,getObjectArrayOffset()+elemSize*i, value.getBase(), value.___offset, byteSize);
            } else {
                try {
                    ___fac.toByteArray(value, ___bytes, (int)getObjectArrayOffset()-FSTUtil.bufoff+elemSize*i);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            elems[i] = value;
        }
    }

    public int getSize() {
        return size;
    }

    public long getObjectArrayOffset() {
        return ___offset+getByteSize()-elemSize*size;
    }

    @Override
    public int getEmbeddedSizeAdditon(FSTStructFactory fac) {
        return elemSize*size;
    }

    @Override
    public int insertEmbedded(FSTStructFactory fac, byte[] base, int targetIndex) {
        if ( isOffHeap() ) {
            throw new RuntimeException("expected onheap template");
        }
        for (int i = 0; i < elems.length; i++) {
            Object elem = elems[i];
            if ( elem != null ) {
                try {
                    getFac().toByteArray(elem,base,targetIndex);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                if ( template != null ) {
                    unsafe.copyMemory(base,(long)targetIndex+FSTUtil.bufoff,template.___bytes, template.___offset,template.getByteSize());
                } else {
                    unsafe.setMemory(base,(long)targetIndex+FSTUtil.bufoff,(long)elemSize,(byte)0);
                }
            }
            targetIndex+=elemSize;
        }
        return targetIndex;
    }

}
