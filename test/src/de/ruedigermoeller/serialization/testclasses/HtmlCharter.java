package de.ruedigermoeller.serialization.testclasses;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 01.12.12
 * Time: 01:18
 * To change this template use File | Settings | File Templates.
 */
public class HtmlCharter {
    PrintStream out;
    String color = "#a04040";

    public HtmlCharter(String file) {
        try {
            out = new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openDoc() {
        out.println("<html>");
    }

    public void heading(String title) {
        out.println("<br><h4>" + title + "</h4>");
    }

    public void openChart(String title) {
        out.println("<b>"+title+"</b>");
        out.println("<table border=0 cellpadding=0 cellspacing=0>");
    }

    public void chartBar(String text, int value, int div, String cl) {
        color = cl;
        if ( value == 0 ) {
            out.println("<tr><td><font color=red size=2><b>FAILURE</b> "+text+"</font></td></tr>");
            return;
        }
        out.println("<tr><td><font size=1 color="+ getChartColor() +">");
        for (int i=0; i<value/div;i++) {
            out.print("&#9608;");
        }
        if ( value%div >= div/2 ) {
            out.print("&#9612;");
        }
        out.print("</font>&nbsp;<font size=2><b>"+text+" ("+value+")</b></font>");
        out.println("</td></tr>");
    }

    public String getChartColor() {
        return color;
    }

    public void closeChart() {
        out.println("</table><br>");
    }

    public void closeDoc() {
        out.println("</html>");
    }

    public static void main( String arg[] ) {
        HtmlCharter charter = new HtmlCharter("f:\\tmp\\test.html");
        charter.openDoc();
        charter.openChart("Serialization Size" );
        charter.chartBar("FST", 99, 3, "#a04040");
        charter.chartBar("Kry", 100, 3, "#a04040");
        charter.chartBar("Def", 221, 3, "#a04040");
        charter.closeChart();
        charter.closeDoc();
    }

}
