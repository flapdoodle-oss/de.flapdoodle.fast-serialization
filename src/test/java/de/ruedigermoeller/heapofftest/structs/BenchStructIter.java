package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.heapofftest.gcbenchmarks.BasicGCBench;
import de.ruedigermoeller.serialization.testclasses.HtmlCharter;

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
 * Date: 10.07.13
 * Time: 01:10
 * To change this template use File | Settings | File Templates.
 */
public class BenchStructIter {

    static void fillInstruments(StructArray<TestInstrument> arr) {
        int size = arr.getSize();
        for ( int i = 0; i < size; i++ ) {
            TestInstrument testInstrument = arr.get(i);
            if ( testInstrument == null ) { // if on heap, no prefilled array
                testInstrument = TestInstrument.createInstrumentTemplateOnHeap();
                arr.set(i,testInstrument);
            }
            testInstrument.setInstrId(i);
            testInstrument.getMnemonic().setString("I"+i);
            testInstrument.getMarket().getMnemonic().setString((i % 2) == 0 ? "XEUR" : "XETR");
            for ( int j=0; j < i%4; j++ ) {
                TestInstrumentLeg leg = new TestInstrumentLeg();
                leg.setLegQty(i%4);
                leg.getInstrument().getMnemonic().setString("I"+j);
                testInstrument.addLeg(leg);
            }
        }
    }

    static long test(Runnable r) {
//        r.run();
        long tim = System.currentTimeMillis();
        r.run();
        return (System.currentTimeMillis()-tim);
    }

    final static StructArray<TestInstrument> offheap[] = new StructArray[] { null };
    final static StructArray<TestInstrument> onheap[] = new StructArray[] { null };

    public static void main( String arg[] ) {

//        HtmlCharter charter = new HtmlCharter("./structiter.html");
//        charter.openDoc();
//        charter.text("<i>intel i7 3770K 3,5 ghz, 4 core, 8 threads</i>");
//        charter.text("<i>" + System.getProperty("java.runtime.version") + "," + System.getProperty("java.vm.name") + "," + System.getProperty("os.name") + "</i>");

        final FSTStructFactory fac = FSTStructFactory.getInstance();
        fac.registerClz(TestDate.class,TestInstrument.class,TestInstrumentLeg.class,TestMarket.class,TestTimeZone.class);


        long oninstTime = test( new Runnable() {
            public void run() {
                StructArray<TestInstrument> instruments = new StructArray<TestInstrument>(1000000,1 /*dummy for testing*/);
                fillInstruments(instruments);
                System.out.println("allocated " + instruments.getSize() + " instruments");
                onheap[0] = instruments;
            }
        });
        System.out.println("duration on heap instantiation "+oninstTime);
        BasicGCBench.benchFullGC();

        long instTime = test( new Runnable() {
            public void run() {
                StructArray<TestInstrument> instruments = fac.toStructArray(1000000, TestInstrument.createInstrumentTemplate());
                fillInstruments(instruments);
                System.out.println("allocated " + instruments.getSize() + " instruments using " + instruments.getByteSize() / 1000 / 1000 + " MB");
                offheap[0] = instruments;
            }
        });
        System.out.println("duration off heap instantiation "+instTime);
        final int iterMul = 10;

        BasicGCBench.benchFullGC();

        for ( int xx = 0; xx < 5; xx++ ) {
            System.out.println();

            long offCalcQty = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for ( int i = 0; i < instruments.getSize(); i++ ) {
                            sum+=instruments.get(i).getAccumulatedQty();
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration naive off heap iteration calcQty "+offCalcQty);

            long offCalcQty1 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for (StructArray<TestInstrument>.StructArrIterator<TestInstrument> iterator = instruments.iterator(); iterator.hasNext(); ) {
                            TestInstrument next = iterator.next(siz);
                            sum+=next.getAccumulatedQty();
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt iterator off heap iteration calcQty "+offCalcQty1);

            long offCalcQty2 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            sum+=p.getAccumulatedQty();
                            p.next(siz);
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt pointer off heap iteration calcQty "+offCalcQty2);

            long offCalcQty3 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            sum+=p.getAccumulatedQty();
                            p.___offset+=siz;
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt DIRECT pointer off heap iteration calcQty "+offCalcQty3);

            long offCalcQty4 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        StructArray<TestInstrumentLeg> lp = (StructArray<TestInstrumentLeg>) p.getLegs().detach();
                        TestInstrumentLeg legp = (TestInstrumentLeg) lp.get(0).detach();
                        final long arroff = lp.___offset - p.___offset; // relative offset of StructArray<TestInstrumentLeg>
                        final long legoff = legp.___offset - p.___offset; // relative offset of first TestInstrumentLeg of StructArray<TestInstrumentLeg>
                        final int legSiz = lp.getStructElemSize(); // size of a leg in StructArray<TestInstrumentLeg>
                        for (int i=0; i < count; i++ ) {
                            int legs = p.getNumLegs();
                            sum++;
                            for ( int k = 0; k < legs; k++ ) {
                                sum+=legp.getLegQty();
                                legp.___offset += legSiz;
                            }
                            p.___offset+=siz;
                            lp.___offset=p.___offset+arroff;
                            legp.___offset=p.___offset+legoff;
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt hacked pointer off heap iteration calcQty "+offCalcQty4);

            long onCalcQty = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    int sum = 0;
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        final int size = instruments.getSize();
                        for ( int i = 0; i < size; i++ ) {
                            sum+=instruments.get(i).getAccumulatedQty();
                        }
                    }
                    System.out.println("sum onheap "+sum);
                }
            });
            System.out.println("duration on heap iteration calcQty "+onCalcQty);

            long intAccessOff = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    int sum = 0;
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            sum+=p.getInstrId();
                            p.___offset+=siz;
                        }
                    }
                    System.out.println("sum offheap "+sum);
                }
            });
            System.out.println("duration opt DIRECT pointer int access iteration "+intAccessOff);

            long intAccessOn = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    int sum = 0;
                    final int count = instruments.getSize();
                    for ( int j = 0; j < iterMul; j++ ) {
                        sum = 0;
                        for (int i=0; i < count; i++ ) {
                            sum+=instruments.get(i).getInstrId();
                        }
                    }
                    System.out.println("sum onheap "+sum);
                }
            });
            System.out.println("duration onheap int access iteration "+intAccessOn);

            long stringSearchAccessOff = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        TestInstrument p = instruments.createPointer(0);
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(p.getMnemonic())) {
//                                System.out.println("found "+i);
                                break;
                            }
                            p.___offset+=siz;
                        }
                    }
                }
            });
            System.out.println("duration opt DIRECT pointer string compare iteration "+stringSearchAccessOff);

            long stringSearchAccessOff1 = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = offheap[0];
                    final int siz = instruments.getStructElemSize();
                    final int count = instruments.getSize();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        StructString sp = (StructString) instruments.createPointer(0).getMnemonic().detach();
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(sp)) {
                                break;
                            }
                            sp.___offset+=siz;
                        }
                    }
                }
            });
            System.out.println("duration opt DIRECT String pointer string compare iteration "+stringSearchAccessOff1);

            long stringSearchAccessOon = test( new Runnable() {
                public void run() {
                    StructArray<TestInstrument> instruments = onheap[0];
                    final int count = instruments.getSize();
                    StructString str = new StructString("I999999");
                    for ( int j = 0; j < iterMul; j++ ) {
                        for (int i=0; i < count; i++ ) {
                            if ( str.equals(instruments.get(i).getMnemonic())) {
//                                System.out.println("found "+i);
                                break;
                            }
                        }
                    }
                }
            });
            System.out.println("duration onheap string compare iteration "+stringSearchAccessOon);
        }
//        int size = instruments.getSize();
//        for (int i = 0; i < size; i++) {
//            System.out.println(instruments.get(i));
//        }

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
