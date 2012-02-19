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
 * This class implements an Object representation of the vertices stored in the routing graph db
 * schema.
 */
public class RgVertex implements IRgVertex {

	private final int id;
	private final double longitude, latitude;
	private final long osmNodeId;

	/**
	 * @param id
	 *            the assigned id within the routing graph
	 * @param longitude
	 *            longitude in degrees.
	 * @param latitude
	 *            latitude in degrees.
	 * @param osmNodeId
	 *            the osm node id.
	 */
	public RgVertex(int id, double longitude, double latitude, long osmNodeId) {
		this.id = id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.osmNodeId = osmNodeId;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return retuns the osm id of this node.
	 */
	public long getOsmNodeId() {
		return osmNodeId;
	}

	@Override
	public boolean isDummy() {
		return false;
	}

	@Override
	public String toString() {
		return "v: id=" + id + ", coord=(" + longitude + ", " + latitude + ")";
	}
}
