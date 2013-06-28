package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.util.FSTUtil;
import javassist.*;
import javassist.bytecode.AccessFlag;
import javassist.expr.FieldAccess;
import sun.misc.Unsafe;

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
 * Date: 22.06.13
 * Time: 20:54
 * To change this template use File | Settings | File Templates.
 */
public class FSTByteArrayUnsafeStructGeneration implements FSTStructGeneration {

    @Override
    public FSTStructGeneration newInstance() {
        return new FSTByteArrayUnsafeStructGeneration();
    }

    @Override
    public void initStructInstance(Class tc, Object instance) {
        try {
//          tc.getField("___bytes").set(instance, bytes);
            tc.getField("___unsafe").set(instance, FSTUtil.unFlaggedUnsafe);
            tc.getField("___offset").set(instance, FSTUtil.bufoff);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void defineStructFields(FSTStructFactory fac, ClassPool pool, CtClass newClz, FSTClazzInfo clazzInfo) {
        try {
            CtField facf = new CtField(pool.get(FSTStructFactory.class.getName()),"___fac",newClz);
            facf.setModifiers(AccessFlag.PUBLIC);
            newClz.addField(facf);

            CtField offs = new CtField(pool.get(long.class.getName()),"___offset",newClz);
            offs.setModifiers(AccessFlag.PUBLIC);
            newClz.addField(offs);

            CtField bztes = new CtField(pool.get(byte[].class.getName()),"___bytes",newClz);
            bztes.setModifiers(AccessFlag.PUBLIC);
            newClz.addField(bztes);


            CtField uns = new CtField(pool.get(Unsafe.class.getName()),"___unsafe",newClz);
            uns.setModifiers(AccessFlag.PUBLIC);
            newClz.addField(uns);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void defineStructWriteAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        int off = fieldInfo.getStructOffset();
        try {
            if ( type == CtPrimitiveType.booleanType ) {
                f.replace("___unsafe.putBoolean(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.byteType ) {
                f.replace("___unsafe.putByte(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.charType ) {
                f.replace("___unsafe.putChar(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.shortType ) {
                f.replace("___unsafe.putShort(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.intType ) {
                f.replace("___unsafe.putInt(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.longType ) {
                f.replace("___unsafe.putLong(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.floatType ) {
                f.replace("___unsafe.putFloat(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                f.replace("___unsafe.putDouble(___bytes,"+off+"+___offset,$1);");
            } else
            {
                f.replace("{if ( 1 != 0 ) throw new RuntimeException(\"cannot rewrite subobject in structs. Field:"+f.getFieldName()+"\" );}");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void defineArrayAccessor(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        try {
            Class arrayType = fieldInfo.getArrayType();
            int off = fieldInfo.getStructOffset();
            String prefix ="{ int _st_off=___unsafe.getInt(___bytes,"+off+"+___offset)+"+FSTUtil.bufoff+";"+ // array base offset in byte arr
                    "int _st_len=___unsafe.getInt(___bytes,"+off+"+4+___offset); "+
                    "if ($1>=_st_len||$1<0) throw new ArrayIndexOutOfBoundsException($1);";
            if ( method.getReturnType() == CtClass.voidType ) {
                if ( arrayType == boolean.class ) {
                    method.setBody(prefix+"___unsafe.putBoolean(___bytes, _st_off+$1,$2);}");
                } else
                if ( arrayType == byte.class ) {
                    method.setBody(prefix+"___unsafe.putByte(___bytes, _st_off+$1,$2);}");
                } else
                if ( arrayType == char.class ) {
                    method.setBody(prefix+"___unsafe.putChar(___bytes, _st_off+$1*2,$2);}");
                } else
                if ( arrayType == short.class ) {
                    method.setBody(prefix+" ___unsafe.putShort(___bytes, _st_off+$1*2,$2);}");
                } else
                if ( arrayType == int.class ) {
                    method.setBody(prefix+" ___unsafe.putInt(___bytes, _st_off+$1*4,$2);}");
                } else
                if ( arrayType == long.class ) {
                    method.setBody(prefix+"___unsafe.putLong(___bytes, _st_off+$1*8,$2);}");
                } else
                if ( arrayType == double.class ) {
                    method.setBody(prefix+"___unsafe.putDouble(___bytes, _st_off+$1*8,$2);}");
                } else
                if ( arrayType == float.class ) {
                    method.setBody(prefix+"___unsafe.putFloat(___bytes, _st_off+$1*4,$2);}");
                } else {
                    method.setBody("{throw new RuntimeException(\"unssupported to rewrite Objects\");}");
                }
            } else {
                if ( arrayType == boolean.class ) {
                    method.setBody(prefix+"return ___unsafe.getBoolean(___bytes, _st_off+$1);}");
                } else
                if ( arrayType == byte.class ) {
                    method.setBody(prefix+"return ___unsafe.getByte(___bytes, _st_off+$1);}");
                } else
                if ( arrayType == char.class ) {
                    method.setBody(prefix+"return ___unsafe.getChar(___bytes, _st_off+$1*2); }");
                } else
                if ( arrayType == short.class ) {
                    method.setBody(prefix+"return ___unsafe.getShort(___bytes, _st_off+$1*2);}");
                } else
                if ( arrayType == int.class ) {
                    method.setBody(prefix+"return ___unsafe.getInt(___bytes, _st_off+$1*4);}");
                } else
                if ( arrayType == long.class ) {
                    method.setBody(prefix+"return ___unsafe.getLong(___bytes, _st_off+$1*8);}");
                } else
                if ( arrayType == double.class ) {
                    method.setBody(prefix+"return ___unsafe.getDouble(___bytes, _st_off+$1*8);}");
                } else
                if ( arrayType == float.class ) {
                    method.setBody(prefix+"return ___unsafe.getFloat(___bytes, _st_off+$1*4);}");
                } else { // object array
                    String meth =
                    "{"+
                        "int _st_len=___unsafe.getInt(___bytes,"+off+"+___offset); "+
                        "int _st_off=___unsafe.getInt(___bytes,"+off+"+$1*4+4+___offset);"+
                        "if ($1>=_st_len||$1<0) throw new ArrayIndexOutOfBoundsException($1);"+
                        "return ___fac.getStructWrapper(___bytes,_st_off);"+
                    "}";
                    method.setBody(meth);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void defineArrayLength(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int off = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return ___unsafe.getInt(___bytes,"+off+"+4+___offset); }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineStructReadAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        int off = fieldInfo.getStructOffset();
        try {
            if ( type == CtPrimitiveType.booleanType ) {
                f.replace("$_ = ___unsafe.getBoolean(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.byteType ) {
                f.replace("$_ = ___unsafe.getByte(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.charType ) {
                f.replace("$_ = ___unsafe.getChar(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.shortType ) {
                f.replace("$_ = ___unsafe.getShort(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.intType ) {
                f.replace("$_ = ___unsafe.getInt(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.longType ) {
                f.replace("$_ = ___unsafe.getLong(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.floatType ) {
                f.replace("$_ = ___unsafe.getFloat(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                f.replace("$_ = ___unsafe.getDouble(___bytes,"+off+"+___offset);");
            } else { // object ref
                f.replace("{ int __tmpOff = ___unsafe.getInt(___bytes, "+off+" + ___offset); $_ = ("+f.getField().getType().getName()+")___fac.getStructWrapper(___bytes,__tmpOff); }");
//                f.replace("{ Object _o = ___unsafe.toString(); $_ = _o; }");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
