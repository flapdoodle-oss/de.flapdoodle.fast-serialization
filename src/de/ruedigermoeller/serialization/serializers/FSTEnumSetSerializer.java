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

package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 04:09
 * To change this template use File | Settings | File Templates.
 */
public class FSTEnumSetSerializer extends FSTBasicObjectSerializer {

    Field elemType;
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException {
        EnumSet enset = (EnumSet) toWrite;
        int count = 0;
        out.writeCInt(enset.size());
        if ( enset.isEmpty() ) { //WTF only way to determine enumtype ..
            EnumSet compl = EnumSet.complementOf(enset);
            out.writeClass(compl.iterator().next());
        } else {
            for (Object element : enset) {
                if ( count == 0 ) {
                    out.writeClass(element);
                }
                out.writeObjectInternal(element, Enum.class);
                count++;
            }
        }
    }

    @Override
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        // empty, is done in instantiate
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readCInt();
        Class elemCl = in.readClass();
        EnumSet enSet = EnumSet.noneOf(elemCl);
        in.registerObject(enSet,streamPositioin,serializationInfo); // IMPORTANT, else tracking double objects will fail
        for (int i = 0; i < len; i++)
            enSet.add(in.readObjectInternal(Enum.class));
        return enSet;
    }
}
