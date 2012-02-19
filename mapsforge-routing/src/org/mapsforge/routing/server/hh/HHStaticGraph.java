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
package org.mapsforge.routing.server.hh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHVertexLvl;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHGraphProperties;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHGraphProperties.HHLevelStats;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArray;

/**
 * Array based implementation of a graph. Level are collapsed (no extra adjacency list per
 * level). By specifying the min level when getting adjacent edges, lower levels can be skipped
 * with regard to increasing performance.
 * 
 * Provide an object oriented access layer, a primitive type access layer and a low level access
 * layer. Data must not be written only read!!! Immutable and Thread safe.
 */
public class HHStaticGraph implements Serializable {
	/**
	 * edge direction
	 */
	public static final int FWD = 0;
	/**
	 * edge direction
	 */
	public static final int BWD = 1;

	private static final long serialVersionUID = 5052572584967363425L;

	final int[] vFirstLvlVertex;
	final int[] vLvlVNh;
	final int[] vLvlFirstEdge;
	final int[] eSource;
	final int[] eTarget;
	final int[] eWeight;
	final BitArray[] eDirection;
	final BitArray eShortcut;
	private final int numVertices, numLvlVertices, numEdges;
	private final HHGraphProperties graphProperties;

	private HHStaticGraph(int numVertices, int numLvlVertices, int numEdges,
			HHGraphProperties metaData) {
		this.numVertices = numVertices;
		this.numLvlVertices = numLvlVertices;
		this.numEdges = numEdges;
		this.graphProperties = metaData;

		vFirstLvlVertex = new int[numVertices + 1];
		vFirstLvlVertex[numVertices] = numLvlVertices;
		vLvlVNh = new int[numLvlVertices];
		vLvlFirstEdge = new int[numLvlVertices + 1];
		vLvlFirstEdge[numLvlVertices] = numEdges;
		eSource = new int[numEdges];
		eTarget = new int[numEdges];
		eWeight = new int[numEdges];
		eDirection = new BitArray[] { new BitArray(numEdges), new BitArray(numEdges) };
		eShortcut = new BitArray(numEdges);
	}

	static HHStaticGraph getFromHHDb(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);

		HHStaticGraph g = new HHStaticGraph(reader.numVertices(), reader.numLevelVertices(),
				reader.numEdges(), reader.getGraphProperties());
		int offset = 0;
		for (Iterator<HHVertexLvl> iter = reader.getVertexLvls(); iter.hasNext();) {
			HHVertexLvl v = iter.next();
			g.vLvlVNh[offset] = v.neighborhood;
			if (v.lvl == 0) {
				g.vFirstLvlVertex[v.id] = offset;
			}
			offset++;
		}
		for (int i = 0; i < g.numLvlVertices; i++) {
			g.vLvlFirstEdge[i] = -1;
		}
		offset = 0;
		for (Iterator<HHEdge> iter = reader.getEdges(); iter.hasNext();) {
			HHEdge e = iter.next();
			g.eSource[offset] = e.sourceId;
			g.eTarget[offset] = e.targetId;
			g.eWeight[offset] = e.weight;
			g.eDirection[FWD].set(offset, e.fwd);
			g.eDirection[BWD].set(offset, e.bwd);
			g.eShortcut.set(offset, e.shortcut);
			for (int i = 0; i <= e.maxLvl; i++) {
				if (g.vLvlFirstEdge[g.vFirstLvlVertex[e.sourceId] + i] == -1) {
					g.vLvlFirstEdge[g.vFirstLvlVertex[e.sourceId] + i] = offset;
				}
			}
			offset++;
		}
		return g;
	}

	void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	static HHStaticGraph deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (HHStaticGraph) Serializer.deserialize(iStream);
	}

	void serialize(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
		fos.close();
	}

	static HHStaticGraph getFromSerialization(File f) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HHStaticGraph graph = (HHStaticGraph) ois.readObject();
		ois.close();
		fis.close();
		return graph;
	}

	HHStaticVertex getVertex(int id) {
		return new HHStaticVertex(id);
	}

	HHStaticEdge getEdge(int id) {
		return new HHStaticEdge(id);
	}

	int numVertices() {
		return numVertices;
	}

	int numEdges() {
		return numEdges;
	}

	int numLevels() {
		return graphProperties.levelStats.length;
	}

	HHGraphProperties getGraphPropterties() {
		return graphProperties;
	}

	@Override
	public String toString() {
		String str = "";
		for (HHLevelStats ls : graphProperties.levelStats) {
			str += ls + "\n";
		}
		return str;
	}

	/**
	 * Lighweight vertex.
	 */
	public class HHStaticVertex {

		private final int id;

		HHStaticVertex(int id) {
			this.id = id;
		}

		/**
		 * @return vertex id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param minLvl
		 *            ingore edges below min level.
		 * @return outgoing edges.
		 */
		public HHStaticEdge[] getAdjacentEdges(int minLvl) {
			int startIdx = vLvlFirstEdge[vFirstLvlVertex[id] + minLvl];
			int endIdx = vLvlFirstEdge[vFirstLvlVertex[id + 1]];
			HHStaticEdge[] e = new HHStaticEdge[Math.max(endIdx - startIdx, 0)];
			for (int i = startIdx; i < endIdx; i++) {
				e[i - startIdx] = new HHStaticEdge(i);
			}
			return e;
		}

		/**
		 * @param hopIdx
		 *            index of the edge
		 * @return edge at index.
		 */
		public HHStaticEdge getAdjacentEdge(int hopIdx) {
			return new HHStaticEdge(vLvlFirstEdge[vFirstLvlVertex[id]] + hopIdx);
		}

		/**
		 * @return only level 0 outgoing forward edges.
		 */
		public HHStaticEdge[] getAdjacentLevel0Edges() {
			int startIdx = vLvlFirstEdge[vFirstLvlVertex[id]];
			int endIdx = vLvlFirstEdge[vFirstLvlVertex[id + 1]];
			int n = 0;
			for (int i = startIdx; i < endIdx; i++) {
				if (!eShortcut.get(i) && eDirection[FWD].get(i)) {
					n++;
				}
			}
			HHStaticEdge[] e = new HHStaticEdge[Math.max(n, 0)];
			int j = 0;
			for (int i = startIdx; i < endIdx; i++) {
				if (!eShortcut.get(i) && eDirection[FWD].get(i)) {
					e[j++] = new HHStaticEdge(i);
				}
			}
			return e;
		}

		/**
		 * @return size of the adjacency list.
		 */
		public int numAdjacentEdges() {
			return vLvlFirstEdge[vFirstLvlVertex[id + 1]] - vLvlFirstEdge[vFirstLvlVertex[id]];
		}

		/**
		 * @param lvl
		 *            vertex level.
		 * @return the neighborhood of this vertex within the given level.
		 */
		public int getNeighborhood(int lvl) {
			return vLvlVNh[vFirstLvlVertex[id] + lvl];
		}

		/**
		 * @return level of this vertex.
		 */
		public int getLevel() {
			return (vFirstLvlVertex[id + 1] - vFirstLvlVertex[id]) - 1;
		}
	}

	/**
	 * lighweight edge.
	 * 
	 */
	public class HHStaticEdge {

		private final int id;

		HHStaticEdge(int id) {
			this.id = id;
		}

		/**
		 * @return id of this edge
		 */
		public int getId() {
			return id;
		}

		/**
		 * @return source vertex
		 */
		public HHStaticVertex getSource() {
			return new HHStaticVertex(eSource[id]);
		}

		/**
		 * @return target vertex
		 */
		public HHStaticVertex getTarget() {
			return new HHStaticVertex(eTarget[id]);
		}

		/**
		 * @return weight
		 */
		public int getWeight() {
			return eWeight[id];
		}

		/**
		 * @param direction
		 *            forward or backward.
		 * @return if edge supports the given direction
		 */
		public boolean getDirection(int direction) {
			return eDirection[direction].get(id);
		}

		/**
		 * @param lvl
		 *            the reference level
		 * @return true if edge level is greater or equal to the given level.
		 */
		public boolean isLvlGEQ(int lvl) {
			return vLvlFirstEdge[vFirstLvlVertex[eSource[id]] + lvl] <= id;
		}

		/**
		 * @return true if edge is a shortcut.
		 */
		public boolean isShortcut() {
			return eShortcut.get(id);
		}

		/**
		 * @return the level of this edge.
		 */
		public int getLvl() {
			// no use in query algorithm, slow
			int lvl = 0;
			while (vLvlFirstEdge[vFirstLvlVertex[eSource[id]] + lvl] <= id) {
				lvl++;
			}
			return lvl - 1;
		}

		@Override
		public String toString() {
			return eSource[id] + " -> " + getTarget().getId();
		}

	}
}
