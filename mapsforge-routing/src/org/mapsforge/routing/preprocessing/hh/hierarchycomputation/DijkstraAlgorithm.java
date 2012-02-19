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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicVertex;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.BinaryMinHeap;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue.IBinaryHeapItem;
import org.mapsforge.routing.server.hh.HHStaticGraph;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticEdge;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticVertex;

/**
 * implementation dijkstra s' algorithm an some hh specific details.
 * 
 */
public final class DijkstraAlgorithm {

	private static final int INITIAL_HEAP_SIZE = 1000;
	private static final int INITIAL_MAP_SIZE = 5000;

	// hack around java bug (compiler bug) and limitations of generics and warnings!!!
	private static class DistanceVertexHeap extends
			BinaryMinHeap<DijkstraDistanceVertex, Integer> {
		public DistanceVertexHeap(int initialSize) {
			super(initialSize);
		}

	}

	// hack around java bug (compiler bug) and limitations of generics and warnings!!!
	private static class TreeVertexHeap extends
			BinaryMinHeap<DijkstraTreeVertex, Integer> {
		public TreeVertexHeap(int initialSize) {
			super(initialSize);
		}
	}

	// hack around java bug (compiler bug) and limitations of generics and warnings!!!
	private static class TreeVertexHeap2 extends
			BinaryMinHeap<DijkstraTreeVertex2, Integer> {
		public TreeVertexHeap2(int initialSize) {
			super(initialSize);
		}
	}

	// hack around java bug (compiler bug) and limitations of generics and warnings!!!
	private static class SlackVertexHeap extends
			BinaryMinHeap<DijkstraSlackVertex, Integer> {
		public SlackVertexHeap(int initialSize) {
			super(initialSize);
		}

	}

	/**
	 * dijkstra shortest distance.
	 * 
	 * @param source
	 *            start node
	 * @param target
	 *            end node
	 * @param forward
	 *            search forward edge?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            the level to search in.
	 * @return the sum of the weight along the shortest path.
	 */
	public static int shortestDistance(HHDynamicVertex source, HHDynamicVertex target,
			boolean forward, boolean backward, int lvl) {
		if (!forward && !backward) {
			return -1;
		}

		BinaryMinHeap<DijkstraDistanceVertex, Integer> queue = new DistanceVertexHeap(
				INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraDistanceVertex> discoveredVertices = getMap();

		DijkstraDistanceVertex s = new DijkstraDistanceVertex(source, 0);
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		while (!queue.isEmpty()) {
			// dequeue
			DijkstraDistanceVertex u = queue.extractMin();
			// check if target is found
			if (u.vertex.getId() == target.getId()) {
				break;
			}
			// relax forward edges
			for (HHDynamicEdge e : u.vertex.getOutboundEdges(lvl)) {
				if ((forward && e.isForward()) || (backward && e.isBackward())) {
					DijkstraDistanceVertex v = discoveredVertices.get(e.getTarget().getId());
					if (v == null) {
						// new vertex is discovered
						v = new DijkstraDistanceVertex(e.getTarget(), Integer.MAX_VALUE);
						discoveredVertices.put(v.vertex.getId(), v);
						queue.insert(v);
					}
					int d = u.distance + e.getWeight();
					if (d < v.distance) {
						// shorter distance found
						queue.decreaseKey(v, d);
					}
				}
			}
		}
		int distance;
		if (discoveredVertices.contains(target.getId())) {
			distance = discoveredVertices.get(target.getId()).distance;
		} else {
			distance = Integer.MAX_VALUE;
		}
		queue.clear();
		discoveredVertices.clear();
		return distance;
	}

	/**
	 * the distance form source to the rank-th settled vertex.
	 * 
	 * @param source
	 *            start vertex.
	 * @param rank
	 *            specifies the rank-th closest vertex to the source.
	 * @param forward
	 *            search forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            to search in.
	 * @return the distance.
	 */
	public static int shortestDistance(HHDynamicVertex source, int rank, boolean forward,
			boolean backward, int lvl) {
		BinaryMinHeap<DijkstraDistanceVertex, Integer> queue = new DistanceVertexHeap(
				INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraDistanceVertex> discoveredVertices = getMap();
		int _rank = Math.max(1, rank);
		DijkstraDistanceVertex s = new DijkstraDistanceVertex(source, 0);
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		int numSettled = 0;
		int maxDistance = 0;
		while (!queue.isEmpty()) {
			// settle vertex u
			DijkstraDistanceVertex u = queue.extractMin();
			numSettled++;
			maxDistance = u.distance;
			// check abort criterion
			if (numSettled == _rank) {
				break;
			}

			for (HHDynamicEdge e : u.vertex.getOutboundEdges(lvl)) {
				if ((forward && e.isForward()) || (backward && e.isBackward())) {
					DijkstraDistanceVertex v = discoveredVertices.get(e.getTarget().getId());
					if (v == null) {
						// new vertex is discovered
						v = new DijkstraDistanceVertex(e.getTarget(), Integer.MAX_VALUE);
						discoveredVertices.put(v.vertex.getId(), v);
						queue.insert(v);
					}
					int d = u.distance + e.getWeight();
					if (d < v.distance) {
						// shorter distance found
						queue.decreaseKey(v, d);
					}
				}
			}
		}
		queue.clear();
		discoveredVertices.clear();
		return maxDistance;
	}

	/**
	 * Edges of the the shortest path.
	 * 
	 * @param source
	 *            start vertex
	 * @param target
	 *            end vertex
	 * @param forward
	 *            searhc forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            to search in.
	 * @return the edges along the shortest path.
	 */
	public static LinkedList<HHDynamicEdge> shortestPathEdges(HHDynamicVertex source,
			HHDynamicVertex target, boolean forward, boolean backward, int lvl) {
		DijkstraTreeVertex currentVertex = shortestPath(source, target, forward, backward, lvl);
		// get list of edges from source to target
		LinkedList<HHDynamicEdge> list = null;
		if (currentVertex != null) {
			list = new LinkedList<HHDynamicEdge>();
			while (currentVertex.parent != null) {
				list.addFirst(currentVertex.edgeToParent);
				currentVertex = currentVertex.parent;
			}
		}
		return list;
	}

	/**
	 * 
	 * @param source
	 *            start vertex
	 * @param target
	 *            end vertex
	 * @param forward
	 *            searhc forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            to search in.
	 * @return Vertices of the shortest path.
	 */
	public static LinkedList<HHDynamicVertex> shortestPathVertices(HHDynamicVertex source,
			HHDynamicVertex target, boolean forward, boolean backward, int lvl) {
		DijkstraTreeVertex currentVertex = shortestPath(source, target, forward, backward, lvl);
		// get list of edges from source to target
		LinkedList<HHDynamicVertex> list = null;
		if (currentVertex != null) {
			list = new LinkedList<HHDynamicVertex>();
			while (currentVertex != null) {
				list.addFirst(currentVertex.vertex);
				currentVertex = currentVertex.parent;
			}
		}
		return list;
	}

	/**
	 * @param source
	 *            start vertex
	 * @param forward
	 *            search forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            to search in.
	 * @return the complete shortest path tree.
	 */
	public static LinkedList<DijkstraTreeVertex> shortestPathTree(HHDynamicVertex source,
			boolean forward, boolean backward, int lvl) {
		BinaryMinHeap<DijkstraTreeVertex, Integer> queue = new TreeVertexHeap(INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraTreeVertex> discoveredVertices = getMap();
		DijkstraTreeVertex s = new DijkstraTreeVertex(source, 0, null, null, -1);
		LinkedList<DijkstraTreeVertex> settledVertices = new LinkedList<DijkstraTreeVertex>();
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		while (!queue.isEmpty()) {
			// dequeue
			DijkstraTreeVertex u = queue.extractMin();
			settledVertices.add(u);
			// check if target is found
			// relax forward edges
			int hopIdx = 0;
			for (HHDynamicEdge e : u.vertex.getOutboundEdges(lvl)) {
				if ((forward && e.isForward()) || (backward && e.isBackward())) {
					DijkstraTreeVertex v = discoveredVertices.get(e.getTarget().getId());
					if (v == null) {
						// new vertex is discovered
						v = new DijkstraTreeVertex(e.getTarget(), Integer.MAX_VALUE, null,
								null, hopIdx);
						discoveredVertices.put(v.vertex.getId(), v);
						queue.insert(v);
					}
					int d = u.distance + e.getWeight();
					if (d < v.distance) {
						// shorter distance found
						queue.decreaseKey(v, d);
						v.parent = u;
						v.edgeToParent = e;
						v.hopIdx = hopIdx;
					}
					hopIdx++;
				}
			}
		}
		queue.clear();
		discoveredVertices.clear();
		return settledVertices;
	}

	/**
	 * 
	 * @param source
	 *            start vertex
	 * @param target
	 *            end vertex
	 * @param forward
	 *            search forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            to search in.
	 * @return the indexes to adjacency lists describing the shortest path.
	 */
	public static LinkedList<Integer> shortestPathHopIndices(HHDynamicVertex source,
			HHDynamicVertex target, boolean forward, boolean backward, int lvl) {
		DijkstraTreeVertex currentVertex = shortestPath(source, target, forward, backward, lvl);
		// get list of edges from source to target
		LinkedList<Integer> list = null;
		if (currentVertex != null) {
			list = new LinkedList<Integer>();
			while (currentVertex.parent != null) {
				list.addFirst(currentVertex.hopIdx);
				currentVertex = currentVertex.parent;
			}
		}
		return list;
	}

	static LinkedList<HHDynamicEdge> selectHighwayEdges(HHDynamicVertex source,
			boolean forward, boolean backward, int lvl) {
		LinkedList<HHDynamicEdge> selectedEdges = new LinkedList<HHDynamicEdge>();
		if (!forward && !backward) {
			return selectedEdges;
		}
		LinkedList<DijkstraSlackVertex> dag = dag(source, forward, backward, lvl);
		if (!dag.isEmpty()) {
			// set initial slacks
			for (DijkstraSlackVertex v : dag) {
				v.slack = v.vertex.getNeighborhood(lvl);
			}

			int nh = source.getNeighborhood(lvl);
			// compute slack for parent p of v based on slack of u
			DijkstraSlackVertex v = dag.removeLast();
			while (!dag.isEmpty() && nh < v.distance) {

				for (ParentEntry pe : v.parentEntries) {
					DijkstraSlackVertex p = pe.parent;
					int parentSlack = v.slack - (pe.edgeToParent.getWeight());
					if (parentSlack < 0) {
						selectedEdges.add(pe.edgeToParent);
					}
					if (p.slack > parentSlack) {
						p.slack = parentSlack;
					}
				}
				v = dag.removeLast();
			}
		}
		return selectedEdges;
	}

	/**
	 * 
	 * @param source
	 *            start vertex
	 * @param target
	 *            end vertex
	 * @param forward
	 *            search forward edges?
	 * @param backward
	 *            search backward edges?
	 * @param lvl
	 *            of the graph.
	 * @param eMinLvl
	 *            minimum levels of all edges.
	 * @return edges that must be in the next level.
	 */

	public static LinkedList<Integer> shortestPathHopIndices(HHStaticVertex source,
			HHStaticVertex target, boolean forward, boolean backward, int lvl, int[] eMinLvl) {
		DijkstraTreeVertex2 v = shortestPath(source, target, forward, backward, lvl, eMinLvl);
		LinkedList<Integer> hopIndices = new LinkedList<Integer>();
		if (v == null) {
			return null;
		}
		while (v.parent != null) {
			hopIndices.addFirst(v.hopIdx);
			v = v.parent;
		}

		return hopIndices;
	}

	private static DijkstraTreeVertex2 shortestPath(HHStaticVertex source,
			HHStaticVertex target, boolean forward, boolean backward, int lvl, int[] eMinLvl) {
		BinaryMinHeap<DijkstraTreeVertex2, Integer> queue = new TreeVertexHeap2(
				INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraTreeVertex2> discoveredVertices = getMap();
		DijkstraTreeVertex2 s = new DijkstraTreeVertex2(source, 0, null, -1);
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		while (!queue.isEmpty()) {
			// dequeue
			DijkstraTreeVertex2 u = queue.extractMin();
			// check if target is found
			if (u.vertex.getId() == target.getId()) {
				break;
			}
			// relax forward edges
			int hopIdx = 0;
			for (HHStaticEdge e : u.vertex.getAdjacentEdges(0)) {
				if (eMinLvl[e.getId()] <= lvl && e.getLvl() >= lvl) {
					if ((forward && e.getDirection(HHStaticGraph.FWD))
							|| (backward && e.getDirection(HHStaticGraph.BWD))) {
						DijkstraTreeVertex2 v = discoveredVertices.get(e.getTarget().getId());
						if (v == null) {
							// new vertex is discovered
							v = new DijkstraTreeVertex2(e.getTarget(), Integer.MAX_VALUE, null,
									-1);
							discoveredVertices.put(v.vertex.getId(), v);
							queue.insert(v);
						}
						int d = u.distance + e.getWeight();
						if (d < v.distance) {
							// shorter distance found
							queue.decreaseKey(v, d);
							v.parent = u;
							v.hopIdx = hopIdx;
						}
					}
				}
				hopIdx++;
			}
		}
		DijkstraTreeVertex2 t = null;
		if (discoveredVertices.contains(target.getId())) {
			t = discoveredVertices.get(target.getId());
		}
		queue.clear();
		discoveredVertices.clear();
		return t;
	}

	private static DijkstraTreeVertex shortestPath(HHDynamicVertex source,
			HHDynamicVertex target, boolean forward, boolean backward, int lvl) {
		BinaryMinHeap<DijkstraTreeVertex, Integer> queue = new TreeVertexHeap(INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraTreeVertex> discoveredVertices = getMap();
		DijkstraTreeVertex s = new DijkstraTreeVertex(source, 0, null, null, -1);
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		while (!queue.isEmpty()) {
			// dequeue
			DijkstraTreeVertex u = queue.extractMin();
			// check if target is found
			if (u.vertex.getId() == target.getId()) {
				break;
			}
			// relax forward edges
			int hopIdx = 0;
			for (HHDynamicEdge e : u.vertex.getOutboundEdges(lvl)) {
				if ((forward && e.isForward()) || (backward && e.isBackward())) {
					DijkstraTreeVertex v = discoveredVertices.get(e.getTarget().getId());
					if (v == null) {
						// new vertex is discovered
						v = new DijkstraTreeVertex(e.getTarget(), Integer.MAX_VALUE, null,
								null, hopIdx);
						discoveredVertices.put(v.vertex.getId(), v);
						queue.insert(v);
					}
					int d = u.distance + e.getWeight();
					if (d < v.distance) {
						// shorter distance found
						queue.decreaseKey(v, d);
						v.parent = u;
						v.edgeToParent = e;
						v.hopIdx = hopIdx;
					}
					hopIdx++;
				}
			}
		}
		DijkstraTreeVertex t = null;
		if (discoveredVertices.contains(target.getId())) {
			t = discoveredVertices.get(target.getId());
		}
		queue.clear();
		discoveredVertices.clear();
		return t;
	}

	private static LinkedList<DijkstraSlackVertex> dag(HHDynamicVertex source, boolean forward,
			boolean backward, int lvl) {
		// initialize queue and mapping from vertex to dijkstra-vertex
		BinaryMinHeap<DijkstraSlackVertex, Integer> queue = new SlackVertexHeap(
				INITIAL_HEAP_SIZE);
		TIntObjectHashMap<DijkstraSlackVertex> discoveredVertices = getMap();
		LinkedList<DijkstraSlackVertex> settledVertices = new LinkedList<DijkstraSlackVertex>();
		// initialize and enqueue source
		DijkstraSlackVertex s = new DijkstraSlackVertex(source, 0);
		s.borderDistance = 0;
		s.referenceDistance = Integer.MAX_VALUE;
		s.active = true;
		queue.insert(s);
		discoveredVertices.put(source.getId(), s);
		int numActive = 1;
		while (!queue.isEmpty()) {
			// dequeue vertex u
			DijkstraSlackVertex u = queue.extractMin();
			settledVertices.addLast(u);
			// check abort criterion
			if (numActive == 0) {
				break;
			}
			if (u.active) {
				numActive--;
			}
			if (u.referenceDistance != Integer.MAX_VALUE
					&& u.referenceDistance + u.vertex.getNeighborhood(lvl) < u.distance) {
				u.active = false;
			}
			// relax forward edges
			for (HHDynamicEdge e : u.vertex.getOutboundEdges(lvl)) {
				if ((forward && e.isForward()) || (backward && e.isBackward())) {
					DijkstraSlackVertex v = discoveredVertices.get(e.getTarget().getId());
					if (v == null) {
						// new vertex is discovered
						v = new DijkstraSlackVertex(e.getTarget(), Integer.MAX_VALUE);
						discoveredVertices.put(v.vertex.getId(), v);
						queue.insert(v);
					}
					int d = u.distance + e.getWeight();
					if (d < v.distance) {
						// shorter distance found
						queue.decreaseKey(v, d);
						v.parentEntries.clear();
						v.parentEntries.add(new ParentEntry(u, e));
						v.borderDistance = computeBorderDistance(v,
								settledVertices.size() == 1, v.vertex.getNeighborhood(lvl));
						v.referenceDistance = computeReferenceDistance(v);
						boolean b = false;
						for (ParentEntry pe : v.parentEntries) {
							b = b || pe.parent.active;
						}
						if (!v.active && b) {
							numActive++;
						} else if (v.active && !b) {
							numActive--;
						}
						v.active = b;

					} else if (d == v.distance) {
						// another shortest path is found
						v.parentEntries.add(new ParentEntry(u, e));
						v.borderDistance = computeBorderDistance(v,
								settledVertices.size() == 1, v.vertex.getNeighborhood(lvl));
						v.referenceDistance = computeReferenceDistance(v);
						boolean b = false;
						for (ParentEntry pe : v.parentEntries) {
							b = b || pe.parent.active;
						}
						if (!v.active && b) {
							numActive++;
						} else if (v.active && !b) {
							numActive--;
						}
						v.active = b;
					}
				}
			}

		}
		queue.clear();
		discoveredVertices.clear();
		return settledVertices;
	}

	private static int computeReferenceDistance(DijkstraSlackVertex x) {
		// set a_(x) = max(a(parent(x))
		int a_ = 0;
		for (ParentEntry p : x.parentEntries) {
			a_ = Math.max(a_, p.parent.referenceDistance);
		}
		if (a_ == Integer.MAX_VALUE && x.borderDistance < x.distance) {
			// x is the first node outside neighborhood of
			// 2nd node on path from root to x:
			// set a(x) = max(a(z)) | z = parent(y) && y = parent(x)
			int a = 0;
			for (ParentEntry px : x.parentEntries) {
				DijkstraSlackVertex y = px.parent;
				for (ParentEntry py : y.parentEntries) {
					DijkstraSlackVertex z = py.parent;
					a = Math.max(a, z.distance);
				}
			}
			return a;
		}
		// x is inside the neighborhood of
		// 2nd node on path from root to x
		return a_;

	}

	private static int computeBorderDistance(DijkstraSlackVertex x, boolean successorOfRoot,
			int neighborhood) {
		if (successorOfRoot) {
			return neighborhood + x.distance;
		}

		int max = x.borderDistance;
		for (ParentEntry p : x.parentEntries) {
			max = Math.max(max, p.parent.borderDistance);
		}
		return x.borderDistance = max;

	}

	private static <T> TIntObjectHashMap<T> getMap() {
		return new TIntObjectHashMap<T>(INITIAL_MAP_SIZE);
	}

	private static class DijkstraSlackVertex extends DijkstraDistanceVertex {
		public LinkedList<ParentEntry> parentEntries;
		public int borderDistance, referenceDistance, slack;
		public boolean active;

		public DijkstraSlackVertex(HHDynamicVertex vertex, int distance) {
			super(vertex, distance);
			this.parentEntries = new LinkedList<ParentEntry>();
			this.slack = -1;
			this.borderDistance = 0;
			this.referenceDistance = Integer.MAX_VALUE;
			this.active = false;
		}
	}

	private static class ParentEntry {
		// the successor vertex on tentative shortest path
		public final DijkstraSlackVertex parent;
		// edge which was relaxed when tentative shortest path was found
		public final HHDynamicEdge edgeToParent;

		public ParentEntry(DijkstraSlackVertex vertex, HHDynamicEdge edge) {
			this.parent = vertex;
			this.edgeToParent = edge;
		}
	}

	static class DijkstraTreeVertex extends DijkstraDistanceVertex {

		public DijkstraTreeVertex parent;
		public HHDynamicEdge edgeToParent;
		public int hopIdx;

		public DijkstraTreeVertex(HHDynamicVertex vertex, int distance,
				DijkstraTreeVertex parent, HHDynamicEdge edgeToParent, int hopIdx) {
			super(vertex, distance);
			this.parent = parent;
			this.edgeToParent = edgeToParent;
			this.hopIdx = hopIdx;
		}
	}

	private static class DijkstraTreeVertex2 implements IBinaryHeapItem<Integer> {

		public DijkstraTreeVertex2 parent;
		public int hopIdx;
		public HHStaticVertex vertex;
		public int distance;
		public int heapIndex;

		public DijkstraTreeVertex2(HHStaticVertex vertex, int distance,
				DijkstraTreeVertex2 parent, int hopIdx) {
			this.vertex = vertex;
			this.distance = distance;
			this.parent = parent;
			this.hopIdx = hopIdx;
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
			heapIndex = idx;

		}

		@Override
		public void setHeapKey(Integer key) {
			distance = key;
		}
	}

	private static class DijkstraDistanceVertex implements IBinaryHeapItem<Integer> {

		public HHDynamicVertex vertex;
		public int distance;
		public int heapIndex;

		public DijkstraDistanceVertex(HHDynamicVertex vertex, int distance) {
			this.vertex = vertex;
			this.distance = distance;
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
			heapIndex = idx;

		}

		@Override
		public void setHeapKey(Integer key) {
			distance = key;
		}
	}
}
