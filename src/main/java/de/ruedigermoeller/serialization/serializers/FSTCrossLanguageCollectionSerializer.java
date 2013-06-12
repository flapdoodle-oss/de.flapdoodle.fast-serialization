package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
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
 * Date: 28.12.12
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class FSTCrossLanguageCollectionSerializer  extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Collection col = (Collection)toWrite;
        out.writeCInt(col.size());
        if ( col instanceof List) {
            List l = (List) col;
            for (int i = 0; i < l.size(); i++) {
                Object o = l.get(i);
                out.writeObjectInternal(o, null);
            }
        } else {
            for (Object o : col) {
                out.writeObjectInternal(o, null);
            }
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            Object res = null;
            int len = in.readCInt();
            if ( objectClass == ArrayList.class ) {
                res = new ArrayList(len);
            } else
            if ( objectClass == HashSet.class ) {
                res = new HashSet(len);
            } else
            if ( objectClass == Vector.class ) {
                res = new Vector(len);
            } else {
                res = objectClass.newInstance();
            }
            in.registerObject(res, streamPositioin,serializationInfo, referencee);
            Collection col = (Collection)res;
            if ( col instanceof ArrayList ) {
                ((ArrayList)col).ensureCapacity(len);
            }
            for ( int i = 0; i < len; i++ ) {
                Object obj = in.readObjectInternal(null);
                col.add(obj);
            }
            return res;
        } catch (Throwable th) {
            return null;
        }
    }

    @Override
    public Class getCrossLangLayout() {
        return Object[].class;
    }
}
