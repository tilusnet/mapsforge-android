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
 * dynamic growing array
 */
public class UnsignedByteArrayGrowable implements Serializable {

	private static final long serialVersionUID = 5352302212205236878L;
	private final IntArrayGrowable data;
	private int size;
	private int fourByteOffset, byteOffset;

	/**
	 * @param chunkSize
	 *            amount to increase in size
	 */
	public UnsignedByteArrayGrowable(int chunkSize) {
		data = new IntArrayGrowable(chunkSize / 4);
		data.add(0);
		fourByteOffset = byteOffset = size = 0;
	}

	/**
	 * @param val
	 *            added to the end
	 */
	public void add(int val) {
		int _val = val;
		_val &= 0x000000ff;
		if (byteOffset == 4) {
			byteOffset = 0;
			fourByteOffset++;
			data.add(0);
		}
		data.set(fourByteOffset,
				(data.get(fourByteOffset) & (~(0x000000ff << (byteOffset * 8))))
						| (_val << (byteOffset * 8)));
		byteOffset++;
		size++;
	}

	/**
	 * @param idx
	 *            position
	 * @param val
	 *            new value put at position.
	 */
	public void set(int idx, int val) {
		int _val = val;
		_val &= 0x000000ff;
		int offsetA = idx / 4;
		int offsetB = idx % 4;
		data.set(offsetA, (data.get(offsetA) & (~(0x000000ff << (offsetB * 8))))
				| (_val << (offsetB * 8)));
	}

	/**
	 * @param idx
	 *            position
	 * @return value at index
	 */
	public int get(int idx) {
		int offsetA = idx / 4;
		int offsetB = idx % 4;
		return (data.get(offsetA) >>> (offsetB * 8)) & 0x000000ff;
	}

	/**
	 * @return size of this array
	 */
	public int size() {
		return size;
	}
}
