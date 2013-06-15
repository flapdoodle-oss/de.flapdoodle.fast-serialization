package de.ruedigermoeller.serialization.testclasses.docusample;

import com.software.util.DeepEquals;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.basicstuff.FrequentCollections;
import de.ruedigermoeller.serialization.testclasses.basicstuff.LargeNativeArrays;
import de.ruedigermoeller.serialization.testclasses.basicstuff.PrimitiveArrays;
import de.ruedigermoeller.serialization.testclasses.basicstuff.Primitives;
import de.ruedigermoeller.serialization.testclasses.enterprise.Trader;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
public class FSTTestApp {

    static FSTConfiguration singletonConf = FSTConfiguration.createDefaultConfiguration();
    public static FSTConfiguration getInstance() {
        return singletonConf;
    }

    public void test(int i) throws IOException {
        FileOutputStream fout = null;
        FileInputStream in = null;
        try {
            fout = new FileOutputStream("/test"+i+".tmp");
            Object[] toWrite = {
                    PrimitiveArrays.createPrimArray(),
                    Trader.generateTrader(13, true),
                    new FrequentCollections(),
                    new LargeNativeArrays(),
                    Primitives.createPrimArray()
            };
            mywriteMethod(fout, toWrite);

            in = new FileInputStream("/test"+i+".tmp");
            Object read = myreadMethod(in);
            in.close();
            System.out.println(i+" SUCCESS:" + DeepEquals.deepEquals(read, toWrite));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if ( fout != null )
                fout.close();
            if ( in != null )
                in.close();
        }
    }

    public Object myreadMethod(InputStream stream) throws IOException, ClassNotFoundException {
        FSTObjectInput in = singletonConf.getObjectInput(stream);
        Object result = in.readObject();
        // DON'T: in.close(); prevents reuse and will result in an exception
        stream.close();
        return result;
    }

    public void mywriteMethod( OutputStream stream, Object toWrite ) throws IOException {
        FSTObjectOutput out = singletonConf.getObjectOutput(stream);
        out.writeObject( toWrite, Object.class );
        // DON'T out.close();
        out.flush();
        stream.close();
    }

    public static void main(String arg[]) throws IOException {
        System.setProperty("fts.unsafe","true");
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        for ( int i = 0; i < 500;  i++) {
            final int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        new FSTTestApp().test(finalI);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
        }
        executorService.shutdown();
    }

}
