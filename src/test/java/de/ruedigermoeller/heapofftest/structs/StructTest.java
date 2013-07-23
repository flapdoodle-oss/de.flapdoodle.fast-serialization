package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;

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
 * Date: 07.07.13
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class StructTest {

    public static void main( String arg[] ) {

        FSTStructFactory fac = FSTStructFactory.getInstance();
        fac.registerClz(TestData.class);

        TestData data = new TestData();
        data.setNested(new TestData());
        data.dataStructArray = new StructArray<TestData>(50, new TestData());
        int siz = fac.calcStructSize(data);
        TestData data1 = fac.toStruct(data);
        System.out.println("Size: "+siz+" "+data1.getByteSize()+" "+data1.getString());

        compareTestData( data, data1 );
        compareTestData( data.getNested(), data1.getNested() );

        StructArray<StructString> sl = new StructArray(10,new StructString(10));

        sl = fac.toStruct(sl);
        System.out.println("len "+sl.getByteSize());

        System.out.println("size "+sl.size());
        System.out.println("cont "+sl.get(0));
        sl.set(0, new StructString(10));
        System.out.println("cont " + sl.get(0));
        sl.get(0).setString("Hallo");
        System.out.println("cont " + sl.get(0));

        for ( int i=0; i < sl.size(); i++ ) {
            sl.set(i,new StructString("Hallo"+i));
        }

        sl.set(5,null);
        for ( int i=0; i < sl.size(); i++ ) {
            System.out.println(sl.get(i));
        }

    }

    private static void compareTestData(TestData data, TestData data1) {
        if ( !data.getString().equals(data1.getString()) )
            throw new RuntimeException();

        if ( data.getA() !=data1.getA() )
            throw new RuntimeException();

        if ( data.getB() !=data1.getB() )
            throw new RuntimeException();
        if ( data.getC() !=data1.getC() )
            throw new RuntimeException();
        if ( data.getD() !=data1.getD() )
            throw new RuntimeException();
        if ( data.getE() !=data1.getE() )
            throw new RuntimeException();
        if ( data.getF() !=data1.getF() )
            throw new RuntimeException();
        if ( data.getG() !=data1.getG() )
            throw new RuntimeException();

        for ( int i = 0; i < data.arrALen(); i++ ) {
            if ( data1.arrA(i) != data.arrA(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrbLen(); i++ ) {
            if ( data1.arrb(i) != data.arrb(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrcLen(); i++ ) {
            if ( data1.arrc(i) != data.arrc(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrdLen(); i++ ) {
            if ( data1.arrd(i) != data.arrd(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrgLen(); i++ ) {
            if ( data1.arrg(i) != data.arrg(i) ) {
                throw new RuntimeException();
            }
        }
    }

}

