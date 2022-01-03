
# Convert the raw RDF files into *.nt format triples with the Apache Jena
# "riot" utility.  Riot is an acronym for RDF I\O technology (RIOT).
# See https://jena.apache.org/documentation/io/
# Chris Joakim, Microsoft, January 2022

$infile1  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcutsl.ttl'
$outfile1 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcutsl.nt'

$infile2  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\cmt.ttl'
$outfile2 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\cmt.nt'

$infile3  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\vamcu.ttl'
$outfile3 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\vamcu.nt'

$infile4  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\slbp.rdf'
$outfile4 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\slbp.nt'

echo 'mcutsl'
riot --out nt $infile1 > $outfile1

echo 'cmt'
riot --out nt $infile1 > $outfile1

echo 'vamcu'
riot --out nt $infile1 > $outfile1

echo 'slbp'
riot --out nt $infile1 > $outfile1

echo 'done'
