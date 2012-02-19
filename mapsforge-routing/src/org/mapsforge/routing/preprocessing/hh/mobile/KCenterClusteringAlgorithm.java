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

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.BinaryMinHeap;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.IBinaryHeapItem;
import org.mapsforge.routing.preprocessing.hh.mobile.KCenterClustering.KCenterCluster;

/**
 * Implementation of the k-center clustering algorithm as proposed in the publication of the
 * precomputed cluster distances shortest path algorithm. This is a randomized algorithm which
 * reduces some negative effects drastically by first oversampling and afterwards sampling down
 * to the specified number of clusters. The number of clusters can be specified in advance in
 * contrast to other algorithm like e.g. quad tree algorithm.
 */
class KCenterClusteringAlgorithm {

	/**
	 * interval for console output for cluster building.
	 */
	private static final int MSG_INT_BUILD_CLUSTERS = 100000;
	/**
	 * interval for console output for down sampling.
	 */
	private static final int MSG_INT_SAMPLE_DOWN = 1000;
	/**
	 * Down sampling heuristic, removes clusters of minimal size.
	 */
	public static final int HEURISTIC_MIN_SIZE = 0;
	/**
	 * Down sampling heuristic, removes cluster of minimal radius. The radius is defined as the
	 * weight longest shortest path from the center of a cluster to some other vertex of the
	 * same cluster.
	 */
	public static final int HEURISTIC_MIN_RADIUS = 1;

	/**
	 * The default heuristic to be used if an invalid heuristic is specified.
	 */
	private static final int HEURISTIC_DEFAULT = HEURISTIC_MIN_SIZE;

	/**
	 * Computes a clustering based on the given graph levels.
	 * 
	 * @param graph
	 *            the levels of the graph.
	 * @param avgVerticesPerCluster
	 *            another way to specify the number of clusters to be computed.
	 * @param oversamplingFac
	 *            factor for number of blocks to create in the over sampling phase.
	 * @param heuristic
	 *            see public static variables for available heuristics.
	 * @return the clusterings for each level of the graph.
	 */
	public static KCenterClustering[] computeClustering(Graph[] graph,
			int avgVerticesPerCluster, int oversamplingFac, int heuristic) {
		KCenterClustering[] clustering = new KCenterClustering[graph.length];
		for (int i = 0; i < graph.length; i++) {
			clustering[i] = computeClustering(graph[i], graph[i].numVertices()
					/ avgVerticesPerCluster, oversamplingFac, heuristic);
		}
		return clustering;
	}

	/**
	 * Computes a clustering based on the given graph levels.
	 * 
	 * @param graph
	 *            the graph to be clustered
	 * @param k
	 *            number of clusters to be computed.
	 * @param oversamplingFac
	 *            factor for number of blocks to create in the over sampling phase.
	 * @param heuristic
	 *            see public static variables for available heuristics.
	 * @return the k-center clustering.
	 */
	public static KCenterClustering computeClustering(Graph graph, int k, int oversamplingFac,
			int heuristic) {
		int k_ = Math.min(graph.numVertices(), k * oversamplingFac);
		System.out.println("computing k-center clustering (k = " + k + ", k' = " + k_ + ")");

		System.out.println("randomly choosing k' centers");
		KCenterClustering clustering = chooseRandomCenters(graph, k_);

		System.out.println("building clusters from centers");
		expandClusters(graph, clustering);

		System.out.println("sampling down to k = " + Math.min(k, k_) + " clusters");
		sampleDown(graph, clustering, k, heuristic);

		System.out.println("clustering unassigned vertices of unconnected componts");
		int numClustersCreated = clusterUnconnectedComponents(graph, clustering);
		System.out.println("created " + numClustersCreated + " additional clusters");

		return clustering;
	}

	/**
	 * Randomly choose k_ centers.
	 * 
	 * @param graph
	 *            the graph to choose center vertices from.
	 * @param k_
	 *            number of centers to choose.
	 * @return a clustering containing k_ clusters, each of them contains only the choosen
	 *         center vertex.
	 */
	private static KCenterClustering chooseRandomCenters(Graph graph, int k_) {

		Random rnd = new Random(1);
		int maxVertexId = 0;
		int[] ids = new int[graph.numVertices()];
		int offset = 0;
		for (Iterator<? extends Vertex> iter = graph.getVertices(); iter.hasNext();) {
			Vertex v = iter.next();
			ids[offset] = v.getId();
			maxVertexId = Math.max(ids[offset], maxVertexId);
			offset++;
		}
		KCenterClustering clustering = new KCenterClustering(maxVertexId);

		for (int i = 0; i < k_; i++) {
			int centerVertex = ids[rnd.nextInt(ids.length)];
			while (clustering.getCluster(centerVertex) != null) {
				centerVertex = ids[rnd.nextInt(ids.length)];
			}
			clustering.addCluster(centerVertex);
		}
		return clustering;
	}

	/**
	 * After choosing centers, each cluster contains only one vertex, the center vertex. This
	 * method assigns each unassigned vertex to the cluster with the nearest center.
	 * 
	 * @param graph
	 *            the graph this clustering belongs to.
	 * @param clustering
	 *            the clustering having only one vertex per cluster.
	 */
	private static void expandClusters(Graph graph, KCenterClustering clustering) {
		// map vertex id to heap item of enqueued vertex
		HashMap<Integer, HeapItem> enqueuedVertices = new HashMap<Integer, HeapItem>();

		// initialize heap and enqueue centers
		BinaryMinHeap<HeapItem, Integer> queue = new BinaryMinHeap<HeapItem, Integer>(10000);
		for (KCenterCluster c : clustering.getClusters()) {
			HeapItem item = new HeapItem(c.getCenterVertex(), 0, c.getCenterVertex());
			queue.insert(item);
			enqueuedVertices.put(c.getCenterVertex(), item);
		}

		// remember which vertices were visited
		TIntHashSet visited = new TIntHashSet();

		// dijkstra loop
		int count = 0;
		while (!queue.isEmpty()) {
			// dequeue vertex u
			HeapItem uItem = queue.extractMin();
			int u = uItem.vertexId;
			enqueuedVertices.remove(u);
			visited.add(u);

			// add u to the cluster his it parent belongs to
			if (uItem.parent != u) {
				clustering.getCluster(uItem.parent).addVertex(u, uItem.distance);
			}
			// relax adjacent edges
			Edge[] adjEdges = graph.getVertex(u).getOutboundEdges();
			for (int i = 0; i < adjEdges.length; i++) {
				int weight = adjEdges[i].getWeight();
				int v = adjEdges[i].getTarget().getId();

				// relax edge if v was not already visited
				if (!visited.contains(v)) {
					HeapItem vItem = enqueuedVertices.get(v);
					if (vItem == null) {
						vItem = new HeapItem(v, uItem.distance + weight, u);
						queue.insert(vItem);
						enqueuedVertices.put(v, vItem);
					} else {
						if (uItem.distance + weight < vItem.distance) {
							queue.decreaseKey(vItem, uItem.distance + weight);
							vItem.parent = u;
						}
					}
				}
			}
			if ((++count % MSG_INT_BUILD_CLUSTERS == 0)) {
				System.out.println("[build clusters] vertices : "
						+ (count - MSG_INT_BUILD_CLUSTERS) + " - " + count);
			}
		}
		System.out.println("[build clusters] vertices : "
				+ ((count / MSG_INT_BUILD_CLUSTERS) * MSG_INT_BUILD_CLUSTERS) + " - " + count);
	}

	/**
	 * Iteratively remove one cluster after another until only k clusters are left. This is the
	 * down sampling phase of the algorithm.
	 * 
	 * @param graph
	 *            the graph the clustering belongs to.
	 * @param clustering
	 *            the over sampled clustering.
	 * @param k
	 *            number of cluster after sampling down.
	 * @param heuristik
	 *            the heuristic for choosing cluster for removal.
	 */
	private static void sampleDown(Graph graph, KCenterClustering clustering, int k,
			int heuristik) {
		int count = 0;
		while (clustering.size() > k) {
			KCenterCluster cluster = chooseClusterForRemoval(graph, clustering, heuristik);
			removeClusterAndRearrange(graph, clustering, cluster);
			count++;
			if (count % MSG_INT_SAMPLE_DOWN == 0) {
				System.out.println("[sample down] clusters : " + (count - MSG_INT_SAMPLE_DOWN)
						+ " - " + count);
			}
		}
		System.out.println("[sample down] clusters : "
				+ ((count / MSG_INT_SAMPLE_DOWN) * MSG_INT_SAMPLE_DOWN) + " - " + count);

	}

	/**
	 * Removes a cluster, and assigns all vertices of this removed cluster to the neighbor
	 * cluster with the nearest center to the respective vertex.
	 * 
	 * @param graph
	 *            graph belonging to the clustering.
	 * @param clustering
	 *            the clustering (all vertices assined to a cluster)
	 * @param cluster
	 *            the cluster to be removed.
	 */
	private static void removeClusterAndRearrange(Graph graph, KCenterClustering clustering,
			KCenterCluster cluster) {
		// remove the cluster
		KCenterCluster[] adjClusters = getAdjacentClusters(graph, clustering, cluster);
		int clusterSize = cluster.size();
		clustering.removeCluster(cluster);

		// disseminate vertices to neighbor clusters using dijkstra:

		// map vertex id to heap item of enqueued vertex
		HashMap<Integer, HeapItem> enqueuedVertices = new HashMap<Integer, HeapItem>();

		// initialize heap and enqueue centers
		BinaryMinHeap<HeapItem, Integer> queue = new BinaryMinHeap<HeapItem, Integer>(10000);
		for (KCenterCluster c : adjClusters) {
			HeapItem item = new HeapItem(c.getCenterVertex(), 0, c.getCenterVertex());
			queue.insert(item);
			enqueuedVertices.put(c.getCenterVertex(), item);
		}

		// remember which vertices were visited
		TIntHashSet visited = new TIntHashSet();

		int disseminatedVertices = 0;
		// dijkstra loop (skip if all vertices of cluster are disseminated)
		while (!queue.isEmpty() && disseminatedVertices < clusterSize) {
			// dequeue vertex u
			HeapItem uItem = queue.extractMin();
			int u = uItem.vertexId;
			enqueuedVertices.remove(u);
			visited.add(u);

			// add u to the cluster his it parent belongs to, if u does not belong to any
			// cluster
			if (uItem.parent != u && clustering.getCluster(u) == null) {
				clustering.getCluster(uItem.parent).addVertex(u, uItem.distance);
				disseminatedVertices++;
			}
			// relax adjacent edges
			Edge[] adjEdges = graph.getVertex(u).getOutboundEdges();
			for (int i = 0; i < adjEdges.length; i++) {
				int weight = adjEdges[i].getWeight();
				int v = adjEdges[i].getTarget().getId();

				// relax edge if v was not already visited
				if (!visited.contains(v)) {
					HeapItem vItem = enqueuedVertices.get(v);
					if (vItem == null) {
						vItem = new HeapItem(v, uItem.distance + weight, u);
						queue.insert(vItem);
						enqueuedVertices.put(v, vItem);
					} else {
						if (uItem.distance + weight < vItem.distance) {
							queue.decreaseKey(vItem, uItem.distance + weight);
							vItem.parent = u;
						}
					}
				}
			}
		}
	}

	/**
	 * Computes all clusters adjacent to the given cluster by at least one edge.
	 * 
	 * @param graph
	 *            used for determining adjacency.
	 * @param clustering
	 *            the clustering
	 * @param cluster
	 *            the cluster for which the neighbors a queried.
	 * @return all adjacent clusters.
	 */
	private static KCenterCluster[] getAdjacentClusters(Graph graph,
			KCenterClustering clustering, KCenterCluster cluster) {
		THashSet<KCenterCluster> set = new THashSet<KCenterCluster>();
		for (int v : cluster.getVertices()) {
			for (Edge e : graph.getVertex(v).getOutboundEdges()) {
				KCenterCluster c = clustering.getCluster(e.getTarget().getId());
				if (c != null && !c.equals(cluster)) {
					set.add(c);
				}
			}
		}
		KCenterCluster[] adjClusters = new KCenterCluster[set.size()];
		set.toArray(adjClusters);
		return adjClusters;
	}

	/**
	 * Chose a cluster for removal based on the given heuristic.
	 * 
	 * @param graph
	 *            .
	 * @param clustering
	 *            .
	 * @param heuristik
	 *            .
	 * @return the choosen cluster.
	 */
	private static KCenterCluster chooseClusterForRemoval(Graph graph,
			KCenterClustering clustering, int heuristik) {
		switch (heuristik) {
			case HEURISTIC_MIN_RADIUS:
				return getMinCluster(clustering, new Comparator<KCenterCluster>() {

					@Override
					public int compare(KCenterCluster c1, KCenterCluster c2) {
						return c1.getRadius() - c2.getRadius();
					}
				});
			case HEURISTIC_MIN_SIZE:
				return getMinCluster(clustering, new Comparator<KCenterCluster>() {

					@Override
					public int compare(KCenterCluster c1, KCenterCluster c2) {
						return c1.size() - c2.size();
					}
				});
			default:
				return chooseClusterForRemoval(graph, clustering, HEURISTIC_DEFAULT);
		}
	}

	/**
	 * Checks all clusters an gives the minimal one. The comparator must be transitive.
	 * 
	 * @param clustering
	 *            contains the clusters to be checked.
	 * @param comp
	 *            transitive comparator.
	 * @return the minimal cluster with regard to the given comparator.
	 */
	private static KCenterCluster getMinCluster(KCenterClustering clustering,
			Comparator<KCenterCluster> comp) {
		KCenterCluster min = clustering.getClusters().iterator().next();
		for (KCenterCluster c : clustering.getClusters()) {
			if (comp.compare(c, min) < 0) {
				min = c;
			}
		}
		return min;
	}

	/**
	 * If the graph is not connected, it may happen that a run of the normal algorithm does not
	 * assign a cluster to vertices of some unconnected components.
	 * 
	 * To Fix this problem, this method iterates over all vertices and checks if a cluster is
	 * assigned to it. if not, a breath first search from this vertex is performed. This bfs
	 * only visits vertices not assigned to a cluster. All vertices visited are added to the
	 * same cluster. Clusters build this way are much likely to be smaller than the desired
	 * cluster size, so bfs is not a problem.
	 * 
	 * Each cluster build this way gets the radius 0. This allows distinguishing between regular
	 * build clusters, and clusters build this way.
	 * 
	 * When this method is finished, each vertex is assigned to a cluster.
	 * 
	 * @param graph
	 *            graph to be clustered
	 * @param clustering
	 *            has already been computed by the k-center algorithm for the given graph.
	 * @return number of clusters added.
	 */
	private static int clusterUnconnectedComponents(Graph graph, KCenterClustering clustering) {
		int numCreatedClusters = 0;
		for (Iterator<? extends Vertex> iter = graph.getVertices(); iter.hasNext();) {
			int v = iter.next().getId();
			// find vertices not yet assigned to a cluster
			if (clustering.getCluster(v) == null) {
				// create a new cluster for such a vertex
				KCenterCluster cluster = clustering.addCluster(v);
				numCreatedClusters++;

				// perform a breath first search from v
				LinkedList<Integer> fifoQueue = new LinkedList<Integer>();
				TIntHashSet discoveredVertices = new TIntHashSet();

				// enqueue v
				fifoQueue.addLast(v);
				discoveredVertices.add(v);

				while (!fifoQueue.isEmpty()) {
					// dequeue
					int vertexId = fifoQueue.removeFirst();

					if (clustering.getCluster(vertexId) == null && vertexId != v) {
						if (vertexId != v) {
							cluster.addVertex(vertexId, 0);
						}
					} else if (vertexId != v) {
						// current vertex is already assigned
						// to a cluster, do not relax its
						// outgoing edges
						continue;
					}

					// relax adjacent edges
					for (Edge e : graph.getVertex(vertexId).getOutboundEdges()) {
						int targetId = e.getTarget().getId();
						// enqueue edge target if it was not discovered before
						if (!discoveredVertices.contains(targetId)) {
							// enqueue
							fifoQueue.addLast(targetId);
							discoveredVertices.add(targetId);
						}
					}
				}
			}
		}
		return numCreatedClusters;
	}

	/**
	 * Implementation of a heap item for dijkstra's algorithm.
	 */
	private static class HeapItem implements IBinaryHeapItem<Integer> {
		/**
		 * the id of the vertex this item is for.
		 */
		final int vertexId;
		/**
		 * the index within the array based heap, used for implementing the interface.
		 */
		private int heapIndex;
		/**
		 * the best distance found so far.
		 */
		int distance;
		/**
		 * the predecessor within the shortest path tree.
		 */
		int parent;

		/**
		 * @param vertexId
		 *            the vertex this item is for.
		 * @param distance
		 *            the best distance found so far.
		 * @param parent
		 *            the predecessor within the current state of the shortest path tree.
		 */
		public HeapItem(int vertexId, int distance, int parent) {
			this.heapIndex = -1;
			this.distance = distance;
			this.vertexId = vertexId;
			this.parent = parent;
		}

		@Override
		public int getHeapIndex() {
			return heapIndex;
		}

		@Override
		public Integer getHeapKey() {
			return distance;
		}

		@Override
		public void setHeapIndex(int idx) {
			this.heapIndex = idx;

		}

		@Override
		public void setHeapKey(Integer key) {
			this.distance = key;
		}
	}
}
