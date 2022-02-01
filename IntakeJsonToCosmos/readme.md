# SqlDotnetSynapseApproach

## System Requirements

- Windows, Linux, or macOS operating system
- Java 11 installed (for Apache Jena)
- Apache Jena installed
- DotNet 6 installed

---

## Environment Variables

```
AZURE_RDF2COSMOS_DATA_DIR   
# above points to directory CosmosDB/SqlDotnetSynapseApproach/DotnetConsoleApp/Data in this repo

AZURE_COSMOSDB_SQLDB_CONN_STRING      <-- see account connection string in Azure Portal
AZURE_COSMOSDB_SQLDB_KEY              <-- see account key in Azure Portal
AZURE_COSMOSDB_SQLDB_URI              <-- https://<account-name>.documents.azure.com:443/>
AZURE_COSMOSDB_SQLDB_PREF_REGIONS     <-- eastus
AZURE_COSMOSDB_BULK_BATCH_SIZE        <-- default value is 100
```

---

## Processing Steps

### Summary of Steps

- 1. Transform the raw Disney RDF files to JSONLD format
- 2. Read the JSONLD with DonetConsoleApp and load CosmosDB/SQL
- 3. Ad-hoc queries of the CosmosDB/SQL container

### 1. Transform the raw Disney RDF files to JSONLD format

This process uses the **riot** program in the **Apache Jena** project.

#### Script example

Execute **riot** with tagging.ttl as the input, converting to jsonld, as output file tagging.json

```
riot --out jsonld $ddir/raw/tagging.ttl > $ddir/jsonld/tagging.json
```

#### Script execution 

```
$ cd CosmosDB/SqlDotnetSynapseApproach/DotnetConsoleApp

$ ./1_convert_raw_rdf_to_jsonld.sh

start date/time:
Tue Jan 25 15:55:29 EST 2022
Removing files in the output directory ...
VideoAnnotations_MCUTitles
canonMapTitles
character
creativeWork
manifestation
mcuTitlesStorylines
serialWork
storyContent
tagging
List of the output jsonld files:
total 9912
drwxr-xr-x  11 cjoakim  staff      352 Jan 25 15:55 .
drwxr-xr-x   5 cjoakim  staff      160 Jan 25 13:42 ..
-rw-r--r--   1 cjoakim  staff  3806962 Jan 25 15:55 VideoAnnotations_MCUTitles.json
-rw-r--r--   1 cjoakim  staff   568505 Jan 25 15:55 canonMapTitles.json
-rw-r--r--   1 cjoakim  staff     8289 Jan 25 15:55 character.json
-rw-r--r--   1 cjoakim  staff    38163 Jan 25 15:55 creativeWork.json
-rw-r--r--   1 cjoakim  staff     8182 Jan 25 15:55 manifestation.json
-rw-r--r--   1 cjoakim  staff   124035 Jan 25 15:55 mcuTitlesStorylines.json
-rw-r--r--   1 cjoakim  staff     8864 Jan 25 15:55 serialWork.json
-rw-r--r--   1 cjoakim  staff     7713 Jan 25 15:55 storyContent.json
-rw-r--r--   1 cjoakim  staff    11497 Jan 25 15:55 tagging.json
finish date/time:
Tue Jan 25 15:55:45 EST 2022
done
```

---

### 2. Read the JSONLD with DonetConsoleApp and load CosmosDB/SQL 

This process reads the JSONLD files and converts each object in the **@graph array**
within these files to objects.  Minor transformation is done to these objects, such
as adding id, pk (partition key), and doctype attributes.  The attributes in the
JSONLD that begin with **@** are renamed for CosmosDB, as that is an odd character.

The target in CosmosDB/SQL is a single container, with the partition key attribute
**/pk**.

The target CosmosDB container should be **Synapse Link enabled** so that copies
of the CosmosDB documents can be read from the Analytic Data Store with Synapse,
typically from Spark Notebooks.

#### Script execution 

The **--load** command-line argument will cause the the database to be loaded.
Otherwise, the documents-to-be loaded are simply logged to the console.

```
$ cd CosmosDB/SqlDotnetSynapseApproach/DotnetConsoleApp

# command-line format:
# dotnet run bulk_load_jsonld_data <dbname> <container> <jsonld-data-directory>

$ dotnet run bulk_load_jsonld_data demo graph Data/jsonld_files_list.txt --load

- or -

$ ./2_load_cosmosdb_sql.sh
```

---

### 3. Ad-hoc queries of the CosmosDB/SQL container

#### Edit the Queries File 

Edit a simple text file of your choice, like queries/general.txt.
The contexts of the queries txt file looks like the following;
each line contains a query name and a SQL query, separated by a vertibar.
This design is for simple ad-hoc queries; not for production workloads.

```
q0 | SELECT COUNT(1) FROM c
q1 | SELECT * FROM c offset 0 limit 10
q2 | SELECT * FROM c where c.oid = 'http://data.disney.com/interval#0007861e-883b-43c3-83c3-c14452015e67'
```

#### Execute the Queries 

```
$ dotnet run execute_queries <dbname> <cname> queries/general.txt --verbose
```

Output is displayed in the console/terminal; redirect to a file as necessary.

---

## Synapse Processing

### Initial Sample PySpark Notebook to read the Synapse Link Data

```
df = spark.read\
    .format("cosmos.olap")\
    .option("spark.synapse.linkedService", "CosmosDbDevGraph")\
    .option("spark.cosmos.container", "graph")\
    .load()

display(df.limit(100))
```

In this example CosmosDbDevGraph is the name of the Linked Service in Synapse
which points to the CosmosDB/SQL account and database.  graph is the container name.

TODO - implement Graph processing
