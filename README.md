# azure-rdf2cosmos

A RDF to CosmosDB graph database migration process.

<p align="center"><img src="img/rdf2cosmos.png" width="90%"></p>

This repo: https://github.com/cjoakim/azure-rdf2cosmos

---

## The Conversion/Migration Process

There are four steps to the process, each implemented as the following bash and powershell
scripts.  The process is designed to support huge input files.

The first step converts the raw RDF (i.e. - *.rdf, *.ttl, etc.) files, exported from the
source database, into ***.nt** files.  These are known as **triples**, and this conversion
is done simply with an **Apache Jena** utility called **riot**.  Riot is an acronym for
**RDF I/O technology (RIOT)**.

**Apache Jena** is expected to be installed on the workstation/VM where this process executes;
links and installation instructions below.

The second step accumulates and transforms the *.nt files into Java objects that are persisted
as JSON files.  The nt triples represent atomic data elements in an eventual Gremlin
Vertex or Edge, and the nt files aren't necessarily sorted.  Therefore the atomic nt rows
are aggregated into JSON Vertex and Edge documents.  The many Vertex and Edge JSON files are 
each persisted to disk in the current implementation, but they could alternatively be persisted
to a database (i.e. - CosmosDB/SQL or Azure PostgreSQL) to achieve scalability.

The third step transforms the JSON files into a format suitable for loading into CosmosDB/Gremlin.
The current implementation uses the **Groovy** format.  Alternatively, CSV can be implemented
in the future to enable the use of the 
[azure-cosmosdb-gremlin-bulkloader](https://github.com/cjoakim/azure-cosmosdb-gremlin-bulkloader).

The fourth and final step of the process loads CosmosDB with the file(s) created in step 3.

### Environment Variables

The following environment variables need to be set on the system that executes this process.

```
AZURE_RDF2COSMOS_DATA_DIR
AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT   
```

The ...DATA_DIR defines the location of the root data directory in the project.
It can have the following structure:
```
├── cache
├── gremlin
├── jena
├── meta
├── raw
│   └── ... subdirectories as necessary ...
├── tmp
└── transformed
    └── ... subdirectories as necessary ...
```

The ..._OBJ_CACHE_COUNT refers the maximum number of Java objects that will be stored
in the memory of the JVM in step two.  Once this size is reached the cache is flushed
to disk.  Individual JSON objects will be reloaded into cache and augmented as necessary.
This thus enables huge non-sorted input files.

### remote.yml Configuration File

Connectivity to CosmosDB is configured via the **remote.yaml** file in the
**config/** directory.

Copy file sample_remote.yaml to file remote.yaml, then edit remote.yaml
per your CosmosDB account name, database name, graph name, and account key.

### Linux/macOS bash shell scripts

```
$ ./dec_1_convert_raw_rdf_to_nt.sh
$ ./dec_2_convert_nt_to_objects.sh
$ ./dec_3_convert_objects_to_gremlin.sh
$ ./dec_4_load_cosmosdb.sh
```

### Windows PowerShell Scripts

TODO - implement these 

```
$ .\dec_1_convert_raw_rdf_to_nt.ps1
$ .\dec_2_convert_nt_to_objects.ps1
$ .\dec_3_convert_objects_to_gremlin.ps1
$ .\dec_4_load_cosmosdb.ps1
```

---

## Apache Jena

- See https://jena.apache.org/
- [RIOT](https://jena.apache.org/documentation/io/)
- https://jena.apache.org/tutorials/rdf_api.html
- [JavaDocs](https://jena.apache.org/documentation/javadoc/arq/index.html)

### Installation

- See https://jena.apache.org/download/index.cgi
- Download **apache-jena-4.3.2.zip** from https://jena.apache.org/download/index.cgi
- Unzip it in $HOME/jena, creating directory **~/jena/apache-jena-4.3.2**
- See https://jena.apache.org/documentation/tools/ 
  - export JENA_HOME=the directory you downloaded Jena to  
  - export PATH=$PATH:$JENA_HOME/bin

---

## Gremlin

### Queries

```
g.V().count()
g.E().count()
```

---

## Public Data Sources

- https://jena.apache.org/
- https://github.com/stardog-union/stardog-tutorials
