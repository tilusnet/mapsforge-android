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
 * 
 */
public class UnsignedFourBitArray implements Serializable {

	private static final long serialVersionUID = -4385361050427649174L;

	private final int[] data;
	private final int size;

	/**
	 * @param size
	 *            of this array
	 */
	public UnsignedFourBitArray(int size) {
		this.size = size;
		int len = size / 8;
		if (size % 8 != 0) {
			len++;
		}
		data = new int[len];
	}

	/**
	 * @param idx
	 *            position the put value
	 * @param val
	 *            new value
	 */
	public void set(int idx, int val) {
		int _val = val;
		_val &= 0x0000000f;
		int arrayOffset = idx / 8;
		int bitOffset = (idx % 8) * 4;
		data[arrayOffset] = (data[arrayOffset] & ~(0x0000000f << bitOffset))
				| (_val << bitOffset);
	}

	/**
	 * @param idx
	 *            postition.
	 * @return value at position
	 */
	public int get(int idx) {
		int arrayOffset = idx / 8;
		int bitOffset = (idx % 8) * 4;
		return (data[arrayOffset] >>> bitOffset) & 0x0000000f;
	}

	/**
	 * @return this of this array
	 */
	public int size() {
		return size;
	}
}
