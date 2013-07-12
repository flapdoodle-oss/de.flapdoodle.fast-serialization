package de.ruedigermoeller.heapoff.structs.impl;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.util.FSTUtil;
import javassist.*;
import javassist.expr.FieldAccess;

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
    public void defineStructWriteAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        int off = fieldInfo.getStructOffset();
        try {
            if ( type == CtPrimitiveType.booleanType ) {
                f.replace("unsafe.putBoolean(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.byteType ) {
                f.replace("unsafe.putByte(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.charType ) {
                f.replace("unsafe.putChar(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.shortType ) {
                f.replace("unsafe.putShort(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.intType ) {
                f.replace("unsafe.putInt(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.longType ) {
                f.replace("unsafe.putLong(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.floatType ) {
                f.replace("unsafe.putFloat(___bytes,"+off+"+___offset,$1);");
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                f.replace("unsafe.putDouble(___bytes,"+off+"+___offset,$1);");
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
            String prefix ="{ int _st_off=___offset + unsafe.getInt(___bytes,"+off+"+___offset);"+ // array base offset in byte arr
                    "int _st_len=unsafe.getInt(___bytes,"+off+"+4+___offset); "+
                    "if ($1>=_st_len||$1<0) throw new ArrayIndexOutOfBoundsException($1);";
            if ( method.getReturnType() == CtClass.voidType ) {
                if ( arrayType == boolean.class ) {
                    method.setBody(prefix+"unsafe.putBoolean(___bytes, _st_off+$1,$2);}");
                } else
                if ( arrayType == byte.class ) {
                    method.setBody(prefix+"unsafe.putByte(___bytes, _st_off+$1,$2);}");
                } else
                if ( arrayType == char.class ) {
                    method.setBody(prefix+"unsafe.putChar(___bytes, _st_off+$1*2,$2);}");
                } else
                if ( arrayType == short.class ) {
                    method.setBody(prefix+" unsafe.putShort(___bytes, _st_off+$1*2,$2);}");
                } else
                if ( arrayType == int.class ) {
                    method.setBody(prefix+" unsafe.putInt(___bytes, _st_off+$1*4,$2);}");
                } else
                if ( arrayType == long.class ) {
                    method.setBody(prefix+"unsafe.putLong(___bytes, _st_off+$1*8,$2);}");
                } else
                if ( arrayType == double.class ) {
                    method.setBody(prefix+"unsafe.putDouble(___bytes, _st_off+$1*8,$2);}");
                } else
                if ( arrayType == float.class ) {
                    method.setBody(prefix+"unsafe.putFloat(___bytes, _st_off+$1*4,$2);}");
                } else {
                    method.setBody("{throw new RuntimeException(\"unssupported to rewrite Objects\");}");
                }
            } else {
                if ( arrayType == boolean.class ) {
                    method.setBody(prefix+"return unsafe.getBoolean(___bytes, _st_off+$1);}");
                } else
                if ( arrayType == byte.class ) {
                    method.setBody(prefix+"return unsafe.getByte(___bytes, _st_off+$1);}");
                } else
                if ( arrayType == char.class ) {
                    method.setBody(prefix+"return unsafe.getChar(___bytes, _st_off+$1*2); }");
                } else
                if ( arrayType == short.class ) {
                    method.setBody(prefix+"return unsafe.getShort(___bytes, _st_off+$1*2);}");
                } else
                if ( arrayType == int.class ) {
                    method.setBody(prefix+"return unsafe.getInt(___bytes, _st_off+$1*4);}");
                } else
                if ( arrayType == long.class ) {
                    method.setBody(prefix+"return unsafe.getLong(___bytes, _st_off+$1*8);}");
                } else
                if ( arrayType == double.class ) {
                    method.setBody(prefix+"return unsafe.getDouble(___bytes, _st_off+$1*8);}");
                } else
                if ( arrayType == float.class ) {
                    method.setBody(prefix+"return unsafe.getFloat(___bytes, _st_off+$1*4);}");
                } else { // object array
                    String meth =
                    prefix+
                        "int _elem_len=unsafe.getInt(___bytes,"+off+"+8+___offset); "+
                        "return ("+fieldInfo.getArrayType().getName()+")___fac.getStructPointerByOffset(___bytes,(long)_st_off+$1*_elem_len);"+
                    "}";
                    method.setBody(meth);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void defineArrayIndex(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int off = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return "+off+"; }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineArrayLength(FSTClazzInfo.FSTFieldInfo fieldInfo, FSTClazzInfo clInfo, CtMethod method) {
        int off = fieldInfo.getStructOffset();
        try {
            method.setBody("{ return unsafe.getInt(___bytes,"+off+"+4+___offset); }");
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void defineStructReadAccess(FieldAccess f, CtClass type, FSTClazzInfo.FSTFieldInfo fieldInfo) {
        int off = fieldInfo.getStructOffset();
        try {
            if ( type == CtPrimitiveType.booleanType ) {
                f.replace("$_ = unsafe.getBoolean(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.byteType ) {
                f.replace("$_ = unsafe.getByte(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.charType ) {
                f.replace("$_ = unsafe.getChar(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.shortType ) {
                f.replace("$_ = unsafe.getShort(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.intType ) {
                f.replace("$_ = unsafe.getInt(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.longType ) {
                f.replace("$_ = unsafe.getLong(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.floatType ) {
                f.replace("$_ = unsafe.getFloat(___bytes,"+off+"+___offset);");
            } else
            if ( type == CtPrimitiveType.doubleType ) {
                f.replace("$_ = unsafe.getDouble(___bytes,"+off+"+___offset);");
            } else { // object ref
                String typeString = type.getName();
                f.replace("{ long __tmpOff = ___offset + unsafe.getInt(___bytes, "+off+" + ___offset); $_ = ("+ typeString +")___fac.getStructPointerByOffset(___bytes,__tmpOff); }");
//                f.replace("{ Object _o = unsafe.toString(); $_ = _o; }");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
