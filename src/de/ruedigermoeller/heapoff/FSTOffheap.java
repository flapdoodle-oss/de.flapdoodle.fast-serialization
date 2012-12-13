package de.ruedigermoeller.heapoff;

import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.annotations.Conditional;
import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.annotations.Predict;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
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
    byte tmpBuf[] = new byte[100];

    public FSTOffheap(int size) throws IOException {
        buffer = ByteBuffer.allocateDirect(size*1000*1000);
        in = new FSTObjectInput(conf);
        out = new FSTObjectOutput(conf);
        conf.registerSerializer(ByteBufferEntry.class, new FSTBasicObjectSerializer() {
            @Override
            public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
                out.defaultWriteObject(toWrite,clzInfo);
            }

            @Override
            public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
                in.defaultReadObject(referencee, serializationInfo, entry);
                return entry;
            }
        }, false);
    }

    public int add(Object o, Object tag) throws IOException {
        int res = currPosition;
        entry.content = o;
        entry.prevPosition = lastPosition;
        entry.content = o;
        entry.tag = tag;
        lastPosition = res;
        out.resetForReUse(null);
        buffer.position(currPosition+4); // length
        out.writeObject(entry,entry.getClass());
        buffer.put(out.getBuffer(),0,out.getWritten());
        currPosition = buffer.position();
        buffer.putInt(res,currPosition-res);
        return res;
    }

    public Object getObject( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        in.setConditionalCallback(null);
        return getEntry(handle).content;
    }

    public Object getTag( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        in.setConditionalCallback(alwaysSkip);
        return getEntry(handle).tag;
    }

    ByteBufferEntry getEntry( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        int len = buffer.getInt(handle);
        byte[] buf = getTmpBuf(len);
        buffer.position(handle+4);
        buffer.get(buf);
        in.resetForReuseUseArray(buf, 0, len);
        return (ByteBufferEntry) in.readObject(ByteBufferEntry.class);
    }

    public byte[] getTmpBuf(int siz) {
        if ( tmpBuf.length < siz ) {
            tmpBuf = new byte[siz];
        }
        return tmpBuf;
    }

    public OffHeapIterator iterator() {
        return new OffHeapIterator(lastPosition);
    }

    public class OffHeapIterator implements Iterator {
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
            try {
                in.setConditionalCallback(alwaysSkip);
                ByteBufferEntry en = (ByteBufferEntry) getEntry(position);
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

    @Predict(ByteBufferEntry.class) @Flat
    static class ByteBufferEntry implements Serializable {
        int prevPosition;
        @Conditional Object content;
        Object tag;
    }

//    public static void main( String arg[]) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
//        FSTOffheap off = new FSTOffheap(100);
//        int location = 0;
//
//
//        Trader t = Trader.generateTrader(101, false);
////        int siz = FSTConfiguration.createDefaultConfiguration().calcObjectSizeBytesNotAUtility(t);
////        System.out.println("size "+siz);
//        int i1 = 90000;
////        System.out.println("size "+(siz*i1)/1000000+"mb");
//        long tim = System.currentTimeMillis();
//
//        int handle = off.add(t, null);
//
//        Object ttag = off.getTag(handle);
//        System.out.println(ttag);
//        Object traderRead = off.getObject(handle);
//        System.out.println(traderRead);
//
//        for ( int i = 0; i < i1; i++ ) {
//            location = off.add(t, "hallo" + i);
//        }
//        System.out.println("TIM "+(System.currentTimeMillis()-tim));
//        tim = System.currentTimeMillis();
//        Iterator it = off.iterator();
//        while( it.hasNext() ) {
//            Object tag = it.next();
//        }
//        System.out.println("TIMITER "+(System.currentTimeMillis()-tim));
////        it = off.iterator();
////        while( it.hasNext() ) {
////            System.out.println(it.next());
////        }
//    }
}
