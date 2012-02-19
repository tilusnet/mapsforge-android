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

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.mapsforge.core.Router;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.DBConnection;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.renderer.RendererV2;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level.LevelVertex;
import org.mapsforge.routing.server.hh.HHRouterServerside;

class ClusteringEvaluator {

	public static void main(String[] args) throws SQLException, FileNotFoundException,
			IOException, ClassNotFoundException {

		// get data from db

		Connection conn = DBConnection.getJdbcConnectionPg("localhost", 5432, "berlin",
				"osm", "osm");
		LevelGraph levelGraph = new LevelGraph(conn);
		int lvl = 0;
		Level graph = levelGraph.getLevel(lvl);

		// k-center
		int avgVerticesPerCluster = 500;
		int k = (int) Math.rint(graph.numVertices() / avgVerticesPerCluster);
		int overSamplingFac = 10;
		KCenterClustering kCenterClustering = KCenterClusteringAlgorithm.computeClustering(
				graph, k, overSamplingFac, KCenterClusteringAlgorithm.HEURISTIC_MIN_SIZE);
		for (int v = 0; v < levelGraph.getLevel(0).numVertices(); v++) {
			if (kCenterClustering.getCluster(v) == null) {
				System.out.println("no cluster assigned for vertex : " + v);
			}
		}

		// quad
		QuadTreeClustering quadClustering = QuadTreeClusteringAlgorithm.computeClustering(
				graph, levelGraph.getVertexLongitudesE6(), levelGraph.getVertexLatitudesE6(),
				QuadTreeClusteringAlgorithm.HEURISTIC_CENTER, avgVerticesPerCluster * 2,
				levelGraph.getLevel(0).numVertices());

		quadClustering = QuadTreeClusteringAlgorithm.computeClustering(levelGraph.getLevels(),
				levelGraph.getVertexLongitudesE6(), levelGraph.getVertexLatitudesE6(),
				QuadTreeClusteringAlgorithm.HEURISTIC_CENTER, 1000)[lvl];

		// verify
		int count = 0;
		for (Cluster c : quadClustering.getClusters()) {
			count += c.size();
		}
		System.out.println("VERIFY " + count + ":" + levelGraph.getLevel(lvl).numVertices());

		for (Iterator<LevelVertex> iter = levelGraph.getLevel(lvl).getVertices(); iter
				.hasNext();) {
			LevelVertex v = iter.next();
			if (quadClustering.getCluster(v.getId()) == null) {
				System.out.println("no cluster for vertex : " + v.getId());
			}
		}

		// render
		Router router = HHRouterServerside
				.deserialize(new FileInputStream("router/berlin.hh"));
		renderClustering(router, kCenterClustering);
		renderClustering(router, quadClustering);

		evaluateClustering(quadClustering, graph);
	}

	private static void renderClustering(Router router, Clustering clustering) {
		RendererV2 renderer1 = new RendererV2(1024, 768, router, Color.white, Color.black);
		renderer1.setClustering(clustering);		

	}

	private static void evaluateClustering(Clustering clustering, Graph graph) {
		int[] countV, countInternalE, countExternalE, countVE, percentInternalE;
		countV = new int[clustering.size()];
		countInternalE = new int[clustering.size()];
		countExternalE = new int[clustering.size()];
		countVE = new int[clustering.size()];
		percentInternalE = new int[clustering.size()];
		// compute counts
		int i = 0;
		for (Cluster c : clustering.getClusters()) {
			countV[i] = c.size();
			countVE[i] = c.size();
			countInternalE[i] = 0;
			countExternalE[i] = 0;
			for (int v : c.getVertices()) {
				for (Edge e : graph.getVertex(v).getOutboundEdges()) {
					if (clustering.getCluster(e.getTarget().getId()) == c) {
						countInternalE[i]++;
					} else {
						countExternalE[i]++;
					}
					countVE[i]++;
				}
			}
			percentInternalE[i] = (int) Math
					.rint((((double) countInternalE[i]) / ((double) (countInternalE[i] + countExternalE[i]))) * 100);
			i++;
		}

		int minV = min(countV);
		int maxV = max(countV);
		int minInternalE = min(countInternalE);
		int maxInternalE = max(countInternalE);
		int minExternalE = min(countExternalE);
		int maxExternalE = max(countExternalE);
		int minVE = min(countVE);
		int maxVE = max(countVE);
		int minPercentInternalE = min(percentInternalE);
		int maxPercentInternalE = max(percentInternalE);

		System.out.println("|V| = [" + minV + ", " + maxV + "]");
		System.out.println("|E|(internal) = [" + minInternalE + ", " + maxInternalE + "]");
		System.out.println("|V|(external) = [" + minExternalE + ", " + maxExternalE + "]");
		System.out.println("|V|+|E| = [" + minVE + ", " + maxVE + "]");
		System.out.println("|E|(internal) = [" + minPercentInternalE + "%, "
				+ maxPercentInternalE + "%]");

		int numIntervals = 20;
		int[] distV = intervalCount(countV, numIntervals);
		int[] distInternalE = intervalCount(countInternalE, numIntervals);
		int[] distExternalE = intervalCount(countExternalE, numIntervals);
		int[] distVE = intervalCount(countVE, numIntervals);
		int[] distPercentInternalE = intervalCount(percentInternalE, numIntervals);

		System.out.println("distV : " + arrToS(distV));
		System.out.println("distE(internal) : " + arrToS(distInternalE));
		System.out.println("distE(external) : " + arrToS(distExternalE));
		System.out.println("distVE : " + arrToS(distVE));
		System.out.println("distE(internal%) : " + arrToS(distPercentInternalE));
	}

	private static int[] intervalCount(int[] values, int n) {
		int[] counts = new int[n];
		double min = min(values);
		double max = max(values) + 0.000000001;
		double intervalSize = (max - min) / n;
		for (double v : values) {
			counts[(int) Math.floor((v - min) / intervalSize)]++;
		}
		return counts;
	}

	private static String arrToS(int[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i : arr) {
			sb.append(i + ", ");
		}
		return sb.toString();
	}

	private static int min(int[] arr) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < arr.length; i++) {
			min = Math.min(arr[i], min);
		}
		return min;
	}

	private static int max(int[] arr) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < arr.length; i++) {
			max = Math.max(arr[i], max);
		}
		return max;
	}
}
