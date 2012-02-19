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

import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.AllGraphDataPBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.CompleteEdgePBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.CompleteNodePBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.CompleteRelationPBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.CompleteVertexPBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.GeoCoordinatePBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.KeyValuePairPBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.RelationMemberPBF;
import org.mapsforge.routing.preprocessing.graph.GraphCreatorProtos.RelationMemberPBF.MemberType;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

/**
 * This class can be used to save or load the data, necessary for a routing graph.
 * 
 * @author Michael Bartel
 * 
 */
public class ProtobufSerializer {

	static int processCounter = 0;

	/**
	 * This methods loads data from a pbf-file.
	 * 
	 * @param path
	 *            , where the file is located
	 * @param vertices
	 *            , all nodes will be written into that map
	 * @param edges
	 *            , all nodes will be written into that map
	 * @param relations
	 *            , all nodes will be written into that map
	 */
	public static void loadFromFile(String path, THashMap<Integer, CompleteVertex> vertices,
			THashMap<Integer, CompleteEdge> edges,
			THashMap<Integer, CompleteRelation> relations) {
		AllGraphDataPBF allGraphData = null;
		try {
			allGraphData = AllGraphDataPBF.parseFrom(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
		String dateTime = sdf.format(new Date());

		System.out.println("[" + dateTime + "] Start of object(" + path + ") read!!");
		readVertices(allGraphData, vertices);
		readEdges(allGraphData, edges, vertices);
		readRelations(allGraphData, relations);
		dateTime = sdf.format(new Date());
		System.out.println("[" + dateTime + "] End of object read!!");

	}

	/**
	 * This method saves the lists to a protobuf file, using GraphCreatorProtos.java
	 * 
	 * @param path
	 *            , where the file is located
	 * @param vertices
	 *            the vertices to be saved
	 * @param edges
	 *            the edges to be saved
	 * @param relations
	 *            the relations to be saved
	 */
	public static void saveToFile(String path,
			THashMap<Integer, CompleteVertex> vertices,
			THashMap<Integer, CompleteEdge> edges,
			THashMap<Integer, CompleteRelation> relations) {

		AllGraphDataPBF.Builder allGraphData = AllGraphDataPBF.newBuilder();

		System.out.println("[RGC] Creating PBF edges");
		writeEdges(allGraphData, edges);

		System.out.println("[RGC] Creating PBF vertices");
		writeVertices(allGraphData, vertices);

		System.out.println("[RGC] Creating PBF relations");
		writeRelations(allGraphData, relations);

		System.out.println("[RGC] Writing...");
		FileOutputStream output;
		try {
			output = new FileOutputStream(path);
			allGraphData.build().writeTo(output);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void readEdges(AllGraphDataPBF allGraphData,
			THashMap<Integer, CompleteEdge> edges, THashMap<Integer, CompleteVertex> vertices) {
		edges.clear();

		int i = 1;
		for (CompleteEdgePBF ce_pbf : allGraphData.getAllEdgesList()) {

			HashSet<KeyValuePair> additionalTags = new HashSet<KeyValuePair>();

			for (KeyValuePairPBF kv_pbf : ce_pbf.getAdditionalTagsList()) {
				additionalTags.add(new KeyValuePair(kv_pbf.getValue(), kv_pbf.getKey()));
			}

			GeoCoordinate[] allWP = new GeoCoordinate[ce_pbf.getAllWaypointsCount()];

			for (int j = 0; j < allWP.length; j++) {
				GeoCoordinatePBF geo_pbf = ce_pbf.getAllWaypointsList().get(j);
				allWP[j] = new GeoCoordinate(geo_pbf.getLatitude(), geo_pbf.getLongitude());
			}
			// read nodes
			HashSet<CompleteNode> allUsedNodes = new HashSet<CompleteNode>();

			for (CompleteNodePBF node_pbf : ce_pbf.getAllUsedNodesList()) {
				GeoCoordinate coordinate = new GeoCoordinate(node_pbf.getCoordinate().getLatitude(),
						node_pbf.getCoordinate().getLongitude());
				HashSet<KeyValuePair> hs = new HashSet<KeyValuePair>();

				for (KeyValuePairPBF kv_pbf : node_pbf.getAdditionalTagsList()) {
					hs.add(new KeyValuePair(kv_pbf.getValue(), kv_pbf.getKey()));
				}

				allUsedNodes.add(new CompleteNode(node_pbf.getId(), coordinate, hs));
			}

			CompleteEdge ce = new CompleteEdge(
					ce_pbf.getId(),
					vertices.get(ce_pbf.getSourceID()),
					vertices.get(ce_pbf.getTargetID()),
					null,
					allWP,
					ce_pbf.getName(),
					ce_pbf.getType(),
					ce_pbf.getRoundabout(),
					ce_pbf.getIsOneWay(),
					ce_pbf.getRef(),
					ce_pbf.getDestination(),
					ce_pbf.getWeight(),
					additionalTags,
					allUsedNodes);

			edges.put(i, ce);
			i++;

		}

	}

	private static void readVertices(AllGraphDataPBF allGraphData,
			THashMap<Integer, CompleteVertex> vertices) {

		vertices.clear();

		for (CompleteVertexPBF cv_pbf : allGraphData.getAllVerticesList()) {

			HashSet<KeyValuePair> additionalTags = new HashSet<KeyValuePair>();

			for (KeyValuePairPBF kv_PBF : cv_pbf.getAdditionalTagsList()) {
				additionalTags.add(new KeyValuePair(kv_PBF.getValue(), kv_PBF.getKey()));
			}

			CompleteVertex cv = new CompleteVertex(cv_pbf.getId(),
					null,
					new GeoCoordinate(cv_pbf.getCoordinate().getLatitude(), cv_pbf
							.getCoordinate().getLongitude()),
					additionalTags);
			vertices.put(cv.id, cv);

		}
	}

	private static void readRelations(AllGraphDataPBF allGraphData,
			THashMap<Integer, CompleteRelation> relations) {

		relations.clear();

		int j = 0;
		for (CompleteRelationPBF cr_pbf : allGraphData.getAllRelationsList()) {

			RelationMember[] member = new RelationMember[cr_pbf.getMemberCount()];

			int i = 0;
			for (RelationMemberPBF rm_pbf : cr_pbf.getMemberList()) {
				EntityType memberType = null;

				if (rm_pbf.getMemberType() == MemberType.NODE)
					memberType = EntityType.Node;
				if (rm_pbf.getMemberType() == MemberType.WAY)
					memberType = EntityType.Way;
				if (rm_pbf.getMemberType() == MemberType.RELATION)
					memberType = EntityType.Relation;

				member[i] = new RelationMember(rm_pbf.getMemberId(), memberType,
						rm_pbf.getMemberRole());
				i++;
			}

			HashSet<KeyValuePair> additionalTags = new HashSet<KeyValuePair>();

			for (KeyValuePairPBF kv_PBF : cr_pbf.getTagsList()) {
				additionalTags.add(new KeyValuePair(kv_PBF.getValue(), kv_PBF.getKey()));
			}
			CompleteRelation cr = new CompleteRelation(member, additionalTags);
			relations.put(j, cr);
			j++;
		}

	}

	private static void writeEdges(final AllGraphDataPBF.Builder allGraphData,
			THashMap<Integer, CompleteEdge> edges) {

		edges.forEachValue(new TObjectProcedure<CompleteEdge>() {

			@Override
			public boolean execute(CompleteEdge ce) {
				processCounter++;
				if (processCounter % 50000 == 0)
					System.out.println("[RGC] edges process: " + processCounter);

				// Prevent crash if OSM-Data is corrupt
				if ((ce.source != null) && (ce.target != null)) {
					allGraphData.addAllEdges(writeSingleEdge(ce));
				}
				return true;
			}
		});

		// Old version remove final in arguments

		/*
		 * for (CompleteEdge ce : edges.values()) { allGraphData.addAllEdges(writeSingleEdge(ce)); }
		 */

	}

	static CompleteEdgePBF.Builder writeSingleEdge(CompleteEdge ce) {
		CompleteEdgePBF.Builder ce_PBF = CompleteEdgePBF.newBuilder();
		ce_PBF.setId(ce.id);

		if (ce.source == null)
			System.out.println(ce);

		ce_PBF.setSourceID(ce.source.getId());
		ce_PBF.setTargetID(ce.target.getId());
		if (ce.name != null)
			ce_PBF.setName(ce.name);
		if (ce.type != null)
			ce_PBF.setType(ce.type);
		ce_PBF.setRoundabout(ce.roundabout);
		ce_PBF.setIsOneWay(ce.isOneWay);
		if (ce.ref != null)
			ce_PBF.setRef(ce.ref);
		if (ce.destination != null)
			ce_PBF.setDestination(ce.destination);
		ce_PBF.setWeight(ce.weight);

		for (KeyValuePair kv : ce.additionalTags) {
			KeyValuePairPBF.Builder kv_PBF = KeyValuePairPBF.newBuilder().setKey(kv.key);
			kv_PBF.setValue(kv.value);
			ce_PBF.addAdditionalTags(kv_PBF);
		}

		for (GeoCoordinate geo : ce.allWaypoints) {
			GeoCoordinatePBF.Builder geo_PBF = GeoCoordinatePBF.newBuilder();
			geo_PBF.setLatitude(geo.getLatitude());
			geo_PBF.setLongitude(geo.getLongitude());
			ce_PBF.addAllWaypoints(geo_PBF);
		}
		// write nodes
		for (CompleteNode node : ce.allUsedNodes) {
			CompleteNodePBF.Builder node_PBF = CompleteNodePBF.newBuilder();
			node_PBF.setId(node.id);

			GeoCoordinatePBF.Builder geo_PBF = GeoCoordinatePBF.newBuilder();
			geo_PBF.setLatitude(node.coordinate.getLatitude());
			geo_PBF.setLongitude(node.coordinate.getLongitude());
			node_PBF.setCoordinate(geo_PBF);

			for (KeyValuePair kv : node.additionalTags) {
				KeyValuePairPBF.Builder kv_PBF = KeyValuePairPBF.newBuilder();
				kv_PBF.setKey(kv.key);
				kv_PBF.setValue(kv.value);
				node_PBF.addAdditionalTags(kv_PBF);
			}

			ce_PBF.addAllUsedNodes(node_PBF);

		}

		return ce_PBF;
	}

	private static void writeVertices(final AllGraphDataPBF.Builder allGraphData,
			THashMap<Integer, CompleteVertex> vertices) {

		vertices.forEachValue(new TObjectProcedure<CompleteVertex>() {

			@Override
			public boolean execute(CompleteVertex arg0) {
				allGraphData.addAllVertices(writeSingleVertex(arg0));
				return true;
			}
		});

		// Old version remove final in arguments
		/*
		 * for (CompleteVertex cv : vertices.values()) {
		 * allGraphData.addAllVertices(writeSingleVertex(cv)); }
		 */
	}

	static CompleteVertexPBF.Builder writeSingleVertex(CompleteVertex cv) {
		CompleteVertexPBF.Builder cv_PBF = CompleteVertexPBF.newBuilder();
		cv_PBF.setId(cv.id);

		GeoCoordinatePBF.Builder geo_PBF = GeoCoordinatePBF.newBuilder();
		geo_PBF.setLatitude(cv.coordinate.getLatitude());
		geo_PBF.setLongitude(cv.coordinate.getLongitude());
		cv_PBF.setCoordinate(geo_PBF);

		for (KeyValuePair kv : cv.additionalTags) {
			KeyValuePairPBF.Builder kv_PBF = KeyValuePairPBF.newBuilder();
			kv_PBF.setKey(kv.key);
			kv_PBF.setValue(kv.value);
			cv_PBF.addAdditionalTags(kv_PBF);
		}

		return cv_PBF;
	}

	private static void writeRelations(final AllGraphDataPBF.Builder allGraphData,
			THashMap<Integer, CompleteRelation> relations) {

		relations.forEachValue(new TObjectProcedure<CompleteRelation>() {

			@Override
			public boolean execute(CompleteRelation arg0) {
				allGraphData.addAllRelations(writeSingleRelation(arg0));
				return true;
			}

		});

		// Old version (remove final in arguments)
		/*
		 * for (CompleteRelation cr : relations.values()) {
		 * allGraphData.addAllRelations(writeSingleRelation(cr)); }
		 */

	}

	static CompleteRelationPBF.Builder writeSingleRelation(CompleteRelation cr) {
		CompleteRelationPBF.Builder cr_PBF = CompleteRelationPBF.newBuilder();

		for (RelationMember rm : cr.member) {
			RelationMemberPBF.Builder rm_PBF =
					RelationMemberPBF.newBuilder();

			rm_PBF.setMemberId(rm.getMemberId());
			rm_PBF.setMemberRole(rm.getMemberRole());

			switch (rm.getMemberType()) {
				case Node:
					rm_PBF.setMemberType(MemberType.NODE);
					break;
				case Way:
					rm_PBF.setMemberType(MemberType.WAY);
					break;
				case Relation:
					rm_PBF.setMemberType(MemberType.RELATION);
					break;
				case Bound:
					break;
			}

			cr_PBF.addMember(rm_PBF);
		}

		for (KeyValuePair kv : cr.tags) {
			KeyValuePairPBF.Builder kv_PBF =
					KeyValuePairPBF.newBuilder();
			kv_PBF.setKey(kv.key);
			kv_PBF.setValue(kv.value);
			cr_PBF.addTags(kv_PBF);
		}

		return cr_PBF;
	}

}
