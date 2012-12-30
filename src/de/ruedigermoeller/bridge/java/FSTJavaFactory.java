package de.ruedigermoeller.bridge.java;

import de.ruedigermoeller.serialization.util.FSTInputStream;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 25.12.12
 * Time: 16:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class FSTJavaFactory {

    HashMap<Integer,Object> objectMap = new HashMap<Integer, Object>();

    public abstract Object instantiate(int clzId, FSTCountingInputStream in, FSTSerBase container, int streamPosition) throws IOException;
    public Object defaultInstantiate(Class clz, FSTCountingInputStream in, FSTSerBase container, int streampos) throws IOException {
        if ( clz == String.class || clz == char[].class ) {
            return container.readStringUTF(in);
        }
        if ( clz == Long.class ) {
            return new Long(container.readCLong(in));
        }
        if ( clz == Integer.class ) {
            return new Integer(container.readCInt(in));
        }
        if ( clz == Short.class ) {
            return new Short(container.readCShort(in));
        }
        if ( clz == Date.class ) {
            return new Date(container.readCLong(in));
        }
        if ( clz == Object[].class ) {
            int len = container.readCInt(in);
            Object[] array = new Object[len];
            return array;
        }
        return null;
    }

    public Object decodeFromStream(InputStream inputStream) throws IOException {
        FSTSerBase base = new FSTSerBase(this);
        FSTCountingInputStream in = new FSTCountingInputStream(inputStream);
        return base.decodeObject(in);
    }

    public HashMap<Integer, Object> getObjectMap() {
        return objectMap;
    }

    public Class getClass(int clzId) {
        return null;
    }

    public int getId(Class clz) {
        return 0;
    }

}
