package de.ruedigermoeller.serialization.testclasses.crosslanguage;

import de.ruedigermoeller.bridge.FSTBridgeGenerator;
import de.ruedigermoeller.bridge.cpp.FSTCFactoryGen;
import de.ruedigermoeller.bridge.cpp.FSTCFileGen;
import de.ruedigermoeller.bridge.cpp.FSTCHeaderGen;
import de.ruedigermoeller.bridge.java.generated.MyFSTFactory;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.basicstuff.Primitives;
import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedDataType;
import de.ruedigermoeller.serialization.testclasses.enterprise.ObjectOrientedInt;
import de.ruedigermoeller.serialization.testclasses.enterprise.SimpleOrder;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * Date: 22.12.12
 * Time: 12:52
 * To change this template use File | Settings | File Templates.
 */
public class Cross implements Serializable {
    boolean a = true,b = false,c = false, d = false, e = true, aa = false,ab = false,ac = false, ad = false, ae = false, xx = false;
    int anInt = 1234567890;
    char achar = 12345;
    byte aByte = -13;
    short aShort = 12345;
    long aLong = 1234567890123456l;
    float aFloat = 1.2345678f;
    double aDouble = -1.876176d;
    byte testByte [] = {1,2,3,-1,-2,-3};
    char testChar [] = {1,2,3,38000};
    short testShort [] = {1,2,3,-18000,18000};
    int testInt [] = {-100,100,200,300,398475398,-398457398};
    long testlong [] = {-100,100,200,300,39458734235l,-34953456934l};
    double testDouble[] = { 234.0234234d, -112312.0234234d };
    float testFloat[] = { 234.0234234f, -112312.0234234f };
    String string = "Rüdiger Möller";
    CrossB crossB;
    Object object = new float[] { 234.0234234f, -112312.0234234f };
    Object other[] = { "Hallo", "Holla","Hallo" };

    public Cross(CrossB b) {
        crossB = b;
    }

    public static void main( String arg[]) throws IOException {
        FSTBridgeGenerator generator = new FSTBridgeGenerator();
        generator.addClass(Cross.class);
        generator.addClass(CrossB.class);

        FSTConfiguration conf = generator.getConf();

        FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(Cross.class);
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = clInfo.getFieldInfo();
        System.out.println("{");
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            System.out.println("  "+fstFieldInfo.getType().getSimpleName()+" "+fstFieldInfo.getField().getName() );
        }
        System.out.println("}");
        System.out.println();

//        generator.generateClasses( FSTBridgeGenerator.Language.CPP,  "C:\\Users\\ruedi\\Documents\\Visual Studio 2012\\Projects\\FST\\FST");
        generator.generateClasses( FSTBridgeGenerator.Language.JAVA, "F:\\work\\FSTCrossTest\\src\\de\\ruedigermoeller\\bridge\\java\\generated");

        FSTObjectOutput out = new FSTObjectOutput(new FileOutputStream("\\tmp\\crosstest.oos"), conf);
        out.writeObject(new Cross(new CrossB()));
        byte[] buffer = out.getBuffer();
        for ( int i = 0; i < out.getWritten(); i++ ) {
            System.out.println("["+i+"]"+buffer[i]);
        }
        out.close();

    }
}
