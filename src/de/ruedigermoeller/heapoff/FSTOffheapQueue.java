package de.ruedigermoeller.heapoff;

/*
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
 * Date: 16.12.12
 * Time: 01:44
 * To change this template use File | Settings | File Templates.
 */

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.util.FSTOrderedConcurrentJobExecutor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * a queue based on off heap memory. The advantage is, that objects are serialized at the time you add them to the queue.
 * This has several advantages (Client/Server related)
 *  - the size of the queue is defined in memory size, not number of objects (no risk to get OOM)
 *  - queues do not suck CPU indirectly by being subject to garbage collection
 *  - if a message is added to multiple queues, you actually serialize this message once, then copy the resulting bytes
 *  - its easier to control network message size, as you already know the size of a serialized object
 *  - easy recovery in case you use memory mapped files
 *
 *  Once a message is sent to a client, a bunch of bytes is taken from the queue.
 *
 */
public class FSTOffheapQueue  {

    private static final int HEADER_SIZE = 4;
    ByteBuffer buffer;
    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    int headPosition = 0;
    int tailPosition = 0;
    int currentQeueEnd = 0;
    int count = 0;

    FSTOrderedConcurrentJobExecutor exec;
    ArrayList<FSTObjectOutput> outputs = new ArrayList<FSTObjectOutput>();
    int outSP = 0;

    Object rwLock = "QueueRW";
//    Semaphore added = new Semaphore(0);
//    Semaphore taken = new Semaphore(0);

    ConcurrentWriteContext writer;
    ConcurrentReadContext reader;

    public static class ByteBufferResult {
        public int off;
        public int len;
        public ByteBuffer buffer;
        public byte[] b;
    }

    public FSTOffheapQueue(int sizeMB) throws IOException {
        this( ByteBuffer.allocateDirect(sizeMB*1000*1000), 4);
    }

    public FSTOffheapQueue(int sizeMB, int numThreads) throws IOException {
        this( ByteBuffer.allocateDirect(sizeMB*1000*1000), 4);
    }

    public FSTOffheapQueue(ByteBuffer buffer) throws IOException {
        this(buffer,4);
    }

    public FSTOffheapQueue(ByteBuffer buffer, int numThreads) throws IOException {
        this.buffer = buffer;
        currentQeueEnd = buffer.limit();
        writer = createConcurrentWriter();
        reader = createConcurrentReader();
        exec = new FSTOrderedConcurrentJobExecutor(numThreads);
    }

    FSTObjectOutput getCachedOutput() {
        synchronized (outputs) {
            if (outputs.size()==0) {
                return new FSTObjectOutput(conf);
            }
            FSTObjectOutput ret = outputs.get(outputs.size()-1);
            outputs.remove(outputs.size() - 1);
            return ret;
        }
    }

    void returnOut(FSTObjectOutput ou) {
        synchronized (outputs) {
            outputs.add(ou);
        }
    }

    public boolean addBytes(byte b[]) throws IOException {
        int siz = b.length;
        return addBytes(siz,b);
    }

    public boolean add(Object o) throws IOException {
        return writer.add(o);
    }

    public class ConcurrentWriteContext {
        FSTObjectOutput out = new FSTObjectOutput(conf);

        /**
         * perform multi threaded encoding. Note that depending on object size, you need up to 4 threads in order
         * to break even. If complex objects are added to the queue you'll need fewer threads to break even.
         * Scales well with multicore/hyperthreaded intel cpu's. The order of add's is still maintained, however
         * encoding is done concurrent.
         *
         * It has been tested, that with 8 threads on multicore servers a speed up of up to 400% is possible.
         * @param o
         * @throws IOException
         * @throws ExecutionException
         * @throws InterruptedException
         */
        public void addConcurrent(final Object o) throws IOException, ExecutionException, InterruptedException {
            exec.addCall(new FSTOrderedConcurrentJobExecutor.FSTRunnable() {
                FSTObjectOutput tmp;
//                FSTObjectOutput tmp = new FSTObjectOutput(conf);

                @Override
                public void runConcurrent() {
                    tmp = getCachedOutput();
                    tmp.resetForReUse(null);
                    try {
                        tmp.writeObject(o);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void runInOrder() {
                    int siz = tmp.getWritten();
                    byte[] towrite = tmp.getBuffer();
                    returnOut(tmp);
                    addBytes(siz, towrite);
                }
            });
        }

        public boolean add(Object o) throws IOException {
            out.resetForReUse(null);
            out.writeObject(o);
            int siz = out.getWritten();
            byte[] towrite = out.getBuffer();
            return addBytes(siz, towrite);
        }
    }

    public class ConcurrentReadContext {
        FSTObjectInput in;
        ByteBufferResult tmpRes = new ByteBufferResult();

        public ConcurrentReadContext() throws IOException {
            in = new FSTObjectInput(conf);
        }

        public Object takeObject(int sizeResult[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
            if ( takeBytes(tmpRes) ) {
                in.resetForReuseUseArray(tmpRes.b,0,tmpRes.b.length);
                if ( sizeResult != null ) {
                    sizeResult[0] = tmpRes.b.length;
                }
                return in.readObject();
            }
            return null;
        }
    }

    public ConcurrentWriteContext createConcurrentWriter() {
        return new ConcurrentWriteContext();
    }

    public ConcurrentReadContext createConcurrentReader() throws IOException {
        return new ConcurrentReadContext();
    }

    private boolean addBytes(int siz, byte[] towrite) {
        boolean full;
        synchronized (rwLock) {
            if ( siz+tailPosition+HEADER_SIZE >= buffer.limit() ) {
                currentQeueEnd = tailPosition;
                tailPosition = 0;
            }
            full = count > 0;
            if ( full ) {
                if ( tailPosition < headPosition ) {
                    full = tailPosition+siz >= headPosition;
                } else if ( tailPosition > headPosition ){
                    full = false;
                } else {
                    full = true;
                }
            }
            if (!full) {
                buffer.putInt(tailPosition, siz);
                buffer.position(tailPosition + 4);
                buffer.put(towrite, 0, siz );
                tailPosition+=siz+HEADER_SIZE;
                count++;
                rwLock.notifyAll();
            } else {
                try {
                    rwLock.wait();
                    addBytes(siz,towrite);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }


    public Object takeObject(int sizeResult[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
        return reader.takeObject(sizeResult);
    }

    public boolean takeBytes(ByteBufferResult res) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException{
        synchronized (rwLock) {
            if (headPosition == currentQeueEnd ) {
                headPosition = 0;
            }
            if ( count <= 0 ) {
                try {
                    rwLock.wait();
                } catch (InterruptedException e) {
                    return false;
                }
                return takeBytes(res);
            } else {
                res.len = buffer.getInt(headPosition);
                buffer.position(headPosition+HEADER_SIZE);
                byte b[] = new byte[res.len];
                buffer.get(b);
                res.buffer = ByteBuffer.wrap(b);
                res.off = 0;
                res.b = b;
                headPosition += res.len+HEADER_SIZE;
                count--;
                rwLock.notifyAll();
            }
        }
        return true;
    }

}
