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
 * Optimized Low level de-serialization class. Optimization was only done by thinking about the
 * code, it has not yet been proven to be fast. This should be checked by using the android
 * profiling utilities. Think it depends on how fast a long can be shifted by the cpu of a
 * mobile device.
 */
final class Deserializer {

	/**
	 * used with bitwise and to check if a bit is set.
	 */
	private final static byte[] BYTE_NTH_BIT_SET = new byte[] {
			0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80
	};

	/**
	 * used with bitwise and to clear high bits.
	 */
	private final static int[] INT_HIGH_CLEARED = new int[] { /** */
	0xffffffff, 0x7fffffff, 0x3fffffff, 0x1fffffff, /** */
	0x0fffffff, 0x07ffffff, 0x03ffffff, 0x01ffffff, /** */
	0x00ffffff, 0x007fffff, 0x003fffff, 0x001fffff, /** */
	0x000fffff, 0x0007ffff, 0x0003ffff, 0x0001ffff, /** */
	0x0000ffff, 0x00007fff, 0x00003fff, 0x00001fff, /** */
	0x00000fff, 0x000007ff, 0x000003ff, 0x000001ff, /** */
	0x000000ff, 0x0000007f, 0x0000003f, 0x0000001f, /** */
	0x0000000f, 0x00000007, 0x00000003, 0x00000001, /** */
	0x00000000
			};

	/**
	 * Reads a bit from the given array.
	 * 
	 * @param buff
	 *            the array to read from.
	 * @param byteOffset
	 *            offset to the array.
	 * @param bitOffset
	 *            offset to the array.
	 * @return true if bit is set.
	 */
	static final boolean readBit(byte[] buff, int byteOffset, int bitOffset) {
		return 0 != (buff[byteOffset] & BYTE_NTH_BIT_SET[bitOffset]);
	}

	/**
	 * Reads a bit from the given array. The Offset is incremented by nBits.
	 * 
	 * @param buff
	 *            the array to read from.
	 * @param offset
	 *            offset to the array.
	 * @return true if bit is set.
	 */
	static final boolean readBit(byte[] buff, Offset offset) {
		boolean val = 0 != (buff[offset.byteOffset] & BYTE_NTH_BIT_SET[offset.bitOffset]);
		offset.add(1);
		return val;
	}

	/**
	 * Reads a byte from the given array.
	 * 
	 * @param buff
	 *            the array to read from.
	 * @param byteOffset
	 *            offset to the array.
	 * @param bitOffset
	 *            offset to the array.
	 * @return the value read.
	 */
	static final byte readByte(byte[] buff, int byteOffset, int bitOffset) {
		if (bitOffset == 0) {
			return buff[byteOffset];
		}
		return (byte) (((buff[byteOffset] & 0xff) >>> bitOffset) | ((buff[byteOffset + 1] & 0xff) << (8 - bitOffset)));
	}

	/**
	 * Reads a byte from the given array. The Offset is incremented by nBits.
	 * 
	 * @param buff
	 *            the array to read from.
	 * @param offset
	 *            offset to the array.
	 * @return the value read.
	 */
	static final byte readByte(byte[] buff, Offset offset) {
		byte val;
		if (offset.bitOffset == 0) {
			val = buff[offset.byteOffset];
		} else {
			val = (byte) (((buff[offset.byteOffset] & 0xff) >>> offset.bitOffset) | ((buff[offset.byteOffset + 1] & 0xff) << (8 - offset.bitOffset)));
		}
		offset.add(8);
		return val;
	}

	/**
	 * Reads a short from the array.
	 * 
	 * @param buff
	 *            array to read from.
	 * @param byteOffset
	 *            offset to the array.
	 * @param bitOffset
	 *            offset to the array.
	 * @return the value read.
	 */
	static final short readShort(byte[] buff, int byteOffset, int bitOffset) {
		if (bitOffset == 0) {
			return (short) ((buff[byteOffset] & 0xff) | ((buff[byteOffset + 1] & 0xff) << 8));
		}
		return (short) ((((buff[byteOffset] & 0xff) | ((buff[byteOffset + 1] & 0xff) << 8) | ((buff[byteOffset + 2] & 0xff) << 16)) >>> bitOffset) & 0x0000ffff);
	}

	/**
	 * Reads a short from the array. The Offset is incremented by nBits.
	 * 
	 * @param buff
	 *            array to read from.
	 * @param offset
	 *            offset to the array.
	 * @return the value read.
	 */
	static final short readShort(byte[] buff, Offset offset) {
		short val;
		if (offset.bitOffset == 0) {
			val = (short) ((buff[offset.byteOffset] & 0xff) | ((buff[offset.byteOffset + 1] & 0xff) << 8));
		} else {
			val = (short) ((((buff[offset.byteOffset] & 0xff)
					| ((buff[offset.byteOffset + 1] & 0xff) << 8) | ((buff[offset.byteOffset + 2] & 0xff) << 16)) >>> offset.bitOffset) & 0x0000ffff);
		}
		offset.add(16);
		return val;
	}

	/**
	 * Reads an int from the array.
	 * 
	 * @param buff
	 *            array to read from.
	 * @param byteOffset
	 *            offset to the array.
	 * @param bitOffset
	 *            offset to the array.
	 * @return the value read.
	 */
	static int readInt(byte[] buff, int byteOffset, int bitOffset) {
		if (bitOffset == 0) {
			return (buff[byteOffset] & 0xff) | ((buff[byteOffset + 1] & 0xff) << 8)
					| ((buff[byteOffset + 2] & 0xff) << 16)
					| ((buff[byteOffset + 3] & 0xff) << 24);
		}
		return (int) ((((buff[byteOffset] & 0xffL)
				| ((buff[byteOffset + 1] & 0xffL) << 8)
				| ((buff[byteOffset + 2] & 0xffL) << 16)
				| ((buff[byteOffset + 3] & 0xffL) << 24) | ((buff[byteOffset + 4] & 0xffL) << 32)) >> bitOffset) & 0xffffffffL);

	}

	/**
	 * Reads an int from the array. The Offset is incremented by nBits.
	 * 
	 * @param buff
	 *            array to read from.
	 * @param offset
	 *            offset to the array.
	 * @return the value read.
	 */
	static int readInt(byte[] buff, Offset offset) {
		int val;
		if (offset.bitOffset == 0) {
			val = (buff[offset.byteOffset] & 0xff)
					| ((buff[offset.byteOffset + 1] & 0xff) << 8)
					| ((buff[offset.byteOffset + 2] & 0xff) << 16)
					| ((buff[offset.byteOffset + 3] & 0xff) << 24);
		} else {
			val = (int) ((((buff[offset.byteOffset] & 0xffL)
					| ((buff[offset.byteOffset + 1] & 0xffL) << 8)
					| ((buff[offset.byteOffset + 2] & 0xffL) << 16)
					| ((buff[offset.byteOffset + 3] & 0xffL) << 24) | ((buff[offset.byteOffset + 4] & 0xffL) << 32)) >> offset.bitOffset) & 0xffffffffL);
		}
		offset.add(32);
		return val;
	}

	/**
	 * Reads an unsigned int of maximal 31 bits from the array. For performance reasons the
	 * array to be read from must have a 4 byte unused suffix, since typically this method reads
	 * more byte than needed and avoid the checks.
	 * 
	 * @param buff
	 *            buff array to read from.
	 * @param nBits
	 *            number of bits to read.
	 * @param byteOffset
	 *            offset to the array.
	 * @param bitOffset
	 *            offset to the array.
	 * @return the value read.
	 */
	static long readUInt(byte[] buff, int nBits, int byteOffset, int bitOffset) {
		if (bitOffset == 0) {
			return ((buff[byteOffset] & 0xff) | ((buff[byteOffset + 1] & 0xff) << 8)
					| ((buff[byteOffset + 2] & 0xff) << 16)
					| ((buff[byteOffset + 3] & 0xff) << 24)) & INT_HIGH_CLEARED[32 - nBits];
		}
		return (int) ((((buff[byteOffset] & 0xffL)
				| ((buff[byteOffset + 1] & 0xffL) << 8)
				| ((buff[byteOffset + 2] & 0xffL) << 16)
				| ((buff[byteOffset + 3] & 0xffL) << 24) | ((buff[byteOffset + 4] & 0xffL) << 32)) >>> bitOffset) & INT_HIGH_CLEARED[32 - nBits]);
	}

	/**
	 * Reads an unsigned int of maximal 31 bits from the array. For performance reasons the
	 * array to be read from must have a 4 byte unused suffix, since typically this method reads
	 * more byte than needed and avoid the checks. The Offset is incremented by nBits.
	 * 
	 * @param buff
	 *            buff array to read from
	 * @param nBits
	 *            number of bits to read
	 * @param offset
	 *            offset to the array.
	 * @return the value read.
	 */
	static int readUInt(byte[] buff, int nBits, Offset offset) {
		int val;
		if (offset.bitOffset == 0) {
			val = ((buff[offset.byteOffset] & 0xff)
					| ((buff[offset.byteOffset + 1] & 0xff) << 8)
					| ((buff[offset.byteOffset + 2] & 0xff) << 16)
					| ((buff[offset.byteOffset + 3] & 0xff) << 24))
					& INT_HIGH_CLEARED[32 - nBits];
		} else {
			val = (int) ((((buff[offset.byteOffset] & 0xffL)
					| ((buff[offset.byteOffset + 1] & 0xffL) << 8)
					| ((buff[offset.byteOffset + 2] & 0xffL) << 16)
					| ((buff[offset.byteOffset + 3] & 0xffL) << 24) | ((buff[offset.byteOffset + 4] & 0xffL) << 32)) >>> offset.bitOffset) & INT_HIGH_CLEARED[32 - nBits]);

		}
		offset.add(nBits);
		return val;
	}
}
