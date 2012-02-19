/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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
package org.mapsforge.map.writer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.map.writer.model.GeoCoordinate;
import org.mapsforge.map.writer.model.MercatorProjection;
import org.mapsforge.map.writer.model.TDNode;
import org.mapsforge.map.writer.model.TDWay;
import org.mapsforge.map.writer.model.TileCoordinate;
import org.mapsforge.map.writer.model.WayDataBlock;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Provides utility functions for the maps preprocessing.
 * 
 * @author bross
 */
public final class GeoUtils {

	private GeoUtils() {
	}

	// private static final double DOUGLAS_PEUCKER_SIMPLIFICATION_TOLERANCE = 0.0000188;
	// private static final double DOUGLAS_PEUCKER_SIMPLIFICATION_TOLERANCE = 0.00003;
	/**
	 * The minimum amount of nodes required for a valid closed polygon.
	 */
	public static final int MIN_NODES_POLYGON = 4;

	/**
	 * The minimum amount of coordinates (lat/lon counted separately) required for a valid closed polygon.
	 */
	public static final int MIN_COORDINATES_POLYGON = 8;
	private static final byte SUBTILE_ZOOMLEVEL_DIFFERENCE = 2;
	private static final double[] EPSILON_ZERO = new double[] { 0, 0 };
	private static final Logger LOGGER = Logger.getLogger(GeoUtils.class.getName());

	private static final int[] TILE_BITMASK_VALUES = new int[] { 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128,
			64, 32, 16, 8, 4, 2, 1 };

	// JTS
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

	// **************** WAY OR POI IN TILE *****************
	/**
	 * Computes which tiles on the given base zoom level need to include the given way (which may be a polygon).
	 * 
	 * @param way
	 *            the way that is mapped to tiles
	 * @param baseZoomLevel
	 *            the base zoom level which is used in the mapping
	 * @param enlargementInMeter
	 *            amount of pixels that is used to enlarge the bounding box of the way and the tiles in the mapping
	 *            process
	 * @return all tiles on the given base zoom level that need to include the given way, an empty set if no tiles are
	 *         matched
	 */
	public static Set<TileCoordinate> mapWayToTiles(final TDWay way, final byte baseZoomLevel,
			final int enlargementInMeter) {
		if (way == null) {
			LOGGER.fine("way is null in mapping to tiles");
			return Collections.emptySet();
		}

		HashSet<TileCoordinate> matchedTiles = new HashSet<TileCoordinate>();
		Geometry wayGeometry = toJTSGeometry(way, !way.isForcePolygonLine());
		if (wayGeometry == null) {
			LOGGER.fine("unable to create geometry from way: " + way.getId());
			return matchedTiles;
		}

		TileCoordinate[] bbox = getWayBoundingBox(way, baseZoomLevel, enlargementInMeter);
		// calculate the tile coordinates and the corresponding bounding boxes
		for (int k = bbox[0].getX(); k <= bbox[1].getX(); k++) {
			for (int l = bbox[0].getY(); l <= bbox[1].getY(); l++) {
				Geometry bboxGeometry = tileToJTSGeometry(k, l, baseZoomLevel, enlargementInMeter);
				if (bboxGeometry.intersects(wayGeometry)) {
					matchedTiles.add(new TileCoordinate(k, l, baseZoomLevel));
				}
			}
		}

		return matchedTiles;
	}

	/**
	 * @param point
	 *            the point
	 * @param tile
	 *            the tile
	 * @return true if the point is located in the given tile
	 */
	public static boolean pointInTile(GeoCoordinate point, TileCoordinate tile) {
		if (point == null || tile == null) {
			return false;
		}

		int lon1 = GeoCoordinate.doubleToInt(MercatorProjection.tileXToLongitude(tile.getX(), tile.getZoomlevel()));
		int lon2 = GeoCoordinate.doubleToInt(MercatorProjection.tileXToLongitude(tile.getX() + 1, tile.getZoomlevel()));
		int lat1 = GeoCoordinate.doubleToInt(MercatorProjection.tileYToLatitude(tile.getY(), tile.getZoomlevel()));
		int lat2 = GeoCoordinate.doubleToInt(MercatorProjection.tileYToLatitude(tile.getY() + 1, tile.getZoomlevel()));
		return point.getLatitudeE6() <= lat1 && point.getLatitudeE6() >= lat2 && point.getLongitudeE6() >= lon1
				&& point.getLongitudeE6() <= lon2;
	}

	// *********** PREPROCESSING OF WAYS **************

	/**
	 * Preprocesses a way for writing it to a tile.
	 * 
	 * @param way
	 *            the way
	 * @param innerWays
	 *            the inner ways if existent, null otherwise
	 * @param clipPolygons
	 *            flag whether polygons should be clipped
	 * @param clipWays
	 *            flag whether ways should be clipped
	 * @param simplificationFactor
	 *            factor for simplification of geo objects
	 * @param tile
	 *            the tile
	 * @param enlargementInMeters
	 *            the enlargement of the tile in meters
	 * @return a JTS {@link Geometry} object representing the given way(s)
	 */
	public static Geometry preprocessWay(final TDWay way, final List<TDWay> innerWays, boolean clipPolygons,
			boolean clipWays, double simplificationFactor, final TileCoordinate tile, int enlargementInMeters) {

		Geometry geometry = toJtsGeometry(way, innerWays);
		if (geometry == null) {
			return null;
		}

		// clip geometry?
		Geometry tileBBJTS = null;
		if ((geometry instanceof Polygon || geometry instanceof LinearRing) && clipPolygons
				|| geometry instanceof LineString && clipWays) {

			// create tile bounding box
			tileBBJTS = tileToJTSGeometry(tile.getX(), tile.getY(), tile.getZoomlevel(), enlargementInMeters);

			// clip the polygon/ring by intersection with the bounding box of the tile
			// may throw a TopologyException
			try {
				// geometry = OverlayOp.overlayOp(tileBBJTS, geometry, OverlayOp.INTERSECTION);
				geometry = tileBBJTS.intersection(geometry);
			} catch (TopologyException e) {
				LOGGER.log(Level.FINE, "JTS cannot clip outer way: " + way.getId(), e);
				return null;
			}

		}

		if (simplificationFactor > 0 && tile.getZoomlevel() <= Constants.MAX_SIMPLIFICATION_BASE_ZOOM) {
			// TODO is this the right place to simplify, is better before clipping?
			geometry = TopologyPreservingSimplifier.simplify(geometry, simplificationFactor);
		}

		return geometry;
	}

	/**
	 * A tile on zoom level <i>z</i> has exactly 16 sub tiles on zoom level <i>z+2</i>. For each of these 16 sub tiles
	 * it is analyzed if the given way needs to be included. The result is represented as a 16 bit short value. Each bit
	 * represents one of the 16 sub tiles. A bit is set to 1 if the sub tile needs to include the way. Representation is
	 * row-wise.
	 * 
	 * @param geometry
	 *            the geometry which is analyzed
	 * @param tile
	 *            the tile which is split into 16 sub tiles
	 * @param enlargementInMeter
	 *            amount of pixels that is used to enlarge the bounding box of the way and the tiles in the mapping
	 *            process
	 * @return a 16 bit short value that represents the information which of the sub tiles needs to include the way
	 */
	public static short computeBitmask(final Geometry geometry, final TileCoordinate tile, // NOPMD by bross on
																							// 25.12.11 13:30
			final int enlargementInMeter) {
		List<TileCoordinate> subtiles = tile
				.translateToZoomLevel((byte) (tile.getZoomlevel() + SUBTILE_ZOOMLEVEL_DIFFERENCE));

		short bitmask = 0; // NOPMD by bross on 25.12.11 13:30
		int tileCounter = 0;
		for (TileCoordinate subtile : subtiles) {
			Geometry bbox = tileToJTSGeometry(subtile.getX(), subtile.getY(), subtile.getZoomlevel(),
					enlargementInMeter);
			if (bbox.intersects(geometry)) {
				bitmask |= TILE_BITMASK_VALUES[tileCounter];
			}
			tileCounter++;
		}
		return bitmask;
	}

	/**
	 * @param geometry
	 *            a JTS {@link Geometry} object representing the OSM entity
	 * @param tile
	 *            the tile
	 * @param enlargementInMeter
	 *            the enlargement of the tile in meters
	 * @return true, if the geometry is covered completely by this tile
	 */
	public static boolean coveredByTile(final Geometry geometry, final TileCoordinate tile, final int enlargementInMeter) {
		Geometry bbox = tileToJTSGeometry(tile.getX(), tile.getY(), tile.getZoomlevel(), enlargementInMeter);
		if (bbox.covers(geometry)) {
			return true;
		}

		return false;
	}

	/**
	 * @param geometry
	 *            the JTS {@link Geometry} object
	 * @return the centroid of the given geometry
	 */
	public static GeoCoordinate computeCentroid(Geometry geometry) {
		Point centroid = geometry.getCentroid();
		if (centroid != null) {
			return new GeoCoordinate(centroid.getCoordinate().y, centroid.getCoordinate().x);
		}

		return null;
	}

	/**
	 * Convert a JTS Geometry to a WayDataBlock list.
	 * 
	 * @param geometry
	 *            a geometry object which should be converted
	 * @return a list of WayBlocks which you can use to save the way.
	 */
	public static List<WayDataBlock> toWayDataBlockList(Geometry geometry) {
		List<WayDataBlock> res = new ArrayList<WayDataBlock>();
		if (geometry instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) geometry;
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				Polygon p = (Polygon) mp.getGeometryN(i);
				List<Integer> outer = toCoordinateList(p.getExteriorRing());
				List<List<Integer>> inner = new ArrayList<List<Integer>>();
				for (int j = 0; j < p.getNumInteriorRing(); j++) {
					inner.add(toCoordinateList(p.getInteriorRingN(j)));
				}
				res.add(new WayDataBlock(outer, inner));
			}
		} else if (geometry instanceof Polygon) {
			Polygon p = (Polygon) geometry;
			List<Integer> outer = toCoordinateList(p.getExteriorRing());
			List<List<Integer>> inner = new ArrayList<List<Integer>>();
			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				inner.add(toCoordinateList(p.getInteriorRingN(i)));
			}
			res.add(new WayDataBlock(outer, inner));
		} else if (geometry instanceof MultiLineString) {
			MultiLineString ml = (MultiLineString) geometry;
			for (int i = 0; i < ml.getNumGeometries(); i++) {
				LineString l = (LineString) ml.getGeometryN(i);
				res.add(new WayDataBlock(toCoordinateList(l), null));
			}
		} else if (geometry instanceof LinearRing || geometry instanceof LineString) {
			res.add(new WayDataBlock(toCoordinateList(geometry), null));
		} else if (geometry instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection) geometry;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				List<WayDataBlock> recursiveResult = toWayDataBlockList(gc.getGeometryN(i));
				for (WayDataBlock wayDataBlock : recursiveResult) {
					res.add(wayDataBlock);
				}
			}
		}

		return res;
	}

	// **************** JTS CONVERSIONS *********************

	private static Geometry toJtsGeometry(TDWay way, List<TDWay> innerWays) {

		Geometry wayGeometry = toJTSGeometry(way, !way.isForcePolygonLine());
		if (wayGeometry == null) {
			return null;
		}

		if (innerWays != null) {
			List<LinearRing> innerWayGeometries = new ArrayList<LinearRing>();
			if (!(wayGeometry instanceof Polygon)) {
				LOGGER.warning("outer way of multi polygon is not a polygon, skipping it: " + way.getId());
				return null;
			}
			Polygon outerPolygon = (Polygon) wayGeometry;

			for (TDWay innerWay : innerWays) {
				// in order to build the polygon with holes, we want to create
				// linear rings of the inner ways
				Geometry innerWayGeometry = toJTSGeometry(innerWay, false);
				if (innerWayGeometry == null) {
					continue;
				}

				if (!(innerWayGeometry instanceof LinearRing)) {
					LOGGER.warning("inner way of multi polygon is not a polygon, skipping it, inner id: "
							+ innerWay.getId() + ", outer id: " + way.getId());
					continue;
				}

				LinearRing innerRing = (LinearRing) innerWayGeometry;

				// check if inner way is completely contained in outer way
				if (outerPolygon.covers(innerRing)) {
					innerWayGeometries.add(innerRing);
				} else {
					LOGGER.warning("inner way is not contained in outer way, skipping inner way, inner id: "
							+ innerWay.getId() + ", outer id: " + way.getId());
				}
			}

			if (!innerWayGeometries.isEmpty()) {
				// make wayGeometry a new Polygon that contains inner ways as holes
				LinearRing[] holes = innerWayGeometries.toArray(new LinearRing[innerWayGeometries.size()]);
				LinearRing exterior = GEOMETRY_FACTORY
						.createLinearRing(outerPolygon.getExteriorRing().getCoordinates());
				wayGeometry = new Polygon(exterior, holes, GEOMETRY_FACTORY);
			}

		}

		return wayGeometry;
	}

	/**
	 * Internal conversion method to convert our internal data structure for ways to geometry objects in JTS. It will
	 * care about ways and polygons and will create the right JTS onjects.
	 * 
	 * @param way
	 *            TDway which will be converted. Null if we were not able to convert the way to a Geometry object.
	 * @param area
	 *            true, if the way represents an area, i.e. a polygon instead of a linear ring
	 * @return return Converted way as JTS object.
	 */
	private static Geometry toJTSGeometry(TDWay way, boolean area) {
		if (way.getWayNodes().length < 2) {
			LOGGER.fine("way has fewer than 2 nodes: " + way.getId());
			return null;
		}

		Coordinate[] coordinates = new Coordinate[way.getWayNodes().length];
		for (int i = 0; i < coordinates.length; i++) {
			TDNode currentNode = way.getWayNodes()[i];
			coordinates[i] = new Coordinate(GeoCoordinate.intToDouble(currentNode.getLongitude()),
					GeoCoordinate.intToDouble(currentNode.getLatitude()));
		}

		Geometry res = null;

		try {
			// check for closed polygon
			if (way.isPolygon()) {
				if (area) {
					// polygon
					res = GEOMETRY_FACTORY.createPolygon(GEOMETRY_FACTORY.createLinearRing(coordinates), null);
				} else {
					// linear ring
					res = GEOMETRY_FACTORY.createLinearRing(coordinates);
				}
			} else {
				res = GEOMETRY_FACTORY.createLineString(coordinates);
			}
		} catch (TopologyException e) {
			LOGGER.log(Level.FINE, "error creating JTS geometry from way: " + way.getId(), e);
			return null;
		}
		return res;
	}

	private static List<Integer> toCoordinateList(Geometry jtsGeometry) {

		Coordinate[] jtsCoords = jtsGeometry.getCoordinates();

		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int j = 0; j < jtsCoords.length; j++) {
			GeoCoordinate geoCoord = new GeoCoordinate(jtsCoords[j].y, jtsCoords[j].x);
			result.add(Integer.valueOf(geoCoord.getLatitudeE6()));
			result.add(Integer.valueOf(geoCoord.getLongitudeE6()));
		}

		return result;

	}

	private static double[] computeTileEnlargement(double lat, int enlargementInPixel) {

		if (enlargementInPixel == 0) {
			return EPSILON_ZERO; // NOPMD by bross on 25.12.11 13:32
		}

		double[] epsilons = new double[2];

		epsilons[0] = GeoCoordinate.latitudeDistance(enlargementInPixel);
		epsilons[1] = GeoCoordinate.longitudeDistance(enlargementInPixel, lat);

		return epsilons;
	}

	private static double[] bufferInDegrees(long tileY, byte zoom, int enlargementInMeter) {
		if (enlargementInMeter == 0) {
			return EPSILON_ZERO; // NOPMD by bross on 25.12.11 13:32
		}

		double[] epsilons = new double[2];
		double lat = MercatorProjection.tileYToLatitude(tileY, zoom);
		epsilons[0] = GeoCoordinate.latitudeDistance(enlargementInMeter);
		epsilons[1] = GeoCoordinate.longitudeDistance(enlargementInMeter, lat);

		return epsilons;
	}

	private static Geometry tileToJTSGeometry(long tileX, long tileY, byte zoom, int enlargementInMeter) {
		double minLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double maxLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		double[] epsilons = bufferInDegrees(tileY, zoom, enlargementInMeter);

		minLon -= epsilons[1];
		minLat -= epsilons[0];
		maxLon += epsilons[1];
		maxLat += epsilons[0];

		Coordinate bottomLeft = new Coordinate(minLon, minLat);
		Coordinate topRight = new Coordinate(maxLon, maxLat);

		return GEOMETRY_FACTORY.createLineString(new Coordinate[] { bottomLeft, topRight }).getEnvelope();
	}

	private static TileCoordinate[] getWayBoundingBox(final TDWay way, byte zoomlevel, int enlargementInPixel) {
		double maxx = Double.NEGATIVE_INFINITY, maxy = Double.NEGATIVE_INFINITY, minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY;
		for (TDNode coordinate : way.getWayNodes()) {
			maxy = Math.max(maxy, GeoCoordinate.intToDouble(coordinate.getLatitude()));
			miny = Math.min(miny, GeoCoordinate.intToDouble(coordinate.getLatitude()));
			maxx = Math.max(maxx, GeoCoordinate.intToDouble(coordinate.getLongitude()));
			minx = Math.min(minx, GeoCoordinate.intToDouble(coordinate.getLongitude()));
		}

		double[] epsilonsTopLeft = computeTileEnlargement(maxy, enlargementInPixel);
		double[] epsilonsBottomRight = computeTileEnlargement(miny, enlargementInPixel);

		TileCoordinate[] bbox = new TileCoordinate[2];
		bbox[0] = new TileCoordinate((int) MercatorProjection.longitudeToTileX(minx - epsilonsTopLeft[1], zoomlevel),
				(int) MercatorProjection.latitudeToTileY(maxy + epsilonsTopLeft[0], zoomlevel), zoomlevel);
		bbox[1] = new TileCoordinate(
				(int) MercatorProjection.longitudeToTileX(maxx + epsilonsBottomRight[1], zoomlevel),
				(int) MercatorProjection.latitudeToTileY(miny - epsilonsBottomRight[0], zoomlevel), zoomlevel);

		return bbox;
	}

	static String toGPX(final List<GeoCoordinate> g) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>").append("\n");
		sb.append(
				"<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"byHand\" "
						+ "version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
						+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 "
						+ "http://www.topografix.com/GPX/1/1/gpx.xsd\">").append("\n");
		for (GeoCoordinate c : g) {
			sb.append("\t<wpt ").append("lat=\"").append(c.getLatitude()).append("\" ");
			sb.append("lon=\"").append(c.getLongitude()).append("\"/>");
			sb.append("\n");
		}
		sb.append("</gpx>");

		return sb.toString();
	}

	static String arraysSVG(List<float[]> closedPolygons) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append("\n");
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
				+ "xmlns:ev=\"http://www.w3.org/2001/xml-events\" "
				+ "version=\"1.1\" baseProfile=\"full\" width=\"800mm\" height=\"600mm\">");

		for (float[] fs : closedPolygons) {
			sb.append("<polygon points=\"");
			for (float f : fs) {
				sb.append(f).append(" ");
			}
			sb.append("\" />");
		}

		sb.append("</svg>");

		return sb.toString();
	}
}
