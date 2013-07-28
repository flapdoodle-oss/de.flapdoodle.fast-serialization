package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.Align;
import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.NoAssist;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 17.07.13
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class LargeIntArray extends FSTStruct {
    @Align(8)
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
    @NoAssist
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

    public static void main(String arg[] ) {
        System.gc();
        byte[][] bytes = new byte[10*512*1024][];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = new byte[1024];
        }
        System.gc();
        System.gc();
        System.gc();
        JPanel tmp = null;
        // trigger gc's
        while( bytes[0][0] == 0 ) {
            for (int i = 0; i < 1000; i++) {
                tmp = new JPanel();
                bytes[1][10] = (byte) (tmp.isOpaque() ? 1 : i); // mutate
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if ( bytes[0][0] == 0 ) { // prevent escape analysis in case
            System.out.println("yes "+tmp);
        }
    }

}
