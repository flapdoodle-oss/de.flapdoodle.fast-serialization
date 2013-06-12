package de.ruedigermoeller.bridge.java;

import de.ruedigermoeller.bridge.FSTFileGen;
import de.ruedigermoeller.bridge.FSTBridge;
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
public class FSTJavaFileGen extends FSTFileGen {

    public FSTJavaFileGen(FSTBridge gen) {
        super(gen);
    }

    public boolean shouldGenerateClazz(FSTClazzInfo info) {
        if (isSystemClass(info.getClazz()) || info.getClazz().isEnum()) {
            return false;
        }
        return true;
    }

    protected void generateHeader(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        out.println("package de.ruedigermoeller.bridge.java.generated;");
        out.println();
        out.println("import de.ruedigermoeller.bridge.java.*;" );
        out.println("import java.io.*;" );
        out.println("import java.util.*;" );
        out.println();
        String clz = getBridgeClassName(info.getClazz());
        out.println(depth+"public class "+ clz +" extends FSTSerBase {");
        out.println();
        out.println(depth+"    public "+ clz +"(FSTJavaFactory context)" );
        out.println(depth+"    {");
        out.println(depth+"        super(context);");
        out.println(depth+"    }");
        out.println();
    }

    protected String getFileName(FSTClazzInfo info) {
        return getBridgeClassName(info.getClazz()) + ".java";
    }

    public void generateReadMethod(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        depth+="    ";
        out.println(depth +"public void decode(FSTCountingInputStream in)  throws IOException {");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = layout.getFieldInfo();
        int numBool = layout.getNumBoolFields();
        out.println(depth+"    int bools = 0;");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                out.println(depth+"    bools = (in.read()+256)&0xff;");
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
            out.println(depth+"bools = bools | (!"+fi.getField().getName()+"? 0 : "+mask+");");
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
                out.println(depth+name+" = ("+ getBridgeClassName(fieldInfo.getType().getComponentType()) +"[])decodeObject( in );"); // array
            } else {
                throw new RuntimeException("cannot map type in field "+fieldInfo.getField());
            }
        } else {
            out.println(depth+name+" = ("+getBridgeClassName(fieldInfo.getType())+")"+"decodeObject( in );");
        }
    }


    public void generateWriteMethod(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        depth+="    ";
        out.println(depth +"public void encode(FSTCountingOutputStream out)  throws IOException {");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = layout.getFieldInfo();
        int numBool = info.getNumBoolFields();
        out.println(depth+"    writeCInt( out, "+gen.getIdForClass(info.getClazz())+");");
        out.println(depth+"    int bools = 0;");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                generateBoolWrite((i/8)*8,Math.min(numBool,((i/8)+1)*8),fieldInfo,out,depth+"    ");
                out.println(depth + "    out.write(bools);");
                out.println(depth + "    bools = 0;");
            }
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            generateWriteField(info,fstFieldInfo,out,depth+"    ");
        }
        out.println(depth+"}");
    }

    protected void generateWriteField(FSTClazzInfo clInfo, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        String name = fieldInfo.getField().getName();
        if ( fieldInfo.isIntegral() && ! fieldInfo.isArray()) {
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
            } else {
                throw new RuntimeException("cannot map type in field "+fieldInfo.getField());
            }
        } else {
            out.println(depth+"encodeObject( out, "+name+" );");
        }
    }

/////////////////// header gen

    public void generateFieldDeclaration(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        String name = fieldInfo.getField().getName();
        if ( type.isArray() ) {
            out.println(depth+"protected "+getBridgeClassName(mapDeclarationType(type.getComponentType(),gen.getCLInfo(type)))+"[] "+ name +";");
        } else {
            out.println(depth+"protected "+getBridgeClassName(mapDeclarationType(type,gen.getCLInfo(type)))+" "+ name +";");
        }
    }

    public void generateFieldGetter(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        String name = fieldInfo.getField().getName();
        if ( type.isArray() ) {
            String typeString = getBridgeClassName(mapDeclarationType(type.getComponentType(), gen.getCLInfo(type)));
            String nameString = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            out.println(depth+"public "+ typeString +"[] get"+ nameString +"() { return "+name+";"+" }");
            out.println(depth + "public void set" + nameString + "( " + typeString + "[] arg) { " + name + "=arg;" + " }");
        } else {
            String typeString = getBridgeClassName(mapDeclarationType(type, gen.getCLInfo(type)));
            String nameString = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            out.println(depth+"public "+ typeString +" get"+ nameString +"() { return "+name+";"+" }");
            out.println(depth + "public void set" + nameString + "( " + typeString + " arg) { " + name + "=arg;" + " }");
        }
    }

    protected void generateFooter(FSTClazzInfo info, PrintStream out, String depth) {
        out.println(depth+"}");
    }

}
