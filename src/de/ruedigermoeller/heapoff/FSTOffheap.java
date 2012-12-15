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
 * Core offheap implementation. The offheap is initialized with a fixed size. Multi threaded access is granted using
 * createAccess(). Concurrent read operations perform pretty well (low blocked time), concurrent write is possible but threads probably will be blocked
 * often.
 *
 * Objects added need to implement Serializable !!
 *
 * Use this as a base for higher level collection implementation. In order to get dynamically growing offheaps, consider
 * writing a collection wrapper managing ~1GB slices of FSTOffheap objects.
 *
 * Each object can be stored with a 'tag'. When iterating the off heap, it is possible to only read the Tag Object to improve
 * performance. E.g. if you write a lot of bloaty 'Person' objects, you'd like to add {name, firstname} as a tag so you can search
 * the offheap without the need to completely deserialize every object. In general iterating this for search should be replaced by
 * some kind indexing at a higher level if the amount of data is huge.
 *
 * Searching using iteration (Tag's only) processes ~500.000 objects per second (core I7 3 Ghz).
 *
 * You can provide your own Buffer object in order to use this on a memory mapped file.
 *
 * Note there is no 'overwrite' operation. You only can add objects, so there is the need to 'reorganize' a heap buffer from
 * time to time.
 */
public class FSTOffheap {

    String lock = "Lock";
    ByteBuffer buffer;
    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    OffHeapAccess access;

    int lastPosition = 0, currPosition=0;

    FSTObjectInput.ConditionalCallback alwaysSkip = new FSTObjectInput.ConditionalCallback() {
        @Override
        public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
            return true;
        }
    };

    public FSTOffheap(int sizeMB) throws IOException {
        this(ByteBuffer.allocateDirect(sizeMB * 1000 * 1000));
    }

    public FSTOffheap(ByteBuffer buffer) throws IOException {
        this.buffer = buffer;
        access = createAccess();
        conf.registerSerializer(ByteBufferEntry.class, new FSTBasicObjectSerializer() {

            @Override
            public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
                out.defaultWriteObject(toWrite, clzInfo);
            }

            @Override
            public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPositioin) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
                MyFSTInput inp = (MyFSTInput) in;
                ByteBufferEntry entry = inp.getAccess().currentEntry;
                entry.content = null;
                in.defaultReadObject(referencee, serializationInfo, entry);
                return entry;
            }
        }, false);
    }

    /**
     * single threaded access
     * @param handle
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public Object getObject( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        return access.getObject(handle);
    }

    /**
     * single threaded access
     * @param handle
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public Object getTag( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        return access.getTag(handle);
    }

    /**
     * single threaded access
     */
    public int add(Object toSave, Object tag) throws IOException {
        return access.add(toSave,tag);
    }

    /**
     * @return a heap access object. An OffHeapReadAccess can be used to access the OffHeap from a single thread. In order
     * to access concurrent, each Thread must have its own instance of OffHeapAccess.
     * Try to cache the access Object, as the creation is expensive.
     */
    public OffHeapAccess createAccess() {
        return new OffHeapAccess();
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public OffHeapIterator iterator() {
        return new OffHeapIterator(lastPosition);
    }

    static class MyFSTInput extends FSTObjectInput {

        private final OffHeapAccess acc;

        public MyFSTInput(OffHeapAccess acc, FSTConfiguration conf) throws IOException {
            super(conf);
            this.acc = acc;
        }

        public OffHeapAccess getAccess() {
            return acc;
        }
    }

    public class OffHeapAccess {

        protected ByteBufferEntry currentEntry = new ByteBufferEntry();
        protected FSTObjectInput in;
        FSTObjectOutput out;
        byte tmpBuf[];

        public OffHeapAccess() {
            try {
                in = new MyFSTInput(this,conf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] getTmpBuf(int siz) {
            if ( tmpBuf == null || tmpBuf.length < siz ) {
                tmpBuf = new byte[siz];
            }
            return tmpBuf;
        }

        public ByteBufferEntry getEntry(int handle) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
            int len = buffer.getInt(handle);
            byte[] buf = getTmpBuf(len);
            synchronized (lock) {
                buffer.position(handle+4);
                buffer.get(buf);
            }
            in.resetForReuseUseArray(buf, 0, len);
            return (ByteBufferEntry) in.readObject(ByteBufferEntry.class);
        }

        public Object getObject( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
            in.setConditionalCallback(null);
            return getEntry(handle).content;
        }

        public Object getTag( int handle ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
            in.setConditionalCallback(alwaysSkip);
            return getEntry(handle).tag;
        }

        public int add(Object toSave, Object tag) throws IOException {
            if ( out == null ) {
                out = new FSTObjectOutput(conf);
            }
            out.resetForReUse(null);
            synchronized (lock) {
                int res = currPosition;
                currentEntry.content = toSave;
                currentEntry.prevPosition = lastPosition;
                currentEntry.content = toSave;
                currentEntry.tag = tag;
                lastPosition = res;
                buffer.position(currPosition+4); // length
                out.writeObject(currentEntry,currentEntry.getClass());
                buffer.put(out.getBuffer(),0,out.getWritten());
                currPosition = buffer.position();
                buffer.putInt(res,currPosition-res);
                return res;
            }
        }

    }

    public class OffHeapIterator extends OffHeapAccess implements Iterator {
        int position;

        OffHeapIterator(int position) {
            super();
            this.position = position;
        }

        @Override
        public boolean hasNext() {
            return position > 0;
        }

        public Object getCurrentTag() {
            return currentEntry.tag;
        }

        /**
         * @return null if iterated using next(), the saved object if iterated using nextEntry
         */
        public Object getCurrentEntry() {
            return currentEntry.content;
        }

        /**
         *
         * @param callback - if null always read the full object (tag+value), else
         *                 the callback is called to decide wether to decode the content or not
         * @return
         */
        public Object nextEntry(FSTObjectInput.ConditionalCallback callback) {
            try {
                in.setConditionalCallback(callback);
                currentEntry = getEntry(position);
                position = currentEntry.prevPosition;
                return currentEntry.tag;
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

        /**
         * @return the 'tag' of the next entry. Use nextEntry to obtain the full entry (
         */
        @Override
        public Object next() {
            try {
                in.setConditionalCallback(alwaysSkip);
                currentEntry = getEntry(position);
                position = currentEntry.prevPosition;
                return currentEntry.tag;
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
    static public class ByteBufferEntry implements Serializable {
        public int prevPosition;
        public @Conditional Object content;
        public @Flat Object tag;
    }


}
