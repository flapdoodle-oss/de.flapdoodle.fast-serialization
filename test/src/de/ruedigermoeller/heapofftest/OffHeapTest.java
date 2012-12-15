package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.heapoff.FSTOffHeapMap;
import de.ruedigermoeller.heapoff.FSTOffheap;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;
import de.ruedigermoeller.serialization.testclasses.basicstuff.SmallThing;
import de.ruedigermoeller.serialization.testclasses.enterprise.SimpleOrder;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
 * Date: 14.12.12
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */

// i know i could use junit .. but i just dislike jar jungles ..
public class OffHeapTest {

    public static void benchMap(HtmlCharter charter) throws IOException {

        FSTOffHeapMap<String,SimpleOrder> map = new FSTOffHeapMap<String, SimpleOrder>(1000);
        SimpleOrder o = SimpleOrder.generateOrder(13);
        int numelem = 4000000;
        long tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            map.put("" + i, o);
        }
        tim = System.currentTimeMillis()-tim;
        charter.openChart("Off Heap Map (FSTOffHeapMap) size:"+map.getHeapSize()/1000/1000+" MB "+numelem+" SimpleOrder's (=> is better)");
        charter.chartBar("put SimpleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");


        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            Object something = map.get("" + i);
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("get SimpleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        int count = 0;
        tim = System.currentTimeMillis();
        for (SimpleOrder next : map.values()) {
            count++; // avoid VM optimization
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("iterate SimpleOrder objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        charter.closeChart();

        FSTOffHeapMap<String,SmallThing> map1 = new FSTOffHeapMap<String, SmallThing>(1000);
        SmallThing p = new SmallThing();
        numelem = 8000000;
        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            map1.put("" + i, p);
        }
        tim = System.currentTimeMillis()-tim;
        charter.openChart("Off Heap Map (FSTOffHeapMap) size:"+map1.getHeapSize()/1000/1000+" MB "+numelem+" SmallThing's (=> is better)");
        charter.chartBar("put SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");


        tim = System.currentTimeMillis();
        for ( int i = 0; i < numelem; i++) {
            Object something = map1.get("" + i);
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("get SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        count = 0;
        tim = System.currentTimeMillis();
        for (SmallThing next : map1.values()) {
            count++; // avoid VM optimization
        }
        tim = System.currentTimeMillis()-tim;
        charter.chartBar("iterate SmallThing objects/second ", (int)((numelem/tim)*1000), 10000, "#a0a0ff");

        charter.closeChart();

    }

    private static void search(FSTOffheap off, final String toSearch, int[] sum, int[] count) {
        long tim = System.currentTimeMillis();
        FSTOffheap.OffHeapIterator it = off.iterator();
        while( it.hasNext() ) {
            Object tag = it.nextEntry(new FSTObjectInput.ConditionalCallback() {
                @Override
                public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                    FSTOffheap.ByteBufferEntry be = (FSTOffheap.ByteBufferEntry) halfDecoded;
                    return !toSearch.equals(be.tag);
                }
            });
            if ( it.getCurrentEntry() != null ) {
                System.out.println("found ! "+it.getCurrentTag()+" "+Thread.currentThread().getName());
            }
        }
        if ( sum != null ) {
            synchronized (sum) {
                sum[0] += (System.currentTimeMillis()-tim);
            }
            synchronized (count) {
                count[0]++;
            }
        }
        System.out.println("search time (no break) "+(System.currentTimeMillis()-tim)+" "+Thread.currentThread().getName());
    }

    public static void benchOffHeap(final FSTOffheap off, HtmlCharter charter, String tit) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        final SimpleOrder or = SimpleOrder.generateOrder(22);
        final int iters = 4000000;
        charter.openChart(tit+" FSTOffheap core operations performance (=> is better)");
        long tim = System.currentTimeMillis();

        for ( int i = 0; i < iters; i++ ) {
            if ( i == 1237162 ) {
                off.add(or, "hallo1237162");
            } else {
                off.add(or, "hallo");
            }
        }

        long dur = System.currentTimeMillis() - tim;

        charter.chartBar("add SimpleOrder with String tag (objects/s)", (int) ((iters/dur)*1000),10000,"#a0a0ff");
        System.out.println("TIM add "+ dur +" per ms "+(iters/dur));
        System.out.println("siz " + off.getLastPosition() / 1000 / 1000);


        tim = System.currentTimeMillis();
        search(off, "hallo1237162", null, null);
        dur = System.currentTimeMillis() - tim;
        charter.chartBar("search tag based (objects/s)", (int) ((iters/dur)*1000),10000,"#a0a0ff");
        System.out.println("TIM search" + dur + " per ms " + (iters / dur));
        System.out.println("siz " + off.getLastPosition() / 1000 / 1000);


        tim = System.currentTimeMillis();
        FSTOffheap.OffHeapIterator it = off.iterator();
        while( it.hasNext() ) {
            it.nextEntry(null);
        }
        dur = System.currentTimeMillis() - tim;
        charter.chartBar("iterate SimpleOrder (objects/s)", (int) ((iters/dur)*1000),10000,"#a0a0ff");

        final int sum[] = { 0 };
        final int count[] = { 0 };
        for ( int i = 0; i < 4; i++) {
            new Thread("search "+i) {
                public void run() {
                    search(off, "hallo1237162", sum, count);
                }
            }.start();
        }

        while( count[0] != 4 ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        charter.chartBar("<b>concurrent</b> search tag based (objects/thread/s)", (int) (iters/(sum[0]/count[0])*1000),10000,"#ffa0a0");

        charter.closeChart();
    }

    public static void main( String arg[]) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        HtmlCharter charter = new HtmlCharter("./offheap.html");
        charter.openDoc();
        benchMap(charter);
        benchOffHeap(new FSTOffheap(1000), charter, "Direct ByteBuffer" );
        RandomAccessFile randomFile = new RandomAccessFile("./mappedfile.bin", "rw");
        randomFile.setLength(1000*1000*1000);
        FileChannel channel = randomFile.getChannel();
        MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1000 * 1000 * 1000);
        benchOffHeap(new FSTOffheap(buf), charter, "Memory mapped File:");
        charter.closeDoc();
        randomFile.close();
//        testOffHeap();
    }

}