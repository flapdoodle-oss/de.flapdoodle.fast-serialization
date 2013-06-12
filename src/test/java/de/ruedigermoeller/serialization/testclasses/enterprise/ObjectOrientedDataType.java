package de.ruedigermoeller.serialization.testclasses.enterprise;

import de.ruedigermoeller.serialization.annotations.Compress;
import de.ruedigermoeller.serialization.annotations.EqualnessIsIdentity;
import de.ruedigermoeller.serialization.annotations.OneOf;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: moelrue
 * Date: 22.11.12
 * Time: 18:58
 * To change this template use File | Settings | File Templates.
 */
@EqualnessIsIdentity
public class ObjectOrientedDataType implements Serializable {

    // encapsulate like a boss ..

    // just to test, benchmark ignores this for default conf
    @OneOf({"HipHop", "********","pok a asudh ","killbill not yet","fillorkill","wacka wacka","BeBop","pokasd","irgendwas","adadad" })
    @Compress
    private String aString="";
    private boolean isCapableOfDoingAnythingMeaningful;
    public ObjectOrientedDataType() {}

    public ObjectOrientedDataType(String valueString) {
        if ( valueString == null ) {
            isCapableOfDoingAnythingMeaningful = false;
            aString = "";
        } else {
            this.aString = valueString;
        }
    }

    public boolean equals( Object o ) {
        if ( o instanceof ObjectOrientedDataType) {
            ObjectOrientedDataType dt = (ObjectOrientedDataType) o;
            return dt.aString.equals(aString) && dt.isCapableOfDoingAnythingMeaningful == isCapableOfDoingAnythingMeaningful;
        }
        return super.equals(o);
    }

    public int hashCode() {
        return aString.hashCode();
    }

    public String toString() {
        return aString;
    }
}
