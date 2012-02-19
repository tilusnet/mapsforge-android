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
package org.mapsforge.map.writer.automatization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class separates the handling of file access for automization component.
 */
public class FileOperation {

	/**
	 * Creates a file object to a directory that exists.
	 * 
	 * @param directory
	 *            the absolute path to the directory.
	 * @return a file object to the directory.
	 */
	public static File validateAndCreateDirectoryHandle(String directory) {

		File result = null;

		try {
			if (directory.startsWith(File.separator)) {
				result = new File(directory);
				if (!result.exists()) {
					throw new IllegalArgumentException("cannot find directory: " + result.getAbsolutePath());
				}
				if (!result.isDirectory()) {
					throw new IllegalArgumentException("directory is not a directory: " + result.getAbsolutePath());
				}
				if (!result.canRead()) {
					throw new IOException("Error! You have no permission to read in directory: "
							+ result.getAbsolutePath());
					/*
					 * if (!result.canWrite()) throw new IOException(
					 * "Error! You have no permission to write to working directory: " + result.getAbsolutePath());
					 */
				}
			} else {
				throw new IllegalArgumentException("File is not absolute: " + directory);
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return result;
	}

	/**
	 * Creates a file object to a directory and maybe created it to.
	 * 
	 * @param parent
	 *            the absolute path to parent directory.
	 * @param directory
	 *            the path of the directory to create.
	 * @return a file object to the created directory.
	 */
	public static File createDirectory(File parent, String directory) {
		File result = null;

		try {
			if (directory.startsWith(File.separator)) {
				result = new File(directory);
			} else {

				if (!parent.exists()) {
					throw new FileNotFoundException("No such directory: " + parent.getAbsolutePath());
				}
				if (!parent.isDirectory()) {
					throw new IOException("File is no directory: " + parent.getAbsolutePath());
				}

				result = new File(parent, directory);
			}

			if (!result.exists()) {
				if (!result.mkdirs()) {
					throw new IOException("Can not create directory: " + result.getAbsolutePath());
				}
			} else if (!result.isDirectory()) {
				throw new IOException("File is no directory: " + result.getAbsolutePath());
			}

			if (!result.canRead()) {
				throw new IOException("Can not read from directory: " + result.getAbsolutePath());
			}
			if (!result.canWrite()) {
				throw new IOException("Can not write to directory: " + result.getAbsolutePath());
			}

		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return result;
	}

	/**
	 * Creates a file object to a file for reading.
	 * 
	 * @param parent
	 *            the absolute path of the parent directory.
	 * @param file
	 *            the path to the file.
	 * @return a file object to a file for reading.
	 */
	public static File createReadFile(String parent, String file) {

		File result = null;

		try {
			if (file.startsWith(File.separator)) {
				result = new File(file);
			} else {
				result = new File(parent, file);
			}

			if (!result.exists()) {
				throw new FileNotFoundException("No such file or directory: " + result.getAbsolutePath());
			}
			if (!result.isFile()) {
				throw new IOException("File is directory: " + result.getAbsolutePath());
			}
			if (!result.canRead()) {
				throw new IOException("Can not read file: " + result.getAbsolutePath());
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return result;
	}

	/**
	 * Creates a file for writing.
	 * 
	 * @param parent
	 *            the absolute path of the parent directory.
	 * @param file
	 *            the path to the file.
	 * @return a file object to a file for writing.
	 */
	public static File createWriteFile(String parent, String file) {

		File result = null;

		try {
			if (file.startsWith(File.separator)) {
				result = new File(file);
			} else {
				result = new File(parent, file);
			}

			final File parentFile = result.getParentFile();
			if (!parentFile.exists()) {
				if (!parentFile.mkdirs()) {
					throw new IOException("Can not create directory: " + parentFile.getAbsolutePath());
				} else if (!parentFile.canWrite()) {
					throw new IOException("Can not write to directory: " + parentFile.getAbsolutePath());
				}
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return result;
	}

	/**
	 * Creates a file object to a file for reading and writing.
	 * 
	 * @param parent
	 *            the absolute path of the parent directory.
	 * @param file
	 *            the path to the file.
	 * @return a file object to a file for reading and writing.
	 */
	public static File createReadWriteFile(String parent, String file) {

		File result = null;

		try {
			if (file.startsWith(File.separator)) {
				result = new File(file);
			} else {
				result = new File(parent, file);
			}

			if (!result.exists()) {
				throw new FileNotFoundException("No such file or directory: " + result.getAbsolutePath());
			}
			if (!result.isFile()) {
				throw new IOException("File is directory: " + result.getAbsolutePath());
			}
			if (!result.canRead()) {
				throw new IOException("Can not read file: " + result.getAbsolutePath());
			}
			if (!result.canWrite()) {
				throw new IOException("Can not write to file: " + result.getAbsolutePath());
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return result;
	}

	/**
	 * Creates a file object to a file for execution.
	 * 
	 * @param parent
	 *            the absolute path of the parent directory.
	 * @param file
	 *            the path to the file.
	 * @return a file object to a file for execution.
	 */
	public static File createExecutionFile(File parent, String file) {
		File result = null;

		try {
			if (file.startsWith(File.separator)) {
				result = new File(file);
			} else {
				result = new File(parent, file);
			}

			if (!result.exists()) {
				throw new FileNotFoundException("No such file or directory: " + result.getAbsolutePath());
			}
			if (!result.isFile()) {
				throw new IOException("File is directory: " + result.getAbsolutePath());
			}
			if (!result.canExecute()) {
				throw new IOException("Can not execute file: " + result.getAbsolutePath());
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return result;
	}

}
