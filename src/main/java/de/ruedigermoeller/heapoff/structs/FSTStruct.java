package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
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
    public static long bufoff = FSTUtil.bufoff;

    transient public long ___offset;
    transient public byte[] ___bytes;
    transient public FSTStructFactory ___fac;
    transient public int ___elementSize;

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

    public int getIndexInBase() {
        return (int) (___offset - bufoff);
    }

    public int getByteSize() {
        if ( !isOffHeap() ) {
            return 0;
        }
        return unsafe.getInt(___bytes,___offset);
    }

    public Class getPointedClass() {
        if ( ! isOffHeap() )
            throw new RuntimeException("cannot call on heap");
        Class clazz = ___fac.getClazz(getClzId());
        if ( clazz == null ) {
            return FSTStruct.class;
        }
        return clazz;
    }

    public int getClzId() {
        if ( ! isOffHeap() )
            throw new RuntimeException("cannot call on heap");
        return unsafe.getInt(___bytes,___offset+4);
    }

    public boolean pointsToNull() {
        return getClzId() <= 0;
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

    public boolean isNull() {
        return getClzId() <= 0;
    }

    /**
     * important: iterators, array access and access to substructures of a struct always return
     * pointers, which are cached per thread. To keep a pointer to a struct (e.g. search struct array and find an
     * element which should be kept as result) you need to detach it (removed from cache). Cost of detach is like an
     * object creation in case it is in the cache.
     */
    public FSTStruct detach() {
        if ( isOffHeap() ) {
            ___fac.detach(this);
        }
        return this;
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

    public <T extends FSTStruct> T cast( Class<T> to ) {
        int clzId = ___fac.getClzId(to);
        if ( this.getClass().getSuperclass() == to )
            return (T) this;
        FSTStruct res = ___fac.createStructPointer(___bytes, (int) (___offset - bufoff), clzId);
        res.___elementSize = ___elementSize;
        return (T) res;
    }

    public FSTStruct cast() {
        int clzId = getClzId();
        if ( ___fac.getClazz(clzId) == getClass().getSuperclass() )
            return this;
        FSTStruct res = (FSTStruct) ___fac.getStructPointerByOffset(___bytes, ___offset);
        res.___elementSize = ___elementSize;
        return res;
    }

    public byte getByte() {
        return unsafe.getByte(___bytes,___offset);
    }

    public void setByte(byte i) {
        unsafe.putByte(___bytes,___offset,i);
    }

    public char getChar() {
        return unsafe.getChar(___bytes, ___offset);
    }

    public short getShort() {
        return unsafe.getShort(___bytes, ___offset);
    }

    public void setShort(short i) {
        unsafe.putShort(___bytes, ___offset,i);
    }

    public int getInt() {
        return unsafe.getInt(___bytes,___offset);
    }

    public void setInt(int i) {
        unsafe.putInt(___bytes,___offset,i);
    }

    public long getLong() {
        return unsafe.getLong(___bytes,___offset);
    }

    public void setLong(long i) {
        unsafe.putLong(___bytes, ___offset, i);
    }

    public float getFloat() {
        return unsafe.getFloat(___bytes,___offset);
    }

    public double getDouble() {
        return unsafe.getDouble(___bytes,___offset);
    }

    public void getBytes(byte[] target, int startIndex) {
        if ( ! isOffHeap() ) {
            throw new RuntimeException("must be offheap to call this");
        }
        if ( target.length+startIndex > getByteSize() ) {
            throw new RuntimeException("ArrayIndexOutofBounds byte len:"+target.length+" start+size:"+(startIndex+getByteSize()));
        }
        unsafe.copyMemory(___bytes,___offset, target,bufoff, target.length);
    }

    public void setBytes(byte[] source, int sourceIndex, int len ) {
        if ( ! isOffHeap() ) {
            throw new RuntimeException("must be offheap to call this");
        }
        unsafe.copyMemory(source,bufoff+sourceIndex, ___bytes, ___offset, len);
    }

    public FSTStruct createCopy() {
        if ( ! isOffHeap() ) {
            throw new RuntimeException("must be offheap to call this");
        }
        byte b[] = new byte[getByteSize()];
        getBytes(b,0);
        return ___fac.createStructWrapper(b,0);
    }


}
