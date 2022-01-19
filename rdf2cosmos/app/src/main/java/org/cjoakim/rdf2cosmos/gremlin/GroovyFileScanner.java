package org.cjoakim.rdf2cosmos.gremlin;

import org.cjoakim.rdf2cosmos.AppConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Instances of this class are used to read a generated "Grooby" file (i.e. - groovy.txt)
 * and scan it for unmatched vertices and edges, and other quality-control logic.
 *
 * Chris Joakim, Microsoft, January 2022
 */
public class GroovyFileScanner {

    // Instance variables:
    private String basename = null;
    private int groovyLineCount;
    private int vertexCount = 0;
    private int edgeCount = 0;

    public GroovyFileScanner(String basename) {

        super();
        this.basename = basename;
    }

    // addE

    public void scan() {

        String fqFilename = AppConfig.getGremlinFilename(basename);
        log("scan() fqFilename: " + fqFilename);

        FileInputStream inputStream = null;
        Scanner sc = null;

        try {
            inputStream = new FileInputStream(fqFilename);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String groovyLine = sc.nextLine();
                if ((groovyLine != null) && (groovyLine.length() > 0)) {
                    groovyLineCount++;
                    if (groovyLine.contains("addE(")) {
                        processEdgeLine(groovyLine);
                    }
                    else {
                        processVertexLine(groovyLine);
                    }
                }
            }
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
            eojDisplays();
        }
        catch (Exception ex) {
            log("ERROR exception encountered " + ex.getClass().getName() + " " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex2) {
                    log("ERROR exception encountered closing inputStream " + ex2.getClass().getName() + " " + ex2.getMessage());
                    ex2.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    private void processVertexLine(String groovyLine) {

        vertexCount++;
        log("vertex line " + groovyLineCount + " | "+ groovyLine);
    }

    private void processEdgeLine(String groovyLine) {

        edgeCount++;
        log("edge line " + groovyLineCount + " | "+ groovyLine);
    }

    private void eojDisplays() {

        log("EOJ Totals:");
        log("  groovyLineCount: " + groovyLineCount);
        log("  vertexCount:     " + vertexCount);
        log("  edgeCount:       " + edgeCount);
    }

    private void log(String msg) {

        System.out.println("" + msg);
    }
}
