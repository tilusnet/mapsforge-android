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
package org.mapsforge.routing.preprocessing.graph;

import gnu.trove.function.TIntFunction;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.procedure.TIntProcedure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;
import org.mapsforge.routing.preprocessing.hh.model.TagHighway;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.xml.sax.SAXException;

/**
 * This task writes a sql file (tested for postgresql 8.4) which represents a routing graph. Executing
 * the resulting file bulk-loads the graph to the database.
 * 
 * As parameter this plugin requires an output-file and a comma separated white-list of osm way types.
 * 
 * 
 */
class RoutingGraphCreatorTask implements Sink {
	// possible values for the node-ids on the hashmap :
	private static final int NODE_TYPE_VERTEX = 2;
	private static final int NODE_TYPE_WAYPOINT = 1;

	private String pbfPath = null;

	// amounting :
	private int amountOfNodesProcessed = 0;
	private int amountOfWaysProcessed = 0;
	private int amountOfRelationsProcessed = 0;

	private int amountOfEdgesWritten = 0;
	private int amountOfVerticesWritten = 0;
	private int amountOfRelationsWritten = 0;

	// counter used to assign ids for vertices
	int numVertices = 0;

	// store all nodes temporarily here :
	private SimpleObjectStore<Node> nodes;
	private SimpleObjectStore<Way> ways;
	private SimpleObjectStore<Relation> relations;

	// List for completely filled objects
	private THashMap<Integer, CompleteVertex> vertices;
	private THashMap<Integer, CompleteEdge> edges;
	private THashMap<Integer, CompleteRelation> completeRelations;

	// List with all needed Nodes (usually on ways)
	THashMap<Integer, CompleteNode> neededNodes;

	private final HashSet<String> remValues = new HashSet<String>(Arrays.asList(new String[] { "name",
			"destination", "ref", "highway" }));
	TLongIntHashMap usedNodes;

	// the config file
	ConfigObject configObject;

	RoutingGraphCreatorTask(String xmlConfigPath, String neededVehicles, String outputPath) {

		System.out.println("[RGC] initializing routing-graph extraction");

		pbfPath = outputPath;
		String[] limiter = null;

		if (neededVehicles != null)
			limiter = neededVehicles.split(",");

		this.usedNodes = new TLongIntHashMap();

		this.neededNodes = new THashMap<Integer, CompleteNode>();

		// initialize stores where nodes an ways are temporarily written to :
		this.nodes = new SimpleObjectStore<Node>(new SingleClassObjectSerializationFactory(
				Node.class), "nodes", true);

		this.ways = new SimpleObjectStore<Way>(new SingleClassObjectSerializationFactory(
				Way.class),
				"ways", true);

		this.relations = new SimpleObjectStore<Relation>(new SingleClassObjectSerializationFactory(
				Relation.class),
				"relations", true);

		try {
			configObject = new XMLReader().parseXML(xmlConfigPath, limiter);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[RGC] processing data...");
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		switch (entity.getType()) {
			case Bound:
				break;
			case Node:
				Node node = (Node) entity;

				// check if node is important and put into hash table
				if (isOnWhiteList(node)) {
					HashSet<KeyValuePair> tags = new HashSet<KeyValuePair>();
					addPairsForTagToHashSet(node, tags);

					neededNodes.put(
							((Long) node.getId()).intValue(),
							new CompleteNode(((Long) node.getId()).intValue(), new GeoCoordinate(node
									.getLatitude(), node
									.getLongitude()), tags));

				}

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
				Relation rel = (Relation) entity;
				if (isOnWhiteList(rel))
					relations.add(rel);
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

		// WRITE : all nodes
		vertices = new THashMap<Integer, CompleteVertex>();
		double[] latitudes = new double[usedNodes.size()];
		double[] longitudes = new double[usedNodes.size()];

		ReleasableIterator<Node> iterNodes = nodes.iterate();
		while (iterNodes.hasNext()) {
			Node node = iterNodes.next();
			if (!usedNodes.containsKey(node.getId())) {
				continue;
			}
			int idx = usedNodes.get(node.getId());
			latitudes[idx] = node.getLatitude();
			longitudes[idx] = node.getLongitude();
			if (idx < numVertices) {

				// if it is a vertex (not a waypoint) write it to the graph
				HashSet<KeyValuePair> hs = new HashSet<KeyValuePair>();

				// Check for tags
				addPairsForTagToHashSet(node, hs);

				// add new vertex
				int key = ((Long) node.getId()).intValue();
				vertices.put(key, new CompleteVertex(key, null,
						new GeoCoordinate(node.getLatitude(), node.getLongitude()), hs));
				amountOfVerticesWritten++;
			}
		}
		iterNodes.release();

		// WRITE : all edges
		edges = new THashMap<Integer, CompleteEdge>();
		ReleasableIterator<Way> iterWays = ways.iterate();
		while (iterWays.hasNext()) {
			Way way = iterWays.next();

			// Check tags and create edges
			transformToEdgesAndWrite(way, latitudes, longitudes);
		}
		iterWays.release();

		// WRITE : all relations
		completeRelations = new THashMap<Integer, CompleteRelation>();

		ReleasableIterator<Relation> iterRelations = relations.iterate();
		while (iterRelations.hasNext()) {
			Relation rel = iterRelations.next();
			// Process, create and add new relation

			completeRelations.put(((Long) rel.getId()).intValue(),
					processRelationAndWrite(rel));

			amountOfRelationsWritten++;
		}
		iterRelations.release();
		// FINISH
	}

	@Override
	public void release() {
		// close stream

		// free ram
		this.usedNodes = null;

		// print summary
		System.out.println("[RGC] amountOfNodesProcessed = " + amountOfNodesProcessed);
		System.out.println("[RGC] amountOfWaysProcessed = " + amountOfWaysProcessed);
		System.out.println("[RGC] amountOfRelationsProcessed = " + amountOfRelationsProcessed);
		System.out.println("[RGC] amountOfVerticesWritten = " + amountOfVerticesWritten);
		System.out.println("[RGC] amountOfEdgesWritten = " + amountOfEdgesWritten + " ");
		System.out.println("[RGC] amountOfRelationsWritten = " + amountOfRelationsWritten);

		// TEST Save and read

		int sum = vertices.size() + edges.size() +
				completeRelations.size();
		System.out.println("[RGC] Writing " + sum + " objects fo file: " + pbfPath);
		ProtobufSerializer.saveToFile(pbfPath, vertices, edges, completeRelations);
		//
		// HashMap<Integer, CompleteVertex> verticesC = new HashMap<Integer, CompleteVertex>();
		// HashMap<Integer, CompleteEdge> edgesC = new HashMap<Integer, CompleteEdge>();
		// HashMap<Integer, CompleteRelation> relationsC = new HashMap<Integer, CompleteRelation>();
		// System.out.println("old Edges: ");
		// for (CompleteEdge edge : edges.values()) {
		// System.out.println(edge.toString());
		// }
		//
		// ProtobufSerializer.loadFromFile(pbfPath, verticesC, edgesC, relationsC);
		// System.out.println("loaded Edges: ");
		// for (CompleteEdge edge : edgesC.values()) {
		// System.out.println(edge.toString());
		// }
		System.out.println("[RGC] Ready ");
	}

	/**
	 * This method transforms a way into an CompleteEdge
	 * 
	 * @param way
	 *            the way to be processed
	 * @param latitudeE6
	 *            an array of latitude-doubles
	 * @param longitudeE6
	 *            an array of longitude-doubles
	 */
	private void transformToEdgesAndWrite(Way way, double[] latitudeE6, double[] longitudeE6) {
		LinkedList<Integer> indices = new LinkedList<Integer>();

		// Check waypoints in between and save
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

			// Save Waypoints as complete nodes
			HashSet<CompleteNode> allWayNodes = new HashSet<CompleteNode>();

			for (int j = start; j <= end; j++) {
				Long tmp = way.getWayNodes().get(j).getNodeId();
				int idx = usedNodes.get(tmp);
				lon[j - start] = (longitudeE6[idx]);
				lat[j - start] = (latitudeE6[idx]);

				if (neededNodes.containsKey(tmp.intValue())) {
					CompleteNode cn = neededNodes.get(tmp.intValue());
					allWayNodes.add(cn);
				}
			}

			int sourceId;
			int targetId;
			boolean oneway;
			double distanceMeters = 0d;
			for (int k = 1; k < lon.length; k++) {
				distanceMeters += GeoCoordinate.sphericalDistance(
						lon[k - 1], lat[k - 1], lon[k], lat[k]);
			}

			// Check if oneway
			if (isOneWay(way) == -1) {
				sourceId = ((Long) way.getWayNodes().get(end).getNodeId()).intValue();
				targetId = ((Long) way.getWayNodes().get(start).getNodeId()).intValue();
				oneway = true;
				lon = reverse(lon);
				lat = reverse(lat);
			} else {
				sourceId = ((Long) way.getWayNodes().get(start).getNodeId()).intValue();
				targetId = ((Long) way.getWayNodes().get(end).getNodeId()).intValue();
				oneway = isOneWay(way) == 1;
			}

			// this is for motorways and primary roads
			Tag wayName = null;
			if (configObject.containsWayTagKey("name"))
				wayName = getTag(way, "name");

			// this is for motorway links which lead onto a highway
			Tag wayRef = null;
			if (configObject.containsWayTagKey("ref"))
				wayRef = getTag(way, "ref");

			// this is for destination of a link
			Tag wayDest = null;
			if (configObject.containsWayTagKey("destination"))
				wayDest = getTag(way, "destination");

			// type of the highway
			Tag wayType = getTag(way, "highway");

			if (wayType != null) {
				if (!(configObject.containsWayTag(wayType.getKey(), wayType.getValue())))
					wayType = null;
			}

			// Save the coordinates of all waypoints
			GeoCoordinate[] allwp = new GeoCoordinate[lon.length];
			if (lat.length != lon.length) {
				System.out.println("FATAL error lat.length!=lon.length ");
				break;
			}

			for (int m = 0; m < allwp.length; m++) {
				allwp[m] = new GeoCoordinate(lat[m], lon[m]);
			}

			HashSet<KeyValuePair> hs = new HashSet<KeyValuePair>();

			// check all tags
			addPairsForTagToHashSet(way, hs);

			amountOfEdgesWritten++;
			// create the new edge
			int key = ((Long) way.getId()).intValue();

			CompleteEdge ce = new CompleteEdge(key,
					getVertexFromList(sourceId),
					getVertexFromList(targetId),
					null,
					allwp,
					wayName != null ? wayName.getValue() : null,
					wayType != null ? wayType.getValue() : null,
					isRoundabout(way),
					oneway,
					wayRef != null ? wayRef.getValue() : null,
					wayDest != null ? wayDest.getValue() : null,
					0,
					hs,
					allWayNodes);

			edges.put(amountOfEdgesWritten, ce);

		}
	}

	private CompleteRelation processRelationAndWrite(Relation rel) {
		HashSet<KeyValuePair> hs = new HashSet<KeyValuePair>();

		RelationMember[] relMember = new RelationMember[rel.getMembers().size()];
		int i = 0;

		// create serializable RelationmemberObjects
		List<RelationMember> memberList = new ArrayList<RelationMember>();
		for (RelationMember rm : rel.getMembers()) {
			memberList.add(new RelationMember(rm.getMemberId(), rm.getMemberType(), rm
					.getMemberRole()));
		}
		for (RelationMember rm : memberList) {
			relMember[i] = rm;
			i++;
		}

		addPairsForTagToHashSet(rel, hs);

		return new CompleteRelation(relMember, hs);
	}

	private Vertex getVertexFromList(int id) {
		return vertices.get(id);
	}

	private boolean isOnWhiteList(Way way) {

		// TODO CHECK if correct or if flag necessary as below (I think it doesn't matter and was
		// correct before)!!!
		for (Tag tag : way.getTags()) {
			if ((tag.getKey().equals("highway") && (this.configObject
						.containsWayTag(tag.getKey(),
								tag.getValue()))))
				return true;

			if ((tag.getKey().equals("railway") && (this.configObject
						.containsWayTag(tag.getKey(),
								tag.getValue())))) {
				return true;
			}
		}
		return false;
	}

	private boolean isOnWhiteList(Node node) {

		for (Tag tag : node.getTags()) {
			if (this.configObject.containsNodeTag(tag.getKey(), tag.getValue()))
				return true;
		}
		return false;
	}

	private boolean isOnWhiteList(Relation rel) {

		for (Tag tag : rel.getTags()) {
			if (this.configObject.containsNodeTag(tag.getKey(), tag.getValue()))
				return true;
		}
		return false;
	}

	private void addPairsForTagToHashSet(Node node, HashSet<KeyValuePair> hs) {
		for (Tag tag : node.getTags()) {

			if (configObject.containsNodeTag(tag.getKey(), tag.getValue())) {
				hs.add(new KeyValuePair(tag.getValue(), tag.getKey()));
			}
		}
	}

	private void addPairsForTagToHashSet(Relation relation, HashSet<KeyValuePair> hs) {
		for (Tag tag : relation.getTags()) {
			if (configObject.containsRelationTag(tag.getKey(), tag.getValue()))
				hs.add(new KeyValuePair(tag.getValue(), tag.getKey()));
		}
	}

	private void addPairsForTagToHashSet(Way way, HashSet<KeyValuePair> hs) {
		for (Tag tag : way.getTags()) {

			if (remValues.contains(tag.getKey()))
				continue;

			if (configObject.containsWayTag(tag.getKey(), tag.getValue()))
				hs.add(new KeyValuePair(tag.getValue(), tag.getKey()));
		}
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
						|| hwyTag.getValue().equals(TagHighway.TRUNK) || hwyTag.getValue()
						.equals(TagHighway.TRUNK_LINK))) {
			return 1;
		}
		Tag onwyTag = getTag(way, "oneway");
		if (onwyTag == null) {
			return 0;
		} else if (onwyTag.getValue().equals("true")
				|| onwyTag.getValue().equals("yes")
				|| onwyTag.getValue().equals("t")
				|| onwyTag.getValue().equals("1")) {
			return 1;
		} else if (onwyTag.getValue().equals("false") || onwyTag.getValue().equals("no")
				|| onwyTag.getValue().equals("f")
				|| onwyTag.getValue().equals("0")) {
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
}
