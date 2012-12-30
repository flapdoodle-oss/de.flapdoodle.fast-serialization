package de.ruedigermoeller.bridge.java;

import de.ruedigermoeller.bridge.FSTBridgeGenerator;import de.ruedigermoeller.bridge.FSTFactoryGen;

import java.io.PrintStream;
import java.util.HashSet;
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
public class FSTJavaFactoryGen extends FSTFactoryGen {

    public FSTJavaFactoryGen(FSTBridgeGenerator gen) {
        super(gen);
    }

    @Override
    public void generateFactoryHeader(PrintStream out) {
    }

    @Override
    public void generateFactoryImpl(PrintStream out) {
        out.println("package de.ruedigermoeller.bridge.java.generated;");
        out.println();
        out.println("import de.ruedigermoeller.bridge.java.*;");
        out.println("import java.io.*;");
        out.println();
        out.println("public class MyFSTFactory extends FSTJavaFactory {");
        out.println();
        HashSet<String> defined = new HashSet<String>();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) ) {
                continue;
            }
            defined.add(cidClazzName);
            out.println("    public static final int CID_"+ cidClazzName +" = "+(gen.getIdForClass(cls))+";");
        }
        out.println();
        out.println("    public Object instantiate(int clzId, FSTCountingInputStream in, FSTSerBase container, int streampos) throws IOException {");
        out.println("        int len = 0;");
        out.println("        switch(clzId) {");
        defined.clear();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) || gen.getMappedClasses().get(cls) != null ) {
                continue;
            }
            defined.add(cidClazzName);
            if ( cls.isArray() ) {
                out.println("            case CID_"+ cidClazzName +": len = container.readCInt(in); return new "+getBridgeClassName(cls).replace("[]","")+"[len];");
            } else if ( isSystemClass(cls))
            {
//                out.println("            case CID_"+getBridgeClassName(cls).toUpperCase()+": return new "+getBridgeClassName(cls)+"();");
            } else if ( !cls.isEnum() )
            {
                out.println("            case CID_"+ cidClazzName +": return new "+getBridgeClassName(cls)+"(this);");
            }
        }
        out.println("            default: return defaultInstantiate(getClass(clzId),in,container,streampos);");
        out.println("        }");
        out.println("    }");
        out.println("    ");
        out.println("    public Class getClass(int clzId) {");
        out.println("        int len = 0;");
        out.println("        switch(clzId) {");

        defined.clear();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) ) {
                continue;
            }
            defined.add(cidClazzName);
            out.println("            case CID_"+ cidClazzName +": return "+getBridgeClassName(cls)+".class;");
        }
        out.println("            default: throw new RuntimeException(\"unknown class id:\"+clzId);");
        out.println("        }");
        out.println("    }");
        out.println("}");
    }

    private String getCIDClazzName(Class cls) {
        if ( Enum.class.isAssignableFrom(cls) ) {
            return cls.getName().toUpperCase().replace('.','_').replace("$","_");
        }
        return getBridgeClassName(cls,false).toUpperCase().replace('.','_').replace("[]","_ARR");
    }

    protected String getImplFileName() {
        return "MyFSTFactory.java";
    }

    protected String getHeaderFileName() {
        return null;
    }

}
