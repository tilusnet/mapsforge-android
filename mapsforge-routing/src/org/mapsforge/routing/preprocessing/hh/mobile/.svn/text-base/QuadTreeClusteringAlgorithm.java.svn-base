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
package org.mapsforge.routing.preprocessing.hh.mobile;

import java.util.Iterator;

import org.apache.hadoop.util.IndexedSortable;
import org.apache.hadoop.util.QuickSort;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.hh.mobile.QuadTreeClustering.QuadTreeCluster;

/**
 * This class clusters graph implementing this packages graph interface. The well known quad
 * tree algorithm splits rectangles into four quadrants until the recursion anchor is reached,
 * that is to say the rectangles are small enough. There are different heuristics to choose the
 * point of reference. Three of them are implemented which differ only a little.
 */
class QuadTreeClusteringAlgorithm {

	/**
	 * This heuristic chooses the center of the rectangle as split coordinate.
	 */
	public static final int HEURISTIC_CENTER = 0;
	/**
	 * This heuristic chooses the medians of latitudes and longitudes as split coordinate.
	 */
	public static final int HEURISTIC_MEDIAN = 1;

	/**
	 * This heuristic chooses the average latitude and average longitude as split coordinate.
	 */
	public static final int HEURISTIC_AVERAGE = 2;
	/**
	 * The default heuristic, used if an invalid heuristic is specified.
	 */
	private static final int HEURISTIC_DEFAULT = HEURISTIC_MEDIAN;
	/**
	 * Maps heuristic identifiers to heuristic names, used for console output.
	 */
	private static final String[] HEURISTIC_NAMES = new String[] { "center", "median",
			"average" };
	/**
	 * A nice quicksort implementation.
	 */
	private static final QuickSort quicksort = new QuickSort();

	/**
	 * Computes the quad tree clustering.
	 * 
	 * @param graph
	 *            a set of graphs, here each graph is a level of a multileveled graph.
	 * @param longitudeE6
	 *            the longitudes of all vertices of the graph.
	 * @param latitudeE6
	 *            the latitudes of all vertices of the graph.
	 * @param heuristik
	 *            have a look at the public static variables.
	 * @param threshold
	 *            limit on number of vertices, defining the recursion anchor.
	 * @return the clustering of all levels.
	 * @throws IllegalArgumentException
	 *             if parameters are wrong.
	 */
	public static QuadTreeClustering[] computeClustering(Graph[] graph, int[] longitudeE6,
			int[] latitudeE6,
			int heuristik, int threshold) throws IllegalArgumentException {
		QuadTreeClustering[] clustering = new QuadTreeClustering[graph.length];
		for (int i = 0; i < graph.length; i++) {
			clustering[i] = computeClustering(graph[i], longitudeE6, latitudeE6, heuristik,
					threshold,
					graph[0].numVertices());
		}
		return clustering;
	}

	/**
	 * Compute the clustering for a single graph.
	 * 
	 * @param graph
	 *            the graph to be clustered.
	 * @param longitudeE6
	 *            the longitudes of all vertices of the graph.
	 * @param latitudeE6
	 *            the latitudes of all vertices of the graph.
	 * @param heuristik
	 *            have a look at the public static variables.
	 * @param threshold
	 *            limit on number of vertices, defining the recursion anchor.
	 * @param numVerticesLvlZero
	 *            number of vertices in level 0
	 * @return the clustering of the graph.
	 * @throws IllegalArgumentException
	 *             if parameters are wrong.
	 */
	public static QuadTreeClustering computeClustering(Graph graph, int[] longitudeE6,
			int[] latitudeE6,
			int heuristik, int threshold, int numVerticesLvlZero)
			throws IllegalArgumentException {
		System.out.println("computing quad-clustering (|V|=" + graph.numVertices()
				+ ", threshold=" + threshold + ", heuristic=" + HEURISTIC_NAMES[heuristik]
				+ ")");
		int[] vertexId = new int[graph.numVertices()];
		int[] lon_ = new int[graph.numVertices()];
		int[] lat_ = new int[graph.numVertices()];
		int i = 0;
		for (Iterator<? extends Vertex> iter = graph.getVertices(); iter.hasNext();) {
			Vertex v = iter.next();
			vertexId[i] = v.getId();
			lon_[i] = longitudeE6[v.getId()];
			lat_[i] = latitudeE6[v.getId()];
			i++;
		}

		// subdivide
		QuadTreeClustering clustering = new QuadTreeClustering(numVerticesLvlZero);
		subdivide(vertexId, lon_, lat_, 0, vertexId.length, heuristik, clustering, threshold);

		return clustering;
	}

	/**
	 * Recursive subdivision of rectangles.
	 * 
	 * @param vertexId
	 *            the ids of the vertices.
	 * @param longitudeE6
	 *            the longitudes of the vertices.
	 * @param latitudeE6
	 *            the latitudes of the vertices.
	 * @param l
	 *            the left boundary of the range to be regarded. (array index)
	 * @param r
	 *            the right boundary of the range to be regarded. (array index)
	 * @param heuristic
	 *            split heuristic.
	 * @param clustering
	 *            the clustering which will be modified.
	 * @param threshold
	 *            recursive anchor, threshold oon number of vertices.
	 */
	private static void subdivide(int vertexId[], int longitudeE6[], int latitudeE6[], int l,
			int r,
			int heuristic, QuadTreeClustering clustering, int threshold) {
		if ((r - l) > threshold) {
			// subdivide
			GeoCoordinate splitCoord = getSplitCoordinate(vertexId, longitudeE6, latitudeE6, l,
					r, heuristic);
			int splitLon = splitCoord.getLongitudeE6();
			int splitLat = splitCoord.getLatitudeE6();
			SortableVertices s = new SortableVertices(vertexId, longitudeE6, latitudeE6);

			// 1st quadrant
			int j = l - 1;
			for (int i = j + 1; i < r; i++) {
				if (latitudeE6[i] >= splitLat && longitudeE6[i] <= splitLon) {
					s.swap(i, ++j);
				}
			}
			int l_ = l;
			int r_ = j + 1;
			subdivide(vertexId, longitudeE6, latitudeE6, l_, r_, heuristic, clustering,
					threshold);

			// 2nd quadrant
			for (int i = j + 1; i < r; i++) {
				if (latitudeE6[i] >= splitLat && longitudeE6[i] > splitLon) {
					s.swap(i, ++j);
				}
			}
			l_ = r_;
			r_ = j + 1;
			subdivide(vertexId, longitudeE6, latitudeE6, l_, r_, heuristic, clustering,
					threshold);

			// 3rd quadrant
			for (int i = j + 1; i < r; i++) {
				if (latitudeE6[i] < splitLat && longitudeE6[i] <= splitLon) {
					s.swap(i, ++j);
				}
			}
			l_ = r_;
			r_ = j + 1;
			subdivide(vertexId, longitudeE6, latitudeE6, l_, r_, heuristic, clustering,
					threshold);

			// 4rd quadrant
			for (int i = j + 1; i < r; i++) {
				if (latitudeE6[i] < splitLat && longitudeE6[i] > splitLon) {
					s.swap(i, ++j);
				}
			}
			l_ = r_;
			r_ = j + 1;
			subdivide(vertexId, longitudeE6, latitudeE6, l_, r_, heuristic, clustering,
					threshold);

		} else {
			// recursive anchor - no subdivision - create new cluster if cluster is not empty
			if (r > l) {
				QuadTreeCluster cluster = clustering.addCluster();
				for (int i = l; i < r; i++) {
					cluster.addVertex(vertexId[i]);
				}
			}
		}
	}

	/**
	 * Gives the coordinate to split at, depending oon the given heuristic.
	 * 
	 * @param vertexId
	 *            ids of all vertices.
	 * @param longitudeE6
	 *            latitudes of all vertices.
	 * @param latitudeE6
	 *            latitudes of all vertices.
	 * @param l
	 *            the left boundary of the range to be regarded. (array index)
	 * @param r
	 *            the right boundary of the range to be regarded. (array index)
	 * @param heuristic
	 *            split heuristic.
	 * @return the coordinate to split at.
	 */
	private static GeoCoordinate getSplitCoordinate(int vertexId[], int longitudeE6[],
			int latitudeE6[],
			int l, int r, int heuristic) {
		switch (heuristic) {
			case HEURISTIC_CENTER:
				return getCenterCoordinate(longitudeE6, latitudeE6, l, r);
			case HEURISTIC_MEDIAN:
				return getMedianCoordinate(vertexId, longitudeE6, latitudeE6, l, r);
			case HEURISTIC_AVERAGE:
				return getAverageCoordinate(longitudeE6, latitudeE6, l, r);
			default:
				return getSplitCoordinate(vertexId, longitudeE6, latitudeE6, l, r,
						HEURISTIC_DEFAULT);
		}
	}

	/**
	 * Gives the split coordinate for the center heuristic
	 * 
	 * @param longitudeE6
	 *            the longitudes of the vertices.
	 * @param latitudeE6
	 *            the latitudes of the vertices.
	 * @param l
	 *            the left boundary of the range to be regarded. (array index)
	 * @param r
	 *            the right boundary of the range to be regarded. (array index)
	 * @return Returns the split coordinate.
	 */
	private static GeoCoordinate getCenterCoordinate(int longitudeE6[], int latitudeE6[],
			int l, int r) {
		long minLongitude = Integer.MAX_VALUE;
		long minLatitude = Integer.MAX_VALUE;
		long maxLongitude = Integer.MIN_VALUE;
		long maxLatitude = Integer.MIN_VALUE;

		for (int i = l; i < r; i++) {
			minLongitude = Math.min(longitudeE6[i], minLongitude);
			minLatitude = Math.min(latitudeE6[i], minLatitude);
			maxLongitude = Math.max(longitudeE6[i], maxLongitude);
			maxLatitude = Math.max(latitudeE6[i], maxLatitude);
		}

		int longitude = (int) ((minLongitude + maxLongitude) / 2);
		int latitude = (int) ((minLatitude + maxLatitude) / 2);

		return new GeoCoordinate(latitude, longitude);
	}

	/**
	 * Gives the split coordinate for the average heuristic
	 * 
	 * @param longitudeE6
	 *            the longitudes of the vertices.
	 * @param latitudeE6
	 *            the latitudes of the vertices.
	 * @param l
	 *            the left boundary of the range to be regarded. (array index)
	 * @param r
	 *            the right boundary of the range to be regarded. (array index)
	 * @return Returns the split coordinate.
	 */
	private static GeoCoordinate getAverageCoordinate(int longitudeE6[], int latitudeE6[],
			int l, int r) {
		double sumLon = 0d;
		double sumLat = 0d;

		for (int i = l; i < r; i++) {
			sumLon += GeoCoordinate.intToDouble(longitudeE6[i]);
			sumLat += GeoCoordinate.intToDouble(latitudeE6[i]);
		}

		double longitude = sumLon / (r - l);
		double latitude = sumLat / (r - l);

		return new GeoCoordinate(latitude, longitude);
	}

	/**
	 * Gives the split coordinate for the median heuristic
	 * 
	 * @param vertexId
	 *            ids of all vertices.
	 * @param longitudeE6
	 *            the longitudes of the vertices.
	 * @param latitudeE6
	 *            the latitudes of the vertices.
	 * @param l
	 *            the left boundary of the range to be regarded. (array index)
	 * @param r
	 *            the right boundary of the range to be regarded. (array index)
	 * @return Returns the split coordinate.
	 */
	private static GeoCoordinate getMedianCoordinate(int[] vertexId, int longitudeE6[],
			int latitudeE6[],
			int l, int r) {
		SortableVertices s = new SortableVertices(vertexId, longitudeE6, latitudeE6);
		int medianIdx = l + ((r - l) / 2);

		s.setSortByLongitude();
		quicksort.sort(s, l, r);
		int longitude = longitudeE6[medianIdx];

		s.setSortByLattitude();
		quicksort.sort(s, l, r);
		int latitude = latitudeE6[medianIdx];

		return new GeoCoordinate(latitude, longitude);
	}

	/**
	 * Need to implement this interface for using the quicksort algorithm.
	 */
	private static class SortableVertices implements IndexedSortable {

		/**
		 * holds vertex ids, longitudes and latitudes.
		 */
		private final int[][] data;
		private int sortDim;

		public SortableVertices(int[] vertexId, int[] longitudeE6, int[] latitudeE6) {
			this.data = new int[][] { vertexId, longitudeE6, latitudeE6 };
			sortDim = 0;
		}

		/**
		 * sets the sort dimension to longitude.
		 */
		public void setSortByLongitude() {
			sortDim = 1;
		}

		/**
		 * sets the sort dimension to longitude.
		 */
		public void setSortByLattitude() {
			sortDim = 2;
		}

		@Override
		public int compare(int i, int j) {
			return data[sortDim][i] - data[sortDim][j];
		}

		@Override
		public void swap(int i, int j) {
			for (int d = 0; d < data.length; d++) {
				int tmp = data[d][i];
				data[d][i] = data[d][j];
				data[d][j] = tmp;
			}
		}
	}
}
