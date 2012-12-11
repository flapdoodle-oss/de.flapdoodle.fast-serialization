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
package de.ruedigermoeller.serialization.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 27.11.12
 * Time: 00:35
 * To change this template use File | Settings | File Templates.
 */
public final class FSTInputStream extends InputStream {

    public int chunkSize = 500;
    public byte buf[];
    public  int pos;
    public  int count;
    boolean eof = false;
    InputStream in;

    public FSTInputStream(InputStream in) throws IOException {
        initFromStream(in);
    }

    public void initFromStream(InputStream in) throws IOException {
        this.in = in;
        if (buf==null) {
            buf = new byte[chunkSize];
        }
        ensureReadAhead(chunkSize);
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunk_size) {
        this.chunkSize = chunk_size;
    }

    public void ensureReadAhead(int len) throws IOException {
        final int target = count+len;
        if ( eof ) {
            if ( buf.length < target ) {
                byte newBuf[] = new byte[target+1];
                System.arraycopy(buf,0,newBuf,0,buf.length);
                buf = newBuf;
//                for (int i=count; i < target; i++ ) {
//                    buf[i] = -1;
//                }
                count = target;
            }
            return;
        }
        while( count < target ) {
            if ( buf.length < count+ chunkSize) {
                byte newBuf[] = new byte[buf.length*2];
                System.arraycopy(buf,0,newBuf,0,buf.length);
                buf = newBuf;
            }
            int read = in.read(buf,count, chunkSize);
            if ( read >= 0 ) {
                count += read;
            }
            if ( read != chunkSize) {
                eof = true;
                if ( count < target ) {
                    if ( buf.length < target ) {
                        byte newBuf[] = new byte[target];
                        System.arraycopy(buf,0,newBuf,0,buf.length);
                        buf = newBuf;
                    }
//                    for (int i=count; i < target; i++ ) {
//                        buf[i] = -1;
//                    }
                    count = target;
                }
                return;
            }
        }
    }

    public FSTInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public FSTInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
    }

    byte streamStack[][] = new byte[30][];
    int countStack[] = new int[30];
    int posStack[] = new int[30];
    int sp = 0;

    public void push(byte[] inb, int newpos, int newCount) {
        streamStack[sp] = buf;
        countStack[sp] = count;
        posStack[sp] = pos;
        sp++;
        buf = inb; pos = newpos;
        count = newCount;
    }

    public void pop() {
        sp--;
        buf = streamStack[sp];
        count = countStack[sp];
        pos = posStack[sp];
    }

    public int read() {
        if  (pos < count) {
            return (buf[pos++] & 0xff);
        }
        return -1;
    }

    public int read(byte b[], int off, int len) {
        if (pos >= count) {
            return -1;
        }
        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }

    public int available() {
        return count - pos;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) {
    }

    public void reset() {
        count = 0;
        pos = 0;
        eof = false;
    }

    public void close() throws IOException {
        in.close();
    }

}
