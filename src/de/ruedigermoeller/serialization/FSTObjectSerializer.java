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
package de.ruedigermoeller.serialization;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public interface FSTObjectSerializer {
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException;
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException;
    public boolean willHandleClass(Class cl);
    // return an object or null for default instantiation
    public Object instantiate(Class objectClass, FSTObjectInput fstObjectInput, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException;
}
