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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level.LevelEdge;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level.LevelVertex;

/**
 * This class is responsible for writing a binary file for the mobile highway hierarchies
 * algorithm.
 */
public class HHBinaryFileWriter {
	/** parameter for specifying the clustering algorithm */
	public static final String CLUSTERING_ALGORTHM_K_CENTER = "k_center";
	/** parameter for specifying the clustering algorithm */
	public static final String CLUSTERING_ALGORITHM_QUAD_TREE = "quad_tree";

	private final static byte[] HEADER_MAGIC = HHGlobals.BINARY_FILE_HEADER_MAGIC;
	private final static int HEADER_LENGTH = HHGlobals.BINARY_FILE_HEADER_LENGTH;

	private final static int BUFFER_SIZE = 16384 * 1000;

	/**
	 * Writes the binary file for the mobile highway hierarchies algorithm. input is take from a
	 * database.
	 * 
	 * @param conn
	 *            input database.
	 * @param clusteringAlgorithmName
	 *            name of the clustering algorithm, see static class variables.
	 * @param clusterSizeThreshold
	 *            limit on the number of nodes per logical block of the graph.
	 * @param kcenterOversamplingFactor
	 *            controls the quality of the k-center clusters.
	 * @param targetFile
	 *            file to write output to.
	 * @param indexGroupSizeThreshold
	 *            controls compression and runtime overhead of the address lookup table.
	 * @param rtreeBlockSize
	 *            sets size and alignment of r tree nodes.
	 * @param includeHopIndices
	 *            set to true for storing pre-computed information for shortcut expansion.
	 * @throws IOException
	 *             on error reading or writing file.
	 * @throws SQLException
	 *             on error with database.
	 */
	public static void writeBinaryFile(Connection conn, String clusteringAlgorithmName,
			int clusterSizeThreshold, int kcenterOversamplingFactor,
			File targetFile, int indexGroupSizeThreshold, int rtreeBlockSize,
			boolean includeHopIndices) throws IOException, SQLException {

		// load the graph into ram
		LevelGraph levelGraph = new LevelGraph(conn);
		conn.close();

		// compute the clustering
		// compute clustering
		System.out.println("compute clustering: ");
		System.out.println("algorithm = " + clusteringAlgorithmName);
		System.out.println("clusterSizeThreshold = " + clusterSizeThreshold);
		Clustering[] clustering;
		if (clusteringAlgorithmName.equals(CLUSTERING_ALGORITHM_QUAD_TREE)) {
			clustering = QuadTreeClusteringAlgorithm.computeClustering(levelGraph
					.getLevels(),
					levelGraph.getVertexLongitudesE6(), levelGraph.getVertexLatitudesE6(),
					QuadTreeClusteringAlgorithm.HEURISTIC_MEDIAN,
					clusterSizeThreshold);

		} else if (clusteringAlgorithmName.equals(CLUSTERING_ALGORTHM_K_CENTER)) {
			clustering = KCenterClusteringAlgorithm.computeClustering(levelGraph
					.getLevels(), clusterSizeThreshold, kcenterOversamplingFactor,
					KCenterClusteringAlgorithm.HEURISTIC_MIN_SIZE);
		} else {
			System.out.println("invalid clustering algorithm specified in properties.");
			return;
		}

		// ---------------- WRITE TEMPORARY FILES --------------------------
		System.out.println("targetFile = '" + targetFile.getAbsolutePath() + "'");

		File fBlocks = new File(targetFile.getAbsolutePath() + ".blocks");
		File fileAddressLookupTable = new File(targetFile.getAbsolutePath()
				+ ".addressLookupTable");
		File fRTree = new File(targetFile.getAbsolutePath() + ".rtree");

		// write the graphs cluster blocks
		ClusterBlockMapping mapping = new ClusterBlockMapping(clustering);
		int[] blockSize = BlockWriter.writeClusterBlocks(fBlocks, levelGraph, clustering,
				mapping, includeHopIndices);

		// write block index
		AddressLookupTableWriter.writeTable(blockSize, indexGroupSizeThreshold,
				fileAddressLookupTable);

		// construct and write r-tree (insert only level 0 clusters)
		int[] minLat = new int[clustering[0].size()];
		int[] maxLat = new int[clustering[0].size()];
		int[] minLon = new int[clustering[0].size()];
		int[] maxLon = new int[clustering[0].size()];
		int[] blockId = new int[clustering[0].size()];
		{
			int i = 0;
			for (Cluster c : clustering[0].getClusters()) {
				Rect r = getBoundingBox(c, levelGraph.getLevel(0));
				minLat[i] = r.minLatitudeE6;
				maxLat[i] = r.maxLatitudeE6;
				minLon[i] = r.minLongitudeE6;
				maxLon[i] = r.maxLongitudeE6;
				blockId[i] = mapping.getBlockId(c);
				i++;
			}
		}
		StaticRTreeWriter.packSortTileRecursive(minLon, maxLon, minLat, maxLat, blockId,
				rtreeBlockSize, fRTree);

		// ---------------- WRITE THE BINARY FILE --------------------------

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(targetFile)));

		// write header of the binary
		long startAddrGraph = HEADER_LENGTH;
		long endAddrGraph = startAddrGraph + fBlocks.length();

		long startAddrBlockIdx = endAddrGraph;
		long endAddrBlockIdx = startAddrBlockIdx + fileAddressLookupTable.length();

		long startAddrRTree = endAddrBlockIdx;
		long endAddrRTree = startAddrRTree + fRTree.length();

		out.write(HEADER_MAGIC);
		out.writeLong(startAddrGraph);
		out.writeLong(endAddrGraph);
		out.writeLong(startAddrBlockIdx);
		out.writeLong(endAddrBlockIdx);
		out.writeLong(startAddrRTree);
		out.writeLong(endAddrRTree);
		if (out.size() <= HEADER_LENGTH) {
			out.write(new byte[HEADER_LENGTH - out.size()]);
		} else {
			throw new RuntimeException("need to increase header length.");
		}

		// write components
		writeFile(fBlocks, out);
		writeFile(fileAddressLookupTable, out);
		writeFile(fRTree, out);

		System.out.println(out.size() + " bytes written to '" + targetFile + "'");

		out.flush();
		out.close();

		// ---------------- CLEAN UP TEMPORARY FILES --------------------------
		fBlocks.delete();
		fileAddressLookupTable.delete();
		fRTree.delete();
	}

	private static Rect getBoundingBox(Cluster c, Level graph) {
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		int[] vertexIds = c.getVertices();
		for (int vId : vertexIds) {
			LevelVertex v = graph.getVertex(vId);
			GeoCoordinate coord = v.getCoordinate();
			minLat = Math.min(coord.getLatitudeE6(), minLat);
			maxLat = Math.max(coord.getLatitudeE6(), maxLat);
			minLon = Math.min(coord.getLongitudeE6(), minLon);
			maxLon = Math.max(coord.getLongitudeE6(), maxLon);
			for (LevelEdge e : v.getOutboundEdges()) {
				GeoCoordinate[] waypoints = e.getWaypoints();
				if (waypoints != null) {
					for (GeoCoordinate wp : waypoints) {
						minLat = Math.min(wp.getLatitudeE6(), minLat);
						maxLat = Math.max(wp.getLatitudeE6(), maxLat);
						minLon = Math.min(wp.getLongitudeE6(), minLon);
						maxLon = Math.max(wp.getLongitudeE6(), maxLon);
					}
				}
			}
		}
		return new Rect(minLon, maxLon, minLat, maxLat);
	}

	/**
	 * Writes the content of the given file to the output stream.
	 * 
	 * @param f
	 *            the file to be written.
	 * @param oStream
	 *            the stream to write to.
	 * @return number of bytes written.
	 * @throws IOException
	 *             on write errors.
	 */
	private static long writeFile(File f, OutputStream oStream) throws IOException {
		byte[] buff = new byte[BUFFER_SIZE];
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));

		int len;
		long offset = 0;
		while ((len = in.read(buff)) > 0) {
			oStream.write(buff, 0, len);
			offset += len;
		}
		in.close();
		return offset;
	}
}
