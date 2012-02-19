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

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.DijkstraAlgorithm;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedByteArray;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedByteArrayGrowable;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedFourBitArray;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedFourBitArrayGrowable;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticEdge;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticVertex;

/**
 * Maps shortcuts to low level edges by a multi leveled index.
 * 
 * 
 */
class HHEdgeExpanderRecursive implements Serializable {

	private static final long serialVersionUID = 5266959681778938376L;

	private final static int ESCAPE_VALUE_OFFSET = 255;
	private final static int ESCAPE_VALUE_HOPS = 15;

	private final UnsignedFourBitArray hops;
	private final UnsignedByteArray offsets;
	private final TIntLongHashMap vertexMap;
	private final TIntIntHashMap edgeMap;

	private HHEdgeExpanderRecursive(UnsignedFourBitArray hops, UnsignedByteArray offsets,
			TIntLongHashMap vertexMap, TIntIntHashMap edgeMap) {
		this.hops = hops;
		this.offsets = offsets;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	public static HHEdgeExpanderRecursive deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (HHEdgeExpanderRecursive) Serializer.deserialize(iStream);
	}

	public void expandShortestPath(LinkedList<HHStaticEdge> edges, LinkedList<HHStaticEdge> buff) {
		for (HHStaticEdge e : edges) {
			expandEdge(e, buff);
		}
		for (HHStaticEdge e : buff) {
			if (e.isShortcut()) {
				System.out.println("error in expander");
			}
		}
	}

	public void expandEdge(HHStaticEdge e, LinkedList<HHStaticEdge> buff) {
		if (e.isShortcut()) {
			HHStaticVertex s = e.getSource();
			HHStaticVertex t = e.getTarget();

			long val = vertexMap.get(s.getId());
			int vHopsIdx = decodeVertexHopsIdx(val);
			int vOffsetsIdx = decodeVertexOffsetsIdx(val);
			int offset = offsets.get(vOffsetsIdx + (e.getId() - s.getAdjacentEdge(0).getId()));
			if (offset == ESCAPE_VALUE_OFFSET) {
				offset = edgeMap.get(e.getId());
			}
			int hopsOffset = vHopsIdx + offset;

			HHStaticEdge e_;
			HHStaticVertex s_ = s;
			HHStaticVertex t_ = s_;
			int hopIdx;
			while (t_.getId() != t.getId()) {
				s_ = t_;

				// get next hop-idx
				hopIdx = hops.get(hopsOffset);
				hopsOffset++;
				if (hopIdx == ESCAPE_VALUE_HOPS) {
					hopIdx = hops.get(hopsOffset);
					hopIdx = (hopIdx << 4) | hops.get(hopsOffset + 1);
					hopIdx = (hopIdx << 4) | hops.get(hopsOffset + 2);
					hopIdx = (hopIdx << 4) | hops.get(hopsOffset + 3);
					hopsOffset += 4;
				}

				// get edge by hop-idx
				e_ = s_.getAdjacentEdge(hopIdx);
				if (e_.isShortcut()) {
					expandEdge(e_, buff);
				} else {
					buff.addLast(e_);
				}
				t_ = e_.getTarget();
			}
		} else {
			buff.addLast(e);
		}
	}

	public static HHEdgeExpanderRecursive createIndex(HHStaticGraph graph, int[] eMinLvl) {
		final int CHUNK_SIZE = 1000000;
		UnsignedByteArrayGrowable offsets = new UnsignedByteArrayGrowable(CHUNK_SIZE);
		UnsignedFourBitArrayGrowable hops = new UnsignedFourBitArrayGrowable(CHUNK_SIZE);

		TIntLongHashMap vertexMap = new TIntLongHashMap();
		TIntIntHashMap edgeMap = new TIntIntHashMap();

		for (int i = 0; i < graph.numVertices(); i++) {
			HHStaticVertex v = graph.getVertex(i);
			if (!hasAdjacentShortCuts(v)) {
				continue;
			}
			int vOffsetIdx = offsets.size();
			int vHopsIdx = hops.size();
			vertexMap.put(v.getId(), encode(vHopsIdx, vOffsetIdx));
			for (HHStaticEdge e : v.getAdjacentEdges(0)) {
				// add edge offset
				int offset = hops.size() - vHopsIdx;
				if (offset < ESCAPE_VALUE_OFFSET) {
					offsets.add(offset);
				} else {
					edgeMap.put(e.getId(), offset);
					offsets.add(ESCAPE_VALUE_OFFSET);
				}

				// add edge hops
				boolean fwd = e.getDirection(HHStaticGraph.FWD);
				boolean bwd = e.getDirection(HHStaticGraph.BWD);
				if (fwd && bwd) {
					bwd = false;
				}
				int searchLvl = Math.max(0, eMinLvl[e.getId()] - 1);
				LinkedList<Integer> hopsIndices = DijkstraAlgorithm.shortestPathHopIndices(
						e.getSource(), e.getTarget(), fwd, bwd, searchLvl, eMinLvl);
				for (int hopIdx : hopsIndices) {
					if (hopIdx < ESCAPE_VALUE_HOPS) {
						hops.add(hopIdx);
					} else {
						hops.add(ESCAPE_VALUE_HOPS);
						// store 16 bit hop idx (0..65535) highest bits first
						hops.add((hopIdx & 0x0000f000) >> 12);
						hops.add((hopIdx & 0x00000f00) >> 8);
						hops.add((hopIdx & 0x000000f0) >> 4);
						hops.add((hopIdx & 0x0000000f));
					}
				}
			}
		}
		UnsignedByteArray offsets_ = new UnsignedByteArray(offsets.size());
		for (int i = 0; i < offsets.size(); i++) {
			offsets_.set(i, offsets.get(i));
		}
		UnsignedFourBitArray hops_ = new UnsignedFourBitArray(hops.size());
		for (int i = 0; i < hops.size(); i++) {
			hops_.set(i, hops.get(i));
		}
		return new HHEdgeExpanderRecursive(hops_, offsets_, vertexMap, edgeMap);
	}

	private static boolean hasAdjacentShortCuts(HHStaticVertex v) {
		for (HHStaticEdge e : v.getAdjacentEdges(0)) {
			if (e.isShortcut())
				return true;
		}
		return false;
	}

	private static long encode(int offsetHops, int offsetOffsets) {
		return (((long) offsetHops) << 32) | offsetOffsets;
	}

	private static int decodeVertexHopsIdx(long l) {
		return (int) (l >>> 32);
	}

	private static int decodeVertexOffsetsIdx(long l) {
		return (int) (l & 0x00000000ffffffff);
	}

	public static int[] getEMinLvl(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);
		int i = 0;
		int[] eMinLvl = new int[reader.numEdges()];
		for (Iterator<HHEdge> iter = reader.getEdges(); iter.hasNext();) {
			eMinLvl[i++] = iter.next().minLvl;
		}
		return eMinLvl;
	}
}
