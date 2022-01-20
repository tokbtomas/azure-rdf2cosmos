<#
.SYNOPSIS
    This scripts takes a list of Environment Variable separated by a comma.
.DESCRIPTION
    This scripts takes a list of Environment Variable separated by a comma.
.PARAMETER accountList
    This is a list of paths that contains psm1 files
.OUTPUTS
    None.
#>
param (
    $EnvVariablesList = "JENA_HOME=H:\Apache-Jena\apache-jena-4.3.2;H:\Apache-Jena\apache-jena-4.3.2\bin,Path=H:\Apache-Jena\apache-jena-4.3.2\bat"
)

###########################################################################################################################################################
# Adds System Environment Variable 
###########################################################################################################################################################

$dictionary = @{ }
$EnvVariablesList = ($EnvVariablesList).split(",")

foreach ($pair in $EnvVariablesList) {
    $key, $value = $pair.Split('=')
    $dictionary[$key] = $value
}

foreach ($EnvVariable in $dictionary.GetEnumerator()) {

    Write-Verbose "Env. Variable: $($EnvVariable.key) with $($EnvVariable.value) variables" -verbose

    $currentValue = [Environment]::GetEnvironmentVariable($EnvVariable.key, "Machine")

    $currrentpPathList = $currentValue
    $addModulePath = $true

    if ($currentValue) {

        $currrentpPathList = $currentValue.Split(';')
        $newPaths = $EnvVariable.value.Split(';')

        foreach ($newPath in $newPaths) {

            foreach ($currentPath in $currrentpPathList ) {

                if ($currentPath -eq $newPath) {

                    $addModulePath = $false

                }
                if ($addModulePath) {
                    [Environment]::SetEnvironmentVariable($EnvVariable.key, $newPath + [System.IO.Path]::PathSeparator + $currentValue , "Machine")
                } 
            }

        }

    }
    else {
        [Environment]::SetEnvironmentVariable($EnvVariable.key, $EnvVariable.value, "Machine") 
    }

}