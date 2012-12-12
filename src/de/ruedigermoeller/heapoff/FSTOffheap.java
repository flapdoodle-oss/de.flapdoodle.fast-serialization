package de.ruedigermoeller.heapoff;

import com.sun.jmx.remote.util.OrderClassLoaders;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.annotations.Conditional;
import de.ruedigermoeller.serialization.annotations.Plain;
import de.ruedigermoeller.serialization.annotations.Predict;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 12.12.12
 * Time: 01:12
 * To change this template use File | Settings | File Templates.
 */
public class FSTOffheap {

    ByteBuffer buffer;
    ByteBufferEntry entry = new ByteBufferEntry();
    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    FSTObjectOutput out;
    FSTObjectInput in;
    int lastPosition = 0;

    public FSTOffheap(int size) throws IOException {
        buffer = ByteBuffer.allocateDirect(size*1000*1000);
        in = new FSTObjectInput(conf);
        out = new FSTObjectOutput(conf);
    }

    public int put(Object o, Object tag) throws IOException {
        int res = buffer.position();
        entry.content = o;
        entry.prevPosition = lastPosition;
        entry.content = o;
        entry.tag = tag;
        lastPosition = res;
        out.resetForReUse(null);
        out.writeObject(entry,entry.getClass());
        buffer.put(out.getBuffer(),0,out.getWritten());
        return res;
    }

    public Object getObject( int handle ) {
        return null;
    }

    class OffHeapIterator implements Iterator {
        int position;
        @Override
        public boolean hasNext() {
            return position > 0;
        }

        @Override
        public Object next() {

            return null;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }
    }

    @Predict(ByteBufferEntry.class)
    static class ByteBufferEntry implements Serializable {
        @Plain int prevPosition;
        @Conditional Object content;
        Object tag;
    }

    public static void main( String arg[]) throws IOException {
        FSTOffheap off = new FSTOffheap(100);
        JButton button = new JButton("hallo");
        int location = off.put(button,"hallo");
        Trader t = Trader.generateTrader(101,false);
        int siz = FSTConfiguration.createDefaultConfiguration().calcObjectSizeBytesNotAUtility(t);
        System.out.println("size "+siz);
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < 1000; i++ ) {
            location = off.put(t,"hallo"+i);
        }
        System.out.println("TIM "+(System.currentTimeMillis()-tim));
    }
}
