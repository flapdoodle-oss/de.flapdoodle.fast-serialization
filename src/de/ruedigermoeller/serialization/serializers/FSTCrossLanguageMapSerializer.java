package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
 * Date: 29.12.12
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
public class FSTCrossLanguageMapSerializer  extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Map col = (Map)toWrite;
        out.writeCInt(col.size()*2);
        for (Object o : col.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            Object key = next.getKey();
            Object value = next.getValue();
            out.writeObjectInternal(key, null);
            out.writeObjectInternal(value, null);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object res = null;
        int len = in.readCInt()/2;
        if ( objectClass == HashMap.class ) {
            res = new HashMap(len);
        } else
        if ( objectClass == Hashtable.class ) {
            res = new Hashtable(len);
        } else
        {
            res = objectClass.newInstance();
        }
        in.registerObject(res, streamPositioin,serializationInfo, referencee);
        Map col = (Map)res;
        for ( int i = 0; i < len; i++ ) {
            Object key = in.readObjectInternal(null);
            Object val = in.readObjectInternal(null);
            col.put(key,val);
        }
        return res;
    }

    @Override
    public Class getCrossLangLayout() {
        return Object[].class;
    }
}
