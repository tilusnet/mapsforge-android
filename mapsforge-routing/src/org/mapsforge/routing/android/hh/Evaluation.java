package org.mapsforge.routing.android.hh;
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
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.LineNumberReader;
//import java.io.PrintStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.text.DecimalFormat;
//import java.util.LinkedList;
//
//import org.mapsforge.core.DBConnection;
//import org.mapsforge.core.GeoCoordinate;
//import org.mapsforge.preprocessing.routing.highwayHierarchies.HHDbReader;
//import org.mapsforge.preprocessing.routing.highwayHierarchies.HHGraphProperties;
//
///**
// * ATTENTION : dirty coded CONSTRAINT use only one instance!!!
// * 
// */
//class Evaluation {
//
//	private static double[] BPS = readBPSFile();
//
//	private static double[] readBPSFile() {
//		try {
//			LineNumberReader lnr = new LineNumberReader(new FileReader(new File(
//					"evaluation/flash.dat")));
//			String line = lnra.readLine();
//			line = lnr.readLine();
//			LinkedList<Integer> list = new LinkedList<Integer>();
//			while (line != null) {
//				String[] cols = line.split("\t");
//				int bytesPerSecond = Integer.parseInt(cols[2]);
//				list.addLast(bytesPerSecond);
//				line = lnr.readLine();
//			}
//			double[] bps = new double[list.size() + 1];
//			int i = 1;
//			for (int b : list) {
//				bps[i++] = b;
//			}
//			return bps;
//		} catch (NumberFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private static class TestRoute {
//		final HHVertex source;
//		final HHVertex target;
//
//		public TestRoute(HHVertex source, HHVertex target) {
//			this.source = source;
//			this.target = target;
//		}
//	}
//
//	private static class Result {
//		int numGraphBlockReads;
//		int numFileSystemBlockReads;
//		int numSettledVertices;
//		int maxHeapSize;
//		int bytesRead;
//		double read_time_s;
//
//		Result() {
//			this.numGraphBlockReads = 0;
//			this.numFileSystemBlockReads = 0;
//			this.numSettledVertices = 0;
//			this.maxHeapSize = 0;
//			this.bytesRead = 0;
//			this.read_time_s = 0;
//		}
//	}
//
//	public static final int PHASE_A = 0;
//	public static final int PHASE_B = 1;
//
//	private static int currentPhase = PHASE_A;
//	private static Result[] currentResult = getEmptyResult();
//
//	private static final String SQL_INSERT_BINARY_FILE = "INSERT INTO hh_binary (file_name, c, h, hop_limit, hop_indices, clustering, clustering_threshold) VALUES(?, ?, ?, ?, ?, ?, ?);";
//	private static final String SQL_INSERT_TEST_ROUTE = "INSERT INTO test_route (file_name, rank, test_route_id, p1_num_settled, p1_max_heap_size, p1_num_cluster_reads, p1_bytes_read, p1_read_time_s, p2_num_settled, p2_max_heap_size, p2_num_cluster_reads, p2_bytes_read, p2_read_time_s, cache_size_bytes) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//	private static final String SQL_TRUNCATE_BINARY_FILE_TABLE = "TRUNCATE TABLE hh_binary CASCADE;";
//	private static final String SQL_TRUNCATE_TEST_ROUTE_TABLE = "TRUNCATE TABLE test_route CASCADE;";
//
//	// database
//	private static final String DB_HOST = "localhost";
//	private static final int DB_PORT = 5432;
//	private static final String DB_USER = "osm";
//	private static final String DB_PASS = "osm";
//
//	// general configuration
//	private static final int FILE_SYSTEM_BLOCK_SIZE = 4096;
//	private static final int CACHE_SIZE = 400 * 1000 * 1024; // 400MB big enough to cache it all
//
//	private static Result[] getEmptyResult() {
//		return new Result[] { new Result(), new Result() };
//	}
//
//	static void setPhase(int phase) {
//		// if (phase == PHASE_A || phase == PHASE_B) {
//		// currentPhase = phase;
//		// }
//	}
//
//	static void notifyBlockRead(long startAddr, long endAddr) {
//		// one graph block was read
//		// currentResult[currentPhase].numGraphBlockReads++;
//		// currentResult[currentPhase].numFileSystemBlockReads++;
//		// currentResult[currentPhase].bytesRead += (endAddr - startAddr);
//		// if ((endAddr - startAddr) < BPS.length) {
//		// currentResult[currentPhase].read_time_s += (1 / BPS[(int) (endAddr - startAddr)])
//		// * (endAddr - startAddr);
//		// } else {
//		// currentResult[currentPhase].read_time_s += (1 / BPS[BPS.length - 1])
//		// * (endAddr - startAddr);
//		//
//		// }
//	}
//
//	static void notifyVertexSettled() {
//		// currentResult[currentPhase].numSettledVertices++;
//	}
//
//	static void notifyHeapSizeChanged(int currentHeapSize) {
//		// currentResult[currentPhase].maxHeapSize = Math.max(
//		// currentResult[currentPhase].maxHeapSize, currentHeapSize);
//	}
//
//	private final Connection evalDbConnection;
//	private final PreparedStatement pstmtInsertBinaryFile;
//	private final PreparedStatement pstmtInsertTestRoute;
//
//	public Evaluation(Connection evalDbConnection) throws SQLException {
//		this.evalDbConnection = evalDbConnection;
//		this.pstmtInsertBinaryFile = this.evalDbConnection
//				.prepareStatement(SQL_INSERT_BINARY_FILE);
//		this.pstmtInsertTestRoute = this.evalDbConnection
//				.prepareStatement(SQL_INSERT_TEST_ROUTE);
//	}
//
//	private void executeTestRoutesWriteToDb(File[] hhBinaryFiles, File[] testRouteFiles,
//			int[] cacheSizes)
//			throws IOException, SQLException {
//		System.out.println("truncate tables");
//
//		Statement stmt = evalDbConnection.createStatement();
//		stmt.executeUpdate(SQL_TRUNCATE_BINARY_FILE_TABLE);
//		stmt.executeUpdate(SQL_TRUNCATE_TEST_ROUTE_TABLE);
//		stmt.close();
//
//		for (File hhBinaryFile : hhBinaryFiles) {
//			System.out.println("executing testroutes on file : '" + hhBinaryFile.getName()
//					+ "'");
//			Connection conn = getConnection(getDbNameFromFileName(hhBinaryFile));
//			HHGraphProperties props = getHHPropertiesFromDb(conn);
//			conn.close();
//
//			// (file_name, c, h, hop_limit, hop_indices)
//			pstmtInsertBinaryFile.setString(1, hhBinaryFile.getName());
//			pstmtInsertBinaryFile.setDouble(2, props.c);
//			pstmtInsertBinaryFile.setInt(3, props.h);
//			pstmtInsertBinaryFile.setInt(4, props.hopLimit);
//			pstmtInsertBinaryFile.setBoolean(5, getHasHopindicesFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.setString(6,
//					getClusteringAlgorithmNameFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.setInt(7, getClusteringThresholdFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.executeUpdate();
//
//			for (File testRoutesFile : testRouteFiles) {
//				for (int cs : cacheSizes) {
//					System.out.println(cs);
//					System.out.println("rank = " + Integer.parseInt(testRoutesFile.getName()
//							.substring(0, testRoutesFile.getName().length() - 4)));
//					LinkedList<Result[]> result = executeTestRouteBinaryFile(hhBinaryFile,
//							testRoutesFile, cs);
//					int testRouteId = 0;
//					for (Result[] r : result) {
//						pstmtInsertTestRoute.setString(1, hhBinaryFile.getName());
//						pstmtInsertTestRoute.setInt(2, Integer.parseInt(testRoutesFile
//								.getName()
//								.substring(0, testRoutesFile.getName().length() - 4)));
//						pstmtInsertTestRoute.setInt(3, testRouteId++);
//
//						pstmtInsertTestRoute.setInt(4, r[0].numSettledVertices);
//						pstmtInsertTestRoute.setInt(5, r[0].maxHeapSize);
//						pstmtInsertTestRoute.setInt(6, r[0].numGraphBlockReads);
//						pstmtInsertTestRoute.setInt(7, r[0].bytesRead);
//						pstmtInsertTestRoute.setDouble(8, r[0].read_time_s);
//
//						pstmtInsertTestRoute.setInt(9, r[1].numSettledVertices);
//						pstmtInsertTestRoute.setInt(10, r[1].maxHeapSize);
//						pstmtInsertTestRoute.setInt(11, r[1].numGraphBlockReads);
//						pstmtInsertTestRoute.setInt(12, r[1].bytesRead);
//						pstmtInsertTestRoute.setDouble(13, r[1].read_time_s);
//						pstmtInsertTestRoute.setInt(14, cs);
//
//						pstmtInsertTestRoute.addBatch();
//					}
//					pstmtInsertTestRoute.executeBatch();
//				}
//			}
//		}
//	}
//
//	private void executeTestRoutesWriteToDb(HHRoutingGraph routingGraph, File[] testRouteFiles)
//			throws IOException, SQLException {
//		System.out.println("truncate tables");
//
//		// Statement stmt = evalDbConnection.createStatement();
//		// stmt.executeUpdate(SQL_TRUNCATE_BINARY_FILE_TABLE);
//		// stmt.executeUpdate(SQL_TRUNCATE_TEST_ROUTE_TABLE);
//		// stmt.close();
//
//		File hhBinaryFile = new File("ger_12_k_center_75_true.blockedHH");
//		System.out.println("executing testroutes on file : '" + hhBinaryFile.getName()
//				+ "'");
//		Connection conn = getConnection(getDbNameFromFileName(hhBinaryFile));
//		HHGraphProperties props = getHHPropertiesFromDb(conn);
//		conn.close();
//
//		// (file_name, c, h, hop_limit, hop_indices)
//		try {
//			pstmtInsertBinaryFile.setString(1, hhBinaryFile.getName());
//			pstmtInsertBinaryFile.setDouble(2, props.c);
//			pstmtInsertBinaryFile.setInt(3, props.h);
//			pstmtInsertBinaryFile.setInt(4, props.hopLimit);
//			pstmtInsertBinaryFile.setBoolean(5, getHasHopindicesFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.setString(6,
//					getClusteringAlgorithmNameFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.setInt(7, getClusteringThresholdFromFileName(hhBinaryFile));
//			pstmtInsertBinaryFile.executeUpdate();
//		} catch (SQLException e) {
//
//		}
//		for (File testRoutesFile : testRouteFiles) {
//			System.out.println("rank = " + Integer.parseInt(testRoutesFile.getName()
//					.substring(0, testRoutesFile.getName().length() - 4)));
//			System.out.println("cache Size = " + routingGraph.getCacheSizeBytes());
//			LinkedList<Result[]> result = executeTestRouteBinaryFile(routingGraph,
//					testRoutesFile);
//			int testRouteId = 0;
//			for (Result[] r : result) {
//				pstmtInsertTestRoute.setString(1, hhBinaryFile.getName());
//				pstmtInsertTestRoute.setInt(2, Integer.parseInt(testRoutesFile
//						.getName()
//						.substring(0, testRoutesFile.getName().length() - 4)));
//				pstmtInsertTestRoute.setInt(3, testRouteId++);
//
//				pstmtInsertTestRoute.setInt(4, r[0].numSettledVertices);
//				pstmtInsertTestRoute.setInt(5, r[0].maxHeapSize);
//				pstmtInsertTestRoute.setInt(6, r[0].numGraphBlockReads);
//				pstmtInsertTestRoute.setInt(7, r[0].bytesRead);
//				pstmtInsertTestRoute.setDouble(8, r[0].read_time_s);
//
//				pstmtInsertTestRoute.setInt(9, r[1].numSettledVertices);
//				pstmtInsertTestRoute.setInt(10, r[1].maxHeapSize);
//				pstmtInsertTestRoute.setInt(11, r[1].numGraphBlockReads);
//				pstmtInsertTestRoute.setInt(12, r[1].bytesRead);
//				pstmtInsertTestRoute.setDouble(13, r[1].read_time_s);
//				pstmtInsertTestRoute.setInt(14, routingGraph.getCacheSizeBytes());
//
//				pstmtInsertTestRoute.addBatch();
//			}
//			pstmtInsertTestRoute.executeBatch();
//		}
//	}
//
//	static LinkedList<Result[]> executeTestRouteBinaryFile(File hhBinaryFile,
//			File testRoutesFile, int cacheSize)
//			throws IOException {
//		HHRoutingGraph routingGraph = new HHRoutingGraph(hhBinaryFile, cacheSize);
//		HHAlgorithm algo = new HHAlgorithm(routingGraph);
//		LinkedList<TestRoute> testRoutes = getTestRoutesFromFile(testRoutesFile, routingGraph);
//		LinkedList<Result[]> results = new LinkedList<Result[]>();
//		int errors = 0;
//		int count = 0;
//		for (TestRoute testRoute : testRoutes) {
//			currentResult = getEmptyResult();
//			try {
//				algo.getShortestPath(testRoute.source.vertexIds[0],
//						testRoute.target.vertexIds[0],
//						new LinkedList<HHEdge>(), false);
//			} catch (Exception e) {
//				errors++;
//				continue;
//			}
//			results.add(currentResult);
//
//			if (count % 10 == 0) {
//				System.out.println(count + "/" + testRoutes.size());
//			}
//			count++;
//		}
//		System.out.println("errors=" + errors);
//		return results;
//	}
//
//	static LinkedList<Result[]> executeTestRouteBinaryFile(HHRoutingGraph routingGraph,
//			File testRoutesFile)
//			throws IOException {
//		HHAlgorithm algo = new HHAlgorithm(routingGraph);
//		LinkedList<TestRoute> testRoutes = getTestRoutesFromFile(testRoutesFile, routingGraph);
//		LinkedList<Result[]> results = new LinkedList<Result[]>();
//		int errors = 0;
//		int count = 0;
//		for (TestRoute testRoute : testRoutes) {
//			currentResult = getEmptyResult();
//			try {
//				algo.getShortestPath(testRoute.source.vertexIds[0],
//						testRoute.target.vertexIds[0],
//						new LinkedList<HHEdge>(), false);
//			} catch (Exception e) {
//				errors++;
//				continue;
//			}
//			results.add(currentResult);
//
//			if (count % 10 == 0) {
//				System.out.println(count + "/" + testRoutes.size());
//			}
//			count++;
//		}
//		System.out.println("errors=" + errors);
//		return results;
//	}
//
//	private static LinkedList<TestRoute> getTestRoutesFromFile(File testRoutesFile,
//			HHRoutingGraph routingGraph)
//			throws IOException {
//		final int maxDistance = 300;
//		LinkedList<TestRoute> testRoutes = new LinkedList<TestRoute>();
//		LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(
//				testRoutesFile)));
//		String line;
//		while ((line = lnr.readLine()) != null) {
//			String[] coords = line.split(";");
//			String[] s = coords[0].split(",");
//			String[] t = coords[1].split(",");
//			HHVertex source = routingGraph.getNearestVertex(new GeoCoordinate(Double
//					.parseDouble(s[0]), Double.parseDouble(s[1])), maxDistance);
//			HHVertex target = routingGraph.getNearestVertex(new GeoCoordinate(Double
//					.parseDouble(t[0]), Double.parseDouble(t[1])), maxDistance);
//			testRoutes.add(new TestRoute(source, target));
//		}
//		lnr.close();
//		return testRoutes;
//	}
//
//	private Connection getConnection(String dbName) throws SQLException {
//		return DBConnection.getJdbcConnectionPg(DB_HOST, DB_PORT, dbName, DB_USER, DB_PASS);
//	}
//
//	private static HHGraphProperties getHHPropertiesFromDb(Connection conn) throws SQLException {
//		HHDbReader reader = new HHDbReader(conn);
//		return reader.getGraphProperties();
//	}
//
//	private String getDbNameFromFileName(File hhBinaryFile) {
//		return hhBinaryFile.getName().substring(0, 6);
//	}
//
//	private String getClusteringAlgorithmNameFromFileName(File hhBinaryFile) {
//		String s = hhBinaryFile.getName().substring(7);
//		if (s.startsWith("quad_tree")) {
//			return "quad_tree";
//		}
//		return "k_center";
//	}
//
//	private int getClusteringThresholdFromFileName(File hhBinaryFile) {
//		return Integer.parseInt(hhBinaryFile.getName().split("_")[4]);
//	}
//
//	private boolean getHasHopindicesFromFileName(File hhBinaryFile) {
//		return hhBinaryFile.getName().split("_")[5].startsWith("true");
//	}
//
//	public static void main(String[] args) throws IOException {
//		double[] x = BPS;
//		try {
//			printMin();
//			// File[] hhBinaryFiles = new File[] {
//			// new File("evaluation/opthh/ger_12_k_center_75_true.blockedHH")
//			// };
//			// int[] cs = new int[] { 1024 * 512, 1024 * 1024, 1024 * 1024 * 2, 1024 * 1024 * 4
//			// };
//			// for (int c : cs) {
//			// HHRoutingGraph routingGraph = new HHRoutingGraph(hhBinaryFiles[0],
//			// c);
//			// File[] testRoutesFiles = new File("evaluation/naviRoute/").listFiles();
//			// // for (int i = 0; i < testRoutesFiles.length; i++) {
//			// // executeTestRouteBinaryFile(routingGraph, testRoutesFiles[i]);
//			// // }
//			// Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432,
//			// "eval_navi",
//			// "osm", "osm");
//			// Evaluation eval = new Evaluation(conn);
//			// eval.executeTestRoutesWriteToDb(routingGraph, testRoutesFiles);
//			//
//			// }
//			// int[] cacheSizes = new int[35];
//			// cacheSizes[0] = 100 * 1024;
//			// for (int i = 1; i < cacheSizes.length; i++) {
//			// cacheSizes[i] = cacheSizes[i - 1] + (512 * 100);
//			// }
//
//			// int[] cacheSizes = new int[] { 1024 * 1024 };
//			//
//			// eval.executeTestRoutesWriteToDb(hhBinaryFiles,
//			// testRoutesFiles, cacheSizes);
//			//
//			// conn.commit();
//			// conn.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//			while (e.getNextException() != null) {
//				e = e.getNextException();
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static void printMin() throws SQLException {
//		DecimalFormat df = new DecimalFormat("#.##");
//		for (int i = 1; i <= 27; i++) {
//			String dbName = "ger_" + (i < 10 ? "0" : "") + i;
//			Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432, dbName,
//					"osm", "osm");
//			HHGraphProperties props = getHHPropertiesFromDb(conn);
//			System.out.println(i + " " + df.format(props.compTimeMinutes / 60));
//			conn.close();
//		}
//
//	}
//
//	public static void evalMobile(File binary, File routes, int CacheSize, PrintStream ps)
//			throws IOException {
//		HHRoutingGraph graph = new HHRoutingGraph(
//				new File("evaluation/opthh/ger_12_k_center_75_true.blockedHH"),
//				1024 * 1000 * 4);
//		ps.println("cpu" + "\t" + "read" + "\t" + "distance");
//		HHVertex s = graph.getNearestVertex(new GeoCoordinate(52.509769, 13.4567655), 300);
//		HHVertex t = graph.getNearestVertex(new GeoCoordinate(52.4556941, 13.2918805), 300);
//		LinkedList<HHEdge> spHH = new LinkedList<HHEdge>();
//		HHAlgorithm hh = new HHAlgorithm(graph);
//		long startNanos = System.nanoTime();
//		int d = hh.getShortestPath(s.vertexIds[0], t.vertexIds[0], spHH, false);
//		long endNanos = System.nanoTime();
//		ps.println((endNanos - startNanos) + "\t" + graph.readNanos + "\t" + d);
//	}
// }
