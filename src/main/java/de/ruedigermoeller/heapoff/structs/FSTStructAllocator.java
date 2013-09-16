package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.serialization.util.FSTUtil;

/**
 * This class is the main entry point to create structs, arrays of structs and maps of structs.
 *
 * One needs to provide a (on heap allocated) Template instance, which then determines the structure and
 * initial values of structs created when calling newStruct() methods.
 *
 * By default a new byte array is created on the heap for each struct instance created.
 * Since GC effort scales with the number of objects, one can advise the Allocator to use larger chunks of
 * byte[] and allocate several struct instances into them. The byte array will be freed, if all structs
 * contained in the byte[] chunk are not referenced by your code anymore.
 *
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 23.07.13
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class FSTStructAllocator<T extends FSTStruct> {

    T template;
    int chunkSize;
    byte chunk[];
    int chunkIndex;

    /**
     * Create a Structallocator with given chunk size in bytes. If allocated structs are larger than the given size, a new bytearray is
     * created for the allocation.
     * @param chunkSizeBytes
     */
    public FSTStructAllocator(int chunkSizeBytes) {
        this.chunkSize = chunkSizeBytes;
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set to the size of one template struct,
     * so with each strutc allocation an individual byte array is created behind the scenes
     * @param ontpl
     */
    public FSTStructAllocator(T ontpl) {
        this.template = getFactory().toStruct(ontpl);
        chunkSize = template.getByteSize();
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set to contain 'objectsPerChunk' instances
     * of the given struct template.
     * @param ontpl
     * @param objectsPerChunk
     */
    public FSTStructAllocator(T ontpl, int objectsPerChunk) {
        this(ontpl);
        chunkSize = objectsPerChunk * template.getByteSize();
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set directly
     * @param template
     * @param chunkSizeBytes
     */
    public FSTStructAllocator(int chunkSizeBytes, T template) {
        this(template);
        chunkSize = chunkSizeBytes;
    }

    /**
     * create a new struct array of same type as template
     * @param size
     * @return
     */
    public StructArray<T> newArray(int size) {
        if ( template == null )
            throw new RuntimeException("need to specify a template in constructore in order to use this.");
        return newArray(size,template);
    }

    /**
     * create a new struct array of same type as template
     * @param size
     * @return
     */
    public <X extends FSTStruct> StructArray<X> newArray(int size, X templ) {
        StructArray<X> aTemplate = new StructArray<X>(size, templ);
        int siz = getFactory().calcStructSize(aTemplate);
        try {
            if ( siz < chunkSize )
                return newStruct(aTemplate);
            else {
                return getFactory().toStruct(aTemplate);
            }
        } catch (Throwable e) {
            System.out.println("tried to allocate "+siz+" bytes. StructArray of "+size+" "+templ.getClass().getName());
            throw new RuntimeException(e);
        }
    }

    /**
     * create a fixed size struct hashmap. Note it should be of fixed types for keys and values, as
     * the space for those is allocated directly. Additionally keys and values are stored 'in-place' without references.
     * @param size
     * @param keyTemplate
     * @param <K>
     * @return
     */
    public <K extends FSTStruct> StructMap<K,T> newMap(int size, K keyTemplate) {
        if ( template == null )
            throw new RuntimeException("need to specify a template in constructore in order to use this.");
        return newStruct( new StructMap<K, T>(keyTemplate,template,size) );
    }

    /**
     * allocate a Struct instance from an arbitrary template. This is provided to avoid having to construct tons
     * of "allocator" instances.
     * @param aTemplate
     * @param <S>
     * @return
     */
    public <S extends FSTStruct> S newStruct(S aTemplate) {
        aTemplate = getFactory().toStruct(aTemplate);
        if (aTemplate.getByteSize()>=chunkSize)
            return (S)aTemplate.createCopy();
        int byteSize = aTemplate.getByteSize();
        synchronized (this) {
            if (chunk == null || chunkIndex+ byteSize > chunk.length) {
                chunk = new byte[chunkSize];
                chunkIndex = 0;
            }
            FSTStruct.unsafe.copyMemory(aTemplate.___bytes, aTemplate.___offset, chunk, FSTStruct.bufoff + chunkIndex, byteSize);
            S res = (S) getFactory().createStructWrapper(chunk, chunkIndex );
            chunkIndex+=byteSize;
            return res;
        }
    }

    public T newStruct() {
        if ( template == null )
            throw new RuntimeException("need to specify a template in constructore in order to use this. Use newStruct(template) instead.");
        return newStruct(template);
    }

    protected FSTStructFactory getFactory() {
        return FSTStructFactory.getInstance();
    }

    public int getTemplateSize() {
        return template.getByteSize();
    }

    public T newPointer(Class<T> dataPacketClass) {
        try {
            return (T)getFactory().getProxyClass(dataPacketClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
