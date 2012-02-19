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

/**
 * A Simple implementation of a pointer, it points to a specific 63 bit address and holds the
 * number of bytes it points to.
 */
final class Pointer {

	/**
	 * The first byte this pointer points to.
	 */
	public final long startAddr;
	/**
	 * The number of bytes this pointer points to.
	 */
	public final int lengthBytes;

	/**
	 * Constructs a Pointer.
	 * 
	 * @param startAddr
	 *            The first byte this pointer points to.
	 * @param lenghtBytes
	 *            The number of bytes this pointer points to.
	 */
	Pointer(long startAddr, int lenghtBytes) {
		this.startAddr = startAddr;
		this.lengthBytes = lenghtBytes;
	}

	@Override
	public String toString() {
		return "[" + startAddr + " - " + (startAddr + lengthBytes) + "]";
	}
}
