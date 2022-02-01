namespace RdfToCosmos
{

    using System;
    using System.Collections.Generic;
    using System.Dynamic;
    using System.IO;

    using System.Text.Json;
    using System.Text.Json.Nodes;
    using System.Text.Json.Serialization;

    using System.Threading.Tasks;
    using Newtonsoft.Json;
    using Newtonsoft.Json.Converters;
    using Microsoft.Azure.Cosmos;

    class Program
    {

        // Class variables:
        private static string[] cliArgs = null;
        private static string cliFunction = null;
        private static Config config = null;
        private static CosmosClient cosmosClient = null;
        private static int documentNumber = 0;

        static async Task Main(string[] args)
        {

            if (args.Length < 1)
            {
                PrintCliExamples("Invalid command-line args");
                await Task.Delay(0);
                return;
            }
            cliArgs = args;
            config = Config.Singleton(cliArgs);
            cliFunction = args[0];

            try
            {
                switch (cliFunction)
                {
                    case "list_databases":
                        await ListDatabases();
                        break;
                    case "create_database":
                        await CreateDatabase();
                        break;
                    case "update_database_throughput":
                        await UpdateDatabaseThroughput();
                        break;
                    case "delete_database":
                        await DeleteDatabase();
                        break;
                    case "list_containers":
                        await ListContainers();
                        break;
                    case "create_container":
                        await CreateContainer();
                        break;
                    case "update_container_throughput":
                        await UpdateContainerThroughput();
                        break;
                    case "update_container_indexing":
                        await UpdateContainerIndexing();
                        break;
                    case "truncate_container":
                        await TruncateContainer();
                        break;
                    case "delete_container":
                        await DeleteContainer();
                        break;
                    case "bulk_load_jsonld_data":
                        await BulkLoadJsonldData();
                        break;
                    case "count_documents":
                        await CountDocuments();
                        break;
                    case "execute_queries":
                        await ExecuteQueries();
                        break;
                    default:
                        PrintCliExamples($"invalid cliFunction: {cliFunction}");
                        break;
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"ERROR: Exception in Main() - ", e.Message);
                Console.WriteLine(e.StackTrace);
            }
            finally
            {
                if (cosmosClient != null)
                {
                    cosmosClient.Dispose();
                }
            }
            await Task.Delay(0);
        }

        private static void PrintCliExamples(string msg)
        {
            if (msg != null)
            {
                Console.WriteLine($"Error: {msg}");
            }
            Console.WriteLine("");
            Console.WriteLine("Command-Line Examples:");
            Console.WriteLine("dotnet run list_databases");
            Console.WriteLine("dotnet run create_database <dbname> <shared-ru | 0>");
            Console.WriteLine("dotnet run delete_database <dbname>");
            Console.WriteLine("dotnet run update_database_throughput <dbname> <shared-ru>");
            Console.WriteLine("---");
            Console.WriteLine("dotnet run list_containers <dbname>");
            Console.WriteLine("dotnet run create_container <dbname> <cname> <pk> <ru>");
            Console.WriteLine("dotnet run update_container_throughput <dbname> <cname> <ru>");
            Console.WriteLine("dotnet run update_container_indexing <dbname> <cname> <json-data-path>");
            Console.WriteLine("dotnet run truncate_container <dbname> <cname>");
            Console.WriteLine("dotnet run delete_container <dbname> <cname>");
            Console.WriteLine("---");
            Console.WriteLine("dotnet run explore_jsonld_file <infile>");
            Console.WriteLine("dotnet run explore_jsonld_file Data/jsonld/creativeWork.json");
            Console.WriteLine("---");
            Console.WriteLine("dotnet run bulk_load_jsonld_data <dbname> <cname> Data/jsonld_files_list.txt --load --verbose");
            Console.WriteLine("---");
            Console.WriteLine("dotnet run count_documents <dbname> <cname>");
            Console.WriteLine("---");
            Console.WriteLine("dotnet run execute_queries <dbname> <cname> queries/general.txt --verbose");
            Console.WriteLine("");
        }

        private static async Task ListDatabases()
        {
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            List<string> dbList = await util.ListDatabases();
            Console.WriteLine($"ListDatabases - count {dbList.Count}");
            for (int i = 0; i < dbList.Count; i++)
            {
                string dbname = dbList[i];
                Database db = await util.GetDatabase(dbname);
                int? currentRU = await db.ReadThroughputAsync();
                if (currentRU == null)
                {
                    Console.WriteLine($"database {i + 1}: {dbname} non-shared");
                }
                else
                {
                    Console.WriteLine($"database {i + 1}: {dbname} current RU {currentRU}");
                }
            }
        }

        private static async Task CreateDatabase()
        {
            string dbname = cliArgs[1];
            int sharedRu = Int32.Parse(cliArgs[2]);
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            DatabaseResponse resp = await util.CreateDatabase(dbname, sharedRu);
            Console.WriteLine(
                $"CreateDatabase {dbname} {sharedRu} -> status code {resp.StatusCode}, RU {resp.RequestCharge}");
        }

        private static async Task UpdateDatabaseThroughput()
        {
            string dbname = cliArgs[1];
            int sharedRu = Int32.Parse(cliArgs[2]);
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            int statusCode = await util.UpdateDatabaseThroughput(dbname, sharedRu);
            Console.WriteLine($"UpdateDatabaseThroughput {dbname} {sharedRu} -> statusCode {statusCode}");
        }

        private static async Task DeleteDatabase()
        {
            string dbname = cliArgs[1];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            int statusCode = await util.DeleteDatabase(dbname);
            Console.WriteLine($"DeleteDatabase {dbname} -> statusCode {statusCode}");  // 204 expected
        }

        private static async Task ListContainers()
        {
            string dbname = cliArgs[1];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            List<string> containerList = await util.ListContainers(dbname);
            Console.WriteLine($"ListContainers - count {containerList.Count}");
            for (int i = 0; i < containerList.Count; i++)
            {
                string cname = containerList[i];
                Console.WriteLine($"container in db: {dbname} -> {cname}");
            }
        }

        private static async Task CreateContainer()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            string pk = cliArgs[3];
            int ru = Int32.Parse(cliArgs[4]);
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            string id = await util.CreateContainer(dbname, cname, pk, ru);
            Console.WriteLine($"CreateContainer {dbname} {cname} {ru} -> Id {id}");
        }

        private static async Task UpdateContainerThroughput()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            int ru = Int32.Parse(cliArgs[3]);
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            int statusCode = await util.UpdateContainerThroughput(dbname, cname, ru);
            Console.WriteLine($"UpdateContainerThroughput {dbname} {cname} {ru} -> statusCode {statusCode}");
        }

        private static async Task UpdateContainerIndexing()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            string infile = cliArgs[3];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            // int statusCode = await util.UpdateContainerIndexing(dbname, cname, infile);
            // Console.WriteLine($"UpdateContainerThroughput {dbname} {cname} {ru} -> statusCode {statusCode}");
            await Task.Delay(0);
        }

        private static async Task TruncateContainer()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            await util.SetCurrentDatabase(dbname);
            await util.SetCurrentContainer(cname);
            int deleteOperations = await util.TruncateContainer();
            Console.WriteLine($"TruncateContainer {dbname} {cname} -> deleteOperations {deleteOperations}");
        }

        private static async Task DeleteContainer()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            int statusCode = await util.DeleteContainer(dbname, cname);
            Console.WriteLine($"DeleteContainer {dbname} {cname} -> statusCode {statusCode}");
        }

        private static async Task BulkLoadJsonldData()
        {
            string dbname = cliArgs[1];  // CosmosDB database name
            string cname = cliArgs[2];  // CosmosDB container name
            string dataDirectory = cliArgs[3];  // the path where the data files to be migrated are located

            cosmosClient = CosmosClientFactory.BulkLoadingClient();
            try
            {
                Database database = cosmosClient.GetDatabase(dbname);
                Container container = database.GetContainer(cname);

                string[] filesPaths = Directory.GetFileSystemEntries(dataDirectory);

                foreach (string filePath in filesPaths)
                {
                    await BulkLoadJsonldFile(container, filePath);
                }

            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                throw;
            }
            finally
            {
                if (cosmosClient != null)
                {
                    cosmosClient.Dispose();
                }
            }
        }

        private static async Task BulkLoadJsonldFile(Container container, string filePath)
        {
            string infile = filePath;   
            int batchSize = config.GetBulkBatchSize();
            int batchCount = 0;
            bool doLoad = config.HasCliFlagArg("--load");
            bool verbose = config.HasCliFlagArg("--verbose");

            Console.WriteLine("============================================================");
            Console.WriteLine($"BulkLoadJsonldFile; infile: {infile}");

            // See https://www.c-sharpcorner.com/article/new-programming-model-for-handling-json-in-net-6/
            // See https://kevsoft.net/2021/12/29/manipulate-json-with-system-text-json-nodes.html

            string jtext = System.IO.File.ReadAllText(infile);
            var jsonObject = JsonNode.Parse(jtext);
            // Array of System.Text.Json.Nodes.JsonObject
            JsonArray graphArray = jsonObject["@graph"].AsArray();
            int itemCount = graphArray.Count;
            Console.WriteLine($"array count {itemCount}");

            List<Task> tasks = new List<Task>(config.GetBulkBatchSize());

            var jso = new JsonSerializerOptions
            {
                WriteIndented = true
            };

            for (int i = 0; i < itemCount; i++)
            {
                var node = graphArray[i];
                //Console.WriteLine(node.GetType().FullName);  // node is an instance of System.Text.Json.Nodes.JsonObject
                JsonObject obj = graphArray[i].AsObject();

                if ((i == 0) || (verbose))
                {
                    Console.WriteLine("---");
                    Console.WriteLine($"JSONLD Document at index {i} in file {filePath}");
                    Console.WriteLine(obj.ToJsonString(jso));  // <-- JsonSerializerOptions for "pretty printing" the JSON
                }
                string doctype = filePath.Split(".")[0].ToLower(); // converts "tagging.json" to "tagging"

                TransformJsonldObject(obj, i, doctype);  // Add id, pk, and doctype attributes, etc

                // Convert to a dynamic object for loading into CosmosDB with UpsertItemAsync
                dynamic jsonDoc = JsonConvert.DeserializeObject(obj.ToJsonString());
                string pk = jsonDoc["pk"];
                tasks.Add(container.UpsertItemAsync(jsonDoc, new PartitionKey(pk)));

                if ((i == 0) || (verbose))
                {
                    Console.WriteLine($"Converted Document");
                    Console.WriteLine(jsonDoc.ToString());
                }
                if (tasks.Count == batchSize)
                {
                    if (doLoad)
                    {
                        batchCount++;
                        Console.WriteLine($"writing batch {batchCount} ({tasks.Count})");
                        await Task.WhenAll(tasks); // <-- executes the several UpsertItemAsync tasks
                        tasks = new List<Task>(batchSize);  // reset the task list to a new empty list for next batch
                    }
                }
            }
            if (tasks.Count > 0)
            {
                if (doLoad)
                {
                    batchCount++;
                    Console.WriteLine($"writing last batch {batchCount} ({tasks.Count})");
                    await Task.WhenAll(tasks);
                }
            }
            await Task.Delay(0);
        }

        /**
         * Transform the given document so that:
         * 1) it has an id attribute with a Guid/UUID value
         * 2) it has a pk attribute - partition key
         * 3) it has a doctype attribute - indicating what type of document it is
         * 4) convert the attributes that begin with @ to other names without the @.
         *    for example @type -> type, and @id -> oid
         */
        private static void TransformJsonldObject(JsonObject obj, int idx, string doctype)
        {
            if (obj.ContainsKey("id"))
            {
                obj["raw_id"] = obj["id"].ToString();
                obj.Remove("id");
            }
            documentNumber++;
            obj["id"] = Guid.NewGuid().ToString();
            obj["doctype"] = doctype;
            obj["pk"] = "none";
            obj["seq"] = idx + 1;  // for initial development only
            obj["docnum"] = documentNumber;   // for initial development only 

            if (obj.ContainsKey("@id"))
            {
                string oid = obj["@id"].ToString();
                if (oid.StartsWith("_"))
                {
                    obj["schema"] = true;
                    obj["pk"] = "schema";
                    obj["oid"] = oid;
                }
                else
                {
                    obj["pk"] = oid;
                    obj["oid"] = oid;
                }
                obj.Remove("@id");
            }

            if (obj.ContainsKey("@type"))
            {
                obj["type"] = obj["@type"].ToString();
                obj.Remove("@type");
            }


        }

        private static List<string> ReadLines(string filename)
        {

            List<string> lines = new List<string>();

            if (filename != null)
            {
                string line = null;
                System.IO.StreamReader file = new System.IO.StreamReader(filename);
                while ((line = file.ReadLine()) != null)
                {
                    lines.Add(line);
                }
            }
            return lines;
        }

        private static async Task CountDocuments()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosQueryUtil util = new CosmosQueryUtil(cosmosClient, config.IsVerbose());
            await util.SetCurrentDatabase(dbname);
            await util.SetCurrentContainer(cname);
            int count = (await util.CountDocuments("")).items[0];
            Console.WriteLine($"CountDocuments {dbname} {cname} -> {count}");
        }

        private static async Task ExecuteQueries()
        {
            string dbname = cliArgs[1];
            string cname = cliArgs[2];
            string infile = cliArgs[3];

            cosmosClient = CosmosClientFactory.RegularClient();
            CosmosQueryUtil util = new CosmosQueryUtil(cosmosClient, config.IsVerbose());
            await util.SetCurrentDatabase(dbname);
            await util.SetCurrentContainer(cname);

            // Console.WriteLine("warming sdk client...");
            for (int i = 0; i < 2; i++)
            {
                await util.CountDocuments("");
            }

            foreach (string line in File.ReadLines(infile))
            {
                string[] tokens = line.Split("|");
                if (tokens.Length > 1)
                {
                    string qname = tokens[0].Trim();
                    string sql = tokens[1].Trim();
                    if (qname.StartsWith("q"))
                    {
                        Console.WriteLine("");
                        Console.WriteLine("================================================================================");
                        Console.WriteLine($"executing qname: {qname}, db: {dbname}, cname: {cname}, sql: {sql}");
                        QueryResponse respObj = await util.ExecuteQuery(sql);
                        respObj.queryName = qname;
                        respObj.sql = sql;
                        respObj.dbname = dbname;
                        respObj.cname = cname;
                        respObj.Finish();
                        Console.WriteLine(respObj.ToString());
                        string jstr = respObj.ToJson();
                        if (config.IsVerbose())
                        {
                            Console.WriteLine(jstr);
                        }
                    }
                }
            }
        }

        /**
         * Use the cosmosClient to "warm it up" before starting a timed performance test.
         */
        private static async Task WarmupClient(string dbname, string cname)
        {
            CosmosAdminUtil util = new CosmosAdminUtil(cosmosClient, config.IsVerbose());
            List<string> containerList = await util.ListContainers(dbname);
            Console.WriteLine($"ListContainers - count {containerList.Count}");
            for (int i = 0; i < containerList.Count; i++)
            {
                string name = containerList[i];
                if (name.Equals(cname))
                {
                    Console.WriteLine($"OK: container {cname} is present in db: {dbname}");
                }
            }
        }

        private static long EpochMsTime()
        {
            return new DateTimeOffset(DateTime.UtcNow).ToUnixTimeMilliseconds();
        }
    }
}