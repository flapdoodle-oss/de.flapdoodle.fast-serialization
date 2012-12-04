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

    FSTObject2ObjectMap mInfos = new FSTObject2ObjectMap(97);
//    HashMap mInfos = new HashMap(97);
    FSTSerializerRegistry serializerRegistry = new FSTSerializerRegistry();
    boolean ignoreAnnotations = false;
//    final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public FSTClazzInfoRegistry() {
    }

    public FSTClazzInfo getCLInfo(Class c) {
//        rwLock.readLock().lock();
        FSTClazzInfo res = (FSTClazzInfo) mInfos.get(c);
        if ( res == null ) {
            if ( c == null ) {
                throw new NullPointerException("Class is null");
            }
            res = new FSTClazzInfo(c, this, ignoreAnnotations);
//            rwLock.readLock().unlock();
//            rwLock.writeLock().lock();
            mInfos.put( c, res );
//            rwLock.writeLock().unlock();
        } else {
//            rwLock.readLock().unlock();
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
