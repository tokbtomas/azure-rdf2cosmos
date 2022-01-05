# azure-rdf2cosmos

A RDF to CosmosDB graph database migration process.

<p align="center"><img src="img/rdf2cosmos.png" width="100%"></p>

---

## Property Graphs

<p align="center"><img src="img/sample-graph.png" width="90%"></p>

- **Vertices** are the **entities**
- **Edges** are the **relationships** between the Vertices
  - Each Edge is a **one-way relationship**
  - There can be multiple Edges connecting two Vertices
- Both Vertices and Edges can have **Properties**
- The database is **schemaless**, there are no ontologies

## Azure CosmosDB

- [Intro to Gremlin API in Azure CosmosDB](https://docs.microsoft.com/en-us/azure/cosmos-db/graph/graph-introduction)
- Infinitely scalable PaaS service
  - Scale is defined by **Request Units (RUs)**
- Implements the open-source **Apache Tinkerpop and Gremlin APIs wire protocol**
- Actual physical implementation is based on the Azure CosmosDB foundation
- Multi-region replication
- Query the database (i.e. - a **Traversal**) with the **Gremlin** syntax

---

## The Conversion/Migration Process

### Highlights

- There are **four steps to the process**, each implemented as a bash/ps1 script
  - See the list of scripts below
- Implemented with **Java 11** and **Apache Jena**
- **The process is designed to support huge input files**
  - **streaming** Java APIs are used for reading
  - not limited by JVM memory
  - utilizes out-of-JVM caching for **Aggregated Vertex and Edge Data**
    - Non-sequential input data re: Vertices, Edges, and their Properties
    - v1 implementation uses JSON files on disk
    - v2 implemention is Azure PostgreSQL
      - v2 cache expected to be complete on 1/9 or sooner 
- CosmosDB can be loaded in one of several ways
  - As **groovy files** with Java Gremlin Driver (current implementation)
    - Library org.apache.tinkerpop:gremlin-driver:3.4.0
  - As CSV files with a DotNet Bulk Loader
    - See https://github.com/cjoakim/azure-cosmosdb-gremlin-bulkloader
  - New Graph Bulk Executor .NET library
    - See https://docs.microsoft.com/en-us/azure/cosmos-db/graph/bulk-executor-graph-dotnet
- **BYOD - Bring Your Own Data**
  - This repo contains no RDF data files; bring your own
  - Use environment variable AZURE_RDF2COSMOS_DATA_DIR to define where your RDF data resides
  - See example directory structure below; it corresponds to the scripts in this repo

### Roadmap

- Complete the v2 Cache implementation with Azure PostgreSQL
- Create Docker containers for both Apache Jena and this custom Java code
  - Docker containers are easier to deploy, and enable AKS/Kubernetes
- Enable Azure Storage Blobs for IO

### Step 1: Convert raw RDF files to NT Triples

The first step converts the raw RDF (i.e. - *.rdf, *.ttl, etc.) files, exported from the
source database, into ***.nt** files.  These are known as **triples**, and this conversion
is done simply with an **Apache Jena** utility called **riot**.  Riot is an acronym for
**RDF I/O technology (RIOT)**.  Each triple consists of a subject, predicate, and object.
These nt files are easy to read and parse programmatically.

**Apache Jena** is expected to be installed on the workstation/VM where this process executes;
links and installation instructions below.

Example script:
```
$ riot --out nt $ddir/raw/december/gdata/mcma_v1.ttl > $ddir/raw/december/gdata/mcma.nt
```

### Step 2: Read the NT Triples and Aggregate Vertex & Edge Data

The second step accumulates and transforms the *.nt files into Java objects that are persisted
as JSON files.  The nt triples represent atomic data elements in an eventual Gremlin
Vertex or Edge, and the nt files aren't necessarily sorted.  Therefore the atomic nt rows
are **aggregated into JSON Vertex and Edge documents**.  The many Vertex and Edge JSON files are 
each persisted to disk in the current implementation, but they can alternatively be persisted
to a database (i.e. - CosmosDB/SQL or Azure PostgreSQL) to achieve scalability.

Example script:
```
$ java -jar app/build/libs/app-uber.jar convert_rdf_to_objects $rdf_infile5 > $log_outfile5
```

### Step 3: Transform Aggregated data into a loadable format

The third step transforms the JSON files into a format suitable for loading into CosmosDB/Gremlin.
The current implementation uses the **Groovy** format.  Alternatively, CSV can be implemented
in the future to enable the use of the 
[azure-cosmosdb-gremlin-bulkloader](https://github.com/cjoakim/azure-cosmosdb-gremlin-bulkloader).

Example script:
```
$ java -jar app/build/libs/app-uber.jar convert_objects_to_gremlin > $log_outfile
```

### Step 4: Load CosmosDB with the transformed loadable data 

The fourth and final step of the process loads CosmosDB with the file(s) created in step 3.

Example script:
```
$ java -jar app/build/libs/app-uber.jar load_cosmosdb_graph $infile > $log_outfile
```

### Linux/macOS bash shell scripts

```
$ ./build.sh                             <-- compile and assemble the java code
$ ./dec_1_convert_raw_rdf_to_nt.sh
$ ./dec_2_convert_nt_to_objects.sh
$ ./dec_3_convert_objects_to_gremlin.sh
$ ./dec_4_load_cosmosdb.sh
```

### Windows PowerShell Scripts

```
PS >  \build.ps1                         <-- compile and assemble the java code
PS > .\dec_1_convert_raw_rdf_to_nt.ps1
PS > .\dec_2_convert_nt_to_objects.ps1
PS > .\dec_3_convert_objects_to_gremlin.ps1
PS > .\dec_4_load_cosmosdb.ps1
```

---

## System Requirements

### Operating System

- Linux, Windows, or macOS operating system

### Java JDK

- **Java JDK version 11 or higher**

### Git

- git source control system, recent version

### Gradle

- Gradle build tool (not Maven).  See https://gradle.org/

### Apache Jena

This project uses both the Apache Jena **riot** utility, and the Java **SDK**.

- See https://jena.apache.org/
- [RIOT (RDF I/O Technology)](https://jena.apache.org/documentation/io/)
- https://jena.apache.org/tutorials/rdf_api.html
- [JavaDocs](https://jena.apache.org/documentation/javadoc/arq/index.html)

#### Installation

- See https://jena.apache.org/download/index.cgi
- Download **apache-jena-4.3.2.zip** from https://jena.apache.org/download/index.cgi
- Unzip it in $HOME/jena, creating directory **~/jena/apache-jena-4.3.2**
- See https://jena.apache.org/documentation/tools/ 
  - export JENA_HOME=the directory you downloaded Jena to  
  - export PATH=$PATH:$JENA_HOME/bin

### Environment Variables

The following environment variables need to be set on the system that executes this process.

```
AZURE_RDF2COSMOS_DATA_DIR
AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT    
```

The **AZURE_RDF2COSMOS_DATA_DIR** defines the location of the root data directory in the project.
It can and should be **external to this github repo directory**.
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

The **AZURE_RDF2COSMOS_MAX_OBJ_CACHE_COUNT** refers the maximum number of Java objects
that will be stored in the memory of the JVM in step two.  Once this size is reached 
the cache is flushed to disk.  Individual JSON objects will be reloaded into cache
and augmented as necessary.  This design thus enables huge non-sorted input files that
are beyond the memory constraints of the JVM.

### remote.yml Configuration File

Connectivity to CosmosDB is configured via the **remote.yaml** file in the
**config/** directory.  This file is read by the Java program when loading
your CosmosDB graph.

Copy file sample_remote.yaml to file remote.yaml, then edit remote.yaml
per your CosmosDB account name, database name, graph name, and account key.

See the documentation at https://docs.microsoft.com/en-us/azure/cosmos-db/graph/create-graph-java for more information.

---

## Quick Start Instructions

- Create your Azure CosmosDB/Gremlin account in Azure Portal
- Set environment variables per above
- Populate your AZURE_RDF2COSMOS_DATA_DIR directory with your RDF files
- git clone https://github.com/cjoakim/azure-rdf2cosmos.git
- cd azure-rdf2cosmos
- cd rdf2cosmos
- Execute build.sh or build.ps1
- Execute the four scripts in sequence

---

## Miscellaneous

### Java Implementation Notes

The following design decisions were made to reduce solution complexity
and to increase portability.

- Gradle was chosen as the build tool for simplicity vs verbose Maven XML
- The Gradle build process creates an easily deployable uber-jar file
- No framework is used, such as Spring
- No logging libraries are used, such as Log4J.  System.out.println instead
- Initial cache system uses JSON files on local disk
- If necessary, a V2 cache system will use CosmosDB/SQL

### Gremlin Queries

```
g.V().count()
g.E().count()
```

### Public RDF Data Sources

- https://jena.apache.org/
- https://github.com/stardog-union/stardog-tutorials
