<#
.SYNOPSIS
    Convert the raw RDF files into JSONLD format with the Apache Jena "riot" utility.
    Riot is an acronym for RDF I/O technology (RIOT).
    See https://jena.apache.org/documentation/io/
    Tomás Espinosa, Chris Joakim, Microsoft, January 2022
.DESCRIPTION

.OUTPUTS
    .json files.
#>

param(
    $inputPath = "H:\Data\"
)

$outputPath = $inputPath + "Out\"
$inputFileBaseNames = (Get-ChildItem -Path $inputPath | Where-Object { $_.Extension -match "ttl" }).BaseName

Remove-Item -Path $outputPath -Recurse -Force -ErrorAction SilentlyContinue
New-Item -Path $outputPath -ItemType Directory

foreach ($inputBaseNameFile in $inputFileBaseNames) {

    $inputFile = $inputPath + $inputBaseNameFile + ".ttl"
    $outputFile = $outputPath + $inputBaseNameFile + ".json"

    riot --out jsonld $inputFile > $outputFile

    Write-Verbose $outputFile -Verbose

}