package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Subclass of PersistentCache, implements local disk caching
 * of GraphNode objects.
 *
 * Chris Joakim, Microsoft, January 2022
 */
public class DiskCache extends PersistentCache {

    public DiskCache() {

        super();
    }

    public void flushMemoryCache() throws Exception {

        log("flushMemoryCache");
        Iterator<String> it = memoryCache.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            GraphNode gn = memoryCache.get(key);
            persistGraphNode(gn);
        }
        resetMemoryCache();
    }

    public GraphNode getGraphNode(String key) {

        GraphNode gn = memoryCache.get(key);  // first check the in-memory cache

        if (gn != null) {
            cacheHits ++;
        }
        else {
            // check disk-cache if not in memory
            cacheMisses++;
            String filename = AppConfig.getCacheFilename(key);
            try {
                log("cache_reading_file: " + filename);
                File file = new File(filename);
                if (file.exists()) {
                    cacheFileExists++;
                    String jstr = FileUtils.readFileToString(file, "UTF-8");
                    //log("jstr: " + jstr);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    gn = mapper.readValue(jstr, GraphNode.class);
                    if (gn != null) {
                        log("cache_key_repopulated: " + key);
                        memoryCache.put(key, gn);
                        cacheRepopulate++;
                    }
                }
                else {
                    cacheFileAbsent++;
                }
            }
            catch (IOException e) {
                cacheExceptions++;
                log("ERROR on_disk_cache_file: " + filename + " " + e.getMessage());
                e.printStackTrace();
            }
        }
        return gn;
    }

    public boolean persistGraphNode(GraphNode gn) throws Exception {

        String outfile = AppConfig.getCacheFilename(gn.getCacheKey());
        writeJsonObject(gn, outfile, false);
        return true;
    }

    public  long deleteAll() throws Exception {

        File cacheDirectory = new File(AppConfig.getCacheDirectory());
        String[] fileTypes  = new String[] {"json"};

        Collection<File> files = FileUtils.listFiles(cacheDirectory, fileTypes, false);
        Iterator<File> it = files.iterator();
        long count = 0;
        while(it.hasNext()) {
            File f = it.next();
            log("deleting file " + f.getAbsolutePath());
            FileUtils.delete(f);
            count++;
        }
        log("delete count " + count);
        return count;
    }

    public boolean reconnect() {

        return true;
    }

    public void close() {

        return;
    }

    public ArrayList<GraphNode> getUnconverted(String nodeType, int maxCount) throws Exception {

        return null;  // a no-op implementation in this class; see superclass
    }

    public boolean setConverted(GraphNode gn) throws Exception {

        return false;  // a no-op implementation in this class; see superclass
    }

    protected void log(String msg) {

        System.out.println("DiskCache: " + msg);
    }

}
