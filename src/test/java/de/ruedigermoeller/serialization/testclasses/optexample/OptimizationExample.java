package de.ruedigermoeller.serialization.testclasses.optexample;

import de.ruedigermoeller.serialization.FSTConfiguration;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 00:07
 * To change this template use File | Settings | File Templates.
 */
public class OptimizationExample implements Serializable {

    int anInt = 10;
    int arr[] = { 0, 0, 0, 0, 0, 0, 0, 10, 0, 1 };
    Person persons[] = { null, new Person("Emil", "Moeller"), null, new Person("Felix", "Moelle"+(anInt>0?"r":"")), null, null, new Person("Emil","Moeller")};
    boolean bool = false;
    OtherPerson p = new OtherPerson("Bla", "Blubberblubb");
    OtherPerson p1 = new OtherPerson("Bla1", "Blubberblubb1");
    OtherPerson p2 = new OtherPerson("Bla2", "Blubberblubb2");
    OtherPerson p3 = new OtherPerson("Bla1", "Blubberblubb1");

    class Person implements Serializable {
        String name;
        String firstname;
        long makemebigger=7777l;

        Person(String name, String firstname) {
            this.name = name;
            this.firstname = firstname;
        }

    }

    class OtherPerson implements Serializable {
        String name;
        String firstname;
        long makemebigger=7777l;

        OtherPerson(String name, String firstname) {
            this.name = name;
            this.firstname = firstname;
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
//        FSTConfiguration conf = FSTConfiguration.createMinimalConfiguration();
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        OptimizationExample ex = new OptimizationExample();
        System.out.println("Size:"+conf.calcObjectSizeBytesNotAUtility(ex));
        System.out.println("Write:"+conf.calcObjectWriteTimeNotAUtility(1000000, ex));
        System.out.println("Read:"+conf.calcObjectReadTimeNotAUtility(1000000, ex));
    }

}
