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

import java.io.Serializable;
import java.util.Date;

/**
 * immutable class holding information about the hierarchy.
 */
public class HHGraphProperties implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * date of creation
	 */
	public final Date creationDate;
	/**
	 * the transport string specified during preprocessing.
	 */
	public final String transport;
	/**
	 * dijkstra rank for computing neighborhoods.
	 */
	public final int h;
	/**
	 * hh preprocessing param, recursion anchor on number of vertices.
	 */
	public final int vertexThreshold;
	/**
	 * maximum number of edes per shortcut.
	 */
	public final int hopLimit;
	/**
	 * number of threads used for preprocessing.
	 */
	public final int numThreads;
	/**
	 * the contraction factor.
	 */
	public final double c;
	/**
	 * time in minutes needed for computing the hierarchy.
	 */
	public final double compTimeMinutes;
	/**
	 * true if edges leaving the core were downgraded to the next lower level.
	 */
	public final boolean downgradedEdges;
	/**
	 * per level info about the graph.
	 */
	public final HHLevelStats[] levelStats;

	HHGraphProperties(Date creationDate, String transport, int h, int vertexThreshold,
			int hopLimit, int numThreads, double c, double compTimeMinutes,
			boolean downgradedEdges, HHLevelStats[] levelStats) {
		this.creationDate = creationDate;
		this.transport = transport;
		this.h = h;
		this.vertexThreshold = vertexThreshold;
		this.hopLimit = hopLimit;
		this.numThreads = numThreads;
		this.c = c;
		this.compTimeMinutes = compTimeMinutes;
		this.downgradedEdges = downgradedEdges;
		this.levelStats = levelStats;
	}

	@Override
	public String toString() {
		String str = "createdOn = " + creationDate.toString() + "\n" + "transport = '"
				+ transport + "'\n" + "h = " + h + "\n" + "c = " + c + "\n" + "hopLimit = "
				+ hopLimit + "\n" + "downgradedEdges = " + downgradedEdges + "\n"
				+ "vertexThreshold = " + vertexThreshold + "\n" + "numThreads = " + numThreads
				+ "\n" + "compTime = " + compTimeMinutes + " minutes \n";
		for (HHLevelStats ls : levelStats) {
			str += ls.toString() + "\n";
		}
		return str;
	}

	/**
	 * immutable per level info about the graph
	 */
	public static class HHLevelStats implements Serializable {

		private static final long serialVersionUID = -6115864997731495133L;
		/**
		 * the level this class is about.
		 */
		public final int lvl;
		/**
		 * number of edges in the level.
		 */
		public final int numEdges;
		/**
		 * number of vertices in the level.
		 */
		public final int numVertices;
		/**
		 * number of core edges in the level.
		 */
		public final int numCoreEdges;
		/**
		 * number of core vertices in the level.
		 */
		public final int numCoreVertices;

		HHLevelStats(int lvl, int numEdges, int numVertices, int numCoreEdges,
				int numCoreVertices) {
			this.lvl = lvl;
			this.numEdges = numEdges;
			this.numVertices = numVertices;
			this.numCoreEdges = numCoreEdges;
			this.numCoreVertices = numCoreVertices;
		}

		@Override
		public String toString() {
			return "G" + lvl + "  : |V|=" + numVertices + " |E|=" + numEdges + "\n" + "G" + lvl
					+ "' : |V|=" + numCoreVertices + " |E|=" + numCoreEdges;
		}
	}
}
