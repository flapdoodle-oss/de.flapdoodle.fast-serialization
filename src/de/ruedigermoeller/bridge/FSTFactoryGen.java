package de.ruedigermoeller.bridge;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
 * Date: 23.12.12
 * Time: 01:23
 * To change this template use File | Settings | File Templates.
 */
public class FSTFactoryGen {

    protected FSTBridgeGenerator gen;

    public FSTFactoryGen(FSTBridgeGenerator gen) {
        this.gen = gen;
    }

    public void generateFactoryHeader(PrintStream out) {
        throw new NotImplementedException();
    }

    public void generateFactoryImpl(PrintStream out) {
    }

    public void generateFactory(String s) throws FileNotFoundException {
        if ( getHeaderFileName() != null ) {
            PrintStream header = new PrintStream(new FileOutputStream(s+ File.separator+getHeaderFileName()));
            generateFactoryHeader(header);
        }
        PrintStream impl = new PrintStream(new FileOutputStream(s+File.separator+getImplFileName()));
        generateFactoryImpl(impl);
    }

    protected String getImplFileName() {
        return null;
    }

    protected String getHeaderFileName() {
        return null;
    }
}
