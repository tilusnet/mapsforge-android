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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.hh.dao.RgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.DBConnection;
import org.mapsforge.routing.preprocessing.hh.model.RgEdge;

final class EdgeIndex {

	private final int[] lons, lats, edgeIds;
	private final int[] offsets;
	private final GeoCoordinateKDTree waypointIndex;

	private EdgeIndex(int[] lons, int[] lats, int[] edgeIds, int[] offsets) {
		this.lons = lons;
		this.lats = lats;
		this.offsets = offsets;
		this.edgeIds = edgeIds;
		this.waypointIndex = new GeoCoordinateKDTree(lons, lats);
	}

	public void serialize(OutputStream oStream) throws IOException {
		ObjectOutputStream objOut = new ObjectOutputStream(oStream);
		objOut.writeObject(lons);
		objOut.writeObject(lats);
		objOut.writeObject(edgeIds);
		objOut.writeObject(offsets);
		objOut.close();
	}

	public static EdgeIndex deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		ObjectInputStream objIn = new ObjectInputStream(iStream);
		int[] lon = (int[]) objIn.readObject();
		int[] lat = (int[]) objIn.readObject();
		int[] edgeIds = (int[]) objIn.readObject();
		int[] offsets = (int[]) objIn.readObject();
		objIn.close();
		return new EdgeIndex(lon, lat, edgeIds, offsets);
	}

	public static EdgeIndex importFromDb(Connection conn) throws SQLException {
		RgDAO rg = new RgDAO(conn);

		int[] lon = new int[rg.getNumWaypoints()];
		int[] lat = new int[rg.getNumWaypoints()];
		int[] edgeIds = new int[rg.getNumWaypoints()];
		int[] offsets = new int[rg.getNumEdges() + 1];
		offsets[offsets.length - 1] = lon.length;

		int offset = 0;
		for (Iterator<RgEdge> iter = rg.getEdges().iterator(); iter.hasNext();) {
			RgEdge e = iter.next();
			offsets[e.getId()] = offset;

			double[] lon_ = e.getLongitudes();
			double[] lat_ = e.getLatitudes();
			for (int i = 1; i < lon_.length - 1; i++) {
				lon[offset] = GeoCoordinate.doubleToInt(lon_[i]);
				lat[offset] = GeoCoordinate.doubleToInt(lat_[i]);
				edgeIds[offset] = e.getId();
				offset++;
			}
		}
		return new EdgeIndex(lon, lat, edgeIds, offsets);
	}

	public GeoCoordinate[] getWaypoints(int edgeId) {
		int startIx = offsets[edgeId];
		int endIdx = offsets[edgeId + 1];

		GeoCoordinate[] coords = new GeoCoordinate[endIdx - startIx];
		int j = 0;
		for (int i = startIx; i < endIdx; i++) {
			coords[j++] = new GeoCoordinate(lats[i], lons[i]);
		}
		return coords;
	}

	public int getMaxLongitude() {
		return waypointIndex.getMaxLongitude();
	}

	public int getMinLongitude() {
		return waypointIndex.getMinLongitude();
	}

	public int getMaxLatitude() {
		return waypointIndex.getMaxLatitude();
	}

	public int getMinLatitude() {
		return waypointIndex.getMinLatitude();
	}

	public int getNearestEdge(int lon, int lat) {
		int idx = waypointIndex.getNearestNeighborIdx(lon, lat);
		return edgeIds[idx];
	}

	public int numEdges() {
		return offsets.length - 1;
	}

	public int numCoordinates() {
		return lons.length;
	}

	public static void main(String[] args) throws SQLException, FileNotFoundException,
			IOException, ClassNotFoundException {
		Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432, "osm_base",
				"osm", "osm");
		EdgeIndex edgeWaypoints = importFromDb(conn);
		edgeWaypoints.serialize(new FileOutputStream("test"));
		edgeWaypoints = EdgeIndex.deserialize(new FileInputStream("test"));
		for (int i = 0; i < edgeWaypoints.numEdges(); i++) {
			for (GeoCoordinate c : edgeWaypoints.getWaypoints(i)) {
				System.out.print(c);
			}
			System.out.print("\n");
		}

		System.out.println(edgeWaypoints.getNearestEdge(GeoCoordinate.doubleToInt(13.3039767),
				GeoCoordinate.doubleToInt(52.6008858)));
	}
}
