package de.ruedigermoeller.bridge.python;

import de.ruedigermoeller.bridge.FSTBridge;
import de.ruedigermoeller.bridge.FSTFactoryGen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 01.01.13
 * Time: 23:29
 * To change this template use File | Settings | File Templates.
 */
public class FSTPyFactoryGen extends FSTFactoryGen {

    public FSTPyFactoryGen(FSTBridge gen) {
        super(gen);
    }

    @Override
    public void generateFactoryHeader(PrintStream out) {
    }

    @Override
    public void generateFactoryImpl(PrintStream out) {
        out.println("__author__ = 'fst generator'");
        out.println();
        out.println("import pyfst");
        out.println("import array");
        out.println("import numpy");
        out.println();
        out.println("class MyFSTFactory(pyfst.FSTPyFactory) : ");
        out.println();
        HashSet<String> defined = new HashSet<String>();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) ) {
                continue;
            }
            defined.add(cidClazzName);
            out.println("    CID_"+ cidClazzName +" = "+(gen.getIdForClass(cls)));
        }
        out.println();
        out.println("    def instantiate(self, clzId, stream, serbase, streampos) : ");
        out.println("        len = 0");
        defined.clear();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) || gen.getMappedClasses().get(cls) != null ) {
                continue;
            }
            defined.add(cidClazzName);
            if ( cls.isArray() ) {
                out.println("        if (clzId == self.CID_"+ cidClazzName +") :");
                out.println("            len = serbase.readCInt(stream)" );
                out.println("            return " + getArrayCreationString(cls));
            } else if ( isSystemClass(cls) )
            {
//                out.println("            case CID_"+getBridgeClassName(cls).toUpperCase()+": return new "+getBridgeClassName(cls)+"();");
            } else if ( !cls.isEnum() )
            {
                out.println("        if (clzId == self.CID_"+ cidClazzName +") :");
                out.println("            return "+getBridgeClassName(cls)+"(this)");
            }
        }
        out.println("        return self.defaultInstantiate(self.getClass(clzId),stream,container,streampos)");
        out.println("");
        out.println("    def getClass(self, clzId) :");

        defined.clear();
        for (Iterator<Class> iterator = gen.getSortedClazzes().iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            if ( defined.contains(cidClazzName) ) {
                continue;
            }
            defined.add(cidClazzName);
//            out.println("        if ( clzId == self.CID_" + cidClazzName + " ) : ");
//            out.println("            return "+getBridgeClassName(cls));
        }
        out.println("            #default: throw new RuntimeException(\"unknown class id:\"+clzId);");
        out.println("    ");
        out.println("    def getId(self, clazz) :");
        defined.clear();
        ArrayList<Class> sortedClazzes = new ArrayList<Class>();
        sortedClazzes.add(Boolean.class);
        sortedClazzes.add(Byte.class);
        sortedClazzes.add(Character.class);
        sortedClazzes.add(Short.class);
        sortedClazzes.add(Integer.class);
        sortedClazzes.add(Long.class);
        sortedClazzes.add(Float.class);
        sortedClazzes.add(Double.class);
        sortedClazzes.add(String.class);
        sortedClazzes.add(Date.class);
        sortedClazzes.addAll(gen.getSortedClazzes());
        for (Iterator<Class> iterator = sortedClazzes.iterator(); iterator.hasNext(); ) {
            Class cls = iterator.next();
            String cidClazzName = getCIDClazzName(cls);
            String bridgeClassName = getBridgeClassName(cls);
            if ( defined.contains(cidClazzName) || defined.contains(bridgeClassName)
                    || (bridgeClassName.equals("Object[]") && !cidClazzName.equals("JAVA_LANG_OBJECT_ARR") )
                    || (bridgeClassName.equals("String") && !cidClazzName.equals("STRING") )
                    )
            {
                continue;
            }
            defined.add(cidClazzName);
            defined.add(bridgeClassName);
//            out.println("        if ( clazz == " + bridgeClassName + " ) : ");
//            out.println("            return self.CID_"+cidClazzName);
        }
        out.println("        return 0");
    }

    private String getArrayCreationString(Class cls) {
        String bridgeClassName = getBridgeClassName(cls);
        if ("byte[]".equals(bridgeClassName)||"java.lang.Byte[]".equals(bridgeClassName)) {
            return "array.array('b', range(len))";
        }
        if ("boolean[]".equals(bridgeClassName)||"java.lang.Boolean[]".equals(bridgeClassName)) {
            return "array.array('c', range(len))";
        }
        if ("char[]".equals(bridgeClassName)||"java.lang.Character[]".equals(bridgeClassName)) {
            return "array.array('H', range(len))";
        }
        if ("short[]".equals(bridgeClassName)||"java.lang.Short[]".equals(bridgeClassName)) {
            return "array.array('h', range(len))";
        }
        if ("int[]".equals(bridgeClassName)||"java.lang.Integer[]".equals(bridgeClassName)) {
            return "array.array('i', range(len))";
        }
        if ("long[]".equals(bridgeClassName)||"java.lang.Long[]".equals(bridgeClassName)) {
            return "array.array('l', range(len))";
        }
        if ("float[]".equals(bridgeClassName)||"java.lang.Float[]".equals(bridgeClassName)) {
            return "array.array('f', range(len))";
        }
        if ("double[]".equals(bridgeClassName)||"java.lang.Double[]".equals(bridgeClassName)) {
            return "array.array('d', range(len))";
        }
        if ("java.lang.Object[]".equals(bridgeClassName)) {
            return "numpy.empty(shape=(len,), dtype=object)";
        }
        return bridgeClassName.replace("[]", "") + "[len]";
    }

    private String getCIDClazzName(Class cls) {
        if ( Enum.class.isAssignableFrom(cls) ) {
            return cls.getName().toUpperCase().replace('.','_').replace("$","_");
        }
        return getBridgeClassName(cls,false).toUpperCase().replace('.','_').replace("[]","_ARR");
    }

    protected String getImplFileName() {
        return "MyFSTFactory.py";
    }

    protected String getHeaderFileName() {
        return null;
    }

}
