package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.annotations.*;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 22:01
 * To change this template use File | Settings | File Templates.
 */
@Predict(PrimitiveArrays.Dim.class)
public class PrimitiveArrays implements Serializable {
    @Flat byte b[] = {1,2,3,4,5,6,7,8,-1,-2,127,1,2,3,4,5,6,7,8,-1,-2,127,1,2,3,4,5,6,7,8,-1,-2,127,1,2,3,4,5,6,7,8,-1,-2,127,1,2,3,-4,5,6,7,8,-1,-2,127};
    // aim for diff compression
    @Compress @Flat int i[] = {9991,9992,9993,9994,9995,9996,9987,9878,9878,-2,7127,9871,9872,8435,9784,9785,9786,9877,9878,9877,-2,7127,7891,9876,9873,9874,8798,7896,9877,9878,9784,9785,9786,9877,9878,9877,-2,7127,7891,9876,9873,9874,8798,7896,9877,9878,9784,9785,9786,9877,9878,9877,-2,7127,7891,9876,9873,9874,8798,7896,9877,9878,};
    // aim for offset compression
    @Compress @Flat int ia2[] = {1000,9992,1000,9994,1000,9992,1000,9994,1000,9992,1000,5994,1000,1992,1000,1994,1000,12992,1000,4994,1000,2992,1000,3994,1000,7992,1000,5994,1000,7992,1000,9344,};
    // aim for thin compression
    @Compress @Flat int ia3[] = {0,-12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,0,0,0,0,345645665,0,0,0,0,0,-1,0,127,0,66662,3,0,0,0,0,0,0,-2,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,-1,0,0,0,0,0,0,0,0,-2,127};

    @Thin @Flat int ia1[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,0,0,0,0,345645665,0,0,0,0,0,-1,-2,127,0,66662,3,0,0,0,0,0,-1,-2,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,-1,0,0,0,0,0,0,0,0,-2,127};
    @Flat char c[] = {1,2,3,4,333,7777,7,8,0,6666,127};

    @Flat Object o0 = new byte[] {1,2,3,4,5,6,7,8,-1,-2,127};
    @Flat @Compress Object o1 = new int[] {13453,234534,334,4345,5456,645,74,84,-1,-2,127,1567,2678,3678678,4456465,5456456,6456456,4564657,5675678,-567561,-256756756,12755,155555,2666666,36666666,46666,5,6,7,8,-1,-256756,127666,1666,2,3,4,5,6,7,8,-1,-2,127,1567565,25675656,3567567,4678678,5678678,6676,767867,86777,-1,-2,127678,1665,25567,35675,455544,54444,645664,7456,8456,-14,-2,127,888888,-8888888,888888,-8888888,888888,-8888888,888888,-8888888,888888,-8888888,888888,-8888888};
    @Flat Object o2 = new char[] {1,2,3,4,333,7777,7,8,0,6666,127};

    @Flat Object i1 = new Integer[] {1,2,3,4,5,6,7,8,-1,-2,127};

    @Flat int iii[][][] = new int[][][] { { {1,2,3}, {4,5,6} }, { {7,8,9}, {10,11,12} } };
    @Flat Object oiii = new int[][][] { { {1,2,3}, {4,5,6} }, { {7,8,9}, {10,11,12} } };

    Dim dim[][][] = new Dim[][][] {{{new Dim(11,10)},{new Dim(9,10),new Dim(1666661,11)}}};
    Object dimo[][][] = new Object[][][] {{{new Dim(11,10)},{new Dim(10,8),new Dim(6,11)}}};
    Object dimonotype0 = new Object[][][] {{{new Date(),new Dim(10,777)},{new Dim(1666661,11),new Dim(6,11)}}};
    Object dimonotype1 = new Object[] { new Date(), new Object[]{ new Dim[]{ new Dim(1666661,11)},new Object[]{new Dim(66,10),new Dim(1666661,11)}}};

    @Flat @Thin Object nul[] = { null, null, null, null,null, null,null, null, "pok", null, null, null, null, null,null, null, null, null, null,null, null, null, null, null,null, null, null, null, null,null, null, null, null, null };

    // to avoid measurement of stream init performance
    public static PrimitiveArrays[] createPrimArray() {
        PrimitiveArrays res[] = new PrimitiveArrays[20];
        for (int i = 0; i < res.length; i++) {
            res[i] = new PrimitiveArrays();
        }
        return res;
    }

    @EqualnessIsBinary
    static class Dim implements Serializable {
        int x;
        int y;
        long z;

        // kryo
        public Dim() {
        }

        Dim(int x, int y) {
            this.x = x;
            this.y = y;
            z = x*y*x*y;
        }

        public int hashCode() {
            return x+y;
        }
        public boolean equals( Object other ) {
            if ( other instanceof Dim) {
                return ((Dim) other).x == x && ((Dim) other).y == y;
            }
            return false;
        }
    }

}
