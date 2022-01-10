package org.cjoakim.rdf2cosmos.gremlin;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.exception.ResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Instances of this class are used to load a CosmosDB/Graph/Gremlin database
 * froma text file with "groovy statements" per line.
 *
 * Chris Joakim, Microsoft, January 2022
 */

public class GremlinLoader {

    // Instance variables:
    private Cluster cluster = null;
    private Client  client  = null;

    public GremlinLoader() {

        super();
    }

    private void createClient() {

        log("createClient...");

        try {
            cluster = Cluster.build(new File("config/remote.yaml")).create();
            log("cluster created...");

            client  = cluster.connect();
            log("client created...");
        }
        catch (Exception ex) {
            log("ERROR execption encountered " + ex.getClass().getName() + " " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void load(String inputGroovyStatementsFilename) throws Exception {

        createClient();

        if (client == null) {
            log("ERROR eunable to create Gremlin Client object, exiting");
            return;
        }

        FileInputStream inputStream = null;
        Scanner sc = null;
        int lineNum = 0;

        try {
            inputStream = new FileInputStream(inputGroovyStatementsFilename);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                processGroovyStatement(++lineNum, sc.nextLine());
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        }
        catch (Exception ex) {
            log("ERROR execption encountered " + ex.getClass().getName() + " " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

        log("closing cluster...");
        cluster.close();
        log("cluster closed");
    }

    private void processGroovyStatement(int lineNum, String groovyStatement) {

        log("processGroovyStatement - lineNum: " + lineNum + " : " + groovyStatement);
        log("groovyStatement: " + groovyStatement);

        List<Result> resultList = null;

        try {
            ResultSet results = client.submit(groovyStatement);
            CompletableFuture<List<Result>> completableFutureResults;
            CompletableFuture<Map<String, Object>> completableFutureStatusAttributes;

            Map<String, Object> statusAttributes;

            completableFutureResults = results.all();
            completableFutureStatusAttributes = results.statusAttributes();
            resultList = completableFutureResults.get();
            statusAttributes = completableFutureStatusAttributes.get();
        }
        catch (ExecutionException | InterruptedException e) {
            log("ExecutionException on: " + groovyStatement);
            e.printStackTrace();
            return;
        }
        catch (Exception e) {
            log("Exception on: " + groovyStatement);
            ResponseException re = (ResponseException) e.getCause();

            // Response status codes. You can catch the 429 status code response and work on retry logic.
            System.out.println("Status code: " + re.getStatusAttributes().get().get("x-ms-status-code"));
            System.out.println("Substatus code: " + re.getStatusAttributes().get().get("x-ms-substatus-code"));

            // If error code is 429, this value will inform how many milliseconds you need to wait before retrying.
            System.out.println("Retry after (ms): " + re.getStatusAttributes().get().get("x-ms-retry-after"));

            // Total Request Units (RUs) charged for the operation, upon failure.
            System.out.println("Request charge: " + re.getStatusAttributes().get().get("x-ms-total-request-charge"));

            // ActivityId for server-side debugging
            System.out.println("ActivityId: " + re.getStatusAttributes().get().get("x-ms-activity-id"));
            throw(e);
        }

        for (Result result : resultList) {
            System.out.println("Query result: " + result.toString());
        }
    }

    private void log(String msg) {

        System.out.println(msg);
    }
}
