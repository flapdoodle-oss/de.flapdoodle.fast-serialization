package de.ruedigermoeller.serialization.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 19.11.12
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public final class FSTOutputStream extends OutputStream {
    /**
     * The buffer where data is stored.
     */
    public byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    public int pos;
    OutputStream outstream;

    public FSTOutputStream(OutputStream out) {
        this(32,out);
    }

    public FSTOutputStream(int size, OutputStream out) {
        buf = new byte[size];
        outstream = out;
    }

    public OutputStream getOutstream() {
        return outstream;
    }

    public void setOutstream(OutputStream outstream) {
        this.outstream = outstream;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public final void ensureFree(int free) {
        ensureCapacity(pos +free);
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity * 2;
        if ( oldCapacity < 1001 ) {
            newCapacity = 4000; // large step initially
        }
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        buf = Arrays.copyOf(buf, newCapacity);
    }

    public void write(int b) {
        ensureCapacity(pos + 1);
        buf[pos] = (byte) b;
        pos += 1;
    }

    public void write(byte b[], int off, int len) {
        ensureCapacity(pos + len);
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    public void copyTo(OutputStream out) throws IOException {
        out.write(buf, 0, pos);
    }

    public void reset() {
        pos = 0;
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, pos);
    }

    public int size() {
        return pos;
    }

    public void close() throws IOException {
        flush();
        outstream.close();
    }

    public void flush() throws IOException {
        if ( pos > 0 && outstream != null) {
            copyTo(outstream);
            reset();
        }
    }

}
