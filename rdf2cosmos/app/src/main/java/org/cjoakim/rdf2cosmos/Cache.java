package org.cjoakim.rdf2cosmos;

import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.sql.*;
import java.util.*;

public class Cache {

    // Instance variables:
    private Connection pgConnection = null;
    private PreparedStatement getStatement = null;
    private PreparedStatement persistStatement = null;
    private PreparedStatement setConvertedStatement = null;
    private PreparedStatement getConvertedStatement = null;

    public static void main(String[] args) {

        Cache c = new Cache();
        c.getPgConnection();
        c.close();
    }

    public GraphNode getGraphNode(String key) {

        return null;  //TODO
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


    private boolean reconnect() {

        // TODO - establish pgConnection, and PreparedStatements

        return false;
    }

    private Connection getPgConnection() {

        try {
            if (pgConnection != null) {
                return pgConnection;
            }
            else {
                // See https://docs.microsoft.com/en-us/azure/postgresql/connect-java
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
                log("Database connection test: " + pgConnection.getCatalog());

                String sql = "SELECT key, type, data, created_at, updated_at, converted_at FROM node_cache;";
                PreparedStatement readStatement = pgConnection.prepareStatement(sql);
                ResultSet resultSet = readStatement.executeQuery();
                long rowCount = 0;
                while (resultSet.next()) {
                    rowCount++;
                    String key  = resultSet.getString("key");
                    String type = resultSet.getString("type");
                    String data = resultSet.getString("data");
                    long   created_at   = resultSet.getLong("created_at");
                    long   updated_at   = resultSet.getLong("updated_at");
                    long   converted_at = resultSet.getLong("converted_at");
                    log("row: " + key + " | " + type +  " | " + data + " | " + created_at + " | " + updated_at +  " | " + converted_at);
                }
                log("rows: " + rowCount);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return pgConnection;
    }

    public void close() {

        log("closing pgConnection...");
        try {
            if (pgConnection != null) {
                pgConnection.close();
                log("pgConnection closed");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void log(String msg) {

        System.out.println(msg);
    }
}
