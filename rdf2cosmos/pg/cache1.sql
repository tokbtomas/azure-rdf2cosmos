drop table if exists cache1;

CREATE TABLE "cache1" (
	"key"  character varying(255) unique not null,
	"node" JSON
);

select count(*) from cache1;