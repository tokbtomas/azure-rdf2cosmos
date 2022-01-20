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
    $fileName = "Git-2.34.1-64.exe",
    $filesPath = "C:\AzureArmTemplates\"
)

function Main {

    $path = $filesPath + $fileName   

    Start-Process -Wait $path -ArgumentList /silent
   
}

Main