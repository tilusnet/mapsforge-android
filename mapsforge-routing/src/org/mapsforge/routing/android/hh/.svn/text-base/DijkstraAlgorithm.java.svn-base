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
package org.mapsforge.routing.android.hh;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.BinaryMinHeap;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.IBinaryHeapItem;

final class DijkstraAlgorithm {

	private final BinaryMinHeap<HeapItem, Integer> queue;
	private final HHRoutingGraph graph;
	private final TIntObjectHashMap<HeapItem> discovered;

	public DijkstraAlgorithm(HHRoutingGraph graph) {
		this.queue = new BinaryMinHeap<HeapItem, Integer>(10000);
		this.graph = graph;
		this.discovered = new TIntObjectHashMap<HeapItem>();
	}

	public int getShortestPath(int sourceId, int targetId, LinkedList<HHVertex> shortestPathBuff)
			throws IOException {
		this.queue.clear();
		this.discovered.clear();

		HeapItem s = new HeapItem(sourceId, 0, null);
		queue.insert(s);
		discovered.put(s.vertexId, s);
		while (!queue.isEmpty()) {
			HeapItem _u = queue.extractMin();
			HHVertex u = graph.getVertex(_u.vertexId);
			if (u.vertexIds[u.vertexIds.length - 2] == targetId) {
				break;
			}
			HHEdge[] adjEdges = graph.getOutboundEdges(u);
			for (int i = 0; i < adjEdges.length; i++) {
				HHEdge e = adjEdges[i];
				if (!e.isForward) {
					continue;
				}
				HeapItem _v = discovered.get(e.targetId);
				if (_v == null) {
					_v = new HeapItem(e.targetId, _u.distance + e.weight, u);
					queue.insert(_v);
					discovered.put(_v.vertexId, _v);
				} else if (_v.distance > _u.distance + e.weight) {
					queue.decreaseKey(_v, _u.distance + e.weight);
					_v.parent = u;
				}
			}
		}
		HeapItem _t = discovered.get(targetId);
		if (_t == null) {
			return Integer.MAX_VALUE;
		}
		int distance = _t.distance;
		HHVertex v = graph.getVertex(targetId);
		shortestPathBuff.add(v);
		while (_t.parent != null) {
			shortestPathBuff.add(_t.parent);
			_t = discovered.get(_t.parent.vertexIds[_t.parent.vertexIds.length - 2]);
		}
		return distance;
	}

	private class HeapItem implements IBinaryHeapItem<Integer> {

		private int heapIdx;
		int distance;
		HHVertex parent;
		int vertexId;

		public HeapItem(int vertexId, int distance, HHVertex parent) {
			this.vertexId = vertexId;
			this.distance = distance;
			this.parent = parent;
			this.heapIdx = -1;
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
