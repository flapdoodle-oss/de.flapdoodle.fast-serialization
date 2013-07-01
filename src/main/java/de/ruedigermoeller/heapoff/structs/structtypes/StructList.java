package de.ruedigermoeller.heapoff.structs.structtypes;

import de.ruedigermoeller.heapoff.structs.FSTStruct;

import java.io.Serializable;
import java.util.*;

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
public class StructList<E> extends FSTStruct {

    Object[] elems;
    int size;

    public StructList(int size) {
        this.size = size;
        elems = new Object[size];
    }

    public StructList(Collection<E> col) {
        elems = new Object[col.size()];
        for (Iterator<E> iterator = col.iterator(); iterator.hasNext(); ) {
            add(iterator.next());
        }
    }

    public int indexOf(E toSearch) {
        for (int i = 0; i < elemsLen(); i++) {
            Object elem = elems(i);
            if ( toSearch.equals(elem) ) {
                return i;
            }
        }
        return -1;
    }

    public int getSize() {
        return size;
    }

    public void elems(int index, E val) {
        elems[index] = val;
    }

    public int elemsLen() {
        return elems.length;
    }

    public int elemsIndex() {
        return -1; // regenerated offheap
    }

    public E elems( int i ) {
        return (E) elems[i];
    }

    public E get( int i ) {
        return elems(i);
    }

    public void add(E elem) {
        elems(size,elem);
        size++;
    }
}
