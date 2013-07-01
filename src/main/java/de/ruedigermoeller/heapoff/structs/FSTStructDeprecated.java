package de.ruedigermoeller.heapoff.structs;

import sun.misc.Unsafe;

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
 * Time: 19:55
 * To change this template use File | Settings | File Templates.
 */
public interface FSTStructDeprecated {
    public void _setOffset(long off);
    public long  _getOffset();
    public void _addOffset(long off);
    public void _setBase(byte[] base);
    public byte[] _getBase();
    public FSTStructFactory _getFac();
    public boolean _isIdenticTo(FSTStructDeprecated other);

    public void internal_setFac(FSTStructFactory fac);
    public void internal_setUnsafe(Unsafe uns);
//    public FSTStructFactory _getFac();
}
