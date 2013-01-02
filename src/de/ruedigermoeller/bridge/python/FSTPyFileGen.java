package de.ruedigermoeller.bridge.python;

import de.ruedigermoeller.bridge.FSTBridge;
import de.ruedigermoeller.bridge.FSTFileGen;
import de.ruedigermoeller.serialization.FSTClazzInfo;

import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 01.01.13
 * Time: 23:29
 * To change this template use File | Settings | File Templates.
 */
public class FSTPyFileGen  extends FSTFileGen {

    public FSTPyFileGen(FSTBridge gen) {
        super(gen);
    }

    public boolean shouldGenerateClazz(FSTClazzInfo info) {
        if (isSystemClass(info.getClazz()) || info.getClazz().isEnum()) {
            return false;
        }
        return true;
    }

    protected void generateHeader(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        out.println("__author__ = 'fst generator'");
        out.println();
        out.println("import pyfst" );
        out.println("import array" );
        out.println();
        String clz = getBridgeClassName(info.getClazz());
        out.println(depth+"class "+ clz +"(pyfst.FSTSerBase) :");
        out.println();
        out.println(depth+"    def __init__(self, factory): " );
        out.println(depth+"        self.fac = factory");
        out.println();
    }

    protected String getFileName(FSTClazzInfo info) {
        return getBridgeClassName(info.getClazz()) + ".py";
    }

    public void generateReadMethod(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        depth+="    ";
        out.println(depth +"def decode(self, stream) :");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = layout.getFieldInfo();
        int numBool = layout.getNumBoolFields();
        out.println(depth+"    bools = 0;");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                out.println(depth+"    bools = self.readU(stream)");
                generateBoolRead((i/8)*8,Math.min(numBool,((i/8)+1)*8),fieldInfo,out,depth+"    ");
            }
        }
        for (int i = 0; i < fieldInfo.length; i++) {
            if ( fieldInfo[i].getType() != boolean.class ) {
                FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
                generateReadField(info,fstFieldInfo,out,depth+"    ");
            }
        }
    }

    protected void generateBoolRead( int start, int end, FSTClazzInfo.FSTFieldInfo fields[], PrintStream out, String depth ) {
        int mask = 128;
        for (int i = start; i < end; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fields[i];
            out.println(depth+fi.getField().getName()+" = (bools & "+mask+") != 0");
            mask = mask >>> 1;
        }
    }

    protected void generateBoolWrite( int start, int end, FSTClazzInfo.FSTFieldInfo fields[], PrintStream out, String depth ) {
        int mask = 128;
        for (int i = start; i < end; i++) {
            FSTClazzInfo.FSTFieldInfo fi = fields[i];
            out.println(depth+"bools = bools | (!"+fi.getField().getName()+"? 0 : "+mask+")");
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
                out.println(depth+name+" = self.readCInt( stream )");
            } else
            if (type == char.class ) {
                out.println(depth+name+" = self.readCChar( stream )");
            } else
            if (type == short.class ) {
                out.println(depth+name+" = self.readCShort( stream )");
            } else
            if (type == long.class ) {
                out.println(depth+name+" = self.readCLong( stream )");
            } else
            if (type == float.class ) {
                out.println(depth+name+" = self.readCFloat( stream )");
            } else
            if (type == double.class ) {
                out.println(depth+name+" = self.readCDouble( stream )");
            } else
            if (type == byte.class ) {
                out.println(depth+name+" = self.readS( stream )");
            } else if ( fieldInfo.isArray() && fieldInfo.getArrayDepth() == 1 ) {
                out.println(depth+name+" = self.decodeObject( stream )"); // array
            } else {
                throw new RuntimeException("cannot map type in field "+fieldInfo.getField());
            }
        } else {
            out.println(depth+name+" = self.decodeObject( stream )");
        }
    }


    public void generateWriteMethod(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
        if ( 1 != 0 ) return;
        depth+="    ";
        out.println(depth +"public void encode(FSTCountingOutputStream out)  throws IOException {");
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = layout.getFieldInfo();
        int numBool = info.getNumBoolFields();
        out.println(depth+"    writeCInt( out, "+gen.getIdForClass(info.getClazz())+");");
        out.println(depth+"    int bools = 0");
        for (int i = 0; i < numBool; i++) {
            if ( i%8 == 0 ) {
                generateBoolWrite((i/8)*8,Math.min(numBool,((i/8)+1)*8),fieldInfo,out,depth+"    ");
                out.println(depth + "    out.write(bools)");
                out.println(depth + "    bools = 0");
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
            out.println(depth+"encodeObject( out, "+name+" )");
        }
    }

/////////////////// header gen

    public void generateFieldDeclaration(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        Class type = fieldInfo.getType();
        String name = fieldInfo.getField().getName();
        if ( type.isArray() ) {
            out.println(depth+name +" = None # "+getBridgeClassName(mapDeclarationType(type.getComponentType(),gen.getCLInfo(type)))+"[] ");
        } else {
            String initVal = "None";
            if ( type == boolean.class) {
                initVal = "False";
            }
            if ( type == byte.class ||
                 type == char.class ||
                 type == short.class ||
                 type == int.class ||
                 type == long.class ||
                 type == float.class ||
                 type == double.class
            ) {
                initVal = "0";
            }
            if ( type == float.class) {
                initVal = "0.0";
            }
            if ( type == double.class) {
                initVal = "0.0";
            }
            out.println(depth+ name +" = "+initVal+" # "+getBridgeClassName(mapDeclarationType(type,gen.getCLInfo(type))));
        }
    }

    public void generateFieldGetter(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
        if ( 1 != 0) {
            return;
        }
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
    }

}
