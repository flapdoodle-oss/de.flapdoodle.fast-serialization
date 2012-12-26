package de.ruedigermoeller.bridge.java;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 25.12.12
 * Time: 16:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class FSTJavaFactory {

    public abstract Object instantiate(int clzId, InputStream in, FSTSerBase container) throws IOException;
    public Object defaultInstantiate(Class clz, InputStream in, FSTSerBase container) {
        return null;
    }

}
