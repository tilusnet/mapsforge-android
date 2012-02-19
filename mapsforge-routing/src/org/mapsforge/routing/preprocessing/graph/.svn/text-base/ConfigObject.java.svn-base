/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.routing.preprocessing.graph;

import java.util.Set;

/**
 * This class bundles given Key/Value-Pairs for OSM-Extraction.
 * 
 * @author Michael Bartel
 * 
 */
public class ConfigObject {

	Set<KeyValuePair> wayTagsSet;
	Set<KeyValuePair> nodeTagsSet;
	Set<KeyValuePair> relationTagsSet;

	/**
	 * Constructor for Creation
	 * 
	 * @param wt
	 *            - The Set including all needed Key/Value-Pairs of OSM.ways
	 * @param nt
	 *            - The Set including all needed Key/Value-Pairs of OSM.nodes
	 * @param rt
	 *            - The Set including all needed Key/Value-Pairs of OSM.relations
	 */
	public ConfigObject(Set<KeyValuePair> wt, Set<KeyValuePair> nt, Set<KeyValuePair> rt) {
		this.wayTagsSet = wt;
		this.nodeTagsSet = nt;
		this.relationTagsSet = rt;
	}

	/**
	 * Returns true if the key/value pair exists in the corresponding set
	 * 
	 * @param key
	 *            The key to be checked
	 * @param value
	 *            The value to be checked
	 * @return true, if the pair exists
	 */
	public boolean containsWayTag(String key, String value) {
		return (wayTagsSet.contains(new KeyValuePair(value, key)) || wayTagsSet
				.contains(new KeyValuePair(null, key)));
	}

	/**
	 * Returns true if just the key exists
	 * 
	 * @param key
	 *            the key to be checked
	 * @return true if key was found
	 */
	public boolean containsWayTagKey(String key) {
		return wayTagsSet.contains(new KeyValuePair(null, key));
	}

	/**
	 * Returns true if the key/value pair exists in the corresponding set
	 * 
	 * @param key
	 *            The key to be checked
	 * @param value
	 *            The value to be checked
	 * @return true, if the pair exists
	 */
	public boolean containsRelationTag(String key, String value) {
		/*
		 * for (KeyValuePair sp : relationTagsSet) if (sp.value != null) if ((sp.key.equals(key)) &&
		 * sp.value.equals(value)) return true; return false;
		 */
		return (relationTagsSet.contains(new KeyValuePair(value, key)) || relationTagsSet
				.contains(new KeyValuePair(null, key)));
	}

	/**
	 * Returns true if the key/value pair exists in the corresponding set
	 * 
	 * @param key
	 *            The key to be checked
	 * @param value
	 *            The value to be checked
	 * @return true, if the pair exists
	 */
	public boolean containsNodeTag(String key, String value) {
		/*
		 * for (KeyValuePair sp : nodeTagsSet) if (sp.value != null) if ((sp.key.equals(key)) &&
		 * sp.value.equals(value)) return true; return false;
		 */
		return (nodeTagsSet.contains(new KeyValuePair(value, key)) || nodeTagsSet
				.contains(new KeyValuePair(null, key)));
	}

}
