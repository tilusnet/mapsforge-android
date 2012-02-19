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
package org.mapsforge.routing.preprocessing.hh.mobile;

import java.io.Serializable;

/**
 * This interface represents a cluster of the graph which is a subset of its vertices. It holds
 * an ordered set of vertices. The order can be changed by the swap method. Additionally it can
 * be checked in O(1) average time if a given vertex belons to this cluster.
 */
public interface Cluster extends Serializable {

	/**
	 * Check if the given vertex belongs to this cluster in O(1) average time.
	 * 
	 * @param vertexId
	 *            the vertex identifier.
	 * @return true if and only if the given vertex belongs to this cluster.
	 */
	public boolean containsVertex(int vertexId);

	/**
	 * @return Returns an ordered set of all vertices belonging to this cluster.
	 */
	public int[] getVertices();

	/**
	 * swap the order of the i-th an j-th vertex.
	 * 
	 * @param i
	 *            the index of the first vertex to be swapped.
	 * @param j
	 *            the index of the second vertex to be swapped.
	 */
	public void swapVertices(int i, int j);

	/**
	 * @return Return the number of vertices belonging to this cluster.
	 */
	public int size();

}
