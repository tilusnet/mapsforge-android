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
package org.mapsforge.routing.preprocessing.hh.mobile;

import java.io.IOException;

/**
 * Bit-granular stream implemented on top of the BitSerializer class for writing arrays.
 */
class BitArrayOutputStream {
	/**
	 * byte offset for next write.
	 */
	private int byteOffset;
	/**
	 * bit offset for next write.
	 */
	private int bitOffset;
	/**
	 * the data array to write to.
	 */
	private byte[] buff;
	/**
	 * bits until the end of the array. this value is used for checking against array out of
	 * bounds before writing.
	 */
	private long bitsRemain;

	/**
	 * @param buff
	 *            the array to write to.
	 */
	public BitArrayOutputStream(byte[] buff) {
		this.byteOffset = 0;
		this.bitOffset = 0;
		this.buff = buff;
		this.bitsRemain = buff.length * 8L;
	}

	/**
	 * @return byte array offset
	 */
	public int getByteOffset() {
		return byteOffset;
	}

	/**
	 * @return bit array offset, a value in [0 .. 7]
	 */
	public int getBitOffset() {
		return bitOffset;
	}

	/**
	 * @return number of bits that can be written until the end of the array is reached.
	 */
	public long getBitsRemain() {
		return bitsRemain;
	}

	/**
	 * @return number of bytes written to this stream.
	 */
	public int numBytesWritten() {
		if (bitOffset == 0) {
			return byteOffset;
		}
		return byteOffset + 1;
	}

	/**
	 * @param val
	 *            true writes a 1
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeBit(boolean val) throws IOException {
		if (bitsRemain >= 1) {
			Serializer.writeBit(val, buff, byteOffset, bitOffset);
			bitsWritten(1);
		} else {
			throw new IOException();
		}
	}

	/**
	 * @param val
	 *            to be written
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeByte(byte val) throws IOException {
		if (bitsRemain >= Serializer.BITS_PER_BYTE) {
			Serializer.writeByte(val, buff, byteOffset, bitOffset);
			bitsWritten(Serializer.BITS_PER_BYTE);
		} else {
			throw new IOException();
		}
	}

	/**
	 * @param val
	 *            to be written
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeShort(short val) throws IOException {
		if (bitsRemain >= Serializer.BITS_PER_SHORT) {
			Serializer.writeShort(val, buff, byteOffset, bitOffset);
			bitsWritten(Serializer.BITS_PER_SHORT);
		} else {
			throw new IOException();
		}
	}

	/**
	 * 
	 * @param val
	 *            to be written
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeInt(int val) throws IOException {
		if (bitsRemain >= Serializer.BITS_PER_INT) {
			Serializer.writeInt(val, buff, byteOffset, bitOffset);
			bitsWritten(Serializer.BITS_PER_INT);
		} else {
			throw new IOException();
		}
	}

	/**
	 * 
	 * @param val
	 *            to be written
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeLong(long val) throws IOException {
		if (bitsRemain >= Serializer.BITS_PER_LONG) {
			Serializer.writeLong(val, buff, byteOffset, bitOffset);
			bitsWritten(Serializer.BITS_PER_LONG);
		} else {
			throw new IOException();
		}
	}

	/**
	 * 
	 * @param val
	 *            to be written
	 * @param nBits
	 *            number of least significant bits to write.
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void writeUInt(long val, int nBits) throws IOException {
		if (bitsRemain >= nBits) {
			Serializer.writeUInt(val, nBits, buff, byteOffset, bitOffset);
			bitsWritten(nBits);
		} else {
			throw new IOException();
		}
	}

	/**
	 * 
	 * @param b
	 *            to be written
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void write(byte[] b) throws IOException {
		for (int i = 0; i < b.length; i++) {
			writeByte(b[i]);
		}
	}

	/**
	 * Move pointer forward, maybe skip some bit and move pointer to the next multiple of the
	 * given alignment.
	 * 
	 * @param byteAlignment
	 *            offset must be a multiple of this number.
	 * @throws IOException
	 *             if the end of the array is reached.
	 */
	public void alignPointer(int byteAlignment) throws IOException {
		int _byteOffset = byteOffset;
		if (bitOffset != 0) {
			_byteOffset++;
		}
		if (_byteOffset % byteAlignment != 0) {
			_byteOffset += byteAlignment - (_byteOffset % byteAlignment);
		}
		if (_byteOffset <= buff.length) {
			byteOffset = _byteOffset;
			bitOffset = 0;
		} else {
			throw new IOException();
		}
	}

	private void bitsWritten(int nBits) {
		byteOffset += (bitOffset + nBits) / 8;
		bitOffset = (bitOffset + nBits) % 8;
		bitsRemain -= 8;
	}
}
