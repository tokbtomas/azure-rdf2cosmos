#!/bin/bash

# Convert the *.nt triple files into JSON objects for translation
# into Gremlin/"Groovy" and/or CSV content for loading into CosmosDB.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

# export AZURE_RDF2COSMOS_CACHE_TYPE="local-disk" 
# export AZURE_RDF2COSMOS_CACHE_TYPE="az-postgresql" 

mkdir -p log
mkdir -p $ddir/cache

echo 'executing clear_cache with '$AZURE_RDF2COSMOS_CACHE_TYPE
java -jar app/build/libs/app-uber.jar clear_cache $AZURE_RDF2COSMOS_CACHE_TYPE

echo 'pausing 10 seconds after clear_cache ...'
sleep 10

rdf_infile3=$ddir"/raw/december/gdata/cmt_mini.nt"

log_outfile3="log/convert_nt_to_objects3_mini.txt"

echo 'executing convert_rdf_to_objects with '$rdf_infile3
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile3 > $log_outfile3

echo '===================='
tail -12 $log_outfile3

echo 'done'
