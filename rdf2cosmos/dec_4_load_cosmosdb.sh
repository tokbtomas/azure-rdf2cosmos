#!/bin/bash

# Load CosmosDB with the transformed RDF graph data.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

infile=$ddir"/gremlin/groovy.txt"
log_outfile="log/load_cosmosdb_graph.txt"

rm $log_outfile

echo 'executing load_cosmosdb_graph'
java -jar app/build/libs/app-uber.jar load_cosmosdb_graph $infile > $log_outfile

cat $log_outfile

echo 'done'
