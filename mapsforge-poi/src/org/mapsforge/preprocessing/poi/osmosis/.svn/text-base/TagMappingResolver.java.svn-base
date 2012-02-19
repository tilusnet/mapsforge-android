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

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mapsforge.preprocessing.poi.osmosis.jaxb.Category;
import org.mapsforge.preprocessing.poi.osmosis.jaxb.Mapping;
import org.mapsforge.storage.poi.PoiCategory;
import org.mapsforge.storage.poi.PoiCategoryManager;
import org.mapsforge.storage.poi.UnknownPoiCategoryException;

/**
 * This class maps a given tag (e.g. amenity=restaurant) to a certain {@link PoiCategory}. The mapping
 * configuration is read from a XML file.
 * 
 * @author Karsten Groll
 * 
 */
class TagMappingResolver {
	private static final Logger LOGGER = Logger.getLogger(POIWriterTask.class.getName());
	private final PoiCategoryManager categoryManager;

	/** Maps a tag to a category's title */
	private HashMap<String, String> tagMap;

	/**
	 * 
	 * @param configFilePath
	 *            Path to the XML file containing the tag to POI mappings.
	 * @param categoryManager
	 *            The category manager for loading a category tree.
	 */
	TagMappingResolver(String configFilePath, PoiCategoryManager categoryManager) {
		this.categoryManager = categoryManager;
		this.tagMap = new HashMap<String, String>();

		// Read root category from XML
		final File f = new File(configFilePath);

		JAXBContext ctx = null;
		Unmarshaller um = null;
		Category xmlRootCategory = null;

		try {
			ctx = JAXBContext.newInstance(Category.class);
			um = ctx.createUnmarshaller();
			xmlRootCategory = (Category) um.unmarshal(f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		LOGGER.info("Adding tag mappings...");
		Stack<Category> categories = new Stack<Category>();
		categories.push(xmlRootCategory);

		while (!categories.isEmpty()) {
			for (Category c : categories.pop().getCategory()) {
				categories.push(c);

				for (Mapping m : c.getMapping()) {
					LOGGER.finer("'" + m.getTag() + "' ==> '" + c.getTitle() + "'");
					this.tagMap.put(m.getTag(), c.getTitle());
				}

			}
		}
	}

	PoiCategory getCategoryFromTag(String tag) throws UnknownPoiCategoryException {
		String categoryName = this.tagMap.get(tag);
		// Tag not found?
		if (categoryName == null) {
			return null;
		}

		PoiCategory ret = this.categoryManager.getPoiCategoryByTitle(categoryName);

		return ret;
	}
}
