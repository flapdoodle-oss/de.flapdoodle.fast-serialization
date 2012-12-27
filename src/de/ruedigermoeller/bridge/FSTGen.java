package de.ruedigermoeller.bridge;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTCrossLanguageSerializer;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 26.12.12
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */
public class FSTGen {
    protected FSTBridgeGenerator gen;

    public FSTGen(FSTBridgeGenerator gen) {
        this.gen = gen;
    }

    protected Class mapDeclarationType(Class type, FSTClazzInfo info) {
        if ( gen.isRegistered(type) || isSystemClass(type) ) {
            return type;
        }
        if (info.getSer() instanceof FSTCrossLanguageSerializer) {
            return ((FSTCrossLanguageSerializer) info.getSer()).getCrossLangLayout();
        }
        throw new RuntimeException("unmappable class:"+type.getName());
    }

    public boolean isSystemClass(Class clz) {
        return clz.isPrimitive() || clz.getName().startsWith("java") || clz == Object[].class;
    }
}
