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

package org.mapsforge.routing.preprocessing.mm.osmosis;

import java.io.FileNotFoundException;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * @author Michael Bartel, Frank 
 */


public class MultiRoutingGraphWriterFactory extends TaskManagerFactory{

	private final static String DEFAULT_PARAM_MULTIWAY_TYPES = "motorway,motorway_link,trunk,trunk_link,primary,primary_link,secondary,secondary_link,tertiary,unclassified,road,residential,living_street,rail,light_rail,narrow_gauge,tram,monorail,funicular,subway";
	private final static String DEFAULT_PARAM_OUTPUT_FILE = "multiRoutingGraph.sql";

	private final static String PARAM_MULTIWAY_TYPES = "multiway-types";
	private final static String PARAM_OUTPUT_FILE = "file";

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String multiWayTypes = getStringArgument(taskConfig, PARAM_MULTIWAY_TYPES,
				DEFAULT_PARAM_MULTIWAY_TYPES);
		
		
		String outputFile = getStringArgument(taskConfig, PARAM_OUTPUT_FILE,
				DEFAULT_PARAM_OUTPUT_FILE);
		try {
			return new SinkManager(
					taskConfig.getId(),
					new MultiRoutingGraphWriterTask(multiWayTypes, outputFile),
					taskConfig.getPipeArgs());
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("invalid output file");
		}
	}

}
