package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;
import org.cjoakim.rdf2cosmos.gremlin.GremlinLoader;
import org.cjoakim.rdf2cosmos.gremlin.GroovyBuilder;
import org.cjoakim.rdf2cosmos.gremlin.Property;

import java.io.File;
import java.io.IOException;

/**
 * This is the entry-point into this application; it has a main() method.
 * See the main() method for functionality and command-line args.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class App {

    public static void main(String[] args) {

        if (args.length < 1) {
            log("No command-line args; terminating...");
        }
        else {
            AppConfig.setCommandLineArgs(args);
            String function = args[0];
            AppConfig.display();

            switch (function) {

                case "app_config":
                    displayAppConfig();
                    break;

                case "convert_rdf_to_objects":
                    String infile = args[1];
                    convertRdfToObjects(infile);
                    break;

                case "convert_objects_to_gremlin":
                    convertObjectsToGremlinGroovy();
                    break;

                case "load_cosmosdb_graph":
                    String groovyStatementsFile = args[1];
                    loadCosmosDbGraph(groovyStatementsFile);
                    break;

                case "ad_hoc":
                    AdHoc();
                    break;

                default:
                    log("unknown main function: " + function);
            }
        }
    }

    private static void displayAppConfig() {

        AppConfig.display();
    }

    private static void convertRdfToObjects(String infile) {

        String fqInfile = AppConfig.getDataFileFqPath(infile);
        log("infile:   " + infile);
        log("fqInfile: " + fqInfile);

        Accumulator accumulator = new Accumulator();

        // Write a stream out.
        StreamRDF output = StreamRDFLib.writer(System.out);

        // Wrap in a filter.
        AppRdfStream customStream = new AppRdfStream(output);
        customStream.setAccumulator(accumulator);

        // Call the parsing process.
        RDFParser.source(fqInfile).parse(customStream);

        log("App end_of_convertRdfToObjects with: " + infile);
    }

    private static void convertObjectsToGremlinGroovy() {

        log("convertObjectsToGremlinGroovy....");
        GroovyBuilder builder = new GroovyBuilder();
        try {
            builder.build();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadCosmosDbGraph(String groovyStatementsFile) {

        try {
            GremlinLoader loader = new GremlinLoader();
            loader.load(groovyStatementsFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Experimental ad-hoc code.
     */
    private static void AdHoc() {

    }

    private static void log(String msg) {

        System.out.println(msg);
    }
}
