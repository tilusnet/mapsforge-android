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
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Router;
import org.mapsforge.core.Vertex;
import org.mapsforge.routing.android.hh.HHRouter;

/**
 *
 */
public class RouteViewer {

	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	final JXMapKit mapKit;
	final RouteOverlay overlay;
	GeoPosition routeSource;
	GeoPosition routeDestination;

	private static class DrawStyle {
		Color c;
		int strokeWidth;

		public DrawStyle(Color c, int strokeWidth) {
			this.c = c;
			this.strokeWidth = strokeWidth;
		}

	}

	LinkedList<Edge[]> sortedEdges = new LinkedList<Edge[]>();
	HashMap<Edge[], DrawStyle> edges = new HashMap<Edge[], DrawStyle>();

	/**
	 * Constructs a route viewer for the given router.
	 * 
	 * @param router
	 *            the router to use for rendering.
	 */
	public RouteViewer(Router router) {
		this.frame = new JFrame("RouteViewer");
		this.frame.setSize(800, 600);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mapKit = new BWMapKit();

		mapKit.setDefaultProvider(DefaultProviders.OpenStreetMaps);
		mapKit.setDataProviderCreditShown(true);

		this.overlay = new RouteOverlay(router, Color.RED.darker());
		mapKit.getMainMap().setOverlayPainter(overlay);
		mapKit.getMainMap().addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				mapKit.setAddressLocation((mapKit.getMainMap().convertPointToGeoPosition(evt
						.getPoint())));
			}
		});

		PopMenu menu = new PopMenu();
		mapKit.getMainMap().addMouseListener(menu.getMouseAdapter());
		this.frame.add(mapKit);
		frame.setVisible(true);
	}

	class BWMapKit extends JXMapKit {

		private static final long serialVersionUID = -2663786941719804979L;

		private ColorConvertOp op;

		public BWMapKit() {
			super();
			op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		}

		@Override
		public void paint(Graphics g) {

			// We use two images in case the operation doesn't allow in-place filtering

			// TODO - Ideally these would be cached and recreated whenever the size of this
			// component changes...

			BufferedImage originalImage = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_4BYTE_ABGR);

			BufferedImage filteredImage = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_4BYTE_ABGR);

			// Paint this component onto our buffer

			super.paint(originalImage.getGraphics());

			// Filter the buffer image

			op.filter(originalImage, filteredImage);

			// Paint the filtered image in place of the component
			g.drawImage(filteredImage, 0, 0, null);
			overlay.paint((Graphics2D) g, mapKit.getMainMap(), getWidth(), getHeight());
			// System.out.println("test");
		}

	}

	/**
	 * Draws an edge.
	 * 
	 * @param e
	 *            the edge to draw
	 * @param c
	 *            the color
	 * @param strokeWidth
	 *            with in pixels
	 */
	public void drawEdges(LinkedList<Edge> e, Color c, int strokeWidth) {
		synchronized (sortedEdges) {
			Edge[] arr = new Edge[e.size()];
			e.toArray(arr);
			drawEdges(arr, c, strokeWidth);
		}
	}

	/**
	 * Draws some edges.
	 * 
	 * @param e
	 *            the edges to be drawn.
	 * @param c
	 *            the color of the edges.
	 * @param strokeWidth
	 *            with in pixels.
	 */
	public void drawEdges(Edge[] e, Color c, int strokeWidth) {
		synchronized (sortedEdges) {
			sortedEdges.addLast(e);
			this.edges.put(e, new DrawStyle(c, strokeWidth));
		}
	}

	private class PopMenu extends JPopupMenu {

		private static final long serialVersionUID = 1L;

		private MouseAdapter mouseListener;
		Color cSelected = Color.green.darker().darker();
		Point position;

		public PopMenu() {
			super();
			mouseListener = new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// isPopUpTrigger doesn't work on the Linux platform that's the reason for this
					// complex if clause.
					if ((System.getProperty("os.name").toLowerCase().startsWith("linux") && e
							.getButton() == MouseEvent.BUTTON3) || e.isPopupTrigger()) {
						show(e.getComponent(), e.getX(), e.getY());
						position = e.getPoint();
					}
				}
			};
			add(getRouterMenu());
		}

		public MouseAdapter getMouseAdapter() {
			return mouseListener;
		}

		private JMenu getRouterMenu() {
			JMenu menu = new JMenu("routing");
			JMenuItem miSrc = new JMenuItem("choose as source") {
				private static final long serialVersionUID = 1L;

				@Override
				public Color getForeground() {
					if (routeSource == null) {
						return super.getForeground();
					}
					return cSelected;
				}
			};
			miSrc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					routeSource = mapKit.getMainMap().convertPointToGeoPosition(position);
					if (routeDestination != null && routeSource != null) {
						overlay.setRoute(new GeoCoordinate(routeSource.getLatitude(),
								routeSource.getLongitude()),
								new GeoCoordinate(routeDestination.getLatitude(),
										routeDestination.getLongitude()));
						routeSource = null;
						routeDestination = null;
					}

				}
			});
			menu.add(miSrc);
			JMenuItem miDst = new JMenuItem("choose as destination") {
				private static final long serialVersionUID = 1L;

				@Override
				public Color getForeground() {
					if (routeDestination == null) {
						return super.getForeground();
					}
					return cSelected;
				}
			};
			miDst.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					routeDestination = mapKit.getMainMap().convertPointToGeoPosition(position);
					if (routeDestination != null && routeSource != null) {
						overlay.setRoute(new GeoCoordinate(routeSource.getLatitude(),
								routeSource.getLongitude()),
								new GeoCoordinate(routeDestination.getLatitude(),
										routeDestination.getLongitude()));
						routeSource = null;
						routeDestination = null;
					}

				}
			});
			menu.add(miDst);

			return menu;
		}
	}

	private class RouteOverlay implements Painter<JXMapViewer> {

		private final Router router;
		private final Color cRoute;
		private Edge[] route;

		public RouteOverlay(Router router, Color cRoute) {
			this.router = router;
			this.route = null;
			this.cRoute = cRoute;
		}

		public void setRoute(GeoCoordinate src, GeoCoordinate tgt) {
			synchronized (sortedEdges) {
				Vertex s = router.getNearestVertex(src);
				Vertex t = router.getNearestVertex(tgt);
				this.route = router.getShortestPath(s.getId(), t.getId());
				mapKit.repaint();
			}
		}

		@Override
		public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
			Graphics2D _g = (Graphics2D) g.create();
			Rectangle rect = mapKit.getMainMap().getViewportBounds();
			_g.translate(-rect.x, -rect.y);
			_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			synchronized (sortedEdges) {
				for (Edge[] arr : sortedEdges) {
					_g.setColor(edges.get(arr).c);
					_g.setStroke(new BasicStroke(edges.get(arr).strokeWidth));
					for (Edge e : arr) {
						GeoCoordinate[] coords = e.getAllWaypoints();
						for (int j = 1; j < coords.length; j++) {
							double[] cs = new double[] { coords[j - 1].getLatitude(),
									coords[j - 1].getLongitude() };
							double[] ct = new double[] { coords[j].getLatitude(),
									coords[j].getLongitude() };
							Point2D s = mapKit.getMainMap().getTileFactory()
									.geoToPixel(new GeoPosition(cs),
											mapKit.getMainMap().getZoom());
							Point2D t = mapKit.getMainMap().getTileFactory()
									.geoToPixel(new GeoPosition(ct),
											mapKit.getMainMap().getZoom());
							_g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(),
									(int) t.getY());
						}
					}
				}

				_g.setStroke(new BasicStroke(2));
				_g.setColor(cRoute);
				if (route != null) {
					for (Edge e : route) {
						GeoCoordinate[] coords = e.getAllWaypoints();
						for (int j = 1; j < coords.length; j++) {
							double[] cs = new double[] { coords[j - 1].getLatitude(),
									coords[j - 1].getLongitude() };
							double[] ct = new double[] { coords[j].getLatitude(),
									coords[j].getLongitude() };
							Point2D s = mapKit
									.getMainMap()
									.getTileFactory()
									.geoToPixel(new GeoPosition(cs),
											mapKit.getMainMap().getZoom());
							Point2D t = mapKit
									.getMainMap()
									.getTileFactory()
									.geoToPixel(new GeoPosition(ct),
											mapKit.getMainMap().getZoom());
							_g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(),
									(int) t.getY());
						}
					}
				}
			}
			_g.dispose();
		}
	}

	public static void main(String[] args) {
		try {
			Router router = new HHRouter(new File("data/binary/berlin.mobileHH"), 1024 * 1024 * 100);
			new RouteViewer(router);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
