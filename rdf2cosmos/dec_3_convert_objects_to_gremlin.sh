#!/bin/bash

# Convert the parsed/accumulated JSON objects a text file containing 
# "Groovy"/Gremlin for loading the data into CosmosDB.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

#export AZURE_RDF2COSMOS_CACHE_TYPE="local-disk" 
#export AZURE_RDF2COSMOS_CACHE_TYPE="az-postgresql" 

log_outfile="log/convert_objects_to_gremlin.txt"

echo 'removing groovy.txt output file'
rm $ddir/gremlin/groovy.txt

echo 'executing convert_objects_to_gremlin'
java -jar app/build/libs/app-uber.jar convert_objects_to_gremlin > $log_outfile

echo '=========='
echo 'see output file '$ddir/gremlin/groovy.txt

wc -l $ddir/gremlin/groovy.txt

echo 'done'
