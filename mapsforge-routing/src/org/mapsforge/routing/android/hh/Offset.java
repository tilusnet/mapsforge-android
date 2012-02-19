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
package org.mapsforge.routing.android.hh;

import org.mapsforge.routing.android.hh.ObjectPool.Poolable;

class Offset implements Poolable {

	int byteOffset;
	int bitOffset;
	private boolean isReleased;

	Offset() {
		this.byteOffset = 0;
		this.bitOffset = 0;
		this.isReleased = false;
	}

	Offset(int byteOffset, int bitOffset) {
		this.byteOffset = byteOffset;
		this.bitOffset = bitOffset;
	}

	@Override
	public String toString() {
		return byteOffset + ":" + bitOffset;
	}

	public void add(int nBits) {
		bitOffset += nBits;
		byteOffset += bitOffset / 8;
		bitOffset = bitOffset % 8;
	}

	public void set(int byteOffset, int bitOffset) {
		this.byteOffset = byteOffset;
		this.bitOffset = bitOffset;
		this.byteOffset += bitOffset / 8;
		this.bitOffset = bitOffset % 8;
	}

	@Override
	public boolean isReleased() {
		return isReleased;
	}

	@Override
	public void setReleased(boolean b) {
		this.isReleased = b;
	}
}
