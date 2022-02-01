#!/bin/bash

# $ tree -d -L 4
# ├── DotnetConsoleApp
# │   ├── Data
# │   │   ├── jsonld
# │   │   └── raw
# │   ├── bin
# │   │   └── Debug
# │   │       ├── net5.0
# │   │       └── net6.0
# │   ├── obj
# │   │   └── Debug
# │   │       ├── net5.0
# │   │       └── net6.0
# │   ├── queries
# │   └── tmp
# └── img

tarfile="DotnetConsoleApp_code.tar"

rm $tarfile

echo ''
echo 'creating tar file ...'
tar cfv $tarfile \
    DotnetConsoleApp/*.csproj \
    DotnetConsoleApp/*.cs \
    DotnetConsoleApp/*.cs \
    DotnetConsoleApp/*.sh \
    DotnetConsoleApp/*.ps1 \
    DotnetConsoleApp/queries/*.txt \

echo ''
echo 'listing the contents the tar file ...'
tar tvf $tarfile

echo 'done'

# Partial script output:
# listing the contents the tar file ...
# -rw-r--r--  0 cjoakim staff     395 Jan 25 17:12 DotnetConsoleApp/DotnetConsoleApp.csproj
# -rw-r--r--  0 cjoakim staff    4532 Jan 27 09:52 DotnetConsoleApp/Config.cs
# -rw-r--r--  0 cjoakim staff    8955 Jan 25 17:24 DotnetConsoleApp/CosmosAdminUtil.cs
# -rw-r--r--  0 cjoakim staff    1286 Jan 25 17:24 DotnetConsoleApp/CosmosBaseUtil.cs
# -rw-r--r--  0 cjoakim staff    1924 Jan 25 17:24 DotnetConsoleApp/CosmosClientFactory.cs
# -rw-r--r--  0 cjoakim staff    3661 Jan 25 17:24 DotnetConsoleApp/CosmosQueryUtil.cs
# -rw-r--r--  0 cjoakim staff     717 Jan 25 17:24 DotnetConsoleApp/GenericDocument.cs
# -rw-r--r--  0 cjoakim staff   21031 Jan 27 17:45 DotnetConsoleApp/Program.cs
# -rw-r--r--  0 cjoakim staff    2019 Jan 25 17:24 DotnetConsoleApp/QueryResponse.cs
# -rw-r--r--  0 cjoakim staff    4532 Jan 27 09:52 DotnetConsoleApp/Config.cs
# -rw-r--r--  0 cjoakim staff    8955 Jan 25 17:24 DotnetConsoleApp/CosmosAdminUtil.cs
# -rw-r--r--  0 cjoakim staff    1286 Jan 25 17:24 DotnetConsoleApp/CosmosBaseUtil.cs
# -rw-r--r--  0 cjoakim staff    1924 Jan 25 17:24 DotnetConsoleApp/CosmosClientFactory.cs
# -rw-r--r--  0 cjoakim staff    3661 Jan 25 17:24 DotnetConsoleApp/CosmosQueryUtil.cs
# -rw-r--r--  0 cjoakim staff     717 Jan 25 17:24 DotnetConsoleApp/GenericDocument.cs
# -rw-r--r--  0 cjoakim staff   21031 Jan 27 17:45 DotnetConsoleApp/Program.cs
# -rw-r--r--  0 cjoakim staff    2019 Jan 25 17:24 DotnetConsoleApp/QueryResponse.cs
# -rwxr-xr-x  0 cjoakim staff    2865 Jan 25 16:05 DotnetConsoleApp/1_convert_raw_rdf_to_jsonld.sh
# -rwxr-xr-x  0 cjoakim staff     437 Jan 27 15:59 DotnetConsoleApp/2_load_cosmosdb_sql.sh
# -rwxr-xr-x  0 cjoakim staff     876 Jan 27 13:14 DotnetConsoleApp/1_convert_raw_rdf_to_jsonld.ps1
# -rw-r--r--  0 cjoakim staff     173 Jan 27 16:06 DotnetConsoleApp/queries/general.txt
# done
