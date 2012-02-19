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
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.arrays;

import java.io.Serializable;

/**
 * array of bytes
 */
public class UnsignedByteArray implements Serializable {

	private static final long serialVersionUID = 2422456076440395610L;

	private final int[] data;

	/**
	 * @param size
	 *            of this array
	 */
	public UnsignedByteArray(int size) {
		int len = size / 4;
		if (size % 4 != 0) {
			len++;
		}
		data = new int[len];
	}

	/**
	 * @param idx
	 *            index to modify
	 * @param val
	 *            new value.
	 */
	public void set(int idx, int val) {
		int _val = val;
		_val &= 0x000000ff;
		int arrayOffset = idx / 4;
		int bitOffset = (idx % 4) * 8;
		data[arrayOffset] = (data[arrayOffset] & ~(0x000000ff << bitOffset))
				| (_val << bitOffset);
	}

	/**
	 * @param idx
	 *            position.
	 * @return value at index.
	 */
	public int get(int idx) {
		int arrayOffset = idx / 4;
		int bitOffset = (idx % 4) * 8;
		return (data[arrayOffset] >>> bitOffset) & 0x000000ff;
	}
}