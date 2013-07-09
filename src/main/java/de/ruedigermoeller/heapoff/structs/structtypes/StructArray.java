package de.ruedigermoeller.heapoff.structs.structtypes;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.NoAssist;
import de.ruedigermoeller.heapoff.structs.impl.FSTEmbeddedBinary;
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
 * Date: 30.06.13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */
public class StructArray<E extends FSTStruct> extends FSTStruct implements FSTEmbeddedBinary {

    protected transient FSTStruct template; // expect to be offheap
    protected transient FSTStruct[] elems;
    protected int elemSize;
    protected int size;

    /**
     * initializes an inline-object array filled with null values
     * @param size
     * @param elemSize
     */
    public StructArray(int size, int elemSize) {
        this.size = size;
        this.elemSize = elemSize;
    }

    /**
     * initializes with a template. When off heaped, all elements are filled with a copy of that template.
     * Warning: if this is used, this class behaves different off-heap compared to on-heap. On Heap the template
     * element is ignored.
     *
     * @param size
     * @param template
     */
    @NoAssist
    public StructArray(int size, E template) {
        if ( ! template.isOffHeap() ) {
            throw new RuntimeException("template should be offheap. Use constructor including FSTStructFactory");
        }
        this.size = size;
        this.template = template;
    }

    @NoAssist
    public StructArray(int size, E templateOnHeap, FSTStructFactory fac) {
        if ( templateOnHeap.isOffHeap() )
            template = templateOnHeap;
        else
            template = fac.toStruct((FSTStruct) templateOnHeap);
        elemSize = template.getByteSize();
        this.size = size;
    }

    public int getStructElemSize() {
        return elemSize;
    }

    @NoAssist
    public E get( int i ) {
        return (E) getInternal(i); // workaround javassist limit: no generics
    }

    protected Object getInternal( int i ) {
        int __siz = size;
        if ( i < 0 || i >= __siz ) {
            throw new ArrayIndexOutOfBoundsException("index:"+i+" size: "+__siz);
        }
        if ( isOffHeap() ) {
            return ___fac.getStructPointerByOffset(___bytes, getObjectArrayOffset() + elemSize * i);
        } else {
            if ( elems == null )
                return null;
            return elems[i];
        }
    }

    @NoAssist
    public void set(int i, E value ) {
        setInternal(i,value); // workaround javassist limit: no generics
    }

    protected void setInternal(int i, Object myValue0 ) {
        FSTStruct value = (FSTStruct) myValue0;
        int __siz = size;
        if ( i < 0 || i >= __siz ) {
            throw new ArrayIndexOutOfBoundsException("index:"+i+" size: "+__siz);
        }
        if ( isOffHeap() ) {
            if ( value == null ) {
                clearEntry(___bytes, getObjectArrayIndex()+elemSize*i);
                return;
            }
            if ( value.isOffHeap() ) {
                int byteSize = value.getByteSize();
                if ( byteSize > elemSize )
                    throw new RuntimeException("element is too large to fit: tried "+byteSize+" size:"+elemSize+" class "+myValue0.getClass().getName());
                FSTStruct.unsafe.copyMemory(___bytes,getObjectArrayOffset()+elemSize*i, value.getBase(), value.___offset, byteSize);
            } else {
                try {
                    ___fac.toByteArray(value, ___bytes, (int)getObjectArrayOffset()-FSTUtil.bufoff+elemSize*i);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if ( elems == null ) {
                elems = new FSTStruct[size];
            }
            elems[i] = value;
        }
    }

    public int getSize() {
        return size;
    }

    public long getObjectArrayOffset() {
        return ___offset+getByteSize()-elemSize*size;
    }

    public long getObjectArrayIndex() {
        return ___offset+getByteSize()-elemSize*size-FSTUtil.bufoff;
    }

    @Override
    public int getEmbeddedSizeAdditon(FSTStructFactory fac) {
        return elemSize*size;
    }

    @Override
    public int insertEmbedded(FSTStructFactory fac, byte[] base, int targetIndex) {
        if ( isOffHeap() ) {
            throw new RuntimeException("expected to be onheap");
        }
        for (int i = 0; i < size; i++) {
            Object elem = get(i);
            if ( elem != null ) {
                try {
                    getFac().toByteArray(elem,base,targetIndex);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                clearEntry(base, targetIndex);
            }
            targetIndex+=elemSize;
        }
        return targetIndex;
    }

    protected void clearEntry(byte[] base, long targetIndex) {
        if ( template != null ) {
            unsafe.copyMemory(template.___bytes, template.___offset, base, targetIndex + FSTUtil.bufoff, template.getByteSize());
        } else {
            unsafe.setMemory(base, targetIndex +FSTUtil.bufoff,(long)elemSize,(byte)0);
        }
    }

    @NoAssist
    public Iterator<E> iterator() {
        return new StructArrIterator<E>();
    }

    public E createPointer(int index) {
        if ( ! isOffHeap() )
            throw new RuntimeException("must be offheap to call this");
        E res = (E) ___fac.createStructWrapper(___bytes, (int) (getObjectArrayIndex() + index * elemSize));
        res.___elementSize = elemSize;
        return res;
    }

    final class StructArrIterator<T extends FSTStruct> implements Iterator<T> {

        T current;
        final long maxPos;
        final int eSiz;
        final byte[] bytes;

        StructArrIterator() {
            bytes = ___bytes;
            this.eSiz = getStructElemSize();
            current = (T) ___fac.createStructWrapper(bytes, (int) getObjectArrayIndex());
            current.___offset-=eSiz;
            maxPos = getSize()*eSiz + getObjectArrayOffset()-eSiz;
        }

        @Override
        public final boolean hasNext() {
            return current.___offset < maxPos;
        }

        @Override
        public final T next() {
            current.___offset+=eSiz;
            return current;
        }

        @Override
        public void remove() {
            throw new RuntimeException("unsupported operation");
        }
    }
}
