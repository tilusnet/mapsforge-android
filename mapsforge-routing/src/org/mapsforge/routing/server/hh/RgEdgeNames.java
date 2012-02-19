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

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.mapsforge.routing.preprocessing.hh.dao.RgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.DBConnection;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.model.RgEdge;
import org.mapsforge.routing.preprocessing.hh.model.TagHighway;

class RgEdgeNames implements Serializable {

	private static final int HIGHWAYLEVEL_BITMASK = 31;
	private static final int ROUNDABOUT_BIT = 64;

	private static final long serialVersionUID = 2122661604323386224L;

	private final String[] names;
	private final int[] namesIndex;
	private final String[] refs;
	private final int[] refsIndex;
	private final String[] destinations;
	private final int[] destinationIndex;
	private final byte[] flags;

	private RgEdgeNames(String[] names, int[] namesIndex, String[] refs, int[] refsIndex,
			byte[] flags, String[] destinations, int[] destinationIndex) {
		this.names = names;
		this.namesIndex = namesIndex;
		this.refs = refs;
		this.refsIndex = refsIndex;
		this.flags = flags;
		this.destinations = destinations;
		this.destinationIndex = destinationIndex;
	}

	public String getName(int rgEdgeId) {
		if (rgEdgeId < 0 || rgEdgeId >= namesIndex.length || namesIndex[rgEdgeId] == -1) {
			return "";
		}
		return names[namesIndex[rgEdgeId]];
	}

	public String getRef(int rgEdgeId) {
		if (rgEdgeId < 0 || rgEdgeId >= refsIndex.length || refsIndex[rgEdgeId] == -1) {
			return "";
		}
		return refs[refsIndex[rgEdgeId]];
	}

	public String getDestination(int rgEdgeId) {
		if (rgEdgeId < 0 || rgEdgeId >= destinationIndex.length
				|| destinationIndex[rgEdgeId] == -1) {
			return "";
		}
		return destinations[destinationIndex[rgEdgeId]];
	}

	public String getHighwayLevel(int rgEdgeId) {
		return TagHighway.vk[flags[rgEdgeId] & HIGHWAYLEVEL_BITMASK];
	}

	public boolean isRoundabout(int rgEdgeId) {
		return (flags[rgEdgeId] & ROUNDABOUT_BIT) == ROUNDABOUT_BIT;
	}

	public int size() {
		return namesIndex.length;
	}

	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	public static RgEdgeNames deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (RgEdgeNames) Serializer.deserialize(iStream);
	}

	public static RgEdgeNames importFromDb(Connection conn) throws SQLException {
		RgDAO rg = new RgDAO(conn);

		int[] namesIndex = new int[rg.getNumEdges()];
		int[] refsIndex = new int[rg.getNumEdges()];
		int[] destinationIndex = new int[rg.getNumEdges()];
		byte[] flags = new byte[rg.getNumEdges()];

		int counter = 0;
		// put all names on a map
		TObjectIntHashMap<String> namesMap = new TObjectIntHashMap<String>();
		TObjectIntHashMap<String> refsMap = new TObjectIntHashMap<String>();
		TObjectIntHashMap<String> destinationMap = new TObjectIntHashMap<String>();
		for (Iterator<RgEdge> iter = rg.getEdges().iterator(); iter.hasNext();) {
			RgEdge e = iter.next();
			String name = e.getName();
			String ref = e.getRef();
			String destination = e.getDestination();
			if (name != null && !name.isEmpty()) {
				if (!namesMap.containsKey(name)) {
					namesMap.put(name, counter++);
				}
				int offset = namesMap.get(name);
				namesIndex[e.getId()] = offset;
			} else {
				namesIndex[e.getId()] = -1;
			}
			if (ref != null && !ref.isEmpty()) {
				if (!refsMap.containsKey(ref)) {
					refsMap.put(ref, counter++);
				}
				int offset = refsMap.get(ref);
				refsIndex[e.getId()] = offset;
			} else {
				refsIndex[e.getId()] = -1;
			}
			if (destination != null && !destination.isEmpty()) {
				if (!destinationMap.containsKey(destination)) {
					destinationMap.put(destination, counter++);
				}
				int offset = destinationMap.get(destination);
				destinationIndex[e.getId()] = offset;
			} else {
				destinationIndex[e.getId()] = -1;
			}
			// Set additional flags
			byte flagByte = 0;
			flagByte = TagHighway.kv.get(e.getHighwayLevel());
			if (e.isRoundabout()) {
				flagByte = (byte) (flagByte + ROUNDABOUT_BIT);
			}
			flags[e.getId()] = flagByte;
		}

		String[] names = new String[counter];
		String[] refs = new String[counter];
		String[] destinations = new String[counter];
		for (Object s : namesMap.keys()) {
			String s1 = (String) s;
			names[namesMap.get(s)] = s1;
		}
		for (Object s : refsMap.keys()) {
			String s1 = (String) s;
			refs[refsMap.get(s)] = s1;
		}
		for (Object s : destinationMap.keys()) {
			String s1 = (String) s;
			destinations[destinationMap.get(s)] = s1;
		}

		return new RgEdgeNames(names, namesIndex, refs, refsIndex, flags, destinations,
				destinationIndex);
	}

	public static void main(String[] args) throws SQLException {
		Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432, "osm_base",
				"osm", "osm");
		RgEdgeNames edgeNames = importFromDb(conn);
		for (int i = 0; i < edgeNames.size(); i++) {
			if (!edgeNames.getDestination(i).isEmpty())
				System.out.println(edgeNames.getName(i) + " " + edgeNames.getRef(i) + " "
						+ edgeNames.getHighwayLevel(i) + " " + edgeNames.getDestination(i));
		}
	}
}
