package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import de.ruedigermoeller.serialization.annotations.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ruedi
 * Date: 29.11.12
 * Time: 23:52
 * To change this template use File | Settings | File Templates.
 */
@Predict(ExternalizableTest.ExternalTest.class)
public class ExternalizableTest implements Serializable {
    transient Vector test = new Vector();
    ArrayList<ExternalTest> li = new ArrayList<ExternalTest>();
    ExternalTest ext = new ExternalTest(31);

    public ExternalizableTest() {
        for (int i = 0; i < 500; i++)
        {
            test.add(new ExternalTest(i));
            li.add(new ExternalTest(i+500));
        }
    }

    static class ExternalTest implements Externalizable {

        String pok = "HuckaHuckaHuckaHuckaHuckaHucka";
        int i = 34535;

        int j = 0;

        public ExternalTest() {
        }

        ExternalTest(int j) {
            this.j = j;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
            out.writeInt(j);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            i = in.readInt();
            j = in.readInt();
            pok = "HuckaHuckaHuckaHuckaHuckaHucka";
        }
    }

}
