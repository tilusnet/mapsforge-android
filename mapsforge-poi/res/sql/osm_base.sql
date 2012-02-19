--
-- PostgreSQL database dump
--

-- Started on 2009-11-18 23:04:42 CET

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1827 (class 1262 OID 16388)
-- Name: osm_base; Type: DATABASE; Schema: -; Owner: osm
--

CREATE DATABASE osm_base WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TABLESPACE = osm;


ALTER DATABASE osm_base OWNER TO osm;

--\connect osm_base

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1514 (class 1259 OID 16585)
-- Dependencies: 1796 3
-- Name: edges; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE edges (
    id integer NOT NULL,
    length integer NOT NULL,
    source_id bigint NOT NULL,
    dest_id bigint NOT NULL,
    traffic_light smallint,
    urban boolean,
    streetname character varying(255),
    maxspeed character varying(50),
    level character varying(50),
    unidirectional boolean DEFAULT false NOT NULL
);


ALTER TABLE public.edges OWNER TO osm;

--
-- TOC entry 1516 (class 1259 OID 16628)
-- Dependencies: 3
-- Name: weights; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE weights (
    edge_id integer NOT NULL,
    configuration character varying(255) NOT NULL,
    weight int NOT NULL
);


ALTER TABLE public.weights OWNER TO osm;

CREATE TABLE reaches (
    node_id bigint NOT NULL,
    configuration character varying(255) NOT NULL,
    reach int NOT NULL
);

ALTER TABLE public.reaches OWNER TO osm;

--
-- TOC entry 1517 (class 1259 OID 16860)
-- Dependencies: 1604 3
-- Name: adjacency_list; Type: VIEW; Schema: public; Owner: osm
--

CREATE VIEW adjacency_list AS
    SELECT e.source_id, e.dest_id, w.weight, w.configuration FROM (edges e JOIN weights w ON ((e.id = w.edge_id))) UNION SELECT e.dest_id AS source_id, e.source_id AS dest_id, w.weight, w.configuration FROM (edges e JOIN weights w ON ((e.id = w.edge_id))) WHERE (e.unidirectional = false) ORDER BY 1;


ALTER TABLE public.adjacency_list OWNER TO osm;



--
-- TOC entry 1515 (class 1259 OID 16610)
-- Dependencies: 3
-- Name: edge_nodes; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE edge_nodes (
    edge_id integer NOT NULL,
    node_id bigint NOT NULL,
    sequence_id integer NOT NULL
);


ALTER TABLE public.edge_nodes OWNER TO osm;

--
-- TOC entry 1509 (class 1259 OID 16390)
-- Dependencies: 3
-- Name: node_tags; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE node_tags (
    id bigint NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL
);


ALTER TABLE public.node_tags OWNER TO osm;

--
-- TOC entry 1510 (class 1259 OID 16396)
-- Dependencies: 3
-- Name: nodes; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE nodes (
    id bigint NOT NULL,
    int_latitude int NOT NULL,
    int_longitude int NOT NULL
);


CREATE TABLE id_mapping (
    osm_id bigint NOT NULL,
    internal_id int NOT NULL,
    configuration varchar(255) NOT NULL
);


ALTER TABLE public.id_mapping OWNER TO osm;


ALTER TABLE public.nodes OWNER TO osm;

--
-- TOC entry 1511 (class 1259 OID 16399)
-- Dependencies: 3
-- Name: way_nodes; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE way_nodes (
    id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id integer NOT NULL
);


ALTER TABLE public.way_nodes OWNER TO osm;

--
-- TOC entry 1512 (class 1259 OID 16402)
-- Dependencies: 3
-- Name: way_tags; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE way_tags (
    id bigint NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL
);


ALTER TABLE public.way_tags OWNER TO osm;

--
-- TOC entry 1513 (class 1259 OID 16408)
-- Dependencies: 3
-- Name: ways; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE ways (
    id bigint NOT NULL
);


ALTER TABLE public.ways OWNER TO osm;

--
-- TOC entry 1798 (class 2606 OID 16412)
-- Dependencies: 1509 1509 1509
-- Name: node_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT node_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 1815 (class 2606 OID 16635)
-- Dependencies: 1516 1516 1516 1516
-- Name: pg_weights; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY weights
    ADD CONSTRAINT pg_weights PRIMARY KEY (edge_id, configuration);
    
ALTER TABLE ONLY reaches
    ADD CONSTRAINT reaches_pkey PRIMARY KEY (node_id, configuration);    


--
-- TOC entry 1812 (class 2606 OID 16802)
-- Dependencies: 1515 1515 1515
-- Name: pk_edge_nodes; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY edge_nodes
    ADD CONSTRAINT pk_edge_nodes PRIMARY KEY (edge_id, sequence_id);


--
-- TOC entry 1810 (class 2606 OID 16589)
-- Dependencies: 1514 1514
-- Name: pk_edges; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY edges
    ADD CONSTRAINT pk_edges PRIMARY KEY (id);


--
-- TOC entry 1800 (class 2606 OID 16414)
-- Dependencies: 1510 1510
-- Name: pk_nodes; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT pk_nodes PRIMARY KEY (id);


--
-- TOC entry 1803 (class 2606 OID 16416)
-- Dependencies: 1511 1511 1511
-- Name: way_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_pkey PRIMARY KEY (id, sequence_id);


--
-- TOC entry 1805 (class 2606 OID 16418)
-- Dependencies: 1512 1512 1512
-- Name: way_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT way_tags_pkey PRIMARY KEY (id, k);


--transport_type
-- TOC entry 1807 (class 2606 OID 16420)
-- Dependencies: 1513 1513
-- Name: ways_pkey; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT ways_pkey PRIMARY KEY (id);


--
-- TOC entry 1801 (class 1259 OID 16421)
-- Dependencies: 1511
-- Name: fki_way_nodes_node; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX fki_way_nodes_node ON way_nodes USING btree (node_id);


--
-- TOC entry 1808 (class 1259 OID 16864)
-- Dependencies: 1514
-- Name: idx_edge_level; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX idx_edge_level ON edges USING btree (level);


--
-- TOC entry 1813 (class 1259 OID 16928)
-- Dependencies: 1516 1516
-- Name: idx_weights_type; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX idx_weights_configuration ON weights USING btree (configuration);


--
-- TOC entry 1820 (class 2606 OID 16590)
-- Dependencies: 1799 1510 1514
-- Name: fk_dest_vertice; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY edges
    ADD CONSTRAINT fk_dest_vertice FOREIGN KEY (dest_id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE;


--
-- TOC entry 1822 (class 2606 OID 16615)
-- Dependencies: 1515 1809 1514
-- Name: fk_edge_nodes_edges; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY edge_nodes
    ADD CONSTRAINT fk_edge_nodes_edges FOREIGN KEY (edge_id) REFERENCES edges(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1823 (class 2606 OID 16796)
-- Dependencies: 1510 1515 1799
-- Name: fk_edge_nodes_nodes; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY edge_nodes
    ADD CONSTRAINT fk_edge_nodes_nodes FOREIGN KEY (node_id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1816 (class 2606 OID 16422)
-- Dependencies: 1510 1799 1509
-- Name: fk_node_tags; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT fk_node_tags FOREIGN KEY (id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE;


--
-- TOC entry 1821 (class 2606 OID 16595)
-- Dependencies: 1514 1799 1510
-- Name: fk_source_vertice; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY edges
    ADD CONSTRAINT fk_source_vertice FOREIGN KEY (source_id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE;


--
-- TOC entry 1819 (class 2606 OID 16427)
-- Dependencies: 1512 1806 1513
-- Name: fk_way_tags_way; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT fk_way_tags_way FOREIGN KEY (id) REFERENCES ways(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1817 (class 2606 OID 16432)
-- Dependencies: 1806 1513 1511
-- Name: fk_waynodes_way; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT fk_waynodes_way FOREIGN KEY (id) REFERENCES ways(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1824 (class 2606 OID 16636)
-- Dependencies: 1516 1514 1809
-- Name: fk_weights_edges; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY weights
    ADD CONSTRAINT fk_weights_edges FOREIGN KEY (edge_id) REFERENCES edges(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
    
ALTER TABLE ONLY reaches
    ADD CONSTRAINT fk_reaches_nodes FOREIGN KEY (node_id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1818 (class 2606 OID 16437)
-- Dependencies: 1511 1799 1510
-- Name: way_nodes_node; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_node FOREIGN KEY (node_id) REFERENCES nodes(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

    
    
    
    
--
-- manually added by Till:
-- View 'waynode_vertex', providing adjacent vertices to all way nodes with the corresponding distance
--
-- TODO: distance is Euklidean at the moment. This needs to be replaced by a the earth surface's roundness regarding one.
--   

CREATE OR REPLACE VIEW waynode_vertex AS 
         SELECT wn.int_latitude AS latitude, wn.int_longitude AS longitude, v.internal_id, 0 AS distance
           FROM id_mapping v
      JOIN nodes wn ON wn.id = v.osm_id
UNION 
         SELECT wn.int_latitude AS latitude, wn.int_longitude AS longitude, v.internal_id, sqrt(((vn.int_latitude - wn.int_latitude)::double precision ^ 2::double precision) + ((vn.int_longitude - wn.int_longitude)::double precision ^ 2::double precision))::integer AS distance
           FROM (         SELECT en.node_id, e.source_id AS source_dest_id
                           FROM edge_nodes en
                      JOIN edges e ON e.id = en.edge_id
                     WHERE en.node_id <> e.source_id AND en.node_id <> e.dest_id
                UNION 
                         SELECT en.node_id, e.dest_id AS source_dest_id
                           FROM edge_nodes en
                      JOIN edges e ON e.id = en.edge_id
                     WHERE en.node_id <> e.source_id AND en.node_id <> e.dest_id) nsd
      JOIN id_mapping v ON v.osm_id = nsd.source_dest_id
   JOIN nodes vn ON vn.id = v.osm_id
   JOIN nodes wn ON wn.id = nsd.node_id;

ALTER TABLE waynode_vertex OWNER TO osm;



--
-- TOC entry 1829 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2009-11-18 23:04:42 CET

--
-- PostgreSQL database dump complete
--

