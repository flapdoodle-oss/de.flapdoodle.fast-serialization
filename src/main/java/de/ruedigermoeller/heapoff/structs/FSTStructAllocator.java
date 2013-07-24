package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.serialization.util.FSTUtil;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 23.07.13
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructAllocator<T extends FSTStruct> {

    T template;
    byte chunk[];
    int chunkIndex;

    int objPerChunk =1;

    public FSTStructAllocator(T template) {
        this.template = getFactory().toStruct(template);
    }

    public FSTStructAllocator(T template, int objectsPerChunk) {
        this(template);
        objPerChunk = objectsPerChunk;
    }

    public StructArray<T> newArray(int size) {
        return getFactory().toStruct(new StructArray<T>(size,template));
    }

    public <K extends FSTStruct> StructMap<K,T> newMap(int size, K keyTemplate) {
        return getFactory().toStruct( new StructMap<K, T>(keyTemplate,template,size) );
    }

    public T newStruct() {
        if (objPerChunk==1)
            return (T)template.createCopy();
        int byteSize = template.getByteSize();
        synchronized (this) {
            if (chunk == null || chunkIndex+ byteSize > chunk.length) {
                chunk = new byte[byteSize *objPerChunk];
                chunkIndex = 0;
            }
            FSTStruct.unsafe.copyMemory(template.___bytes, template.___offset, chunk, FSTUtil.bufoff + chunkIndex * byteSize, byteSize);
            T res = (T) getFactory().createStructWrapper(chunk, chunkIndex * byteSize);
            chunkIndex++;
            return res;
        }
    }

    protected FSTStructFactory getFactory() {
        return FSTStructFactory.getInstance();
    }

}
