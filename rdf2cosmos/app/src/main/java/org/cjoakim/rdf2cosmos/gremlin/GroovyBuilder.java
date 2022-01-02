package org.cjoakim.rdf2cosmos.gremlin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.cjoakim.rdf2cosmos.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used to read the parsed and cached JSON files and convert these
 * into "Groovy" syntax for loading into CosmosDB in a separate process.
 *
 * Each of the JSON files in the data/cache directory will be converted into the
 * corresponding Groovy syntax and written to one file in the data/gremlin directory.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class GroovyBuilder {

    // Instance variables:
    private File outputFile = null;
    private ArrayList<String> outputLines = null;
    private int maxOutputLinesCacheCount;

    public GroovyBuilder() {

        super();

        this.outputFile = new File(AppConfig.getGremlinFilename("groovy.txt"));
        resetOutputLines();
        maxOutputLinesCacheCount = AppConfig.getMaxObjectCacheCount();
    }

    public void build() throws IOException {

        log("GroovyBuilder.build() start");
        deleteRecreateOutfile();
        buildGroovyNodes("vertex__");
        buildGroovyNodes("edge__");
        log("GroovyBuilder.build() finish");
    }

    private void buildGroovyNodes(String type) throws IOException {

        ArrayList<String> filesList = getCacheFilesList(type);
        log("buildGroovyNodes type: " + type + ", file count: " + filesList.size());

        for (int i = 0; i < filesList.size(); i++) {
            String filename = filesList.get(i);
            GraphNode gn = readCachedGraphNode(filename);
            if (gn.isValid()) {
                String groovy = gn.toGroovy();
                //log(groovy);
                outputLines.add(groovy);

                if (outputLines.size() > maxOutputLinesCacheCount) {
                    flushOutputLines();
                }
            }
        }
        flushOutputLines();
    }

    private ArrayList<String> getCacheFilesList(String type) {

        File cacheDir = new File(AppConfig.getCacheDirectory());
        ArrayList<String> list = new ArrayList<String>();
        Iterator<File> files = FileUtils.iterateFiles(cacheDir, TrueFileFilter.INSTANCE, null);

        while (files.hasNext()) {
            File f = files.next();
            String fqPath = f.getAbsolutePath();
            if (fqPath.contains(type)) {
                list.add(fqPath);
            }
        }
        return list;
    }

    private GraphNode readCachedGraphNode(String filename) {

        try {
            log("readCachedGraphNode: " + filename);
            File file = new File(filename);
            if (file.exists()) {
                String jstr = FileUtils.readFileToString(file, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(jstr, GraphNode.class);
            }
        }
        catch (IOException e) {
            log("ERROR readCachedGraphNode: " + filename + " " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void deleteRecreateOutfile() {

        log("deleteRecreateOutfile: " + outputFile.getAbsolutePath());

        if (outputFile.exists()) {
            try {
                FileUtils.delete(outputFile);
                log("deleted outputFile");
            }
            catch (Exception e) {
                log("ERROR deleting outputFile");
            }
        }
        else {
            log("outputFile is absent");
        }

        try {
            FileUtils.write(outputFile, "");
            log("outputFile created");
        }
        catch (Exception e) {
            log("ERROR creating outputFile");
        }
    }

    private void flushOutputLines() throws IOException {

        String sep = System.lineSeparator();

        for (int i = 0; i < outputLines.size(); i++) {
            String line = outputLines.get(i) + sep;
            FileUtils.writeStringToFile(outputFile, line, true);
        }
        resetOutputLines();
    }

    private void resetOutputLines() {

        outputLines = new ArrayList<String>();
    }

    private void log(String msg) {

        System.out.println(msg);
    }
}
