package org.cjoakim.rdf2cosmos;

import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.sql.*;
import java.util.*;

public class Cache {

    // Instance variables:
    private Connection        pgConnection;
    private PreparedStatement getNodeStatement;
    private PreparedStatement insertNodeStatement;
    private PreparedStatement updateNodeStatement;
    private PreparedStatement setConvertedStatement;
    private PreparedStatement getUnconvertedStatement;



    public GraphNode getGraphNode(String key) {

//        String sql = "SELECT key, type, data, created_at, updated_at, converted_at FROM node_cache;";
//        PreparedStatement readStatement = pgConnection.prepareStatement(sql);
//        ResultSet resultSet = readStatement.executeQuery();
//        long rowCount = 0;
//        while (resultSet.next()) {
//            rowCount++;
//            String key = resultSet.getString("key");
//            String type = resultSet.getString("type");
//            String data = resultSet.getString("data");
//            long created_at = resultSet.getLong("created_at");
//            long updated_at = resultSet.getLong("updated_at");
//            long converted_at = resultSet.getLong("converted_at");
//            log("row: " + key + " | " + type + " | " + data + " | " + created_at + " | " + updated_at + " | " + converted_at);
//        }
//        log("rows: " + rowCount);
        return null;
    }

    public boolean persistGraphNode(GraphNode gn) {

        return false;  //TODO
    }

    public boolean setConverted(GraphNode gn) {

        return false;  // TODO
    }

    public ArrayList<GraphNode> getUnconverted(ArrayList<String> keys) {

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

        pgConnection     = null;
        getNodeStatement = null;
        insertNodeStatement = null;
        updateNodeStatement = null;
        setConvertedStatement = null;
        getUnconvertedStatement = null;
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
                pgConnection.setAutoCommit(false);
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

        getNodeStatement        = pgConnection.prepareStatement(getNodeSQL);
        getUnconvertedStatement = pgConnection.prepareStatement(getUnconvertedSQL);
        insertNodeStatement     = pgConnection.prepareStatement(updateNodeSQL);
        updateNodeStatement     = pgConnection.prepareStatement(updateNodeSQL);
        setConvertedStatement   = pgConnection.prepareStatement(setConvertedSQL);

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
    public static void main(String[] args) {

        Cache c = new Cache();
        c.reconnect();
        c.close();
    }
}
