package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 02.12.12
 * Time: 14:16
 * To change this template use File | Settings | File Templates.
 */
public class FSTDateSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException {
        out.writeFLong(((Date)toWrite).getTime());
    }

    @Override
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object res = new Date(in.readFLong());
        in.registerObject(res,streamPositioin,serializationInfo);
        return res;
    }

}
