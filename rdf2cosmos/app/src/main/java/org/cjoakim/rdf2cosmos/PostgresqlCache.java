package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Concrete subclass of class Cache, implemented with Azure PostgreSQL
 * as the persistence mechanism.
 * 
 * Chris Joakim, Microsoft, January 2022
 */

public class PostgresqlCache extends PersistentCache {

    // Instance variables:
    private Connection pgConnection;
    private boolean    connected;

    public PostgresqlCache() {

        super();
        connected = connect();
    }

    public void flushMemoryCache() throws Exception {

        Iterator<String> it = memoryCache.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            GraphNode gn = memoryCache.get(key);
            persistGraphNode(gn);
        }
        resetMemoryCache();
    }

    public GraphNode getGraphNode(String key) throws Exception {

        GraphNode gn = memoryCache.get(key);  // first check the in-memory cache

        if (gn != null) {
            cacheHits++;
            return gn;
        }
        else {
            if (!connected) {
                reconnect();
            }
            String sql = "select type, data, created_at, updated_at, converted_at from node_cache where key = ? limit 1";
            PreparedStatement stmt = pgConnection.prepareStatement(sql);
            stmt.setString(1, key);

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String type       = resultSet.getString("type");
                String data       = resultSet.getString("data");
                long created_at   = resultSet.getLong("created_at");
                long updated_at   = resultSet.getLong("updated_at");
                long converted_at = resultSet.getLong("converted_at");

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                gn = mapper.readValue(data, GraphNode.class);
                gn.setType(type);
                gn.setCreatedAt(created_at);
                gn.setUpdatedAt(updated_at);
                gn.setConvertedAt(converted_at);
            }
        }
        return gn;
    }

    public boolean persistGraphNode(GraphNode gn) throws Exception {

        if (gn == null) {
            return false;
        }
        if (isKeyPersisted(gn.getCacheKey())) {
            return updateGraphNode(gn);
        }
        else {
            return insertGraphNode(gn);
        }
    }

    public boolean isKeyPersisted(String key) throws Exception {

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

    public boolean updateGraphNode(GraphNode gn) throws Exception {

        long epoch = System.currentTimeMillis();
        gn.setUpdatedAt(epoch);

        String sql= "update node_cache set data = ?, updated_at = ? where key = ?";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setString(1, gn.toJson());
        stmt.setLong(2, epoch);
        stmt.setString(3, gn.getCacheKey());

        int count = stmt.executeUpdate();
        return (count > 0) ? true : false;
    }

    public boolean insertGraphNode(GraphNode gn) throws Exception {

        long epoch = System.currentTimeMillis();
        gn.setCreatedAt(epoch);
        gn.setUpdatedAt(epoch);

        String sql= "insert into node_cache values (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setString(1, gn.getCacheKey());
        stmt.setString(2, gn.getType());
        stmt.setString(3, gn.toJson());
        stmt.setLong(4, epoch);
        stmt.setLong(5, epoch);
        stmt.setLong(6, 0);
        int count = stmt.executeUpdate();
        return (count > 0) ? true : false;
    }

    public long deleteAll() throws Exception {

        String sql= "delete from node_cache where created_at > ? and created_at < ?";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setLong(1, Long.MIN_VALUE);
        stmt.setLong(2, Long.MAX_VALUE);
        return stmt.executeUpdate();
    }

    public boolean reconnect() {

        log("Cache reconnect ...");
        close();
        connected = connect();
        return connected;
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
        connected = false;
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
                sb.append(AppConfig.getAzurePostgresqlServer());
                sb.append(".postgres.database.azure.com:5432/");
                sb.append(AppConfig.getAzurePostgresqlDatabase());
                sb.append("?ssl=true&sslmode=require");

                Properties props = new Properties();
                props.put("url", sb.toString());
                props.put("user", AppConfig.getAzurePostgresqlUser());
                props.put("password", AppConfig.getAzurePostgresqlPassword());

                log("url:       " + props.get("url"));
                log("user:      " + props.get("user"));
                log("pw length: " + ((String) props.get("password")).length());

                pgConnection = DriverManager.getConnection(props.getProperty("url"), props);
                pgConnection.setAutoCommit(true);
                long elapsedMs = System.currentTimeMillis() - startMs;

                log("Database connection obtained in " + elapsedMs + " ms");
                return true;
            }
        }
        catch (SQLException e) {
            logException("unable to establish JDBC Connection or PreparedStatements", e);
            e.printStackTrace();
            return false;
        }
    }



    private static void loghh(String msg) {

        System.out.println(msg);
    }

    /**
     * This method is used for ad-hoc testing and development only.
     */
    public static void main(String[] args) throws Exception {

        PostgresqlCache c = new PostgresqlCache();
        c.reconnect();

        System.out.println("=== deleteAll, count: " + c.deleteAll());

        GraphNode gn1 = new GraphNode(GraphNode.TYPE_VERTEX);
        gn1.setVertexId1("miles_" + System.currentTimeMillis());
        gn1.getCacheKey();
        gn1.addProperty("color", "black");
        gn1.addProperty("type", "tux");
        System.out.println(gn1.toJson());

        boolean b = c.isKeyPersisted(gn1.getCacheKey());
        System.out.println("=== keyExists: " + gn1.getCacheKey() + " -> " + b);

        System.out.println("persist gn1: " + c.persistGraphNode(gn1));

        b = c.isKeyPersisted(gn1.getCacheKey());
        System.out.println("=== keyExists: " + gn1.getCacheKey() + " -> " + b);

        System.out.println("=== getGraphNode");
        GraphNode gn2 = c.getGraphNode(gn1.getCacheKey());
        System.out.println(gn2.toJson());

        gn2.addProperty("birth_date", "2017-12-28");
        System.out.println("persist gn2: " + c.persistGraphNode(gn2));

        System.out.println("=== getGraphNode");
        GraphNode gn3 = c.getGraphNode(gn1.getCacheKey());
        System.out.println(gn3.toJson());

        System.out.println("=== deleteAll, count: " + c.deleteAll());

        c.close();
    }
}
