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

import java.util.Collection;
import java.util.Iterator;

/**
 * This class functionality is composed of spatial indices allowing for nearest neighbor queries, a
 * routing algorithm for shortest path queries and a graph like interface alowing traversal and querying
 * satellite data of vertices and edges.
 */
public interface Router {

	/**
	 * Computes the Shortest path from source to target vertex.
	 * 
	 * @param sourceId
	 *            identifier of the source vertex.
	 * @param targetId
	 *            identifier of the target vertex.
	 * @return Returns all edges along the shortest path, sorted from source to target.
	 */
	public Edge[] getShortestPath(int sourceId, int targetId);

	/**
	 * Computes the Shortest path from source to target vertex.
	 * 
	 * @param sourceId
	 *            identifier of the source vertex.
	 * @param targetId
	 *            identifier of the target vertex.
	 * @param searchspaceBuff
	 *            all edges visited by the algorithm are put here.
	 * @return Returns all edges along the shortest path, sorted from source to target.
	 */
	public Edge[] getShortestPathDebug(int sourceId, int targetId,
			Collection<Edge> searchspaceBuff);

	/**
	 * Nearest neighbor query for vertices.
	 * 
	 * @param coord
	 *            The query parameter.
	 * @return Returns the vertex nearest to the given coordinate.
	 */
	public Vertex getNearestVertex(GeoCoordinate coord);

	/**
	 * Looks up the vertex of given id.
	 * 
	 * @param id
	 *            vertex identifier.
	 * @return Returns the vertex or null if the id is invalid.
	 */
	public Vertex getVertex(int id);

	/**
	 * Range query for vertices.
	 * 
	 * @param bbox
	 *            the bounding rectangle.
	 * @return all vertices within the specified range.
	 */
	public Iterator<? extends Vertex> getVerticesWithinBox(Rect bbox);

	/**
	 * Nearest Neighbor query for the nearest edge.
	 * 
	 * @param coord
	 *            The query parameter.
	 * @return The nearest edge with regard to the given coordinate. If multiple edges fulfill the
	 *         Nearest Neighbor criterion then only one is returned arbitrarily.
	 */
	public Edge getNearestEdge(GeoCoordinate coord);

	/**
	 * Nearest Neighbor query for Edges.
	 * 
	 * @param coord
	 *            The query parameter.
	 * @return The set of nearest Edges with regard to to the given coordinate.
	 */
	public Edge[] getNearestEdges(GeoCoordinate coord);

	/**
	 * @return Returns the name of the algorithm used for shortest path computations.
	 */
	public String getAlgorithmName();

	/**
	 * @return Returns a minimal bounding rectangle enclosing all vertices and edges.
	 */
	public Rect getBoundingBox();

}
