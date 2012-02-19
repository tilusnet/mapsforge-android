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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHGraphProperties.HHLevelStats;
import org.mapsforge.routing.server.hh.DistanceTable;

/**
 * This class provides write access to the highway hierarchies database.
 */
public class HHDbWriter {

	private static final int BATCH_SIZE = 10000;

	private final static String SQL_INSERT_VERTEX = "INSERT INTO hh_vertex (id, longitude, latitude) VALUES (?,?,?);";
	private static final String SQL_INSERT_VERTEX_LVL = "INSERT INTO hh_vertex_lvl (id, lvl, neighborhood) VALUES (?,?,?);";
	private static final String SQL_INSERT_EDGE = "INSERT INTO hh_edge (id, source_id, target_id, weight, min_lvl, max_lvl, fwd, bwd, shortcut) VALUES (?,?,?,?,?,?,?,?,?);";
	private static final String SQL_INSERT_DISTANCE_TABLE_ROW = "INSERT INTO hh_distance_table_row (row_idx, vertex_id, distances) VALUES (?, ?, ? :: integer[]);";

	private final Connection conn;
	private final PreparedStatement pstmtInsertVertex, pstmtInsertVertexLvl, pstmtInsertEdge,
			pstmtInsertDtRow;
	private int insertEdgeCount, insertVertexCount, insertVertexLvlCount;

	/**
	 * @param conn
	 *            connects the database to write to, must conform to the highway hierarchies db
	 *            schema.
	 * @throws SQLException
	 *             on error accessing database.
	 */
	public HHDbWriter(Connection conn) throws SQLException {
		this.conn = conn;
		conn.setAutoCommit(false);
		pstmtInsertVertex = conn.prepareStatement(SQL_INSERT_VERTEX);
		pstmtInsertVertexLvl = conn.prepareStatement(SQL_INSERT_VERTEX_LVL);
		pstmtInsertEdge = conn.prepareStatement(SQL_INSERT_EDGE);
		pstmtInsertDtRow = conn.prepareStatement(SQL_INSERT_DISTANCE_TABLE_ROW);
		insertEdgeCount = insertVertexCount = insertVertexLvlCount = 0;
	}

	/**
	 * writes all unwritten updates.
	 * 
	 * @throws SQLException
	 *             on error executing update.
	 */
	public void flush() throws SQLException {
		System.out.println("flush batches : ");
		System.out.println("execute batch inserts (hh_vertex) : "
				+ (insertVertexCount - (insertVertexCount % BATCH_SIZE)) + " - "
				+ insertVertexCount);
		pstmtInsertVertex.executeBatch();
		System.out.println("execute batch inserts (hh_vertex_lvl) : "
				+ (insertVertexLvlCount - (insertVertexLvlCount % BATCH_SIZE)) + " - "
				+ insertVertexLvlCount);
		pstmtInsertVertexLvl.executeBatch();
		System.out.println("execute batch inserts (hh_edge) : "
				+ (insertEdgeCount - (insertEdgeCount % BATCH_SIZE)) + " - " + insertEdgeCount);
		pstmtInsertEdge.executeBatch();
		System.out.println("committing...");
		conn.commit();
	}

	/**
	 * Truncates all tables of the highway hierarchies db schema.
	 * 
	 * @throws SQLException
	 *             on error executing update.
	 */
	public void clearTables() throws SQLException {
		String sql = "TRUNCATE TABLE hh_vertex CASCADE;"
				+ "TRUNCATE TABLE hh_vertex_lvl CASCADE;" + "TRUNCATE TABLE hh_edge CASCADE;"
				+ "TRUNCATE TABLE hh_graph_properties CASCADE;"
				+ "TRUNCATE TABLE hh_distance_table_row CASCADE;"
				+ "TRUNCATE TABLE hh_lvl_stats CASCADE;";
		conn.createStatement().executeUpdate(sql);
	}

	/**
	 * Writes a highway hierarchies edge to the database.
	 * 
	 * @param id
	 *            edge identifier
	 * @param sourceId
	 *            identifier of source vertex.
	 * @param targetId
	 *            identifier of target vertex.
	 * @param weight
	 *            of this edge
	 * @param minLvl
	 *            minimum level of this edge
	 * @param maxLvl
	 *            maximum level of this edge.
	 * @param isForward
	 *            true means the edge is in the forward graph.
	 * @param isBackward
	 *            true means the edge is in the backward graph.
	 * @param isShortcut
	 *            true means edge is a shortcut, not in the original routing graph!
	 * @throws SQLException
	 *             on execution update errors.
	 */
	public void writeEdge(int id, int sourceId, int targetId, int weight, int minLvl,
			int maxLvl, boolean isForward, boolean isBackward, boolean isShortcut)
			throws SQLException {
		pstmtInsertEdge.setInt(1, id);
		pstmtInsertEdge.setInt(2, sourceId);
		pstmtInsertEdge.setInt(3, targetId);
		pstmtInsertEdge.setInt(4, weight);
		pstmtInsertEdge.setInt(5, minLvl);
		pstmtInsertEdge.setInt(6, maxLvl);
		pstmtInsertEdge.setBoolean(7, isForward);
		pstmtInsertEdge.setBoolean(8, isBackward);
		pstmtInsertEdge.setBoolean(9, isShortcut);
		pstmtInsertEdge.addBatch();
		if ((++insertEdgeCount) % BATCH_SIZE == 0) {
			pstmtInsertEdge.executeBatch();
			conn.commit();
			System.out.println("execute batch inserts (hh_edge) : "
					+ (insertEdgeCount - BATCH_SIZE) + " - " + insertEdgeCount);
		}
	}

	/**
	 * Write vertex level information.
	 * 
	 * @param id
	 *            identifier of the vertex.
	 * @param lvl
	 *            level
	 * @param neighborhood
	 *            neighborhood of the vertex within the specified level.
	 * @throws SQLException
	 *             on executing the update.
	 */
	public void writeVertexLevel(int id, int lvl, int neighborhood) throws SQLException {
		pstmtInsertVertexLvl.setInt(1, id);
		pstmtInsertVertexLvl.setInt(2, lvl);
		pstmtInsertVertexLvl.setInt(3, neighborhood);
		pstmtInsertVertexLvl.addBatch();
		if ((++insertVertexLvlCount) % BATCH_SIZE == 0) {
			pstmtInsertVertexLvl.executeBatch();
			conn.commit();
			System.out.println("execute batch inserts (hh_vertex_lvl) : "
					+ (insertVertexLvlCount - BATCH_SIZE) + " - " + insertVertexLvlCount);
		}
	}

	/**
	 * Writes a vertex.
	 * 
	 * @param id
	 *            vertex identifier.
	 * @param longitude
	 *            in degree
	 * @param latitude
	 *            in degree
	 * @throws SQLException
	 *             on error executing the update.
	 */
	public void writeVertex(int id, double longitude, double latitude) throws SQLException {
		pstmtInsertVertex.setInt(1, id);
		pstmtInsertVertex.setDouble(2, longitude);
		pstmtInsertVertex.setDouble(3, latitude);
		pstmtInsertVertex.addBatch();
		if ((++insertVertexCount) % BATCH_SIZE == 0) {
			pstmtInsertVertex.executeBatch();
			conn.commit();
			System.out.println("execute batch inserts (hh_vertex) : "
					+ (insertVertexCount - BATCH_SIZE) + " - " + insertVertexCount);
		}
	}

	/**
	 * Write the graph properties.
	 * 
	 * @param props
	 *            values to write.
	 * @throws SQLException
	 *             ...
	 */
	public void writeGraphProperties(HHGraphProperties props) throws SQLException {
		String sql = "INSERT INTO hh_graph_properties (creation_date, transport, c, h, hoplimit, vertex_threshold, downgraded_edges, num_threads, comp_time_mins) "
				+ "VALUES( "
				+ "'"
				+ new Timestamp(props.creationDate.getTime()).toString()
				+ "', "
				+ "'"
				+ props.transport
				+ "', "
				+ props.c
				+ ", "
				+ props.h
				+ ", "
				+ props.hopLimit
				+ ", "
				+ props.vertexThreshold
				+ ", "
				+ props.downgradedEdges
				+ ", " + props.numThreads + ", " + props.compTimeMinutes + ");";
		conn.createStatement().executeUpdate(sql);

		for (HHLevelStats ls : props.levelStats) {
			sql = "INSERT INTO hh_lvl_stats (lvl, num_vertices, num_edges, num_core_vertices, num_core_edges) "
					+ "VALUES("
					+ ls.lvl
					+ ", "
					+ ls.numVertices
					+ ", "
					+ ls.numEdges
					+ ", "
					+ ls.numCoreVertices + ", " + ls.numCoreEdges + ");";
			conn.createStatement().executeUpdate(sql);
		}
	}

	/**
	 * Writes the distance table.
	 * 
	 * @param distanceTable
	 *            the object to be written.
	 * @throws SQLException
	 *             argh
	 */
	public void writeDistanceTable(DistanceTable distanceTable) throws SQLException {
		int msg_int = 1000;
		int[] vertexIds = distanceTable.getVertexIds();
		for (int i = 0; i < vertexIds.length; i++) {
			int rowIdx = distanceTable.getRowColIndex(vertexIds[i]);
			pstmtInsertDtRow.setInt(1, rowIdx);
			pstmtInsertDtRow.setInt(2, vertexIds[i]);
			pstmtInsertDtRow.setString(3,
					intArrayToSqlString(distanceTable.getDistances()[rowIdx]));
			pstmtInsertDtRow.executeUpdate();
			if (i % msg_int == 0 && i > 0) {
				System.out.println("executed inserts (hh_distance_table_row) : "
						+ (i - msg_int) + " - " + i);
			}
		}
		System.out.println("executed inserts (hh_distance_table_row) : "
				+ (distanceTable.size() - (distanceTable.size() % msg_int)) + " - "
				+ distanceTable.size());
	}

	/**
	 * Converts an in array to a sql string.
	 * 
	 * @param array
	 *            hate the warnings
	 * 
	 * @return the sql string gepresenting the int array.
	 */
	private static String intArrayToSqlString(int[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (i < array.length - 1) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
