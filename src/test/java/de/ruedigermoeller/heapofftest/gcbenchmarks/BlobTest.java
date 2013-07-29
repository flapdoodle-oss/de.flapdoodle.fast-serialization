package de.ruedigermoeller.heapofftest.gcbenchmarks;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 28.07.13
 * Time: 23:26
 * To change this template use File | Settings | File Templates.
 */
public class BlobTest {

    static ArrayList blobs = new ArrayList();
    static ArrayList normal = new ArrayList();
    static Object randomStuff[] = new Object[100000];

    public static void main( String arg[] ) {
        int blobGB = 5;
        for (int i = 0; i < blobGB; i++) {
//            blobs.add(new byte[1024*1024*1024]);
        }
        int numNorm = 1000*1000*50;
        for (int i = 0; i < numNorm; i++) {
            normal.add(new Rectangle());
        }
        while( true ) {
            randomStuff[((int) (Math.random()*Math.random() * randomStuff.length))] = new Rectangle();
        }

    }

}
