
# Convert the *.nt triple files into JSON objects for translation
# into Gremlin\"Groovy" and\or CSV content for loading into CosmosDB.
# Chris Joakim, Microsoft, January 2022

echo 'resetting data\cache directory ...'

$cache_dir = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\cache'
if (Test-Path $cache_dir) {
    Remove-Item $cache_dir -Recurse -Force
}

New-Item -ItemType directory -Path $cache_dir -Force 
New-Item -ItemType directory -Path log -Force 

$rdf_infile1 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcutsl.nt'
$rdf_infile2 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\cmt.nt'
$rdf_infile3 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\vamcu.nt'
$rdf_infile4 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\slbp.nt'

$log_outfile1="log\convert_nt_to_objects1.txt"
$log_outfile2="log\convert_nt_to_objects2.txt"
$log_outfile3="log\convert_nt_to_objects3.txt"
$log_outfile4="log\convert_nt_to_objects4.txt"

echo 'executing convert_rdf_to_objects with '$rdf_infile1
java -jar app\build\libs\app-uber.jar convert_rdf_to_objects $rdf_infile1 > $log_outfile1

echo 'executing convert_rdf_to_objects with '$rdf_infile2
java -jar app\build\libs\app-uber.jar convert_rdf_to_objects $rdf_infile2 > $log_outfile2

echo 'executing convert_rdf_to_objects with '$rdf_infile3
java -jar app\build\libs\app-uber.jar convert_rdf_to_objects $rdf_infile3 > $log_outfile3

echo 'executing convert_rdf_to_objects with '$rdf_infile4
java -jar app\build\libs\app-uber.jar convert_rdf_to_objects $rdf_infile4 > $log_outfile4

echo 'done'
