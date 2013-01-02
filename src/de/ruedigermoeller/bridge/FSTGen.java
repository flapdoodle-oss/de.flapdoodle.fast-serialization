package de.ruedigermoeller.bridge;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTCrossLanguageSerializer;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 26.12.12
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */
public class FSTGen {
    protected FSTBridge gen;

    public FSTGen(FSTBridge gen) {
        this.gen = gen;
    }

    protected Class mapDeclarationType(Class type, FSTClazzInfo info) {
        if ( Enum.class.isAssignableFrom(type) ) {
            return String.class;
        }
        if ( gen.isRegistered(type) || isSystemClass(type) ) {
            return type;
        }
        if (info.getSer() instanceof FSTCrossLanguageSerializer) {
            return ((FSTCrossLanguageSerializer) info.getSer()).getCrossLangLayout();
        }
        if (isCrossPlatformCollection(type)) {
            return Object[].class;
        }
        throw new RuntimeException("unmappable class:"+type.getName());
    }

    public boolean isSystemClass(Class clz) {
        return clz.isPrimitive() || clz == Object.class || clz == Object[].class || clz == Date.class ||
               clz == Boolean.class || clz == Byte.class || clz == Character.class || clz == Short.class || clz == Integer.class ||
               clz == Long.class || clz == Float.class || clz == Double.class || clz == String.class;
    }

    protected String getBridgeClassName(Class clazz0) {
        return getBridgeClassName(clazz0,true);
    }

    protected String getBridgeClassName(Class clazz0, boolean replaceByLayout ) {
        if (clazz0 == String.class || Enum.class.isAssignableFrom(clazz0)) {
            return "String";
        }
        Class clazz = clazz0;
        if ( clazz.isArray() && clazz.getComponentType().isPrimitive() ) {
            return clazz.getComponentType().getSimpleName()+"[]";
        }
        if ( clazz.isArray() ) {
            clazz = clazz.getComponentType();
            if ( clazz.isArray() ) {
                throw new RuntimeException("multi dimensional arrays are not supported "+clazz0);
            }
        }

        if ( replaceByLayout && gen.getMappedClasses().containsKey(clazz) ) {
            Class prevClazz = clazz;
            clazz = gen.getMappedClasses().get(clazz);
            if ( clazz != prevClazz ) {
                return getBridgeClassName(clazz);
            }
        }
        if ( clazz.isPrimitive() ) {
            return clazz.getSimpleName();
        }
        if ( isSystemClass(clazz) ) {
            if ( clazz0.isArray() ) {
                return clazz.getName()+"[]";
            }
            return clazz.getName();
        }
        if (!gen.isRegistered(clazz)) {
            FSTClazzInfo info = gen.getCLInfo(clazz);
            if ( info.getSer() instanceof FSTCrossLanguageSerializer) {
                Class crossLangLayout = ((FSTCrossLanguageSerializer) info.getSer()).getCrossLangLayout();
//                gen.addMappedClass(crossLangLayout, clazz);
                return getBridgeClassName(crossLangLayout);
            } else
            if (isCrossPlatformCollection(clazz)) {
                return "Object[]";
            } else
            {
                throw new RuntimeException("reference to unregistered or unsupported class:"+clazz.getName());
            }
        }
        if ( clazz0.isArray() ) {
            return clazz.getSimpleName()+"_FST[]";
        }
        return clazz.getSimpleName()+"_FST";
    }

    private boolean isCrossPlatformCollection(Class clazz) {
        return clazz == List.class || clazz== Map.class || clazz == Dictionary.class || clazz == Collection.class;
    }

}
