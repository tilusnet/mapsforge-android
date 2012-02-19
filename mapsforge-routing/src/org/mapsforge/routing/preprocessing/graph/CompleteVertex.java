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

import java.util.HashSet;

import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;

/**
 * A vertex filled with all information needed
 * 
 * @author Michael Bartel
 * 
 */
public class CompleteVertex implements Vertex {

	int id;
	Edge[] outboundEdges;
	GeoCoordinate coordinate;
	HashSet<KeyValuePair> additionalTags;

	/**
	 * The constructor for the completeVertex
	 * 
	 * @param id
	 *            the OSM-id
	 * @param outboundEdges
	 *            all outgoing edges
	 * @param coordinate
	 *            the geo-coordinate
	 * @param hs
	 *            the hashset for additional tags
	 */
	public CompleteVertex(int id, Edge[] outboundEdges, GeoCoordinate coordinate,
			HashSet<KeyValuePair> hs) {
		super();
		this.id = id;
		this.outboundEdges = outboundEdges;
		this.coordinate = coordinate;
		additionalTags = hs;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Edge[] getOutboundEdges() {
		return outboundEdges;
	}

	@Override
	public GeoCoordinate getCoordinate() {
		return coordinate;
	}

	/**
	 * Adds a new tag to this vertex
	 * 
	 * @param kv
	 *            the tag to be added
	 */
	public void addNewTag(KeyValuePair kv) {
		additionalTags.add(kv);
	}

	/**
	 * Gets the HashSet with the additional Tags
	 * 
	 * @return the HashSets with the given additional Key/Value pairs.
	 */
	public HashSet<KeyValuePair> getAdditionalTags() {
		return additionalTags;
	}

	/**
	 * Sets the identifying int of this vertex
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the outboundEdges for this vertex
	 * 
	 * @param oe
	 *            the new outgoind edges
	 */
	public void setOutboundEdges(Edge[] oe) {
		outboundEdges = oe;
	}

	@Override
	public String toString() {
		String s = "[Vertex " + this.id + "";
		s += "(" + coordinate.getLatitude() + "," + coordinate.getLongitude() + ")";

		s += " TAGS ";
		for (KeyValuePair kv : this.additionalTags) {
			s += kv.toString() + ", ";
		}
		s += " ]";
		return s;
	}

}
