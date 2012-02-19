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

import java.util.Iterator;

/**
 * Package private graph to allow reusing the clustering code on different graph
 * implementations.
 */
interface Graph {

	/**
	 * @return Iterator making available all vertices of this graph.
	 */
	public Iterator<? extends Vertex> getVertices();

	/**
	 * @return Returns the number of vertices in this graph.
	 */
	public int numVertices();

	/**
	 * @return Returns the number of edges in this graph.
	 */
	public int numEdges();

	/**
	 * Looks up the vertex of given id.
	 * 
	 * @param id
	 *            identifier of the vertex.
	 * @return the desired vertex.
	 */
	public Vertex getVertex(int id);

}
