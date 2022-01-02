#!/bin/bash

# Convert the *.nt triple files into JSON objects for translation
# into Gremlin/"Groovy" and/or CSV content for loading into CosmosDB.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

mkdir -p log

echo 'resetting data/cache directory ...'
rm -rf   $ddir/cache
mkdir -p $ddir/cache

rdf_infile1=$ddir"/raw/december/gdata/slbp.nt"
rdf_infile2=$ddir"/raw/december/gdata/vamcu.nt"
rdf_infile3=$ddir"/raw/december/gdata/cmt.nt"
rdf_infile4=$ddir"/raw/december/gdata/mcutsl.nt"

log_outfile1="log/convert_rdf_to_objects1.txt"
log_outfile2="log/convert_rdf_to_objects2.txt"
log_outfile3="log/convert_rdf_to_objects3.txt"
log_outfile4="log/convert_rdf_to_objects4.txt"

echo 'executing convert_rdf_to_objects with '$rdf_infile1
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile1 > $log_outfile1

echo 'executing convert_rdf_to_objects with '$rdf_infile2
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile2 > $log_outfile2

echo 'executing convert_rdf_to_objects with '$rdf_infile3
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile3 --verbose > $log_outfile3

echo 'executing convert_rdf_to_objects with '$rdf_infile4
java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile4 --verbose > $log_outfile4

echo '===================='
tail -12 $log_outfile1

echo '===================='
tail -12 $log_outfile2

echo '===================='
tail -12 $log_outfile3

echo '===================='
tail -12 $log_outfile4

echo 'number of files in data/cache directory:'
ls -al $ddir/cache | wc -l 

echo 'done'
