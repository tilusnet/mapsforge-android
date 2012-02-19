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
 * This class represents a way from the OpenStreetMap data. All mandatory fields are final. All
 * other fields may be set via their corresponding set method.
 */
public class Way implements Comparable<Way> {
	private final long id;
	private byte layer;
	private String name;
	private final LinkedList<Long> nodesSequence;
	private LinkedList<String> tags;

	/**
	 * Constructs a new Way with the given parameters.
	 * 
	 * @param id
	 *            the way ID.
	 * @param nodesSequence
	 *            the nodes sequence.
	 */
	public Way(long id, LinkedList<Long> nodesSequence) {
		this.id = id;
		this.nodesSequence = nodesSequence;
	}

	@Override
	public int compareTo(Way way) {
		if (this.id > way.id) {
			return 1;
		} else if (this.id < way.id) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof Way)) {
			return false;
		} else {
			Way other = (Way) obj;
			if (this.id != other.id) {
				return false;
			} else if (this.layer != other.layer) {
				return false;
			} else if (this.name == null && other.name != null) {
				return false;
			} else if (!this.name.equals(other.name)) {
				return false;
			} else if (this.nodesSequence == null && other.nodesSequence != null) {
				return false;
			} else if (!this.nodesSequence.equals(other.nodesSequence)) {
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
	 * Returns the ID of this way.
	 * 
	 * @return the ID of this way.
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Returns the layer of this way.
	 * 
	 * @return the layer of this way.
	 */
	public byte getLayer() {
		return this.layer;
	}

	/**
	 * Returns the name of this way. This may be null if no name was set.
	 * 
	 * @return the name of this way.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the nodes sequence of this way. This may be null if no nodes sequence was set.
	 * 
	 * @return the nodes sequence of this way.
	 */
	public LinkedList<Long> getNodesSequence() {
		return this.nodesSequence;
	}

	/**
	 * Returns the tags of this way. This may be null if no tags were set.
	 * 
	 * @return the tags of this way.
	 */
	public LinkedList<String> getTags() {
		return this.tags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.id ^ (this.id >>> 32));
		result = prime * result + this.layer;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result
				+ ((this.nodesSequence == null) ? 0 : this.nodesSequence.hashCode());
		result = prime * result + ((this.tags == null) ? 0 : this.tags.hashCode());
		return result;
	}

	/**
	 * Sets the layer of this way.
	 * 
	 * @param layer
	 *            the layer of this way.
	 */
	public void setLayer(byte layer) {
		this.layer = layer;
	}

	/**
	 * Sets the name of this way.
	 * 
	 * @param name
	 *            the name of this way.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the tags of this way.
	 * 
	 * @param tags
	 *            the tags of this way.
	 */
	public void setTags(LinkedList<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "ID: " + this.id;
	}
}