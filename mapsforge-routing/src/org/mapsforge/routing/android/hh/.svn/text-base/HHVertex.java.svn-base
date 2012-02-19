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
 * A package visible vertex. Decided not to use of information hiding here, since it would
 * result in very basic simple getters and setters here. This class it not implemented as
 * immutable since it objects are recycled by using obejct pooling.
 */
final class HHVertex implements Poolable {

	/**
	 * This variable is used for implementing the poolable interface. It is set to true if this
	 * object is released to a pool.
	 */
	private boolean isReleased;
	/**
	 * This holds the identifiers of this vertex. Each vertex has different identifiers for each
	 * level. Each vertex of level n holds identifiers of the levels 0 to (n + 1). if the vertex
	 * is not present in level (n + 1) the identifier of that level is set to -1;
	 */
	int[] vertexIds;
	/**
	 * The neighborhood of the vertex which is >= 0. The value Integer.MAX_VALUE represents
	 * infinity.
	 */
	int neighborhood;
	/**
	 * The bitOffset to the first outbound edge within the block. This variable is used to
	 * efficiently implement the getOutbound edges functionality.
	 */
	int bitOffsetFirstOutboundEdge;
	/**
	 * The latitude of this vertex in micro degrees. It is set to -1 if this vertex belongs to a
	 * level > 0.
	 */
	int latitudeE6;
	/**
	 * The longitude of this vertex in micro degrees. It is set to -1 if this vertex belongs to
	 * a level > 0.
	 */
	int longitudeE6;

	/**
	 * Constructor for object pooling used by a PoolableFactory.
	 */
	HHVertex() {
		// do nothing is faster than clearing the object.
	}

	/**
	 * @return The level this vertex belongs to.
	 */
	int getLevel() {
		return vertexIds.length - 2;
	}

	@Override
	public boolean isReleased() {
		return isReleased;
	}

	@Override
	public void setReleased(boolean b) {
		this.isReleased = b;
	}
}
