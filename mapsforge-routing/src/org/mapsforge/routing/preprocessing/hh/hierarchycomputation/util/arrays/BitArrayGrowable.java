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
 * dynamic growable bit array, increases only by a constant amount of bytes, implementations i
 * had seen always grow by a factor of current size, which may give out of memory exceptions too
 * early.
 */
public class BitArrayGrowable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1451950718014781784L;
	private final static int BIT = 0x00000001;
	private IntArrayGrowable data;
	private int offsetA, offsetB;

	/**
	 * @param chunkSize
	 *            amount to increase siye.
	 */
	public BitArrayGrowable(int chunkSize) {
		this.data = new IntArrayGrowable(chunkSize / 32);
		offsetA = -1;
		offsetB = 32;
	}

	/**
	 * @param b
	 *            is added to the end of the array.
	 */
	public void add(boolean b) {
		if (offsetB == 32) {
			offsetA++;
			offsetB = 0;
			if (offsetA >= data.size()) {
				data.add(0);
			}
		}
		if (b) {
			data.set(offsetA, data.get(offsetA) | (BIT << offsetB));
		}
		offsetB++;
	}

	/**
	 * @param idx
	 *            index of the bit.
	 * @return the bit located at the given index.
	 */
	public boolean get(int idx) {
		int fourByteOffset = idx / 32;
		int bitOffset = idx % 32;
		return (data.get(fourByteOffset) & (BIT << bitOffset)) != 0;
	}

	/**
	 * @param idx
	 *            index of the bit
	 * @param b
	 *            new value of the bit.
	 */
	public void set(int idx, boolean b) {
		if (b) {
			set(idx);
		} else {
			clear(idx);
		}
	}

	/**
	 * set to 1.
	 * 
	 * @param idx
	 *            index of the bit
	 */
	public void set(int idx) {
		int fourByteOffset = idx / 32;
		int bitOffset = idx % 32;
		data.set(fourByteOffset, data.get(fourByteOffset) | (BIT << bitOffset));
	}

	/**
	 * set to 0.
	 * 
	 * @param idx
	 *            of the bit.
	 */
	public void clear(int idx) {
		int fourByteOffset = idx / 32;
		int bitOffset = idx % 32;
		data.set(fourByteOffset, data.get(fourByteOffset) & (~(BIT << bitOffset)));
	}
}
