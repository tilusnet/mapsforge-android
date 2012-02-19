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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHComputation;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.BinaryMinHeap;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.IBinaryHeapItem;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level.LevelEdge;
import org.mapsforge.routing.preprocessing.hh.mobile.LevelGraph.Level.LevelVertex;

/**
 * A Binary Heap and hash map based implementation of the Algorithm of dijkstra.
 */
class DijkstraAlgorithm {
	/**
	 * The priority used for determining the order for processing vertices.
	 */
	private final BinaryMinHeap<HeapItem, Integer> queue;
	/**
	 * The graph to perform the searches on.
	 */
	private final LevelGraph graph;
	/**
	 * All vertices discovered during a search are put here.
	 */
	private final TIntObjectHashMap<HeapItem> discovered;

	public DijkstraAlgorithm(LevelGraph graph) {
		this.queue = new BinaryMinHeap<HeapItem, Integer>(10000);
		this.graph = graph;
		this.discovered = new TIntObjectHashMap<HeapItem>();
	}

	/**
	 * Computes a shortest path between source and target within the specified level of the
	 * multileveled graph.
	 * 
	 * @param sourceId
	 *            the start vertex of the shortest path.
	 * @param targetId
	 *            the end vertex of the shortest path.
	 * @param level
	 *            the level of the graph to be searched.
	 * @param shortestPathBuff
	 *            all vertices of the shortest path are put here, sorted from source to target.
	 * @param hopIndicesBuff
	 *            hop indices are put here, this indices can be used as index to the adjacency
	 *            lists, starting at the source vertex. This is another way for describing a
	 *            path in a graph.
	 * @param forward
	 *            if the to true, edges must have the forward flag set, else they are ignored by
	 *            the dijkstra search.
	 * @param backward
	 *            if the to true, edges must have the backward flag set, else they are ignored
	 *            by the dijkstra search.
	 * @param coreOnly
	 *            search only the core
	 * @return the sum of the edge weights along the shortest path.
	 */
	public int getShortestPath(int sourceId, int targetId, int level,
			LinkedList<LevelVertex> shortestPathBuff, LinkedList<Integer> hopIndicesBuff,
			boolean forward, boolean backward, boolean coreOnly) {
		this.queue.clear();
		this.discovered.clear();

		HeapItem s = new HeapItem(sourceId, level, null);
		queue.insert(s);
		discovered.put(s.vertexId, s);
		while (!queue.isEmpty()) {
			HeapItem _u = queue.extractMin();
			LevelVertex u = graph.getLevel(level).getVertex(_u.vertexId);
			if (u.getId() == targetId) {
				break;
			}
			LevelEdge[] adjEdges = u.getOutboundEdges();
			for (int i = 0; i < adjEdges.length; i++) {
				LevelEdge e = adjEdges[i];
				if ((forward && !e.isForward())
						|| (backward && !e.isBackward())
						|| (coreOnly && (e.getTarget().getNeighborhood() == HHComputation.INFINITY_1))) {
					continue;
				}
				HeapItem _v = discovered.get(e.getTarget().getId());
				if (_v == null) {
					_v = new HeapItem(e.getTarget().getId(), _u.distance + e.getWeight(), u);
					queue.insert(_v);
					discovered.put(_v.vertexId, _v);
					_v.hopIdx = i;
				} else if (_v.distance > _u.distance + e.getWeight()) {
					queue.decreaseKey(_v, _u.distance + e.getWeight());
					_v.hopIdx = i;
					_v.parent = u;
				}
			}
		}
		HeapItem _t = discovered.get(targetId);
		if (_t == null) {
			return Integer.MAX_VALUE;
		}
		int distance = _t.distance;
		shortestPathBuff.addFirst(graph.getLevel(level).getVertex(targetId));
		while (_t.parent != null) {
			shortestPathBuff.addFirst(_t.parent);
			hopIndicesBuff.addFirst(_t.hopIdx);
			_t = discovered.get(_t.parent.getId());
		}
		return distance;
	}

	/**
	 * Returns the identifier of rank-th visited vertex of a dijkstra search.
	 * 
	 * @param sourceId
	 *            the id of the start vertex.
	 * @param rank
	 *            the rank of the vertex to be returned.
	 * @return the id of the vertex with the desired rank, -1 if such a vertex does not exist.
	 */
	public int getVertexByDijkstraRank(int sourceId, int rank) {
		this.queue.clear();
		this.discovered.clear();

		HeapItem s = new HeapItem(sourceId, 0, null);
		queue.insert(s);
		discovered.put(s.vertexId, s);
		int numsettled = 0;
		while (!queue.isEmpty()) {
			HeapItem _u = queue.extractMin();
			LevelVertex u = graph.getLevel(0).getVertex(_u.vertexId);
			numsettled++;
			if (numsettled == rank) {
				return u.getId();
			}
			LevelEdge[] adjEdges = u.getOutboundEdges();
			for (int i = 0; i < adjEdges.length; i++) {
				LevelEdge e = adjEdges[i];
				if (!e.isForward()) {
					continue;
				}
				HeapItem _v = discovered.get(e.getTarget().getId());
				if (_v == null) {
					_v = new HeapItem(e.getTarget().getId(), _u.distance + e.getWeight(), u);
					queue.insert(_v);
					discovered.put(_v.vertexId, _v);
					_v.hopIdx = i;
				} else if (_v.distance > _u.distance + e.getWeight()) {
					queue.decreaseKey(_v, _u.distance + e.getWeight());
					_v.hopIdx = i;
					_v.parent = u;
				}
			}
		}
		return -1;
	}

	private class HeapItem implements IBinaryHeapItem<Integer> {
		/**
		 * the array index within the heap, used for implementing the IBinaryHeapItem interface.
		 */
		private int heapIdx;
		/**
		 * the currently found minimal distance of this vertex to the source vertex, also used
		 * as heap key.
		 */
		int distance;
		/**
		 * the predecessor within the shortest path tree.
		 */
		LevelVertex parent;
		/**
		 * the id of the vertex this heap item is for.
		 */
		int vertexId;
		/**
		 * the hop idx to the outgoing adjacency list of the parent vertex. This edge leads to
		 * this vertex.
		 */
		int hopIdx; // the index of the outbound edge lying on shortest path tree

		/**
		 * This class holds information about each discovered / visited vertex.
		 * 
		 * @param vertexId
		 *            the id of the discovered vertex.
		 * @param distance
		 *            the best distance found by dijkstra's algorithm till now.
		 * @param parent
		 *            the predecessor within the shortest path tree.
		 */
		public HeapItem(int vertexId, int distance, LevelVertex parent) {
			this.vertexId = vertexId;
			this.distance = distance;
			this.parent = parent;
			this.heapIdx = -1;
			this.hopIdx = -1;
		}

		@Override
		public int getHeapIndex() {
			return heapIdx;
		}

		@Override
		public Integer getHeapKey() {
			return distance;
		}

		@Override
		public void setHeapIndex(int idx) {
			this.heapIdx = idx;

		}

		@Override
		public void setHeapKey(Integer key) {
			distance = key;
		}

	}
}
