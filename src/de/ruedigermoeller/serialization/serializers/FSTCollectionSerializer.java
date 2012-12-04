package de.ruedigermoeller.serialization.serializers;

import de.ruedigermoeller.serialization.*;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class FSTCollectionSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException {
        Collection col = (Collection)toWrite;
        out.writeCInt(col.size());
        Class[] possibleClasses = referencedBy.getPossibleClasses();
        if ( (possibleClasses == null || possibleClasses.length == 0) ) {
            possibleClasses = new Class[] {null};
            if ( col instanceof List) {
                List l = (List) col;
                for (int i = 0; i < l.size(); i++) {
                    Object o = l.get(i);
                    out.writeObjectInternal(o, possibleClasses);
                    if ( o != null ) {
                        possibleClasses[0] = o.getClass();
                    }
                }
            } else {
                for (Object o : col) {
                    out.writeObjectInternal(o, possibleClasses);
                    if ( o != null ) {
                        possibleClasses[0] = o.getClass();
                    }
                }
            }
        } else {
            if ( col instanceof List) {
                List l = (List) col;
                for (int i = 0; i < l.size(); i++) {
                    Object o = l.get(i);
                    out.writeObjectInternal(o, possibleClasses);
                }
            } else {
                for (Object o : col) {
                    out.writeObjectInternal(o, possibleClasses);
                }
            }
        }
    }

    @Override
    public void readObject(FSTObjectInput in, Object toRead, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
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
            in.registerObject(res, streamPositioin,serializationInfo);
            Collection col = (Collection)res;
            if ( col instanceof ArrayList ) {
                ((ArrayList)col).ensureCapacity(len);
            }
            Class[] possibleClasses = referencee.getPossibleClasses();
            if ( (possibleClasses == null || possibleClasses.length == 0) ) {
                possibleClasses = new Class[] {null};
                for ( int i = 0; i < len; i++ ) {
                    Object obj = in.readObjectInternal(possibleClasses);
                    col.add(obj);
                    if ( obj != null ) {
                        possibleClasses[0] = obj.getClass();
                    }
                }
            } else {
                for ( int i = 0; i < len; i++ ) {
                    col.add(in.readObjectInternal(possibleClasses));
                }
            }
            return res;
        } catch (Throwable th) {
            return null;
        }
    }
}
