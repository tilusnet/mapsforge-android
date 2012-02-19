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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.dao.IRgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicVertex;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHGraphProperties.HHLevelStats;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArraySynchronized;
import org.mapsforge.routing.preprocessing.hh.model.IRgEdge;
import org.mapsforge.routing.preprocessing.hh.model.IRgVertex;
import org.mapsforge.routing.preprocessing.hh.model.IRgWeightFunction;
import org.mapsforge.routing.server.hh.DistanceTable;

/**
 * 
 * This class is responsible for the complete process of preprocessing. As input, it reads a
 * properties file res/hhPreprocessing.properties. tuning params for preprocessing: (names
 * similar to highway hierarchies paper)
 * 
 * 1. h = neighborhood size
 * 
 * 2. c = contraction facor, see method isBypassable for details.
 * 
 * 3. vertexThreshold = on number of core node, should be >>> 0 to use distance table
 * 
 * 4. hop limit = limit on number of edges of level l - 1 a shortcut of level l can represent
 * 
 * 5. downgrade edge = downgrade edges leaving core (check of restriction 2 is no more needed
 * during query)
 */
public final class HHComputation {

	/**
	 * neighborhood of vertices not in core
	 */
	public static final int INFINITY_1 = Integer.MAX_VALUE;
	/**
	 * neighborhood of vertices belonging to the top level core.
	 */
	public static final int INFINITY_2 = Integer.MAX_VALUE - 1;

	private static final String SQL_CREATE_TABLES_FILE = "createTables.sql";

	/* hierarchy construction parameters */
	private static int H;
	static int HOP_LIMIT;
	private static int NUM_THREADS, VERTEX_THRESHOLD;
	private static boolean DOWNGRADE_EDGES_LEAVING_CORE;
	static double C;

	/**
	 * 
	 * @param <V>
	 *            type of vertices
	 * @param <E>
	 *            type of edges
	 * @param rgDao
	 *            data access object to input graph
	 * @param wFunc
	 *            weight function
	 * @param h
	 *            h-neighborhood
	 * @param hopLimit
	 *            max. length of shortcuts in hops
	 * @param c
	 *            contraction rate
	 * @param vertexThreshold
	 *            recursion anchor for highway network construction
	 * @param downgradeEdges
	 *            server side optimization (not for mobile routing)
	 * @param numThreads
	 *            number of threads used for re-computation
	 * @param outputDb
	 *            database to write to
	 * @throws SQLException
	 *             on error with database
	 * @throws IOException
	 *             on error reading createTablesFile
	 */
	public static <V extends IRgVertex, E extends IRgEdge> void doPreprocessing(
			IRgDAO<V, E> rgDao, IRgWeightFunction<E> wFunc, int h, int hopLimit, double c,
			int vertexThreshold, boolean downgradeEdges, int numThreads, Connection outputDb)
			throws SQLException, IOException {
		DecimalFormat df = new DecimalFormat("#.#");

		System.out.println("import routing graph : ");
		HHDynamicGraph graph = HHDynamicGraph.importRoutingGraph(rgDao, wFunc);

		System.out.println("\nstart hierarchy computation : ");
		System.out.println("input-graph : |V| = " + graph.numVertices() + " |E| = "
				+ graph.numEdges());
		System.out.println("h = " + h);
		System.out.println("hopLimit = " + hopLimit);
		System.out.println("c = " + df.format(c));
		System.out.println("vertexThreshold = " + vertexThreshold);
		System.out.println("downgradeEdges = " + downgradeEdges);
		System.out.println("numThreads = " + numThreads);

		System.out.println("create database schema");
		createTables(outputDb);

		// compute hierarchy
		long hierarchyComputationStart = System.currentTimeMillis();
		HierarchyComputationResult result = HHComputation.computeHierarchy(graph, h, hopLimit,
				c, vertexThreshold, downgradeEdges, numThreads);
		if (result == null) {
			System.out.println("aborting.");
			return;
		}
		System.out.println(result);
		double compTimeMinutes = ((double) (System.currentTimeMillis() - hierarchyComputationStart)) / 60000;

		// compute core distance table
		DistanceTable distanceTable = ThreadedDistanceTableComputation
				.computeCoreDistanceTable(graph, graph.numLevels() - 1, numThreads);

		// write result to output database
		HHDbWriter writer = new HHDbWriter(outputDb);
		writer.clearTables();

		// write vertices
		for (Iterator<V> iter = rgDao.getVertices().iterator(); iter.hasNext();) {
			IRgVertex v = iter.next();
			writer.writeVertex(result.originalVertexIdsToAssignedVertexId[v.getId()], v
					.getLongitude(), v.getLatitude());
		}
		writer.flush();

		// write vertex-levels
		for (int i = 0; i < graph.numVertices(0); i++) {
			HHDynamicVertex v = graph.getVertex(i);
			for (int lvl = 0; lvl <= v.getMaxLevel(); lvl++) {
				writer.writeVertexLevel(v.getId(), lvl, v.getNeighborhood(lvl));
			}
		}
		writer.flush();

		// write edges
		for (int i = 0; i < graph.numEdgeEntries(); i++) {
			HHDynamicEdge e = graph.getEdge(i);
			writer.writeEdge(e.getId(), e.getSource().getId(), e.getTarget().getId(), e
					.getWeight(), e.getMinLevel(), e.getMaxLevel(), e.isForward(), e
					.isBackward(), e.isShortcut());
		}
		writer.flush();

		// write hierarchy meta data
		writer.writeGraphProperties(new HHGraphProperties(new Date(System.currentTimeMillis()),
				"car", h, vertexThreshold, hopLimit, numThreads, c, compTimeMinutes,
				downgradeEdges, result.levelStats));
		// write distance table
		writer.writeDistanceTable(distanceTable);

		writer.flush();
		System.out.println("\n" + result);
		double minutes = (System.currentTimeMillis() - hierarchyComputationStart) / 60000d;
		System.out.println("finished in " + df.format(minutes) + " minutes.");
	}

	private static HierarchyComputationResult computeHierarchy(HHDynamicGraph graph, int h,
			int hopLimit, double c, int vertexThreshold, boolean downgradeEdgesLeavingCore,
			int numThreads) {
		H = h;
		HOP_LIMIT = hopLimit;
		NUM_THREADS = numThreads;
		C = c;
		VERTEX_THRESHOLD = vertexThreshold;
		DOWNGRADE_EDGES_LEAVING_CORE = downgradeEdgesLeavingCore;

		return computeHH(graph);
	}

	private static HierarchyComputationResult computeHH(HHDynamicGraph graph) {

		if (!verifyInputGraph(graph)) {
			return null;
		}
		LinkedList<HHLevelStats> levelInfo = new LinkedList<HHLevelStats>();
		levelInfo.add(new HHLevelStats(0, graph.numEdges(), graph.numVertices(), graph
				.numEdges(), graph.numVertices()));

		if (graph.numVertices(0) > VERTEX_THRESHOLD) {
			/*
			 * COMPUTE LEVEL ONE
			 */
			ThreadedNeighborhoodComputation.computeNeighborhoods(graph, 0, H, NUM_THREADS);
			BitArraySynchronized highwayEdges = new BitArraySynchronized(graph
					.getEdgeIdUpperBound());
			ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, 0, highwayEdges,
					NUM_THREADS, true);
			ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, 0, highwayEdges,
					NUM_THREADS, false);
			graph.addLevel();
			graph.addLevel();

			for (Iterator<HHDynamicVertex> iter = graph.getVertices(0); iter.hasNext();) {
				HHDynamicVertex v = iter.next();
				for (HHDynamicEdge e : v.getOutboundEdges(0)) {
					if (highwayEdges.get(e.getId())) {
						graph.addEdge(e, 1);
						graph.addEdge(e, 2);
					}
				}
			}
			Contractor.contractGraph(graph, 2);
			removeParallelEdges(graph, 2);
			addLevelInfo(graph, levelInfo);
			ThreadedNeighborhoodComputation.computeNeighborhoods(graph, 2, H, NUM_THREADS);

			for (Iterator<HHDynamicVertex> iter = graph.getVertices(1); iter.hasNext();) {
				HHDynamicVertex v = iter.next();
				if (v.getMaxLevel() == 2) {
					v.setNeighborhood(v.getNeighborhood(2), 1);
				} else {
					v.setNeighborhood(INFINITY_1, 1);
				}
				for (HHDynamicEdge e : v.getOutboundEdges(2)) {
					if (e.getMinLevel() == 2 && e.getHopCount() > 1) {
						graph.addEdge(e, 1);
					}
				}
			}
			// graph-level 1 now contains Highway network of level 0 + core edges of level 1
			// graph-level 2 now contains only the core of level 1
			// graph-level 2 is input for higher levels
			// remove non highway edges
			// copy highway network upward
			// contract it
			// remove parallel core edges
			// propagate new shortcuts downward -> next iteration
			/*
			 * COMPUTE FURTHER LEVELS
			 */
			while (graph.numVertices(graph.numLevels() - 1) > VERTEX_THRESHOLD) {

				// remove non highway edges
				highwayEdges = new BitArraySynchronized(graph.getEdgeIdUpperBound());
				ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, graph
						.numLevels() - 1, highwayEdges, NUM_THREADS, true);
				ThreadedHighwayNetworkComputation.computeHighwayNetwork(graph, graph
						.numLevels() - 1, highwayEdges, NUM_THREADS, false);
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 1); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
						if (!highwayEdges.get(e.getId())) {
							graph.removeEdge(e, graph.numLevels() - 1);
						}
					}
				}

				// add all edges to next level
				graph.addLevel();
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 2)) {
						graph.addEdge(e, graph.numLevels() - 1);
					}
				}

				// get core of topmost level
				Contractor.contractGraph(graph, graph.numLevels() - 1);
				removeParallelEdges(graph, graph.numLevels() - 1);
				addLevelInfo(graph, levelInfo);
				ThreadedNeighborhoodComputation.computeNeighborhoods(graph,
						graph.numLevels() - 1, H, NUM_THREADS);

				// set neighborhoods of vertices in 2nd highest level & propagate new shortcuts
				// to 2nd highest level
				for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
						.hasNext();) {
					HHDynamicVertex v = iter.next();
					if (v.getMaxLevel() == graph.numLevels() - 1) {
						v.setNeighborhood(v.getNeighborhood(graph.numLevels() - 1), graph
								.numLevels() - 2);
					} else {
						v.setNeighborhood(INFINITY_1, graph.numLevels() - 2);
					}
					for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
						if (e.getMinLevel() == graph.numLevels() - 1 && e.getHopCount() > 1) {
							graph.addEdge(e, graph.numLevels() - 2);
						}
					}
				}
			}

			// set neighborhoods of top level
			for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 2); iter
					.hasNext();) {
				HHDynamicVertex v = iter.next();
				if (v.getMaxLevel() == graph.numLevels() - 1) {
					v.setNeighborhood(INFINITY_2, graph.numLevels() - 2);
				} else {
					v.setNeighborhood(INFINITY_1, graph.numLevels() - 2);
				}
			}

			// remove top level containing only the core
			for (Iterator<HHDynamicVertex> iter = graph.getVertices(graph.numLevels() - 1); iter
					.hasNext();) {
				HHDynamicVertex v = iter.next();
				for (HHDynamicEdge e : v.getOutboundEdges(graph.numLevels() - 1)) {
					graph.removeEdge(e, graph.numLevels() - 1);
				}
			}
			while (graph.numEdges(graph.numLevels() - 1) == 0
					&& graph.numVertices(graph.numLevels() - 1) == 0) {
				graph.removeTopLevel();
			}

			while (levelInfo.size() > graph.numLevels()) {
				levelInfo.removeLast();
			}

			if (DOWNGRADE_EDGES_LEAVING_CORE) {
				for (int lvl = 1; lvl < graph.numLevels(); lvl++) {
					for (Iterator<HHDynamicVertex> iter = graph.getVertices(lvl); iter
							.hasNext();) {
						HHDynamicVertex v = iter.next();
						for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {// TODO: check this,
							// may down grade
							// also top level
							// edges?
							if (e.getSource().getNeighborhood(lvl) != INFINITY_1
									&& e.getTarget().getNeighborhood(lvl) == INFINITY_1) {
								graph.addEdge(e, lvl - 1);
								graph.removeEdge(e, lvl);
								// remember : down grade from level 1 to level 0 never down
								// grades
								// shortcuts, since shortcuts of level 1 cannot leave the
								// core, they are always completely within the core!!!
							}
						}
					}
				}
			}

		}
		// group vertices by core level
		int[] orgIdToAssignedId = regroupVerticesByCoreLevel(graph);
		graph.reassignEdgeIds();

		return new HierarchyComputationResult(levelInfo, graph, orgIdToAssignedId);
	}

	private static void addLevelInfo(HHDynamicGraph graph, LinkedList<HHLevelStats> levelInfo) {
		levelInfo
				.add(new HHLevelStats(graph.numLevels() - 2, graph
						.numEdges(graph.numLevels() - 2), graph
						.numVertices(graph.numLevels() - 2), graph
						.numEdges(graph.numLevels() - 1), graph
						.numVertices(graph.numLevels() - 1)));
	}

	private static boolean verifyInputGraph(HHDynamicGraph graph) {
		// graph has exactly one level?
		if (graph.numLevels() != 1) {
			System.out.println("input graph has too many levels");
			return false;
		}
		for (int i = 0; i < graph.numVertices(); i++) {
			HHDynamicVertex v = graph.getVertex(i);
			// has unconnected vertices?
			if (v.getOutboundEdges(0).length == 0 && v.getInboundEdges(0).length == 0) {
				System.out.println("input graph has unconnected vertices");
				return false;
			}
			// negative edge weights?
			for (HHDynamicEdge e : v.getOutboundEdges(0)) {
				if (e.getWeight() < 0) {
					System.out.println("input graph has negative edge weights");
					return false;
				}
			}
		}
		return true;
	}

	private static int[] regroupVerticesByCoreLevel(HHDynamicGraph graph) {
		int[] originalIds = new int[graph.numVertices(0)];
		for (int i = 0; i < originalIds.length; i++) {
			originalIds[i] = i;
		}
		return originalIds;
		/*
		 * int lvlOffset = 0; for (int lvl = -1; lvl <= graph.numLevels(); lvl++) { int i =
		 * lvlOffset; while (i < graph.numVertices(0) && getCoreLevel(graph.getVertex(i)) ==
		 * lvl) i++;
		 * 
		 * for (int j = graph.numVertices(0) - 1; j > i; j--) { if
		 * (getCoreLevel(graph.getVertex(j)) == lvl) { graph.swapVertexIds(graph.getVertex(i),
		 * graph.getVertex(j)); int tmp = originalIds[i]; originalIds[i] = originalIds[j];
		 * originalIds[j] = tmp; while (i < graph.numVertices(0) &&
		 * getCoreLevel(graph.getVertex(i)) == lvl) i++; } } lvlOffset = i; } int[] orgIdToId =
		 * new int[graph.numVertices(0)]; for (int i = 0; i < orgIdToId.length; i++) {
		 * orgIdToId[originalIds[i]] = i; }
		 * 
		 * return orgIdToId;
		 */
	}

	// private static int getCoreLevel(HHDynamicVertex v) {
	// if (v.getNeighborhood(v.getMaxLevel()) != INFINITY_1) {
	// return v.getMaxLevel();
	// }
	// return v.getMaxLevel() - 1;
	// }

	private static void removeParallelEdges(HHDynamicGraph graph, int lvl) {
		for (Iterator<HHDynamicVertex> iter = graph.getVertices(lvl); iter.hasNext();) {
			HHDynamicVertex v = iter.next();
			HashMap<Integer, Integer> dFwd = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> dBwd = new HashMap<Integer, Integer>();
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				if (e.isForward()) {
					if (dFwd.containsKey(e.getTarget().getId())) {
						dFwd.put(e.getTarget().getId(), Math.min(e.getWeight(), dFwd.get(e
								.getTarget().getId())));
					} else {
						dFwd.put(e.getTarget().getId(), e.getWeight());
					}
				}
				if (e.isBackward()) {
					if (dBwd.containsKey(e.getTarget().getId())) {
						dBwd.put(e.getTarget().getId(), Math.min(e.getWeight(), dBwd.get(e
								.getTarget().getId())));
					} else {
						dBwd.put(e.getTarget().getId(), e.getWeight());
					}
				}
			}
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				if ((!e.isBackward() || (e.isBackward() && e.getWeight() > dBwd.get(e
						.getTarget().getId())))
						&& (!e.isForward() || (e.isForward() && e.getWeight() > dFwd.get(e
								.getTarget().getId())))) {
					graph.removeEdge(e, lvl);

				}
			}
		}
	}

	/*
	 * CONTRACTION (VERTEX REDUCTION BY INTRODUCING SHORTCUT EDGES)
	 */

	private static class Contractor {

		public static void contractGraph(HHDynamicGraph graph, int lvl) {
			TIntArrayStack queue = new TIntArrayStack();
			for (int i = 0; i < graph.numVertices(0); i++) {
				HHDynamicVertex v = graph.getVertex(i);
				bypassVertex(graph, v, queue, lvl);

			}
			while (queue.size() > 0) {
				bypassVertex(graph, graph.getVertex(queue.pop()), queue, lvl);
			}
		}

		private static void bypassVertex(HHDynamicGraph graph, HHDynamicVertex v,
				TIntArrayStack queue, int lvl) {
			if (!isBypassable(v, lvl)) {
				return;
			}
			// create shortcuts
			for (HHDynamicEdge in : v.getInboundEdges(lvl)) {
				for (HHDynamicEdge out : v.getOutboundEdges(lvl)) {
					if (in.getSource().getId() == out.getTarget().getId()) {
						// don't insert loops into the graph
						continue;
					}
					if ((in.isForward() != out.isForward())
							&& (in.isBackward() != out.isBackward())) {
						// no shortcut can be created
						continue;
					}
					HHDynamicEdge e = graph.addEdge(in.getSource().getId(), out.getTarget()
							.getId(), in.getWeight() + out.getWeight(), in.isForward()
							&& out.isForward(), in.isBackward() && out.isBackward(), lvl);
					if (!e.isForward() && !e.isBackward()) {
						System.out.println("error");
					}
					e.setHopCount(getHops(in, lvl) + getHops(out, lvl));
					e.setShortcut(true);
				}
			}

			// remove all adjacent edges of v and collect incident vertices
			TIntHashSet set = new TIntHashSet();
			for (HHDynamicEdge e : v.getInboundEdges(lvl)) {
				set.add(e.getSource().getId());
				graph.removeEdge(e, lvl);
			}
			for (HHDynamicEdge e : v.getOutboundEdges(lvl)) {
				set.add(e.getTarget().getId());
				graph.removeEdge(e, lvl);
			}

			// enqueue all incident vertices
			for (TIntIterator iter = set.iterator(); iter.hasNext();) {
				queue.push(iter.next());
			}
		}

		private static boolean isBypassable(HHDynamicVertex v, int lvl) {
			if (v == null || ((v.getInDegree(lvl) == 0) && (v.getOutDegree(lvl) == 0))) {
				return false;
			}

			// check if at least one shortcut would exceed the hopLimit
			int numShortcuts = 0;
			for (HHDynamicEdge in : v.getInboundEdges(lvl)) {
				for (HHDynamicEdge out : v.getOutboundEdges(lvl)) {
					if (in.getSource().getId() == out.getTarget().getId()) {
						continue;
					}
					if ((in.isForward() != out.isForward())
							&& (in.isBackward() != out.isBackward())) {
						continue;
					}
					if (getHops(in, lvl) + getHops(out, lvl) > HOP_LIMIT) {
						return false;
					}
					numShortcuts++;
				}
			}

			// check standard bypassability criterion
			float dIn = v.getInDegree(lvl);
			float dOut = v.getOutDegree(lvl);
			return numShortcuts <= C * (dIn + dOut);
		}

		private static int getHops(HHDynamicEdge e, int lvl) {
			if (e.getMinLevel() == lvl) {
				return e.getHopCount();
			}
			return 1;

		}
	}

	static class HierarchyComputationResult {
		public final HHLevelStats[] levelStats;
		public final int[] originalVertexIdsToAssignedVertexId;
		public final HHDynamicGraph highwayHierarchy;

		HierarchyComputationResult(LinkedList<HHLevelStats> levelInfo,
				HHDynamicGraph highwayHierarchy, int[] originalVertexIdsToAssignedVertexId) {
			this.levelStats = new HHLevelStats[levelInfo.size()];
			levelInfo.toArray(this.levelStats);
			this.originalVertexIdsToAssignedVertexId = originalVertexIdsToAssignedVertexId;
			this.highwayHierarchy = highwayHierarchy;
		}

		@Override
		public String toString() {
			String str = "";
			for (int i = 0; i < levelStats.length; i++) {
				str += levelStats[i] + "\n";
			}
			return str;
		}
	}

	private static void createTables(Connection conn) throws IOException, SQLException {
		InputStream in = HHComputation.class.getResourceAsStream(SQL_CREATE_TABLES_FILE);
		byte[] b = new byte[in.available()];
		in.read(b);
		in.close();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(new String(b));
		stmt.close();
	}
}
