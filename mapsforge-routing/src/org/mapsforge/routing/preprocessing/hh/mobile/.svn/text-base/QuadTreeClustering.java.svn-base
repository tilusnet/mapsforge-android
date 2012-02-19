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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;

/**
 * Quad-tree specific implementation of a Clustering.
 */
class QuadTreeClustering implements Clustering {

	private static final long serialVersionUID = 1L;

	private final TIntObjectHashMap<QuadTreeCluster> clusters;
	final int[] clusterIds;

	/**
	 * next cluster id to be assigned.
	 */
	private int nextClusterId;

	/**
	 * @param maxVertexId
	 *            highest vertex id within the graph.
	 */
	public QuadTreeClustering(int maxVertexId) {
		this.clusters = new TIntObjectHashMap<QuadTreeCluster>();
		this.clusterIds = new int[maxVertexId + 1];
		this.nextClusterId = 0;

		for (int i = 0; i < clusterIds.length; i++) {
			clusterIds[i] = -1;
		}
	}

	/**
	 * Adds a new cluster to this clustering.
	 * 
	 * @return Returns the newly added cluster.
	 */
	public QuadTreeCluster addCluster() {
		int clusterId = nextClusterId++;
		QuadTreeCluster c = new QuadTreeCluster(clusterId);
		clusters.put(clusterId, c);
		return c;
	}

	@Override
	public QuadTreeCluster getCluster(int vertexId) {
		return clusters.get(clusterIds[vertexId]);
	}

	@Override
	public Collection<QuadTreeCluster> getClusters() {
		return clusters.valueCollection();
	}

	@Override
	public int size() {
		return clusters.size();
	}

	class QuadTreeCluster implements Cluster {

		private static final long serialVersionUID = 1L;

		private TIntArrayList vertices;
		private int clusterId;

		QuadTreeCluster(int clusterId) {
			this.vertices = new TIntArrayList();
			this.clusterId = clusterId;
		}

		public boolean addVertex(int vertexId) {
			if (clusterIds[vertexId] == -1) {
				vertices.add(vertexId);
				clusterIds[vertexId] = clusterId;
				return true;
			}
			System.out.println("Warning: could not add vertex " + vertexId + " to cluster "
					+ clusterId);
			return false;
		}

		@Override
		public boolean containsVertex(int vertexId) {
			return clusterIds[vertexId] == clusterId;
		}

		@Override
		public int[] getVertices() {
			return vertices.toArray();
		}

		@Override
		public void swapVertices(int i, int j) {
			int tmp = vertices.get(i);
			vertices.set(i, vertices.get(j));
			vertices.set(j, tmp);
		}

		@Override
		public int size() {
			return vertices.size();
		}

	}
}
