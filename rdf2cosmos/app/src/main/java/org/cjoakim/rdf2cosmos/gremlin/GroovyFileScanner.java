package org.cjoakim.rdf2cosmos.gremlin;

import org.cjoakim.rdf2cosmos.AppConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    public static final String NULL_VALUE_PATTERN   = "null";

    // Instance variables:
    private String basename = null;
    private int groovyLineCount;
    private int vertexCount = 0;
    private int edgeCount = 0;
    private int errorCount = 0;
    private int exceptionCount = 0;
    private HashMap<String,String> vertexMap;
    private HashMap<String,String> edgeMap;
    private ArrayList<String> rawInputFiles;
    private ArrayList<String> rawInputLines;

    public GroovyFileScanner(String basename, ArrayList<String> rawInputs) {

        super();
        this.basename = basename;
        rawInputFiles = rawInputs;                // <-- the raw RDF files produced by riot step 1
        rawInputLines = new ArrayList<String>();  // <-- the aggregated nt file lines from riot step 1
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

        loadRawInputs();

        FileInputStream inputStream = null;
        Scanner sc = null;

        try {
            inputStream = new FileInputStream(fqFilename);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String groovyLine = sc.nextLine();
                if ((groovyLine != null) && (groovyLine.length() > 0)) {
                    groovyLineCount++;
                    log("line " + groovyLineCount + " | "+ groovyLine);

                    if (groovyLine.contains(EMPTY_VALUE_PATTERN)) {
                        errorCount++;
                        log("ERROR on line " + groovyLineCount + ", empty value");
                    }
                    if (groovyLine.contains(NULL_VALUE_PATTERN)) {
                        errorCount++;
                        log("ERROR on line " + groovyLineCount + ", null value");
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

    private void loadRawInputs() {

        for (int i = 0; i < rawInputFiles.size(); i++) {
            String path = rawInputFiles.get(i);
            String fqFilename = AppConfig.getDataFileFqPath(path);
            log("Reading_raw_input: " + fqFilename);
            FileInputStream inputStream = null;
            Scanner sc = null;
            int lineNum = 0;
            try {
                inputStream = new FileInputStream(fqFilename);
                sc = new Scanner(inputStream, "UTF-8");
                while (sc.hasNextLine()) {
                    lineNum++;
                    String raw = path + " " + lineNum + " | " + sc.nextLine();
                    this.rawInputLines.add(raw);
                    log("raw_input_line: " + raw);
                }
                if (sc.ioException() != null) {
                    throw sc.ioException();
                }
            }
            catch (Exception ex) {
                exceptionCount++;
                log("EXCEPTION exception encountered on raw file " + fqFilename);
                ex.printStackTrace();
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException ex2) {
                        exceptionCount++;
                        log("EXCEPTION exception encountered closing raw file " + fqFilename);
                        ex2.printStackTrace();
                    }
                }
                if (sc != null) {
                    sc.close();
                }
            }
        }
        log("Loaded_raw_inputs; line count: " + this.rawInputLines.size());
    }

    private void processVertexLine(String groovyLine) {
        // Vertex line looks like:
        // g.addV('xxx').property('id','z2fd528f4-cec2-4530-9640-13e11eb1fbf6ed79332f-b259-4cd7-bc9f-5be4f20625fa').property

        vertexCount++;
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
        // Edge line looks like:
        // g.V(['15bbd6c2-3345-4718-888f-0a231249188a-20191223','15bbd6c2-3345-4718-888f-0a231249188a-20191223']).addE('isStoryRoleIn').to(g.V(['17a154a4-58b0-4da4-965a-528461b26be6-20191223','17a154a4-58b0-4da4-965a-528461b26be6-20191223']))

        edgeCount++;
        int idIdx1a = groovyLine.indexOf(EDGE_ID1_PATTERN);
        int idIdx2a = groovyLine.indexOf(EDGE_ID2_PATTERN);
        log("idIdx1a: " + idIdx1a + ", idIdx2a: " + idIdx2a);

        if ((idIdx1a >= 0) && (idIdx2a > idIdx1a)) {
            int idIdx1b = idIdx1a + EDGE_ID1_PATTERN.length();
            int idIdx1c = groovyLine.indexOf("'", idIdx1b);
            log("idIdx1a " + idIdx1a + ", idIdx1b " + idIdx1b + ", idIdx1c " + idIdx1c);
            String id1 = groovyLine.substring(idIdx1b, idIdx1c);
            log("id1: " + id1);

            int idIdx2b = idIdx2a + EDGE_ID2_PATTERN.length();
            int idIdx2c = groovyLine.indexOf("'", idIdx2b);
            String id2 = groovyLine.substring(idIdx2b, idIdx2c);
            log("id2: " + id2);

            if (id1.length() < 2) {
                errorCount++;
                log("ERROR on line " + groovyLineCount + ", id1 too short");
            }
            if (id2.length() < 2) {
                errorCount++;
                log("ERROR on line " + groovyLineCount + ", id2 too short");
            }

            if (!vertexMap.containsKey(id1)) {
                errorCount++;
                log("ERROR on line " + groovyLineCount + ", id1 is not a vertex id | " + id1);
                scanRawDataForValue(id1);
            }
            if (!vertexMap.containsKey(id2)) {
                errorCount++;
                log("ERROR on line " + groovyLineCount + ", id2 is not a vertex id | " + id2);
                scanRawDataForValue(id2);
            }
        }
        else {
            errorCount++;
            log("ERROR on line " + groovyLineCount + ", malformed edge ids");
        }
    }

    private void scanRawDataForValue(String s) {
        int matcheCount = 0;
        log("MATCHING: " + s);
        for (int i = 0; i < rawInputLines.size(); i++) {
            String line = rawInputLines.get(i);
            if (line.contains(s)) {
                matcheCount++;
                log("MATCHED: " + line);
            }
        }
        log("MATCH_COUNT: " + matcheCount + " for " + s);
    }

    private void eojDisplays() {

        log("EOJ Totals:");
        log("  groovyLineCount: " + groovyLineCount);
        log("  rawInput files:: " + rawInputFiles.size());
        log("  rawInput lines:: " + rawInputLines.size());
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
