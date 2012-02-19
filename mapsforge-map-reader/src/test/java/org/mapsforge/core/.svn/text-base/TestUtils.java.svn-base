/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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
package org.mapsforge.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

final class TestUtils {
	static void equalsTest(Object object1, Object object2) {
		Assert.assertEquals(object1.hashCode(), object2.hashCode());
		Assert.assertEquals(object1, object2);
		Assert.assertEquals(object2, object1);
	}

	static void serializeTest(Object objectToSerialize) throws IOException, ClassNotFoundException {
		File file = File.createTempFile("object", ".ser");
		file.deleteOnExit();
		Assert.assertTrue(file.exists());
		Assert.assertEquals(0, file.length());

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(objectToSerialize);
		objectOutputStream.close();
		fileOutputStream.close();

		FileInputStream fileInputStream = new FileInputStream(file);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		Object deserializedObject = objectInputStream.readObject();

		objectInputStream.close();
		fileInputStream.close();

		TestUtils.equalsTest(objectToSerialize, deserializedObject);
	}

	private TestUtils() {
		throw new IllegalStateException();
	}
}
