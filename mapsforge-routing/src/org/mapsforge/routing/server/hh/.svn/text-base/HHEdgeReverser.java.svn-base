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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.UnsignedFourBitArray;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticEdge;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticVertex;

/**
 * Hash index for finding reverse edges. This is usefull for all bidirected dijkstra
 * derivatives, to quickly determine the forward edge for a given backward edge.
 * 
 * 
 */
class HHEdgeReverser implements Serializable {

	private static final long serialVersionUID = 5882576822167830309L;

	private final static int ESCAPE_VALUE = 15;

	private final TIntIntHashMap map;
	private final UnsignedFourBitArray hopIndices;

	public HHEdgeReverser(HHStaticGraph graph) {
		this.map = new TIntIntHashMap();
		this.hopIndices = new UnsignedFourBitArray(graph.numEdges());
		for (int i = 0; i < graph.numVertices(); i++) {
			HHStaticVertex v = graph.getVertex(i);
			for (int j = 0; j < v.numAdjacentEdges(); j++) {
				HHStaticEdge e = v.getAdjacentEdge(j);
				if (!e.isShortcut()) {
					HHStaticVertex t = e.getTarget();
					int hopIdx = getHopIdx(t, v);
					if (hopIdx == -1) {
						System.out.println("error");
					} else if (hopIdx >= ESCAPE_VALUE) {
						map.put(e.getId(), hopIdx);
						hopIndices.set(e.getId(), ESCAPE_VALUE);
					} else {
						hopIndices.set(e.getId(), hopIdx);
					}
				} else {
					hopIndices.set(e.getId(), ESCAPE_VALUE);
				}
			}
		}
	}

	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	public static HHEdgeReverser deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (HHEdgeReverser) Serializer.deserialize(iStream);
	}

	private int getHopIdx(HHStaticVertex s, HHStaticVertex t) {
		for (int hopIdx = 0; hopIdx < s.numAdjacentEdges(); hopIdx++) {
			HHStaticEdge e = s.getAdjacentEdge(hopIdx);
			if (e.getTarget().getId() == t.getId() && !e.isShortcut()) {
				if (e.isShortcut()) {
					System.out.println("error in edge reverser");
				}
				return hopIdx;
			}
		}
		return -1;
	}

	/**
	 * Reverses order of edges and reverses direction of edges. Result is added to the end of
	 * buff.
	 * 
	 * @param edges
	 *            edges to reverse.
	 * @param buff
	 *            buffer to add reversed edges to.
	 */
	public void reverseEdges(LinkedList<HHStaticEdge> edges, LinkedList<HHStaticEdge> buff) {
		for (Iterator<HHStaticEdge> iter = edges.descendingIterator(); iter.hasNext();) {
			HHStaticEdge e = iter.next();
			int hopIdx = hopIndices.get(e.getId());
			if (hopIdx == ESCAPE_VALUE) {
				hopIdx = map.get(e.getId());
			}
			HHStaticEdge e_ = e.getTarget().getAdjacentEdge(hopIdx);
			buff.addLast(e_);
		}
	}

}
