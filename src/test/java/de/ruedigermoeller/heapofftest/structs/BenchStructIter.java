package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;

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
 * Date: 10.07.13
 * Time: 01:10
 * To change this template use File | Settings | File Templates.
 */
public class BenchStructIter {

    static void fillInstruments(StructArray<TestInstrument> arr) {
        int size = arr.getSize();
        for ( int i = 0; i < size; i++ ) {
            TestInstrument testInstrument = arr.get(i);
            testInstrument.setInstrId(i);
            testInstrument.getMnemonic().setString("I"+i);
            testInstrument.getMarket().getMnemonic().setString("XEUR");
//            for ( int j=0; j < i%4; j++ ) {
//                TestInstrumentLeg leg = new TestInstrumentLeg();
//                leg.setLegQty(j);
//                leg.getInstrument().getMnemonic().setString("I"+j);
//                testInstrument.addLeg(leg);
//            }
        }
    }

    public static void main( String arg[] ) {

//        HtmlCharter charter = new HtmlCharter("./structiter.html");
//        charter.openDoc();
//        charter.text("<i>intel i7 3770K 3,5 ghz, 4 core, 8 threads</i>");
//        charter.text("<i>" + System.getProperty("java.runtime.version") + "," + System.getProperty("java.vm.name") + "," + System.getProperty("os.name") + "</i>");

        FSTStructFactory fac = FSTStructFactory.getInstance();
        fac.registerClz(TestDate.class,TestInstrument.class,TestInstrumentLeg.class,TestMarket.class,TestTimeZone.class);

        StructArray<TestInstrument> instruments = fac.toStructArray(1, TestInstrument.createInstrumentTemplate());
        fillInstruments(instruments);

//        TestInstrument instrument = fac.toStruct(TestInstrument.createInstrumentTemplate());
//        instrument.setInstrId(99);
//        instrument.getMnemonic().setString("AA");
//        instrument.getMarket().getMnemonic().setString("XEUR");
//        instrument.getMarket().getCloses().setTime(System.currentTimeMillis());
//        instrument.getMarket().getOpens().setTime(System.currentTimeMillis()-12*60*60*1000);
//        System.out.println(instrument.toString());
//        charter.closeChart();

    }
}
