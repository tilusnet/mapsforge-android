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
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Wrapper for the serializable interface / object stream classes.
 */
public class Serializer {

	/**
	 * @param oStream
	 *            stream to write to.
	 * @param s
	 *            object to be written.
	 * @throws IOException
	 *             write error
	 */
	public static void serialize(OutputStream oStream, Serializable s) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(oStream);
		out.writeObject(s);
		out.flush();
	}

	/**
	 * @param dst
	 *            file to write to
	 * @param s
	 *            object to be written
	 * @throws IOException
	 *             write error
	 */
	public static void serialize(File dst, Serializable s) throws IOException {
		FileOutputStream oStream = new FileOutputStream(dst);
		serialize(oStream, s);
		oStream.close();
	}

	/**
	 * @param iStream
	 *            stream to read object from
	 * @return the read object.
	 * @throws IOException
	 *             read error.
	 * @throws ClassNotFoundException
	 *             cast error.
	 */
	public static Serializable deserialize(InputStream iStream)
			throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(iStream);
		Serializable s = (Serializable) in.readObject();
		return s;
	}

	/**
	 * @param src
	 *            file to read from
	 * @return the read object.
	 * @throws IOException
	 *             read error.
	 * @throws ClassNotFoundException
	 *             cast error.
	 */
	public static Serializable deserialize(File src) throws IOException,
			ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(src),
				64 * 1000 * 1024));
		Serializable s = (Serializable) in.readObject();
		in.close();
		return s;
	}

}
