#!/bin/bash

# Load the CosmosDB/SQL database with the JSONLD data, doing only minor
# transformations to that data (adding id and pk attributes, etc)
# Chris Joakim, Microsoft, January 2022

# dotnet run bulk_load_jsonld_data <dbname> <container> <infile> --verbose --load

database="dev"
container="graph"
infile="Data/jsonld_files_list.txt"

date 

dotnet run bulk_load_jsonld_data $database $container $inPath --verbose --load

date
