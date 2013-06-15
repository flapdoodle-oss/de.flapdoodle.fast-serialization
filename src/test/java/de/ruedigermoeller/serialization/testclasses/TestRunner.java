package de.ruedigermoeller.serialization.testclasses;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.software.util.DeepEquals;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.basicstuff.*;
import de.ruedigermoeller.serialization.testclasses.enterprise.*;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.*;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 12.11.12
 * Time: 02:43
 * To change this template use File | Settings | File Templates.
 */
public class TestRunner {

    static {
        System.setProperty("fst.unsafe","true");
    }
    static int WarmUP = 10000;
    static int Run = WarmUP+1;
    abstract class SerTest {
        String title;
        ByteArrayOutputStream bout;
        ByteArrayInputStream bin;
        long timWrite, timRead;
        int length;
        Object testObject;
        Object resObject;

        public SerTest(String title) {
            this.title = title;
        }

        public void run( Object toWrite ) {
            testObject = toWrite;
            System.out.println("==================== Run Test "+title);
            System.out.println("warmup ..");
            for ( int i = 0; i < WarmUP; i++ ) {
                try {
                    runOnce(toWrite);
                } catch (Throwable th) {
                    break;
                }
            }

            System.gc();
            System.out.println("write ..");
            long startTim = 0;
            try {
                startTim = System.currentTimeMillis();
                for ( int i = 0; i < Run; i++ ) {
                    runWriteTest(toWrite);
                }
                timWrite = System.currentTimeMillis()-startTim;
            } catch (Exception e) {
                System.out.println(""+title+" FAILURE in write "+e.getMessage());
            }
            length = bout.toByteArray().length;

            System.gc();
            try {
                System.out.println("read ..");
                startTim = System.currentTimeMillis();
                for ( int i = 0; i < Run; i++ ) {
                    runReadTest(toWrite.getClass());
                }
                timRead = System.currentTimeMillis()-startTim;
            } catch (Exception e) {
                timRead = 0;
                e.printStackTrace();
                System.out.println(""+title+" FAILURE in read "+e.getMessage());
            }
            if ( resObject != null ) {
                if ( ! DeepEquals.deepEquals(resObject, toWrite) ) {
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! EqualTest failed !!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    length = 0;
                    timRead = 0;
                    timWrite = 0;
                } else {
                    System.out.println("+++++++++++++++++ EqualTest succeed ++++++++++++++++++++");
                }
            }
        }

        public void runOnce( Object toWrite ) {
            runWriteTest(toWrite);
            runReadTest(toWrite.getClass());
        }

        public void dumpRes() {
            System.out.println(title+" : Size:"+length+",  TimeRead: "+(timRead*1000/Run)+" microsec,   TimeWrite: "+(timWrite*1000/Run)+" microsec");
        }

        public void runReadTest(Class cl) {
            bin = new ByteArrayInputStream(bout.toByteArray());
            readTest(bin, cl);
        }

        public void runWriteTest( Object toWrite ) {
            bout = new ByteArrayOutputStream(10000);
            writeTest(toWrite, bout, toWrite.getClass());
        }

        protected abstract void readTest(ByteArrayInputStream bin, Class cl);

        protected abstract void writeTest(Object toWrite, OutputStream bout, Class aClass);

        public String getColor() {
            return "#a04040";
        }
    }

    static Kryo kryo = new Kryo();
    SerTest kryotest = new SerTest("kryo") {

        public String getColor() {
            return "#A0A0A0";
        }

        Input out;
        @Override
        protected void readTest(ByteArrayInputStream bin, Class cl) {
            if ( out ==null )
                out = new Input(bin);
            else
                out.setInputStream(bin);
            Object res = kryo.readObject(out,cl);
            if ( res instanceof Swing && WarmUP == 0) {
                ((Swing) res).showInFrame("KRYO COPY");
            }
            out.close();
            resObject = res;
        }
        Output output;
        @Override
        protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
            if ( output == null )
                output = new Output(bout);
            else
                output.setOutputStream(bout);
            kryo.writeObject(output, toWrite);
            output.close();
        }
    };

    Object swingDef, minFSTSwing;

    SerTest defser = new SerTest("Java built in") {
        public String getColor() {
            return "#c0c0c0";
        }

        @Override
        protected void readTest(ByteArrayInputStream bin, Class cl) {
            try {
                ObjectInputStream out = new ObjectInputStream(bin);
                Object res = out.readObject();
                if ( res instanceof Swing && WarmUP == 0) {
                    swingDef = res;
                    ((Swing) res).showInFrame("DEF SER COPY");
                }
                out.close();
                resObject = res;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        @Override
        protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(bout);
                out.writeObject(toWrite);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    static FSTConfiguration optconf;
    static {
        optconf = FSTConfiguration.createDefaultConfiguration();
        optconf.registerAsEqualnessReplaceable(String.class);
        optconf.registerAsEqualnessReplaceable(BigDecimal.class);
    }

    SerTest optFST = new SerTest("FST optimized conf") {
        FSTObjectInput.ConditionalCallback conditionalCallback = new FSTObjectInput.ConditionalCallback() {
            @Override
            public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                return false;
            }
        };

        public String getColor() {
            return "#9090ff";
        }

        @Override
        protected void readTest(ByteArrayInputStream bin, Class cl) {
            FSTObjectInput in = null;
            try {
                in = new FSTObjectInput(bin, optconf);
                in.setConditionalCallback(conditionalCallback);
                Object res = in.readObject(cl);
                if ( res instanceof Swing && WarmUP == 0) {
                    ((Swing) res).showInFrame("FST Copy");
                }
                in.close();
                resObject = res;
//                out.clnames.differencesTo(in.clnames);
            } catch (Throwable e) {
//                out.clnames.differencesTo(in.clnames);
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
        FSTObjectOutput out;
        @Override
        protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
            out = new FSTObjectOutput(bout, optconf);
            try {
                out.writeObject(toWrite, aClass);
                out.flush();
                out.close();
            } catch (Throwable e) {
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
        public void run( Object toWrite ) {
            super.run(toWrite);
            System.out.println(out.getObjectMap().getObjectSize());
        }
    };

    static FSTConfiguration crossconf;
    static {
        crossconf = FSTConfiguration.createCrossLanguageConfiguration();
    }

    SerTest crossFST = new SerTest("FST cross language conf") {
        FSTObjectInput.ConditionalCallback conditionalCallback = new FSTObjectInput.ConditionalCallback() {
            @Override
            public boolean shouldSkip(Object halfDecoded, int streamPosition, Field field) {
                return false;
            }
        };

        public String getColor() {
            return "#909090";
        }

        @Override
        protected void readTest(ByteArrayInputStream bin, Class cl) {
            FSTObjectInput in = null;
            try {
                in = new FSTObjectInput(bin, crossconf);
                in.setConditionalCallback(conditionalCallback);
                Object res = in.readObject(cl);
                if ( res instanceof Swing && WarmUP == 0) {
                    ((Swing) res).showInFrame("FST Copy");
                }
                in.close();
                resObject = res;
//                out.clnames.differencesTo(in.clnames);
            } catch (Throwable e) {
//                out.clnames.differencesTo(in.clnames);
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
        FSTObjectOutput out;
        @Override
        protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
            out = new FSTObjectOutput(bout, crossconf);
            try {
                out.writeObject(toWrite, aClass);
                out.flush();
                out.close();
            } catch (Throwable e) {
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
        public void run( Object toWrite ) {
            super.run(toWrite);
            System.out.println(out.getObjectMap().getObjectSize());
        }
    };


    SerTest defFST = new FSTTest("FST default conf",true);
    SerTest defFSTNoUns = new FSTTest("FST default no unsafe ",false);

    class FSTTest extends SerTest {

        boolean uns;
        FSTConfiguration defconf;
        {
            defconf = FSTConfiguration.createDefaultConfiguration();
            // dont do this, just for testing
            defconf.getCLInfoRegistry().setIgnoreAnnotations(true);
        }

        FSTTest(String desc,boolean uns) {
            super(desc);
            this.uns = uns;
        }

        @Override
        public void run( Object toWrite ) {
            Unsafe tmp = FSTUtil.unsafe;
            if ( ! uns ) {
                FSTUtil.unsafe = null;
            }
            super.run(toWrite);
            FSTUtil.unsafe = tmp;
        }

        public String getColor() {
            return "#4040a0";
        }

        @Override
        protected void readTest(ByteArrayInputStream bin, Class cl) {
            try {
                FSTObjectInput in = defconf.getObjectInput(bin);
                Object res = in.readObject(cl);
                if ( res instanceof Swing && WarmUP == 0) {
                    ((Swing) res).showInFrame("FST Copy");
                }
                bin.close();
                resObject = res;
//                out.clnames.differencesTo(in.clnames);
            } catch (Throwable e) {
//                out.clnames.differencesTo(in.clnames);
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
        @Override
        protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
            FSTObjectOutput out = defconf.getObjectOutput(bout);
            try {
                out.writeObject(toWrite, aClass);
                out.flush();
                bout.close();
            } catch (Throwable e) {
                FSTUtil.printEx(e);
                throw new RuntimeException(e);
            }
        }
    };


    Class testClass;
    public SerTest[] runAll( Object toSer ) {
        testClass = toSer.getClass();
        if ( toSer instanceof Swing) {
            ((Swing) toSer).showInFrame("Original");
            ((Swing) toSer).init();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("************** Running all with "+toSer.getClass().getName()+" **********************************");
//        SerTest tests[] = { defFST, kryotest, defser, optFST, minFST, crossFST};
//        SerTest tests[] = { optFST, defFST, kryotest, minFST, crossFST};
//        SerTest tests[] = { defFST, optFST,  kryotest};
        SerTest tests[] = { defFST, defFSTNoUns, kryotest, defser };
//        SerTest tests[] = { kryotest};
//        SerTest tests[] = { kryotest, defFST};
//        SerTest tests[] = { defFST};
//        SerTest tests[] = { defser, minFST };
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.run(toSer);
        }
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            test.dumpRes();
        }

        charter.heading("Test Class: "+testClass.getSimpleName());
        Object testIns = null;
        try {
            if ( testClass.isArray() ) {
                testIns = testClass.getComponentType().newInstance();
            } else
                testIns = testClass.newInstance();
            if ( testIns instanceof HasDescription ) {
                charter.text(((HasDescription) testIns).getDescription());
                charter.text("");
            }
        } catch (Exception e) {
        }

        charter.openChart("Read Time (micros)");
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, (int)(test.timRead*1000/Run), 2, test.getColor());
        }
        charter.closeChart();

        charter.openChart("Write Time (micros)");
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, (int)(test.timWrite*1000/Run), 2, test.getColor());
        }
        charter.closeChart();

        charter.openChart("Size (byte)");
        for (int i = 0; i < tests.length; i++) {
            SerTest test = tests[i];
            charter.chartBar(test.title, test.bout.size(), 300, test.getColor());
        }
        charter.closeChart();

        return tests;
    }
    HtmlCharter charter = new HtmlCharter("./result.html");


    public static void main( String[] arg ) {
        System.setProperty("fst.unsafe","true");

        TestRunner runner = new TestRunner();


        runner.charter.openDoc();
        runner.charter.text("<i>intel i7 3770K 3,5 ghz, 4 core, 8 threads</i>");
        runner.charter.text("<i>"+System.getProperty("java.runtime.version")+","+System.getProperty("java.vm.name")+","+System.getProperty("os.name")+"</i>");

        WarmUP = 20000; Run = WarmUP+1;
        runner.runAll(FrequentPrimitives.getArray(200));
        runner.runAll(new FrequentCollections());
        runner.runAll(new LargeNativeArrays());
        runner.runAll(new Primitives(0).createPrimArray());
        runner.runAll(new PrimitiveArrays().createPrimArray());
        runner.runAll(new CommonCollections());
        runner.runAll(Trader.generateTrader(101, true));
        runner.runAll(ManyClasses.getArray() );
        runner.runAll(new ExternalizableTest());
//        WarmUP = 0; Run = WarmUP+1;
//        runner.runAll(new Swing() );
        runner.charter.closeDoc();

//        try {
//            String fnam = "F:\\tmp\\test.oos";
//            FileOutputStream out = new FileOutputStream(fnam);
//            FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
//            FSTObjectOutput fstout = new FSTObjectOutput(out, conf);
//            Trader initial = Trader.generateTrader(101, true);
//            fstout.writeObject(initial);
//            fstout.close();
//
//            FileInputStream fin = new FileInputStream(fnam);
//            FSTObjectInput fstin = new FSTObjectInput(fin,conf);
//            Object res = fstin.readObject();
//            fstin.close();
//            System.out.println("file test success "+DeepEquals.deepEquals(res,initial));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

    }
}
