/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.routing.preprocessing.graph;

import java.util.HashSet;
import java.util.Set;

import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;

/**
 * An edge filled with maximal Data from OpenStreetMap.
 * 
 * @author Michael Bartel
 * 
 */
public class CompleteEdge implements Edge {

	int id;
	Vertex source;
	Vertex target;
	GeoCoordinate[] allWaypoints;
	String name;
	String type;
	boolean roundabout;
	boolean isOneWay;
	String ref;
	String destination;
	int weight;
	HashSet<KeyValuePair> additionalTags;
	HashSet<CompleteNode> allUsedNodes;

	/**
	 * The Constructor to create a CompleteEdge-instance
	 * 
	 * @param id
	 *            the OSM-ID
	 * @param source
	 *            The source-vertex of the edge.
	 * @param target
	 *            The target-vertex of the edge.
	 * @param waypoints
	 *            The waypoints excluding source and target.
	 * @param allWaypoints
	 *            The waypoints including source and target.
	 * @param name
	 *            The name of the street.
	 * @param type
	 *            The type of the street.
	 * @param roundabout
	 *            Is this way a roundabout.
	 * @param isOneWay
	 *            Is the street oneway?
	 * @param ref
	 *            This reference means another description of a street e.g "B15".
	 * @param destination
	 *            The destination of motor-links e.g. "Leipzig München".
	 * @param weight
	 *            The weight for routing.
	 * @param additionalTags
	 *            the additional Tags that exist for this way
	 * @param allUsedNodes
	 *            All nodes with id, tag and coordinate.
	 */
	public CompleteEdge(int id, Vertex source, Vertex target, GeoCoordinate[] waypoints,
			GeoCoordinate[] allWaypoints, String name, String type, boolean roundabout,
			boolean isOneWay, String ref,
			String destination, int weight, HashSet<KeyValuePair> additionalTags,
			HashSet<CompleteNode> allUsedNodes) {
		super();
		this.id = id;
		this.source = source;
		this.target = target;
		this.allWaypoints = allWaypoints;
		this.name = name;
		this.type = type;
		this.roundabout = roundabout;
		this.isOneWay = isOneWay;
		this.ref = ref;
		this.destination = destination;
		this.weight = weight;
		this.additionalTags = additionalTags;
		this.allUsedNodes = allUsedNodes;
	}

	/**
	 * Adds a new additional Tag key/value pair to this way
	 * 
	 * @param sp
	 *            the new pair to be added
	 */
	public void addAdditionalTags(KeyValuePair sp) {
		this.additionalTags.add(sp);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Vertex getSource() {
		return source;
	}

	@Override
	public Vertex getTarget() {
		return target;
	}

	@Override
	public GeoCoordinate[] getWaypoints() {
		GeoCoordinate[] wp = new GeoCoordinate[allWaypoints.length - 2];
		System.arraycopy(allWaypoints, 1, wp, 0, allWaypoints.length - 2);
		return wp;
	}

	@Override
	public GeoCoordinate[] getAllWaypoints() {
		return allWaypoints;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isRoundabout() {
		return roundabout;
	}

	@Override
	public String getRef() {
		return ref;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	/**
	 * Returns the additional tags for this way
	 * 
	 * @return The set of restrictions for this way
	 */
	public Set<KeyValuePair> getAdditionalTags() {
		return additionalTags;
	}

	/**
	 * Returns the tagged and used nodes of a way
	 * 
	 * @return The set of nodes
	 */
	public HashSet<CompleteNode> getAllUsedNodes() {
		return allUsedNodes;
	}

	/**
	 * Returns true if the street is a oneway street
	 * 
	 * @return true if the street is a oneway street
	 */
	public boolean isOneWay() {
		return isOneWay;
	}

	/**
	 * Sets the source vertex
	 * 
	 * @param source
	 *            the vertex that represents the source
	 */
	public void setSource(Vertex source) {
		this.source = source;
	}

	/**
	 * Sets the target vertex
	 * 
	 * @param target
	 *            the vertex that represents the target
	 */
	public void setTarget(Vertex target) {
		this.target = target;
	}

	/**
	 * Sets the waypoints of this edge
	 * 
	 * @param allWaypoints
	 *            the new waypoints to be set
	 */
	public void setAllWaypoints(GeoCoordinate[] allWaypoints) {
		this.allWaypoints = allWaypoints;
	}

	/**
	 * Sets the type of this edge
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * sets the weight for this edge
	 * 
	 * @param weight
	 *            the new weight
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Sets the used nodes of this edge
	 * 
	 * @param allUsedNodes
	 *            the hashset of new nodes
	 */
	public void setAllUsedNodes(HashSet<CompleteNode> allUsedNodes) {
		this.allUsedNodes = allUsedNodes;
	}

	/**
	 * Sets the reference field
	 * 
	 * @param ref
	 *            the new reference to be set
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}

	/**
	 * Sets the name
	 * 
	 * @param name
	 *            the new name to be set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the destination
	 * 
	 * @param destination
	 *            the new destination to be set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * Sets the ID
	 * 
	 * @param id
	 *            the new id to be set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String s = "[Way " + this.id;
		if (source != null)
			s += " source-ID: " + this.source.getId();
		if (target != null)
			s += " target-ID: " + this.target.getId();
		if (type != null)
			s += " type: " + this.type;
		if (name != null)
			s += " name: " + this.name;
		s += " WAYPOINTS ";
		for (GeoCoordinate geo : this.allWaypoints)
			s += geo.getLatitude() + " " + geo.getLongitude() + ", ";
		s += " TAGS ";
		for (KeyValuePair kv : this.additionalTags) {
			s += kv.toString() + ", ";
		}
		s += " Nodes ";

		for (CompleteNode node : this.allUsedNodes) {
			s += node.toString() + ", ";
		}
		s += "]";
		return s;
	}
}
