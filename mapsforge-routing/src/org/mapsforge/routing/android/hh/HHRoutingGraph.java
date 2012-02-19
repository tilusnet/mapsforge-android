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
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.core.WGS84;
import org.mapsforge.routing.preprocessing.hh.mobile.HHGlobals;

/**
 * This class implements the routing graph, the highway hierarchies algorithm directly works on. Thus it
 * holds all information required by the algorithm. This class internally uses object pooling, for
 * recycling objects, the vertices and edges can be released back to the pool by using this classes'
 * release methods. The graph is blocked an stored on secondary storage. Due to limited memory this
 * graph uses a cache of specified size exploit locality of access patterns. In Addition to the standard
 * graph functionality this graph also provides functionality to find an entry point to the graph in
 * terms of nearest neighbor queries. Furthermore some satellite data e.g. edge names are stored.
 */
class HHRoutingGraph {
	/** no hop indices, edges are expanded using dijkstra */
	public static final int HOP_INDICES_NONE = 0;
	/** recursive mapping to the respective subjacent layer of the edge */
	public static final int HOP_INDICES_RECURSIVE = 1;
	/** direct mapping to level 0 */
	public static final int HOP_INDICES_DIRECT = 2;

	/**
	 * header magic of the highway hierarchies binary file. Used for checking if the file might be o.k.
	 */
	private static final byte[] BINARY_FILE_HEADER_MAGIC = HHGlobals.BINARY_FILE_HEADER_MAGIC;
	/**
	 * header length of the highway hierarchies binary file.
	 */
	private static final int BINARY_FILE_HEADER_LENGTH = HHGlobals.BINARY_FILE_HEADER_LENGTH;
	/**
	 * header length of the cluster blocks section of the file.
	 */
	private static final int CLUSTER_BLOCKS_HEADER_LENGTH = HHGlobals.CLUSTER_BLOCKS_HEADER_LENGTH;
	/**
	 * the initial size of the vertex pool.
	 */
	private static final int INITIAL_POOL_SIZE_VERTICES = 1;
	/**
	 * the initial size of the edge pool.
	 */
	private static final int INITIAL_POOL_SIZE_EDGES = 1;
	/**
	 * the initial size of the Offset pool.
	 */
	private static final int INITIAL_POOL_SIZE_OFFSETS = 1;

	private static final int getBitmask(int shiftClusterId) {
		int bMask = 0;
		for (int i = 0; i < shiftClusterId; i++) {
			bMask = (bMask << 1) | 1;
		}
		return bMask;
	}

	/**
	 * This r-tree indexes minimum bounding rectangles of each block.
	 */
	final StaticRTree blockIndex;
	/**
	 * This address lookup table holds pointers which can be looked up by the block id.
	 */
	final AddressLookupTable blockAddressTable;
	/**
	 * The Cache which hopefully holds the near future most valuable blocks.
	 */
	private final Cache<Block> blockCache;
	/**
	 * Object pool for the vertices.
	 */
	final ObjectPool<HHVertex> poolVertices;
	/**
	 * Object pool for the edges.
	 */
	final ObjectPool<HHEdge> poolEdges;
	/**
	 * Object pool for Offsets.
	 */
	final ObjectPool<Offset> poolOffsets;

	/**
	 * The highway hierarchies binary file.
	 */
	private final RandomAccessFile raf;
	/**
	 * The address of the first cluster block of the graph (the one with id 0).
	 */
	private final long startAddrClusterBlocks;
	/**
	 * This bitmask is used to efficiently compute the vertex offset within the block from a given
	 * vertex id.
	 */
	private final int bitMask;
	/**
	 * number of levels this multileveled graph has.
	 */
	private final byte numLevels;
	/**
	 * number of bits used for encoding block identifiers.
	 */
	final byte bitsPerBlockId;
	/**
	 * number of bits used for encode vertex offsets within the block.
	 */
	final byte bitsPerVertexOffset;
	/**
	 * number of bits used for encoding edge weights.
	 */
	final byte bitsPerEdgeWeight;
	/**
	 * number of bits used for encoding street type.
	 */
	final byte bitsPerStreetType;
	/**
	 * true if the graph stores hop indices, which can be used to expand shortcuts recursively without
	 * using dijkstra's algorithm.
	 */
	final boolean hasShortcutHopIndices;
	/**
	 * osm street types.
	 */
	final String[] streetTypes;

	/**
	 * @param hhBinaryFile
	 *            the highway hierarchies binary file.
	 * @param cacheSizeBytes
	 *            threshold for the cache size.
	 * @throws IOException
	 *             on error reading the file.
	 */
	public HHRoutingGraph(File hhBinaryFile, int cacheSizeBytes) throws IOException {
		// FETCH THE HEADER OF THE BINARY FILE
		this.raf = new RandomAccessFile(hhBinaryFile, "r");
		byte[] header = new byte[BINARY_FILE_HEADER_LENGTH];
		raf.seek(0);
		raf.readFully(header);

		// READ THE HEADER OF THE BINARY FILE
		DataInputStream iStream = new DataInputStream(new ByteArrayInputStream(header));

		// verify header magic
		byte[] headerMagic = new byte[BINARY_FILE_HEADER_MAGIC.length];
		iStream.read(headerMagic);
		for (int i = 0; i < headerMagic.length; i++) {
			if (headerMagic[i] != BINARY_FILE_HEADER_MAGIC[i]) {
				throw new IOException("invalid header.");
			}
		}

		long startAddrGraph = iStream.readLong();
		/* long endAddrGraph = */iStream.readLong();
		long startAddrBlockIndex = iStream.readLong();
		long endAddrBlockIndex = iStream.readLong();
		long startAddrRTree = iStream.readLong();
		long endAddrRTree = iStream.readLong();

		// READ THE HEADER OF THE CLUSTER BLOCKS SECTION
		raf.seek(startAddrGraph);
		header = new byte[CLUSTER_BLOCKS_HEADER_LENGTH];
		raf.readFully(header);

		iStream = new DataInputStream(new ByteArrayInputStream(header));
		this.numLevels = iStream.readByte();
		this.bitsPerBlockId = iStream.readByte();
		this.bitsPerVertexOffset = iStream.readByte();
		this.bitsPerEdgeWeight = iStream.readByte();
		this.bitsPerStreetType = iStream.readByte();
		this.hasShortcutHopIndices = iStream.readBoolean();

		int numStreetTypes = iStream.readByte();
		this.streetTypes = new String[numStreetTypes];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numStreetTypes; i++) {
			byte b = iStream.readByte();
			while (b != (byte) 0) {
				sb.append((char) b);
				b = iStream.readByte();
			}
			if (sb.length() > 0) {
				streetTypes[i] = sb.toString();
			} else {
				streetTypes[i] = null;
			}
			sb.setLength(0);
		}

		iStream.close();
		this.bitMask = getBitmask(bitsPerVertexOffset);
		this.startAddrClusterBlocks = startAddrGraph + CLUSTER_BLOCKS_HEADER_LENGTH;

		// INITIALIZE COMPONENTS
		this.blockIndex = new StaticRTree(hhBinaryFile, startAddrRTree, endAddrRTree);
		this.blockAddressTable = new AddressLookupTable(startAddrBlockIndex, endAddrBlockIndex,
				hhBinaryFile);
		this.blockCache = new LRUCache<Block>(cacheSizeBytes);
		this.poolVertices = new ObjectPool<HHVertex>(
				new ObjectPool.PoolableFactory<HHVertex>() {

					@Override
					public HHVertex makeObject() {
						return new HHVertex();
					}
				}, INITIAL_POOL_SIZE_VERTICES);
		this.poolEdges = new ObjectPool<HHEdge>(
				new ObjectPool.PoolableFactory<HHEdge>() {

					@Override
					public HHEdge makeObject() {
						return new HHEdge();
					}
				}, INITIAL_POOL_SIZE_EDGES);

		this.poolOffsets = new ObjectPool<Offset>(
				new ObjectPool.PoolableFactory<Offset>() {
					@Override
					public Offset makeObject() {
						return new Offset();
					}
				}, INITIAL_POOL_SIZE_OFFSETS);
	}

	/**
	 * @return Returns true if this graph stores hop indices.
	 */
	public boolean hasShortcutHopIndices() {
		return hasShortcutHopIndices;
	}

	/**
	 * Clears the cluster blocks cache.
	 */
	public void clearCache() {
		blockCache.clear();
	}

	public int getCacheSizeBytes() {
		return blockCache.maxSizeBytes();
	}

	/**
	 * @return Returns the minimum bounding rectangle around all vertices and edges.
	 */
	public Rect getBoundingBox() {
		return blockIndex.getBoundingBox();
	}

	/**
	 * @return Returns the number of levels this multi leveled graph consists of.
	 */
	public int getNumLevels() {
		return numLevels;
	}

	/**
	 * @return Returns the number of blocks this graph is partitioned in.
	 */
	public int getNumBlocks() {
		return blockAddressTable.size();
	}

	/**
	 * Computes the vertexId from a blockId and a vertexOffset.
	 * 
	 * @param blockId
	 *            the block the vertex belongs to.
	 * @param vertexOffset
	 *            the offset within the block the vertex is stored at.
	 * @return Returns the id of the specified vertex, it is not verified if the id is valid!
	 */
	public int getVertexId(int blockId, int vertexOffset) {
		return (blockId << bitsPerVertexOffset) | vertexOffset;
	}

	/**
	 * Extracts the blockId from a given vertex id.
	 * 
	 * @param vertexId
	 *            not verified.
	 * @return Returns the block identifier, the given vertex belongs to.
	 */
	public int getBlockId(int vertexId) {
		return vertexId >>> bitsPerVertexOffset;
	}

	/**
	 * Extracts the offset of the vertex within its block.
	 * 
	 * @param vertexId
	 *            not verified.
	 * @return the offset within the block.
	 */
	public int getVertexOffset(int vertexId) {
		return vertexId & bitMask;
	}

	/**
	 * Looks up a vertex by id. The vertex id must be valid!
	 * 
	 * @param vertexId
	 *            identifies the vertex to be looked up.
	 * @return The specified vertex.
	 * @throws IOException
	 *             on error reading file.
	 */
	public HHVertex getVertex(int vertexId) throws IOException {
		int blockId = getBlockId(vertexId);
		Block block = getBlock(blockId);
		int vertexOffset = getVertexOffset(vertexId);
		return block.getVertex(vertexOffset);
	}

	/**
	 * Nearest neighbor query bounded by the given range.
	 * 
	 * @param c
	 *            the point of reference.
	 * @param maxDistanceMeters
	 *            examine at least all points up to this distance.
	 * @return the vertex nearest to c.
	 * @throws IOException
	 *             on error reading file.
	 */
	public HHVertex getNearestVertex(GeoCoordinate c, double maxDistanceMeters)
			throws IOException {
		double alphaLon = (maxDistanceMeters * 180)
				/ (Math.cos(Math.toRadians(c.getLatitude())) * WGS84.EQUATORIALRADIUS * Math.PI);
		double alphaLat = (maxDistanceMeters * 180) / (WGS84.EQUATORIALRADIUS * Math.PI);
		int minLon = GeoCoordinate.doubleToInt(c.getLongitude() - alphaLon);
		int maxLon = GeoCoordinate.doubleToInt(c.getLongitude() + alphaLon);
		int minLat = GeoCoordinate.doubleToInt(c.getLatitude() - alphaLat);
		int maxLat = GeoCoordinate.doubleToInt(c.getLatitude() + alphaLat);
		LinkedList<Integer> blockIds = blockIndex.overlaps(minLon, maxLon, minLat, maxLat);
		double dBest = Double.MAX_VALUE;
		HHVertex vBest = null;
		for (int blockId : blockIds) {
			Block block = getBlock(blockId);
			int n = block.getNumVertices();
			for (int i = 0; i < n; i++) {
				HHVertex v = block.getVertex(i);
				double distance = GeoCoordinate.sphericalDistance(c.getLatitudeE6(), c
						.getLongitudeE6(), v.latitudeE6, v.longitudeE6);
				if (dBest > distance) {
					dBest = distance;
					releaseVertex(vBest);
					vBest = v;
				} else {
					releaseVertex(v);
				}
			}
		}
		return vBest;
	}

	/**
	 * Collects all vertices within the specified range. If this functionality is really required, this
	 * should be implemented by a lazy iterator like the ones in database systems. Currently this
	 * suffices since the method was only used during testing.
	 * 
	 * @param bbox
	 *            the range to search for vertices.
	 * @return Returns all vertices within the specified range.
	 * @throws IOException
	 *             on error reading file.
	 */
	public LinkedList<HHVertex> getVerticesWithinBBox(final Rect bbox) throws IOException {
		// this should be implemented as lazy iterator!
		LinkedList<Integer> blockIds = blockIndex.overlaps(bbox);
		LinkedList<HHVertex> result = new LinkedList<HHVertex>();
		for (int blockId : blockIds) {

			Block block = getBlock(blockId);
			int n = block.getNumVertices();
			for (int i = 0; i < n; i++) {
				HHVertex currentVertex = block.getVertex(i);
				if (bbox.includes(currentVertex.latitudeE6, currentVertex.longitudeE6)) {
					result.add(currentVertex);
				}
			}
		}

		return result;
	}

	/**
	 * Lookup the outgoing adjacency list of the given vertex.
	 * 
	 * @param vertex
	 *            must not be null.
	 * @return all outgoing edges of the given vertex.
	 * @throws IOException
	 *             on error reading file
	 */
	public HHEdge[] getOutboundEdges(HHVertex vertex) throws IOException {
		int blockId = getBlockId(vertex.vertexIds[vertex.vertexIds.length - 2]);
		Block block = getBlock(blockId);
		return block.getOutboundEdges(vertex);
	}

	/**
	 * Release a vertex and make it available for recycling.
	 * 
	 * @param vertex
	 *            the vertex to be released.
	 */
	public void releaseVertex(HHVertex vertex) {
		poolVertices.release(vertex);
	}

	/**
	 * Release a edge and make it available for recycling.
	 * 
	 * @param edge
	 *            the edge to be released.
	 */
	public void releaseEdge(HHEdge edge) {
		poolEdges.release(edge);
	}

	/**
	 * Gives the desired block, it is taken either from cache or from secondary storage.
	 * 
	 * @param blockId
	 *            the identifier of the desired block, must be valid!
	 * @return Returns the desired block.
	 * @throws IOException
	 *             on error reading file
	 */
	Block getBlock(int blockId) throws IOException {
		Block block = blockCache.getItem(blockId);
		if (block == null) {
			block = readBlock(blockId);
			blockCache.putItem(block);
		}
		return block;
	}

	/**
	 * Reads a block form secondary storage.
	 * 
	 * @param blockId
	 *            the identifier of the desired block. Must be valid!
	 * @return the desired block.
	 * @throws IOException
	 *             on error reading file.
	 */
	private Block readBlock(int blockId) throws IOException {
		Pointer pointer = blockAddressTable.getPointer(blockId);
		if (pointer != null) {
			long startAddr = startAddrClusterBlocks + pointer.startAddr;
			// need to read 4 bytes to much since the Deserializer requires that.
			int nBytes = pointer.lengthBytes + 4;

			raf.seek(startAddr);
			byte[] buff = new byte[nBytes];
			raf.readFully(buff);

			Block block = new Block(blockId, buff, this);
			return block;
		}
		return null;
	}

	public LinkedList<HHEdge> getCachedEdges(int level) {
		LinkedList<HHEdge> l = new LinkedList<HHEdge>();
		for (int i = 0; i < blockAddressTable.size(); i++) {
			Block b = blockCache.getItem(i);
			if (b != null && b.getLevel() == level) {
				for (int j = 0; j < b.getNumVertices(); j++) {
					HHVertex v = b.getVertex(j);
					HHEdge[] e = b.getOutboundEdges(v);
					for (int k = 0; k < e.length; k++) {
						l.add(e[k]);
					}
				}
			}
		}
		return l;
	}

	@Override
	public String toString() {
		DecimalFormat df1 = new DecimalFormat("#.#");
		StringBuilder sb = new StringBuilder();
		sb.append("--- HHRoutingGraph ---\n\n");

		sb.append("AddressLookupTable\n");
		sb.append("  size = " + blockAddressTable.byteSize() + " bytes\n");
		sb.append("  #entries = " + blockAddressTable.size() + "\n");
		sb.append("  bits / entry = "
				+ df1.format(((double) blockAddressTable.byteSize() * 8) / blockAddressTable
						.size()) + "\n");
		sb.append("\n");

		sb.append("StaticRTree\n");
		sb.append("  size = " + blockIndex.getSizeBytes() + " bytes\n");
		sb.append("  #nodes = " + blockIndex.getNumNodes() + "\n");
		sb.append("  block size = " + blockIndex.getBlockSizeBytes() + " bytes\n");
		sb.append("  branching factor = " + blockIndex.getBranchingFactor() + "\n");
		sb.append("\n");

		sb.append("Blocks\n");

		int[] levelBytes = new int[numLevels];
		int[] levelNodes = new int[numLevels];
		int[] levelEdges = new int[numLevels];
		int[] levelNumBlocks = new int[numLevels];

		for (int blockId = 0; blockId < blockAddressTable.size(); blockId++) {
			try {
				Block b = readBlock(blockId);
				levelBytes[b.getLevel()] += blockAddressTable.getPointer(blockId).lengthBytes;
				levelNodes[b.getLevel()] += b.getNumVertices();
				levelNumBlocks[b.getLevel()]++;
				for (int j = 0; j < b.getNumVertices(); j++) {
					HHVertex v = b.getVertex(j);
					levelEdges[b.getLevel()] += b.getOutboundEdges(v).length;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < levelBytes.length; i++) {
			sb.append("  level-"
					+ i
					+ " = "
					+ levelBytes[i]
					+ " bytes, "
					+ levelNodes[i]
					+ " nodes, "
					+ levelEdges[i]
					+ " edges, "
					+ df1
							.format(((double) levelBytes[i] * 8)
									/ (levelNodes[i] + levelEdges[i]))
					+ " bits/entry, "
					+ " avg Block Size = "
					+ (levelBytes[i] / Math.max(1, (levelNodes[i] / 75))) + "\n"
					);

		}

		return sb.toString();
	}
}
