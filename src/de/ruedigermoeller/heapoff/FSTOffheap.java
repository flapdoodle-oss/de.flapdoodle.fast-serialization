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
import java.lang.reflect.Field;
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
    int lastPosition = 0, currPosition=0;
    FSTObjectInput.ConditionalCallback alwaysSkip = new FSTObjectInput.ConditionalCallback() {
        @Override
        public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
            return true;
        }
    };

    public FSTOffheap(int size) throws IOException {
        buffer = ByteBuffer.allocateDirect(size*1000*1000);
        in = new FSTObjectInput(conf);
        out = new FSTObjectOutput(conf);
    }

    public int put(Object o, Object tag) throws IOException {
        buffer.position(currPosition);
        int res = currPosition;
        entry.content = o;
        entry.prevPosition = lastPosition;
        entry.content = o;
        entry.tag = tag;
        lastPosition = res;
        out.resetForReUse(null);
        out.writeObject(entry,entry.getClass());
        buffer.put(out.getBuffer(),0,out.getWritten());
        currPosition = buffer.position();
        return res;
    }

    public Object getObject( int handle ) {
        return null;
    }

    public Iterator iterator() {
        return new OffHeapIterator(lastPosition);
    }

    class OffHeapIterator implements Iterator {
        byte tmp[] = new byte[100];
        int position;

        OffHeapIterator(int position) {
            this.position = position;
        }

        @Override
        public boolean hasNext() {
            return position > 0;
        }

        @Override
        public Object next() {
            in.setConditionalCallback(alwaysSkip);
            buffer.position(position);
            buffer.get(tmp);
            try {
                in.resetForReuse(tmp, 0, tmp.length);
                ByteBufferEntry en = (ByteBufferEntry) in.readObject(ByteBufferEntry.class);
                position = en.prevPosition;
                return en.tag;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }
    }

    @Predict(ByteBufferEntry.class)
    static class ByteBufferEntry implements Serializable {
        int prevPosition;
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
        int i1 = 90000;
        System.out.println("size "+(siz*i1)/1000000+"mb");
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < i1; i++ ) {
            location = off.put(t,"hallo"+i);
        }
        System.out.println("TIM "+(System.currentTimeMillis()-tim));
        tim = System.currentTimeMillis();
        Iterator it = off.iterator();
        while( it.hasNext() ) {
            Object tag = it.next();
        }
        System.out.println("TIMITER "+(System.currentTimeMillis()-tim));
//        it = off.iterator();
//        while( it.hasNext() ) {
//            System.out.println(it.next());
//        }
    }
}
