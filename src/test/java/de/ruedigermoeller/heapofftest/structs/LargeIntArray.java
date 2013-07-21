package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 17.07.13
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class LargeIntArray extends FSTStruct {
    protected int largeArray[];

    public LargeIntArray() {
        largeArray = new int[2000000];
        for (int i = 0; i < largeArray.length; i++) {
            largeArray[i] = i;
        }
    }

    public int calcSumStraight() {
        int res = 0;
        final int max = largeArrayLen();
        for (int i = 0; i < max; i++) {
            res += largeArray(i);
        }
        return res;
    }

    transient protected FSTStruct pointer;
    public int calcSumPointered() {
        if ( pointer == null )
            pointer = new FSTStruct();
        largeArrayPointer(pointer);
        int res = 0;
        final int max = largeArrayLen();
        for (int i = 0; i < max; i++) {
            res += pointer.getInt();
            pointer.next(4);
        }
        return res;
    }

    public void largeArrayPointer(FSTStruct struct) {
        // generated
    }

    public int largeArray(int index) {
        return largeArray[index];
    }

    public int largeArrayLen() {
        return largeArray.length;
    }

}
