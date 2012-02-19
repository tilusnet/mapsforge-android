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

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;

/**
 * This class implements a mapping from cluster to blocks and vice versa. This is analog to a
 * specialized double hash map. Additionally block ids can be reassigned by the swap operation.
 */
final class ClusterBlockMapping {

	/**
	 * maps cluster to block ids.
	 */
	private final TObjectIntHashMap<Cluster> blockIds;
	/**
	 * All clusters of all levels. The array index defines the block id of each cluster.
	 * 
	 */
	private final Cluster[] clusters;

	/**
	 * Construct the bidirectional mapping between clusters and block ids.
	 * 
	 * @param clustering
	 *            the clustering.
	 */
	public ClusterBlockMapping(Clustering[] clustering) {
		// get number of clusters
		int n = 0;
		for (Clustering c : clustering) {
			n += c.size();
		}

		this.clusters = new Cluster[n];
		this.blockIds = new TObjectIntHashMap<Cluster>();
		int blockId = 0;
		for (int lvl = 0; lvl < clustering.length; lvl++) {
			for (Cluster c : clustering[lvl].getClusters()) {
				clusters[blockId] = c;
				blockIds.put(c, blockId);
				blockId++;
			}
		}
	}

	/**
	 * swaps the ids of the i-th and the j-th cluster.
	 * 
	 * @param i
	 *            the first cluster to be swapped.
	 * @param j
	 *            the second cluster to be swapped.
	 */
	public void swapBlockIds(int i, int j) {
		swap(clusters, i, j);
		blockIds.put(clusters[i], i);
		blockIds.put(clusters[j], j);
	}

	/**
	 * Maps the given cluster to block ids.
	 * 
	 * @param col
	 *            the clusters to be mapped to blcok ids.
	 * @return return the block ids in the same order as the given clusters.
	 */
	public int[] getBlockIds(Collection<Cluster> col) {
		int[] arr = new int[col.size()];
		int i = 0;
		for (Cluster c : col) {
			arr[i++] = getBlockId(c);
		}
		return arr;
	}

	/**
	 * Maps from cluster to the block id.
	 * 
	 * @param cluster
	 *            input for mapping. Must not be null.
	 * @return Returns the assigned block id.
	 */
	public int getBlockId(Cluster cluster) {
		return blockIds.get(cluster);
	}

	/**
	 * Maps from block id to cluster.
	 * 
	 * @param blockId
	 *            must be valid.
	 * @return Returns the associated cluster.
	 */
	public Cluster getCluster(int blockId) {
		return clusters[blockId];
	}

	/**
	 * @return Returns the number of clusters / blocks.
	 */
	public int size() {
		return clusters.length;
	}

	/**
	 * swaps the i-th and j-th element of the array.
	 * 
	 * @param <T>
	 *            class type of array elements.
	 * @param arr
	 *            the array
	 * @param i
	 *            index
	 * @param j
	 *            index
	 */
	private static <T> void swap(T[] arr, int i, int j) {
		T tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

}
