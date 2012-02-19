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

final class HHAlgorithm {

	private static final int INITIAL_HH_QUEUE_SIZE = 300;
	private static final int INITIAL_HH_MAP_SIZE = 2000;
	private static final int INITIAL_DIJKSTRA_QUEUE_SIZE = 50;
	private static final int INITIAL_DIJKSTRA_MAP_SIZE = 100;
	private static final int FWD = 0;
	private static final int BWD = 1;
	private static final int HEAP_IDX_SETTLED = -123456789;

	private final HHRoutingGraph graph;
	private final HHQueue[] queue;
	private final HHMap[] discovered;
	private final BinaryMinHeap<DijkstraHeapItem, DijkstraHeapItem> queueDijkstra;
	private final TIntObjectHashMap<DijkstraHeapItem> discoveredDijkstra;

	public HHAlgorithm(HHRoutingGraph graph) {
		this.graph = graph;
		this.queue = new HHQueue[] { new HHQueue(INITIAL_HH_QUEUE_SIZE),
				new HHQueue(INITIAL_HH_QUEUE_SIZE) };
		this.discovered = new HHMap[] { new HHMap(INITIAL_HH_MAP_SIZE),
				new HHMap(INITIAL_HH_MAP_SIZE) };
		this.queueDijkstra = new BinaryMinHeap<DijkstraHeapItem, DijkstraHeapItem>(
				INITIAL_DIJKSTRA_QUEUE_SIZE);
		this.discoveredDijkstra = new TIntObjectHashMap<DijkstraHeapItem>(
				INITIAL_DIJKSTRA_MAP_SIZE);
	}

	public int getShortestPath(int sourceId, int targetId, LinkedList<HHEdge> shortestPathBuff,
			boolean clearCacheAfterPhaseA)
			throws IOException {

		int direction = FWD;
		int distance = Integer.MAX_VALUE;
		int searchScopeHitId = -1;

		HHVertex s = graph.getVertex(sourceId);
		HHHeapItem sItem = new HHHeapItem(0, 0, s.neighborhood, sourceId, sourceId, -1, -1, -1);
		queue[FWD].insert(sItem);
		discovered[FWD].put(s.vertexIds[0], sItem);
		graph.releaseVertex(s);

		HHVertex t = graph.getVertex(targetId);
		HHHeapItem tItem = new HHHeapItem(0, 0, t.neighborhood, targetId, targetId, -1, -1, -1);
		queue[BWD].insert(tItem);
		discovered[BWD].put(t.vertexIds[0], tItem);
		graph.releaseVertex(t);

		while (!queue[FWD].isEmpty() || !queue[BWD].isEmpty()) {

			if (queue[direction].isEmpty()) {
				direction = (direction + 1) % 2;
			}
			HHHeapItem uItem = queue[direction].extractMin();
			uItem.heapIdx = HEAP_IDX_SETTLED;

			if (uItem.distance > distance) {
				queue[direction].clear();
				continue;
			}

			HHHeapItem uItem_ = discovered[(direction + 1) % 2].get(uItem.idLvlZero);
			if (uItem_ != null && uItem_.heapIdx == HEAP_IDX_SETTLED) {
				if (distance > uItem.distance + uItem_.distance) {
					distance = uItem.distance + uItem_.distance;
					searchScopeHitId = uItem.idLvlZero;
				}
			}

			HHVertex u = graph.getVertex(uItem.id);
			if (uItem.gap == Integer.MAX_VALUE) {
				uItem.gap = u.neighborhood;
			}
			int lvl = uItem.level;
			int gap = uItem.gap;
			while (!relaxAdjacentEdges(uItem, u, direction, lvl, gap)
					&& u.vertexIds[lvl + 1] != -1) {
				// switch to next level
				lvl++;

				int levelId = u.vertexIds[lvl];
				graph.releaseVertex(u);
				u = graph.getVertex(levelId);

				uItem.id = u.vertexIds[lvl];
				gap = u.neighborhood;
			}
			direction = (direction + 1) % 2;
			graph.releaseVertex(u);
		}
		if (searchScopeHitId != -1) {

			if (clearCacheAfterPhaseA) {
				graph.clearCache();
			}

			expandEdges(discovered[FWD].get(searchScopeHitId), discovered[BWD]
					.get(searchScopeHitId), shortestPathBuff);
		}

		// clear temporary data
		this.discovered[FWD].clear();
		this.discovered[BWD].clear();
		this.queue[FWD].clear();
		this.queue[BWD].clear();
		this.queueDijkstra.clear();
		this.discoveredDijkstra.clear();

		return distance;
	}

	private boolean relaxAdjacentEdges(HHHeapItem uItem, HHVertex u, int direction, int lvl,
			int gap) throws IOException {
		boolean result = true;
		boolean forward = (direction == FWD);

		HHEdge[] adjEdges = graph.getOutboundEdges(u);
		for (int i = 0; i < adjEdges.length; i++) {
			HHEdge e = adjEdges[i];
			if ((forward && !e.isForward) || (!forward && !e.isBackward)) {
				graph.releaseEdge(e);
				continue;
			}

			int gap_ = gap;
			if (gap != Integer.MAX_VALUE) {
				gap_ = gap - e.weight;
				if (!e.isCore) {
					// don't leave the core
					graph.releaseEdge(e);
					continue;
				}
				if (gap_ < 0) {
					// edge crosses neighborhood of entry point, don't relax it
					result = false;
					graph.releaseEdge(e);
					continue;
				}
			}

			HHVertex v = graph.getVertex(e.targetId);
			HHHeapItem vItem = discovered[direction].get(v.vertexIds[0]);
			if (vItem == null) {
				vItem = new HHHeapItem(uItem.distance + e.weight, lvl, gap_, e.targetId,
						v.vertexIds[0], u.vertexIds[0], e.sourceId, e.targetId);
				discovered[direction].put(v.vertexIds[0], vItem);
				queue[direction].insert(vItem);
			} else if (vItem.compareTo(uItem.distance + e.weight, lvl, gap_) > 0) {
				vItem.distance = uItem.distance + e.weight;
				vItem.level = lvl;
				vItem.id = e.targetId;
				vItem.gap = gap_;
				vItem.parentIdLvlZero = u.vertexIds[0];
				vItem.eSrcId = e.sourceId;
				vItem.eTgtId = e.targetId;
				queue[direction].decreaseKey(vItem, vItem);
			}
			graph.releaseEdge(e);
			graph.releaseVertex(v);
		}

		return result;
	}

	private void expandEdges(HHHeapItem fwd, HHHeapItem bwd, LinkedList<HHEdge> buff)
			throws IOException {
		HHHeapItem item = fwd;
		while (item.eSrcId != -1) {
			if (graph.hasShortcutHopIndices) {
				// expandEdgeRecursiveByHopIndices(fwd.eSrcId, fwd.eTgtId, buff, true);
				expandEdgeRecursiveDijkstra(item.eSrcId, item.eTgtId, buff, true);
			} else if (graph.hasShortcutHopIndices) {
				expandEdgeRecursiveDijkstra(item.eSrcId, item.eTgtId, buff, true);
			}
			item = discovered[FWD].get(item.parentIdLvlZero);
		}
		item = bwd;
		while (item.eSrcId != -1) {
			if (graph.hasShortcutHopIndices) {
				// expandEdgeRecursiveByHopIndices(bwd.eSrcId, bwd.eTgtId, buff, false);
				expandEdgeRecursiveDijkstra(item.eSrcId, item.eTgtId, buff, false);
			} else if (graph.hasShortcutHopIndices) {
				expandEdgeRecursiveDijkstra(item.eSrcId, item.eTgtId, buff, false);
			}
			item = discovered[BWD].get(item.parentIdLvlZero);
		}
	}

	private void expandEdgeRecursiveDijkstra(int src, int tgt, LinkedList<HHEdge> buff,
			boolean fwd)
			throws IOException {
		HHVertex s = graph.getVertex(src);
		HHVertex t = graph.getVertex(tgt);

		HHEdge e = extractEdge(s, t, fwd);

		// if edge belongs to level 0, recursion anchor is reached!
		if (s.getLevel() == 0) {
			// add the edge to buff and do not release to pool!
			// if the edge is backward only, fetch the corresponding forward edge
			if (fwd) {
				buff.addFirst(e);
			} else {
				graph.releaseEdge(e);
				e = extractEdge(t, s, true);
				buff.addLast(e);
			}
			graph.releaseVertex(s);
			graph.releaseVertex(t);
			// need to return here since we don't want to release e
			// to the pool which is done at the end of the method.
			return;
		}

		// if edge is not a shortcut, we can directly jump to level 0
		// if not we have to expand the shortcut using dijkstra within the core
		// of the underlying level
		if (e.minLevel == 0) {
			expandEdgeRecursiveDijkstra(s.vertexIds[0], t.vertexIds[0], buff, fwd);
		} else {
			discoveredDijkstra.clear();
			queueDijkstra.clear();
			DijkstraHeapItem sItem = new DijkstraHeapItem(0, s.vertexIds[e.minLevel - 1], null);
			discoveredDijkstra.put(s.vertexIds[e.minLevel - 1], sItem);
			queueDijkstra.insert(sItem);

			while (!queueDijkstra.isEmpty()) {

				DijkstraHeapItem uItem = queueDijkstra.extractMin();
				if (uItem.id == t.vertexIds[e.minLevel - 1]) {
					// found target
					break;
				}
				HHVertex u = graph.getVertex(uItem.id);

				// relax edges
				HHEdge[] adjEdges = graph.getOutboundEdges(u);
				for (int i = 0; i < adjEdges.length; i++) {
					HHEdge e_ = adjEdges[i];
					if (!e_.isCore || (fwd && !e_.isForward) || (!fwd && !e_.isBackward)) {
						// -skip edge if it is not applicable for current search direction
						// -skip non core edges
						graph.releaseEdge(e_);
						continue;
					}
					DijkstraHeapItem vItem = discoveredDijkstra.get(e_.targetId);
					if (vItem == null) {
						vItem = new DijkstraHeapItem(uItem.distance + e_.weight, e_
								.targetId, uItem);
						discoveredDijkstra.put(e_.targetId, vItem);
						queueDijkstra.insert(vItem);
					} else if (vItem.distance > uItem.distance + e_.weight) {
						vItem.distance = uItem.distance + e_.weight;
						vItem.parent = uItem;
					}
					graph.releaseEdge(e_);
				}
				graph.releaseVertex(u);
			}
			DijkstraHeapItem i = discoveredDijkstra.get(t.vertexIds[e.minLevel - 1]);
			while (i.parent != null) {
				int s_ = i.parent.id;
				int t_ = i.id;
				expandEdgeRecursiveDijkstra(s_, t_, buff, fwd);
				i = i.parent;
			}
		}
		graph.releaseEdge(e);
		graph.releaseVertex(s);
		graph.releaseVertex(t);
	}

	private HHEdge extractEdge(HHVertex s, HHVertex t, boolean fwd)
			throws IOException {
		int minWeight = Integer.MAX_VALUE;
		HHEdge[] adjEdges = graph.getOutboundEdges(s);
		HHEdge result = null;
		for (int i = 0; i < adjEdges.length; i++) {
			if (adjEdges[i].targetId == t.vertexIds[t.vertexIds.length - 2]
					&& adjEdges[i].weight < minWeight
					&& ((fwd && adjEdges[i].isForward) || (!fwd && adjEdges[i].isBackward))) {

				minWeight = adjEdges[i].weight;
				graph.releaseEdge(result);
				result = adjEdges[i];
			} else {
				graph.releaseEdge(adjEdges[i]);
			}
		}
		return result;
	}

	private static class HHHeapItem implements IBinaryHeapItem<HHHeapItem>,
			Comparable<HHHeapItem> {

		int heapIdx;
		// the key
		public int distance;
		public int level;
		public int gap;
		//
		public int id;
		public int idLvlZero;

		public int parentIdLvlZero;
		public int eSrcId;
		public int eTgtId;

		public HHHeapItem(int distance, int level, int gap, int id, int idLvlZero,
				int parentIdLvlZero, int eSrcId, int eTgtId) {
			this.heapIdx = -1;
			this.distance = distance;
			this.level = level;
			this.gap = gap;

			this.id = id;
			this.idLvlZero = idLvlZero;

			this.parentIdLvlZero = parentIdLvlZero;
			this.eSrcId = eSrcId;
			this.eTgtId = eTgtId;
		}

		@Override
		public int getHeapIndex() {
			return heapIdx;
		}

		@Override
		public void setHeapIndex(int idx) {
			this.heapIdx = idx;
		}

		@Override
		public void setHeapKey(HHHeapItem key) {
			this.distance = key.distance;
			this.level = key.level;
			this.gap = key.gap;
		}

		@Override
		public int compareTo(HHHeapItem other) {
			if (distance < other.distance) {
				return -3;
			} else if (distance > other.distance) {
				return 3;
			} else if (level < other.level) {
				return -2;
			} else if (level > other.level) {
				return 2;
			} else if (gap < other.gap) {
				return -1;
			} else if (gap > other.gap) {
				return 1;
			} else {
				return 0;
			}
		}

		public int compareTo(int _distance, int _level, int _gap) {
			if (distance < _distance) {
				return -3;
			} else if (distance > _distance) {
				return 3;
			} else if (level < _level) {
				return -2;
			} else if (level > _level) {
				return 2;
			} else if (gap < _gap) {
				return -1;
			} else if (gap > _gap) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public HHHeapItem getHeapKey() {
			return this;
		}
	}

	private static class DijkstraHeapItem implements IBinaryHeapItem<DijkstraHeapItem>,
			Comparable<DijkstraHeapItem> {

		public int heapIdx;
		public int distance;
		public int id;
		public DijkstraHeapItem parent;

		public DijkstraHeapItem(int distance, int id, DijkstraHeapItem parent) {
			this.distance = distance;
			this.id = id;
			this.parent = parent;
		}

		@Override
		public int getHeapIndex() {
			return heapIdx;
		}

		@Override
		public DijkstraHeapItem getHeapKey() {
			return this;
		}

		@Override
		public void setHeapIndex(int idx) {
			this.heapIdx = idx;

		}

		@Override
		public void setHeapKey(DijkstraHeapItem key) {
			this.distance = key.distance;

		}

		@Override
		public int compareTo(DijkstraHeapItem other) {
			return distance - other.distance;
		}

	}

	private static class HHMap extends TIntObjectHashMap<HHHeapItem> {
		// need class without parameter to allow array creation without warning
		public HHMap(int initialCapacity) {
			super(initialCapacity);
		}

	}

	private static class HHQueue extends BinaryMinHeap<HHHeapItem, HHHeapItem> {
		// need class without parameter to allow array creation without warning
		public HHQueue(int initialSize) {
			super(initialSize);
		}
	}
}
