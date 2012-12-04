package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 01:20
 * To change this template use File | Settings | File Templates.
 */
public class FSTStringBuilderSerializer extends FSTStringBufferSerializer {
    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String s = in.readUTF();
        StringBuilder stringBuilder = new StringBuilder(s);
        in.registerObject(stringBuilder, streamPositioin,serializationInfo);
        return stringBuilder;
    }
}
