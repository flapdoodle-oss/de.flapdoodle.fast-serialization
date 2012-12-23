package de.ruedigermoeller.bridge;

import de.ruedigermoeller.serialization.FSTClazzInfo;

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
 * Date: 22.12.12
 * Time: 19:40
 * To change this template use File | Settings | File Templates.
 */
public class FSTCHeaderGen extends FSTBridgeGen {

    protected void generateHeader(FSTClazzInfo info, PrintStream out, String depth) {
        out.println(depth+"#include \"FSTSerializationBase.h\"" );
        out.println(depth+"#include <iostream>" );
        out.println();
        String clz = getBridgeClassName(info);
        out.println(depth+"class "+ clz +":FSTSerializationBase {");
        out.println();
        out.println(depth+"public:");
        out.println(depth+"    ~"+ clz +"(void);");
        out.println(depth+"    virtual void "+clz+"::decode(istream &in);");
        out.println(depth+"    virtual void "+clz+"::encode(ostream &out);");
    }

    protected String getFileName(FSTClazzInfo info) {
        return getBridgeClassName(info) + ".h";
    }

    public void generateFieldDeclaration(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        if (type == boolean.class ) {
            out.println(depth+"jboolean "+fieldInfo.getField().getName()+";");
        } else
        if (type == int.class ) {
            out.println(depth+"jint "+fieldInfo.getField().getName()+";");
        } else
        if (type == char.class ) {
            out.println(depth+"jchar "+fieldInfo.getField().getName()+";");
        } else
        if (type == short.class ) {
            out.println(depth+"jshort "+fieldInfo.getField().getName()+";");
        } else
        if (type == long.class ) {
            out.println(depth+"jlong "+fieldInfo.getField().getName()+";");
        } else
        if (type == float.class ) {
            out.println(depth+"jfloat "+fieldInfo.getField().getName()+";");
        } else
        if (type == double.class ) {
            out.println(depth+"jdouble "+fieldInfo.getField().getName()+";");
        } else
        if (type == byte.class ) {
            out.println(depth+"jbyte "+fieldInfo.getField().getName()+";");
        }
    }

    protected void generateFooter(FSTClazzInfo info, PrintStream out, String depth) {
        out.println(depth+"};");
    }
}
