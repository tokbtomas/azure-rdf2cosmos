package org.cjoakim.rdf2cosmos;

import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.cjoakim.rdf2cosmos.gremlin.GremlinLoader;
import org.cjoakim.rdf2cosmos.gremlin.GroovyBuilder;

import java.io.File;

/**
 * This is the entry-point into this application; it has a main() method.
 * See the main() method for functionality and command-line args.
 *
 * This class can be executed either via gradle tasks (see gradle.build file), or from
 * a traditional command-line "java" program execution (see the *.sh and *.ps1 scripts).
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class App {

    public static void main(String[] args) {

        if (args.length < 1) {
            log("No command-line args; terminating...");
        }
        else {
            try {
                AppConfig.setCommandLineArgs(args);
                String function = args[0];
                AppConfig.display(false);

                switch (function) {

                    case "app_config":
                        displayAppConfig();
                        break;

                    case "clear_cache":
                        String cacheType = args[1];
                        clearCache(cacheType);
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
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayAppConfig() {

        AppConfig.display(false);
    }

    private static void clearCache(String cacheType) {

        try {
            PersistentCache persistentCache = getPersistentCacheInstance();
            persistentCache.deleteAll();
            persistentCache.deleteAll();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void convertRdfToObjects(String infile) throws Exception {

        log("infile:   " + infile);

        String fqInfile = AppConfig.getDataFileFqPath(infile);
        File f = new File(fqInfile);
        log("fqInfile: " + fqInfile + " exists: " + f.exists());

        PersistentCache persistentCache = getPersistentCacheInstance();

        // Write a stream out.
        StreamRDF output = StreamRDFLib.writer(System.out);

        // Wrap in a filter.
        AppRdfStream outputStream = new AppRdfStream(output, persistentCache);

        // Call the parsing process.
        RDFParser.source(fqInfile).parse(outputStream);

        log("App end_of_convertRdfToObjects with: " + infile);
    }

    private static void convertObjectsToGremlinGroovy() {

        try {
            log("convertObjectsToGremlinGroovy....");
            PersistentCache persistentCache = getPersistentCacheInstance();
            GroovyBuilder builder = new GroovyBuilder(persistentCache);
            builder.build();
        }
        catch (Exception e) {
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

    /**
     * Return either an instance of DiskCache or PostgresqlCache depending on
     * application configuration.
     */
    private static PersistentCache getPersistentCacheInstance() {

        if (AppConfig.isAzurePostgresqlCacheType()) {
            return new PostgresqlCache();
        }
        else {
            return new DiskCache();
        }
    }

    private static void log(String msg) {

        System.out.println(msg);
    }
}
