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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.mapsforge.routing.preprocessing.hh.dao.RgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.DBConnection;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArray;

class EdgeMapper implements Serializable {

	private static final long serialVersionUID = 7749333198499552855L;

	private final int[] hhEdgeIdToRgEdgeId;
	private final int[][] rgEdgeIdToHhEdgeId;

	private final BitArray reversed;

	private EdgeMapper(int[] hhEdgeIdToRgEdgeId, int[][] rgEdgeIdToHhEdgeId, BitArray isReversed) {
		this.hhEdgeIdToRgEdgeId = hhEdgeIdToRgEdgeId;
		this.reversed = isReversed;
		this.rgEdgeIdToHhEdgeId = rgEdgeIdToHhEdgeId;
	}

	public EdgeMapping mapFromHHEdgeId(int hhEdgeId) {
		if (hhEdgeId < 0 || hhEdgeId >= hhEdgeIdToRgEdgeId.length
				|| hhEdgeIdToRgEdgeId[hhEdgeId] == -1) {
			return null;
		}
		return new EdgeMapping(hhEdgeId, hhEdgeIdToRgEdgeId[hhEdgeId], reversed.get(hhEdgeId));
	}

	public EdgeMapping[] mapFromRgEdgeId(int rgEdgeId) {
		if (rgEdgeId < 0 || rgEdgeId >= rgEdgeIdToHhEdgeId[0].length) {
			return null;
		}
		return new EdgeMapping[] {
				new EdgeMapping(rgEdgeIdToHhEdgeId[0][rgEdgeId], rgEdgeId,
						reversed.get(rgEdgeIdToHhEdgeId[0][rgEdgeId])),
				new EdgeMapping(rgEdgeIdToHhEdgeId[0][rgEdgeId], rgEdgeId,
						reversed.get(rgEdgeIdToHhEdgeId[1][rgEdgeId])), };
	}

	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	public static EdgeMapper deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (EdgeMapper) Serializer.deserialize(iStream);
	}

	public static EdgeMapper importFromDb(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);
		RgDAO rg = new RgDAO(conn);

		// mapping from hhEdgeId to rgEdgeId
		int[] hhEdgeIdToRgEdgeId = new int[reader.numEdges()];
		int[][] rgEdgeIdToHhEdgeId = new int[2][rg.getNumEdges()];
		for (int i = 0; i < rgEdgeIdToHhEdgeId[0].length; i++) {
			rgEdgeIdToHhEdgeId[0][i] = -1;
			rgEdgeIdToHhEdgeId[1][i] = -1;
		}

		BitArray reversed = new BitArray(reader.numEdges());
		int hhEdgeId = 0;
		for (Iterator<HHEdge> iter = reader.getEdges(); iter.hasNext();) {
			HHEdge e = iter.next();
			if (e.shortcut) {
				// edge is not in routing graph
				hhEdgeIdToRgEdgeId[hhEdgeId] = -1;
			} else {
				// edge is in routing graph, either forward or backward

				// hh -> rg
				int rgEdgeId = e.id / 2;
				hhEdgeIdToRgEdgeId[hhEdgeId] = e.id / 2;
				reversed.set(hhEdgeId, e.id % 2 == 1);

				// rg ->hh
				if (rgEdgeIdToHhEdgeId[0][rgEdgeId] == -1) {
					rgEdgeIdToHhEdgeId[0][rgEdgeId] = hhEdgeId;
				} else {
					rgEdgeIdToHhEdgeId[1][rgEdgeId] = hhEdgeId;
				}
			}
			hhEdgeId++;
		}

		return new EdgeMapper(hhEdgeIdToRgEdgeId, rgEdgeIdToHhEdgeId, reversed);
	}

	public class EdgeMapping {
		public final int hhEdgeId, rgEdgeId;
		public final boolean isReversed;

		public EdgeMapping(int hhEdgeId, int rgEdgeId, boolean isReversed) {
			this.hhEdgeId = hhEdgeId;
			this.rgEdgeId = rgEdgeId;
			this.isReversed = isReversed;
		}

		@Override
		public String toString() {
			String str = hhEdgeId + " -> " + rgEdgeId;
			if (isReversed) {
				str += " (reversed)";
			}
			return str;
		}

	}

	public static void main(String[] args) throws SQLException {
		Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432, "berlin",
				"osm", "osm");
		EdgeMapper mapper = importFromDb(conn);
		for (int i = 0; i < 50000; i++) {
			System.out.println(mapper.mapFromHHEdgeId(i));
		}
		for (int i = 0; i < 1000; i++) {
			for (EdgeMapping m : mapper.mapFromRgEdgeId(i)) {
				System.out.print(m + " | ");
			}
			System.out.println();
		}
	}

}
