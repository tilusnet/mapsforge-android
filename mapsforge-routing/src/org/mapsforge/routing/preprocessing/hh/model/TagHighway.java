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
package org.mapsforge.routing.preprocessing.hh.model;

import java.util.HashMap;

/**
 * List of known highway tags (only roads for now).
 */
public class TagHighway {

	// roads
	/** */
	public static final String MOTORWAY = "motorway";
	/** */
	public static final byte MOTORWAY_BYTE = 1;
	/** */
	public static final String MOTORWAY_LINK = "motorway_link";
	/** */
	public static final byte MOTORWAY_LINK_BYTE = 2;
	/** */
	public static final String TRUNK = "trunk";
	/** */
	public static final byte TRUNK_BYTE = 3;
	/** */
	public static final String TRUNK_LINK = "trunk_link";
	/** */
	public static final byte TRUNK_LINK_BYTE = 4;
	/** */
	public static final String PRIMARY = "primary";
	/** */
	public static final byte PRIMARY_BYTE = 5;
	/** */
	public static final String PRIMARY_LINK = "primary_link";
	/** */
	public static final byte PRIMARY_LINK_BYTE = 6;
	/** */
	public static final String SECONDARY = "secondary";
	/** */
	public static final byte SECONDARY_BYTE = 7;
	/** */
	public static final String SECONDARY_LINK = "secondary_link";
	/** */
	public static final byte SECONDARY_LINK_BYTE = 8;
	/** */
	public static final String TERTIARY = "tertiary";
	/** */
	public static final byte TERTIARY_BYTE = 9;
	/** */
	public static final String UNCALSSIFIED = "unclassified";
	/** */
	public static final byte UNCALSSIFIED_BYTE = 10;
	/** */
	public static final String ROAD = "road";
	/** */
	public static final byte ROAD_BYTE = 11;
	/** */
	public static final String RESIDENTAL = "residential";
	/** */
	public static final byte RESIDENTAL_BYTE = 12;
	/** */
	public static final String LIVING_STREET = "living_street";
	/** */
	public static final byte LIVING_STREET_BYTE = 13;
	/** */
	public static final String SERVICE = "service";
	/** */
	public static final byte SERVICE_BYTE = 14;
	/** */
	public static final String TRACK = "track";
	/** */
	public static final byte TRACK_BYTE = 15;
	/** */
	public static final String PEDESTRIAN = "pedestrian";
	/** */
	public static final byte PEDESTRIAN_BYTE = 16;
	/** */
	public static final String RACEWAY = "raceway";
	/** */
	public static final byte RACEWAY_BYTE = 17;
	/** */
	public static final String SERVICES = "services";
	/** */
	public static final byte SERVICES_BYTE = 18;
	/** */
	public static final String BUS_GUIDEWAY = "bus_guideway";
	/** */
	public static final byte BUS_GUIDEWAY_BYTE = 19;
	/** map tag to bytes */
	public static HashMap<String, Byte> kv = new HashMap<String, Byte>();
	/** map bytes to tag */
	public static String[] vk; // reverse kv

	/**
	 * simple constructor
	 */
	static {
		kv.put(MOTORWAY, MOTORWAY_BYTE);
		kv.put(MOTORWAY_LINK, MOTORWAY_LINK_BYTE);
		kv.put(TRUNK, TRUNK_BYTE);
		kv.put(TRUNK_LINK, TRUNK_LINK_BYTE);
		kv.put(PRIMARY, PRIMARY_BYTE);
		kv.put(PRIMARY_LINK, PRIMARY_LINK_BYTE);
		kv.put(SECONDARY, SECONDARY_BYTE);
		kv.put(SECONDARY_LINK, SECONDARY_LINK_BYTE);
		kv.put(TERTIARY, TERTIARY_BYTE);
		kv.put(UNCALSSIFIED, UNCALSSIFIED_BYTE);
		kv.put(ROAD, ROAD_BYTE);
		kv.put(RESIDENTAL, RESIDENTAL_BYTE);
		kv.put(LIVING_STREET, LIVING_STREET_BYTE);
		kv.put(SERVICE, SERVICE_BYTE);
		kv.put(TRACK, TRACK_BYTE);
		kv.put(PEDESTRIAN, PEDESTRIAN_BYTE);
		kv.put(RACEWAY, RACEWAY_BYTE);
		kv.put(SERVICES, SERVICES_BYTE);
		kv.put(BUS_GUIDEWAY, BUS_GUIDEWAY_BYTE);
		vk = new String[kv.size() + 1];
		for (String k : kv.keySet()) {
			vk[kv.get(k)] = k;
		}
	}
}
