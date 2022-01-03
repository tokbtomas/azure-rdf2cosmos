
# Convert the raw RDF files into *.nt format triples with the Apache Jena
# "riot" utility.  Riot is an acronym for RDF I\O technology (RIOT).
# See https:\\jena.apache.org\documentation\io\
# Chris Joakim, Microsoft, January 2022

$infile1  = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcutsl.ttl'
$outfile1 = Join-Path $env:AZURE_RDF2COSMOS_DATA_DIR '\raw\december\gdata\mcutsl.nt'

echo 'mcutsl'
riot --out nt $infile1 > $outfile1

# echo 'cmt'
# riot --out nt $ddir\raw\december\gdata\cmt.ttl > $ddir\raw\december\gdata\cmt.nt

# echo 'vamcu'
# riot --out nt $ddir\raw\december\gdata\vamcu.ttl > $ddir\raw\december\gdata\vamcu.nt

# echo 'slbp'
# riot --syntax n3 --out nt $ddir\raw\december\gdata\slbp.rdf > $ddir\raw\december\gdata\slbp.nt

echo 'done'
