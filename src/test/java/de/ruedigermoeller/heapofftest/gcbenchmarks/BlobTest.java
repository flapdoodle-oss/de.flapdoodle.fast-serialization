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
    static Object randomStuff[] = new Object[300000];

    public static void main( String arg[] ) {
        if ( Runtime.getRuntime().maxMemory() > 2*1024*1024*1024l) { // 'autodetect' testcase with blobs from mem settings
            int blobGB = (int) (Runtime.getRuntime().maxMemory()/(1024*1024*1024l));
            System.out.println("Allocating "+blobGB*32+" 32Mb blobs ... (="+blobGB+"Gb) ");
            for (int i = 0; i < blobGB*32; i++) {
                blobs.add(new byte[32*1024*1024]);
            }
            System.gc(); // force VM to adapt ..
        }
        // create eden collected tmps with a medium promotion rate (promotion rate can be adjusted by size of randomStuff[])
        while( true ) {
            randomStuff[((int) (Math.random() * randomStuff.length))] = new Rectangle();
        }

    }

}
