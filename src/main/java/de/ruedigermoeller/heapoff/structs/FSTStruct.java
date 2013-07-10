package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.Serializable;

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
 * Date: 01.07.13
 * Time: 20:20
 * To change this template use File | Settings | File Templates.
 */
public class FSTStruct implements Serializable {

    public static Unsafe unsafe = FSTUtil.getUnsafe();

    transient public long ___offset;
    transient public byte[] ___bytes;
    transient public FSTStructFactory ___fac;
    public transient int ___elementSize;

    protected Unsafe getUnsafe() {
        return unsafe;
    }

    /**
     * must include bytearray offset in case of unsafe use
     * @param off
     */
    protected void setAbsoluteOffset(long off) {
        ___offset = off;
    }

    protected long getAbsoluteOffset() {
        return ___offset;
    }

    public int getByteSize() {
        if ( !isOffHeap() ) {
            return 0;
        }
        return unsafe.getInt(___bytes,___offset);
    }

    public int getClzId() {
        if ( ! isOffHeap() )
            throw new RuntimeException("cannot call on heap");
        return unsafe.getInt(___bytes,___offset+4);
    }

    protected void addOffset(long off) {
        ___offset+=off;
    }

    protected void setBase(byte[] base) {
        ___bytes = base;
    }

    public byte[] getBase() {
        return ___bytes;
    }

    public FSTStructFactory getFac() {
        return ___fac;
    }

    public void baseOn( byte base[], long offset, FSTStructFactory fac) {
        ___bytes = base; ___offset = offset; ___fac = fac;
    }

    public boolean isIdenticTo(FSTStruct other) {
        return other.getBase() == ___bytes && other.getAbsoluteOffset() == ___offset;
    }

    public boolean isOffHeap() {
        return ___fac != null;
    }

    public int getElementInArraySize() {
        return ___elementSize;
    }

    public boolean isStructArrayPointer() {
        return ___elementSize > 0;
    }

    /**
     * important: iterators, array access and access to substructures of a struct always return
     * pointers, which are cached per thread. To keep a pointer to a struct (e.g. search struct array and find an
     * element which should be kept as result) you need to detach it (removed from cache). Cost of detach is like an
     * object creation in case it is in the cache.
     */
    public void detach() {
        if ( isOffHeap() ) {
            ___fac.detach(this);
        }
    }
    /**
     *  Warning: no bounds checking. Moving the pointer outside the underlying byte[] will cause access violations
     */
    public final void next() {
        if ( ___elementSize > 0 )
            ___offset += ___elementSize;
        else
            throw new RuntimeException("not pointing to a struct array");
    }

    /**
     *  Warning: no bounds checking. Moving the pointer outside the underlying byte[] will cause access violations
     */
    public final void next(int offset) {
        ___offset += offset;
    }

    /**
     *  Warning: no bounds checking. Moving the pointer outside the underlying byte[] will cause access violations
     */
    public final void previous() {
        if ( ___elementSize > 0 )
            ___offset -= ___elementSize;
        else
            throw new RuntimeException("not pointing to a struct array");
    }

}
