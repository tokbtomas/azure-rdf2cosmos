#!/bin/bash

# Convert the raw RDF files into JSONLD format with the Apache Jena "riot" utility.
# Riot is an acronym for RDF I/O technology (RIOT).
# See https://jena.apache.org/documentation/io/
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR


echo 'start date/time:'
date 

echo 'Removing files in the output directory ...'
rm $ddir/jsonld/*.*

echo 'YOUR-FILENAME'
riot --out jsonld $ddir/raw/YOUR-FILENAME.ttl > $ddir/jsonld/YOUR-FILENAME.json

echo 'List of the output jsonld files:'
ls -al $ddir/jsonld/

echo 'finish date/time:'
date 

echo 'done'

