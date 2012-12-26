package de.ruedigermoeller.bridge.java;

import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 25.12.12
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class FSTSerBase {

    protected FSTJavaFactory fac;

    public FSTSerBase(FSTJavaFactory fac) {
        this.fac = fac;
    }

    public Object decodeObject( InputStream in ) {
        return null;
    }

    public void encodeObject( OutputStream out ) {

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
        final int head = in.read();
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
        int head = in.read();
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
