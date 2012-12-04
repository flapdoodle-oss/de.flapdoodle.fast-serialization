package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.annotations.EqualnessIsIdentity;
import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.annotations.Predict;

import javax.swing.*;
import javax.swing.text.html.StyleSheet;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 03:13
 * To change this template use File | Settings | File Templates.
 */
@Predict(Primitives.SampleEnum.class)
public class Primitives extends PrivatePrimitive implements Serializable {


    public enum SampleEnum {
        None("","None",0),
                Complete("C","Complete",1),
                Complete_GiveUp_Allowed("D","Complete Give-Up Allowed",2),
                Complete_Position_Transaction_Allowed("E","Complete Position Transaction Allowed",3),
                Designated("G","Designated",4),
                Predesignated("P","Predesignated",5),
                Predesignated_GiveUp_Allowed("Q","Predesignated Give-Up Allowed",6),
                Predesignated_Position_Transaction_Allowed("R","Predesignated Position Transaction Allowed",7),
                GiveUp_Allowed("X","Give-Up Allowed",8),
                Position_Transaction_Allowed("Y","Position Transaction Allowed",9);

        private String value;
        private String stringRepresentation;
        private int nativeEnumValue;

        private SampleEnum(String value, String stringRepresentation, int nativeEnumValue)
        {
            this.value=value;
            this.stringRepresentation = stringRepresentation;
            this.nativeEnumValue = nativeEnumValue;
        }
    }

    private char w = 234, x = 33344;
    private byte y = -34, z = 126;
    short sh0 = 127;

    private int gg = -122;
    private int zz = 99999;
    private int ii = -23424;
    private int jj = 0;
    private int kk = Integer.MIN_VALUE;
    private int hh = Integer.MAX_VALUE;

    private long lll = 123;
    private long mmm = 99999;

    private double dq = 300.0;
    private float t = 300.0f;

    private boolean a0 = true;
    private boolean a1 = false;
    private boolean a2 = false;
    private boolean a3 = true;

    Integer i0 = 1, i1 = 2, i3 = 23894, i4 = 238475638;
    Double  d1 = 2334234.0;
    Boolean bol1 = Boolean.TRUE;
    Boolean bol2 = new Boolean(false);

    @Flat Date date = new Date(1);
    @Flat Date date1 = new Date();

    SampleEnum en1 = SampleEnum.Predesignated_GiveUp_Allowed;
    @Flat EnumSet<SampleEnum> enSet = EnumSet.of(SampleEnum.Predesignated,SampleEnum.Complete);

    @Flat String st;
    @Flat String st1;
    @Flat String st2;
    @Flat String hidden;
    @Flat String st3;
    @Flat String st4;
    @Flat String st5;
    @Flat String st6;
    @Flat String st7;

    StyleSheet on = null;
    URL on1 = null;
    File on2 = null;

    public Primitives() {
    }

    public Primitives(int num) {
        st = "String"+num;
        st1 = "String1"+num;
        st2 = st+"1"+num;
        hidden = "Visible"+num;
        st3 = "Visible blablabla Visible blablabla Visible blablabla "+num;
        st4 = "Etwas Deutsch läuft.. "+num;
        st5 = st+"1"+num;
        st6 = "Some english, text; fragment. "+num;
        st7 = st6+" paokasd 1"+num;
    }

    // to avoid measurement of pure stream init performance
    public static Primitives[] createPrimArray() {
        Primitives res[] = new Primitives[50];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Primitives(i);
        }
        return res;
    }
}

class PrivatePrimitive {
    private String hidden = "Hidden";
}

