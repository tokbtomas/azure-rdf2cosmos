package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract superclass of classes DiskCache and PostgresqlCache,
 * which override these methods as necessary.
 *
 * In-memory caching is implemented in this common superclass, but the
 * accesss to the persistence mechanism is implemented in the subclasses.
 * 
 * Chris Joakim, Microsoft, January 2022
 */
public abstract class PersistentCache {

    // Instance variables:
    protected HashMap<String, GraphNode> memoryCache = null;
    protected long cacheHits = 0;
    protected long cacheMisses = 0;
    protected long cacheFileExists = 0;
    protected long cacheFileAbsent = 0;
    protected long cacheRepopulate = 0;
    protected long cacheExceptions = 0;
    protected int maxObjectCacheCount = 1000;

    public PersistentCache() {

        super();
        maxObjectCacheCount = AppConfig.getMaxObjectCacheCount();
        resetMemoryCache();
    }

    // Subclasses must implement these abstract methods:

    public abstract void flushMemoryCache() throws Exception;

    public abstract GraphNode getGraphNode(String key) throws Exception;

    public abstract boolean persistGraphNode(GraphNode gn) throws Exception;

    public abstract boolean reconnect();

    public abstract long deleteAll() throws Exception;

    public abstract ArrayList<GraphNode> getUnconverted(String nodeType, int maxCount) throws Exception;

    public abstract boolean setConverted(GraphNode gn) throws Exception;

    public abstract void close();

    public void putGraphNode(String key, GraphNode gn) throws Exception {

        if (key != null) {
            if (gn != null) {
                memoryCache.put(key, gn);
            }
        }
    }

    public void flushMemoryCacheIfNecessary() throws Exception {

        if (memoryCache.size() >= maxObjectCacheCount) {
            flushMemoryCache();
            resetMemoryCache();
        }
    }

    protected void resetMemoryCache() {

        memoryCache = new HashMap<String, GraphNode>();
    }

    protected void writeJsonObject(Object obj, String outfile, boolean pretty) {

        try {
            ObjectMapper mapper = null;
            if (pretty) {
                mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            }
            else {
                mapper = new ObjectMapper();
            }
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            String json = mapper.writeValueAsString(obj);
            FileUtils.write(new File(outfile), json);
            log("file written: " + outfile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eojLogging() {

        log("Accumulator_eoj cache_hits:        " + cacheHits);
        log("Accumulator_eoj cache_misses:      " + cacheMisses);
        log("Accumulator_eoj cache_file_exists: " + cacheFileExists);
        log("Accumulator_eoj cache_file_absent: " + cacheFileAbsent);
        log("Accumulator_eoj cache_repopulated: " + cacheRepopulate);
        log("Accumulator_eoj cache_exceptions:  " + cacheExceptions);
    }

    protected void log(String msg) {

        System.out.println(msg);
    }

    protected void logException(String msg, Exception e) {

        log("Cache EXCEPTION: " + msg + " class: " + e.getClass().getCanonicalName() + " msg: " + e.getMessage());
    }
}
