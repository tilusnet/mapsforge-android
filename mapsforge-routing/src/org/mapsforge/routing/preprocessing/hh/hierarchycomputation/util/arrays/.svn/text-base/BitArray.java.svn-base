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
 * array based implementation requires about 1 bit per bit.
 */
public class BitArray implements Serializable {

	private static final long serialVersionUID = 5879036119351268170L;

	private static final int BITS = 32;
	private final int[] data;
	private final int size;

	/**
	 * @param size
	 *            of this array
	 */
	public BitArray(int size) {
		this.size = size;
		int len = size / 32;
		if (size % 32 != 0) {
			len++;
		}
		data = new int[len];
	}

	/**
	 * @param i
	 *            set bit i to 0
	 */
	public void clear(int i) {
		data[i / BITS] &= ~(1 << (i % BITS));
	}

	/**
	 * @param i
	 *            index of the bit to return
	 * @return true if bit is set.
	 */
	public boolean get(int i) {
		return (data[i / BITS] & (1 << (i % BITS))) != 0;
	}

	/**
	 * @param i
	 *            index of the bit to manipulate
	 * @param b
	 *            the new value of the bit.
	 */
	public void set(int i, boolean b) {
		if (b) {
			set(i);
		} else {
			clear(i);
		}
	}

	/**
	 * Set bit to 1.
	 * 
	 * @param i
	 *            index of the bit
	 */
	public void set(int i) {
		data[i / BITS] |= (1 << (i % BITS));
	}

	/**
	 * @return size of this bit array.
	 */
	public int size() {
		return size;
	}
}
