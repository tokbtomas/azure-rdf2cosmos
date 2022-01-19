package org.cjoakim.rdf2cosmos.gremlin;

import org.cjoakim.rdf2cosmos.AppConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Instances of this class are used to read a generated "Grooby" file (i.e. - groovy.txt)
 * and scan it for unmatched vertices and edges, and other quality-control logic.
 *
 * Chris Joakim, Microsoft, January 2022
 */
public class GroovyFileScanner {

    // Constants:
    public static final String VERTEX_ID_PATTERN    = ".property('id','";
    public static final String EDGE_ID1_PATTERN     = "g.V(['";
    public static final String EDGE_ID2_PATTERN     = "to(g.V(['";
    public static final String EMPTY_VALUE_PATTERN  = "''";

    // Instance variables:
    private String basename = null;
    private int groovyLineCount;
    private int vertexCount = 0;
    private int edgeCount = 0;
    private int errorCount = 0;
    private int exceptionCount = 0;
    private HashMap<String,String> vertexMap;
    private HashMap<String,String> edgeMap;

    public GroovyFileScanner(String basename) {

        super();
        this.basename = basename;
        vertexMap = new HashMap<String,String>();
        edgeMap   = new HashMap<String,String>();
    }

    /**
     * Scan the given groovy file, for loading into CosmosDB.  The sequence of the
     * file is assumed to have all Vertices first, then the Edges - as was created
     * in class GroovyBuilder.
     */
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
                    if (groovyLine.contains(EMPTY_VALUE_PATTERN)) {
                        errorCount++;
                        log("ERROR on line " + groovyLineCount + ", empty value | " + groovyLine);
                    }
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
            exceptionCount++;
            log("EXCEPTION exception encountered " + ex.getClass().getName() + " " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex2) {
                    exceptionCount++;
                    log("EXCEPTION exception encountered closing inputStream " + ex2.getClass().getName() + " " + ex2.getMessage());
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

        // Vertex line looks like:
        // g.addV('xxx').property('id','z2fd528f4-cec2-4530-9640-13e11eb1fbf6ed79332f-b259-4cd7-bc9f-5be4f20625fa').property

        int idIdx0 = groovyLine.indexOf(VERTEX_ID_PATTERN);
        if (idIdx0 > 0) {
            try {
                int idIdx1 = idIdx0 + VERTEX_ID_PATTERN.length();
                int idIdx2 = groovyLine.indexOf("'", idIdx1 + VERTEX_ID_PATTERN.length());
                log("idIdx0 " + idIdx0 + ", idIdx1 " + idIdx1 + ", idIdx2 " + idIdx2);
                String id = groovyLine.substring(idIdx1, idIdx2);
                log("id: " + id);
                if (vertexMap.containsKey(id)) {
                    errorCount++;
                    log("ERROR on line " + groovyLineCount + ", duplicate id | " + id);
                }
                else {
                    if (id.length() < 8) {
                        log("WARNING on line " + groovyLineCount + ", short id | " + id);
                    }
                    else {
                        vertexMap.put(id, "" + groovyLineCount + " | " + groovyLine);
                    }
                }
            }
            catch (Exception e) {
                errorCount++;
                log("ERROR on line " + groovyLineCount + ", inparsable id");
                e.printStackTrace();
            }
        }
        else {
            errorCount++;
            log("ERROR on line " + groovyLineCount + ", no VERTEX_ID_PATTERN");
        }



    }

    private void processEdgeLine(String groovyLine) {

        edgeCount++;
        //log("edge line " + groovyLineCount + " | "+ groovyLine);

        // Edge line looks like:
        // g.V(['15bbd6c2-3345-4718-888f-0a231249188a-20191223','15bbd6c2-3345-4718-888f-0a231249188a-20191223']).addE('isStoryRoleIn').to(g.V(['17a154a4-58b0-4da4-965a-528461b26be6-20191223','17a154a4-58b0-4da4-965a-528461b26be6-20191223']))
    }

    private void eojDisplays() {

        log("EOJ Totals:");
        log("  groovyLineCount: " + groovyLineCount);
        log("  vertexCount:     " + vertexCount);
        log("  vertexMap size:  " + vertexMap.size());
        log("  edgeCount:       " + edgeCount);
        log("  errorCount:      " + errorCount);
        log("  exceptionCount:  " + exceptionCount);
    }

    private void log(String msg) {

        System.out.println("" + msg);
    }
}
