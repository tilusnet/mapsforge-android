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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * @author Frank Viernau
 */
class BufferedCanvas extends JComponent {

	private static final long serialVersionUID = -2216749283414845797L;

	private final int w, h;
	private final BufferedImage buffer;
	private final Graphics g;

	public BufferedCanvas(int width, int height) {
		super();
		this.w = width;
		this.h = height;
		this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = buffer.getGraphics();
		setPreferredSize(new Dimension(width, height));
	}

	@Override
	public void paint(Graphics graphics) {
		graphics.drawImage(buffer, 0, 0, this);
		super.paint(graphics);
	}

	public void clear(Color bgColor) {
		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);
	}

	public void drawCircle(int x, int y, Color c, int radius) {
		// c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 180);
		g.setColor(c);
		g.fillArc(x - radius, h - y - radius, (radius * 2) + 1, (radius * 2) + 1, 0, 360);
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color c) {
		g.setColor(c);
		g.drawLine(x1, h - y1, x2, h - y2);
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color c, int width) {
		g.setColor(c);
		g.drawLine(x1, h - y1, x2, h - y2);

		double vx = y2 - y1;
		double vy = x2 - x1;
		double len = Math.sqrt((vx * vx) + (vy * vy));
		int nx = (int) Math.rint(vx / (len / width));
		int ny = (int) Math.rint(vy / (len / width));

		int[] x = new int[] { x1 + nx, x1 - nx, x2 - nx, x2 + nx };
		int[] y = new int[] { h - y1 + ny, h - y1 - ny, h - y2 - ny, h - y2 + ny, };
		g.fillPolygon(x, y, 4);
	}

	public void update() {
		repaint();
	}
}
