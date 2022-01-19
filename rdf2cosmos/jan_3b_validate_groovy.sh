#!/bin/bash

# Bash script to scan/validate the generated groovy.txt file,
# looking for mismatched vertices and edges, etc.
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR
infile=$ddir/gremlin/groovy.txt

log_outfile="log/validate_groovy.txt"

echo 'wc infile '$infile
wc $infile 

echo 'executing validate_groovy'
java -jar app/build/libs/app-uber.jar validate_groovy $infile > $log_outfile

echo 'done'
