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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.hadoop.util.IndexedSortable;
import org.apache.hadoop.util.QuickSort;

/**
 * This class implements a static r-tree for indexing two dimensional rectangles. Packing is
 * done using the 'SortTileRecursive' (STR) algorithm which gives an optimal space utilization
 * and local clustering. Data can be retrieved by an overlaps query against a given rectangle.
 */
class StaticRTreeWriter {

	/**
	 * The number of byte required for storing a single node entry.
	 */
	private static final int BYTES_PER_ENTRY = 20;
	/**
	 * The header magic bytes written at the start of the header of this r-tree. This is used
	 * for checking if the file is likely contain a valid r-tree representation.
	 */
	private static final byte[] HEADER_MAGIC = HHGlobals.STATIC_RTREE_HEADER_MAGIC;

	/**
	 * SortTileRecursive(STR) Algorithm for static r-tree packing. All given arrays must be of
	 * equal length.
	 * 
	 * @param minLon
	 *            longitude bound of the rectangle.
	 * @param maxLon
	 *            longitude bound of the rectangle.
	 * @param minLat
	 *            latitude bound of the rectangle.
	 * @param maxLat
	 *            latitude bound of the rectangle.
	 * @param pointer
	 *            each rectangle is associated with one pointer.
	 * @param blockSizeBytes
	 *            number of bytes per node (should be equal to file system block size).
	 * @param targetFile
	 *            the file the r-tree will be stored to.
	 * @throws IOException
	 *             on error accessing file.
	 */
	public static void packSortTileRecursive(final int[] minLon, final int[] maxLon,
			final int[] minLat, final int[] maxLat, final int[] pointer, int blockSizeBytes,
			File targetFile) throws IOException {
		IndexSortableNodeEntries rectangles = new IndexSortableNodeEntries(minLon, maxLon,
				minLat, maxLat, pointer);
		RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
		packSTR(rectangles, blockSizeBytes, raf, 1, true);

		// fill the last node with zeroes if necessary
		long length = raf.length();
		if (length % blockSizeBytes != 0) {
			long newLength = length + blockSizeBytes - (length % blockSizeBytes);
			raf.setLength(newLength);
		}

		raf.close();
	}

	/**
	 * Recursive STR packing, each recursion processes one level of the tree.
	 * 
	 * @param rectangles
	 *            rectangles to be indexed on the current level.
	 * @param blockSizeBytes
	 *            number of bytes per node.
	 * @param raf
	 *            used to write the r-tree to.
	 * @param nodeId
	 *            next node id to be assigned. (should always be 1).
	 * @param isLeaf
	 *            true if the current level is a leaf level.
	 * @throws IOException
	 *             on error accessing file.
	 */
	private static void packSTR(IndexSortableNodeEntries rectangles, int blockSizeBytes,
			RandomAccessFile raf, int nodeId, boolean isLeaf) throws IOException {
		int _nodeId = nodeId;
		// number of rectangles
		int r = rectangles.size();
		// rectangles per block
		int b = (blockSizeBytes - 3) / BYTES_PER_ENTRY;
		// number of nodes
		int n = (int) Math.ceil(((double) r) / ((double) b));
		// number of vertical slices
		int s = (int) Math.ceil(Math.sqrt(n));

		// rectangles for recursive call
		IndexSortableNodeEntries parentEntries = new IndexSortableNodeEntries(new int[n],
				new int[n], new int[n], new int[n], new int[n]);
		int parentEntryIdx = 0;

		// sort all rectangles by x-coordinate (longitude)
		QuickSort quicksort = new QuickSort();
		rectangles.setSortByLongitude(true);
		quicksort.sort(rectangles, 0, r);

		// partition into s slices
		rectangles.setSortByLongitude(false);
		for (int i = 0; i < rectangles.size(); i++) {

			// start of slice, sort slice by y-coordinate (latitude)
			if (i % (s * b) == 0) {
				int sliceStart = i;
				int sliceEnd = Math.min(i + (s * b), rectangles.size());
				quicksort.sort(rectangles, sliceStart, sliceEnd);
			}

			// start of node, write node header, get bounding rectangle
			if (i % b == 0) {
				// end of node?
				if (i > 0) {
					parentEntryIdx++;
				}
				// set nodeId of current node
				if (n > 1) {
					_nodeId++;
				} else {
					// put root into block number one (number 0 is header)
					_nodeId = 1;
				}

				int nodeStart = i;
				int nodeEnd = nodeStart + Math.min(b, rectangles.size() - i);

				raf.seek(_nodeId * blockSizeBytes);
				raf.writeBoolean(isLeaf);
				raf.writeShort(nodeEnd - nodeStart);
				parentEntries.minLon[parentEntryIdx] = rectangles.minLon[i];
				parentEntries.maxLon[parentEntryIdx] = rectangles.maxLon[i];
				parentEntries.minLat[parentEntryIdx] = rectangles.minLat[i];
				parentEntries.maxLat[parentEntryIdx] = rectangles.maxLat[i];
				parentEntries.pointer[parentEntryIdx] = _nodeId;
			}

			// write node entry
			raf.writeInt(rectangles.minLon[i]);
			raf.writeInt(rectangles.maxLon[i]);
			raf.writeInt(rectangles.minLat[i]);
			raf.writeInt(rectangles.maxLat[i]);
			raf.writeInt(rectangles.pointer[i]);

			// update bounding rectangle
			parentEntries.minLon[parentEntryIdx] = Math.min(
					parentEntries.minLon[parentEntryIdx], rectangles.minLon[i]);
			parentEntries.maxLon[parentEntryIdx] = Math.max(
					parentEntries.maxLon[parentEntryIdx], rectangles.maxLon[i]);
			parentEntries.minLat[parentEntryIdx] = Math.min(
					parentEntries.minLat[parentEntryIdx], rectangles.minLat[i]);
			parentEntries.maxLat[parentEntryIdx] = Math.max(
					parentEntries.maxLat[parentEntryIdx], rectangles.maxLat[i]);
		}

		// update centers of rectangles for recursive call
		parentEntries.updateCenters();

		// recursively pack next level of the tree
		if (n > 1) {
			packSTR(parentEntries, blockSizeBytes, raf, _nodeId, false);
		} else {
			// write header
			raf.seek(0);
			raf.write(HEADER_MAGIC);
			raf.writeInt(blockSizeBytes);
		}
	}

	/**
	 * The QuickSort algorithm used requires to implement the IndexedSortable interface. This
	 * class exists only for this purpose, and is used during recursive tree construction for
	 * holding all entries of the current tree level.
	 */
	private static class IndexSortableNodeEntries implements IndexedSortable {

		final int[] minLon, maxLon, minLat, maxLat, pointer, centerLon, centerLat;
		private boolean sortByLongitude;

		/**
		 * All given arrays must be of same length.
		 * 
		 * @param minLon
		 *            rectangle bound.
		 * @param maxLon
		 *            rectangle bound.
		 * @param minLat
		 *            rectangle bound.
		 * @param maxLat
		 *            rectangle bound.
		 * @param pointer
		 *            integer associated with the rectangle.
		 */
		public IndexSortableNodeEntries(int[] minLon, int[] maxLon, int[] minLat, int[] maxLat,
				int[] pointer) {
			// check input
			int n = minLon.length;
			if (n != maxLon.length || n != minLat.length || n != maxLat.length
					|| n != pointer.length) {
				throw new IllegalArgumentException("Arrays must have equal length");
			}

			// initialize
			this.minLon = minLon;
			this.maxLon = maxLon;
			this.minLat = minLat;
			this.maxLat = maxLat;
			this.pointer = pointer;

			// initialize centers
			this.centerLon = new int[n];
			this.centerLat = new int[n];
			updateCenters();

			// sorting dimension
			this.sortByLongitude = true;
		}

		/**
		 * Set to true for sorting by comparing the longitude. Set to false for sorting by
		 * comparing latitude.
		 * 
		 * @param b
		 *            switch.
		 */
		void setSortByLongitude(boolean b) {
			this.sortByLongitude = b;
		}

		@Override
		public void swap(int i, int j) {
			swap(minLon, i, j);
			swap(maxLon, i, j);
			swap(minLat, i, j);
			swap(maxLat, i, j);
			swap(pointer, i, j);
			swap(centerLon, i, j);
			swap(centerLat, i, j);
		}

		private void swap(int[] arr, int i, int j) {
			int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}

		@Override
		public int compare(int i, int j) {
			if (sortByLongitude) {
				return centerLon[i] - centerLon[j];
			}
			return centerLat[i] - centerLat[j];
		}

		/**
		 * Set the centers of the given rectangles based on their bounding coordinates.
		 */
		public void updateCenters() {
			for (int i = 0; i < centerLon.length; i++) {
				centerLon[i] = (minLon[i] + maxLon[i]) / 2;
				centerLat[i] = (minLat[i] + maxLat[i]) / 2;
			}
		}

		/**
		 * @return number of rectangles.
		 */
		public int size() {
			return minLon.length;
		}
	}
}
