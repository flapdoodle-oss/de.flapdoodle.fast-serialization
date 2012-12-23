package de.ruedigermoeller.bridge.cpp;

import de.ruedigermoeller.bridge.FSTBridgeGen;
import de.ruedigermoeller.bridge.FSTBridgeGenerator;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import sun.reflect.FieldInfo;

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
public class FSTCFileGen extends FSTBridgeGen {

    public FSTCFileGen(FSTBridgeGenerator gen) {
        super(gen);
    }

    protected void generateHeader(FSTClazzInfo info, PrintStream out, String depth) {
        out.print(depth + "#include \"" + getBridgeClassName(info) + ".h\"");
        out.println();
        out.println();
        out.println(depth+getBridgeClassName(info)+"::~"+getBridgeClassName(info)+"(void) {");
        out.println(depth+"}");
        out.println();
    }

    protected String getFileName(FSTClazzInfo info) {
        return getBridgeClassName(info) + ".cpp";
    }

    public void generateReadMethod( FSTClazzInfo info, PrintStream out, String depth ) {
        out.println(depth +"void "+ getBridgeClassName(info) + "::decode(istream &in) {");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = info.getFieldInfo();
        int numBool = info.getNumBoolFields();
        out.println(depth+"    char bools = 0;");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                out.println(depth+"    bools = in.get();");
                generateBoolRead((i/8)*8,Math.min(numBool,((i/8)+1)*8),fieldInfo,out,depth+"    ");
            }
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            if ( fieldInfo[i].getType() != boolean.class ) {
                FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
                generateReadField(info,fstFieldInfo,out,depth+"    ");
            }
        }
        out.println(depth+"}");
    }

    protected void generateBoolRead( int start, int end, FSTClazzInfo.FSTFieldInfo fields[], PrintStream out, String depth ) {
        int mask = 128;
        for (int i = start; i < end; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fields[i];
            out.println(depth+fi.getField().getName()+" = (bools & "+mask+") != 0;");
            mask = mask >>> 1;
        }
    }

    protected void generateBoolWrite( int start, int end, FSTClazzInfo.FSTFieldInfo fields[], PrintStream out, String depth ) {
        int mask = 128;
        for (int i = start; i < end; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fields[i];
            out.println(depth+"bools = bools | ("+fi.getField().getName()+"? 0 : "+mask+");");
            mask = mask >>> 1;
        }
    }

    protected void generateReadField(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        String name = fieldInfo.getField().getName();
        if ( fieldInfo.isIntegral() ) {
            if (type == boolean.class ) {
                //out.println(depth+"jboolean "+fieldInfo.getField().getName()+";");
            } else
            if (type == int.class ) {
                out.println(depth+name+" = readCInt( in );");
            } else
            if (type == char.class ) {
                out.println(depth+name+" = readCChar( in );");
            } else
            if (type == short.class ) {
                out.println(depth+name+" = readCShort( in );");
            } else
            if (type == long.class ) {
                out.println(depth+name+" = readCLong( in );");
            } else
            if (type == float.class ) {
                out.println(depth+name+" = readCFloat( in );");
            } else
            if (type == double.class ) {
                out.println(depth+name+" = readCDouble( in );");
            } else
            if (type == byte.class ) {
                out.println(depth+name+" = readByte( in );");
            } else if ( fieldInfo.isArray() && fieldInfo.getArrayDepth() == 1 ) {
                out.println(depth+name+" = ("+FSTCHeaderGen.getCPPArrayClzName(fieldInfo.getType())+"*)decodeObject( in );"); // array
            }
        } else {
            out.println(depth+name+" = decodeObject( in );");
        }
    }

    public void generateWriteMethod( FSTClazzInfo info, PrintStream out, String depth ) {
        out.println(depth +"void "+getBridgeClassName(info) + "::encode(ostream &out) {");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = info.getFieldInfo();
        int numBool = info.getNumBoolFields();
        out.println(depth+"    char bools = 0;");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                generateBoolWrite((i/8)*8,Math.min(numBool,((i/8)+1)*8),fieldInfo,out,depth+"    ");
                out.println(depth+"    out.put(bools);");
            }
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            generateWriteField(info,fstFieldInfo,out,depth+"    ");
        }
        out.println(depth+"}");
    }

    protected void generateWriteField(FSTClazzInfo clInfo, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        if ( fieldInfo.isIntegral() && ! fieldInfo.isArray() ) {
            Class type = fieldInfo.getType();
            String name = fieldInfo.getField().getName();
            if (type == boolean.class ) {
                //out.println(depth+"jboolean "+fieldInfo.getField().getName()+";");
            } else
            if (type == int.class ) {
                out.println(depth+"writeCInt( out, "+ name + ");");
            } else
            if (type == char.class ) {
                out.println(depth+"writeCChar( out, "+ name + ");");
            } else
            if (type == short.class ) {
                out.println(depth+"writeCShort( out, "+ name + ");");
            } else
            if (type == long.class ) {
                out.println(depth+"writeCLong( out, "+ name + ");");
            } else
            if (type == float.class ) {
                out.println(depth+"writeCFloat( out, "+ name + ");");
            } else
            if (type == double.class ) {
                out.println(depth+"writeCDouble( out, "+ name + ");");
            } else
            if (type == byte.class ) {
                out.println(depth+"writeByte( out, "+ name + ");");
            }
        }
    }


}
