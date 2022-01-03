
# Load CosmosDB with the transformed RDF graph data.
# Chris Joakim, Microsoft, January 2022

$groovy_infile = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\gremlin\groovy.txt'
$log_outfile="log\load_cosmosdb.txt"

echo 'executing load_cosmosdb_graph with infile: '$groovy_infile
java -jar app\build\libs\app-uber.jar load_cosmosdb_graph $groovy_infile > $log_outfile

echo 'done'
