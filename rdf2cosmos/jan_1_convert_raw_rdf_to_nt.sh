#!/bin/bash

# Convert the raw RDF files into *.nt format triples with the Apache Jena
# "riot" utility.  Riot is an acronym for RDF I/O technology (RIOT).
# See https://jena.apache.org/documentation/io/
# Chris Joakim, Microsoft, January 2022

ddir=$AZURE_RDF2COSMOS_DATA_DIR  # ddir is a shorthand abbreviation for AZURE_RDF2COSMOS_DATA_DIR

echo 'vamcu'
riot --out nt $ddir/raw/january/gdata/vamcu.ttl > $ddir/raw/january/gdata/vamcu.nt
head -3 $ddir/raw/january/gdata/vamcu.nt

echo 'cmt'
riot --out nt $ddir/raw/january/gdata/cmt.ttl > $ddir/raw/january/gdata/cmt.nt
head -3 $ddir/raw/january/gdata/cmt.nt

echo 'mcutsl'
riot --out nt $ddir/raw/january/gdata/mcutsl.ttl > $ddir/raw/january/gdata/mcutsl.nt
head -3 $ddir/raw/january/gdata/mcutsl.nt

echo 'done'
