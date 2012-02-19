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
package org.mapsforge.routing.preprocessing.hh.dao;

import org.mapsforge.routing.preprocessing.hh.model.IRgEdge;
import org.mapsforge.routing.preprocessing.hh.model.IRgVertex;

/**
 * @param <V>
 *            data type of vertices.
 * @param <E>
 *            data type of edges. This interface can be used as input for routing graph
 *            preprocessing. For a graph consisting of n vertices, ids should range from 0 to
 *            n-1.
 */
public interface IRgDAO<V extends IRgVertex, E extends IRgEdge> {

	/**
	 * @return the number of vertices in the routing graph.
	 */
	public int getNumVertices();

	/**
	 * @return the number of edges in the routing graph.
	 */
	public int getNumEdges();

	/**
	 * @return iterates over all vertices in the graph.
	 */
	public Iterable<V> getVertices();

	/**
	 * @return iterates over all edges in this graph.
	 */
	public Iterable<E> getEdges();

}
