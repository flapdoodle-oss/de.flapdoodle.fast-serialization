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
