
# Convert the parsed\accumulated JSON objects a text file containing 
# "Groovy"\Gremlin for loading the data into CosmosDB.
# Chris Joakim, Microsoft, January 2022

$groovy_outfile = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\gremlin\groovy.txt'
$log_outfile="log\convert_objects_to_gremlin.txt"

if (Test-Path $groovy_outfile) {
    Remove-Item $groovy_outfile -Force
}

echo 'executing convert_objects_to_gremlin'
java -jar app\build\libs\app-uber.jar convert_objects_to_gremlin > $log_outfile

echo 'see output file '$groovy_outfile

echo 'done'
