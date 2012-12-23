package de.ruedigermoeller.bridge.cpp;

import de.ruedigermoeller.bridge.FSTBridgeGenerator;import de.ruedigermoeller.bridge.FSTFactoryGen;

import java.io.PrintStream;
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
 * Date: 23.12.12
 * Time: 01:37
 * To change this template use File | Settings | File Templates.
 */
public class FSTCFactoryGen extends FSTFactoryGen {

    public FSTCFactoryGen(FSTBridgeGenerator gen) {
        super(gen);
    }

    protected String getBridgeClassName(Class c) {
        if ( c.getComponentType() != null && c.getComponentType().isPrimitive() ) {
            return c.getComponentType().getSimpleName().toUpperCase()+"ARR";
        }
        return "fst" + c.getSimpleName();
    }

    @Override
    public void generateFactoryHeader(PrintStream out) {
        out.println("#include \"FSTSerializationBase.h\"");
        out.println();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            if ( !cls.isArray() ) {
                out.println("#include \""+getBridgeClassName(cls)+".h\"");
            }
        }
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            if ( cls.isArray() ) {
                out.println("#define "+getBridgeClassName(cls)+" "+(gen.getIdForClass(cls)));
            }
        }
        out.println();
        out.println("class MyFSTConfiguration : public FSTConfiguration {");
        out.println("public:");
        out.println("    virtual FSTSerializationBase * MyFSTConfiguration::instantiate(int clzId);");
        out.println("};");
    }

    @Override
    public void generateFactoryImpl(PrintStream out) {
        out.println("#include \"MyFSTConfiguration.h\"");
        out.println();
        out.println("FSTSerializationBase * MyFSTConfiguration::instantiate(int clzId) {");
        out.println("    switch(clzId) {");

        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            if ( cls.isArray() ) {

            } else {
                out.println("        case "+(gen.getIdForClass(cls))+": return new "+getBridgeClassName(cls)+"(this);");
            }
        }
        out.println("        default: return NULL;");
        out.println("    }");
        out.println("};");
    }

    protected String getImplFileName() {
        return "MyFSTConfiguration.cpp";
    }

    protected String getHeaderFileName() {
        return "MyFSTConfiguration.h";
    }

}
