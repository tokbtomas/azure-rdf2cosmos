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

tarfile="DotnetConsoleApp_data.tar"

rm $tarfile

echo ''
echo 'creating tar file ...'
tar cfv $tarfile \
    DotnetConsoleApp/Data/*.txt \
    DotnetConsoleApp/Data/raw/*.ttl \
    DotnetConsoleApp/Data/jsonld/*.json

echo ''
echo 'listing the contents the tar file ...'
tar tvf $tarfile

echo 'done'

# Partial script output:
# listing the contents the tar file ...
# -rw-r--r--  0 cjoakim staff     175 Jan 26 18:02 DotnetConsoleApp/Data/jsonld_files_list.txt
# -rw-r--r--  0 cjoakim staff 3907899 Jan 18 13:01 DotnetConsoleApp/Data/raw/VideoAnnotations_MCUTitles.ttl
# -rw-r--r--  0 cjoakim staff  521655 Jan 18 13:01 DotnetConsoleApp/Data/raw/canonMapTitles.ttl
# -rw-r--r--  0 cjoakim staff    6487 Jan 13 12:06 DotnetConsoleApp/Data/raw/character.ttl
# -rw-r--r--  0 cjoakim staff   32593 Jan 13 12:06 DotnetConsoleApp/Data/raw/creativeWork.ttl
# -rw-r--r--  0 cjoakim staff    6277 Jan 13 12:06 DotnetConsoleApp/Data/raw/manifestation.ttl
# -rw-r--r--  0 cjoakim staff  116473 Jan 18 13:01 DotnetConsoleApp/Data/raw/mcuTitlesStorylines.ttl
# -rw-r--r--  0 cjoakim staff    7018 Jan 13 12:06 DotnetConsoleApp/Data/raw/serialWork.ttl
# -rw-r--r--  0 cjoakim staff    5742 Jan 13 12:06 DotnetConsoleApp/Data/raw/storyContent.ttl
# -rw-r--r--  0 cjoakim staff    9156 Jan 13 12:06 DotnetConsoleApp/Data/raw/tagging.ttl
# -rw-r--r--  0 cjoakim staff 3806962 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/VideoAnnotations_MCUTitles.json
# -rw-r--r--  0 cjoakim staff  568505 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/canonMapTitles.json
# -rw-r--r--  0 cjoakim staff    8289 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/character.json
# -rw-r--r--  0 cjoakim staff   38163 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/creativeWork.json
# -rw-r--r--  0 cjoakim staff    8182 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/manifestation.json
# -rw-r--r--  0 cjoakim staff  124035 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/mcuTitlesStorylines.json
# -rw-r--r--  0 cjoakim staff    8864 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/serialWork.json
# -rw-r--r--  0 cjoakim staff    7713 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/storyContent.json
# -rw-r--r--  0 cjoakim staff   11497 Jan 25 15:55 DotnetConsoleApp/Data/jsonld/tagging.json
# done
