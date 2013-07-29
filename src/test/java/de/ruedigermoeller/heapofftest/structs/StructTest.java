package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructAllocator;
import de.ruedigermoeller.heapoff.structs.structtypes.StructInt;
import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
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

        TestData data = new TestData();
        data.setNested(new TestData());
        data.dataStructArray = new StructArray<TestData>(50, new TestData());

        FSTStructAllocator<TestData> alloc = new FSTStructAllocator<TestData>(data,10);
        FSTStructAllocator<StructString> strAlloc = new FSTStructAllocator<StructString>( new StructString(10), 10 );


        StructMap<StructInt,TestData> intMap = alloc.newMap(1000, new StructInt(0));

        for (int i=0; i < 1000; i++ ) {
            TestData value = new TestData();
            value.getString().setString("int "+i);
            try {
                intMap.put(new StructInt(i), value);
            } catch (Exception wx) {
                System.out.println(i+" "+wx);
            }
        }

        for (int i=0; i < 1000; i++ ) {
            StructString string = intMap.get(new StructInt(i)).getString();
            if ( ! string.toString().equals("int "+i) ) {
                throw new RuntimeException("error: '"+string+"'");
            }
//            System.out.println(""+i+" => "+ string);
        }

        compareTestData( data, alloc.newStruct() );
        compareTestData( data.getNested(), alloc.newStruct().getNested() );

        // test untyped polymorphic objectArray
        TestData objArrayTest = alloc.newStruct();
        check( ((StructInt)objArrayTest.objArray(3).cast()).get() == 17 );
        System.out.println("len "+objArrayTest.objArrayElementSize()+" "+((StructString)objArrayTest.objArray(0).cast()).getByteSize());
        check( objArrayTest.objArrayElementSize() == ((StructString)objArrayTest.objArray(0).cast()).getByteSize() );
        boolean exThrown = false;
        try {
            ((StructString)objArrayTest.objArray(0).cast()).setString("01234567890123456780");
        } catch (Exception ex) {
            exThrown = true;
        }
        check(exThrown);
        objArrayTest.objArray(0, new StructString("01234567890123456780"));
        System.out.println("Struct index " + objArrayTest.objArrayStructIndex());
        check(objArrayTest.objArrayStructIndex() > 0);
        check(objArrayTest.objArrayElementSize() > 0);
        check(objArrayTest.objArrayPointer() != null);
        check(objArrayTest.objArrayStructIndex()%8 == 0);

        for ( int i = 0; i < objArrayTest.objArrayLen(); i++) {
            objArrayTest.objArray(i,null);
            check(objArrayTest.objArray(i) == null);
        }

        FSTStruct fstStruct = objArrayTest.objArrayPointer();
        check(fstStruct.isNull());

        for ( int i = 0; i < objArrayTest.objArrayLen(); i++) {
            objArrayTest.objArray(i,new StructInt(i));
            check(((StructInt)objArrayTest.objArray(i).cast()).get() == i);
        }


        StructArray <StructString> sl = strAlloc.newArray(25);
        System.out.println("len "+sl.getByteSize());

        for ( int i=0; i < sl.size(); i++ ) {
            sl.set(i,new StructString("Hallo"+i));
        }

        for ( int i=0; i < sl.size(); i++ ) {
            check(sl.get(i).toString().equals("Hallo"+i));
        }


    }

    private static void check(boolean b) {
        if ( ! b ) {
            throw new RuntimeException("assertion failed");
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

