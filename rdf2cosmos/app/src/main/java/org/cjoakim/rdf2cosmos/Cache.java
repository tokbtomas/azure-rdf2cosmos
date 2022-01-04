package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;
import org.cjoakim.rdf2cosmos.gremlin.Property;

import java.sql.*;
import java.util.*;

public class Cache {

    // Instance variables:
    private Connection pgConnection;

    public GraphNode getGraphNode(String key) throws Exception {

        String sql = "select type, data, created_at, updated_at, converted_at from node_cache where key = ? limit 1";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setString(1, key);

        ResultSet resultSet = stmt.executeQuery();
        GraphNode gn = null;

        while (resultSet.next()) {
            String type       = resultSet.getString("type");
            String data       = resultSet.getString("data");
            long created_at   = resultSet.getLong("created_at");
            long updated_at   = resultSet.getLong("updated_at");
            long converted_at = resultSet.getLong("converted_at");

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            gn = mapper.readValue(data, GraphNode.class);

//            gn = new GraphNode();
//            gn.setCacheKey(key);
//            gn.setType(type);
//            gn.setCreatedAt(created_at);
//            gn.setUpdatedAt(updated_at);
//            gn.setConvertedAt(converted_at);
        }
        return gn;
    }

    public boolean persistGraphNode(GraphNode gn) throws Exception {

        if (gn == null) {
            return false;
        }
        if (keyExists(gn.getCacheKey())) {
            return updateGraphNode(gn);
        }
        else {
            return insertGraphNode(gn);
        }
    }

    public boolean keyExists(String key) throws Exception {

        // select exists(select 1 from node_cache where key = 'key1')

        String sql = "select count(key) from node_cache where key = ?";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setString(1, key);

        ResultSet resultSet = stmt.executeQuery();
        int count = 0;
        while (resultSet.next()) {
           count = resultSet.getInt(1);
        }
        return (count > 0) ? true : false;
    }

    public boolean insertGraphNode(GraphNode gn) throws Exception {

        String sql= "insert into node_cache values (?, ?, ?, ?, ?, ?);";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setString(1, gn.getCacheKey());
        stmt.setString(2, gn.getType());
        stmt.setString(3, gn.toJson());
        stmt.setLong(4, System.currentTimeMillis());
        stmt.setLong(5, 0);
        stmt.setLong(6, 0);
        int count = stmt.executeUpdate();
        return (count > 0) ? true : false;
    }

    public boolean updateGraphNode(GraphNode gn) throws Exception {

        return false;  //TODO
    }

    public boolean setConverted(GraphNode gn) throws Exception {

        return false;  // TODO
    }

    public ArrayList<GraphNode> getUnconverted(ArrayList<String> keys) throws Exception {

        ArrayList<GraphNode> nodes = new ArrayList<GraphNode>();

        // TODO

        return nodes;
    }



    public boolean reconnect() {

        log("Cache reconnect ...");
        close();
        return connect();
    }

    public void close() {

        log("Cache close ...");
        try {
            if (pgConnection != null) {
                pgConnection.close();
                log("Cache connection closed");
            }
        }
        catch (SQLException e) {
            logException("unable to close JDBC Connection", e);
            e.printStackTrace();
        }

        pgConnection = null;
    }

    // private methods below

    /**
     * Create a JDBC Connection to the target Azure PostgreSQL database.
     * Return a boolean indicating success or failure to connect.
     */
    private boolean connect() {

        try {
            if (pgConnection != null) {
                return true;
            }
            else {
                // See https://docs.microsoft.com/en-us/azure/postgresql/connect-java
                long startMs = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append("jdbc:postgresql://");
                sb.append(AppConfig.getEnvVar("AZURE_PG_SERVER"));
                sb.append(".postgres.database.azure.com:5432/");
                sb.append(AppConfig.getEnvVar("AZURE_PG_DATABASE"));
                sb.append("?ssl=true&sslmode=require");

                Properties props = new Properties();
                props.put("url", sb.toString());
                props.put("user", AppConfig.getEnvVar("AZURE_PG_USER"));
                props.put("password", AppConfig.getEnvVar("AZURE_PG_PASS"));

                log("url:       " + props.get("url"));
                log("user:      " + props.get("user"));
                log("pw length: " + ((String) props.get("password")).length());

                pgConnection = DriverManager.getConnection(props.getProperty("url"), props);
                pgConnection.setAutoCommit(true);
                long elapsedMs = System.currentTimeMillis() - startMs;

                log("Database connection obtained in " + elapsedMs + " ms");

                return createPreparedStatements();
            }
        }
        catch (SQLException e) {
            logException("unable to establish JDBC Connection or PreparedStatements", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * All SQL for the application is defined in this method.
     */
    private boolean createPreparedStatements() throws SQLException {

        // The DDL for the node_cache table looks like this:
        //
        // CREATE TABLE "node_cache" (
        //   "key"          character varying(255) unique not null,
        //	 "type"         character varying(8) not null,
        //	 "data"         JSON not null,
        //	 "created_at"   bigint default 0,
        //	 "updated_at"   bigint default 0,
        //	 "converted_at" bigint default 0);

        log("Cache createPreparedStatements() ...");

        String getNodeSQL        = "select key, type, data, created_at, updated_at, converted_at from node_cache where key = ?";
        String getUnconvertedSQL = "select key, type, data, created_at, updated_at, converted_at from node_cache where type = ? and converted_at < 1 order by key";
        String insertNodeSQL     = "insert into node_cache values (?, ?, ?, ?, ?, ?);";
        String updateNodeSQL     = "update node_cache set data = ?, updated_at = ? where key = ?";
        String setConvertedSQL   = "update node_cache set converted_at = ? where key = ?";

//        getNodeStatement        = pgConnection.prepareStatement(getNodeSQL);
//        getUnconvertedStatement = pgConnection.prepareStatement(getUnconvertedSQL);
//        insertNodeStatement     = pgConnection.prepareStatement(updateNodeSQL);
//        updateNodeStatement     = pgConnection.prepareStatement(updateNodeSQL);
//        setConvertedStatement   = pgConnection.prepareStatement(setConvertedSQL);

        log("Cache createPreparedStatements() completed");
        return true;
    }

    private static void logException(String msg, Exception e) {

        log("Cache EXCEPTION: " + msg + " class: " + e.getClass().getCanonicalName() + " msg: " + e.getMessage());
    }

    private static void log(String msg) {

        System.out.println(msg);
    }

    /**
     * This method is used for ad-hoc testing and development only.
     */
    public static void main(String[] args) throws Exception {

        Cache c = new Cache();
        c.reconnect();

        log("=== getGraphNode");
        long t1 = System.currentTimeMillis();
        GraphNode gn0 = c.getGraphNode("key1");
        long t2 = System.currentTimeMillis();
        log(gn0.toJson());
        log("elapsed: " + (t2 - t1));

        GraphNode gn1a = new GraphNode(GraphNode.TYPE_VERTEX);
        gn1a.setVertexId1("miles_" + System.currentTimeMillis());
        gn1a.getCacheKey();
        gn1a.addProperty("color", "black");
        gn1a.addProperty("type", "tux");
        log(gn1a.toJson());

        log("=== insertGraphNode");
        boolean i = c.insertGraphNode(gn1a);
        log("insert count: " + i);

        log("key exists xxxx: " + c.keyExists("xxxx"));
        log("key exists key1: " + c.keyExists("key1"));

        c.close();
    }
}
