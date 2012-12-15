package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.heapoff.FSTOffHeapMap;
import de.ruedigermoeller.heapoff.FSTOffheap;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.testclasses.enterprise.SimpleOrder;

import java.io.IOException;
import java.lang.reflect.Field;
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

    public static void testHeapMap() throws IOException {
        FSTOffHeapMap<String,SimpleOrder> map = new FSTOffHeapMap<String, SimpleOrder>(1000);
        SimpleOrder o = SimpleOrder.generateOrder(13);
        for ( int i = 0; i < 2000000; i++) {
            map.put("" + i, o);
        }

        System.out.println("Map size "+map.getHeapSize()/1000/1000+" mb");

        for ( int i = 0; i < 2000000; i++) {
            map.put( ""+i, o );
        }

        int count = 0;
        for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            count++;
        }

        System.out.println("COUNT "+count);

        for ( int i = 0; i < 2000000; i+=2) {
            map.remove("" + i);
        }

        FSTOffHeapMap<String,SimpleOrder> map1 = new FSTOffHeapMap<String, SimpleOrder>(1000);
        map1.putAll(map);
        System.out.println("map 1 reorg "+map1.getHeapSize()/1000/1000);
    }

    private static void search(FSTOffheap off, final String toSearch) {
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
        System.out.println("search time (no break) "+(System.currentTimeMillis()-tim)+" "+Thread.currentThread().getName());
    }

    public static void testOffHeap() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        final FSTOffheap off = new FSTOffheap(1000);
        int location = 0;

        final SimpleOrder or = SimpleOrder.generateOrder(22);
        final int iters = 2000000;
        long tim = System.currentTimeMillis();

        for ( int i = 0; i < iters; i++ ) {
            location = off.add(or, "hallo" + i);
        }

        long dur = System.currentTimeMillis() - tim;
        System.out.println("TIM "+ dur +" per ms "+(iters/dur));
        System.out.println("siz " + off.getLastPosition() / 1000 / 1000);
        for ( int i = 0; i < 8; i++) {
            new Thread("search "+i) {
                public void run() {
                    search(off, "hallo1237162");
                }
            }.start();
        }
        new Thread("change ") {
            public void run() {
                try {
                    for ( int i = 0; i < iters/2; i++ ) {
                        off.add(or, "hallo1237162");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("change finish");
            }
        }.start();
//        it = off.iterator();
//        while( it.hasNext() ) {
//            System.out.println(it.next());
//        }
    }

    public static void main( String arg[]) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        testHeapMap();
        testOffHeap();
    }

}
