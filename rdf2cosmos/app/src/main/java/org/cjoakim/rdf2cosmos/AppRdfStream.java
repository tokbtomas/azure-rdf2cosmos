package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a custom extension of class StreamRDFWrapper in the Apache Jena SDK.
 * It receives the stream of events as Jena reads and parses an RDF (i.e. - ttl, nt, rdf) file.
 *
 * See SDK docs at https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/system/StreamRDFWrapper.html
 *
 * The primary event handled by this stream class is "triple(...)", "start()" and "finish()".
 * The Triples are received, processed into "GraphNode" objects, and accumulated/aggregated
 * an instance of class PersistentCache (DiskCache or PostgresqlCache).  The GraphNode objects
 * in cache are flushed to disk or database at EOJ, and Gremlin/Groovy syntax can be created from
 * these for loading into CosmosDB.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class AppRdfStream extends StreamRDFWrapper {

    // Instance variables:
    private int tripleCount = 0;
    private long startTime  = 0;
    private long finishTime = 0;
    private HashMap<String, String> prefixMap = null;
    private HashMap<String, String> edgeLabelsMap = null;
    private ArrayList<String>  prefixList = new ArrayList<String>();
    private PersistentCache persistentCache = null;
    private long tripleHandledCount = 0;
    private long tripleUnhandledCount = 0;
    private int  maxObjectCacheCount;

    AppRdfStream(StreamRDF dest) {

        super(dest);
        startTime = System.currentTimeMillis();
        prefixMap = new HashMap<String, String>();
        edgeLabelsMap = new HashMap<String, String>();
        maxObjectCacheCount = AppConfig.getMaxObjectCacheCount();

        if (AppConfig.isAzurePostgresqlCacheType()) {
            persistentCache = new DiskCache();
            //persistentCache = new PostgresqlCache();    <-- TODO: uncomment later
        }
        else {
            persistentCache = new DiskCache();
        }
        log("AppRdfStream maxObjectCacheCount: " + maxObjectCacheCount);
    }

    public int getTripleCount() {

        return tripleCount;
    }

    @Override
    public void start() {

        log("AppRdfStream_start");
    }

    @Override
    public void finish() {

        finishTime = System.currentTimeMillis();
        double elapsed = (finishTime - startTime) / 1000.0;
        log("AppRdfStream_finish in " + elapsed + " seconds, tripleCount: " + tripleCount);

        String outfile = AppConfig.getMetaFilename("edge_labels_map.json");
        writeJsonObject(edgeLabelsMap, outfile);

        try {
            persistentCache.flushMemoryCache();
            log("AppRdfStream_finish tripleHandledCount:   " + tripleHandledCount);
            log("AppRdfStream_finish tripleUnhandledCount: " + tripleUnhandledCount);
            persistentCache.eojLogging();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prefix(String prefix, String iri) {

        if (prefix != null) {
            if (iri != null) {
                prefixMap.put(prefix, iri);
                prefixMap.put(iri, prefix);
                log("AppRdfStream_prefix " + prefix + " | " + iri);
            }
        }
    }

    @Override
    public void quad(Quad quad) {

        log("AppRdfStream_quad | " + quad );  // this event isn't expected or handled
    }

    @Override
    public void triple(Triple triple) {

        log("AppRdfStream_triple_event | " + triple);
        tripleCount++;
        if (tripleCount == 1) {
            String outfile = AppConfig.getMetaFilename("prefix_map.json");
            writeJsonObject(prefixMap, outfile);
        }

        try {
            boolean tripleHandled = false;
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();
            if (AppConfig.isVerbose()) {
                logNode("subj", s);
                logNode("pred", p);
                logNode("obj", o);
            }

            if (isResourceUri(s)) {
                // all triples are expected to have a subject that is a URI
                GraphNode gn = null;
                String vertexId1     = uriResourceId(s.getURI());
                String resourceLabel = uriResourceLabel(s.getURI());

                if (isResourceUri(o)) {
                    // this triple is an Edge as both subject and object are resource URIs
                    String vertexId2 = uriResourceId(o.getURI());
                    String label     = edgeLabel(p.toString());
                    edgeLabelsMap.put(label, p.toString());
                    String key = GraphNode.edgeCacheKey(vertexId1, vertexId2, label);
                    gn = persistentCache.getGraphNode(key);
                    if (gn == null) {
                        gn = new GraphNode(GraphNode.TYPE_EDGE);
                        gn.setVertexId1(vertexId1);
                        gn.setVertexId2(vertexId2);
                        gn.setLabel(label);
                        persistentCache.putGraphNode(key, gn);
                    }
                    tripleHandled = true;
                }
                else {
                    // this triple is a Vertex or Vertex Property
                    String key = GraphNode.vertexCacheKey(vertexId1);
                    gn = persistentCache.getGraphNode(key);
                    if (gn == null) {
                        gn = new GraphNode(GraphNode.TYPE_VERTEX);
                        gn.setVertexId1(vertexId1);
                        gn.setLabel(resourceLabel);
                        persistentCache.putGraphNode(vertexId1, gn);
                    }

                    if (o.isLiteral()) {
                        String propName = uriLastNode(p.toString());
                        String propValue = o.getLiteral().getValue().toString();
                        String lang  = o.getLiteralLanguage();
                        gn.addProperty(propName, propValue);
                        if (isPopulated(lang)) {
                            gn.addProperty(propName + "_lang", lang);
                        }
                        tripleHandled = true;
                    }
                    else {
                        // TODO - implement logic?  No data encountered in this logic path.
                    }
                }
            }
            else {
                log("ERROR - unexpected triple, subj not a resource URI: " + triple);
            }

            log("triple_handled: " + tripleHandled + " " + triple);
            if (tripleHandled) {
                tripleHandledCount++;
            }
            else {
                tripleUnhandledCount++;
            }
        }
        catch (Exception e) {
            log("Exception on triple " + getTripleCount() + ": " + triple);
            e.printStackTrace();
        }
    }

    protected boolean isResourceUri(Node node) {

        if (node.isURI()) {
            if (hasOntologyPrefix(node.toString())) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    protected boolean hasOntologyPrefix(String uri) {

        if (prefixMap.containsKey(uri)) {
            return true;
        }
        else {
            if (uri.contains("#")) {
                String uriPrefix = ontologyPrefix(uri);
                if (prefixMap.containsKey(uriPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isPopulated(String s) {

        if (s == null) {
            return false;
        }
        return s.length() > 0;
    }

    /**
     * Return a value like "http://www.w3.org/2004/02/skos/core#"
     * from a URI value like "http://www.w3.org/2004/02/skos/core#prefLabel".
     * For use with checking vs the prefixMap keys.
     */
    protected String ontologyPrefix(String uri) {

        if (uri.contains("#")) {
            int poundIndex = uri.indexOf("#");
            return uri.substring(0, poundIndex + 1);
        }
        else {
            return uri;
        }
    }

    /**
     * Return a value like "q02b184e4-3542-4025-8fc6-dcd2c963d94e" from a given URI
     * like "http://data.cjoakim.org/resources/q02b184e4-3542-4025-8fc6-dcd2c963d94e"
     */
    protected String uriResourceId(String uri) {

        String normalized = normalizedUri(uri);

        int lastLashIdx = normalized.lastIndexOf("/");
        return normalized.substring(lastLashIdx + 1);
    }

    /**
     * Return a value like "resources" from a given URI like
     * "http://data.cjoakim.org/resources/q02b184e4-3542-4025-8fc6-dcd2c963d94e"
     */
    protected String uriResourceLabel(String uri) {

        String normalized = normalizedUri(uri);
        String[] tokens = normalized.split("/");
        return tokens[tokens.length - 2];
    }

    protected String normalizedUri(String uri) {

        if (uri.strip().endsWith("/")) {
            return removeLastChar(uri.strip());
        }
        else {
            return uri.strip();
        }
    }

    protected String removeLastChar(String s) {

        return (s == null || s.length() == 0)
                ? ""
                : (s.substring(0, s.length() - 1));
    }

    protected String uriLastNode(String uri) {

        String normalized = normalizedUri(uri);
        int lastLashIdx = normalized.lastIndexOf("/");
        return normalized.substring(lastLashIdx + 1);
    }

    /**
     * Return a value like "q02b184e4-3542-4025-8fc6-dcd2c963d94e" from a given URI
     * like "http://data.cjoakim.org/resources/q02b184e4-3542-4025-8fc6-dcd2c963d94e"
     */
    protected String edgeLabel(String s) {

        int poundIdx = s.lastIndexOf("#");
        if (poundIdx > 0) {
            return s.substring(poundIdx + 1);
        }
        return s;
    }

    protected void logNode(String type, Node node) {

        log("  logNode_" + type + ", isURI_" + node.isURI() + ", isLiteral_" + node.isLiteral() + " | " + node);

        if (node.isURI()) {
            log("    node getURI:    " + node.getURI());
            log("    node toString:  " + node.toString());
            log("    node ontPrefix: " + ontologyPrefix(node.getURI()));
        }

        else if (node.isLiteral()) {
            log("    node litValue: " + node.getLiteralValue());
            log("    node litLang:  " + node.getLiteralLanguage());
            log("    node litDT:    " + node.getLiteralDatatype().toString());
            log("    node litLex:   " + node.getLiteralLexicalForm());
        }

        if (type.equalsIgnoreCase("subj")) {
            if (node.isURI()) {
                if (hasOntologyPrefix(node.toString())) {
                    log("    subj_uri_onto:  " + node.toString());
                }
                else {
                    String id = uriResourceId(node.toString());
                    log("    subj_uri_other: " + node.toString() + " id: " + id);
                }
            }
            else {
                log("    subj_not_uri: " + node.toString());
            }
        }
    }

    protected void writeJsonObject(Object obj, String outfile) {

        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(obj);
            FileUtils.write(new File(outfile), json);
            log("file written: " + outfile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {

        System.out.println(msg);
    }
}
