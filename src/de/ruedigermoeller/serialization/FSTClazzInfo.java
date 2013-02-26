/*
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 */
package de.ruedigermoeller.serialization;

import de.ruedigermoeller.serialization.annotations.*;
import de.ruedigermoeller.serialization.util.FSTObject2ObjectMap;
import de.ruedigermoeller.serialization.util.FSTUtil;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public final class FSTClazzInfo {

    Class clazz;
    FSTFieldInfo fieldInfo[]; // serializable fields
    FSTObject2ObjectMap<String, FSTFieldInfo> fieldMap = new FSTObject2ObjectMap<String, FSTFieldInfo>(15); // all fields
    Constructor cons;
    Method writeReplaceMethod, readResolveMethod;
    boolean requiresCompatibleMode;

    FSTObject2ObjectMap<Class, FSTCompatibilityInfo> compInfo = new FSTObject2ObjectMap<Class, FSTCompatibilityInfo>(7);
    Class[] predict;
    boolean equalIsIdentity;
    boolean equalIsBinary;
    boolean flat; // never share instances of this class

    FSTObjectSerializer ser;
    FSTClazzInfoRegistry reg;
    private boolean ignoreAnn;
    boolean externalizable;

    public FSTClazzInfo(Class clazz, FSTClazzInfoRegistry infoRegistry, boolean ignoreAnnotations) {
        this.clazz = clazz;
        reg = infoRegistry;
        ignoreAnn = ignoreAnnotations;
        createFields(clazz);
        if (Externalizable.class.isAssignableFrom(clazz)) {
            externalizable = true;
            cons = FSTUtil.findConstructorForExternalize(clazz);
        } else {
            externalizable = false;
            cons = FSTUtil.findConstructorForSerializable(clazz);
        }
        if ( ! ignoreAnnotations ) {
            Predict annotation = (Predict) clazz.getAnnotation(Predict.class);
            if (annotation != null) {
                predict = annotation.value();
            }
            equalIsIdentity = clazz.isAnnotationPresent(EqualnessIsIdentity.class);
            equalIsBinary = clazz.isAnnotationPresent(EqualnessIsBinary.class);
            flat = clazz.isAnnotationPresent(Flat.class);
        }

        if (cons != null) {
            cons.setAccessible(true);
        }
    }

    public int getNumBoolFields() {
        FSTFieldInfo[] fis = getFieldInfo();
        for (int i = 0; i < fis.length; i++) {
            FSTFieldInfo fstFieldInfo = fis[i];
            if ( fstFieldInfo.getType() != boolean.class ) {
                return i;
            }
        }
        return fis.length;
    }

    public boolean isExternalizable() {
        return externalizable;
    }

    public final boolean isEqualIsBinary() {
        return equalIsBinary;
    }

    public final boolean isEqualIsIdentity() {
        return equalIsIdentity;
    }

    public final boolean isFlat() {
        return flat;
    }

    public final Class[] getPredict() {
        return predict;
    }

    public final Object newInstance() {
        try {
            return cons.newInstance();
        } catch (Throwable ignored) {
            return null;
        }
    }

    public final List<Field> getAllFields(Class c, List<Field> res) {
        if (res == null) {
            res = new ArrayList<Field>();
        }
        if (c == null) {
            return res;
        }
        res.addAll(Arrays.asList(c.getDeclaredFields()));
        return getAllFields(c.getSuperclass(), res);
    }

    public final FSTFieldInfo[] getFieldInfo() {
        return fieldInfo;
    }

    public final FSTFieldInfo getFieldInfo(String name, Class declaringClass) {
        return fieldMap.get(declaringClass.getName() + "#" + name);
    }

    private void createFields(Class c) {
        if (c.isInterface()||c.isPrimitive()) {
            return;
        }
        List<Field> fields = getAllFields(c, null);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            fieldMap.put(field.getDeclaringClass().getName() + "#" + field.getName(), createFieldInfo(field));
        }

        // comp info sort order
        Comparator<FSTFieldInfo> infocomp = new Comparator<FSTFieldInfo>() {
            @Override
            public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
                int res = 0;
                res = o1.getType().getSimpleName().compareTo(o2.getType().getSimpleName());
                if (res == 0)
                    res = o1.getType().getName().compareTo(o2.getType().getName());
                if (res == 0) {
                    Class declaringClass = o1.getType().getDeclaringClass();
                    Class declaringClass1 = o2.getType().getDeclaringClass();
                    if (declaringClass == null && declaringClass1 == null) {
                        return 0;
                    }
                    if (declaringClass != null && declaringClass1 == null) {
                        return 1;
                    }
                    if (declaringClass == null && declaringClass1 != null) {
                        return -1;
                    }
                    if (res == 0) {
                        return declaringClass.getName().compareTo(declaringClass1.getName());
                    }
                }
                return res;
            }
        };
        Class curCl = c;
        fields.clear();

        while (curCl != Object.class) {
            ObjectStreamClass os = null;
            try {
                os = ObjectStreamClass.lookup(curCl);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (os != null) {
                final ObjectStreamField[] fi = os.getFields();
                List<FSTFieldInfo> curClzFields = new ArrayList<FSTFieldInfo>();
                if (fi != null) {
                    for (int i = 0; i < fi.length; i++) {
                        ObjectStreamField objectStreamField = fi[i];
                        String ff = objectStreamField.getName();
                        final FSTFieldInfo fstFieldInfo = fieldMap.get(curCl.getName() + "#" + ff);
                        if (fstFieldInfo != null && fstFieldInfo.getField() != null) {
                            curClzFields.add(fstFieldInfo);
                            fields.add(fstFieldInfo.getField());
                        } else {
                            if (FSTObjectOutput.DUMP)
                                System.out.println("Class:" + c.getName() + " no field found " + ff);
                        }
                    }
                }
                Collections.sort(curClzFields, infocomp);
                FSTCompatibilityInfo info = new FSTCompatibilityInfo(curClzFields, curCl);
                compInfo.put(curCl, info);
                if (info.needsCompatibleMode()) {
                    requiresCompatibleMode = true;
                }
            }
            curCl = curCl.getSuperclass();
        }

        // default sort order
        Comparator<FSTFieldInfo> comp = new Comparator<FSTFieldInfo>() {
            @Override
            public int compare(FSTFieldInfo o1, FSTFieldInfo o2) {
                int res = 0;
                if ( o1.getType() == boolean.class && o2.getType() != boolean.class ) {
                    return -1;
                }
                if ( o1.getType() != boolean.class && o2.getType() == boolean.class ) {
                    return 1;
                }
                if ( o1.isConditional() && ! o2.isConditional() ) {
                    res = 1;
                } else if ( ! o1.isConditional() && o2.isConditional() ) {
                    res = -1;
                } else if ( o1.isIntegral() && !o2.isIntegral() )
                    res = -1;
                if ( res == 0 )
                    res = (int) (o1.getMemOffset()-o2.getMemOffset());
                if ( res == 0 )
                    res = o1.getType().getSimpleName().compareTo(o2.getType().getSimpleName());
                if (res == 0)
                    res = o1.getField().getName().compareTo(o2.getField().getName());
                if (res == 0) {
                    return o1.getField().getDeclaringClass().getName().compareTo(o2.getField().getDeclaringClass().getName());
                }
                return res;
            }
        };
        fieldInfo = new FSTFieldInfo[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            fieldInfo[i] = createFieldInfo(field);
            fieldMap.put(field.getDeclaringClass().getName() + "#" + field.getName(), fieldInfo[i]);
        }
        Arrays.sort(fieldInfo, comp);
        writeReplaceMethod = FSTUtil.findDerivedMethod(
                c, "writeReplace", null, Object.class);
        readResolveMethod = FSTUtil.findDerivedMethod(
                c, "readResolve", null, Object.class);
        if (writeReplaceMethod != null) {
            writeReplaceMethod.setAccessible(true);
        }
        if (readResolveMethod != null) {
            readResolveMethod.setAccessible(true);
        }
    }

    public boolean useCompatibleMode() {
        return requiresCompatibleMode; // || writeReplaceMethod != null || readResolveMethod != null;
    }


    private FSTFieldInfo createFieldInfo(Field field) {
        field.setAccessible(true);
        Predict predict = field.getAnnotation(Predict.class);
        return new FSTFieldInfo(predict != null ? predict.value() : null, field, ignoreAnn);
    }

    public final int getByteValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getByte(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getByte(obj);
    }

    public final int getCharValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getChar(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getChar(obj);
    }

    public final int getShortValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getShort(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getShort(obj);
    }

    public final int getIntValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getInt(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getInt(obj);
    }

    public final long getLongValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getLong(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getLong(obj);
    }

    public final boolean getBooleanValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getBoolean(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getBoolean(obj);
    }

    public final Object getObjectValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getObject(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().get(obj);
    }

    public final float getFloatValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getFloat(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getFloat(obj);
    }

    public final double getDoubleValue(Object obj, FSTFieldInfo fstFieldInfo) throws IllegalAccessException {
        if (fstFieldInfo.memOffset >= 0 ) {
            return FSTUtil.unsafe.getDouble(obj,fstFieldInfo.memOffset);
        }
        return fstFieldInfo.getField().getDouble(obj);
    }

    public final void setByteValue(Object newObj, FSTFieldInfo subInfo, byte b) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putByte(newObj,subInfo.memOffset,b);
            return;
        }
        subInfo.getField().setByte(newObj, b);
    }

    public final void setCharValue(Object newObj, FSTFieldInfo subInfo, char c) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putChar(newObj,subInfo.memOffset,c);
            return;
        }
        subInfo.getField().setChar(newObj, c);
    }

    public final void setShortValue(Object newObj, FSTFieldInfo subInfo, short i1) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putShort(newObj,subInfo.memOffset,i1);
            return;
        }
        subInfo.getField().setShort(newObj, i1);
    }

    public final void setIntValue(Object newObj, FSTFieldInfo subInfo, int i1) throws IllegalAccessException {
        if ( subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putInt(newObj,subInfo.memOffset,i1);
        } else {
            subInfo.getField().setInt(newObj, i1);
        }
    }

    public final void setLongValue(Object newObj, FSTFieldInfo subInfo, long i1) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putLong(newObj,subInfo.memOffset,i1);
            return;
        }
        subInfo.getField().setLong(newObj, i1);
    }

    public final void setBooleanValue(Object newObj, FSTFieldInfo subInfo, boolean i1) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putBoolean(newObj,subInfo.memOffset,i1);
            return;
        }
        subInfo.getField().setBoolean(newObj, i1);
    }

    public final void setObjectValue(Object newObj, FSTFieldInfo subInfo, Object i1) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putObject(newObj,subInfo.memOffset,i1);
            return;
        }
        subInfo.getField().set(newObj, i1);
    }

    public final void setFloatValue(Object newObj, FSTFieldInfo subInfo, float l) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putFloat(newObj,subInfo.memOffset,l);
            return;
        }
        subInfo.getField().setFloat(newObj, l);
    }

    public final void setDoubleValue(Object newObj, FSTFieldInfo subInfo, double l) throws IllegalAccessException {
        if (subInfo.memOffset >= 0 ) {
            FSTUtil.unsafe.putDouble(newObj,subInfo.memOffset,l);
            return;
        }
        subInfo.getField().setDouble(newObj, l);
    }

    public final Method getReadResolveMethod() {
        return readResolveMethod;
    }

    public final Method getWriteReplaceMethod() {
        return writeReplaceMethod;
    }

    public final Class getClazz() {
        return clazz;
    }

    public final static class FSTFieldInfo {

        final public static int BOOL = 1;
        final public static int BYTE = 2;
        final public static int CHAR = 3;
        final public static int SHORT = 4;
        final public static int INT = 5;
        final public static int LONG = 6;
        final public static int FLOAT = 7;
        final public static int DOUBLE = 8;

        Class possibleClasses[];
        Class type;
        Field field;
        boolean integral = false;
        int arrayDim;
        Class arrayType;
        boolean flat = false;
        boolean thin = false;
        boolean isArr = false;
        boolean isConditional = false;
        boolean isCompressed = false;
        boolean isPlain = false;
        int integralType;
        FSTClazzInfo lastInfo;
        long memOffset = -1;

        public FSTFieldInfo(Class[] possibleClasses, Field fi, boolean ignoreAnnotations) {
            this.possibleClasses = possibleClasses;
            field = fi;
            if (fi == null) {
                isArr = false;
            } else {
                isArr = field.getType().isArray();
                type = fi.getType();
                if ( FSTUtil.unsafe != null ) {
                    fi.setAccessible(true);
                    if ( ! Modifier.isStatic(fi.getModifiers()) ) {
                        try {
                            memOffset = FSTUtil.unsafe.objectFieldOffset(fi);
//                            int x = 1;
                        } catch ( Throwable th ) {
//                            int y = 1;
                        }
                    }
                }
            }
            if (isArray()) {
                String clName = field.getType().getName();
                arrayDim = 1 + clName.lastIndexOf('[');
                arrayType = calcComponentType(field.getType());
            }
            calcIntegral();
            if ( fi != null && ! ignoreAnnotations ) {
                isPlain = fi.isAnnotationPresent(Plain.class);
                flat = fi.isAnnotationPresent(Flat.class);
                thin = fi.isAnnotationPresent(Thin.class);
                isConditional = fi.isAnnotationPresent(Conditional.class);
                isCompressed = fi.isAnnotationPresent(Compress.class);
                if (isIntegral()) {
                    isConditional = false;
                }
            }

        }

        public boolean isPlain() {
            return isPlain;
        }

        public void setPlain(boolean plain) {
            isPlain = plain;
        }

        public long getMemOffset() {
            return memOffset;
        }

        public boolean isCompressed() {
            return isCompressed;
        }

        public boolean isConditional() {
            return isConditional;
        }

        public FSTClazzInfo getLastInfo() {
            return lastInfo;
        }

        public void setLastInfo(FSTClazzInfo lastInfo) {
            this.lastInfo = lastInfo;
        }

        Class calcComponentType(Class c) {
            if (c.isArray()) {
                return calcComponentType(c.getComponentType());
            }
            return c;
        }

        public final Class getType() {
            return type;
        }

        public boolean isThin() {
            return thin;
        }

        public boolean isArray() {
            return isArr;
        }

        public int getArrayDepth() {
            return arrayDim;
        }

        public Class getArrayType() {
            return arrayType;
        }

        public Class[] getPossibleClasses() {
            return possibleClasses;
        }

        public void setPossibleClasses(Class[] possibleClasses) {
            this.possibleClasses = possibleClasses;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
            calcIntegral();
        }

        public void calcIntegral() {
            if (field == null) {
                return;
            }
            if (isArray()) {
                integral = isIntegral(getArrayType());
            } else {
                integral = isIntegral(field.getType());

                Class type = field.getType();
                if ( type == boolean.class ) {
                    integralType = BOOL;
                } else
                if ( type == byte.class ) {
                    integralType = BYTE;
                } else
                if ( type == char.class ) {
                    integralType = CHAR;
                } else
                if ( type == short.class ) {
                    integralType = SHORT;
                } else
                if ( type == int.class ) {
                    integralType = INT;
                } else
                if ( type == long.class ) {
                    integralType = LONG;
                } else
                if ( type == float.class ) {
                    integralType = FLOAT;
                } else
                if ( type == double.class ) {
                    integralType = DOUBLE;
                }
            }
        }

        /**
         * only set if is not an array, but a direct native field type
         * @return
         */
        public int getIntegralType() {
            return integralType;
        }

        public boolean isIntegral(Class type) {
            return type.isPrimitive();
        }

        public boolean isIntegral() {
            return integral;
        }

        public String getDesc() {
            return field != null ? "<" + field.getName() + " of " + field.getDeclaringClass().getSimpleName() + ">" : "<undefined referencee>";
        }

        public String toString() {
            return getDesc();
        }
        public boolean isFlat() {
            return flat;
        }
    }

    public FSTObjectSerializer getSer() {
        if (ser == null) {
            if (clazz == null) {
                return null;
            }
            ser = reg.serializerRegistry.getSerializer(clazz);
            if (ser == null) {
                ser = FSTSerializerRegistry.NULL;
            }
        }
        if (ser == FSTSerializerRegistry.NULL) {
            return null;
        }
        return ser;
    }

    static class FSTCompatibilityInfo {
        Method writeMethod, readMethod;
        ObjectStreamClass objectStreamClass;
        List<FSTFieldInfo> infos;
        Class clazz;
        FSTFieldInfo infoArr[];

        public FSTCompatibilityInfo(List<FSTFieldInfo> inf, Class c) {
            readClazz(c);
            infos = inf;
            clazz = c;
        }

        public List<FSTFieldInfo> getFields() {
            return infos;
        }

        public FSTFieldInfo[] getFieldArray() {
            if (infoArr == null) {
                List<FSTClazzInfo.FSTFieldInfo> fields = getFields();
                infoArr = new FSTClazzInfo.FSTFieldInfo[fields.size()];
                fields.toArray(infoArr);
            }
            return infoArr;
        }

        public Class getClazz() {
            return clazz;
        }

        public boolean needsCompatibleMode() {
            return writeMethod != null || readMethod != null;
        }

        public void readClazz(Class c) {
            writeMethod = FSTUtil.findPrivateMethod(c, "writeObject",
                    new Class<?>[]{ObjectOutputStream.class},
                    Void.TYPE);
            readMethod = FSTUtil.findPrivateMethod(c, "readObject",
                    new Class<?>[]{ObjectInputStream.class},
                    Void.TYPE);
            if (writeMethod != null) {
                writeMethod.setAccessible(true);
            }
            if (readMethod != null) {
                readMethod.setAccessible(true);
            }
        }

        public Method getReadMethod() {
            return readMethod;
        }

        public void setReadMethod(Method readMethod) {
            this.readMethod = readMethod;
        }

        public Method getWriteMethod() {
            return writeMethod;
        }

        public void setWriteMethod(Method writeMethod) {
            this.writeMethod = writeMethod;
        }

        public boolean isAsymmetric() {
            return (getReadMethod() == null && getWriteMethod() != null) || (getWriteMethod() == null && getReadMethod() != null);
        }
    }


}
