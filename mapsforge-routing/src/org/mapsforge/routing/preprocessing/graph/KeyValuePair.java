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
package org.mapsforge.routing.preprocessing.graph;

import java.io.Serializable;

/**
 * This class represents a KeyValue-Pair
 * 
 * @author Michael Bartel
 * 
 */
public class KeyValuePair implements Serializable {

	private static final long serialVersionUID = 1L;

	String value;
	String key;

	/**
	 * The constructor to create a new instance
	 * 
	 * @param v
	 *            the value
	 * @param k
	 *            the key
	 */
	public KeyValuePair(String v, String k) {
		value = v;
		key = k;
		// System.out.println("Key: " + k + " Value: " + v);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KeyValuePair) {
			KeyValuePair o = (KeyValuePair) obj;

			if (o.value == null)
				return ((key.equals(o.key)));

			if (value == null)
				return ((key.equals(o.key)));

			return ((value.equals(o.value)) && (key.equals(o.key)));
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (value == null)
			return key.hashCode();
		return value.hashCode() + key.hashCode();
	}

	@Override
	public String toString() {
		return "Key: " + key + " Value: " + value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * sets the value
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * sets a new key
	 * 
	 * @param key
	 *            the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

}
