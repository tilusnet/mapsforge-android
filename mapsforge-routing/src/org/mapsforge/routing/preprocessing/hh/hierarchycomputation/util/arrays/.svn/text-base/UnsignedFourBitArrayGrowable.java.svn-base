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
public class UnsignedFourBitArrayGrowable implements Serializable {

	private static final long serialVersionUID = 5352302212205236878L;

	private final IntArrayGrowable data;
	private int size;
	private int arrayOffset, bitOffset;

	/**
	 * @param chunkSize
	 *            amount to increase size
	 */
	public UnsignedFourBitArrayGrowable(int chunkSize) {
		data = new IntArrayGrowable(chunkSize / 4);
		data.add(0);
		arrayOffset = bitOffset = size = 0;
	}

	/**
	 * @param val
	 *            added to the end
	 */
	public void add(int val) {
		int _val = val;
		_val &= 0x0000000f;
		if (bitOffset == 32) {
			bitOffset = 0;
			arrayOffset++;
			data.add(0);
		}
		data.set(arrayOffset, (data.get(arrayOffset) & (~(0x0000000f << (bitOffset))))
				| (_val << (bitOffset)));
		bitOffset += 4;
		size++;
	}

	/**
	 * @param idx
	 *            position
	 * @param val
	 *            is put to position
	 */
	public void set(int idx, int val) {
		int _val = val;
		_val &= 0x0000000f;
		int arrOffs = idx / 8;
		int bitOffs = (idx % 8) * 4;
		data.set(arrOffs, (data.get(arrOffs) & (~(0x0000000f << (bitOffs))))
				| (_val << (bitOffs)));
	}

	/**
	 * @param idx
	 *            addresses the value
	 * @return value at pos.
	 */
	public int get(int idx) {
		int offsetA = idx / 8;
		int offsetB = (idx % 8) * 4;
		return (data.get(offsetA) >>> (offsetB)) & 0x0000000f;
	}

	/**
	 * @return size of this array.
	 */
	public int size() {
		return size;
	}
}
