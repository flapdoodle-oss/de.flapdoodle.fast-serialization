package de.ruedigermoeller.serialization;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 19.11.12
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public interface FSTObjectCopy {

    public Object copy( Object toCopy, FSTConfiguration conf ) throws IOException, ClassNotFoundException;

}
