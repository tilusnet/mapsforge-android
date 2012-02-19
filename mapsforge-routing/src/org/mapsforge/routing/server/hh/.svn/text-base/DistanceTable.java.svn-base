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
package org.mapsforge.routing.server.hh;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;

/**
 * Maps vertex id|s to table rows / cols and hold distances in an array.
 * 
 * 
 */
public class DistanceTable implements Serializable {

	private static final long serialVersionUID = 7621456445875680368L;

	private final TIntIntHashMap map;
	private int[][] distances;

	/**
	 * @param vertexIds
	 *            of all vertices to add to distance table.
	 */
	public DistanceTable(List<Integer> vertexIds) {
		this.map = new TIntIntHashMap();
		int idx = 0;
		for (int id : vertexIds) {
			map.put(id, idx++);
		}
		distances = new int[map.size()][map.size()];
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[i].length; j++) {
				distances[i][j] = Integer.MAX_VALUE;
			}
		}
	}

	/**
	 * @param oStream
	 *            stream to write to.
	 * @throws IOException
	 *             guess what?
	 * 
	 */
	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	/**
	 * @param iStream
	 *            stream to read from
	 * @return the deserialized distance table
	 * @throws IOException
	 *             read error
	 * @throws ClassNotFoundException
	 *             cast error
	 */
	public static DistanceTable deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (DistanceTable) Serializer.deserialize(iStream);
	}

	/**
	 * write this table to file.
	 * 
	 * @param f
	 *            target.
	 * @throws IOException
	 *             write error.
	 */
	public void serialize(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
		fos.close();
	}

	/**
	 * Read a table from file.
	 * 
	 * @param f
	 *            source
	 * @return the table
	 * @throws IOException
	 *             read
	 * @throws ClassNotFoundException
	 *             cast
	 */
	public static DistanceTable getFromSerialization(File f) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		DistanceTable dt = (DistanceTable) ois.readObject();
		ois.close();
		fis.close();
		return dt;
	}

	/**
	 * get table form db.
	 * 
	 * @param conn
	 *            to read from.
	 * @return the table
	 * @throws SQLException
	 *             sql error
	 */
	public static DistanceTable getFromHHDb(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);
		return reader.getDistanceTable();
	}

	/**
	 * construct table from all pairs distances.
	 * 
	 * @param distances
	 *            all pairs distances
	 * @param mapping
	 *            maps vertex id to array index within the table.
	 */
	public DistanceTable(int[][] distances, TIntIntHashMap mapping) {
		this.distances = distances;
		this.map = mapping;
	}

	/**
	 * @return all ids of vertices within this table
	 */
	public int[] getVertexIds() {
		return map.keys();
	}

	/**
	 * @param vertexId
	 *            ..
	 * @return array index of the vertex in this table.
	 */
	public int getRowColIndex(int vertexId) {
		if (map.contains(vertexId)) {
			return map.get(vertexId);
		}
		return -1;

	}

	/**
	 * Set the distance between a pair of vertices.
	 * 
	 * @param vId1
	 *            source
	 * @param vId2
	 *            target
	 * @param distance
	 *            ..
	 */
	public void set(int vId1, int vId2, int distance) {
		distances[map.get(vId1)][map.get(vId2)] = distance;
	}

	/**
	 * get the distance
	 * 
	 * @param vId1
	 *            source
	 * @param vId2
	 *            target
	 * @return distance
	 */
	public int get(int vId1, int vId2) {
		if (!map.containsKey(vId1) || !map.containsKey(vId2)) {
			return Integer.MAX_VALUE;
		}
		return distances[map.get(vId1)][map.get(vId2)];
	}

	/**
	 * @return the table
	 */
	public int[][] getDistances() {
		return distances;
	}

	/**
	 * @return number of vertices in this table
	 */
	public int size() {
		return distances.length;
	}

	@Override
	public String toString() {
		return distances.length + "x" + distances.length;
	}
}
