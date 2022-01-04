drop table if exists node_cache;

CREATE TABLE "node_cache" (
	"key"          character varying(255) unique not null,
	"type"         character varying(8) not null,
	"data"         character varying(8000) unique not null,
	"created_at"   bigint default 0,
	"updated_at"   bigint default 0,
	"converted_at" bigint default 0
);

insert into node_cache values (
	'key1', 
	'vertex', 
	'{"properties":{"cat":{"name":"cat","value":"Elsa","dataType":"string"}},"type":"vertex","vertexId1":"abc","vertexId2":null}',
	 0, 0, 0);

select count(*) from node_cache;

select * from node_cache;
