#!/bin/bash

# Convert the *.nt triple files into JSON objects for translation
# into Gremlin/"Groovy" and/or CSV content for loading into CosmosDB.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

#export AZURE_RDF2COSMOS_CACHE_TYPE="local-disk" 
#export AZURE_RDF2COSMOS_CACHE_TYPE="az-postgresql" 

mkdir -p log

# echo 'resetting data/cache directory ...'
# rm -rf   $ddir/cache
mkdir -p $ddir/cache

echo 'executing clear_cache with '$AZURE_RDF2COSMOS_CACHE_TYPE
java -jar app/build/libs/app-uber.jar clear_cache $AZURE_RDF2COSMOS_CACHE_TYPE

echo 'pausing 10 seconds after clear_cache ...'
sleep 10

rdf_infile2=$ddir"/raw/january/gdata/vamcu.nt"
rdf_infile3=$ddir"/raw/january/gdata/cmt.nt"
rdf_infile4=$ddir"/raw/january/gdata/mcutsl.nt"

log_outfile2="log/convert_nt_to_objects2.txt"
log_outfile3="log/convert_nt_to_objects3.txt"
log_outfile4="log/convert_nt_to_objects4.txt"

echo 'executing convert_rdf_to_objects with '$rdf_infile2
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile2 > $log_outfile2

echo 'executing convert_rdf_to_objects with '$rdf_infile3
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile3 > $log_outfile3

echo 'executing convert_rdf_to_objects with '$rdf_infile4
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile4 > $log_outfile4

echo '===================='
tail -12 $log_outfile2

echo '===================='
tail -12 $log_outfile3

echo '===================='
tail -12 $log_outfile4

echo 'done'
