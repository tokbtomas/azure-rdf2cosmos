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
    //private HashMap<String, GraphNode> cache = null;
    private PersistentCache persistentCache = null;

    public Accumulator() {

        super();

        if (AppConfig.isAzurePostgresqlCacheType()) {
            persistentCache = new DiskCache();
            //persistentCache = new PostgresqlCache();    <-- TODO: uncomment later
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
    public synchronized void flushCache() {

        persistentCache.flushMemoryCache();
    }


    
    private void log(String msg) {
        
        System.out.println(msg);
    }
} 
