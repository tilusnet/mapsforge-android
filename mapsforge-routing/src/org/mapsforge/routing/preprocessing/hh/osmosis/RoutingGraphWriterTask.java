/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.routing.preprocessing.hh.osmosis;

import gnu.trove.function.TIntFunction;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntProcedure;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.hh.model.TagHighway;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/**
 * This task writes a sql file (tested for postgresql 8.4) which represents a routing graph. Executing the
 * resulting file bulk-loads the graph to the database. As parameter this plugin requires an output-file and a
 * comma separated white-list of osm way types.
 */
class RoutingGraphWriterTask implements Sink {
	// possible values for the node-ids on the hashmap :
	private static final int NODE_TYPE_VERTEX = 2;
	private static final int NODE_TYPE_WAYPOINT = 1;

	// sql statements to be written to the sql file :
	private static final String SQL_COPY_EDGES = "COPY rg_edge (id, source_id, target_id, osm_way_id, name, ref, destination, length_meters, undirected, urban, roundabout, hwy_lvl, longitudes, latitudes) FROM stdin;";
	private static final String SQL_COPY_VERTICES = "COPY rg_vertex (id, osm_node_id, lon, lat) FROM stdin;";
	private static final String SQL_COPY_HWY_LEVELS = "COPY rg_hwy_lvl (id, name) FROM stdin;";
	private static final String SQL_TERMINAL = "\\.";

	// sql create tables file (to be written to the output) :
	private static final String SQL_CREATE_TABLES_FILE = "hh-dml.sql";

	// plugin parameters :
	private File outputFile;
	private PrintWriter out;

	// amounting :
	private int amountOfNodesProcessed = 0;
	private int amountOfWaysProcessed = 0;
	private int amountOfRelationsProcessed = 0;

	private int amountOfEdgesWritten = 0;
	private int amountOfVerticesWritten = 0;

	// counter used to assign ids for vertices
	int numVertices = 0;

	// store all nodes temporarily here :
	private SimpleObjectStore<Node> nodes;
	private SimpleObjectStore<Way> ways;

	// map the highway level to integers :
	private TObjectIntHashMap<String> wayTypes_2_id;
	private TIntObjectHashMap<String> id_2_wayTypes;

	// stores as key the ids of all used nodes (as vertex or waypoint) :
	TLongIntHashMap usedNodes;

	RoutingGraphWriterTask(String wayTypesCsv, String outputFile) throws FileNotFoundException {
		System.out.println("initializing routing-graph extraction");

		// setup output :
		this.outputFile = new File(outputFile);
		System.out.println("target file = '" + this.outputFile.getAbsolutePath() + "'");
		try {
			this.out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(
					outputFile)), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("the utf-8 encoding is not available", e);
		}

		// put way types on the hashmaps :
		System.out.println("way-types : ");
		this.wayTypes_2_id = new TObjectIntHashMap<String>();
		this.id_2_wayTypes = new TIntObjectHashMap<String>();
		String[] wayTypes = wayTypesCsv.split(",");
		for (int i = 0; i < wayTypes.length; i++) {
			wayTypes_2_id.put(wayTypes[i], i);
			id_2_wayTypes.put(i, wayTypes[i]);
			System.out.println("   " + wayTypes[i] + " (" + i + ")");
		}
		this.usedNodes = new TLongIntHashMap();

		// initialize stores where nodes an ways are temporarily written to :
		this.nodes = new SimpleObjectStore<Node>(new SingleClassObjectSerializationFactory(Node.class),
				"nodes", true);
		this.ways = new SimpleObjectStore<Way>(new SingleClassObjectSerializationFactory(Way.class), "ways",
				true);
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		switch (entity.getType()) {
			case Bound:
				break;
			case Node:
				Node node = (Node) entity;
				// add to store
				nodes.add(node);
				amountOfNodesProcessed++;
				break;
			case Way:
				Way way = (Way) entity;
				if (isOnWhiteList(way) && way.getWayNodes().size() > 1) {
					List<WayNode> waynodes = way.getWayNodes();

					// set start node type to vertex
					WayNode wayNode = waynodes.get(0);
					usedNodes.remove(wayNode.getNodeId());
					usedNodes.put(wayNode.getNodeId(), NODE_TYPE_VERTEX);

					// set end node type to vertex
					wayNode = waynodes.get(waynodes.size() - 1);
					usedNodes.remove(wayNode.getNodeId());
					usedNodes.put(wayNode.getNodeId(), NODE_TYPE_VERTEX);

					// set intermediate node types
					for (int i = 1; i < waynodes.size() - 1; i++) {
						wayNode = waynodes.get(i);
						if (usedNodes.containsKey(wayNode.getNodeId())) {
							// node has been referenced by a different way
							usedNodes.remove(wayNode.getNodeId());
							usedNodes.put(wayNode.getNodeId(), NODE_TYPE_VERTEX);
						} else {
							// node has not been referenced by a different way
							usedNodes.put(wayNode.getNodeId(), NODE_TYPE_WAYPOINT);
						}
					}
					// add to store
					ways.add(way);
				}
				amountOfWaysProcessed++;
				break;
			case Relation:
				amountOfRelationsProcessed++;
				break;
		}
	}

	@Override
	public void complete() {
		// count number of vertices :
		usedNodes.forEachValue(new TIntProcedure() {
			@Override
			public boolean execute(int v) {
				if (v == NODE_TYPE_VERTEX) {
					numVertices++;
				}
				return true;
			}
		});
		// assign ids to vertices and waypoints :
		usedNodes.transformValues(new TIntFunction() {
			int nextVertexId = 0;
			int nextWaypointId = usedNodes.size() - 1;

			@Override
			public int execute(int v) {
				if (v == NODE_TYPE_WAYPOINT) {
					return nextWaypointId--;
				}
				return nextVertexId++;
			}
		});

		// WRITE : createTables.sql

		try {
			writeCreateTables();
		} catch (IOException e) {
			throw new RuntimeException("cannot access file", e);
		}

		// WRITE : highway levels table

		out.println("\n" + SQL_COPY_HWY_LEVELS);
		writeHighwayLevels();
		out.println(SQL_TERMINAL);

		// WRITE : all nodes

		out.println("\n" + SQL_COPY_VERTICES);
		int[] latitudes = new int[usedNodes.size()];
		int[] longitudes = new int[usedNodes.size()];

		ReleasableIterator<Node> iterNodes = nodes.iterate();
		while (iterNodes.hasNext()) {
			Node node = iterNodes.next();
			if (!usedNodes.containsKey(node.getId())) {
				continue;
			}
			int idx = usedNodes.get(node.getId());
			latitudes[idx] = GeoCoordinate.doubleToInt(node.getLatitude());
			longitudes[idx] = GeoCoordinate.doubleToInt(node.getLongitude());
			if (idx < numVertices) {
				// if it is a vertex (not a waypoint) write it to the graph
				writeVertex(idx, node.getId(), GeoCoordinate.intToDouble(longitudes[idx]),
						GeoCoordinate.intToDouble(latitudes[idx]));
			}
		}
		iterNodes.release();
		out.println(SQL_TERMINAL);

		// WRITE : all edges

		out.println("\n" + SQL_COPY_EDGES);
		ReleasableIterator<Way> iterWays = ways.iterate();
		while (iterWays.hasNext()) {
			Way way = iterWays.next();
			transformToEdgesAndWrite(way, latitudes, longitudes);
		}
		iterWays.release();
		out.println(SQL_TERMINAL);

		// FINISH
	}

	@Override
	public void release() {
		// close stream
		out.flush();
		out.close();

		// free ram
		this.usedNodes = null;
		this.wayTypes_2_id = null;
		this.id_2_wayTypes = null;

		// print summary
		System.out.println("routing-graph written to '" + outputFile.getAbsolutePath() + "'");
		System.out.println("amountOfNodesProcessed = " + amountOfNodesProcessed);
		System.out.println("amountOfWaysProcessed = " + amountOfWaysProcessed);
		System.out.println("amountOfRelationsProcessed = " + amountOfRelationsProcessed);
		System.out.println("amountOfVerticesWritten = " + amountOfVerticesWritten);
		System.out.println("amountOfEdgesWritten = " + amountOfEdgesWritten);
	}

	private void transformToEdgesAndWrite(Way way, int[] latitudeE6, int[] longitudeE6) {
		LinkedList<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < way.getWayNodes().size(); i++) {
			int idx = usedNodes.get(way.getWayNodes().get(i).getNodeId());
			if (idx < numVertices) {
				indices.addLast(i);
			}
		}

		for (int i = 1; i < indices.size(); i++) {
			int start = indices.get(i - 1);
			int end = indices.get(i);
			double[] lon = new double[end - start + 1];
			double[] lat = new double[end - start + 1];

			for (int j = start; j <= end; j++) {
				int idx = usedNodes.get(way.getWayNodes().get(j).getNodeId());
				lon[j - start] = GeoCoordinate.intToDouble(longitudeE6[idx]);
				lat[j - start] = GeoCoordinate.intToDouble(latitudeE6[idx]);
			}

			int sourceId;
			int targetId;
			boolean oneway;
			double distanceMeters = 0d;
			for (int k = 1; k < lon.length; k++) {
				distanceMeters += GeoCoordinate.sphericalDistance(lon[k - 1], lat[k - 1], lon[k], lat[k]);
			}
			if (isOneWay(way) == -1) {
				sourceId = usedNodes.get(way.getWayNodes().get(end).getNodeId());
				targetId = usedNodes.get(way.getWayNodes().get(start).getNodeId());
				oneway = true;
				lon = reverse(lon);
				lat = reverse(lat);
			} else {
				sourceId = usedNodes.get(way.getWayNodes().get(start).getNodeId());
				targetId = usedNodes.get(way.getWayNodes().get(end).getNodeId());
				oneway = isOneWay(way) == 1;
			}

			Tag wayName = getTag(way, "name");
			// this is for motorways and primary roads
			Tag wayRef = getTag(way, "ref");
			// this is for motorway links which lead onto a highway

			writeEdge(sourceId, targetId, way.getId(), wayName != null ? wayName.getValue() : null,
					wayRef != null ? wayRef.getValue() : null, null, distanceMeters, !oneway, false,
					isRoundabout(way), wayTypes_2_id.get(getTag(way, "highway").getValue()), lon, lat);

		}
	}

	private void writeCreateTables() throws IOException {
		InputStream iStream = RoutingGraphWriterTask.class.getResourceAsStream(SQL_CREATE_TABLES_FILE);
		byte[] b = new byte[iStream.available()];
		iStream.read(b);
		out.println(new String(b));
		iStream.close();
	}

	private void writeHighwayLevels() {
		for (String key : wayTypes_2_id.keySet()) {
			int value = wayTypes_2_id.get(key);
			out.print(value + "\t");
			out.println(toPgString(key));
		}
	}

	private void writeEdge(int sourceId, int targetId, long wayId, String name, String ref, String destination,
			double lengthMeters, boolean undirected, boolean urban, boolean roundabout, int highwayLevel,
			double[] longitudes, double[] latitudes) {
		int id = amountOfEdgesWritten;
		out.print(id + "\t");
		out.print(sourceId + "\t");
		out.print(targetId + "\t");
		out.print(wayId + "\t");
		out.print(toPgString(name) + "\t");
		out.print(toPgString(ref) + "\t");
		out.print(toPgString(destination) + "\t");
		out.print(lengthMeters + "\t");
		out.print(toPgString(undirected) + "\t");
		out.print(toPgString(urban) + "\t");
		out.print(toPgString(roundabout) + "\t");
		out.print(highwayLevel + "\t");
		out.print(toPgString(longitudes) + "\t");
		out.println(toPgString(latitudes));
		amountOfEdgesWritten++;
	}

	private void writeVertex(int vertexId, long nodeId, double longitude, double latitude) {
		out.print(vertexId + "\t");
		out.print(nodeId + "\t");
		out.print(longitude + "\t");
		out.println(latitude);
		amountOfVerticesWritten++;
	}

	private boolean isOnWhiteList(Way way) {
		for (Tag tag : way.getTags()) {
			if (tag.getKey().equals("highway") && this.wayTypes_2_id.contains(tag.getValue())) {
				return true;
			}
		}
		return false;
	}

	private static Tag getTag(Way way, String tagName) {
		for (Tag tag : way.getTags()) {
			if (tag.getKey().equals(tagName)) {
				return tag;
			}
		}
		return null;
	}

	private static double[] reverse(double[] array) {
		double[] tmp = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			tmp[array.length - 1 - i] = array[i];
		}
		return tmp;
	}

	private static int isOneWay(Way way) {
		Tag hwyTag = getTag(way, "highway");
		if (hwyTag != null
				&& (hwyTag.getValue().equals(TagHighway.MOTORWAY)
						|| hwyTag.getValue().equals(TagHighway.MOTORWAY_LINK)
						|| hwyTag.getValue().equals(TagHighway.TRUNK) || hwyTag.getValue().equals(
						TagHighway.TRUNK_LINK))) {
			return 1;
		}
		Tag onwyTag = getTag(way, "oneway");
		if (onwyTag == null) {
			return 0;
		} else if (onwyTag.getValue().equals("true") || onwyTag.getValue().equals("yes")
				|| onwyTag.getValue().equals("t") || onwyTag.getValue().equals("1")) {
			return 1;
		} else if (onwyTag.getValue().equals("false") || onwyTag.getValue().equals("no")
				|| onwyTag.getValue().equals("f") || onwyTag.getValue().equals("0")) {
			return 0;
		} else if (onwyTag.getValue().equals("-1")) {
			return -1;
		} else {
			return 0;
		}
	}

	private static boolean isRoundabout(Way way) {
		Tag t = getTag(way, "junction");
		if (t == null) {
			return false;

		}
		if (t.getValue().equals("roundabout")) {
			return true;
		}
		return false;
	}

	private static String toPgString(double[] arr) {
		if (arr == null) {
			return "\\null";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < arr.length; i++) {
			sb.append(arr[i]);
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append("}");

		return sb.toString();
	}

	private static String toPgString(boolean b) {
		if (b) {
			return "t";
		}
		return "f";
	}

	private static String toPgString(String s) {
		if (s == null) {
			return "";
		}
		// takes a bit longer to create the sql file but solves some issues with street names
		return s.replaceAll("[\r\n\t\\\\]", "").trim();
	}
}
