package de.ruedigermoeller.bridge;

import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTCrossLanguageSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

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
 * Date: 22.12.12
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class FSTBridgeGen {

    protected FSTBridgeGenerator gen;

    public FSTBridgeGen(FSTBridgeGenerator gen) {
        this.gen = gen;
    }

    public void generateClazz(FSTClazzInfo info, String file, String depth ) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new FileOutputStream(file + File.separator + getFileName(info)) );
        generateClazz(info, ps, depth);
        ps.close();
    }

    protected String getFileName(FSTClazzInfo info) {
        return "UNDEFINED";
    }

    public void generateClazz( FSTClazzInfo info, PrintStream out, String depth ) {
        FSTClazzInfo layout = info;
        if ( info.getSer() instanceof FSTCrossLanguageSerializer ) {
            layout = gen.getCLInfo(((FSTCrossLanguageSerializer) info.getSer()).getCrossLangLayout());
        }
        generateHeader(info,layout,out,depth);
        FSTClazzInfo.FSTFieldInfo[] fieldInfo = layout.getFieldInfo();
        for (int i = 0; i < fieldInfo.length; i++) {
            FSTClazzInfo.FSTFieldInfo fstFieldInfo = fieldInfo[i];
            generateFieldDeclaration(info, fstFieldInfo, out,depth+"    ");
        }
        out.println();
        generateWriteMethod(info,layout,out,depth);
        out.println();
        generateReadMethod(info,layout, out, depth);
        out.println();
        generateFooter(info,out,depth);

    }

    public void generateFieldDeclaration(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {
    }

    protected void generateHeader(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
    }

    protected void generateFooter(FSTClazzInfo info, PrintStream out, String depth) {
    }

    protected String getBridgeClassName(FSTClazzInfo info) {
        return "fst" + info.getClazz().getSimpleName();
    }

    public void generateWriteMethod(FSTClazzInfo clInfo, FSTClazzInfo layout, PrintStream out, String depth) {
    }

    protected void generateWriteField(FSTClazzInfo clInfo, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {

    }

    public void generateReadMethod(FSTClazzInfo info, FSTClazzInfo layout, PrintStream out, String depth) {
    }

    protected void generateReadField(FSTClazzInfo info, FSTClazzInfo.FSTFieldInfo fieldInfo, PrintStream out, String depth) {

    }

}
