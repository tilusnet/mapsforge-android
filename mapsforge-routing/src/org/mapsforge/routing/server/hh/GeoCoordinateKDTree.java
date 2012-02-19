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

import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Random;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.hh.dao.RgDAO;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHVertex;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.DBConnection;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.Serializer;
import org.mapsforge.routing.preprocessing.hh.model.RgVertex;

class GeoCoordinateKDTree implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int START_DIM = 0;

	private final int[][] coords;
	private final int[] ind;
	private final Random rnd;

	private int minLongitude = Integer.MAX_VALUE;
	private int maxLongitude = Integer.MIN_VALUE;
	private int minLatitude = Integer.MAX_VALUE;
	private int maxLatitude = Integer.MIN_VALUE;

	public GeoCoordinateKDTree(int[] lon, int[] lat) {
		coords = new int[][] { lon, lat };
		rnd = new Random();
		ind = new int[lon.length];
		for (int i = 0; i < ind.length; i++) {
			ind[i] = i;
		}
		construct(0, lon.length - 1, START_DIM);

		// compute bounding rectangle
		for (int i = 0; i < coords[0].length; i++) {
			minLongitude = Math.min(minLongitude, coords[0][i]);
			maxLongitude = Math.max(maxLongitude, coords[0][i]);
			minLatitude = Math.min(minLatitude, coords[1][i]);
			maxLatitude = Math.max(maxLatitude, coords[1][i]);
		}
	}

	public void serialize(OutputStream oStream) throws IOException {
		Serializer.serialize(oStream, this);
	}

	public static GeoCoordinateKDTree deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		return (GeoCoordinateKDTree) Serializer.deserialize(iStream);
	}

	public int getMinLongitude() {
		return minLongitude;
	}

	public int getMaxLongitude() {
		return maxLongitude;
	}

	public int getMinLatitude() {
		return minLatitude;
	}

	public int getMaxLatitude() {
		return maxLatitude;
	}

	public int getNearestNeighborIdx(int lon, int lat) {
		return ind[nearestNeighbor(new int[] { lon, lat }, 0, coords[0].length - 1, START_DIM,
				null)];
	}

	public TIntArrayList getIndicesByBoundingBox(int minLon, int minLat, int maxLon, int maxLat) {
		TIntArrayList buff = new TIntArrayList();
		getIndicesByBoundingBox(new int[] { minLon, minLat }, new int[] { maxLon, maxLat }, 0,
				coords[0].length - 1, START_DIM, buff);
		return buff;
	}

	public GeoCoordinate getCoordinate(int idx) {
		return new GeoCoordinate(coords[1][idx], coords[0][idx]);
	}

	public int size() {
		return ind.length;
	}

	private void getIndicesByBoundingBox(int[] minCoord, int[] maxCoord, int p, int r, int dim,
			TIntArrayList buff) {
		if (p > r) {
			return;
		}
		// c is index of split axis or the coordinate compared in current recursion step
		int c = (p + r) / 2;

		boolean left = coords[dim][ind[c]] >= minCoord[dim];
		boolean right = coords[dim][ind[c]] <= maxCoord[dim];

		if (left && right && coords[(dim + 1) % 2][ind[c]] >= minCoord[(dim + 1) % 2]
				&& coords[(dim + 1) % 2][ind[c]] <= maxCoord[(dim + 1) % 2]) {
			buff.add(ind[c]);
		}

		if (left) {
			getIndicesByBoundingBox(minCoord, maxCoord, p, c - 1, (dim + 1) % 2, buff);
		}
		if (right) {
			getIndicesByBoundingBox(minCoord, maxCoord, c + 1, r, (dim + 1) % 2, buff);
		}
	}

	private int nearestNeighbor(int[] coord, int p, int r, int dim, Integer best) {
		Integer best_ = best;
		// ...warning crap

		if (p > r) {
			return best_;
		}
		// c is index of split axis or the coordinate compared in current recursion step
		int c = (p + r) / 2;
		if (best_ == null) {
			best_ = c;
		}
		// calculate distance to nearest (best) coordinate found so far
		double dBest = GeoCoordinate.sphericalDistance(coord[0], coord[1],
				coords[0][ind[best_]], coords[1][ind[best_]]);
		double dCurrent = GeoCoordinate.sphericalDistance(coord[0], coord[1],
				coords[0][ind[c]], coords[1][ind[c]]);

		if (dCurrent < dBest) {
			best_ = c;
		}
		// always search the containing branch
		if (coord[dim] < coords[dim][ind[c]]) {
			best_ = nearestNeighbor(coord, p, c - 1, (dim + 1) % 2, best_);
		} else if (coord[dim] > coords[dim][ind[c]]) {
			best_ = nearestNeighbor(coord, c + 1, r, (dim + 1) % 2, best_);
		} else {
			int best1 = nearestNeighbor(coord, p, c - 1, (dim + 1) % 2, best_);
			int best2 = nearestNeighbor(coord, c + 1, r, (dim + 1) % 2, best_);
			if (best1 < best2) {
				best_ = best1;
			} else {
				best_ = best2;
			}
		}
		// sometimes search the other branch

		// calculate distance to nearest (best) coordinate found so far
		dBest = GeoCoordinate.sphericalDistance(coord[0], coord[1], coords[0][ind[best_]],
				coords[1][ind[best_]]);

		// calculate distance to split axis
		double dAxis;
		if (dim == 0) {
			dAxis = GeoCoordinate.sphericalDistance(coords[0][ind[c]], coord[1], coord[0],
					coord[1]);
		} else {
			dAxis = GeoCoordinate.sphericalDistance(coord[0], coords[1][ind[c]], coord[0],
					coord[1]);
		}
		// search the other branch if necessary
		if (dAxis < dBest) {
			if (coord[dim] > coords[dim][ind[c]]) {
				best_ = nearestNeighbor(coord, p, c - 1, (dim + 1) % 2, best_);
			} else if (coord[dim] < coords[dim][ind[c]]) {
				best_ = nearestNeighbor(coord, c + 1, r, (dim + 1) % 2, best_);
			}
		}
		return best_;
	}

	private void construct(int p, int r, int dim) {
		if (p < r) {
			quicksort(p, r, dim);
			int c = (p + r) / 2;
			construct(p, c - 1, (dim + 1) % 2);
			construct(c + 1, r, (dim + 1) % 2);
		}
	}

	private void quicksort(int p, int r, int dim) {
		if (p < r) {
			int q = partition(p, r, dim);
			quicksort(p, q - 1, dim);
			quicksort(q + 1, r, dim);
		}
	}

	private int partition(int p, int r, int dim) {
		swap(r, rnd.nextInt(r - p + 1) + p);
		int pivot = coords[dim][ind[r]];
		int i = p - 1;
		for (int j = p; j < r; j++) {
			if (coords[dim][ind[j]] <= pivot) {
				i++;
				swap(i, j);
			}
		}
		swap(i + 1, r);
		return i + 1;
	}

	private void swap(int i, int j) {
		int tmp = ind[i];
		ind[i] = ind[j];
		ind[j] = tmp;
	}

	public static GeoCoordinateKDTree buildHHVertexIndex(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);
		int[] lon = new int[reader.numVertices()];
		int[] lat = new int[reader.numVertices()];

		for (Iterator<HHVertex> iter = reader.getVertices(); iter.hasNext();) {
			HHVertex v = iter.next();
			lon[v.id] = GeoCoordinate.doubleToInt(v.longitude);
			lat[v.id] = GeoCoordinate.doubleToInt(v.latitude);
		}

		GeoCoordinateKDTree index = new GeoCoordinateKDTree(lon, lat);
		return index;
	}

	public static void main(String[] args) throws SQLException {

		System.out.println("read coords from db");
		RgDAO rg = new RgDAO(DBConnection.getJdbcConnectionPg("localhost", 5432, "berlin",
				"postgres", "admin"));
		int n = rg.getNumVertices();
		int[] lon = new int[n];
		int[] lat = new int[n];

		int i = 0;
		for (Iterator<RgVertex> iter = rg.getVertices().iterator(); iter.hasNext();) {
			RgVertex v = iter.next();
			lon[i] = GeoCoordinate.doubleToInt(v.getLongitude());
			lat[i++] = GeoCoordinate.doubleToInt(v.getLatitude());
		}
		GeoCoordinateKDTree tree = new GeoCoordinateKDTree(lon, lat);

		System.out.println("test nn");
		Random rnd = new Random();
		long startTime = System.currentTimeMillis();
		for (int j = 0; j < 1000; j++) {
			int idx = rnd.nextInt(lon.length);
			int nn = tree.getNearestNeighborIdx(lon[idx], lat[idx]);
			System.out.println(idx + " " + nn);
		}
		long time = System.currentTimeMillis() - startTime;
		System.out.println(time + "ms");

		System.out.println(tree.getNearestNeighborIdx(GeoCoordinate.doubleToInt(13.468122),
				GeoCoordinate.doubleToInt(52.505740)));
	}
}
