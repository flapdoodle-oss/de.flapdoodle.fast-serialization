package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */
public class FSTBigNumberSerializers {

    public static class FSTByteSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeFByte(((Byte)toWrite).byteValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Byte(in.readByte());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Byte.class;
        }
    }

    static public class FSTCharSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeCChar(((Character)toWrite).charValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Character(in.readCChar());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Character.class;
        }
    }

    static public class FSTShortSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeCShort(((Short)toWrite).shortValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Short(in.readCShort());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Short.class;
        }
    }

    static public class FSTLongSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeCLong(((Long) toWrite).longValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Long(in.readCLong());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Long.class;
        }
    }

    static public class FSTFloatSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeCFloat(((Float) toWrite).floatValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Float(in.readFloat());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Float.class;
        }
    }

    static public class FSTDoubleSerializer extends FSTBasicObjectSerializer implements FSTCrossLanguageSerializer {
        @Override
        public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
            out.writeFDouble(((Double) toWrite).doubleValue());
        }

        @Override
        public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            Object res = new Double(in.readDouble());
            return res;
        }

        @Override
        public Class getCrossLangLayout() {
            return Double.class;
        }
    }

}
