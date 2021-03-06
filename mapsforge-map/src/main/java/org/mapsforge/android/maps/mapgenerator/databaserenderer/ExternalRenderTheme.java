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
package org.mapsforge.android.maps.mapgenerator.databaserenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.mapsforge.android.maps.mapgenerator.JobTheme;

/**
 * An ExternalRenderTheme allows for customizing the rendering style of the map via an XML file.
 */
public class ExternalRenderTheme implements JobTheme {
	private static final long serialVersionUID = 1L;

	private final long fileModificationDate;
	private transient int hashCodeValue;
	private final String renderThemePath;

	/**
	 * @param renderThemePath
	 *            the path to the XML render theme file.
	 * @throws FileNotFoundException
	 *             if the file does not exist or cannot be read.
	 */
	public ExternalRenderTheme(String renderThemePath) throws FileNotFoundException {
		File renderThemeFile = new File(renderThemePath);
		if (!renderThemeFile.exists()) {
			throw new FileNotFoundException("file does not exist: " + renderThemePath);
		} else if (!renderThemeFile.isFile()) {
			throw new FileNotFoundException("not a file: " + renderThemePath);
		} else if (!renderThemeFile.canRead()) {
			throw new FileNotFoundException("cannot read file: " + renderThemePath);
		}

		this.fileModificationDate = renderThemeFile.lastModified();
		if (this.fileModificationDate == 0L) {
			throw new FileNotFoundException("cannot read last modification time");
		}
		this.renderThemePath = renderThemePath;
		calculateTransientValues();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof ExternalRenderTheme)) {
			return false;
		}
		ExternalRenderTheme other = (ExternalRenderTheme) obj;
		if (this.fileModificationDate != other.fileModificationDate) {
			return false;
		} else if (this.renderThemePath == null && other.renderThemePath != null) {
			return false;
		} else if (this.renderThemePath != null && !this.renderThemePath.equals(other.renderThemePath)) {
			return false;
		}
		return true;
	}

	@Override
	public InputStream getRenderThemeAsStream() throws FileNotFoundException {
		return new FileInputStream(this.renderThemePath);
	}

	@Override
	public int hashCode() {
		return this.hashCodeValue;
	}

	/**
	 * @return the hash code of this object.
	 */
	private int calculateHashCode() {
		int result = 1;
		result = 31 * result + (int) (this.fileModificationDate ^ (this.fileModificationDate >>> 32));
		result = 31 * result + ((this.renderThemePath == null) ? 0 : this.renderThemePath.hashCode());
		return result;
	}

	/**
	 * Calculates the values of some transient variables.
	 */
	private void calculateTransientValues() {
		this.hashCodeValue = calculateHashCode();
	}

	private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
		objectInputStream.defaultReadObject();
		calculateTransientValues();
	}
}
