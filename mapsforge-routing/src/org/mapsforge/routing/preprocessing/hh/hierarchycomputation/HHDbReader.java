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
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation;

import gnu.trove.map.hash.TIntIntHashMap;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHGraphProperties.HHLevelStats;
import org.mapsforge.routing.server.hh.DistanceTable;

/**
 * This class allows for object oriented reading of the hihway hierarchies database.
 */
public class HHDbReader {

	private static final int FETCH_SIZE = 1000;
	private static final int MESSAGE_INTERVAL = 100000;

	private static final String SQL_COUNT_VERTICES = "SELECT COUNT(*) AS count FROM hh_vertex_lvl WHERE lvl = 0;";
	private static final String SQL_COUNT_LVL_VERTICES = "SELECT COUNT(*) AS count FROM hh_vertex_lvl;";
	private static final String SQL_COUNT_EDGES = "SELECT COUNT(*) AS count FROM hh_edge;";
	private static final String SQL_COUNT_EDGES_LVL = "SELECT COUNT(*) AS count FROM hh_edge WHERE min_lvl <= ? AND max_lvl >= ?;";
	private static final String SQL_COUNT_LEVELS = "SELECT MAX(lvl) + 1 AS count FROM hh_vertex_lvl;";
	private static final String SQL_SELECT_VERTICES = "SELECT id, longitude AS lon, latitude AS lat FROM hh_vertex ORDER BY id;";
	private static final String SQL_SELECT_VERTEX_LVLS = "SELECT * FROM hh_vertex_lvl ORDER BY id, lvl;";
	private static final String SQL_SELECT_EDGES = "SELECT * FROM hh_edge ORDER BY source_id, max_lvl, min_lvl, weight;";
	// private static final String SQL_SELECT_LVL_EDGES =
	// "SELECT v.lvl, e.* FROM hh_vertex_lvl v JOIN hh_edge e ON v.id = e.source_id AND v.lvl >= e.min_lvl AND v.lvl <= e.max_lvl LEFT OUTER JOIN rg_edge rge ON e.id / 2 = rge.id ORDER BY e.source_id, v.lvl;";
	private static final String SQL_SELECT_LVL_EDGES = "SELECT v.lvl, e.*, rge.name, rge.latitudes, rge.longitudes, rge.ref, rge.roundabout, l.name AS hwy_lvl FROM hh_vertex_lvl v JOIN hh_edge e ON v.id = e.source_id AND v.lvl >= e.min_lvl AND v.lvl <= e.max_lvl LEFT OUTER JOIN rg_edge rge ON e.id / 2 = rge.id LEFT OUTER JOIN rg_hwy_lvl l ON rge.hwy_lvl = l.id ORDER BY e.source_id, v.lvl;";
	private static final String SQL_SELECT_EDGES_LVL = "SELECT * FROM hh_edge  WHERE min_lvl <= ? AND max_lvl >= ? ORDER BY source_id, weight;";
	private static final String SQL_SELECT_LEVEL_STATS = "SELECT * FROM hh_lvl_stats ORDER BY lvl;";
	private static final String SQL_SELECT_GRAPH_PROPERTIES = "SELECT * FROM hh_graph_properties;";

	private final Connection conn;
	private final int numVertices, numLevelVertices, numEdges, numLevels;

	/**
	 * This reader can only read from databases conform to the highway hierarchies schema AND
	 * the osm2rg schema.
	 * 
	 * @param conn
	 *            the database to read from.
	 * @throws SQLException
	 *             on error querying database.
	 */
	public HHDbReader(Connection conn) throws SQLException {
		this.conn = conn;
		this.conn.setAutoCommit(false);
		ResultSet rs;

		rs = conn.createStatement().executeQuery(SQL_COUNT_VERTICES);
		rs.next();
		numVertices = rs.getInt("count");

		rs = conn.createStatement().executeQuery(SQL_COUNT_LVL_VERTICES);
		rs.next();
		numLevelVertices = rs.getInt("count");

		rs = conn.createStatement().executeQuery(SQL_COUNT_EDGES);
		rs.next();
		numEdges = rs.getInt("count");

		rs = conn.createStatement().executeQuery(SQL_COUNT_LEVELS);
		rs.next();
		numLevels = rs.getInt("count");
	}

	/**
	 * @return the number of vertices in the hierarchy.
	 */
	public int numVertices() {
		return numVertices;
	}

	/**
	 * @return the number of level vertices in the hierarchy.
	 */
	public int numLevelVertices() {
		return numLevelVertices;
	}

	/**
	 * @return the number of edges in the hierarchy.
	 */
	public int numEdges() {
		return numEdges;
	}

	/**
	 * @param lvl
	 *            specifies a level of the hierarchy.
	 * @return the number of edges in the given level of the hierarchy.
	 * @throws SQLException
	 *             on error querying database.
	 */
	public int numEdges(int lvl) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(SQL_COUNT_EDGES_LVL);
		pstmt.setInt(1, lvl);
		pstmt.setInt(2, lvl);
		ResultSet rs = pstmt.executeQuery();

		int count;
		if (rs.next()) {
			count = rs.getInt("count");
		} else {
			count = 0;
		}
		return count;

	}

	/**
	 * @return the number of levels the hierarchy has.
	 */
	public int numLevels() {
		return numLevels;
	}

	/**
	 * @return the distance table.
	 * @throws SQLException
	 *             on error querying the database.
	 */
	public DistanceTable getDistanceTable() throws SQLException {
		String selectRowCount = "SELECT count(*) AS c FROM hh_distance_table_row;";
		ResultSet rs = conn.createStatement().executeQuery(selectRowCount);
		if (rs.next()) {
			int rowCount = rs.getInt("c");
			int[][] distances = new int[rowCount][rowCount];
			TIntIntHashMap map = new TIntIntHashMap();

			String selectRows = "SELECT row_idx, vertex_id, distances FROM hh_distance_table_row;";
			PreparedStatement pstmt = getResultStreamingPreparedStatemet(conn,
					selectRows, FETCH_SIZE);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int rowIdx = rs.getInt("row_idx");
				int vertexId = rs.getInt("vertex_id");
				map.put(vertexId, rowIdx);

				Integer[] tmp = (Integer[]) rs.getArray("distances").getArray();
				for (int i = 0; i < tmp.length; i++) {
					distances[rowIdx][i] = tmp[i];
				}
			}
			return new DistanceTable(distances, map);
		}
		return null;

	}

	/**
	 * @return the graph properties.
	 * @throws SQLException
	 *             on error querying the database.
	 */
	public HHGraphProperties getGraphProperties() throws SQLException {
		HHLevelStats[] stats = new HHLevelStats[numLevels];
		ResultSet rs = conn.createStatement().executeQuery(SQL_SELECT_LEVEL_STATS);
		while (rs.next()) {
			int lvl = rs.getInt("lvl");
			stats[lvl] = new HHLevelStats(lvl, rs.getInt("num_edges"),
					rs.getInt("num_vertices"), rs.getInt("num_core_edges"),
					rs.getInt("num_core_vertices"));
		}
		rs.close();

		rs = conn.createStatement().executeQuery(SQL_SELECT_GRAPH_PROPERTIES);
		if (rs.next()) {
			HHGraphProperties props = new HHGraphProperties(new Date(rs.getTimestamp(
					"creation_date").getTime()), rs.getString("transport"), rs.getInt("h"),
					rs.getInt("vertex_threshold"), rs.getInt("hoplimit"),
					rs.getInt("num_threads"), rs.getDouble("c"),
					rs.getDouble("comp_time_mins"), rs.getBoolean("downgraded_edges"), stats);
			return props;
		}
		return null;

	}

	/**
	 * The iterator may return null values in case of sql errors.
	 * 
	 * @return get all vertices of the hierarchy.
	 */
	public Iterator<HHVertex> getVertices() {
		try {
			final PreparedStatement pstmt = getResultStreamingPreparedStatemet(
					conn, SQL_SELECT_VERTICES, FETCH_SIZE);
			final ResultSet rs = pstmt.executeQuery();

			return new Iterator<HHVertex>() {

				private int count = 0;

				@Override
				public boolean hasNext() {
					try {
						return !(rs.isLast() || rs.isAfterLast());
					} catch (SQLException e) {
						e.printStackTrace();
						while (e.getNextException() != null) {
							e = e.getNextException();
							e.printStackTrace();
						}
						return false;
					}
				}

				@Override
				public HHVertex next() {
					try {
						if (rs.next()) {
							if ((++count) % MESSAGE_INTERVAL == 0) {
								System.out.println("read HHVertex "
										+ (count - MESSAGE_INTERVAL) + " - " + count);
							}
							return new HHVertex(rs.getInt("id"), rs.getDouble("lon"),
									rs.getDouble("lat"));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public void remove() {
					// only read data.
				}
			};
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The iterator may return null values in case of sql errors.
	 * 
	 * @return all level vertices of this hierarchy.
	 */
	public Iterator<HHVertexLvl> getVertexLvls() {
		try {
			final PreparedStatement pstmt = getResultStreamingPreparedStatemet(
					conn, SQL_SELECT_VERTEX_LVLS, FETCH_SIZE);
			final ResultSet rs = pstmt.executeQuery();

			return new Iterator<HHVertexLvl>() {

				private int count = 0;

				@Override
				public boolean hasNext() {
					try {
						return !(rs.isLast() || rs.isAfterLast());
					} catch (SQLException e) {
						e.printStackTrace();
						while (e.getNextException() != null) {
							e = e.getNextException();
							e.printStackTrace();
						}
						return false;
					}
				}

				@Override
				public HHVertexLvl next() {
					try {
						if (rs.next()) {
							if ((++count) % MESSAGE_INTERVAL == 0) {
								System.out.println("read HHVertexLvl "
										+ (count - MESSAGE_INTERVAL) + " - " + count);
							}
							return new HHVertexLvl(rs.getInt("id"), rs.getInt("neighborhood"),
									rs.getInt("lvl"));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public void remove() {
					// only read
				}
			};
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The iterator may return null values in case of sql errors.
	 * 
	 * @return all edges of the hierarchy.
	 */
	public Iterator<HHEdge> getEdges() {
		try {
			PreparedStatement pstmt = getResultStreamingPreparedStatemet(conn,
					SQL_SELECT_EDGES, FETCH_SIZE);
			return getEdges(pstmt);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The iterator may return null values in case of sql errors.
	 * 
	 * @param lvl
	 *            the level the edges are in.
	 * @return all edges belonging to the given level of the graph.
	 */
	public Iterator<HHEdge> getEdges(int lvl) {
		try {
			PreparedStatement pstmt = getResultStreamingPreparedStatemet(conn,
					SQL_SELECT_EDGES_LVL, FETCH_SIZE);
			pstmt.setInt(1, lvl);
			pstmt.setInt(2, lvl);
			return getEdges(pstmt);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The iterator may return null values in case of sql errors.
	 * 
	 * @return all level edges of the graph.
	 */
	public Iterator<HHEdgeLvl> getEdgesLvl() {
		try {
			PreparedStatement pstmt = getResultStreamingPreparedStatemet(conn,
					SQL_SELECT_LVL_EDGES, FETCH_SIZE);
			return getEdgesLvl(pstmt);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Extracts a primitive double array out of an sql array.
	 * 
	 * @param a
	 *            the sql array must contain an array of type Double[].
	 * @return primitive double array, same order.
	 * @throws SQLException
	 *             on error accessing the array.
	 */
	static double[] toDoubleArray(Array a) throws SQLException {
		if (a == null) {
			return null;
		}
		Double[] tmp = (Double[]) a.getArray();
		double[] result = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			result[i] = tmp[i];
		}
		return result;
	}

	private Iterator<HHEdgeLvl> getEdgesLvl(final PreparedStatement stmt) {
		try {
			return new Iterator<HHEdgeLvl>() {
				private final ResultSet rs = stmt.executeQuery();

				private int count = 0;

				@Override
				public boolean hasNext() {
					try {
						return !(rs.isLast() || rs.isAfterLast());
					} catch (SQLException e) {
						e.printStackTrace();
						while (e.getNextException() != null) {
							e = e.getNextException();
							e.printStackTrace();
						}
						return false;
					}
				}

				@Override
				public HHEdgeLvl next() {
					try {
						if (rs.next()) {
							if ((++count) % MESSAGE_INTERVAL == 0) {
								System.out.println("read HHEdgeLvl "
										+ (count - MESSAGE_INTERVAL) + " - " + count);
							}
							boolean isReversed = rs.getInt("id") % 2 == 1;
							double[] latitudes = toDoubleArray(rs.getArray("latitudes"));
							double[] longitudes = toDoubleArray(rs.getArray("longitudes"));

							return new HHEdgeLvl(rs.getInt("id"), rs.getInt("source_id"),
									rs.getInt("target_id"), rs.getInt("weight"),
									rs.getInt("min_lvl"), rs.getInt("max_lvl"),
									rs.getBoolean("fwd"), rs.getBoolean("bwd"),
									rs.getBoolean("shortcut"), rs.getInt("lvl"),
									rs.getString("name"), rs.getString("ref"),
									latitudes, longitudes, isReversed, rs.getString("hwy_lvl"),
									rs.getBoolean("roundabout"));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public void remove() {
					// only read.
				}
			};
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Iterator<HHEdge> getEdges(final PreparedStatement stmt) {
		try {
			return new Iterator<HHEdge>() {
				private final ResultSet rs = stmt.executeQuery();
				private int count = 0;

				@Override
				public boolean hasNext() {
					try {
						return !(rs.isLast() || rs.isAfterLast());
					} catch (SQLException e) {
						e.printStackTrace();
						while (e.getNextException() != null) {
							e = e.getNextException();
							e.printStackTrace();
						}
						return false;
					}
				}

				@Override
				public HHEdge next() {
					try {
						if (rs.next()) {
							if ((++count) % MESSAGE_INTERVAL == 0) {
								System.out.println("read HHVertex "
										+ (count - MESSAGE_INTERVAL) + " - " + count);
							}
							return new HHEdge(rs.getInt("id"), rs.getInt("source_id"),
									rs.getInt("target_id"), rs.getInt("weight"),
									rs.getInt("min_lvl"), rs.getInt("max_lvl"),
									rs.getBoolean("fwd"), rs.getBoolean("bwd"),
									rs.getBoolean("shortcut"));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public void remove() {
					// read only.
				}
			};
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Immutable Highway hierarchies edge.
	 */
	public static class HHEdge {
		/**
		 * identifier of this edge
		 */
		public final int id;
		/**
		 * identifier of the source vertex of this edge.
		 */
		public final int sourceId;
		/**
		 * identifier of the target vertex of this edge.
		 */
		public final int targetId;
		/**
		 * weight of this edge.
		 */
		public final int weight;
		/**
		 * the minimum level of the graph, this edge belongs to.
		 */
		public final int minLvl;
		/**
		 * the maximum level of the graph this edge belongs to.
		 */
		public final int maxLvl;
		/**
		 * true if edge is in forward graph.
		 */
		public final boolean fwd;
		/**
		 * true if this edge is in backward graph.
		 */
		public final boolean bwd;
		/**
		 * true if this edge is a shortcut.
		 */
		public final boolean shortcut;

		HHEdge(int id, int sourceId, int targetId, int weight, int minLvl, int maxLvl,
				boolean fwd, boolean bwd, boolean shortcut) {
			this.id = id;
			this.sourceId = sourceId;
			this.targetId = targetId;
			this.weight = weight;
			this.minLvl = minLvl;
			this.maxLvl = maxLvl;
			this.fwd = fwd;
			this.bwd = bwd;
			this.shortcut = shortcut;
		}
	}

	/**
	 * This immutable class represents an edge of a given level.
	 */
	public static class HHEdgeLvl extends HHEdge {
		/**
		 * the level of this edge.
		 */
		public final int lvl;
		/**
		 * street name of this edge.
		 */
		public final String name;
		/**
		 * ref name of this edge
		 */
		public final String ref;
		/**
		 * the latitudes of the way points of this edge, excluding source an target coordinates.
		 */
		public final double[] latitudes;
		/**
		 * the longitudes of the way points of this edge, excluding source an target
		 * coordinates.
		 */
		public final double[] longitudes;
		/**
		 * false if this exists in the osm xml in the same direction.
		 */
		public final boolean isReversed;
		/**
		 * true if this edge is part of a roundabout.
		 */
		public final boolean isRoundabout;
		/**
		 * e.g. motorway, trunk, primary etc.
		 */
		public final String osmStreetType;

		HHEdgeLvl(int id, int sourceId, int targetId, int weight, int minLvl,
				int maxLvl, boolean fwd, boolean bwd, boolean shortcut, int lvl,
				String name, String ref, double[] latitudes, double[] longitudes,
				boolean isReversed,
				String osmStreetType, boolean isRoundabout) {
			super(id, sourceId, targetId, weight, minLvl, maxLvl, fwd, bwd, shortcut);
			this.lvl = lvl;
			this.name = name;
			this.ref = ref;
			this.latitudes = latitudes;
			this.longitudes = longitudes;
			this.isReversed = isReversed;
			this.osmStreetType = osmStreetType;
			this.isRoundabout = isRoundabout;
		}
	}

	/**
	 * This immutable class holds level specific information of vertices.
	 */
	public static class HHVertexLvl {
		/**
		 * the identifier of the respective vertex. multiple level vertices can have the same
		 * identifier!
		 */
		public final int id;
		/**
		 * the neighborhood value of this vertex level.
		 */
		public final int neighborhood;
		/**
		 * the level of this vertex level.
		 */
		public final int lvl;

		HHVertexLvl(int id, int neighborhood, int lvl) {
			this.id = id;
			this.neighborhood = neighborhood;
			this.lvl = lvl;
		}
	}

	/**
	 * This immutable class represents a vertex within the highway hierarchy.
	 */
	public static class HHVertex {
		/**
		 * the identifier of this vertex.
		 */
		public final int id;
		/**
		 * the longitude in degrees of this vertex.
		 */
		public final double longitude;
		/**
		 * the latitude in derees of this vertex.
		 */
		public final double latitude;

		HHVertex(int id, double longitude, double latitude) {
			this.id = id;
			this.longitude = longitude;
			this.latitude = latitude;
		}
	}

	private static PreparedStatement getResultStreamingPreparedStatemet(Connection conn,
			String sql, int fetchSize) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		pstmt.setFetchSize(fetchSize);
		return pstmt;
	}
}
