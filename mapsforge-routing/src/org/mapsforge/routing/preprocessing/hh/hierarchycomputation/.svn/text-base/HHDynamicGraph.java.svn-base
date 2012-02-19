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

import gnu.trove.stack.array.TIntArrayStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

import org.mapsforge.routing.preprocessing.hh.dao.IRgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArrayGrowable;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.IntArrayGrowable;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedByteArrayGrowable;
import org.mapsforge.routing.preprocessing.hh.model.IRgEdge;
import org.mapsforge.routing.preprocessing.hh.model.IRgVertex;
import org.mapsforge.routing.preprocessing.hh.model.IRgWeightFunction;

/**
 * Array based Graph for use in preprocessing holding additional inbound adjacency lists.
 */
class HHDynamicGraph implements Serializable {

	private static final long serialVersionUID = -8913914656001216846L;
	private static final int INITIAL_NEIGHBORHOOD = Integer.MAX_VALUE;
	private static final int CHUNK_SIZE = 1000 * 1024;

	// vertex data
	// dim 1: vertexId
	// dim 2: level
	// dim 3:
	// -first value : neighborhood
	// -second value :
	// -second value : num-inbound edges
	// -other values : ids of adjacent edges,
	// inbound edges are inserted before outbound edges
	final int[][][] vertices;

	// edge data
	final IntArrayGrowable eSource;
	final IntArrayGrowable eTarget;
	final IntArrayGrowable eWeight;
	final UnsignedByteArrayGrowable eHops;
	final UnsignedByteArrayGrowable eMinLevel;
	final UnsignedByteArrayGrowable eMaxLevel;
	private final BitArrayGrowable eSlotUsed;
	final BitArrayGrowable eFwd;
	final BitArrayGrowable eBwd;
	final BitArrayGrowable eShortcut;

	// counts for each level
	private int[] lvlNumVertices, lvlNumEdges;

	// edge id assigning
	private int nextEdgeId;
	private TIntArrayStack freeEdgeSlots;

	public HHDynamicGraph(int numVertices) {

		// initialize vertices
		vertices = new int[numVertices][1][];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i][0] = new int[] { INITIAL_NEIGHBORHOOD, 0 };
		}

		// initialize edges
		eTarget = new IntArrayGrowable(CHUNK_SIZE);
		eWeight = new IntArrayGrowable(CHUNK_SIZE);
		eSource = new IntArrayGrowable(CHUNK_SIZE);
		eHops = new UnsignedByteArrayGrowable(CHUNK_SIZE);
		eMinLevel = new UnsignedByteArrayGrowable(CHUNK_SIZE);
		eMaxLevel = new UnsignedByteArrayGrowable(CHUNK_SIZE);
		eSlotUsed = new BitArrayGrowable(CHUNK_SIZE);
		eFwd = new BitArrayGrowable(CHUNK_SIZE);
		eBwd = new BitArrayGrowable(CHUNK_SIZE);
		eShortcut = new BitArrayGrowable(CHUNK_SIZE);

		// initialize counts
		lvlNumVertices = new int[] { numVertices };
		lvlNumEdges = new int[] { 0 };

		// edge id related
		nextEdgeId = 0;
		freeEdgeSlots = new TIntArrayStack();
	}

	public static <V extends IRgVertex, E extends IRgEdge> HHDynamicGraph importRoutingGraph(
			IRgDAO<V, E> dao, IRgWeightFunction<E> wFunc) {

		HHDynamicGraph graph = new HHDynamicGraph(dao.getNumVertices());
		int count = 0;
		int lvl = 0;
		for (Iterator<? extends E> iter = dao.getEdges().iterator(); iter.hasNext();) {
			if (count % 10000 == 0) {
				System.out.println("read edges " + count + " - " + (count + 10000));
			}
			E e = iter.next();
			graph.addEdge(e.getSourceId(), e.getTargetId(), wFunc.getWeightInt(e), true, e
					.isUndirected(), lvl);
			graph.addEdge(e.getTargetId(), e.getSourceId(), wFunc.getWeightInt(e), e
					.isUndirected(), true, lvl);
			count++;
		}
		return graph;
	}

	public void addLevel() {
		lvlNumVertices = insertIntValueLast(lvlNumVertices, 0);
		lvlNumEdges = insertIntValueLast(lvlNumEdges, 0);
	}

	public HHDynamicVertex getVertex(int id) {
		if (id < vertices.length && id >= 0) {
			return new HHDynamicVertex(id);
		}
		return null;

	}

	public HHDynamicEdge getEdge(int id) {
		if (eSlotUsed.get(id)) {
			return new HHDynamicEdge(id);
		}
		return null;

	}

	public Iterator<HHDynamicVertex> getVertices(final int lvl) {
		return new Iterator<HHDynamicVertex>() {

			private volatile int nextId = getNextId(-1);

			@Override
			public synchronized boolean hasNext() {
				return nextId < vertices.length;
			}

			@Override
			public synchronized HHDynamicVertex next() {
				if (nextId < vertices.length) {
					int tmp = nextId;
					nextId = getNextId(nextId);
					if (vertices[tmp].length <= lvl) {
						// vertex has been removed while iterating
						return null;
					}
					return new HHDynamicVertex(tmp);

				}
				return null;

			}

			private int getNextId(int id) {
				int _id = id + 1;
				while (_id < vertices.length && vertices[_id].length <= lvl) {
					_id++;
				}
				return _id;
			}

			@Override
			public synchronized void remove() {
				// read only
			}
		};
	}

	public int numVertices() {
		int sum = 0;
		for (int i = 0; i < lvlNumVertices.length; i++) {
			sum += lvlNumVertices[i];
		}
		return sum;
	}

	public int numVertices(int lvl) {
		return lvlNumVertices[lvl];
	}

	public int numEdges() {
		int sum = 0;
		for (int i = 0; i < lvlNumEdges.length; i++) {
			sum += lvlNumEdges[i];
		}
		return sum;
	}

	public int numEdges(int lvl) {
		return lvlNumEdges[lvl];
	}

	public int numEdgeEntries() {
		return eSource.size() - freeEdgeSlots.size();
	}

	public int numLevels() {
		return lvlNumVertices.length;
	}

	public int maxInDegree() {
		int max = 0;
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices[i].length; j++) {
				max = Math.max(vertices[i][j][1], max);
			}
		}
		return max;
	}

	public int maxOutDegree() {
		int max = 0;
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices[i].length; j++) {
				max = Math.max(vertices[i][j].length - 2 - vertices[i][j][1], max);
			}
		}
		return max;
	}

	public HHDynamicEdge addEdge(int sourceId, int targetId, int weight, boolean forward,
			boolean backward, int lvl) {
		int _weight = weight;

		if (_weight <= 0) {
			_weight = 1;
		}
		// increase vertex level of source and target
		if (vertices[sourceId].length <= lvl) {
			increaseVertexLevel(sourceId, lvl);
		}
		if (vertices[targetId].length <= lvl) {
			increaseVertexLevel(targetId, lvl);
		}
		// obtain edgeId and insert edge data
		int edgeId;
		if (freeEdgeSlots.size() > 0) {
			edgeId = freeEdgeSlots.pop();
			eSource.set(edgeId, sourceId);
			eTarget.set(edgeId, targetId);
			eWeight.set(edgeId, _weight);
			eMinLevel.set(edgeId, lvl);
			eMaxLevel.set(edgeId, lvl);
			eHops.set(edgeId, 1);
			eSlotUsed.set(edgeId);
			eFwd.set(edgeId, forward);
			eBwd.set(edgeId, backward);
			eShortcut.set(edgeId, false);
		} else {
			edgeId = nextEdgeId++;
			eSource.add(sourceId);
			eTarget.add(targetId);
			eWeight.add(_weight);
			eMinLevel.add(lvl);
			eMaxLevel.add(lvl);
			eHops.add(1);
			eSlotUsed.add(true);
			eFwd.add(forward);
			eBwd.add(backward);
			eShortcut.add(false);
		}
		// insert references into both adjacency arrays
		insertOutboundEntry(sourceId, edgeId, lvl);
		insertInboundEntry(targetId, edgeId, lvl);
		// adjust count
		lvlNumEdges[lvl]++;

		return new HHDynamicEdge(edgeId);
	}

	public boolean addEdge(HHDynamicEdge e, int lvl) {
		int sourceId = eSource.get(e.id);
		int targetId = eTarget.get(e.id);
		if (eSlotUsed.get(e.id) && !containsInboundEntry(eTarget.get(e.id), e.id, lvl)) {
			int maxLevel = eMaxLevel.get(e.id);
			int minLevel = eMinLevel.get(e.id);

			// increase vertex level of source and target
			if (vertices[sourceId].length <= lvl) {
				increaseVertexLevel(sourceId, lvl);
			}
			if (vertices[targetId].length <= lvl) {
				increaseVertexLevel(targetId, lvl);
			}

			// insert references into both adjacency arrays
			insertOutboundEntry(sourceId, e.id, lvl);
			insertInboundEntry(targetId, e.id, lvl);

			// adjust min/max level
			if (lvl > maxLevel) {
				eMaxLevel.set(e.id, lvl);
			} else if (lvl < minLevel) {
				eMinLevel.set(e.id, lvl);
			}
			// adjust count
			lvlNumEdges[lvl]++;

			return true;
		}
		return false;

	}

	public boolean removeEdge(HHDynamicEdge e, int lvl) {
		int targetId = eTarget.get(e.id);
		if (eSlotUsed.get(e.id) && containsInboundEntry(targetId, e.id, lvl)) {
			int sourceId = eSource.get(e.id);
			int maxLevel = eMaxLevel.get(e.id);
			int minLevel = eMinLevel.get(e.id);

			// remove edge from adjacency arrays
			removeOutboundEntry(sourceId, e.id, lvl);
			removeInboundEntry(targetId, e.id, lvl);

			// adjust min and maxlevel
			// maybe decrease level of incident vertices
			if (maxLevel == lvl) {
				if (vertices[sourceId][lvl].length == 2) {
					decreaseVertexLevel(sourceId);
				}
				if (vertices[targetId][lvl].length == 2) {
					decreaseVertexLevel(targetId);
				}
				eMaxLevel.set(e.id, maxLevel - 1);
			}
			if (minLevel == lvl) {
				eMinLevel.set(e.id, minLevel + 1);
			}

			// make edge slot available if
			// edge is not present in other levels
			if (maxLevel == minLevel) {
				eSlotUsed.set(e.id, false);
				freeEdgeSlots.push(e.id);
			}

			// adjust edge count
			lvlNumEdges[lvl]--;

			return true;
		}
		return false;

	}

	public int getEdgeIdUpperBound() {
		return eSource.size();
	}

	public void reassignEdgeIds() {
		int numEdgeEntries = eSource.size() - freeEdgeSlots.size();
		// make edge ids to be in [0 .. numEdgeEntries - 1]

		// get free slots indices (targetSlots) below numEdges
		TIntArrayStack targetSlots = new TIntArrayStack();
		while (freeEdgeSlots.size() > 0) {
			int freeSlot = freeEdgeSlots.pop();
			if (freeSlot < numEdgeEntries) {
				targetSlots.push(freeSlot);
			}
		}

		// move all edges with id >= numEdgeEntries to a targetSlot
		int c = 0;
		for (int sourceSlot = numEdgeEntries; sourceSlot < eSource.size(); sourceSlot++) {
			if (eSlotUsed.get(sourceSlot)) {
				moveEdgeToSlot(sourceSlot, targetSlots.pop());
				c++;
			}
		}

		// push free edge slots
		for (int i = numEdgeEntries; i < eSource.size(); i++) {
			if (eSlotUsed.get(i)) {
				System.out.println("should not happen");
			}
			freeEdgeSlots.push(i);
		}
	}

	public void regroupVertices() {
		int lvlOffset = 0;
		for (int lvl = -1; lvl <= numLevels(); lvl++) {
			int i = lvlOffset;
			while (i < vertices.length && getCoreLevel(i) == lvl)
				i++;

			for (int j = vertices.length - 1; j > i; j--) {
				if (getCoreLevel(j) == lvl) {
					swapVertexIds(new HHDynamicVertex(i), new HHDynamicVertex(j));
					while (i < vertices.length && getCoreLevel(i) == lvl)
						i++;
				}
			}
			lvlOffset = i;
		}
	}

	public boolean removeTopLevel() {
		if (numVertices(numLevels() - 1) == 0 && numEdges(numLevels() - 1) == 0) {
			lvlNumEdges = removeLastIntValue(lvlNumEdges);
			lvlNumVertices = removeLastIntValue(lvlNumVertices);
			return true;
		}
		return false;

	}

	private int[] removeLastIntValue(int[] array) {
		if (array.length > 0) {
			int[] tmp = new int[array.length - 1];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = array[i];
			}
			return tmp;
		}
		return array;

	}

	public void swapVertexIds(HHDynamicVertex a, HHDynamicVertex b) {
		if (a.id != b.id) {
			// adjust source and target of a's adjacent edges
			for (int lvl = 0; lvl <= a.getMaxLevel(); lvl++) {
				for (HHDynamicEdge e : a.getOutboundEdges(lvl)) {
					eSource.set(e.getId(), b.getId());
				}
				for (HHDynamicEdge e : a.getInboundEdges(lvl)) {
					eTarget.set(e.getId(), b.getId());
				}
			}
			// adjust source and target of b's adjacent edges
			for (int lvl = 0; lvl <= b.getMaxLevel(); lvl++) {
				for (HHDynamicEdge e : b.getOutboundEdges(lvl)) {
					eSource.set(e.getId(), a.getId());
				}
				for (HHDynamicEdge e : b.getInboundEdges(lvl)) {
					eTarget.set(e.getId(), a.getId());
				}
			}
			// swap vertex data
			int[][] tmp = vertices[a.getId()];
			vertices[a.getId()] = vertices[b.getId()];
			vertices[b.getId()] = tmp;
		}
	}

	private int getCoreLevel(int vertexId) {
		HHDynamicVertex v = new HHDynamicVertex(vertexId);
		if (v.getNeighborhood(v.getMaxLevel()) != Integer.MAX_VALUE) {
			return v.getMaxLevel();
		}
		return v.getMaxLevel() - 1;

	}

	public void serialize(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
		fos.close();
	}

	public static HHDynamicGraph getFromSerialization(File f) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HHDynamicGraph graph = (HHDynamicGraph) ois.readObject();
		ois.close();
		fis.close();
		return graph;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName() + " (\n");
		for (int lvl = 0; lvl < lvlNumVertices.length; lvl++) {
			sb.append("  level " + lvl + ":\n");
			sb.append("     |V| = " + numVertices(lvl) + "\n");
			sb.append("     |E| = " + numEdges(lvl) + "\n");
		}
		sb.append("  all levels :\n");
		sb.append("     |V| = " + numVertices(0) + "\n");
		sb.append("     |E| = " + numEdges() + " (" + numEdgeEntries() + " entries)\n");
		sb.append(")");
		return sb.toString();
	}

	private void moveEdgeToSlot(int sourceSlot, int targetSlot) {

		// move edge from edgeIdx to freeIdx
		eSource.set(targetSlot, eSource.get(sourceSlot));
		eTarget.set(targetSlot, eTarget.get(sourceSlot));
		eWeight.set(targetSlot, eWeight.get(sourceSlot));
		eMinLevel.set(targetSlot, eMinLevel.get(sourceSlot));
		eMaxLevel.set(targetSlot, eMaxLevel.get(sourceSlot));
		eHops.set(targetSlot, eHops.get(sourceSlot));
		eFwd.set(targetSlot, eFwd.get(sourceSlot));
		eBwd.set(targetSlot, eBwd.get(sourceSlot));
		eShortcut.set(targetSlot, eShortcut.get(sourceSlot));
		eSlotUsed.clear(sourceSlot);
		eSlotUsed.set(targetSlot);

		// adjust adjacency entries
		int sourceId = eSource.get(sourceSlot);
		int targetId = eTarget.get(targetSlot);
		replaceAdjacencyEntry(sourceId, sourceSlot, targetSlot);
		replaceAdjacencyEntry(targetId, sourceSlot, targetSlot);
	}

	private void replaceAdjacencyEntry(int vertexId, int oldEdgeId, int newEdgeId) {
		for (int lvl = 0; lvl < vertices[vertexId].length; lvl++) {
			for (int j = 2; j < vertices[vertexId][lvl].length; j++) {
				if (vertices[vertexId][lvl][j] == oldEdgeId) {
					vertices[vertexId][lvl][j] = newEdgeId;
				}
			}
		}
	}

	private int getVertexInDegree(int vertexId, int lvl) {
		return vertices[vertexId][lvl][1];
	}

	private void increaseVertexLevel(int vertexId, int lvl) {
		int[][] tmp = new int[lvl + 1][];
		for (int l = 0; l < vertices[vertexId].length; l++) {
			tmp[l] = vertices[vertexId][l];
		}
		for (int l = vertices[vertexId].length; l < tmp.length; l++) {
			tmp[l] = new int[] { INITIAL_NEIGHBORHOOD, 0 };
			lvlNumVertices[l]++;
		}
		vertices[vertexId] = tmp;
	}

	private void decreaseVertexLevel(int vertexId) {
		int idx = vertices[vertexId].length - 1;
		while (vertices[vertexId][idx].length == 2 && idx > 0) {
			lvlNumVertices[idx]--;
			idx--;
		}
		if (vertices[vertexId][idx].length > 2) {
			idx++;
		}
		if (vertices[vertexId].length > idx) {
			int[][] tmp = new int[idx][];
			for (int i = 0; i < idx; i++) {
				tmp[i] = vertices[vertexId][i];
			}
			vertices[vertexId] = tmp;
		}
	}

	private boolean containsInboundEntry(int vertexId, int edgeId, int lvl) {
		if (lvl >= vertices[vertexId].length) {
			return false;
		}
		int inDegree = getVertexInDegree(vertexId, lvl);
		for (int i = 2; i < inDegree + 2; i++) {
			if (vertices[vertexId][lvl][i] == edgeId) {
				return true;
			}
		}
		return false;
	}

	private void insertInboundEntry(int vertexId, int edgeId, int lvl) {
		int[] tmp = new int[vertices[vertexId][lvl].length + 1];
		// neighborhood
		tmp[0] = vertices[vertexId][lvl][0];
		// number of inbound egdes
		tmp[1] = getVertexInDegree(vertexId, lvl) + 1;
		// insert edgeId
		tmp[2] = edgeId;
		// copy remaining values
		for (int i = 3; i < tmp.length; i++) {
			tmp[i] = vertices[vertexId][lvl][i - 1];
		}
		vertices[vertexId][lvl] = tmp;
	}

	private void insertOutboundEntry(int vertexId, int edgeId, int lvl) {
		int[] tmp = new int[vertices[vertexId][lvl].length + 1];
		for (int i = 0; i < tmp.length - 1; i++) {
			tmp[i] = vertices[vertexId][lvl][i];
		}
		tmp[tmp.length - 1] = edgeId;
		vertices[vertexId][lvl] = tmp;
	}

	private void removeInboundEntry(int vertexId, int edgeId, int lvl) {
		int[] tmp = new int[vertices[vertexId][lvl].length - 1];
		// neighborhood
		tmp[0] = vertices[vertexId][lvl][0];
		// number of inbound egdes
		tmp[1] = getVertexInDegree(vertexId, lvl) - 1;
		// copy values != edgeId
		int j = 2;
		for (int i = 2; i < vertices[vertexId][lvl].length; i++) {
			if (vertices[vertexId][lvl][i] != edgeId) {
				tmp[j++] = vertices[vertexId][lvl][i];
			}
		}
		vertices[vertexId][lvl] = tmp;
	}

	private void removeOutboundEntry(int vertexId, int edgeId, int lvl) {
		// copy neighborhood, numInboundEdges and inbound entries
		int[] tmp = new int[vertices[vertexId][lvl].length - 1];
		int firstOutboundEntry = 2 + vertices[vertexId][lvl][1];
		for (int i = 0; i < firstOutboundEntry; i++) {
			tmp[i] = vertices[vertexId][lvl][i];
		}
		// copy outbound entries
		int j = firstOutboundEntry;
		for (int i = firstOutboundEntry; i < vertices[vertexId][lvl].length; i++) {
			if (vertices[vertexId][lvl][i] != edgeId) {
				tmp[j++] = vertices[vertexId][lvl][i];
			}
		}
		vertices[vertexId][lvl] = tmp;
	}

	private int[] insertIntValueLast(int[] array, int value) {
		int[] tmp = new int[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			tmp[i] = array[i];
		}
		tmp[array.length] = value;
		return tmp;
	}

	public class HHDynamicEdge implements Serializable {

		private static final long serialVersionUID = 4460568588598360461L;

		final int id;

		public HHDynamicEdge(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public HHDynamicVertex getSource() {
			return new HHDynamicVertex(eSource.get(id));
		}

		public HHDynamicVertex getTarget() {
			return new HHDynamicVertex(eTarget.get(id));
		}

		public int getWeight() {
			return eWeight.get(id);
		}

		public int getMinLevel() {
			return eMinLevel.get(id);
		}

		public int getMaxLevel() {
			return eMaxLevel.get(id);
		}

		public void setHopCount(int numHops) {
			eHops.set(id, numHops);
		}

		public int getHopCount() {
			return eHops.get(id);
		}

		public boolean isForward() {
			return eFwd.get(id);
		}

		public boolean isBackward() {
			return eBwd.get(id);
		}

		public boolean isShortcut() {
			return eShortcut.get(id);
		}

		public void setShortcut(boolean b) {
			eShortcut.set(id, b);
		}
	}

	public class HHDynamicVertex implements Serializable {

		private static final long serialVersionUID = -5671641367450802170L;

		final int id;

		public HHDynamicVertex(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public HHDynamicEdge[] getInboundEdges(int lvl) {
			if (lvl < vertices[id].length) {
				HHDynamicEdge[] e = new HHDynamicEdge[vertices[id][lvl][1]];
				for (int i = 0; i < e.length; i++) {
					e[i] = new HHDynamicEdge(vertices[id][lvl][i + 2]);
				}
				return e;
			}
			return new HHDynamicEdge[0];

		}

		public int getMaxLevel() {
			return vertices[id].length - 1;
		}

		public int getNeighborhood(int lvl) {
			return vertices[id][lvl][0];
		}

		public void setNeighborhood(int nh, int lvl) {
			vertices[id][lvl][0] = nh;
		}

		public HHDynamicEdge[] getOutboundEdges(int lvl) {
			if (lvl < vertices[id].length) {
				int firstOutboundEgde = 2 + vertices[id][lvl][1];
				HHDynamicEdge[] e = new HHDynamicEdge[vertices[id][lvl].length
						- firstOutboundEgde];
				for (int i = 0; i < e.length; i++) {
					e[i] = new HHDynamicEdge(vertices[id][lvl][firstOutboundEgde + i]);
				}
				return e;
			}
			return new HHDynamicEdge[0];
		}

		public int getInDegree(int lvl) {
			if (lvl < vertices[id].length) {
				return vertices[id][lvl][1];
			}
			return 0;

		}

		public int getOutDegree(int lvl) {
			if (lvl < vertices[id].length) {
				return vertices[id][lvl].length - vertices[id][lvl][1] - 2;
			}
			return 0;
		}
	}
}
