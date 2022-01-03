package org.cjoakim.rdf2cosmos;

import java.sql.*;
import java.util.*;

public class Cache {

    // Instance variables:
    Connection pgConnection = null;

    public static void main(String[] args) {

        Cache c = new Cache();
        c.getPgConnection();
        c.close();
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

                log("url:      " + props.get("url"));
                log("user:     " + props.get("user"));
                log("password: " + props.get("password"));

                pgConnection = DriverManager.getConnection(props.getProperty("url"), props);
                log("Database connection test: " + pgConnection.getCatalog());

                PreparedStatement readStatement = pgConnection.prepareStatement("SELECT node_key, node_type, data FROM cache1;");
                ResultSet resultSet = readStatement.executeQuery();
                long rowCount = 0;
                while (resultSet.next()) {
                    rowCount++;
                    String key  = resultSet.getString("node_key");
                    String type = resultSet.getString("node_type");
                    String data = resultSet.getString("data");
                    log("row: " + key + " | " + type +  " | " + data);
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
