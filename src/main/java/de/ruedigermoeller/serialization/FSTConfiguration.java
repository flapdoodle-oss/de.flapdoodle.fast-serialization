/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */
package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.serializers.*;

import java.awt.*;
import java.io.*;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 18.11.12
 * Time: 20:41
 *
 * Holds a serialization configuration. Reuse this class !!! construction is very expensive. (just keep a static instances around)
 */
public final class FSTConfiguration {


    FSTClazzInfoRegistry serializationInfoRegistry = new FSTClazzInfoRegistry();
    HashMap<Class,List<SoftReference>> cachedObjects = new HashMap<Class, List<SoftReference>>(97);
    FSTClazzNameRegistry classRegistry = new FSTClazzNameRegistry(null, this);
    boolean isCrossLanguage = false;

    public static Integer intObjects[];
    {
        if ( intObjects == null ) {
            intObjects = new Integer[2000];
            for (int i = 0; i < intObjects.length; i++) {
                intObjects[i] = new Integer(i);
            }
        }
    }

    public static FSTConfiguration createCrossLanguageConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.setCrossLanguage(true);
        conf.serializationInfoRegistry.setIgnoreAnnotations(true);
        conf.addDefaultClazzes();
        FSTSerializerRegistry reg = conf.serializationInfoRegistry.serializerRegistry;

        // serializers
        reg.putSerializer(Class.class, new FSTClassSerializer(), false);
        reg.putSerializer(String.class, new FSTStringSerializer(), false);
        reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
        reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
        reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
        reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
        reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);

        reg.putSerializer(Date.class, new FSTDateSerializer(), false);
        reg.putSerializer(StringBuffer.class, new FSTStringBufferSerializer(), true);
        reg.putSerializer(StringBuilder.class, new FSTStringBuilderSerializer(), true);
        reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);

        reg.putSerializer(AbstractCollection.class, new FSTCrossLanguageCollectionSerializer(), true);
        reg.putSerializer(AbstractMap.class, new FSTCrossLanguageMapSerializer(), true);
        reg.putSerializer(Dictionary.class, new FSTCrossLanguageMapSerializer(), true);

        // not cross platform capable (comparator)
        reg.putSerializer(TreeMap.class, FSTSerializerRegistry.NULL, true);
        reg.putSerializer(TreeSet.class, FSTSerializerRegistry.NULL, true);

        return conf;
    }

    public static FSTConfiguration createDefaultConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.addDefaultClazzes();

//        conf.registerAsEqualnessReplaceable(String.class);
//        conf.registerAsEqualnessReplaceable(BigDecimal.class);
//        conf.registerAsEqualnessReplaceable(BigInteger.class);
//        conf.registerAsEqualnessReplaceable(Currency.class);
//        conf.registerAsEqualnessReplaceable(Date.class);

//        conf.registerAsEqualnessCopyable(Dimension.class);
//        conf.registerAsEqualnessCopyable(Rectangle.class);
//        conf.registerAsEqualnessCopyable(Point.class);
//        conf.registerAsEqualnessCopyable(URI.class);
//        conf.registerAsEqualnessCopyable(URL.class);
//        conf.registerAsEqualnessCopyable(BitSet.class);
//        conf.registerAsEqualnessCopyable(AtomicBoolean.class);
//        conf.registerAsEqualnessCopyable(AtomicInteger.class);
//        conf.registerAsEqualnessCopyable(AtomicLong.class);
//        conf.registerAsEqualnessCopyable(SimpleDateFormat.class);

        conf.copier = new FSTObjectCopy()
        { // FIXME: move copying to serializers ?
            @Override
            public Object copy(Object toCopy, FSTConfiguration conf) throws IOException, ClassNotFoundException {
                if ( toCopy instanceof Date ) {
                    Date i = (Date) toCopy;
                    return new Date(i.getTime());
                } else
                if ( toCopy instanceof Rectangle ) {
                    Rectangle i = (Rectangle) toCopy;
                    return new Rectangle(i.x,i.y,i.width,i.height);
                } else
                if ( toCopy instanceof Point ) {
                    Point i = (Point) toCopy;
                    return new Point(i.x,i.y);
                } else
                if ( toCopy instanceof Dimension ) {
                    Dimension i = (Dimension) toCopy;
                    return new Dimension(i.width,i.height);
                }
                return null;
            }
        };

        // serializers
        FSTSerializerRegistry reg = conf.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(Class.class, new FSTClassSerializer(), false);
        reg.putSerializer(String.class, new FSTStringSerializer(), false);
        reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
        reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
        reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
        reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
        reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);

        reg.putSerializer(Date.class, new FSTDateSerializer(), false);
        reg.putSerializer(StringBuffer.class, new FSTStringBufferSerializer(), true);
        reg.putSerializer(StringBuilder.class, new FSTStringBuilderSerializer(), true);
        reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);
        reg.putSerializer(ArrayList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(Vector.class, new FSTCollectionSerializer(), false); // EXCEPTION !!! subclass should register manually
        reg.putSerializer(LinkedList.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashSet.class, new FSTCollectionSerializer(), false); // subclass should register manually
        reg.putSerializer(HashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(Hashtable.class, new FSTMapSerializer(), false); // subclass should register manually
        reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        return conf;
    }

    public void registerSerializer(Class clazz, FSTObjectSerializer ser, boolean alsoForAllSubclasses ) {
        serializationInfoRegistry.serializerRegistry.putSerializer(clazz, ser, alsoForAllSubclasses);
    }

    public static FSTConfiguration createMinimalConfiguration() {
        FSTConfiguration conf = new FSTConfiguration();
        conf.addDefaultClazzes();
        // serializers
        FSTSerializerRegistry reg = conf.serializationInfoRegistry.serializerRegistry;
        reg.putSerializer(Class.class, new FSTClassSerializer(), false);
        reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);
        reg.putSerializer(String.class, new FSTStringSerializer(), false);
        reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
        reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
        reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
        reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
        reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);
        reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
        return conf;
    }

    private FSTConfiguration() {

    }

    public boolean isCrossLanguage() {
        return isCrossLanguage;
    }

    public void setCrossLanguage(boolean crossLanguage) {
        isCrossLanguage = crossLanguage;
    }

    public void returnObject( Object ... cachedObs ) {
        synchronized (cachedObjects) {
            for (int i = 0; i < cachedObs.length; i++) {
                Object cached = cachedObs[i];
                List<SoftReference> li = cachedObjects.get(cached.getClass());
                if ( li == null ) {
                    li = new ArrayList<SoftReference>();
                    cachedObjects.put(cached.getClass(),li);
                }
                li.add(new SoftReference(cached));
            }
        }
    }

    /**
     * for optimization purposes, do not use to benchmark processing time or in a regular program as
     * this methods creates a temporary binaryoutputstream and serializes the object in order to measure the
     * size.
     */
    public int calcObjectSizeBytesNotAUtility( Object obj ) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        FSTObjectOutput ou = new FSTObjectOutput(bout,this);
        ou.writeObject(obj, obj.getClass());
        ou.close();
        return bout.toByteArray().length;
    }

    /**
     * patch default serializer lookup. set to null to delete.
     * Should be set prior to any serialization going on (serializer lookup is cached).
     * @param del
     */
    public void setSerializerRegistryDelegate( FSTSerializerRegistryDelegate del ) {
        serializationInfoRegistry.setSerializerRegistryDelegate(del);
    }

    /**
     * for optimization purposes, do not use to benchmark processing time or in a regular program as
     * this methods creates a temporary binaryoutputstream and serializes the object in order to measure the
     * write time in micros.
     *
     * give ~50.000 to 100.000 for small objects in order to get accurate results
     * for large objects you can decrease the iterations (give at least 10000)
     */
    public int calcObjectWriteTimeNotAUtility( int iterations, Object obj ) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        FSTObjectOutput ou = new FSTObjectOutput(bout,this);
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ) {
            ou.writeObject(obj, obj.getClass());
            ou.getObjectMap().clearForWrite();
            bout.reset();
        }
        long dur = System.currentTimeMillis()-tim;
        return (int) (dur*1000000/iterations);
    }

    /**
     * for optimization purposes, do not use to benchmark processing time or in a regular program as
     * this methods creates a temporary binaryoutputstream and serializes the object in order to measure the
     * read time in picoseconds.
     *
     * give ~500.000 to 1.000.000 for small objects in order to get accurate results
     * for large objects you can decrease the iterations (give at least 10000)
     */
    public int calcObjectReadTimeNotAUtility( int iterations, Object obj ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        FSTObjectOutput ou = new FSTObjectOutput(bout,this);
        ou.writeObject(obj, obj.getClass());
        ou.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        FSTObjectInput in = new FSTObjectInput(bin,this);
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ) {
            Object res = in.readObject(obj.getClass());
            bin.reset();
            in.input.reset();
            in.input.initFromStream(bin);
        }
        long dur = System.currentTimeMillis()-tim;
        return (int) (dur*1000000/iterations);
    }

    public Object getCachedObject( Class cl ) {
        synchronized (cachedObjects) {
            List<SoftReference> li = cachedObjects.get(cl);
            if ( li == null ) {
                return null;
            }
            for (int i = li.size()-1; i >= 0; i--) {
                SoftReference softReference = li.get(i);
                Object res = softReference.get();
                li.remove(i);
                if ( res != null ) {
                    return res;
                }
            }
        }
        return null;
    }

    FSTObjectCopy copier = new FSTObjectCopy() {
        @Override
        public Object copy(Object toCopy, FSTConfiguration conf) throws IOException, ClassNotFoundException {
            return null;
        }
    };
    boolean shareReferences = true;

    public FSTObjectCopy getCopier() {
        return copier;
    }

    public boolean isShareReferences() {
        return shareReferences;
    }

    public void setShareReferences(boolean shareReferences) {
        this.shareReferences = shareReferences;
    }

    /**
     * attention: id should be > CUSTOM_ID_BASE
     * @param c
     */
    public void registerClass( Class c) {
        classRegistry.registerClass(c);
    }

    void addDefaultClazzes() {
        classRegistry.registerClass(String.class);
        classRegistry.registerClass(Byte.class);
        classRegistry.registerClass(Short.class);
        classRegistry.registerClass(Integer.class);
        classRegistry.registerClass(Long.class);
        classRegistry.registerClass(Float.class);
        classRegistry.registerClass(Double.class);
        classRegistry.registerClass(BigDecimal.class);
        classRegistry.registerClass(BigInteger.class);
        classRegistry.registerClass(Character.class);
        classRegistry.registerClass(Boolean.class);
        classRegistry.registerClass(TreeMap.class);
        classRegistry.registerClass(HashMap.class);
        classRegistry.registerClass(ArrayList.class);
        classRegistry.registerClass(ConcurrentHashMap.class);
        classRegistry.registerClass(Color.class);
        classRegistry.registerClass(Dimension.class);
        classRegistry.registerClass(Point.class);
        classRegistry.registerClass(Rectangle.class);
        classRegistry.registerClass(Font.class);
        classRegistry.registerClass(URL.class);
        classRegistry.registerClass(Date.class);
        classRegistry.registerClass(java.sql.Date.class);
        classRegistry.registerClass(SimpleDateFormat.class);
        classRegistry.registerClass(TreeSet.class);
        classRegistry.registerClass(LinkedList.class);
        classRegistry.registerClass(SimpleTimeZone.class);
        classRegistry.registerClass(GregorianCalendar.class);
        classRegistry.registerClass(Vector.class);
        classRegistry.registerClass(Hashtable.class);
        classRegistry.registerClass(BitSet.class);
        classRegistry.registerClass(Locale.class);

        classRegistry.registerClass(StringBuffer.class);
        classRegistry.registerClass(StringBuilder.class);
        classRegistry.registerClass(Object.class);
        classRegistry.registerClass(Object[].class);
        classRegistry.registerClass(Object[][].class);
        classRegistry.registerClass(Object[][][].class);
        classRegistry.registerClass(Object[][][][].class);

        classRegistry.registerClass(byte[].class);
        classRegistry.registerClass(byte[][].class);
        classRegistry.registerClass(byte[][][].class);
        classRegistry.registerClass(byte[][][][].class);
        classRegistry.registerClass(byte[][][][][].class);

        classRegistry.registerClass(char[].class);
        classRegistry.registerClass(char[][].class);
        classRegistry.registerClass(char[][][].class);
        classRegistry.registerClass(char[][][][].class);
        classRegistry.registerClass(char[][][][][].class);
        classRegistry.registerClass(char[][][][][][].class);

        classRegistry.registerClass(short[].class);
        classRegistry.registerClass(short[][].class);
        classRegistry.registerClass(short[][][].class);
        classRegistry.registerClass(short[][][][].class);
        classRegistry.registerClass(short[][][][][].class);
        classRegistry.registerClass(short[][][][][][].class);
        classRegistry.registerClass(short[][][][][][][].class);
        classRegistry.registerClass(short[][][][][][][][].class);

        classRegistry.registerClass(int[].class);
        classRegistry.registerClass(int[][].class);
        classRegistry.registerClass(int[][][].class);
        classRegistry.registerClass(int[][][][].class);
        classRegistry.registerClass(int[][][][][].class);
        classRegistry.registerClass(int[][][][][][].class);
        classRegistry.registerClass(int[][][][][][][].class);
        classRegistry.registerClass(int[][][][][][][][].class);
        classRegistry.registerClass(int[][][][][][][][][].class);
        classRegistry.registerClass(int[][][][][][][][][][].class);

        classRegistry.registerClass(float[].class);
        classRegistry.registerClass(float[][].class);
        classRegistry.registerClass(float[][][].class);
        classRegistry.registerClass(float[][][][].class);
        classRegistry.registerClass(float[][][][][].class);
        classRegistry.registerClass(float[][][][][][].class);
        classRegistry.registerClass(float[][][][][][][].class);
        classRegistry.registerClass(float[][][][][][][][].class);
        classRegistry.registerClass(float[][][][][][][][][].class);

        classRegistry.registerClass(double[].class);
        classRegistry.registerClass(double[][].class);
        classRegistry.registerClass(double[][][].class);
        classRegistry.registerClass(double[][][][].class);
        classRegistry.registerClass(double[][][][][].class);
        classRegistry.registerClass(double[][][][][][].class);
        classRegistry.registerClass(double[][][][][][][].class);


        classRegistry.addSingleSnippet("java.lang");
        classRegistry.addSingleSnippet("java.util");
        classRegistry.addSingleSnippet("java.awt");
        classRegistry.addSingleSnippet("javax.swing");
        classRegistry.addSingleSnippet("java.net");
        classRegistry.addSingleSnippet("java.sql");
        classRegistry.addSingleSnippet("org");
        classRegistry.addSingleSnippet("sun");
        classRegistry.addSingleSnippet("sunw");
        classRegistry.addSingleSnippet("com.oracle");
        classRegistry.addSingleSnippet("com.sun");

    }

    public FSTClazzNameRegistry getClassRegistry() {
        return classRegistry;
    }

    public FSTClazzInfoRegistry getCLInfoRegistry() {
        return serializationInfoRegistry;
    }

    /**
     * mark the given class as being replaceable by an equal instance.
     * E.g. if A=Integer(1) is written and later on an B=Integer(1) is written, after deserializing A == B.
     * This is safe for a lot of immutable classes (A.equals(B) transformed to A == B), e.g. for Number subclasses
     * and String class. See also the EqualnessIsIdentity Annotation
     */
    public void registerAsEqualnessReplaceable(Class cl) {
        getCLInfoRegistry().getCLInfo(cl).equalIsIdentity = true;
    }

    public void registerAsFlat(Class cl) {
        getCLInfoRegistry().getCLInfo(cl).flat = true;
    }

    /**
     * mark the given class as being replaced by a copy of an equal instance.
     * E.g. if A=Dimension(10,10) is written and later on an B=Dimension(10,10) is written, after deserializing B will be copied from A without writing the data of B.
     * This is safe for 99% of the classes e.g. for Number subclasses
     * and String class. See also the EqualnessIsBinary Annotation
     * Note that in addition to equalsness, it is required that A.class == B.class.
     * WARNING: adding collection classes might decrease performance significantly (trade cpu efficiency against size)
     */
    public void registerAsEqualnessCopyable(Class cl) {
        getCLInfoRegistry().getCLInfo(cl).equalIsBinary = true;
    }


    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    public FSTClazzInfo getClassInfo(Class type) {
        return serializationInfoRegistry.getCLInfo(type);
    }

    ThreadLocal<FSTObjectOutput> output = new ThreadLocal<FSTObjectOutput>() {
        @Override
        protected FSTObjectOutput initialValue() {
            return new FSTObjectOutput(FSTConfiguration.this);
        }
    };

    ThreadLocal<FSTObjectInput> input = new ThreadLocal<FSTObjectInput>() {
        @Override
        protected FSTObjectInput initialValue() {
            try {
                return new FSTObjectInput(FSTConfiguration.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given InputStream 'in'
     * @param in
     * @return
     */
    public FSTObjectInput getObjectInput( InputStream in ) {
        FSTObjectInput fstObjectInput = input.get();
        try {
            fstObjectInput.resetForReuse(in);
            return fstObjectInput;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * utility for thread safety and reuse. Do not close the resulting stream. However you should close
     * the given OutputStream 'out'
     * @param out
     * @return
     */
    public FSTObjectOutput getObjectOutput(OutputStream out) {
        FSTObjectOutput fstObjectOutput = output.get();
        fstObjectOutput.resetForReUse(out);
        return fstObjectOutput;
    }

}
