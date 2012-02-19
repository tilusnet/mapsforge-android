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
package org.mapsforge.routing.preprocessing.mm.osmosis;

import org.mapsforge.routing.preprocessing.hh.model.TagHighway;

/**
 * List of known highway tags (only roads for now).
 */
public class TagMultiWay extends TagHighway {

	// roads
	/** */
	public static final String RAIL = "rail";
	/** */
	public static final byte RAIL_BYTE = 20;
	/** */
	public static final String LIGHT_RAIL = "light_rail";
	/** */
	public static final byte LIGHT_RAIL_BYTE = 21;
	/** */
	public static final String NARROW_GAUGE = "narrow_gauge";
	/** */
	public static final byte NARROW_GAUGE_BYTE = 22;
	/** */
	public static final String TRAM = "tram";
	/** */
	public static final byte TRAM_BYTE = 23;
	/** */
	public static final String MONORAIL = "monorail";
	/** */
	public static final byte MONORAIL_BYTE = 24;
	/** */
	public static final String FUNICULAR = "funicular";
	/** */
	public static final byte FUNICULAR_BYTE = 25;
	/** */
	public static final String SUBWAY = "subway";
	/** */
	public static final byte SUBWAY_BYTE = 26;

	/**
	 * simple constructor
	 */
	static {
		kv.put(RAIL, RAIL_BYTE);
		kv.put(LIGHT_RAIL, LIGHT_RAIL_BYTE);
		kv.put(NARROW_GAUGE, NARROW_GAUGE_BYTE);
		kv.put(TRAM, TRAM_BYTE);
		kv.put(MONORAIL, MONORAIL_BYTE);
		kv.put(FUNICULAR, FUNICULAR_BYTE);
		kv.put(SUBWAY, SUBWAY_BYTE);

		vk = new String[kv.size() + 1];
		for (String k : kv.keySet()) {
			vk[kv.get(k)] = k;
		}
	}

}
