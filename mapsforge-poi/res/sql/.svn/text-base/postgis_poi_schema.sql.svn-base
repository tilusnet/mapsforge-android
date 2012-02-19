-- Table: categories

-- DROP TABLE categories;

CREATE TABLE categories
(
  title character varying(255) NOT NULL,
  parent character varying(255),
  CONSTRAINT pk_categories PRIMARY KEY (title),
  CONSTRAINT fk_parent_category FOREIGN KEY (parent)
      REFERENCES categories (title) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE categories OWNER TO postgres;

-- Index: fki_parent_category

-- DROP INDEX fki_parent_category;

CREATE INDEX fki_parent_category
  ON categories
  USING btree
  (parent);

-- Table: pois

-- DROP TABLE pois;

CREATE TABLE pois
(
  "location" geography(Point,4326) NOT NULL,
  id bigserial NOT NULL,
  "name" character varying(255),
  url character varying(255),
  category character varying(255) NOT NULL,
  CONSTRAINT pk_pois PRIMARY KEY (id),
  CONSTRAINT fk_pois_categories FOREIGN KEY (category)
      REFERENCES categories (title) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pois OWNER TO postgres;

-- Index: fki_poi_category

-- DROP INDEX fki_poi_category;

CREATE INDEX fki_poi_category
  ON pois
  USING btree
  (category);

-- Index: idx_gist_location

-- DROP INDEX idx_gist_location;

CREATE INDEX idx_gist_location
  ON pois
  USING gist
  (location);
