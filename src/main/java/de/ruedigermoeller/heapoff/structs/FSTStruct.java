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

    transient protected long ___offset;
    transient protected byte[] ___bytes;
    transient protected FSTStructFactory ___fac;


    protected Unsafe getUnsafe() {
        return unsafe;
    }
    /**
     * must include bytearray offset in case of unsafe use
     * @param off
     */
    public void setAbsoluteOffset(long off) {
        ___offset = off;
    }

    public long getAbsoluteOffset() {
        return ___offset;
    }

    public void addOffset(long off) {
        ___offset+=off;
    }

    public void setBase(byte[] base) {
        ___bytes = base;
    }
    public byte[] getBase() {
        return ___bytes;
    }

    public void setFac(FSTStructFactory fac) {
        ___fac = fac;
    }

    public FSTStructFactory getFac() {
        return ___fac;
    }

    public void baseOn( byte base[], long offset, FSTStructFactory fac ) {
        ___bytes = base; ___offset = offset; ___fac = fac;
    }

    public boolean isIdenticTo(FSTStruct other) {
        return other.getBase() == ___bytes && other.getAbsoluteOffset() == ___offset;
    }

}
