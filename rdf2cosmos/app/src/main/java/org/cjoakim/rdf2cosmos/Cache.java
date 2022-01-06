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
    private boolean    connected;

    public Cache() {

        super();
        connected = connect();
    }

    public GraphNode getGraphNode(String key) throws Exception {

        if (!connected) {
            reconnect();
        }
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
            gn.setType(type);
            gn.setCreatedAt(created_at);
            gn.setUpdatedAt(updated_at);
            gn.setConvertedAt(converted_at);
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

    public boolean setConverted(GraphNode gn) throws Exception {

        long epoch = System.currentTimeMillis();
        gn.setConvertedAt(epoch);

        String sql= "update node_cache set updated_at = ?, converted_at = ? where key = ?";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setLong(1, epoch);
        stmt.setLong(2, epoch);
        stmt.setString(3, gn.getCacheKey());

        int count = stmt.executeUpdate();
        return (count > 0) ? true : false;
    }

    public ArrayList<GraphNode> getUnconverted(int limit) throws Exception {

        ArrayList<GraphNode> nodes = new ArrayList<GraphNode>();

        if (!connected) {
            reconnect();
        }
        String sql = "select type, data, created_at, updated_at, converted_at from node_cache where converted_at = 0 limit ?";
        PreparedStatement stmt = pgConnection.prepareStatement(sql);
        stmt.setInt(1, limit);

        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            String type       = resultSet.getString("type");
            String data       = resultSet.getString("data");
            long created_at   = resultSet.getLong("created_at");
            long updated_at   = resultSet.getLong("updated_at");
            long converted_at = resultSet.getLong("converted_at");

            GraphNode gn = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            gn = mapper.readValue(data, GraphNode.class);
            gn.setType(type);
            gn.setCreatedAt(created_at);
            gn.setUpdatedAt(updated_at);
            gn.setConvertedAt(converted_at);
            nodes.add(gn);
        }
        return nodes;
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
                return true;
            }
        }
        catch (SQLException e) {
            logException("unable to establish JDBC Connection or PreparedStatements", e);
            e.printStackTrace();
            return false;
        }
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

        log("=== deleteAll, count: " + c.deleteAll());

        GraphNode gn1 = new GraphNode(GraphNode.TYPE_VERTEX);
        gn1.setVertexId1("miles_" + System.currentTimeMillis());
        gn1.getCacheKey();
        gn1.addProperty("color", "black");
        gn1.addProperty("type", "tux");
        log(gn1.toJson());

        boolean b = c.keyExists(gn1.getCacheKey());
        log("=== keyExists: " + gn1.getCacheKey() + " -> " + b);

        log("persist gn1: " + c.persistGraphNode(gn1));

        b = c.keyExists(gn1.getCacheKey());
        log("=== keyExists: " + gn1.getCacheKey() + " -> " + b);

        log("=== getGraphNode");
        GraphNode gn2 = c.getGraphNode(gn1.getCacheKey());
        log(gn2.toJson());

        gn2.addProperty("birth_date", "2017-12-28");
        log("persist gn2: " + c.persistGraphNode(gn2));

        log("=== getGraphNode");
        GraphNode gn3 = c.getGraphNode(gn1.getCacheKey());
        log(gn3.toJson());

        ArrayList<GraphNode> nodes = c.getUnconverted(10);
        log("=== getUnconverted, count: " + nodes.size());

        for (int i = 0; i < nodes.size(); i++) {
            GraphNode u = nodes.get(i);
            log("=== setConverted, key: " + gn1.getCacheKey());
            c.setConverted(u);
        }

        nodes = c.getUnconverted(10);
        log("=== getUnconverted, count: " + nodes.size());

        log("=== deleteAll, count: " + c.deleteAll());

        c.close();
    }
}
