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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * The basic implementation of the automatic execution of the mapsforge preprocessing.
 */
public class OsmosisRunner {

	// private static Logger LOGGER =
	// Logger.getLogger(OsmosisRunner.class.getName());
	private static String NEWLINE = System.getProperty("line.separator");
	private static ArrayList<String> MD5LIST = new ArrayList<String>();

	private static File workingDirectory;
	private static File loggingDirectory;
	private static File outputDirectory;

	private static File osmosisExecutable;

	/**
	 * This is starting point of the automatic execution tool of the mapsforge preprocessing. Here the xml file would be
	 * parsed, the osmosis calls of update and the pipeline execution would be generated and started. The MD5 file
	 * generation and the moving of the output folder to the destination would be handeled.
	 * 
	 * @param args
	 *            the arguments of the runner.
	 */
	public static void main(String[] args) {
		OptionParser optionParser = new OptionParser();
		OptionSpec<File> inputSpec = optionParser.accepts("input").withRequiredArg().ofType(File.class).required();

		OptionSet options = null;
		try {
			options = optionParser.parse(args);
		} catch (Exception e) {
			try {
				optionParser.printHelpOn(System.out);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}

		File inputFile = options.valueOf(inputSpec);

		JAXBContext jc = null;
		Unmarshaller unmarshaller = null;
		try {
			jc = JAXBContext.newInstance("org.mapsforge.map.writer.automatization");
			unmarshaller = jc.createUnmarshaller();
			JAXBElement<Configuration> conf = null;
			conf = (JAXBElement<Configuration>) unmarshaller.unmarshal(inputFile);

			workingDirectory = FileOperation.validateAndCreateDirectoryHandle(conf.getValue().getWorkingDir());
			File osmosisHomeDir = FileOperation.validateAndCreateDirectoryHandle(conf.getValue().getOsmosisHome());
			osmosisExecutable = FileOperation.createExecutionFile(osmosisHomeDir, "bin/osmosis");
			loggingDirectory = FileOperation.createDirectory(workingDirectory, conf.getValue().getLoggingDir());
			outputDirectory = FileOperation.createDirectory(workingDirectory, conf.getValue().getOutputDir());

			final boolean move = conf.getValue().isMove();

			// get all pipelines of the xml file
			final List<Pipeline> pipelines = conf.getValue().getPipeline();

			runPipelines(pipelines);
			createMD5Files();

			// all pipelines are done. now we move or copy the generated files to the destination directory
			String directory = conf.getValue().getDestinationDir();
			if (directory != null && directory != "") {
				// destinationDir
				File destinationDirFile = null;
				destinationDirFile = FileOperation.createDirectory(workingDirectory, directory);

				updateDestination(destinationDirFile, move);

			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final JAXBException e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method creates the osmosis calls for each pipeline and execute them. The output of the execution would be
	 * written o a log file with the name of the pipeline.
	 */
	private static void runPipelines(List<Pipeline> pipelines) throws IOException, InterruptedException {
		String call;
		File logfile;
		Process process;
		// run pipelines
		for (final Pipeline pipeline : pipelines) {

			// traverse the XML tree to the leafs to update the output directory of the sinks
			// and generate their osmosis calls. also registered the file for what a md5 file
			// should generated.
			call = osmosisExecutable.getAbsolutePath() + " "
					+ pipeline.generate(MD5LIST, workingDirectory.getAbsolutePath(), outputDirectory.getAbsolutePath());

			// run the osmosis task of the pipeline
			process = Runtime.getRuntime().exec(call, null, workingDirectory);

			// log the output of the osmosis task to a file called like the name of the pipeline
			logfile = FileOperation.createWriteFile(loggingDirectory.getAbsolutePath(), pipeline.getName() + ".log");

			writeLog(logfile.getAbsolutePath(), process, true);

			// wait for process and check the termination
			if (process.waitFor() != 0) {
				System.out.println("osmosis process exited with error");
				return;
			}

		}
	}

	/*
	 * This method is moving or is coping the files from the output directory to the destination directory.
	 */
	private static void updateDestination(File destinationDirFile, boolean move) throws IOException {

		FileUtils.copyDirectory(outputDirectory, destinationDirFile, true);
		if (move) {
			FileUtils.deleteDirectory(outputDirectory);
		}
	}

	/*
	 * This method create MD5 checksums of files an save them to an MD5 file. This file is called like the input file,
	 * only ends with ".md5". Attention: This method use the unix program md5sum, therefore it only works on unix
	 * system, that has installed this program.
	 */
	private static void createMD5Files() throws IOException {
		// for each file of the MD5List a file would be generated
		for (final String file : MD5LIST) {
			File mapFile = new File(file);
			File md5File = new File(mapFile.getParentFile(), mapFile.getName() + ".md5");

			byte[] md5 = DigestUtils.md5(new FileInputStream(mapFile));
			FileUtils.writeByteArrayToFile(md5File, md5);
		}
	}

	/*
	 * This method writes error logs of a process to file.
	 */
	private static void writeLog(String logfile, Process process, boolean append) {

		BufferedWriter bw = null;
		BufferedReader br = null;
		try {
			bw = new BufferedWriter(new FileWriter(logfile, append));
			br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line = null;

			while ((line = br.readLine()) != null) {
				bw.append(line).append(NEWLINE);
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
