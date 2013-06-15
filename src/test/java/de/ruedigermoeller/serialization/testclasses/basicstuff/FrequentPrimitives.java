package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.Serializable;

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
 * Date: 15.06.13
 * Time: 00:54
 * To change this template use File | Settings | File Templates.
 */
public class FrequentPrimitives implements Serializable, HasDescription {

    public static FrequentPrimitives[] getArray(int siz) {
        FrequentPrimitives[] res = new FrequentPrimitives[siz];
        for (int i = 0; i < res.length; i++) {
            res[i] = new FrequentPrimitives();
        }
        return res;
    }

    String str = "R.Moeller";
    String str1 = "R.Moeller1";
    boolean b0 = true;
    boolean b1 = false;
    boolean b2 = true;
    int test1 = 123456;
    int test2 = 123456;
    int test3 = 123456;
    int test4 = 123456;
    int test5 = -1;
    int test6 = 0;
    long l1 = 3845735345l;
    long l2 = 0l;
    double d = 122.33;

    @Override
    public String getDescription() {
        return "A class with a typical distribution of primitve fields: 2 short Strings, 3 boolean, 6 ints, 2 long, one double.";
    }
}
