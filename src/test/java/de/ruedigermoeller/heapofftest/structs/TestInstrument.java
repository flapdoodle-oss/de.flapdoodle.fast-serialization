package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;

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
 * Time: 01:24
 * To change this template use File | Settings | File Templates.
 */
public class TestInstrument extends FSTStruct {

    public static TestInstrument createInstrumentTemplate() {
        // build template
        TestInstrument ti = new TestInstrument();
        // max 4 legs
//        ti.legs = new StructArray<TestInstrumentLeg>(4,FSTStructFactory.getInstance().toStruct(new TestInstrumentLeg()));
        return ti;
    }

    protected long instrId;
    protected StructString mnemonic = new StructString(7);
    protected StructString description = new StructString(50);
    protected TestMarket market = new TestMarket();
    protected int numLegs;
//    protected StructArray<TestInstrumentLeg> legs;

    public StructString getMnemonic() {
        return mnemonic;
    }

    public TestMarket getMarket() {
        return market;
    }

//    public StructArray<TestInstrumentLeg> getLegs() {
//        return legs;
//    }

    public long getInstrId() {
        return instrId;
    }

    public void setInstrId(long instrId) {
        this.instrId = instrId;
    }

    public int getNumLegs() {
        return numLegs;
    }

//    public void addLeg( TestInstrumentLeg leg ) {
//        if ( leg.getInstrument().getNumLegs() > 0 )
//            throw new RuntimeException("cannot nest strategy instruments");
//        legs.set(numLegs++, leg);
//    }

//    public int getAccumulatedQty() {
//        int siz = legs.getStructElemSize();
//        int result = 1;
//        for (StructArray<TestInstrumentLeg>.StructArrIterator<TestInstrumentLeg> iterator = legs.iterator(); iterator.hasNext(); ) {
//            result += iterator.next(siz).getLegQty();
//        }
//        return result;
//    }

    public StructString getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TestInstrument{" +
                "instrId=" + instrId +
                ", mnemonic=" + mnemonic +
                ", description=" + description +
                ", market=" + market +
                ", numLegs=" + numLegs +
//                ", legs=" + legs +
                '}';
    }
}
