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

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * User: Möller
 * Date: 03.11.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class FSTClazzInfoRegistry {

    HashMap mInfos = new HashMap(97);
//    HashMap mInfos = new HashMap(97);
    FSTSerializerRegistry serializerRegistry = new FSTSerializerRegistry();
    boolean ignoreAnnotations = false;
    final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public FSTClazzInfoRegistry() {
    }

    public FSTClazzInfo getCLInfo(Class c) {
        rwLock.readLock().lock();
        FSTClazzInfo res = (FSTClazzInfo) mInfos.get(c);
        if ( res == null ) {
            if ( c == null ) {
                rwLock.readLock().unlock();
                throw new NullPointerException("Class is null");
            }
            res = new FSTClazzInfo(c, this, ignoreAnnotations);
            rwLock.readLock().unlock();
            rwLock.writeLock().lock();
            mInfos.put( c, res );
            rwLock.writeLock().unlock();
        } else {
            rwLock.readLock().unlock();
        }
        return res;
    }

    public boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }
}
