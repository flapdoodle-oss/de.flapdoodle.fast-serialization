package de.ruedigermoeller.serialization;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class FSTBasicObjectSerializer implements FSTObjectSerializer {

    protected FSTBasicObjectSerializer() {
    }

    @Override
    public boolean willHandleClass(Class cl) {
        return true;
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        return null;
    }
}
