package org.cjoakim.rdf2cosmos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjoakim.rdf2cosmos.gremlin.GraphNode;

import java.sql.*;
import java.util.*;

/**
 * Abstract superclass of classes DiskCache and PostgresqlCache,
 * which override these methods as necessary.
 * 
 * Chris Joakim, Microsoft, January 2022
 */
public class Cache {

    public Cache() {

        super();
    }

    public GraphNode getGraphNode(String key) throws Exception {

        return null;
    }

    public boolean persistGraphNode(GraphNode gn) throws Exception {

        return false;
    }

    public boolean keyExists(String key) throws Exception {

        return false;
    }

    public boolean updateGraphNode(GraphNode gn) throws Exception { 

        return false;
    }

    public boolean insertGraphNode(GraphNode gn) throws Exception {

        return false;
    }

    public boolean setConverted(GraphNode gn) throws Exception {

        return false;
    }

    public ArrayList<GraphNode> getUnconverted(int limit) throws Exception {

        return null;  // subclasses should override
    }

    public long deleteAll() throws Exception {

        return 0;
    }

    public boolean reconnect() {

        return true;
    }

    public void close() {

        return;
    }
}
