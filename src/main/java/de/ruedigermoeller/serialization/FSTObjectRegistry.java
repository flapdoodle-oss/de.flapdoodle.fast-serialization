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

import de.ruedigermoeller.serialization.util.*;

import java.util.IdentityHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 11.11.12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public final class FSTObjectRegistry {

    boolean disabled = false;
    IdentityHashMap<Object,Integer> objects = new IdentityHashMap<Object,Integer>(97); // object => id
    FSTInt2ObjectMap idToObject = new FSTInt2ObjectMap(97);
    FSTObject2IntMap equalsMap = new FSTObject2IntMap(97,true); // object => handle

    FSTConfiguration conf;
    FSTClazzInfoRegistry reg;

    private static final boolean DUMP = false;
    private int lastRegisteredReadPos; // last registered streampos wg. double register

    public FSTObjectRegistry(FSTConfiguration conf) {
        this.conf = conf;
        disabled = !conf.isShareReferences();
        reg = conf.getCLInfoRegistry();
    }

    public void clear() {
        objects.clear();
        idToObject.clear();
        equalsMap.clear();
        disabled = !conf.isShareReferences();
    }

    public void clearForRead() {
        lastRegisteredReadPos = 0;
        idToObject.clear();
        disabled = !conf.isShareReferences();
    }

    public void clearForWrite() {
        if ( objects.size() > 0 ) {
            objects.clear();
        }
        equalsMap.clear();
        disabled = !conf.isShareReferences();
    }

    public void replace(Object old, Object replaced, int streamPos) {
        idToObject.put(streamPos, replaced);
        if ( DUMP  )
            System.out.println("REPLACE "+streamPos+" old "+old.getClass().getName()+" new:"+replaced.getClass().getName());
    }

    public void registerObjectForRead(Object o, int streamPosition) {
        if (disabled /*|| streamPosition <= lastRegisteredReadPos*/) {
            return;
        }
        if ( DUMP ) {
            System.out.println("for read "+o.getClass()+" "+streamPosition);
        }
        lastRegisteredReadPos = streamPosition;
        idToObject.put(streamPosition,o);
    }

    /**
    * add an object to the register, return handle if already present
    *
    * @param o
    * @param streamPosition
    * @return 0 if added, handle if already present
    */
    public int registerObject(Object o, boolean dontCheckEqual, int streamPosition, FSTClazzInfo clzInfo, int reUseType[]) {
        if (disabled) {
            return Integer.MIN_VALUE;
        }
        final Class clazz = o.getClass();
        if ( clzInfo == null ) { // array oder enum oder primitive
            clzInfo = reg.getCLInfo(clazz);
        } else if ( clzInfo.isFlat() ) {
            return Integer.MIN_VALUE;
        }
        Integer handle = objects.get(o);
        if ( handle != null ) {
//            if ( idToObject.get(handle) == null ) { // (*) (can get improved)
//                idToObject.add(handle, o);
//            }
            reUseType[0] = 0;
            return handle;
        }
        boolean reUseEquals = !dontCheckEqual && clzInfo != null && !(clazz.isArray() || clazz.isPrimitive());
        if ( reUseEquals ) {
            reUseEquals = reUseEquals && (clzInfo.isEqualIsIdentity() || clzInfo.isEqualIsBinary());
            if (  reUseEquals ) {
                int integer = equalsMap.get(o);
                if ( integer != Integer.MIN_VALUE ) {
                    reUseType[0] = 1;
                    return integer;
                }
            }
        }
        objects.put(o, FSTConfiguration.getInt(streamPosition));
        //idToObject.add(streamPosition, o); only required for equalsness, moved to (*)
        if ( DUMP )
            System.out.println("REGISTER "+o.getClass()+" pos:"+streamPosition+" handle:"+streamPosition+" id:"+System.identityHashCode(o));
        if ( reUseEquals ) {
            equalsMap.put(o,streamPosition);
        }
        return Integer.MIN_VALUE;
    }

    boolean isReuseEqualsByIdentity(Class aClass, FSTClazzInfo serializationInfo) {
        return serializationInfo.isEqualIsIdentity();
    }

    boolean isReuseByCopy(Class aClass, FSTClazzInfo serializationInfo) {
        return serializationInfo.isEqualIsBinary();
    }

    public Object getRegisteredObject(int handle) {
        if (disabled) {
            return null;
        }
        return idToObject.get(FSTConfiguration.getInt(handle));
    }

    public int getObjectSize() {
        return objects.size();
    }

}