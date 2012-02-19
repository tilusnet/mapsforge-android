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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.core.Router;
import org.mapsforge.core.Vertex;
import org.mapsforge.routing.server.hh.EdgeMapper.EdgeMapping;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticEdge;
import org.mapsforge.routing.server.hh.HHStaticGraph.HHStaticVertex;

/**
 * This class servers the highway hierarchies routing functionality to other packages, thus it is the
 * interface of this package.
 */
public class HHRouterServerside implements Router {

	private static final String ALGORITHM_NAME = "Highway Hierarchies";

	// core
	private final HHAlgorithm algorithm;
	final HHStaticGraph routingGraph;

	// index structures
	private final HHEdgeExpanderRecursive edgeExpander;
	private final DistanceTable distanceTable;
	private final HHEdgeReverser edgeReverser;
	final GeoCoordinateKDTree vertexIndex;

	// mapping between rgEdgeIds to hhEdgeIds and vice versa.
	final EdgeMapper mapper;

	// storage components indexed by routing graph edgeIds
	final RgEdgeNames edgeNames;
	final EdgeIndex edgeIndex;

	private HHRouterServerside(HHAlgorithm algorithm, HHStaticGraph routingGraph,
			HHEdgeExpanderRecursive edgeExpander, DistanceTable distanceTable,
			HHEdgeReverser edgeReverser, GeoCoordinateKDTree vertexIndex, EdgeMapper mapper,
			RgEdgeNames edgeNames, EdgeIndex edgeIndex) {
		this.algorithm = algorithm;
		this.routingGraph = routingGraph;
		this.edgeExpander = edgeExpander;
		this.distanceTable = distanceTable;
		this.edgeReverser = edgeReverser;
		this.vertexIndex = vertexIndex;
		this.mapper = mapper;
		this.edgeNames = edgeNames;
		this.edgeIndex = edgeIndex;
	}

	/**
	 * Serializes this class to the given stream.
	 * 
	 * @param oStream
	 *            the stream to write to.
	 * @throws IOException
	 *             on error writing to the given stream.
	 */
	public void serialize(OutputStream oStream) throws IOException {
		routingGraph.serialize(oStream);
		edgeExpander.serialize(oStream);
		distanceTable.serialize(oStream);
		edgeReverser.serialize(oStream);
		vertexIndex.serialize(oStream);
		mapper.serialize(oStream);
		edgeNames.serialize(oStream);
		edgeIndex.serialize(oStream);
	}

	/**
	 * Loads a Router object from the given stream which has to be connected to a highway hierarchies
	 * binary file.
	 * 
	 * @param iStream
	 *            the stream to read from.
	 * @return a new object of this class.
	 * @throws IOException
	 *             on error reading stream.
	 * @throws ClassNotFoundException
	 *             error due to deserialization.
	 */
	public static HHRouterServerside deserialize(InputStream iStream) throws IOException,
			ClassNotFoundException {
		HHAlgorithm algorithm = new HHAlgorithm();
		HHStaticGraph routingGraph = HHStaticGraph.deserialize(iStream);

		// index structures
		HHEdgeExpanderRecursive edgeExpander = HHEdgeExpanderRecursive.deserialize(iStream);
		DistanceTable distanceTable = DistanceTable.deserialize(iStream);
		HHEdgeReverser edgeReverser = HHEdgeReverser.deserialize(iStream);
		GeoCoordinateKDTree vertexIndex = GeoCoordinateKDTree.deserialize(iStream);

		// mapping between rgEdgeIds to hhEdgeIds and vice versa.
		EdgeMapper mapper = EdgeMapper.deserialize(iStream);

		// storage components indexed by routing graph edgeIds
		RgEdgeNames edgeNames = RgEdgeNames.deserialize(iStream);
		EdgeIndex edgeIndex = EdgeIndex.deserialize(iStream);

		return new HHRouterServerside(algorithm, routingGraph, edgeExpander, distanceTable,
				edgeReverser, vertexIndex, mapper, edgeNames, edgeIndex);
	}

	/**
	 * Loads the data from a highway hierarchies database and constructs a router object. This is much
	 * slower than reading from a flat binary file. So it might be better to do this only once.
	 * 
	 * @param conn
	 *            connection to the database to read from.
	 * @return an instance of this class using the data of the input database.
	 * @throws SQLException
	 *             on error reading from database.
	 */
	public static HHRouterServerside getFromDb(Connection conn) throws SQLException {
		HHAlgorithm algorithm = new HHAlgorithm();
		HHStaticGraph routingGraph = HHStaticGraph.getFromHHDb(conn);

		// index structures
		HHEdgeExpanderRecursive edgeExpander = HHEdgeExpanderRecursive.createIndex(
				routingGraph, HHEdgeExpanderRecursive.getEMinLvl(conn));
		DistanceTable distanceTable = DistanceTable.getFromHHDb(conn);
		HHEdgeReverser edgeReverser = new HHEdgeReverser(routingGraph);
		GeoCoordinateKDTree vertexIndex = GeoCoordinateKDTree.buildHHVertexIndex(conn);

		// mapping between rgEdgeIds to hhEdgeIds and vice versa.
		EdgeMapper mapper = EdgeMapper.importFromDb(conn);

		// storage components indexed by routing graph edgeIds
		RgEdgeNames edgeNames = RgEdgeNames.importFromDb(conn);
		EdgeIndex edgeIndex = EdgeIndex.importFromDb(conn);

		return new HHRouterServerside(algorithm, routingGraph, edgeExpander, distanceTable,
				edgeReverser, vertexIndex, mapper, edgeNames, edgeIndex);
	}

	@Override
	public String getAlgorithmName() {
		return ALGORITHM_NAME;
	}

	@Override
	public HHEdge[] getNearestEdges(GeoCoordinate coord) {
		int rgEdgeId = edgeIndex.getNearestEdge(coord.getLongitudeE6(), coord.getLatitudeE6());
		EdgeMapping[] mapping = mapper.mapFromRgEdgeId(rgEdgeId);
		return getEdgesFromMapping(mapping);
	}

	@Override
	public Vertex getNearestVertex(GeoCoordinate coord) {
		int id = vertexIndex.getNearestNeighborIdx(coord.getLongitudeE6(), coord
				.getLatitudeE6());
		return new HHVertex(routingGraph.getVertex(id));
	}

	@Override
	public HHEdge[] getShortestPath(int sourceId, int targetId) {
		LinkedList<HHStaticEdge> searchSpace = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> fwd = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> bwd = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> expandedBwd = new LinkedList<HHStaticEdge>();
		int distance = algorithm.shortestPath(routingGraph, sourceId, targetId, distanceTable,
				fwd, bwd, searchSpace);
		if (distance == Integer.MAX_VALUE) {
			return null;
		}
		LinkedList<HHStaticEdge> sp = new LinkedList<HHStaticEdge>();
		edgeExpander.expandShortestPath(fwd, sp);
		edgeExpander.expandShortestPath(bwd, expandedBwd);
		edgeReverser.reverseEdges(expandedBwd, sp);

		HHEdge[] e = new HHEdge[sp.size()];
		int i = 0;
		for (Iterator<HHStaticEdge> iter = sp.iterator(); iter.hasNext();) {
			e[i++] = new HHEdge(iter.next());
		}
		return e;
	}

	@Override
	public Edge[] getShortestPathDebug(int sourceId, int targetId,
			Collection<Edge> searchspaceBuff) {
		LinkedList<HHStaticEdge> searchSpace = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> fwd = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> bwd = new LinkedList<HHStaticEdge>();
		LinkedList<HHStaticEdge> expandedBwd = new LinkedList<HHStaticEdge>();
		int distance = algorithm.shortestPath(routingGraph, sourceId, targetId, distanceTable,
				fwd, bwd, searchSpace);
		if (distance == Integer.MAX_VALUE) {
			return null;
		}
		LinkedList<HHStaticEdge> sp = new LinkedList<HHStaticEdge>();
		edgeExpander.expandShortestPath(fwd, sp);
		edgeExpander.expandShortestPath(bwd, expandedBwd);
		edgeReverser.reverseEdges(expandedBwd, sp);

		LinkedList<HHStaticEdge> searchSpaceExpanded = new LinkedList<HHStaticEdge>();
		edgeExpander.expandShortestPath(searchSpace, searchSpaceExpanded);
		for (Iterator<HHStaticEdge> iter = searchSpaceExpanded.iterator(); iter.hasNext();) {
			searchspaceBuff.add(new HHEdge(iter.next()));
		}

		HHEdge[] e = new HHEdge[sp.size()];
		int i = 0;
		for (Iterator<HHStaticEdge> iter = sp.iterator(); iter.hasNext();) {
			e[i++] = new HHEdge(iter.next());
		}
		return e;
	}

	private HHEdge[] getEdgesFromMapping(EdgeMapping[] mapping) {
		LinkedList<HHEdge> edges = new LinkedList<HHEdge>();
		for (EdgeMapping m : mapping) {
			HHStaticEdge e = routingGraph.getEdge(m.hhEdgeId);
			if (e.getDirection(HHStaticGraph.FWD)) {
				HHEdge e_ = new HHEdge(e);
				edges.add(e_);
			}
		}
		HHEdge[] arr = new HHEdge[edges.size()];
		edges.toArray(arr);
		return arr;
	}

	@Override
	public Iterator<HHVertex> getVerticesWithinBox(Rect bbox) {
		final TIntArrayList ids = vertexIndex.getIndicesByBoundingBox(bbox.minLongitudeE6,
				bbox.minLatitudeE6, bbox.maxLongitudeE6,
				bbox.maxLatitudeE6);
		return new Iterator<HHVertex>() {

			private TIntIterator iter = ids.iterator();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public HHVertex next() {
				if (iter.hasNext()) {
					return new HHVertex(routingGraph.getVertex(iter.next()));
				}
				return null;

			}

			@Override
			public void remove() {
				//
			}
		};

	}

	@Override
	public Rect getBoundingBox() {
		return new Rect(Math.min(vertexIndex.getMinLongitude(), edgeIndex.getMinLongitude()),
				Math.max(vertexIndex.getMaxLongitude(), edgeIndex.getMaxLongitude()),
				Math.min(vertexIndex.getMinLatitude(), edgeIndex.getMinLatitude()),
				Math.max(vertexIndex.getMaxLatitude(), edgeIndex.getMaxLatitude()));
	}

	@Override
	public Vertex getVertex(int id) {
		return new HHVertex(routingGraph.getVertex(id));
	}

	private class HHEdge implements Edge {

		private HHStaticEdge e;

		public HHEdge(HHStaticEdge e) {
			this.e = e;
		}

		@Override
		public int getId() {
			return e.getId();
		}

		@Override
		public String getName() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			return edgeNames.getName(mapping.rgEdgeId);
		}

		@Override
		public Vertex getSource() {
			return new HHVertex(e.getSource());
		}

		@Override
		public Vertex getTarget() {
			return new HHVertex(e.getTarget());
		}

		@Override
		public GeoCoordinate[] getAllWaypoints() {
			int s = getWaypoints().length + 2;
			GeoCoordinate[] result = new GeoCoordinate[s];
			result[0] = this.getSource().getCoordinate();
			GeoCoordinate[] inbetween = this.getWaypoints();
			for (int i = 1; i <= inbetween.length; i++) {
				result[i] = inbetween[i - 1];
			}
			result[s - 1] = this.getTarget().getCoordinate();
			return result;
		}

		@Override
		public GeoCoordinate[] getWaypoints() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			if (mapping == null) {
				System.out.println("mapping error : shortcut = " + e.isShortcut() + " id ="
						+ e.getId() + " : " + e.getSource().getId() + " -> "
						+ e.getTarget().getId() + " weight = " + e.getWeight());
				return new GeoCoordinate[0];
			}

			GeoCoordinate[] waypoints = edgeIndex.getWaypoints(mapping.rgEdgeId);
			if (waypoints != null && mapping.isReversed) {
				// reverse array
				int i = 0;
				int j = waypoints.length - 1;
				while (i < j) {
					GeoCoordinate tmp = waypoints[i];
					waypoints[i] = waypoints[j];
					waypoints[j] = tmp;
					i++;
					j--;
				}
			}
			return waypoints;
		}

		@Override
		public int getWeight() {
			return e.getWeight();
		}

		@Override
		public String getRef() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			return edgeNames.getRef(mapping.rgEdgeId);
		}

		@Override
		public String getDestination() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			return edgeNames.getDestination(mapping.rgEdgeId);
		}

		@Override
		public boolean isRoundabout() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			return edgeNames.isRoundabout(mapping.rgEdgeId);
		}

		@Override
		public String getType() {
			EdgeMapping mapping = mapper.mapFromHHEdgeId(e.getId());
			return edgeNames.getHighwayLevel(mapping.rgEdgeId);
		}
	}

	private class HHVertex implements Vertex {

		private HHStaticVertex v;

		public HHVertex(HHStaticVertex v) {
			this.v = v;
		}

		@Override
		public GeoCoordinate getCoordinate() {
			return vertexIndex.getCoordinate(v.getId());
		}

		@Override
		public int getId() {
			return v.getId();
		}

		@Override
		public Edge[] getOutboundEdges() {
			HHStaticEdge[] e = v.getAdjacentLevel0Edges();
			HHEdge[] e_ = new HHEdge[e.length];
			for (int i = 0; i < e.length; i++) {
				e_[i] = new HHEdge(e[i]);
			}
			return e_;
		}
	}

	@Override
	public Edge getNearestEdge(GeoCoordinate coord) {
		throw new UnsupportedOperationException();
	}

	// public static void main(final String[] args) throws FileNotFoundException, IOException,
	// ClassNotFoundException {
	// IRouter router = RouterImpl.deserialize(new FileInputStream("router/berlin.hh"));
	//
	// IVertex source = router.getNearestVertex(new GeoCoordinate(52.509769, 13.4567655));
	// IVertex target = router.getNearestVertex(new GeoCoordinate(52.4556941, 13.2918805));
	// IEdge[] shortestPath = router.getShortestPath(source.getId(), target.getId());
	// for (IEdge e : shortestPath) {
	// System.out.println(e.getName() + " " + e.getRef());
	// }
	// }
}
