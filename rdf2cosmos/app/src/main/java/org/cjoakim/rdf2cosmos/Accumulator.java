package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The purpose of this class is to accumulate changes to the state of the
 * parsed GraphNodes as a (potentially very large) RDF (ttl, nt, etc.) file
 * is being read and processed in the migration process.
 *
 * The implementation accumulates the GraphNodes into an in-memory cache.
 * However, the cache entries can be flushed to disk, and also be selectively reloaded,
 * so as to support huge input files.
 *
 * One reason to implement an Accumulator is that the input RDF files may or
 * may not be sorted properly.  The nature of the triple files is that they
 * are independent datapoints, while the Gremlin Vertices and Edges are aggregates.
 *
 * The GraphNodes can be later converted to "groovy" (i.e. - Gremlin) syntax for loading
 * into CosmosDB.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class Accumulator {

    // Instance variables:
    private HashMap<String, GraphNode> cache = null;
    private PersistentCache persistentCache = null;

    public Accumulator() {

        super();

        if (AppConfig.isAzurePostgresqlCacheType()) {
            persistentCache = new PostgresqlCache();
        }
        else {
            persistentCache = new DiskCache();
        }
    }

    public PersistentCache getPersistentCache() {

        return persistentCache;
    }

    public void setPersistentCache(PersistentCache persistentCache) {

        this.persistentCache = persistentCache;
    }

    //    public boolean containsKey(String key) {
//
//        return cache.containsKey(key);
//    }

//    public void putGraphNode(String key, GraphNode gn) {
//
//        this.cache.put(key, gn);
//    }

    /**
     * Flush the cache to the sink (i.e. - disk) if the cache size is greater
     * than the given maxItemCount.
     */
    public synchronized void flushCache(int maxItemCount) {

        persistentCache.flushMemoryCache(maxItemCount);

//        if (cache.size() > maxItemCount) {
//            log("flushCache size: " + cache.size() + " max: " + maxItemCount);
//            flushCacheToDisk();  // disk is the only sink at this time. add blob storage, etc., later if needed
//        }
    }

//    /**
//     * Write the cache entries to JSON files on disk.
//     * Current impl simply writes to one JSON file.
//     */
//    private void flushCacheToDisk() {
//
//        log("flushCacheToDisk");
//        Iterator<String> it = cache.keySet().iterator();
//        while (it.hasNext()) {
//            String key = it.next();
//            GraphNode gn = cache.get(key);
//            String outfile = AppConfig.getCacheFilename(gn.getCacheKey());
//            writeJsonObject(gn, outfile, false);
//        }
//        resetCache();
//        return;
//    }

//    protected void writeJsonObject(Object obj, String outfile, boolean pretty) {
//
//        try {
//            ObjectMapper mapper = null;
//            if (pretty) {
//                mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//            }
//            else {
//                mapper = new ObjectMapper();
//            }
//            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//            mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
//            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
//            String json = mapper.writeValueAsString(obj);
//            FileUtils.write(new File(outfile), json);
//            log("file written: " + outfile);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void eojLogging() {

        log("Accumulator_eoj cache_hits:        " + persistentCache.cacheHits);
        log("Accumulator_eoj cache_misses:      " + persistentCache.cacheMisses);
        log("Accumulator_eoj cache_file_exists: " + persistentCache.cacheFileExists);
        log("Accumulator_eoj cache_file_absent: " + persistentCache.cacheFileAbsent);
        log("Accumulator_eoj cache_repopulated: " + persistentCache.cacheRepopulate);
        log("Accumulator_eoj cache_exceptions:  " + persistentCache.cacheExceptions);
    }
    
    private void log(String msg) {
        
        System.out.println(msg);
    }
} 
