DROP TABLE IF EXISTS hh_edge CASCADE;
DROP TABLE IF EXISTS hh_vertex CASCADE;
DROP TABLE IF EXISTS hh_vertex_lvl CASCADE;
DROP TABLE IF EXISTS hh_lvl_stats CASCADE;
DROP TABLE IF EXISTS hh_graph_properties CASCADE;
DROP TABLE IF EXISTS hh_distance_table_row CASCADE;


CREATE TABLE hh_vertex
(
	id integer NOT NULL,
	longitude double precision NOT NULL,
	latitude double precision NOT NULL,
	CONSTRAINT pk_v PRIMARY KEY (id),
	CONSTRAINT chk1 CHECK (id >=0)
);

CREATE TABLE hh_vertex_lvl
(
  	id integer NOT NULL,
  	lvl integer NOT NULL,
  	neighborhood integer NOT NULL,
  	CONSTRAINT pk_vl PRIMARY KEY (id, lvl),
  	CONSTRAINT fk1 FOREIGN KEY (id) REFERENCES hh_vertex (id) INITIALLY DEFERRED DEFERRABLE,
  	CONSTRAINT chk1 CHECK (neighborhood >= 0),
  	CONSTRAINT chk2 CHECK (lvl >= 0)
);

CREATE TABLE hh_edge
(
	id integer NOT NULL,
  	source_id integer NOT NULL,
  	target_id integer NOT NULL,
  	weight integer NOT NULL,
  	min_lvl integer NOT NULL,
  	max_lvl integer NOT NULL,
  	fwd boolean NOT NULL,
  	bwd boolean NOT NULL,
  	shortcut boolean NOT NULL,
  	CONSTRAINT pk PRIMARY KEY (id),
  	CONSTRAINT fk1 FOREIGN KEY (source_id) REFERENCES hh_vertex (id) INITIALLY DEFERRED DEFERRABLE,
  	CONSTRAINT fk2 FOREIGN KEY (target_id) REFERENCES hh_vertex (id) INITIALLY DEFERRED DEFERRABLE,
  	CONSTRAINT chk1 CHECK (weight >= 0),
	CONSTRAINT chk2 CHECK (min_lvl >= 0),
	CONSTRAINT chk3 CHECK (max_lvl >= min_lvl)
);

CREATE TABLE hh_graph_properties
(
	creation_date timestamp NOT NULL,
	transport varchar NOT NULL,
	c double precision NOT NULL,
	h integer NOT NULL,
	hoplimit integer NOT NULL,
	vertex_threshold integer NOT NULL,
	downgraded_edges boolean NOT NULL,
	num_threads integer NOT NULL,
	comp_time_mins double precision NOT NULL
);

CREATE TABLE hh_distance_table_row(
	row_idx integer NOT NULL,
	vertex_id integer NOT NULL,
	distances integer[] NOT NULL,
	CONSTRAINT pk_dt PRIMARY KEY (row_idx),
	CONSTRAINT chk CHECK (row_idx >=0), 
	CONSTRAINT fk FOREIGN KEY (vertex_id) REFERENCES hh_vertex (id) INITIALLY DEFERRED DEFERRABLE
);

CREATE TABLE hh_lvl_stats 
(
	lvl integer NOT NULL,
	num_vertices integer NOT NULL,
	num_edges integer NOT NULL,
	num_core_vertices integer NOT NULL,
	num_core_edges integer NOT NULL,
	CONSTRAINT pk_ls PRIMARY KEY (lvl),
	CONSTRAINT chk CHECK (lvl >= 0)  
);
