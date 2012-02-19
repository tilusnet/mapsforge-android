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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;

class KCenterClustering implements Clustering {

	private static final long serialVersionUID = 1L;

	private final TIntObjectHashMap<KCenterCluster> clusters;
	final int[] clusterIds;

	private int nextClusterId;

	public KCenterClustering(int maxVertexId) {
		this.clusters = new TIntObjectHashMap<KCenterCluster>();
		this.clusterIds = new int[maxVertexId + 1];
		this.nextClusterId = 0;

		for (int i = 0; i < clusterIds.length; i++) {
			clusterIds[i] = -1;
		}
	}

	public KCenterCluster addCluster(int centerVertex) {
		int clusterId = nextClusterId++;
		KCenterCluster c = new KCenterCluster(centerVertex, clusterId);
		clusters.put(clusterId, c);
		return c;
	}

	@Override
	public KCenterCluster getCluster(int vertexId) {
		if (vertexId < clusterIds.length) {
			return clusters.get(clusterIds[vertexId]);
		}
		return null;
	}

	@Override
	public Collection<KCenterCluster> getClusters() {
		return clusters.valueCollection();
	}

	public void removeCluster(KCenterCluster c) {
		for (TIntIterator iter = c.vertices.iterator(); iter.hasNext();) {
			int v = iter.next();
			clusterIds[v] = -1;
		}
		clusters.remove(c.clusterId);

		c.vertices.clear();
		c.centerVertex = 0;
		c.radius = 0;
	}

	@Override
	public int size() {
		return clusters.size();
	}

	class KCenterCluster implements Cluster {

		private static final long serialVersionUID = 1L;

		TIntArrayList vertices;
		int centerVertex;
		int radius;
		int clusterId;

		KCenterCluster(int centerVertex, int id) {
			this.vertices = new TIntArrayList();
			this.centerVertex = centerVertex;
			this.clusterId = id;

			addVertex(centerVertex, 0);
		}

		public boolean addVertex(int vertexId, int newRadius) {
			if (newRadius >= radius && clusterIds[vertexId] == -1) {
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

		public int getCenterVertex() {
			return centerVertex;
		}

		public int getRadius() {
			return radius;
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
