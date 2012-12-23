package de.ruedigermoeller.bridge;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTCrossLanguageSerializer;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 23.12.12
 * Time: 00:55
 * To change this template use File | Settings | File Templates.
 */
public class FSTBridgeGenerator {

    HashSet<Class> knownClasses = new HashSet<Class>();
    FSTConfiguration conf = FSTConfiguration.createCrossLanguageConfiguration();

    public void addClass(Class c) {
        FSTClazzInfo info = conf.getCLInfoRegistry().getCLInfo(c);
        if ( info.useCompatibleMode() || info.isExternalizable() ) {
            throw new RuntimeException("cannot use class "+c.getName()+" for cross language messages. It defines JDK specific serialization methods.");
        }
        if ( info.getSer() != null && info.getSer() instanceof FSTCrossLanguageSerializer == false ) {
            System.out.println("warning: Serializer registered for "+c.getName()+" will be ignored. Not a cross-language serializer");
        }
        knownClasses.add(c);
    }

}
