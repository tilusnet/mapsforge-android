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
package org.mapsforge.preprocessing.poi.osmosis;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

public class POIWriterFactory extends TaskManagerFactory {

	public POIWriterFactory() {
	}

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		// Output file
		String outputFilePath = getStringArgument(taskConfig, "file", System.getProperty("user.home")
				+ "/map.pbf");

		String categoryConfigFilePath = getStringArgument(taskConfig, "categoryConfigPath", "POICategoriesOsmosis.xml");

		// The creation task
		Sink task = new POIWriterTask(outputFilePath, categoryConfigFilePath);

		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}

}
