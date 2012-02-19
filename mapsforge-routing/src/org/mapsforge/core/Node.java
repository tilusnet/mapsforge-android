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
package org.mapsforge.core;

import java.util.LinkedList;

/**
 * This class represents a node from the OpenStreetMap data. All mandatory fields are final. All
 * other fields may be set via their corresponding set method.
 */
public class Node implements Comparable<Node> {
	private int elevation;
	private String houseNumber;
	private final long id;
	private final double latitude;
	private byte layer;
	private final double longitude;
	private String name;
	private LinkedList<String> tags;

	/**
	 * Constructs a new Node with the given parameters.
	 * 
	 * @param id
	 *            the node ID.
	 * @param latitude
	 *            the node latitude.
	 * @param longitude
	 *            the node longitude.
	 */
	public Node(long id, double latitude, double longitude) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public int compareTo(Node node) {
		if (this.id > node.id) {
			return 1;
		} else if (this.id < node.id) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof Node)) {
			return false;
		} else {
			Node other = (Node) obj;
			if (this.elevation != other.elevation) {
				return false;
			} else if (this.houseNumber == null && other.houseNumber != null) {
				return false;
			} else if (!this.houseNumber.equals(other.houseNumber)) {
				return false;
			} else if (this.id != other.id) {
				return false;
			} else if (this.latitude != other.latitude) {
				return false;
			} else if (this.layer != other.layer) {
				return false;
			} else if (this.longitude != other.longitude) {
				return false;
			} else if (this.name == null && other.name != null) {
				return false;
			} else if (!this.name.equals(other.name)) {
				return false;
			} else if (this.tags == null && other.tags != null) {
				return false;
			} else if (!this.tags.equals(other.tags)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Returns the elevation of this node.
	 * 
	 * @return the elevation of this node.
	 */
	public int getElevation() {
		return this.elevation;
	}

	/**
	 * Returns the house number of this node. This may be null if no house number was set.
	 * 
	 * @return the house number of this node.
	 */
	public String getHouseNumber() {
		return this.houseNumber;
	}

	/**
	 * Returns the ID of this node.
	 * 
	 * @return the ID of this node.
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Returns the latitude of this node.
	 * 
	 * @return the latitude of this node.
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * Returns the layer of this node.
	 * 
	 * @return the layer of this node.
	 */
	public byte getLayer() {
		return this.layer;
	}

	/**
	 * Returns the longitude of this node.
	 * 
	 * @return the longitude of this node.
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * Returns the name of this node. This may be null if no name was set.
	 * 
	 * @return the name of this node.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the tags of this node. This may be null if no tags were set.
	 * 
	 * @return the tags of this node.
	 */
	public LinkedList<String> getTags() {
		return this.tags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.elevation;
		result = prime * result
				+ ((this.houseNumber == null) ? 0 : this.houseNumber.hashCode());
		result = prime * result + (int) (this.id ^ (this.id >>> 32));
		long temp;
		temp = Double.doubleToLongBits(this.latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + this.layer;
		temp = Double.doubleToLongBits(this.longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.tags == null) ? 0 : this.tags.hashCode());
		return result;
	}

	/**
	 * Sets the elevation of this node.
	 * 
	 * @param elevation
	 *            the elevation of this node.
	 */
	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	/**
	 * Sets the house number of this node.
	 * 
	 * @param houseNumber
	 *            the house number of this node.
	 */
	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	/**
	 * Sets the layer of this node.
	 * 
	 * @param layer
	 *            the layer of this node.
	 */
	public void setLayer(byte layer) {
		this.layer = layer;
	}

	/**
	 * Sets the name of this node.
	 * 
	 * @param name
	 *            the name of this node.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the tags of this node.
	 * 
	 * @param tags
	 *            the tags of this node.
	 */
	public void setTags(LinkedList<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "ID: " + this.id + ", latitude: " + this.latitude + ", longitude: "
				+ this.longitude;
	}
}