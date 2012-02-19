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

import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDynamicGraph.HHDynamicVertex;

/**
 * computes vertex neighborhoods.
 */
final class ThreadedNeighborhoodComputation extends ComputationThread {

	private final Iterator<HHDynamicVertex> vertices;
	private final int lvl, h;
	private volatile int progress;

	private ThreadedNeighborhoodComputation(Iterator<HHDynamicVertex> vertices, int lvl, int h) {
		this.vertices = vertices;
		this.lvl = lvl;
		this.h = h;
	}

	public static void computeNeighborhoods(HHDynamicGraph graph, int lvl, int h, int numThreads) {
		Iterator<HHDynamicVertex> iter = graph.getVertices(lvl);

		ThreadedNeighborhoodComputation threads[] = new ThreadedNeighborhoodComputation[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new ThreadedNeighborhoodComputation(iter, lvl, h);
		}
		ThreadedComputation.executeThreads(threads, graph.numVertices(lvl),
				"computeNeighborhoods(lvl=" + lvl + ")");
	}

	@Override
	public void run() {
		while (vertices.hasNext()) {
			HHDynamicVertex v = vertices.next();
			if (v != null) {
				v.setNeighborhood(DijkstraAlgorithm.shortestDistance(v, h, true, true, lvl),
						lvl);
				progress++;
			}
		}
	}

	@Override
	public int progress() {
		return progress;
	}
}
