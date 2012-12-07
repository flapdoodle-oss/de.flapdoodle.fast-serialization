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

import de.ruedigermoeller.serialization.util.FSTOutputStream;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 12:26
 * To change this template use File | Settings | File Templates.
 */
public final class FSTObjectOutput extends DataOutputStream implements ObjectOutput {


    static final byte BIG_BOOLEAN_FALSE = -17;
    static final byte BIG_BOOLEAN_TRUE = -16;
    static final byte BIG_CHAR   = -15;
    static final byte BIG_DOUBLE = -14;
    static final byte BIG_FLOAT = -13;
    static final byte BIG_BYTE = -12;
    static final byte BIG_SHORT = -11;
    static final byte BIG_LONG = -10;
    static final byte BIG_INT = -9;
    static final byte COPYHANDLE = -8;
    static final byte HANDLE = -7;
    static final byte ENUM = -6;
    static final byte OBJECT_ARRAY = -5;
//    static final byte STRING = -4;
    static final byte TYPED = -3; // var class == object written class
    //static final byte PRIMITIVE_ARRAY = -2;
    static final byte NULL = -1;
    static final byte OBJECT = 0;

    public static final boolean DUMP = false;

    public FSTClazzNameRegistry clnames; // immutable
    FSTConfiguration conf; // immutable

    FSTObjectRegistry objects;
    FSTOutputStream buffout;

    int curDepth = 0;

    /**
     * Creates a new FSTObjectOutput stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     */
    public FSTObjectOutput(OutputStream out, FSTConfiguration conf) {
        super(null);
        this.conf = conf;

        buffout = (FSTOutputStream) conf.getCachedObject(FSTOutputStream.class);
        if ( buffout == null ) {
            buffout = new FSTOutputStream(1000,out);
        } else {
            buffout.reset();
            buffout.setOutstream(out);
        }
        this.out = buffout;

        objects = (FSTObjectRegistry) conf.getCachedObject(FSTObjectRegistry.class);
        if ( objects == null ) {
            objects = new FSTObjectRegistry(conf);
            objects.disabled = !conf.isShareReferences();
        } else {
            objects.clearForWrite();
        }
        clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
        if ( clnames == null ) {
            clnames = new FSTClazzNameRegistry(conf.getClassRegistry(), conf);
        } else {
            clnames.clear();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        conf.returnObject(buffout,objects,clnames);
    }

//    private void reUse(OutputStream out) {
//        buffout.reset();
//        buffout.setOutstream(out);
//        objects.clear();
//        clnames.clear();
//        written = 0;
//    }

    @Override
    public void writeObject(Object obj) throws IOException {
        writeObject(obj,(Class[])null);
    }

    public void writeObject(Object obj, Class... possibles) throws IOException {
        if ( curDepth != 0 ) {
            throw new RuntimeException("not intended to be called from inside serialization. Use internal method instead");
        }
        curDepth++;
        try {
            if ( possibles != null ) {
                for (int i = 0; i < possibles.length; i++) {
                    Class possible = possibles[i];
                    clnames.registerClass(possible);
                    clnames.addCLNameSnippets(possible);
                }
            }
            writeObjectInternal(obj, possibles);
        } finally {
            buffout.flush();
            curDepth--;
        }
    }

    public void writeObjectInternal(Object obj, Class... possibles) throws IOException {
        if ( curDepth == 0 ) {
            throw new RuntimeException("not intended to be called from external application. Use public writeObject instead");
        }
        FSTClazzInfo.FSTFieldInfo info = new FSTClazzInfo.FSTFieldInfo(possibles, null, conf.getCLInfoRegistry().isIgnoreAnnotations());
        writeObjectWithContext(info, obj);
    }

    int tmp[] = {0};
    protected void writeObjectWithContext(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite) throws IOException {
        if ( toWrite == null ) {
            writeFByte(NULL);
            return;
        }
        final Class clazz = toWrite.getClass();
        if ( clazz == Boolean.class ) {
            if (((Boolean) toWrite).booleanValue()) {
                writeFByte(BIG_BOOLEAN_TRUE);
            } else {
                writeFByte(BIG_BOOLEAN_FALSE);
            }
            return;
        } else
        if ( toWrite instanceof Number ) {
            if ( clazz == Integer.class ) {
                writeFByte(BIG_INT);
                writeCInt(((Integer) toWrite).intValue());
                return;
            } else
            if ( clazz == Long.class ) {
                writeFByte(BIG_LONG);
                writeCLong(((Long) toWrite).longValue());
                return;
            } else
            if ( clazz == Byte.class ) {
                writeFByte(BIG_BYTE);
                writeFByte(((Byte) toWrite).byteValue());
                return;
            } else
            if ( clazz == Double.class ) {
                writeFByte(BIG_DOUBLE);
                writeCDouble(((Double) toWrite).doubleValue());
                return;
            } else
            if ( clazz == Float.class ) {
                writeFByte(BIG_FLOAT);
                writeCFloat(((Float) toWrite).floatValue());
                return;
            } else
            if ( clazz == Character.class ) {
                writeFByte(BIG_CHAR);
                writeCChar(((Character) toWrite).charValue());
                return;
            } else
            if ( clazz == Short.class ) {
                writeFByte(BIG_SHORT);
                writeCShort(((Short) toWrite).shortValue());
                return;
            }
        }
        FSTClazzInfo serializationInfo = null;
        if ( referencee.lastInfo != null && referencee.lastInfo.getClazz() == clazz ) {
            serializationInfo = referencee.lastInfo;
        } else {
            serializationInfo = getClassInfoRegistry().getCLInfo(clazz);
            referencee.lastInfo = serializationInfo;
        }
        int handle = Integer.MIN_VALUE;
        if ( ! referencee.isFlat() ) {
            handle = objects.registerObject(toWrite, false, written,serializationInfo,tmp);
            // determine class header
            if ( handle >= 0 ) {
                final boolean isIdentical = tmp[0] == 0; //objects.getRegisteredObject(handle) == toWrite;
                if ( isIdentical || serializationInfo.isEqualIsIdentity()) {
                    writeFByte(HANDLE);
                    writeCInt(handle);
                    return;
                } else if ( serializationInfo.isEqualIsBinary() ) {
                    writeFByte(COPYHANDLE);
                    writeCInt(handle);
                    // unneccessary objects.registerObject(toWrite, true, written,serializationInfo); // enforce new id, in case another reference to toWrite exists
                    return;
                }
            }
        }
        if (clazz.isArray()) {
            writeFByte(OBJECT_ARRAY);
            writeArray(referencee, toWrite);
//        } else if ( toWrite instanceof String ) {
//            writeFByte(STRING);
//            writeStringUTF((String) toWrite);
        } else if ( toWrite instanceof Enum ) {
            writeFByte(ENUM);
            writeClass(toWrite);
            writeCInt(((Enum) toWrite).ordinal());
        } else {
            // check for custom serializer
            FSTObjectSerializer ser = serializationInfo.getSer();
            if ( ser == null && serializationInfo.getWriteReplaceMethod() != null ) {
                Object replaced = null;
                try {
                    replaced = serializationInfo.getWriteReplaceMethod().invoke(toWrite);
                } catch (IllegalAccessException e) {
                    throw new IOException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if ( replaced != toWrite ) {
                    toWrite = replaced;
                    serializationInfo = getClassInfoRegistry().getCLInfo(toWrite.getClass());
                    // fixme: update object map
                }
            }
            if (ser == null && serializationInfo.useCompatibleMode() ) {
                writeObjectCompatible(referencee, toWrite, serializationInfo);
            } else {
                // Object header (nothing written till here)
                writeObjectHeader(serializationInfo, referencee, toWrite);

                // write object depending on type (custom, externalizable, serializable/java, default)
                if ( ser != null ) {
                    ser.writeObject(this, toWrite, serializationInfo, referencee);
                } else {
                    if ( serializationInfo.isExternalizable() ) {
                        ((Externalizable) toWrite).writeExternal(this);
                    } else {
                        FSTClazzInfo.FSTFieldInfo[] fieldInfo = serializationInfo.getFieldInfo();
                        writeObjectFields(toWrite, serializationInfo, fieldInfo);
                    }
                }
            }
        }
    }

    private void writeObjectCompatible(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo) throws IOException {
        // Object header (nothing written till here)
        writeObjectHeader(serializationInfo, referencee, toWrite);
        if ( FSTObjectOutput.DUMP )
            System.out.println("write compatible :"+toWrite.getClass());
        Class cl = serializationInfo.getClazz();
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl);
    }

    private void writeObjectCompatibleRecursive(FSTClazzInfo.FSTFieldInfo referencee, Object toWrite, FSTClazzInfo serializationInfo, Class cl) throws IOException {
        FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = serializationInfo.compInfo.get(cl);
        if ( ! Serializable.class.isAssignableFrom(cl) ) {
            return;
        }
        writeObjectCompatibleRecursive(referencee,toWrite,serializationInfo,cl.getSuperclass());
        if ( fstCompatibilityInfo != null && fstCompatibilityInfo.getWriteMethod() != null ) {
            try {
                fstCompatibilityInfo.getWriteMethod().invoke(toWrite,getObjectOutputStream(cl, serializationInfo,referencee,toWrite));
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new IOException(e);
            }
        } else {
            if ( fstCompatibilityInfo != null ) {
                writeObjectFields(toWrite, serializationInfo, fstCompatibilityInfo.getFieldArray());
            }
        }
    }

    private void writeObjectFields(Object toWrite, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo[] fieldInfo) throws IOException {
        int booleanMask = 0;
        int boolcount = 0;
        final int length = fieldInfo.length;
        int conditional = 0;
        for (int i = 0; i < length; i++) {
            try {
                final FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if ( DUMP ) System.out.println("WRITE FIELD:"+subInfo.getField().getName());
                final Class<?> fieldType = subInfo.getType();
                if ( fieldType != boolean.class ) {
                    if ( boolcount > 0 ) {
                        writeFByte(booleanMask<<(8-boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                }
                if ( subInfo.isIntegral() && ! subInfo.isArray() ) {
                    switch (subInfo.getIntegralType()) {
                        case FSTClazzInfo.FSTFieldInfo.BOOL: {
                            if ( boolcount == 8 ) {
                                writeFByte(booleanMask<<(8-boolcount));
                                boolcount = 0; booleanMask = 0;
                            }
                            boolean booleanValue = serializationInfo.getBooleanValue(toWrite, subInfo);
                            booleanMask = booleanMask<<1;
                            booleanMask = (booleanMask|(booleanValue?1:0));
                            boolcount++;
                        }
                            break;
                        case FSTClazzInfo.FSTFieldInfo.BYTE:
                            writeFByte(serializationInfo.getByteValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.CHAR:
                            writeCChar((char) serializationInfo.getCharValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.SHORT:
                            writeCShort((short) serializationInfo.getShortValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.INT:
                            writeCInt(serializationInfo.getIntValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.LONG:
                            writeCLong(serializationInfo.getLongValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.FLOAT:
                            writeCFloat(serializationInfo.getFloatValue(toWrite, subInfo));
                            break;
                        case FSTClazzInfo.FSTFieldInfo.DOUBLE:
                            writeCDouble(serializationInfo.getDoubleValue(toWrite,subInfo));
                            break;
                    }
                } else {
                    if (subInfo.isConditional()) {
                        conditional = buffout.pos;
                        buffout.pos +=4;
                    }
                    // object
                    Object subObject = serializationInfo.getObjectValue(toWrite,subInfo);
                    if ( subObject == null ) {
                        writeFByte(NULL);
                    } else {
                        writeObjectWithContext(subInfo, subObject);
                    }
                    if ( conditional != 0 ) {
                        int v = buffout.pos;
                        buffout.buf[conditional] = (byte) ((v >>> 24) & 0xFF);
                        buffout.buf[conditional+1] = (byte) ((v >>> 16) & 0xFF);
                        buffout.buf[conditional+2] = (byte) ((v >>>  8) & 0xFF);
                        buffout.buf[conditional+3] = (byte) ((v >>> 0) & 0xFF);
                        conditional = 0;
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new IOException(ex);
            }
        }
        if ( boolcount > 0 ) {
            writeFByte(booleanMask<<(8-boolcount));
            boolcount = 0; booleanMask = 0;
        }
    }

    // write identical to other version, but take field values from hashmap
    private void writeCompatibleObjectFields(Object toWrite, Map fields, FSTClazzInfo.FSTFieldInfo[] fieldInfo) throws IOException {
        int booleanMask = 0;
        int boolcount = 0;
        if ( fieldInfo.length != fields.size() ) {
            System.out.println("=(((((((((((((((((((((((((((((((((((((((((((((((");
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            try {
                FSTClazzInfo.FSTFieldInfo subInfo = fieldInfo[i];
                if ( ! fields.containsKey(subInfo.getField().getName() )) {
                    System.out.println("(((((((((((((((((((((((((((((((((((((((((((");
                }
                if ( DUMP ) {
                    System.out.println("WRITE FIELD:"+subInfo.getField().getName());
                }
                Class subInfType = subInfo.getType();
                if ( subInfType != boolean.class ) {
                    if ( boolcount > 0 ) {
                        writeFByte(booleanMask<<(8-boolcount));
                        boolcount = 0; booleanMask = 0;
                    }
                }
                if ( subInfo.isIntegral() && ! subInfo.isArray() ) {
                    if ( subInfType == boolean.class ) {
                        if ( boolcount == 8 ) {
                            writeFByte(booleanMask<<(8-boolcount));
                            boolcount = 0; booleanMask = 0;
                        }
                        boolean booleanValue = ((Boolean)fields.get(subInfo.getField().getName())).booleanValue();
                        booleanMask = booleanMask<<1;
                        booleanMask = (booleanMask|(booleanValue?1:0));
                        boolcount++;
                    } else
                    if ( subInfType == int.class ) {
                        writeCInt(((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == long.class ) {
                        writeCLong(((Number) fields.get(subInfo.getField().getName())).longValue());
                    } else
                    if ( subInfType == byte.class ) {
                        writeFByte(((Number) fields.get(subInfo.getField().getName())).byteValue());
                    } else
                    if ( subInfType == char.class ) {
                        writeCChar((char) ((Number) fields.get(subInfo.getField().getName())).intValue());
                    } else
                    if ( subInfType == short.class ) {
                        writeCShort(((Number) fields.get(subInfo.getField().getName())).shortValue());
                    } else
                    if ( subInfType == float.class ) {
                        writeCFloat(((Number)fields.get(subInfo.getField().getName())).floatValue());
                    } else
                    if ( subInfType == double.class ) {
                        writeCDouble(((Number)fields.get(subInfo.getField().getName())).doubleValue());
                    }
                } else {
                    // object
                    Object subObject = fields.get(subInfo.getField().getName());
                    writeObjectWithContext(subInfo, subObject);
                }
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        if ( boolcount > 0 ) {
            writeFByte(booleanMask<<(8-boolcount));
            boolcount = 0; booleanMask = 0;
        }
    }

    protected void writeObjectHeader(final FSTClazzInfo clsInfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        if (clsInfo.isEqualIsBinary()) {
            writeFByte(OBJECT);
            writeClass(toWrite);
            return;
        }
        if ( toWrite instanceof Serializable == false ) {
            throw new RuntimeException(toWrite.getClass().getName()+" is not serializable. referenced by "+referencee.getDesc());
        }
        if ( toWrite.getClass() == referencee.getType() && ! clsInfo.useCompatibleMode() ) {
            writeFByte(TYPED);
        } else {
            final Class[] possibleClasses = referencee.getPossibleClasses();
            if ( possibleClasses == null ) {
                writeFByte(OBJECT);
                //writeClass(toWrite); inline
                clnames.encodeClass(this,toWrite.getClass());
            } else {
                final int length = possibleClasses.length;
                for (int j = 0; j < length; j++) {
                    final Class possibleClass = possibleClasses[j];
                    if ( possibleClass == toWrite.getClass() ) {
                        writeFByte(j+1);
                        return;
                    }
                }
                writeFByte(OBJECT);
                //writeClass(toWrite); inline
                clnames.encodeClass(this, toWrite.getClass());
            }
        }
    }

    private void writeArray(FSTClazzInfo.FSTFieldInfo referencee, Object array) throws IOException {
        if ( array == null ) {
            writeCInt(-1);
            return;
        }

        final int len = Array.getLength(array);
        writeCInt(len);
        writeClass(array);
        Class<?> componentType = array.getClass().getComponentType();
        if ( ! componentType.isArray() ) {
            if ( componentType == byte.class ) {
                byte[] arr = (byte[])array;
                write(arr);
            } else
            if ( componentType == char.class ) {
                char[] arr = (char[])array;
                for ( int i = 0; i < len; i++ )
                    writeCChar(arr[i]);
            } else
            if ( componentType == short.class ) {
                short[] arr = (short[])array;
                for ( int i = 0; i < len; i++ )
                    writeFShort(arr[i]);
            } else
            if ( componentType == int.class ) {
                if ( referencee.isThin() ) {
                    writeFIntThin((int[]) array);
                } else {
                    writeFInt((int[])array);
                }
            } else
            if ( componentType == double.class ) {
                double[] arr = (double[])array;
                for ( int i = 0; i < len; i++ )
                    writeFDouble(arr[i]);
            } else
            if ( componentType == float.class ) {
                float[] arr = (float[])array;
                for ( int i = 0; i < len; i++ )
                    writeFFloat(arr[i]);
            } else
            if ( componentType == long.class ) {
                long[] arr = (long[])array;
                for ( int i = 0; i < len; i++ )
                    writeFLong(arr[i]);
            } else
            if ( componentType == boolean.class ) {
                boolean[] arr = (boolean[])array;
                for ( int i = 0; i < len; i++ )
                    writeBoolean(arr[i]);
            } else {
                Object arr[] = (Object[])array;
                if ( referencee.isThin() ) {
                    for ( int i = 0; i < len; i++ )
                    {
                        Object toWrite = arr[i];
                        if ( toWrite != null ) {
                            writeCInt(i);
                            writeObjectWithContext(referencee, toWrite);
                        }
                    }
                    writeCInt(len);
                } else {
                    for ( int i = 0; i < len; i++ )
                    {
                        Object toWrite = arr[i];
                        if ( toWrite == null ) {
                            writeFByte(NULL);
                        } else {
                            writeObjectWithContext(referencee, toWrite);
                        }
                    }
                }
//                Class[] possibleClasses = null;
//                if ( referencee.getPossibleClasses() == null ) {
//                    possibleClasses = new Class[5];
//                } else {
//                    possibleClasses = Arrays.copyOf(referencee.getPossibleClasses(),referencee.getPossibleClasses().length+5);
//                }
//                FSTClazzInfo.FSTFieldInfo newFI = new FSTClazzInfo.FSTFieldInfo(false, possibleClasses, null);
//                for ( int i = 0; i < len; i++ )
//                {
//                    Object toWrite = Array.get(array, i);
//                    writeObjectWithContext(newFI, toWrite);
//                    if ( toWrite != null ) {
//                        newFI.setPossibleClasses(addToPredictionArray(newFI.getPossibleClasses(), toWrite.getClass()));
//                    }
//                }
            }
        } else {
            Object[] arr = (Object[])array;
            for ( int i = 0; i < len; i++ ) {
                Object subArr = arr[i];
                if ( subArr != null ) {
                    if ( ! FSTUtil.isPrimitiveArray(subArr.getClass()) ) {
                        objects.registerObject(subArr, true, written, null, tmp); // fixme: shared refs
                    }
                }
                writeArray(new FSTClazzInfo.FSTFieldInfo(referencee.getPossibleClasses(), null, conf.getCLInfoRegistry().isIgnoreAnnotations()), subArr);
            }
        }
    }

    public void writeStringUTF(String str) throws IOException {
        final int strlen = str.length();

        writeCInt(strlen);
        buffout.ensureFree(strlen*3);

        final byte[] bytearr = buffout.buf;
        int count = buffout.pos;

        for (int i=0; i<strlen; i++) {
            final int c = str.charAt(i);
            if ( c < 255 ) {
                bytearr[count++] = (byte)c;
                written++;
            } else {
                bytearr[count++] = (byte) 255;
                bytearr[count++] = (byte) ((c >>> 8) & 0xFF);
                bytearr[count++] = (byte) ((c >>> 0) & 0xFF);
                written += 3;
            }
        }
        buffout.pos = count;
    }

    public final void writeClass(Object toWrite) throws IOException {
        clnames.encodeClass(this,toWrite.getClass());
    }

    public static Class[] addToPredictionArray(Class[] possibleClasses, Class aClass) {
        if ( possibleClasses == null ) {
            return new Class[] {aClass};
        }
        for (int i = 0; i < possibleClasses.length; i++) {
            Class possibleClass = possibleClasses[i];
            if ( aClass == possibleClass ) {
                return possibleClasses;
            }
            if ( possibleClass == null ) {
                possibleClasses[i] = aClass;
                return possibleClasses;
            }
        }
        Class[] newPoss = Arrays.copyOf(possibleClasses,possibleClasses.length+5);
        newPoss[possibleClasses.length] = aClass;
        return newPoss;
    }

    public void writeCShort(short c) throws IOException {
        if ( c < 255 && c >= 0 ) {
            writeFByte(c);
        } else {
            writeFByte(255);
            writeFShort(c);
        }
    }

    public void writeCChar(char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if ( c < 255 && c >= 0 ) {
            buffout.ensureFree(1);
            buffout.buf[buffout.pos++] = (byte)c;
            written++;
        } else {
            buffout.ensureFree(3);
            byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = (byte) 255;
            buf[count++] = (byte) ((c >>>  8) & 0xFF);
            buf[count++] = (byte) ((c >>> 0) & 0xFF);
            buffout.pos += 3;
            written += 3;
        }
    }

    public void writeFChar( int v ) {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public void writeFShort( int v ) {
        buffout.ensureFree(2);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 2;
        written += 2;
    }

    public final void writeFByte( int v ) {
        if ( buffout.buf.length <= buffout.pos +1 )
        {
            buffout.ensureFree(1);
        }
        buffout.buf[buffout.pos++] = (byte)v;
        written++;
    }

    public void writeFInt( int v ) {
        buffout.ensureFree(4);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 4;
        written += 4;
    }

    public void writeFLong( long v ) {
        buffout.ensureFree(8);
        byte[] buf = buffout.buf;
        int count = buffout.pos;
        buf[count++] = (byte)(v >>> 56);
        buf[count++] = (byte)(v >>> 48);
        buf[count++] = (byte)(v >>> 40);
        buf[count++] = (byte)(v >>> 32);
        buf[count++] = (byte) ((v >>> 24) & 0xFF);
        buf[count++] = (byte) ((v >>> 16) & 0xFF);
        buf[count++] = (byte) ((v >>>  8) & 0xFF);
        buf[count++] = (byte) ((v >>> 0) & 0xFF);
        buffout.pos += 8;
        written += 8;
    }

    public void writeFIntThin( int v[] ) throws IOException {
        final int length = v.length;
        for (int i = 0; i < length; i++) {
            final int anInt = v[i];
            if ( anInt != 0 ) {
                writeCInt(i);
                writeCInt(anInt);
            }
        }
        writeCInt(length);
    }

    public void writeFInt( int v[] ) {
        final int free = 5 * v.length;
        buffout.ensureFree(free);
        final byte[] buf = buffout.buf;
        int count = buffout.pos;
        for (int i = 0; i < v.length; i++) {
            final int anInt = v[i];
            if ( anInt > -127 && anInt <=127 ) {
                buffout.buf[count++] = (byte)anInt;
                written++;
            } else
            if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
                buf[count++] = -128;
                buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written += 3;
            } else {
                buf[count++] = -127;
                buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
                buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
                buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
                written += 5;
            }
        }
        buffout.pos = count;
    }


    public void writeCInt(int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if ( anInt > -127 && anInt <=127 ) {
            if ( buffout.buf.length <= buffout.pos +1 )
            {
                buffout.ensureFree(1);
            }
            buffout.buf[buffout.pos++] = (byte)anInt;
            written++;
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            if ( buffout.buf.length <= buffout.pos +2 )
            {
                buffout.ensureFree(3);
            }
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -128;
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos += 3;
            written += 3;
        } else {
            buffout.ensureFree(5);
            final byte[] buf = buffout.buf;
            int count = buffout.pos;
            buf[count++] = -127;
            buf[count++] = (byte) ((anInt >>> 24) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 16) & 0xFF);
            buf[count++] = (byte) ((anInt >>>  8) & 0xFF);
            buf[count++] = (byte) ((anInt >>> 0) & 0xFF);
            buffout.pos = count;
            written += 5;
        }
    }

    public void writeFFloat (float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    /** Writes a 4 byte float. */
    public void writeCFloat (float value) throws IOException {
        writeFInt(Float.floatToIntBits(value));
    }

    public void writeCDouble (double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeFDouble (double value) throws IOException {
        writeFLong(Double.doubleToLongBits(value));
    }

    public void writeCLong(long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if ( anInt > -126 && anInt <=127 ) {
            writeFByte((int) anInt);
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            writeFByte(-128);
            writeFShort((int) anInt);
        } else if ( anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE ) {
            writeFByte(-127);
            writeFInt((int) anInt);
        } else {
            writeFByte(-126);
            writeFLong(anInt);
        }
    }

    /**
     * for internal use only, the state of the outputstream is not reset properly
     */
    void reset() {
        written = 0;
        buffout.reset();
    }

    public FSTClazzInfoRegistry getClassInfoRegistry() {
        return conf.getCLInfoRegistry();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////// java serialization compatibility ////////////////////////////////////////////

    /**
     *
     * @param cl - class or superclass of currently serialized obj, write declared fields of this class only
     * @param clinfo
     * @param referencee
     * @param toWrite
     * @return
     * @throws IOException
     */
    public ObjectOutputStream getObjectOutputStream(final Class cl, final FSTClazzInfo clinfo, final FSTClazzInfo.FSTFieldInfo referencee, final Object toWrite) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream() {
            @Override
            public void useProtocolVersion(int version) throws IOException {
            }

            @Override
            protected void writeObjectOverride(Object obj) throws IOException {
                FSTObjectOutput.this.writeObjectInternal(obj, referencee.getPossibleClasses());
            }

            @Override
            public void writeUnshared(Object obj) throws IOException {
                writeObjectOverride(obj); // fixme
            }

            @Override
            public void defaultWriteObject() throws IOException {
                FSTClazzInfo newInfo = clinfo;//FIXME: only fields of current subclass
                Object replObj = toWrite;
                if ( newInfo.getWriteReplaceMethod() != null ) {
                    System.out.println("WARNING: WRITE REPLACE NOT SUPPORTED");
                    try {
                        Object replaced = newInfo.getWriteReplaceMethod().invoke(replObj);
                        if ( replaced != null && replaced != toWrite ) {
                            replObj = replaced;
                            newInfo = getClassInfoRegistry().getCLInfo(replObj.getClass());
                        }
                    } catch (IllegalAccessException e) {
                        throw new IOException(e);
                    } catch (InvocationTargetException e) {
                        throw new IOException(e);
                    }
                }
                FSTObjectOutput.this.writeObjectFields(replObj, newInfo, newInfo.compInfo.get(cl).getFieldArray());
            }

            PutField pf;
            HashMap<String,Object> fields = new HashMap<String, Object>();
            @Override
            public PutField putFields() throws IOException {
                if ( pf == null ) {
                    pf = new PutField() {
                        @Override
                        public void put(String name, boolean val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, byte val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, char val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, short val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, int val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, long val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, float val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, double val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void put(String name, Object val) {
                            fields.put(name,val);
                        }

                        @Override
                        public void write(ObjectOutput out) throws IOException {
                            throw new IOException("cannot act compatible, use a custom serializer for this class");
                        }
                    };
                }
                return pf;
            }

            @Override
            public void writeFields() throws IOException {
                FSTClazzInfo.FSTCompatibilityInfo fstCompatibilityInfo = clinfo.compInfo.get(cl);
                if ( fstCompatibilityInfo.isAsymmetric() ) {
                    FSTObjectOutput.this.writeCompatibleObjectFields(toWrite, fields, fstCompatibilityInfo.getFieldArray());
                } else {
                    FSTObjectOutput.this.writeObjectInternal(fields, HashMap.class);
                }
            }

            @Override
            public void reset() throws IOException {
                throw new IOException("cannot act compatible, use a custom serializer for this class");
            }

            @Override
            public void write(int val) throws IOException {
                FSTObjectOutput.this.writeCInt(val);
            }

            @Override
            public void write(byte[] buf) throws IOException {
                FSTObjectOutput.this.write(buf);
            }

            @Override
            public void write(byte[] buf, int off, int len) throws IOException {
                FSTObjectOutput.this.write(buf, off, len);
            }

            @Override
            public void flush() throws IOException {
                FSTObjectOutput.this.flush();
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public void writeBoolean(boolean val) throws IOException {
                FSTObjectOutput.this.writeBoolean(val);
            }

            @Override
            public void writeByte(int val) throws IOException {
                FSTObjectOutput.this.writeFByte(val);
            }

            @Override
            public void writeShort(int val) throws IOException {
                FSTObjectOutput.this.writeFShort(val);
            }

            @Override
            public void writeChar(int val) throws IOException {
                FSTObjectOutput.this.writeFChar(val);
            }

            @Override
            public void writeInt(int val) throws IOException {
                FSTObjectOutput.this.writeFInt(val);
            }

            @Override
            public void writeLong(long val) throws IOException {
                FSTObjectOutput.this.writeFLong(val);
            }

            @Override
            public void writeFloat(float val) throws IOException {
                FSTObjectOutput.this.writeFFloat(val);
            }

            @Override
            public void writeDouble(double val) throws IOException {
                FSTObjectOutput.this.writeFDouble(val);
            }

            @Override
            public void writeBytes(String str) throws IOException {
                FSTObjectOutput.this.writeBytes(str);
            }

            @Override
            public void writeChars(String str) throws IOException {
                FSTObjectOutput.this.writeChars(str);
            }

            @Override
            public void writeUTF(String str) throws IOException {
                FSTObjectOutput.this.writeUTF(str);
            }
        };

        return out;
    }

    public FSTObjectRegistry getObjectMap() {
        return objects;
    }

    public byte[] getBuffer() {
        return buffout.buf;
    }
}