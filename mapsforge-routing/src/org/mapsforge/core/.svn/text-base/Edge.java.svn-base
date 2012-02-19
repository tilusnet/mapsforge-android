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

/**
 * This interface represents a directed edge within a routing graph.
 */
public interface Edge {

	/**
	 * @return Returns the unique identifier of this edge.
	 */
	public int getId();

	/**
	 * @return Returns the source vertex of this edge.
	 */
	public Vertex getSource();

	/**
	 * @return Returns the target vertex of this edge.
	 */
	public Vertex getTarget();

	/**
	 * @return Returns the coordinates of this edge sorted from source to target excluding the
	 *         coordinates of source and target vertex.
	 */
	public GeoCoordinate[] getWaypoints();

	/**
	 * @return Returns the coordinates of this edge sorted from source to target including the
	 *         coordinates of source and target vertex.
	 */
	public GeoCoordinate[] getAllWaypoints();

	/**
	 * @return Returns the name of the street.
	 */
	public String getName();

	/**
	 * @return Returns the type of the street. For example motorway, trunk, residential...
	 */
	public String getType();

	/**
	 * @return Returns true if the street is part of a roundabout.
	 */
	public boolean isRoundabout();

	/**
	 * @return Returns the Ref of the street, this can be names of the streets which are higher
	 *         within the naming hierarchy, e.g. names of motorways.
	 */
	public String getRef();

	/**
	 * @return Returns the Destination of the street, usually only available on motorways
	 */
	public String getDestination();

	/**
	 * @return The weight of this edge representing the costs to travel along this edge.
	 */
	public int getWeight();

}
