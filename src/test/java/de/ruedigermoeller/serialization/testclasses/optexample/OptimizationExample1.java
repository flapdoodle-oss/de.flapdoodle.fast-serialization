package de.ruedigermoeller.serialization.testclasses.optexample;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.annotations.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 07.12.12
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */
@Predict({OptimizationExample1.Person.class,OptimizationExample1.OtherPerson.class})
public class OptimizationExample1 implements Serializable {

    int anInt = 10;
    @Thin int arr[] = { 0, 0, 0, 0, 0, 0, 0, 10, 0, 1 };
    Person persons[] = { null, new Person("Emil", "Moeller"), null, new Person("Felix", "Moelle"+(anInt>0?"r":"")),null, null, new Person("Emil","Moeller")};
    boolean bool = false;
    @Flat OtherPerson p = new OtherPerson("Bla", "Blubberblubb");
    OtherPerson p1 = new OtherPerson("Bla1", "Blubberblubb1");
    @Flat OtherPerson p2 = new OtherPerson("Bla2", "Blubberblubb2");
    OtherPerson p3 = new OtherPerson("Bla1", "Blubberblubb1");

    @EqualnessIsIdentity
    class Person implements Serializable {
        @Compress String name;
        @Compress String firstname;
        long makemebigger=7777l;

        Person(String name, String firstname) {
            this.name = name;
            this.firstname = firstname;
        }

        public int hashCode() {
            return name.hashCode()^firstname.hashCode();
        }

        public boolean equals( Object o ) {
            if ( o instanceof Person ) {
                return ((Person) o).name.equals(name) && ((Person) o).firstname.equals(firstname);
            }
            return false;
        }
    }

    @EqualnessIsIdentity
    class OtherPerson implements Serializable {
        String name;
        String firstname;
        long makemebigger=7777l;

        OtherPerson(String name, String firstname) {
            this.name = name;
            this.firstname = firstname;
        }

        public int hashCode() {
            return name.hashCode()^firstname.hashCode();
        }

        public boolean equals( Object o ) {
            if ( o instanceof OtherPerson ) {
                return ((OtherPerson) o).name.equals(name) && ((OtherPerson) o).firstname.equals(firstname);
            }
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
//        FSTConfiguration conf = FSTConfiguration.createMinimalConfiguration();
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        OptimizationExample1 ex = new OptimizationExample1();
        System.out.println("Size:"+conf.calcObjectSizeBytesNotAUtility(ex));
        System.out.println("Write:"+conf.calcObjectWriteTimeNotAUtility(1000000, ex));
        System.out.println("Read:"+conf.calcObjectReadTimeNotAUtility(1000000, ex));
    }

}

