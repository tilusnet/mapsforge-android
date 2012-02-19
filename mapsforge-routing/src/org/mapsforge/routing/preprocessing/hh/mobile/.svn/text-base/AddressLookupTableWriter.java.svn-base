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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class AddressLookupTableWriter {

	private final static int MIN_G_SIZE = 5;

	public static void writeTable(int[] blockSize, int gSizeThreshold, File targetFile)
			throws IOException {

		int bestByteSize = Integer.MAX_VALUE;
		byte[] bestSerialization = null;

		// determine space optimal group Size
		for (int gSize = MIN_G_SIZE; gSize < gSizeThreshold || gSize == MIN_G_SIZE; gSize++) {

			// compute the compressed table data
			int numBlocks = blockSize.length;
			long[] gByteSize = getGroupByteSize(blockSize, gSize);
			long[] gStartAddr = getGroupStartAddr(gByteSize);

			int[] gMaxDiff = getGroupMaxSucceedingBlockSizeDiff(blockSize, gSize);
			byte[] gEncBits = getGroupEncBits(gMaxDiff);

			int numG = getNumGroups(blockSize.length, gSize);
			int[] gBlockEncOffs = new int[numG];
			int[] gFirstBlockSize = new int[numBlocks];
			byte[] encBlockSize = encodeBlockSizes(gEncBits, blockSize, gSize, gBlockEncOffs,
					gFirstBlockSize);

			// serialize
			ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(arrayOut);

			out.writeInt(gSize);
			out.writeInt(numBlocks);

			out.writeInt(gStartAddr.length);
			for (int i = 0; i < gStartAddr.length; i++) {
				out.writeLong(gStartAddr[i]);
				out.writeInt(gBlockEncOffs[i]);
				out.writeInt(gFirstBlockSize[i]);
				out.writeByte(gEncBits[i]);
			}

			out.writeInt(encBlockSize.length);
			out.write(encBlockSize);
			out.flush();
			out.close();
			arrayOut.close();

			// does the group size yield best compression ratio?
			if (out.size() < bestByteSize) {
				bestSerialization = arrayOut.toByteArray();
			}
		}
		FileOutputStream out = new FileOutputStream(targetFile);
		out.write(bestSerialization);
		out.close();
	}

	/**
	 * 
	 * @param gEncBits
	 *            number of bits used for encoding a single entry. gEncBits[i] determines the
	 *            number of bits per entry used for the i th group.
	 * @param blockSize
	 *            sizes sorted ascending.
	 * @param gSize
	 *            number of pointers per group.
	 * @param gBlockEncOffsBuff
	 *            the start addresses of each group are written here.
	 * @param gFirstBlockSizeBuff
	 *            the sizes of the first pointer of each group are put here.
	 * @return run length encoded block sizes.
	 */
	private static byte[] encodeBlockSizes(byte[] gEncBits, int[] blockSize, int gSize,
			int[] gBlockEncOffsBuff, int[] gFirstBlockSizeBuff) {
		try {
			byte[] buff = new byte[blockSize.length * 32];
			BitArrayOutputStream stream = new BitArrayOutputStream(buff);

			for (int i = 0; i < blockSize.length; i++) {

				if (i % gSize == 0) {
					// the first element has always difference = 0
					stream.alignPointer(1);

					gBlockEncOffsBuff[i / gSize] = stream.getByteOffset();
					gFirstBlockSizeBuff[i / gSize] = blockSize[i];

					// no entry for first element of each group!! would always be zero
				} else {
					stream.writeUInt(blockSize[i] - blockSize[i - 1], gEncBits[i / gSize]);
				}
			}
			stream.alignPointer(1);

			byte[] result = new byte[stream.getByteOffset()];
			for (int i = 0; i < result.length; i++) {
				result[i] = buff[i];
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Got impossible error!");
		}
	}

	/**
	 * Computes the number of bits required for encoding an entry of the respective group.
	 * 
	 * @param gMaxDiff
	 *            gMaxDiff[i] contains the maximum difference of two succeeding entries of the i
	 *            th group.
	 * @return number of bits to encode for each group.
	 */
	private static byte[] getGroupEncBits(int[] gMaxDiff) {
		byte[] gEncBits = new byte[gMaxDiff.length];
		for (int i = 0; i < gMaxDiff.length; i++) {
			gEncBits[i] = numBitsToEncode(0, gMaxDiff[i]);
		}
		return gEncBits;
	}

	/**
	 * How many bits needed to encode values of the given range.
	 * 
	 * @param minVal
	 *            minimum value
	 * @param maxVal
	 *            maximum value
	 * @return number of bits required.
	 */
	private static byte numBitsToEncode(int minVal, int maxVal) {
		int interval = maxVal - minVal;
		return (byte) (Math.floor(Math.log(interval) / Math.log(2)) + 1);
	}

	/**
	 * Computes start addresses of each group.
	 * 
	 * @param gByteSize
	 *            gByteSize[i] determines the number of bytes to encode the i th group.
	 * @return start addresses of each group.
	 */
	private static long[] getGroupStartAddr(long[] gByteSize) {
		long[] gStartAddr = new long[gByteSize.length];
		gStartAddr[0] = 0;
		for (int i = 0; i < gByteSize.length - 1; i++) {
			gStartAddr[i + 1] = gStartAddr[i] + gByteSize[i];
		}
		return gStartAddr;
	}

	/**
	 * Number of bytes required for encoding each group.
	 * 
	 * @param blockSize
	 *            block sizes sorted ascending.
	 * @param gSize
	 *            number of entries per group.
	 * @return number of bytes required for each group.
	 */
	private static long[] getGroupByteSize(int[] blockSize, int gSize) {
		int numG = getNumGroups(blockSize.length, gSize);
		long[] gByteSize = new long[numG];
		for (int i = 0; i < numG; i++) {
			gByteSize[i] = sum(blockSize, i * gSize, Math.min((i + 1) * gSize,
					blockSize.length));
		}
		return gByteSize;
	}

	/**
	 * Computes the maximum difference of succeeding entries for each group.
	 * 
	 * @param blockSize
	 *            block sizes sorted ascending.
	 * @param gSize
	 *            number of entries per group.
	 * @return maximum difference within each group.
	 */
	private static int[] getGroupMaxSucceedingBlockSizeDiff(int[] blockSize, int gSize) {
		int numG = getNumGroups(blockSize.length, gSize);
		int[] maxDiffs = new int[numG];
		for (int i = 0; i < numG; i++) {
			maxDiffs[i] = maxSucceedingDiff(blockSize, i * gSize, Math.min((i + 1) * gSize,
					blockSize.length));
		}
		return maxDiffs;
	}

	/**
	 * Helper function for getGroupMaxSucceedingBlockSizeDiff.
	 * 
	 * @param arr
	 *            the values.
	 * @param start
	 *            start index.
	 * @param end
	 *            end index.
	 * @return maximum difference of succeeding array entries within the given bounds.
	 */
	private static int maxSucceedingDiff(int[] arr, int start, int end) {
		int maxDiff = 0;
		for (int i = start + 1; i < end; i++) {
			maxDiff = Math.max(maxDiff, arr[i] - arr[i - 1]);
		}
		return maxDiff;
	}

	/**
	 * Computes the number of groups.
	 * 
	 * @param numBlocks
	 *            number of entries to index.
	 * @param gSize
	 *            entries per group.
	 * @return number of groups to index.
	 */
	private static int getNumGroups(int numBlocks, int gSize) {
		int numG = (int) Math.ceil(((double) numBlocks) / ((double) gSize));
		return numG;
	}

	/**
	 * sum the interval [start .. end)
	 * 
	 * @param arr
	 *            values to sum up
	 * @param start
	 *            index
	 * @param end
	 *            index
	 * @return sum of values
	 */
	public static long sum(int[] arr, int start, int end) {
		if (arr.length == 0) {
			return 0;
		}
		long sum = 0;
		for (int i = start; i < end; i++) {
			sum += arr[i];
		}
		return sum;
	}
}
