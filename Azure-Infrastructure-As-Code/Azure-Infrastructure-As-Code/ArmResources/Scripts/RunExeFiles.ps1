<#
.SYNOPSIS
    This script will install git
.DESCRIPTION
    This script will install git
.PARAMETER fileName
    A string of the name of the exe
.PARAMETER filesPath
    A path where all the .msi files are stored.
.OUTPUTS
    None.
#>
param (
    $executeList = "",
    $filesPath = "C:\AzureArmTemplates\"
)



function Main {

    $dictionary = @{ }
    $executeList = ($executeList).split(",")

    foreach ($pair in $executeList) {
        $key, $value = $pair.Split(';')
        $dictionary[$key] = $value
    }

    foreach ($command in $dictionary.GetEnumerator()) {
    
        $file = $filesPath + $command.Key
        $argList = $command.Value

        Start-Process -Wait $file -ArgumentList $argList
    }

}

Main