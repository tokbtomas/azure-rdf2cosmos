drop table postal_codes;

CREATE TABLE "postal_codes" (
	"id" integer unique not null,
	"postal_cd" integer unique not null,
	"country_cd" character varying(8) not null,
	"city_name" character varying(80) not null,
	"state_abbrv" character varying(8) not null,
	"latitude" decimal(16,11),
	"longitude" decimal(16,11)
);
