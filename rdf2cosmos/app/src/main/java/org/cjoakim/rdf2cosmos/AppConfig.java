package org.cjoakim.rdf2cosmos;

import java.nio.file.FileSystems;

/**
 * This class is the central point in the application for all configuration values,
 * such as environment variables, command-line arguments, and computed filesystem
 * locations.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class AppConfig {

    // Constants, environment variable names:
    public static final String AZURE_RDF2COSMOS_DATA_DIR            = "AZURE_RDF2COSMOS_DATA_DIR";
    public static final String AZURE_RDF2COSMOS_CACHE_TYPE          = "AZURE_RDF2COSMOS_CACHE_TYPE";  // local-disk or az-postgresql
    public static final String AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT = "AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT";

    public static final String AZURE_PG_SERVER                      = "AZURE_PG_SERVER";
    public static final String AZURE_PG_DATABASE                    = "AZURE_PG_DATABASE";
    public static final String AZURE_PG_USER                        = "AZURE_PG_USER";
    public static final String AZURE_PG_PASS                        = "AZURE_PG_PASS";

    public static final String CACHE_TYPE_LOCAL_DISK          = "local-disk";
    public static final String CACHE_TYPE_AZURE_POSTGRESQL    = "az-postgresql";
    public static final int    DEFAULT_MAX_OBJ_CACHE_COUNT    = 1000;

    // Class variables:
    private static String[] commandLineArgs = new String[0];

    public static void display(boolean extended) {

        log("AppConfig commandLineArgs.length: " + commandLineArgs.length);
        for (int i = 0; i < commandLineArgs.length; i++) {
            System.out.println("  arg " + i + " -> " + commandLineArgs[i]);
        }
        log("AppConfig getDataDirectory:               " + getDataDirectory());
        log("AppConfig getCacheDirectory:              " + getCacheDirectory());
        log("AppConfig getMaxObjectCacheCount:         " + getMaxObjectCacheCount());
        log("AppConfig cacheType:                      " + getCacheType());

        if (extended) {
            log("AppConfig isVerbose:                      " + isVerbose());
            log("AppConfig getEnvVar(USER):                " + getEnvVar("USER"));
            log("AppConfig getDataFileFqPath(x.json):      " + getDataFileFqPath("x.json"));
            log("AppConfig getCacheFilename(aaa):          " + getCacheFilename("aaa"));
            log("AppConfig metaFilename(mmm.json):         " + getMetaFilename("mmm.json"));
            log("AppConfig getGremlinFilename(groovy.txt): " + getGremlinFilename("groovy.txt"));
            log("AppConfig getTmpFilename(ttt.txt):        " + getTmpFilename("ttt.txt"));
        }
    }

    public static void setCommandLineArgs(String[] args) {

        if (args != null) {
            commandLineArgs = args;
        }
    }

    public static boolean isVerbose() {

        for (int i = 0; i < commandLineArgs.length; i++) {
            if (commandLineArgs[i].equalsIgnoreCase("--verbose")) {
                return true;
            }
        }
        return false;
    }

    public static String getEnvVar(String name) {

        return System.getenv(name);
    }

    public static String getDataDirectory() {

        return getEnvVar(AZURE_RDF2COSMOS_DATA_DIR);
    }

    public static String getCacheType() {

        String type = getEnvVar(AZURE_RDF2COSMOS_CACHE_TYPE);
        if (type == null) {
            return CACHE_TYPE_LOCAL_DISK;  // default
        }
        if (type.equalsIgnoreCase(CACHE_TYPE_AZURE_POSTGRESQL)) {
            return type;
        }
        return CACHE_TYPE_LOCAL_DISK;  // default
    }

    public static boolean isAzurePostgresqlCacheType() {

        String type = getCacheType();
        if (type == null) {
            return false;
        }
        return type.equalsIgnoreCase(CACHE_TYPE_AZURE_POSTGRESQL);
    }

    public static boolean isLocalDiskCacheType() {

        if (isAzurePostgresqlCacheType()) {
            return false;
        }
        return true;
    }

    public static String getAzurePostgresqlServer() {

        return getEnvVar(AZURE_PG_SERVER);
    }

    public static String getAzurePostgresqlDatabase() {

        return getEnvVar(AZURE_PG_DATABASE);
    }

    public static String getAzurePostgresqlUser() {

        return getEnvVar(AZURE_PG_USER);
    }

    public static String getAzurePostgresqlPassword() {

        return getEnvVar(AZURE_PG_PASS);
    }

    public static int getMaxObjectCacheCount() {

        try {
            return Integer.parseInt(getEnvVar(AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT));
        }
        catch (Exception e) {
            return DEFAULT_MAX_OBJ_CACHE_COUNT;
        }
    }

    public static String getDataFileFqPath(String path) {

        String sep = FileSystems.getDefault().getSeparator();

        if (path.startsWith(sep)) {
            return path;
        }
        if (path.startsWith("C:")) {
            return path;
        }
        else {
            return getDataDirectory() + sep + path;
        }
    }

    public static String getCacheDirectory() {

        String sep = FileSystems.getDefault().getSeparator();

        StringBuffer sb = new StringBuffer();
        sb.append(getDataDirectory());
        sb.append(sep);
        sb.append("cache");
        return sb.toString();
    }

    public static String getCacheFilename(String key) {

        String sep = FileSystems.getDefault().getSeparator();

        StringBuffer sb = new StringBuffer();
        sb.append(getCacheDirectory());
        sb.append(sep);
        sb.append(key);
        sb.append(".json");
        return sb.toString();
    }

    public static String getGremlinFilename(String basename) {

        String sep = FileSystems.getDefault().getSeparator();

        StringBuffer sb = new StringBuffer();
        sb.append(getDataDirectory());
        sb.append(sep);
        sb.append("gremlin");
        sb.append(sep);
        sb.append(basename);
        return sb.toString();
    }

    public static String getMetaFilename(String basename) {

        String sep = FileSystems.getDefault().getSeparator();

        StringBuffer sb = new StringBuffer();
        sb.append(getDataDirectory());
        sb.append(sep);
        sb.append("meta");
        sb.append(sep);
        sb.append(basename);
        return sb.toString();
    }

    public static String getTmpFilename(String basename) {

        String sep = FileSystems.getDefault().getSeparator();

        StringBuffer sb = new StringBuffer();
        sb.append(getDataDirectory());
        sb.append(sep);
        sb.append("tmp");
        sb.append(sep);
        sb.append(basename);
        return sb.toString();
    }

    public static void main(String[] args) {

        commandLineArgs = args;
        display(false);
    }

    private static void log(String msg) {

        System.out.println(msg);
    }
}
