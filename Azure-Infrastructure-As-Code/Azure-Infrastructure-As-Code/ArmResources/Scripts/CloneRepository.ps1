<#
.SYNOPSIS
    This script will install git
.DESCRIPTION
    This script will install git
.PARAMETER repositoryName
    A string of the name of the exe
.PARAMETER filesPath
    A path where all the files are stored.
.OUTPUTS
    None.
#>
param (
    $filesPath = "H:\SourceCode\",    
    $repositoryName = "RDFToCosmosTool",
    $uri = "https://github.com/cjoakim/azure-rdf2cosmos.git"
)

function Set-LocalDirectory {
    param (
        $path
    )

    if (-Not (Test-Path $path)) {

        New-Item -Path $path -ItemType directory
    }
    Set-NTFSPermissions -path $path
}

function Set-NTFSPermissions {
    param (
        $path
    )

    Add-NTFSAccess -Path $path -Account "CREATOR OWNER" -AccessRights "FullControl" -InheritanceFlags ContainerInherit, ObjectInherit -PropagationFlags None -AccessType Allow
    Add-NTFSAccess -Path $path -Account "BUILTIN\Users" -AccessRights "ReadAndExecute" -InheritanceFlags ContainerInherit, ObjectInherit -PropagationFlags None -AccessType Allow
    Add-NTFSAccess -Path $path -Account "NT AUTHORITY\SYSTEM" -AccessRights "FullControl" -InheritanceFlags ContainerInherit, ObjectInherit -PropagationFlags None -AccessType Allow
    Add-NTFSAccess -Path $path -Account "BUILTIN\Administrators" -AccessRights "FullControl" -InheritanceFlags ContainerInherit, ObjectInherit -PropagationFlags None -AccessType Allow

}

function Get-Repository {
    param (
        $uri,
        $destinationPath
    )
    if (Test-Path $destinationPath ) {

        Remove-Item -Path $destinationPath -Recurse -Force     

    }

    powershell.exe "git clone $uri $destinationPath"
}

function Main {

    Write-EventLog -LogName Application -source AzureArmTemplates -eventID 1000 -entrytype Information -message "Starting to Clone Repository"
    Set-LocalDirectory -path $filesPath
    $destinationPath = $filesPath + $repositoryName
    Get-Repository -uri $uri -destinationPath $destinationPath
}

Main