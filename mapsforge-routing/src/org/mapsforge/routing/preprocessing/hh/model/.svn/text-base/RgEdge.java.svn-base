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
package org.mapsforge.routing.preprocessing.hh.model;


/**
 * This class implements an Object representation of edges stored in the routing graph db
 * schema.
 */
public class RgEdge implements IRgEdge {

	private final int id, sourceId, targetId;
	private final double[] longitudes, latitudes;
	private final double lengthMeters;
	private final boolean isUndirected, isUrban, isRoundabout;
	private final long osmWayId;
	private final String name;
	private final String ref; // See http://wiki.openstreetmap.org/wiki/Map_Features#References
	private final String destination; // See http://wiki.openstreetmap.org/wiki/Key:destination
	private final String hwyLevel;

	/**
	 * @param id
	 *            the assigned edge id
	 * @param sourceId
	 *            vertex id.
	 * @param targetId
	 *            vertex id.
	 * @param longitudes
	 *            waypoint longitudes in degrees.
	 * @param latitudes
	 *            waypoint latitudes in degrees.
	 * @param isUndirected
	 *            one way or not
	 * @param isUrban
	 *            within city boundaries StVO.
	 * @param osmWayId
	 *            the osm id of the way.
	 * @param name
	 *            the name of the street.
	 * @param lengthMeters
	 *            the length along the waypoints in meters.
	 * @param hwyLevel
	 *            the highway level
	 * @param ref
	 *            The ref key as it is found in the OSM data
	 * @param isRoundabout
	 *            Whether or not this is a roundabout
	 * @param destination
	 *            Motorway links have a destination which can be used to identify the link
	 */
	public RgEdge(int id, int sourceId, int targetId, double[] longitudes, double[] latitudes,
			boolean isUndirected, boolean isUrban, long osmWayId, String name,
			double lengthMeters, String hwyLevel, String ref, boolean isRoundabout,
			String destination) {
		this.id = id;
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.longitudes = longitudes;
		this.latitudes = latitudes;
		this.isUndirected = isUndirected;
		this.isUrban = isUrban;
		this.isRoundabout = isRoundabout;
		this.osmWayId = osmWayId;
		this.name = name;
		this.ref = ref;
		this.destination = destination;
		this.lengthMeters = lengthMeters;
		this.hwyLevel = hwyLevel;
	}

	/**
	 * @return id of this edge.
	 */
	public int getId() {
		return id;
	}

	@Override
	public double[] getLatitudes() {
		return latitudes;
	}

	@Override
	public double[] getLongitudes() {
		return longitudes;
	}

	@Override
	public int getSourceId() {
		return sourceId;
	}

	@Override
	public int getTargetId() {
		return targetId;
	}

	@Override
	public boolean isUndirected() {
		return isUndirected;
	}

	/**
	 * @return true if edge is within boundaries of a city StVO.
	 */
	public boolean isUrban() {
		return isUrban;
	}

	/**
	 * @return true if edge is part of a roundabout.
	 */
	public boolean isRoundabout() {
		return isRoundabout;
	}

	/**
	 * @return the osm id of the related way.
	 */
	public long getOsmWayId() {
		return osmWayId;
	}

	/**
	 * @return the name of the street.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the ref of the street.
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * @return destination of motorway or motorway link
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @return length along the waypoints in meters.
	 */
	public double getLengthMeters() {
		return lengthMeters;
	}

	/**
	 * @return the highway level.
	 */
	public String getHighwayLevel() {
		return hwyLevel;
	}

	@Override
	public String toString() {
		return "e: " + sourceId + " -> " + targetId + ", length = " + lengthMeters;
	}
}
