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
package org.mapsforge.routing.android.hh;

import org.mapsforge.routing.android.hh.ObjectPool.Poolable;

/**
 * A package visible edge. Decided not to use of information hiding here, since it would result
 * in very basic simple getters and setters here. This class it not implemented as immutable
 * since it objects are recycled by using object pooling. It holds the information required by
 * the hh algorithm as well as satellite data e.g. way points, names...
 */
final class HHEdge implements Poolable {
	/**
	 * Identifier of the source vertex.
	 */
	int sourceId;
	/**
	 * Identifier of the target vertex.
	 */
	int targetId;
	/**
	 * The weight of this edge.
	 */
	int weight;
	/**
	 * the minimum level this edge belongs to. The edge is not a shortcut if and only if this
	 * minimum level is 0.
	 */
	int minLevel;
	/**
	 * true means, the edge can be used by forward search.
	 */
	boolean isForward;
	/**
	 * true means, this edge can be used by backward search.
	 */
	boolean isBackward;
	/**
	 * true if and only if this edge belongs to the core of its level.
	 */
	boolean isCore;
	/**
	 * osm street type mapped to a byte, by the routingraph.
	 */
	byte osmStreetType;
	/**
	 * true if this ede is part of a roundabout (osm tag).
	 */
	boolean isRoundAbout;
	/**
	 * This variable is used for implementing the poolable interface. It is set to true if this
	 * edge is released to some object pool.
	 */
	boolean isReleased;
	/**
	 * The street name of this edge (osm tag)
	 */
	byte[] name;
	/**
	 * A name of this edge of some higher level of the name hierarchy, e.g. motor way number
	 * (osm tag).
	 */
	byte[] ref;
	/**
	 * The way points of this edge sorted from source to target excluding the coordinate of
	 * source and target vertices. The way points are stored by alternating between latitude and
	 * longitude.
	 */
	int[] waypoints;
	/**
	 * This is only used if the graph stores hop indices which is not necessarily the case. It
	 * is only used for shortcuts. The hop indices are indices to the adjacency list. the first
	 * hop index is the index to the adjacency list of the source of this edge in the level:
	 * (minLevel - 1) This way shortcuts can be expanded without performing a dijkstra search.
	 */
	int[] hopIndices;

	/**
	 * Constructor usable for obejct pooling.
	 */
	HHEdge() {
		// do nothing is faster than clearing this object.
	}

	@Override
	public boolean isReleased() {
		return this.isReleased;
	}

	@Override
	public void setReleased(boolean b) {
		this.isReleased = b;
	}
}
