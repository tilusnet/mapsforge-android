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
package org.mapsforge.routing.android.hh;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import org.mapsforge.core.Rect;
import org.mapsforge.routing.preprocessing.hh.mobile.HHGlobals;

/**
 * This class implements a static rtree variant. It supports the overlaps query which is the key
 * to efficient nearest neighbor queries limited to a specific range.
 */
final class StaticRTree {
	/**
	 * The file where this rtree is stored.
	 */
	private final RandomAccessFile raf;
	/**
	 * Number of bytes per tree node (should be equal to the file system's block size)
	 */
	private final int blockSizeBytes;
	/**
	 * The start address of the rtree within the File.
	 */
	private final long startAddr;
	/**
	 * The root of the tree, always kept in main memory.
	 */
	private final RtreeNode root;
	/**
	 * Buffer for reading nodes.
	 */
	private final byte[] readBuff;
	/**
	 * The minimum bounding rectangle around all indexed data.
	 */
	private final Rect boundingBox;

	/** Size in bytes on secondary storage of this r-tree */
	private final int sizeBytes;

	/**
	 * Instantiate an RTree stored in a file.
	 * 
	 * @param file
	 *            File where the RTree is stored.
	 * @param startAddr
	 *            byte index of the 1st byte of the r-tree within the given file.
	 * @param endAddr
	 *            points to the first byte behind the r-tree.
	 * @throws IOException
	 *             if there was an error reading file, or the given location does not contain a
	 *             valid r-tree.
	 */
	public StaticRTree(File file, long startAddr, long endAddr) throws IOException {
		this.startAddr = startAddr;
		this.raf = new RandomAccessFile(file, "r");

		// read header
		raf.seek(startAddr);
		byte[] headerMagic = new byte[HHGlobals.STATIC_RTREE_HEADER_MAGIC.length];
		raf.readFully(headerMagic);
		for (int i = 0; i < headerMagic.length; i++) {
			if (headerMagic[i] != HHGlobals.STATIC_RTREE_HEADER_MAGIC[i]) {
				throw new IOException("Could not access RTree, invalid header.");
			}
		}
		this.blockSizeBytes = raf.readInt();
		this.readBuff = new byte[blockSizeBytes];
		this.root = readNode(1);

		// compute the minimum bounding rectangle around the root node
		int minLat = Integer.MAX_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int maxLon = Integer.MIN_VALUE;
		for (int i = 0; i < root.maxLatitudeE6.length; i++) {
			minLat = Math.min(minLat, root.minLatitudeE6[i]);
			minLon = Math.min(minLon, root.minLongitudeE6[i]);
			maxLat = Math.max(maxLat, root.maxLatitudeE6[i]);
			maxLon = Math.max(maxLon, root.maxLongitudeE6[i]);
		}
		this.boundingBox = new Rect(minLon, maxLon, minLat, maxLat);
		this.sizeBytes = (int) (endAddr - startAddr);
	}

	/**
	 * Gives the minimum bounding rectangle including all indexed data.
	 * 
	 * @return the minimum bounding rectangle around the root node.
	 */
	public Rect getBoundingBox() {
		return this.boundingBox;
	}

	/**
	 * Get integers values associated with the indexed rectangles overlapping the given
	 * rectangle.
	 * 
	 * @param r
	 *            the rectangle to be queried against.
	 * @return integers associated with all matching rectangles.
	 * @throws IOException
	 *             on error accessing file.
	 */
	public LinkedList<Integer> overlaps(Rect r) throws IOException {
		return overlaps(r.minLongitudeE6, r.maxLongitudeE6, r.minLatitudeE6, r.maxLatitudeE6);
	}

	/**
	 * @param minLongitudeE6
	 *            longitude bound of the rectangle.
	 * @param maxLongitudeE6
	 *            longitude bound of the rectangle.
	 * @param minLatitudeE6
	 *            latitude bound of the rectangle.
	 * @param maxLatitudeE6
	 *            latitude bound of the rectangle.
	 * @return integers associated with all matching rectangles.
	 * @throws IOException
	 *             on error accessing file.
	 */
	public LinkedList<Integer> overlaps(int minLongitudeE6, int maxLongitudeE6,
			int minLatitudeE6, int maxLatitudeE6)
			throws IOException {
		LinkedList<Integer> buff = new LinkedList<Integer>();
		overlapsRecursive(minLongitudeE6, maxLongitudeE6, minLatitudeE6,
				maxLatitudeE6, root,
				buff);
		return buff;
	}

	/**
	 * Recursive overlaps query implementation.
	 * 
	 * @param minLon
	 *            longitude bound of the rectangle.
	 * @param maxLon
	 *            longitude bound of the rectangle.
	 * @param minLat
	 *            latitude bound of the rectangle.
	 * @param maxLat
	 *            latitude bound of the rectangle.
	 * @param node
	 *            the current node to be processed.
	 * @param buff
	 *            the result is added to this list.
	 * @return number of disc reads.
	 * @throws IOException
	 *             on error accessing file.
	 */
	private int overlapsRecursive(int minLon, int maxLon, int minLat, int maxLat,
			RtreeNode node,
			LinkedList<Integer> buff) throws IOException {
		int discReads = 0;
		for (int i = 0; i < node.minLongitudeE6.length; i++) {
			boolean overlaps = Rect.overlaps(node.minLongitudeE6[i], node.maxLongitudeE6[i],
					node.minLatitudeE6[i],
					node.maxLatitudeE6[i], minLon, maxLon, minLat, maxLat);
			if (overlaps) {
				if (node.isLeaf) {
					buff.add(node.pointer[i]);
				} else {
					RtreeNode child = readNode(node.pointer[i]);
					discReads++;
					discReads += overlapsRecursive(minLon, maxLon, minLat, maxLat, child, buff);
				}
			}
		}
		return discReads;
	}

	/**
	 * Reads a node from secondary storage.
	 * 
	 * @param id
	 *            The id of the given node (root node has id 1).
	 * @return the desired node representation.
	 * @throws IOException
	 *             on error accessing file.
	 */
	private RtreeNode readNode(int id) throws IOException {
		raf.seek(startAddr + (blockSizeBytes * id));
		raf.readFully(readBuff);
		return new RtreeNode(readBuff);
	}

	public int getSizeBytes() {
		return sizeBytes;
	}

	public int getNumNodes() {
		return (sizeBytes / blockSizeBytes) - 1;
	}

	public int getBlockSizeBytes() {
		return blockSizeBytes;
	}

	public int getBranchingFactor() {
		return (blockSizeBytes - 3) / 20;
	}

	/**
	 * A node representation used only during runtime, not during r-tree construction.
	 */
	private static class RtreeNode {
		/**
		 * True if this node is a leaf node.
		 */
		final boolean isLeaf;
		/**
		 * The minimum longitude in micro degrees of the i-th rectangle.
		 */
		final int[] minLongitudeE6;
		/**
		 * The maximum longitude in micro degrees of the i-th rectangle.
		 */
		final int[] maxLongitudeE6;
		/**
		 * The minimum latitude in micro degrees of the i-th rectangle.
		 */
		final int[] minLatitudeE6;
		/**
		 * The maximum latitude in micro degrees of the i-th rectangle.
		 */
		final int[] maxLatitudeE6;
		/**
		 * The pointer of the i-th rectangle. If this node is a leaf, the pointer identifies the
		 * satellite data. If this node is a inner node, the pointer points to the x-th node of
		 * the file.
		 */
		final int[] pointer;

		/**
		 * Constructs a Tree Node based on the given data representing the tree node.
		 * 
		 * @param b
		 *            data where the tree node is stored.
		 * @throws IOException
		 *             on error accessing file.
		 */
		public RtreeNode(byte[] b) throws IOException {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));

			this.isLeaf = stream.readBoolean();
			short numEntries = stream.readShort();
			this.minLongitudeE6 = new int[numEntries];
			this.maxLongitudeE6 = new int[numEntries];
			this.minLatitudeE6 = new int[numEntries];
			this.maxLatitudeE6 = new int[numEntries];
			this.pointer = new int[numEntries];

			for (int i = 0; i < numEntries; i++) {
				this.minLongitudeE6[i] = stream.readInt();
				this.maxLongitudeE6[i] = stream.readInt();
				this.minLatitudeE6[i] = stream.readInt();
				this.maxLatitudeE6[i] = stream.readInt();
				this.pointer[i] = stream.readInt();
			}
		}
	}

}
