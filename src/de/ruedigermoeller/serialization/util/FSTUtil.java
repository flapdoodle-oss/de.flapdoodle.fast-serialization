package de.ruedigermoeller.serialization.util;

import sun.reflect.ReflectionFactory;

import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 29.11.12
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
public class FSTUtil {

    static int[] EmptyIntArray = new int[1000];
    static Object[] EmptyObjArray = new Object[1000];
    static ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];

    static void clear(int[] arr) {
        int count = 0;
        final int length = EmptyIntArray.length;
        while( arr.length - count > length) {
            System.arraycopy(EmptyIntArray,0,arr,count, length);
            count += length;
        }
        System.arraycopy(EmptyIntArray,0,arr,count,arr.length-count);
    }

    static void clear(Object[] arr) {
        int count = 0;
        final int length = EmptyObjArray.length;
        while( arr.length - count > length) {
            System.arraycopy(EmptyObjArray,0,arr,count, length);
            count += length;
        }
        System.arraycopy(EmptyObjArray,0,arr,count,arr.length-count);
    }

    public static String getPackage(Class clazz) {
        String s = clazz.getName();
        int i = s.lastIndexOf('[');
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf('.');
        if (i >= 0) {
            return s.substring(0, i);
        }
        return "";
    }

    public static Constructor findConstructorForExternalize(Class clazz) {
        try {
            Constructor c = clazz.getDeclaredConstructor((Class[]) null);
            c.setAccessible(true);
            if ((c.getModifiers() & Modifier.PUBLIC) != 0) {
                return c;
            } else {
                return null;
            }
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Constructor findConstructorForSerializable(Class clazz) {
        Class curCl = clazz;
        while (Serializable.class.isAssignableFrom(curCl)) {
            if ((curCl = curCl.getSuperclass()) == null) {
                return null;
            }
        }
        try {
            Constructor c = curCl.getDeclaredConstructor((Class[]) null);
            int mods = c.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 ||
                    ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 &&
                            !isPackEq(clazz, curCl)))
            {
                return null;
            }
            c = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, c);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    static boolean isPackEq(Class clazz1, Class clazz2) {
        return getPackage(clazz1).equals(getPackage(clazz2));
    }

    public static Method findPrivateMethod(Class clazz, String methName,
                                           Class[] clazzArgs,
                                           Class retClazz)
    {
        try {
            Method m = clazz.getDeclaredMethod(methName, clazzArgs);
            int modif = m.getModifiers();
            if ((m.getReturnType() == retClazz) && ((modif & Modifier.PRIVATE) != 0) && ((modif & Modifier.STATIC) == 0))
            {
                m.setAccessible(true);
                return m;
            }
            return null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static Method findDerivedMethod(Class clazz, String metnam,
                                           Class[] argClzz,
                                           Class retClz)
    {
        Method m = null;
        Class defCl = clazz;
        while (defCl != null) {
            try {
                m = defCl.getDeclaredMethod(metnam, argClzz);
                break;
            } catch (NoSuchMethodException ex) {
                defCl = defCl.getSuperclass();
            }
        }
        if (m == null) {
            return null;
        }
        if (m.getReturnType() != retClz) {
            return null;
        }
        int mods = m.getModifiers();
        if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
            return null;
        } else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            m.setAccessible(true);
            return m;
        } else if ((mods & Modifier.PRIVATE) != 0) {
            m.setAccessible(true);
            if (clazz == defCl) {
                return m;
            }
            return null;
        } else {
            m.setAccessible(true);
            if ( isPackEq(clazz, defCl) ) {
                return m;
            }
            return null;
        }
    }

    public static void printEx(Throwable e) {
        while( e.getCause() != null && e.getCause() != e ) {
            e = e.getCause();
        }
        e.printStackTrace();
    }

    public static boolean isPrimitiveArray(Class c) {
        Class componentType = c.getComponentType();
        if ( componentType == null ) {
            return c.isPrimitive();
        }
        return isPrimitiveArray(c.getComponentType());
    }

}
