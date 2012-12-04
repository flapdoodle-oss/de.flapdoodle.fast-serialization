package de.ruedigermoeller.serialization.testclasses.enterprise;

import de.ruedigermoeller.serialization.*;
import de.ruedigermoeller.serialization.annotations.EqualnessIsIdentity;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: moelrue
 * Date: 22.11.12
 * Time: 19:04
 * To change this template use File | Settings | File Templates.
 */
//@EqualnessIsIdentity <= does not pay off
public class ObjectOrientedInt implements Serializable {

    int value;

    public ObjectOrientedInt() {}

    public ObjectOrientedInt(int value) {
        this.value = value;
    }

    public boolean equals( Object o ) {
        if ( o instanceof ObjectOrientedInt) {
            ObjectOrientedInt dt = (ObjectOrientedInt) o;
            return dt.value == value;
        }
        return super.equals(o);
    }

    public int hashCode() {
        return value;
    }

}
