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

import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicEdge;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicVertex;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArraySynchronized;

/**
 * computes the highway network out of a core.
 */
final class ThreadedHighwayNetworkComputation extends ComputationThread {

	private final int lvl;
	private final boolean fwd;
	private final Iterator<HHDynamicVertex> iter;
	private final BitArraySynchronized buffer;
	private int progress;

	private ThreadedHighwayNetworkComputation(Iterator<HHDynamicVertex> iter,
			BitArraySynchronized buffer, int lvl, boolean fwd) {
		this.lvl = lvl;
		this.iter = iter;
		this.buffer = buffer;
		this.fwd = fwd;
		this.progress = 0;
	}

	public static void computeHighwayNetwork(HHDynamicGraph graph, int lvl,
			BitArraySynchronized buffer, int numThreads, boolean fwd) {
		Iterator<HHDynamicVertex> iter = graph.getVertices(lvl);
		ThreadedHighwayNetworkComputation[] threads = new ThreadedHighwayNetworkComputation[numThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ThreadedHighwayNetworkComputation(iter, buffer, lvl, fwd);
		}
		String desc = "computeHighwayNetwork(lvl=" + lvl + ",";
		if (fwd) {
			desc += "fwd)";
		} else {
			desc += "bwd)";
		}
		ThreadedComputation.executeThreads(threads, graph.numVertices(lvl), desc);
	}

	@Override
	public void run() {
		while (iter.hasNext()) {
			HHDynamicVertex v = iter.next();
			if (v != null) {
				LinkedList<HHDynamicEdge> edges = DijkstraAlgorithm.selectHighwayEdges(v, fwd,
						!fwd, lvl);
				for (HHDynamicEdge e : edges) {
					buffer.set(e.getId());
				}
				progress++;
			}
		}
	}

	@Override
	public int progress() {
		return progress;
	}
}
