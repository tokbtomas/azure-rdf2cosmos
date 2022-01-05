
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

$infile5  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcma_v1.ttl'
$outfile5 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcma.nt'

# echo 'mcutsl '$infile1
# riot --out nt $infile1 > $outfile1

# echo 'cmt '$infile2
# riot --out nt $infile2 > $outfile2

# echo 'vamcu '$infile3
# riot --out nt $infile3 > $outfile3

# echo 'slbp '$infile4
# riot --syntax n3 --out nt $infile4 > $outfile4

echo 'mcma '$infile5
riot --out nt $infile5 > $outfile5

echo 'done'
