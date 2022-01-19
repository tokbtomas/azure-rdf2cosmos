package org.cjoakim.rdf2cosmos.gremlin;

import org.cjoakim.rdf2cosmos.AppConfig;

/**
 * Instances of this class are used to read a generated "Grooby" file (i.e. - groovy.txt)
 * and scan it for unmatched vertices and edges, and other quality-control logic.
 *
 * Chris Joakim, Microsoft, January 2022
 */
public class GroovyFileScanner {

    private String basename = null;

    public GroovyFileScanner(String basename) {

        super();
        this.basename = basename;
    }

    public void scan() {

        String fqFilename = AppConfig.getGremlinFilename(basename);
        log("scan() fqFilename: " + fqFilename);


    }

    private void log(String msg) {

        System.out.println("GroovyFileScanner: " + msg);
    }
}
