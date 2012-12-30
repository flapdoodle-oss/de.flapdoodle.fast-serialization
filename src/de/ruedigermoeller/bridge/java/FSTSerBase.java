package de.ruedigermoeller.bridge.java;

import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 25.12.12
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class FSTSerBase {

    static final byte BIG_BOOLEAN_FALSE = -17;
    static final byte BIG_BOOLEAN_TRUE = -16;
    static final byte BIG_INT = -9;
    static final byte COPYHANDLE = -8;
    static final byte HANDLE = -7;
    static final byte ENUM = -6;
    static final byte ARRAY = -5;
    //    static final byte STRING = -4;
    static final byte TYPED = -3; // var class == object written class
    //static final byte PRIMITIVE_ARRAY = -2;
    static final byte NULL = -1;
    static final byte OBJECT = 0;

    protected FSTJavaFactory fac;

    public FSTSerBase(FSTJavaFactory fac) {
        this.fac = fac;
    }

    public Object decodeObject( FSTCountingInputStream in ) throws IOException {
        int streampos = in.getCount();
        byte header = (byte) in.read();
        switch (header) {
            case OBJECT: {
                int clz = readCShort(in);
                Object obj = fac.instantiate(clz, in, this, streampos);
                if ( obj != null ) {
                    fac.getObjectMap().put(streampos,obj);
                    if (obj instanceof FSTSerBase) {
                        ((FSTSerBase) obj).decode(in);
                    } else if (obj.getClass().isArray() ) {
                        readArray(obj,in);
                    }
                } else {
                    throw new RuntimeException("unknown class id "+clz);
                }
                return obj;
            }
            case NULL:
                return null;
            case HANDLE:
                int ha = readCInt(in);
                return fac.getObjectMap().get(ha);
            case BIG_INT:
                return new Integer(readCInt(in));
            case ENUM:
                int clzId = readCShort(in); // skip enum clz
                String s = readStringUTF(in);
                fac.objectMap.put(streampos,s);
                return s;
            case ARRAY: {
                int clsId = readCShort(in);
                Object obj = fac.instantiate(clsId, in, this, streampos);
                readArray(obj, in);
                return obj;
            }
            case COPYHANDLE:
                throw new RuntimeException("class has been written using a non-cross-language configuration");
        }
        return null;
    }


    public void readArray(Object array, FSTCountingInputStream in) throws IOException {
        Class arrType = array.getClass().getComponentType();
        if (arrType == byte.class) {
            byte[] arr = (byte[]) array;
            in.read(arr);
        } else if (arrType == char.class) {
            char[] arr = (char[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readCChar(in);
            }
        } else if (arrType == short.class) {
            short[] arr = (short[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readShort(in);
            }
        } else if (arrType == int.class) {
            final int[] arr = (int[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readCInt(in);
            }
        } else if (arrType == float.class) {
            float[] arr = (float[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readCFloat(in);
            }
        } else if (arrType == double.class) {
            double[] arr = (double[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readCDouble(in);
            }
        } else if (arrType == long.class) {
            long[] arr = (long[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = readLong(in);
            }
        } else if (arrType == boolean.class) {
            boolean[] arr = (boolean[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = in.read() != 0;
            }
        } else if (Object.class.isAssignableFrom(arrType)) {
            Object[] arr = (Object[]) array;
            for (int j = 0; j < arr.length; j++) {
                arr[j] = decodeObject(in);
            }
        } else {
            throw new RuntimeException("unexpected array type " + arrType);
        }
    }

    public void encodeObject( FSTCountingOutputStream out, Object toWrite ) throws IOException {
        int streampos = out.getWritten();
        if ( toWrite instanceof Enum) {
            out.write(ENUM);
            // FIXME: how to remember string was enum ?
        } else if (toWrite == null ) {
            out.write(NULL);
        } else if ( toWrite.getClass() == Integer.class ) {
            out.write(BIG_INT);
            writeCInt(out,((Integer)toWrite).intValue());
        } else if ( toWrite.getClass() == Boolean.class ) {
            out.write(((Boolean)toWrite) ? BIG_BOOLEAN_TRUE:BIG_BOOLEAN_FALSE);
        } else if ( toWrite.getClass().isArray() ) {
            out.write(ARRAY);
            int clzId = fac.getId(toWrite.getClass());
            if ( clzId <= 0 ) {
                throw new RuntimeException("unregistered class "+toWrite.getClass().getName()+", cannot write");
            }
            writeCShort(out, (short) clzId);
            writeArray(toWrite,out);
            // TODO
        } else {
            out.write(OBJECT);
            if ( toWrite instanceof FSTSerBase ) {
                ((FSTSerBase) toWrite).encode(out);
            } else {
                int clzId = fac.getId(toWrite.getClass());
                if ( clzId <= 0 ) {
                    throw new RuntimeException("unregistered class "+toWrite.getClass().getName()+", cannot write");
                }
                writeCShort(out, (short) clzId);
                if ( toWrite instanceof Character ) {
                    writeCChar(out, (Character) toWrite);
                } else if ( toWrite instanceof Short ) {
                    writeCShort(out, (Short) toWrite );
                } else if ( toWrite instanceof Long ) {
                    writeCLong(out, (Long) toWrite);
                } else if ( toWrite instanceof Float ) {
                    writeCFloat( out, (Float) toWrite);
                } else if ( toWrite instanceof Double ) {
                    writeCDouble( out, (Double) toWrite);
                } else if ( toWrite instanceof String ) {
                    writeStringUTF(out, (String) toWrite);
                }
                // built in + sysclasses
            }
            //case HANDLE:
            // todo
        }
    }

    public void writeStringUTF(OutputStream out, String str) throws IOException {
        final int strlen = str.length();

        writeCInt(out,strlen);
        for (int i=0; i<strlen; i++) {
            final int c = str.charAt(i);
            if ( c < 255 ) {
                out.write(c);
            } else {
                out.write(255);
                out.write(((c >>> 8) & 0xFF));
                out.write(((c >>> 0) & 0xFF));
            }
        }
    }

    public void writeArray(Object array, OutputStream out) throws IOException {
        Class arrType = array.getClass().getComponentType();
        if (arrType == byte.class) {
            byte[] arr = (byte[]) array;
            writeCInt(out, arr.length);
            out.write(arr);
        } else if (arrType == char.class) {
            char[] arr = (char[]) array;
            writeCInt(out, arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeCChar( out, arr[j] );
            }
        } else if (arrType == short.class) {
            short[] arr = (short[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeShort(out, arr[j]);
            }
        } else if (arrType == int.class) {
            final int[] arr = (int[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeCInt(out, arr[j]);
            }
        } else if (arrType == float.class) {
            float[] arr = (float[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeCFloat(out, arr[j]);
            }
        } else if (arrType == double.class) {
            double[] arr = (double[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeCDouble(out, arr[j]);
            }
        } else if (arrType == long.class) {
            long[] arr = (long[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                writeLong(out, arr[j]);
            }
        } else if (arrType == boolean.class) {
            boolean[] arr = (boolean[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                out.write( arr[j] ? 1 : 0 );
            }
        } else if (Object.class.isAssignableFrom(arrType)) {
            Object[] arr = (Object[]) array;
            writeCInt(out,arr.length);
            for (int j = 0; j < arr.length; j++) {
                encodeObject((FSTCountingOutputStream) out, arr[j]);
            }
        } else {
            throw new RuntimeException("unexpected array type " + arrType);
        }
    }

    public String readStringUTF(InputStream in) throws IOException {
        int len = readCInt(in);
        char charBuf[] = new char[len * 3];
        int chcount = 0;
        for (int i = 0; i < len; i++) {
            char head = (char) in.read();
            if (head >= 0 && head < 255) {
                charBuf[chcount++] = head;
            } else {
                int ch1 = in.read();
                int ch2 = in.read();
                charBuf[chcount++] = (char) ((ch1 << 8) + (ch2 << 0));
            }
        }
        return new String(charBuf, 0, chcount);
    }

    public void encode(FSTCountingOutputStream out) throws IOException {
    }

    public void decode(FSTCountingInputStream in) throws IOException {
    }

    public void writeCShort(OutputStream out, short c) throws IOException {
        if ( c < 255 && c >= 0 ) {
            out.write(c);
        } else {
            out.write(255);
            writeShort(out, c);
        }
    }

    public void writeByte( OutputStream out, int v ) throws IOException {
        out.write(v & 0xFF);
    }

    public void writeShort( OutputStream out, int v ) throws IOException {
        out.write((v >>>  8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
    }

    public void writeCChar( OutputStream out, char c) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if ( c < 255 && c >= 0 ) {
            out.write( c );
        } else {
            out.write( 255 );
            out.write(((c >>>  8) & 0xFF) );
            out.write( ((c >>> 0) & 0xFF) );
        }
    }

    public void writeCInt(OutputStream out, int anInt) throws IOException {
        // -128 = short byte, -127 == 4 byte
        if ( anInt > -127 && anInt <=127 ) {
            out.write( anInt );
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            out.write( -128 );
            out.write( ((anInt >>>  8) & 0xFF) );
            out.write( (byte) ((anInt >>> 0) & 0xFF) );
        } else {
            out.write( -127 );
            out.write( ((anInt >>> 24) & 0xFF) );
            out.write( (anInt >>> 16) & 0xFF);
            out.write( (anInt >>>  8) & 0xFF);
            out.write( (anInt >>> 0) & 0xFF);
        }
    }

    /** Writes a 4 byte float. */
    public void writeCFloat (OutputStream out,float value) throws IOException {
        writeInt( out, Float.floatToIntBits(value));
    }

    public void writeCDouble (OutputStream out,double value) throws IOException {
        writeLong( out, Double.doubleToLongBits(value) );
    }

    public void writeInt( OutputStream out, int v ) throws IOException {
        out.write( ((v >>> 24) & 0xFF) );
        out.write( ((v >>> 16) & 0xFF) );
        out.write( ((v >>>  8) & 0xFF) );
        out.write( ((v >>> 0) & 0xFF) );
    }

    public void writeLong( OutputStream out, long v ) throws IOException {
        out.write( (int)(v >>> 56) );
        out.write( (int)(v >>> 48) );
        out.write( (int)(v >>> 40) );
        out.write( (int)(v >>> 32) );
        out.write( (int)((v >>> 24) & 0xFF) );
        out.write( (int)((v >>> 16) & 0xFF) );
        out.write( (int)((v >>>  8) & 0xFF) );
        out.write( (int)((v >>>  0) & 0xFF) );
    }

    public void writeCLong( OutputStream out, long anInt) throws IOException {
// -128 = short byte, -127 == 4 byte
        if ( anInt > -126 && anInt <=127 ) {
            out.write((int) anInt);
        } else
        if ( anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE ) {
            out.write(-128);
            writeShort( out, (int) anInt);
        } else if ( anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE ) {
            out.write(-127);
            writeInt( out,(int) anInt);
        } else {
            out.write(-126);
            writeLong(out,anInt);
        }
    }


    public final int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    private void readCIntArr(InputStream in, int len, int[] arr) throws IOException {
        for (int j = 0; j < len; j++) {
            final int head = in.read();
            // -128 = short byte, -127 == 4 byte
            if (head > -127 && head <= 127) {
                arr[j] = head;
                continue;
            }
            if (head == -128) {
                final int ch1 = in.read();
                final int ch2 = in.read();
                arr[j] = (short)((ch1 << 8) + (ch2 << 0));
                continue;
            } else {
                int ch1 = in.read();
                int ch2 = in.read();
                int ch3 = in.read();
                int ch4 = in.read();
                arr[j] = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            }
        }
    }

    public byte readByte(InputStream in) throws IOException {
        return (byte) in.read();
    }

    public final int readCInt(InputStream in) throws IOException {
        final byte head = (byte) in.read();
        // -128 = short byte, -127 == 4 byte
        if (head > -127 && head <= 127) {
            return head;
        }
        if (head == -128) {
            final int ch1 = in.read();
            final int ch2 = in.read();
            return (short)((ch1 << 8) + (ch2 << 0));
        } else {
            int ch1 = in.read();
            int ch2 = in.read();
            int ch3 = in.read();
            int ch4 = in.read();
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
    }

    public long readLong(InputStream in) throws IOException {
        return (((long)in.read() << 56) +
                ((long)(in.read() & 255) << 48) +
                ((long)(in.read() & 255) << 40) +
                ((long)(in.read() & 255) << 32) +
                ((long)(in.read() & 255) << 24) +
                ((in.read() & 255) << 16) +
                ((in.read() & 255) <<  8) +
                ((in.read() & 255) <<  0));
    }

    public long readCLong(InputStream in) throws IOException {
        byte head = (byte) in.read();
        // -128 = short byte, -127 == 4 byte
        if (head > -126 && head <= 127) {
            return head;
        }
        if (head == -128) {
            return readShort(in);
        } else if (head == -127) {
            return readInt(in);
        } else {
            return readLong(in);
        }
    }


    public char readCChar(InputStream in) throws IOException {
        char head = (char) (in.read());
        // -128 = short byte, -127 == 4 byte
        if (head >= 0 && head < 255) {
            return head;
        }
        return readChar(in);
    }

    /**
     * Reads a 4 byte float.
     */
    public float readCFloat(InputStream in) throws IOException {
        return Float.intBitsToFloat(readInt(in));
    }

    /**
     * Reads an 8 bytes double.
     */
    public double readCDouble(InputStream in) throws IOException {
        return Double.longBitsToDouble(readLong(in));
    }

    public short readCShort(InputStream in) throws IOException {
        int head = in.read();
        // -128 = short byte, -127 == 4 byte
        if (head >= 0 && head < 255) {
            return (short) head;
        }
        return readShort(in);
    }

    public short readShort(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public final char readChar(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

}
