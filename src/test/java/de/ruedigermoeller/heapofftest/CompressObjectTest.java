package de.ruedigermoeller.heapofftest;

import de.ruedigermoeller.heapoff.FSTCompressed;
import de.ruedigermoeller.heapoff.FSTCompressor;
import de.ruedigermoeller.serialization.testclasses.enterprise.SimpleOrder;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

import java.util.ArrayList;

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
 * Date: 20.06.13
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public class CompressObjectTest {

    static Object ref;

    public static void main(String arg[]) {
        FSTCompressor comp = new FSTCompressor();

        try {
            FSTCompressed<OffHeapTest.ExampleOrder> order = comp.compress2Byte(new OffHeapTest.ExampleOrder());
            System.out.println(order.get().text+" "+order.getLen());

            FSTCompressed<Trader> trader = comp.compress2Byte(Trader.generateTrader(101,true));
            System.out.println(trader.get().getBusinessUnitName()+" "+trader.getLen());

            Thread.sleep(10000);
            System.out.println("start");

            ArrayList<FSTCompressed<SimpleOrder>> list = new ArrayList<FSTCompressed<SimpleOrder>>();

            ArrayList<SimpleOrder> list1 = new ArrayList<SimpleOrder>();
            ref = list1;

            for ( int i = 0; i < 10000000; i++) {
                SimpleOrder obj = SimpleOrder.generateOrder(i);
                obj.getOrderQty().setValue(i);
//                list.add(comp.compress2Byte(obj));
                list1.add(obj);
            }

            System.out.println("finished add");

            int sum = 0;
            int sum1 = 0;
            for ( int i = 0; i < list.size(); i++) {
                sum += list.get(i).get().getOrderQty().getValue();
                sum1 += i;
            }
            System.out.println("sums "+sum+" "+sum1);
            System.out.println("finished iter 0");

            Thread.sleep(10000);
            sum = 0;
            sum1 = 0;
            for ( int i = 0; i < list.size(); i++) {
                sum += list.get(i).get().getOrderQty().getValue();
                sum1 += i;
            }
            System.out.println("sums " + sum + " " + sum1);
            System.out.println("finished iter 1");

            Thread.sleep(10000000);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
