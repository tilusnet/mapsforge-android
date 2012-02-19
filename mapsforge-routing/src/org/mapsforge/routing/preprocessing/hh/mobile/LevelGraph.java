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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHEdgeLvl;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHVertex;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.HHDbReader.HHVertexLvl;
import org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays.BitArray;

/**
 * Multileveled array based graph implementation. For computing the binary file, it is necessary
 * to have the whole data in main memory. This graph loads the complete highway hierarchies
 * database and thus provides fast random access to the data.
 */
class LevelGraph implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * index to the eDirection array.
	 */
	private static final int FWD = 0;
	/**
	 * index to the eDirection array.
	 */
	private static final int BWD = 1;
	/**
	 * first level vertex entry of each vertex.
	 */
	final int[] vFirstLvlVertex;
	/**
	 * level vertex neighborhood entries.
	 */
	final int[] vLvlVNh;
	/**
	 * points to the index of the first outgoing edge of a level vertex.
	 */
	final int[] vLvlFirstEdge;
	/**
	 * the longitudes of each vertex in micro degrees.
	 */
	final int[] vLon;
	/**
	 * the latitudes of each vertex in micro degrees.
	 */
	final int[] vLat;
	/**
	 * the source id of each edge.
	 */
	final int[] eSource;
	/**
	 * the target id of each edge.
	 */
	final int[] eTarget;
	/**
	 * the weight of each edge.
	 */
	final int[] eWeight;
	/**
	 * the minimum level the edges belong to.
	 */
	final int[] eMinLvl;
	/**
	 * edge direction, if the bit of the direction is that the edge is a forward / backward
	 * edge.
	 */
	final BitArray[] eDirection;
	/**
	 * set to true if the edge is a shortcut.
	 */
	final BitArray eIsShortcut;
	/**
	 * edge names.
	 */
	final String[] eName;
	/**
	 * edge refs (names)
	 */
	final String[] eRef;
	/**
	 * set if edge belongs to a roundabout.
	 */
	final BitArray eRoundabout;
	/**
	 * the way points .
	 */
	final int[][] eLatitudesE6;
	/**
	 * the way points .
	 */
	final int[][] eLongitudesE6;
	/**
	 * osm street type, mapped by the hashmaps of this class
	 */
	final byte[] eOsmStreetType;
	/**
	 * number of levels this graph has.
	 */
	private final int numLevels;
	/**
	 * number of vertices in this graph.
	 */
	final int numVertices;
	/**
	 * number of level vertices in this graph.
	 */
	private final int numLvlVertices;
	/**
	 * number of edges in this graph.
	 */
	private final int numEdges;
	/**
	 * One graph instance for each level.
	 */
	private final Level[] levels;
	/**
	 * use to get an int for storing
	 */
	final HashMap<String, Byte> osmStreetTypeToByte;
	/**
	 * 
	 */
	final HashMap<Byte, String> byteToOsmStreetType;

	/**
	 * Constructs a graph by loading data from a database. the schema must conform to the
	 * highway hierarchies schema and the osm2rg schema.
	 * 
	 * @param conn
	 *            database to be read from.
	 * @throws SQLException
	 *             on error reading database.
	 */
	public LevelGraph(Connection conn) throws SQLException {
		HHDbReader reader = new HHDbReader(conn);

		// initialize counts
		this.numLevels = reader.numLevels();
		this.numVertices = reader.numVertices();
		this.numLvlVertices = reader.numLevelVertices();
		int sum = 0;
		for (int i = 0; i < numLevels; i++) {
			sum += reader.numEdges(i);
		}
		this.numEdges = sum;

		// initialize arrays
		vFirstLvlVertex = new int[numVertices + 1];
		vFirstLvlVertex[numVertices] = numLvlVertices;
		vLon = new int[numVertices];
		vLat = new int[numVertices];
		vLvlVNh = new int[numLvlVertices];
		vLvlFirstEdge = new int[numLvlVertices + 1];
		vLvlFirstEdge[numLvlVertices] = numEdges;
		eSource = new int[numEdges];
		eTarget = new int[numEdges];
		eWeight = new int[numEdges];
		eMinLvl = new int[numEdges];
		eDirection = new BitArray[] { new BitArray(numEdges), new BitArray(numEdges) };
		eIsShortcut = new BitArray(numEdges);
		eName = new String[numEdges];
		eRef = new String[numEdges];
		eOsmStreetType = new byte[numEdges];
		eRoundabout = new BitArray(numEdges);
		eLatitudesE6 = new int[numEdges][];
		eLongitudesE6 = new int[numEdges][];
		levels = new Level[numLevels];

		osmStreetTypeToByte = new HashMap<String, Byte>();
		byteToOsmStreetType = new HashMap<Byte, String>();

		// copy data to arrays

		// vLon + vLat
		int offset = 0;
		for (Iterator<HHVertex> iter = reader.getVertices(); iter.hasNext();) {
			HHVertex v = iter.next();
			vLon[offset] = GeoCoordinate.doubleToInt(v.longitude);
			vLat[offset] = GeoCoordinate.doubleToInt(v.latitude);
			offset++;
		}

		// vLvlVNh + vFirstLvlVertex
		offset = 0;
		for (Iterator<HHVertexLvl> iter = reader.getVertexLvls(); iter.hasNext();) {
			HHVertexLvl v = iter.next();
			vLvlVNh[offset] = v.neighborhood;
			if (v.lvl == 0) {
				vFirstLvlVertex[v.id] = offset;
			}
			offset++;
		}
		// vLvlFirstEdge + eSource + eTarget + eWeight + eDirection
		for (int i = 0; i < numLvlVertices; i++) {
			vLvlFirstEdge[i] = -1;
		}

		offset = 0;
		for (Iterator<HHEdgeLvl> iter = reader.getEdgesLvl(); iter.hasNext();) {
			HHEdgeLvl e = iter.next();
			if (!osmStreetTypeToByte.containsKey(e.osmStreetType)) {
				osmStreetTypeToByte.put(e.osmStreetType, (byte) osmStreetTypeToByte.size());
				byteToOsmStreetType.put((byte) osmStreetTypeToByte.size(), e.osmStreetType);
			}

			eSource[offset] = e.sourceId;
			eTarget[offset] = e.targetId;
			eWeight[offset] = e.weight;
			eMinLvl[offset] = e.minLvl;
			eDirection[FWD].set(offset, e.fwd);
			eDirection[BWD].set(offset, e.bwd);
			eIsShortcut.set(offset, e.minLvl > 0);
			eName[offset] = e.name;
			eRef[offset] = e.ref;
			eOsmStreetType[offset] = osmStreetTypeToByte.get(e.osmStreetType);
			eRoundabout.set(offset, e.isRoundabout);
			eLatitudesE6[offset] = toE6Waypoints(e.latitudes);
			eLongitudesE6[offset] = toE6Waypoints(e.longitudes);
			if (e.isReversed) {
				reverseInplace(eLatitudesE6[offset]);
				reverseInplace(eLongitudesE6[offset]);
			}

			if (vLvlFirstEdge[vFirstLvlVertex[e.sourceId] + e.lvl] == -1) {
				vLvlFirstEdge[vFirstLvlVertex[e.sourceId] + e.lvl] = offset;
			}
			offset++;
		}

		// initialize Levels
		for (int i = 0; i < levels.length; i++) {
			levels[i] = new Level(i, reader.getGraphProperties().levelStats[i].numVertices,
					reader.getGraphProperties().levelStats[i].numEdges);
		}
	}

	private static void reverseInplace(int[] arr) {
		if (arr == null) {
			return;
		}
		int i = 0;
		int j = arr.length - 1;
		while (i < j) {
			// swap
			int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
			i++;
			j--;
		}
	}

	/**
	 * Converts degree to integer micro degree.
	 * 
	 * @param degree
	 *            array of degree values.
	 * @return array of micro degree values.
	 */
	private int[] toE6Waypoints(double[] degree) {
		if (degree == null) {
			return null;
		}
		int[] tmp = new int[degree.length - 2];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = GeoCoordinate.doubleToInt(degree[i + 1]);
		}
		return tmp;
	}

	/**
	 * Get the graph of the specified level of this hierarchy.
	 * 
	 * @param lvl
	 *            must be in range.
	 * @return the level.
	 */
	public Level getLevel(int lvl) {
		return levels[lvl];
	}

	/**
	 * @return all levels of this hierarchy.
	 */
	public Level[] getLevels() {
		return levels;
	}

	/**
	 * @return number of levels in this hierarchy.
	 */
	public int numLevels() {
		return numLevels;
	}

	/**
	 * @return all osm street types within this graph, no duplicates.
	 */
	public String[] getAllOsmStreetTypes() {
		String[] arr = new String[osmStreetTypeToByte.size()];
		osmStreetTypeToByte.keySet().toArray(arr);
		return arr;
	}

	/**
	 * @param id
	 *            vertex identifier.
	 * @return the maximum level the vertex belongs to.
	 */
	int getVertexLvl(int id) {
		return (vFirstLvlVertex[id + 1] - vFirstLvlVertex[id]) - 1;
	}

	/**
	 * @return longitudes in micro degrees of all vertices in this graph.
	 */
	public int[] getVertexLongitudesE6() {
		return vLon;
	}

	/**
	 * @return latitude in micro degrees of all vertices in this graph.
	 */
	public int[] getVertexLatitudesE6() {
		return vLat;
	}

	/**
	 * Each level implements the graph interface.
	 */
	public class Level implements Graph, Serializable {

		private static final long serialVersionUID = 1L;

		public final int lvl;
		private final int lvlNumVertices, lvlNumEdges;

		Level(int lvl, int lvlNumVertices, int lvlNumEdges) {
			this.lvl = lvl;
			this.lvlNumVertices = lvlNumVertices;
			this.lvlNumEdges = lvlNumEdges;
		}

		@Override
		public LevelVertex getVertex(int id) {
			if (getVertexLvl(id) >= lvl) {
				return new LevelVertex(id);
			}
			return null;
		}

		@Override
		public Iterator<LevelVertex> getVertices() {
			return new Iterator<LevelVertex>() {

				private int nextVertex = getNextVertex(0);

				@Override
				public boolean hasNext() {
					return nextVertex < numVertices;
				}

				@Override
				public LevelVertex next() {
					if (nextVertex < numVertices) {
						LevelVertex v = new LevelVertex(nextVertex);
						nextVertex = getNextVertex(nextVertex + 1);
						return v;
					}
					return null;
				}

				@Override
				public void remove() {
					// do nothing
				}

				private int getNextVertex(int startId) {
					int _startId = startId;
					while (_startId < numVertices) {
						if (getVertexLvl(_startId) >= lvl) {
							break;
						}
						_startId++;
					}
					return _startId;
				}
			};
		}

		@Override
		public int numEdges() {
			return lvlNumEdges;
		}

		@Override
		public int numVertices() {
			return lvlNumVertices;
		}

		/**
		 * lieghtweight vertex implementation.
		 * 
		 */
		public class LevelVertex implements Vertex, Serializable {

			private static final long serialVersionUID = 1L;

			private final int id;

			LevelVertex(int id) {
				if (getVertexLvl(id) < lvl) {
					System.out.println("dasdsadasgdasdasgjk");
				}
				this.id = id;
			}

			@Override
			public int getId() {
				return id;
			}

			/**
			 * @return the highway hierarchies neighborhood.
			 */
			public int getNeighborhood() {
				return vLvlVNh[vFirstLvlVertex[id] + lvl];
			}

			@Override
			public LevelEdge[] getOutboundEdges() {
				int start = vLvlFirstEdge[vFirstLvlVertex[id] + lvl];
				int end = vLvlFirstEdge[vFirstLvlVertex[id] + lvl + 1];

				LevelEdge[] edges = new LevelEdge[end - start];
				for (int i = start; i < end; i++) {
					edges[i - start] = new LevelEdge(i);
				}
				return edges;
			}

			/**
			 * @return the coordinate of this vertex.
			 */
			public GeoCoordinate getCoordinate() {
				return new GeoCoordinate(vLat[id], vLon[id]);
			}

			/**
			 * @return the level of this vertex.
			 */
			public int getLevel() {
				return lvl;
			}

			/**
			 * @return the maximum level this vertex belongs to.
			 */
			public int getMaxLevel() {
				return vFirstLvlVertex[id + 1] - vFirstLvlVertex[id] - 1;
			}
		}

		/**
		 * Lightweight edge implementation.
		 */
		public class LevelEdge implements Edge, Serializable {

			private static final long serialVersionUID = 1L;

			private final int id;

			LevelEdge(int id) {
				this.id = id;
			}

			/**
			 * @return identifier of this edge.
			 */
			public int getId() {
				return this.id;
			}

			@Override
			public LevelVertex getSource() {
				return new LevelVertex(eSource[id]);
			}

			@Override
			public LevelVertex getTarget() {
				return new LevelVertex(eTarget[id]);
			}

			@Override
			public int getWeight() {
				return eWeight[id];
			}

			/**
			 * @return the minimum level this edge belongs to.
			 */
			public int getMinLevel() {
				return eMinLvl[id];
			}

			/**
			 * @return true if edge is in forward graph.
			 */
			public boolean isForward() {
				return eDirection[FWD].get(id);
			}

			/**
			 * @return true if edge is in backward graph.
			 */
			public boolean isBackward() {
				return eDirection[BWD].get(id);
			}

			/**
			 * @return true if edge is a shortcut.
			 */
			public boolean isShortcut() {
				return eIsShortcut.get(id);
			}

			/**
			 * @return the street name of this edge.
			 */
			public String getName() {
				return eName[id];
			}

			/**
			 * @return the ref name of this edge.
			 */
			public String getRef() {
				return eRef[id];
			}

			/**
			 * @return osm street type like, motorway trunk etc.
			 */
			public String getOsmStreetType() {
				return byteToOsmStreetType.get(eOsmStreetType[id]);
			}

			/**
			 * @return true if edge is part of a roundabout.
			 */
			public boolean isRoundabout() {
				return eRoundabout.get(id);
			}

			/**
			 * @return the way point coordinates excluding source and target coordinate ordered
			 *         from source to target.
			 */
			public GeoCoordinate[] getWaypoints() {
				if (eLatitudesE6[id] == null) {
					return new GeoCoordinate[0];
				}
				GeoCoordinate[] waypoints = new GeoCoordinate[eLatitudesE6[id].length];
				for (int i = 0; i < eLatitudesE6[id].length; i++) {
					waypoints[i] = new GeoCoordinate(eLatitudesE6[id][i], eLongitudesE6[id][i]);
				}
				return waypoints;
			}
		}
	}
}
