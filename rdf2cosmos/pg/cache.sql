drop table if exists cache1;

CREATE TABLE "cache1" (
	"node_key"   character varying(255) unique not null,
	"node_type"  character varying(8) not null,
	"data" JSON not null
);

insert into cache1 values ('key1', 'vertex', '{"properties":{"cat":{"name":"cat","value":"Elsa","dataType":"string"}},"type":"vertex","vertexId1":"abc","vertexId2":null}');

select count(*) from cache1;

select node_key, node_type, data from cache1;
