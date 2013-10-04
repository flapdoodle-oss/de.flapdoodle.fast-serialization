package de.ruedigermoeller.heapoff.structs;

import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;

/**
 * An extension of FSTStructAllocaor to ease pools of specific objects. A template (for new allocs) instance is associated
 * with this allocator instance.
 *
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 23.07.13
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class FSTTypedStructAllocator<T extends FSTStruct> extends FSTStructAllocator {

    T template;

    /**
     * @param b
     * @param index
     * @return a new allocated pointer matching struct type stored in b[]
     */
    public static FSTStruct createStructPointer(byte b[], int index) {
        return FSTStructFactory.getInstance().getStructPointerByOffset(b,FSTStruct.bufoff+index).detach();
    }

    /**
     * @param onHeapTemplate
     * @param <T>
     * @return return a byte array based struct instance for given on-heap template. Allocates a new byte[] with each call
     */
    public static <T extends FSTStruct> T toStruct(T onHeapTemplate) {
        return FSTStructFactory.getInstance().toStruct(onHeapTemplate);
    }

    /**
     * @param b
     * @param index
     * @return a pointer matching struct type stored in b[] from the thread local cache
     */
    public static FSTStruct getVolatileStructPointer(byte b[], int index) {
        return (FSTStruct) FSTStructFactory.getInstance().getStructPointerByOffset(b, FSTStruct.bufoff + index);
    }

    /**
     * @param clazz
     * @param <C>
     * @return a newly allocated pointer matching. use baseOn to point it to a meaningful location
     */
    public static <C extends FSTStruct> C newPointer(Class<C> clazz) {
        try {
            return (C)FSTStructFactory.getInstance().getProxyClass(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set to the size of one template struct,
     * so with each strutc allocation an individual byte array is created behind the scenes
     * @param ontpl
     */
    public FSTTypedStructAllocator(T ontpl) {
        super();
        this.template = getFactory().toStruct(ontpl);
        chunkSize = template.getByteSize();
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set to contain 'objectsPerChunk' instances
     * of the given struct template.
     * @param ontpl
     * @param objectsPerChunk
     */
    public FSTTypedStructAllocator(T ontpl, int objectsPerChunk) {
        this(ontpl);
        chunkSize = objectsPerChunk * template.getByteSize();
    }

    /**
     * Create a Structallocator for the given template. Chunksize will be set directly
     * @param template
     * @param chunkSizeBytes
     */
    public FSTTypedStructAllocator(int chunkSizeBytes, T template) {
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

    public T newStruct() {
        if ( template == null )
            throw new RuntimeException("need to specify a template in constructore in order to use this. Use newStruct(template) instead.");
        return newStruct(template);
    }

    public int getTemplateSize() {
        return template.getByteSize();
    }

}
