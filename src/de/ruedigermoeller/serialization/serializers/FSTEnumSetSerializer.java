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
