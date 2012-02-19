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
package org.mapsforge.storage.poi;

import java.util.HashMap;
import java.util.TreeMap;

import org.sqlite.android.Database;
import org.sqlite.android.Exception;
import org.sqlite.android.Stmt;


/**
 * A category manager that reads and stores categories from a SQLite database using the Android SQLite3
 * wrapper. (This class can only be used within Android.)
 * 
 * @author Karsten Groll
 * 
 */
public class AndroidPoiCategoryManager implements PoiCategoryManager {
	// private static final String LOG_TAG = "mapformat";
	// This warning can be ignored. This object IS in use within this class.
	private Database db = null;
	private Stmt loadCategoriesStatement = null;
	private PoiCategory rootCategory = null;
	private TreeMap<Integer, PoiCategory> categoryMap = null;

	/**
	 * 
	 * @param db
	 *            SQLite3 database object. (Using SQLite wrapper for Android.)
	 */
	public AndroidPoiCategoryManager(Database db) {
		// Log.d(LOG_TAG, "Initializing category manager");
		this.db = db;
		this.categoryMap = new TreeMap<Integer, PoiCategory>();

		try {
			this.loadCategoriesStatement = db.prepare("SELECT * FROM poi_categories ORDER BY id ASC;");
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadCategories();
	}

	private void loadCategories() {
		PoiCategory pc = null;

		// Column values
		int categoryID = 0;
		String categoryTitle = "";
		int categoryParentID = 0;

		// Maximum ID (for root node)
		int maxID = 0;

		// Maps POIs to their parents ID
		HashMap<PoiCategory, Integer> parentMap = new HashMap<PoiCategory, Integer>();

		try {
			this.loadCategoriesStatement.reset();
			while (this.loadCategoriesStatement.step()) {
				categoryID = this.loadCategoriesStatement.column_int(0);
				categoryTitle = this.loadCategoriesStatement.column_string(1);
				categoryParentID = this.loadCategoriesStatement.column_int(2);

				// Log.d(LOG_TAG, categoryID + "|" + categoryTitle + "|" + categoryParentID);

				pc = new DoubleLinkedPoiCategory(categoryTitle, null, categoryID);
				this.categoryMap.put(new Integer(categoryID), pc);

				// category --> parent ID
				parentMap.put(pc, new Integer(categoryParentID));

				// check for root node
				if (categoryID > maxID) {
					maxID = categoryID;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set root category
		this.rootCategory = getPoiCategoryByID(maxID);

		// Assign parent categories;
		for (PoiCategory c : parentMap.keySet()) {
			c.setParent(getPoiCategoryByID(parentMap.get(c)));
		}

	}

	@Override
	public PoiCategory getPoiCategoryByID(int id) {
		return this.categoryMap.get(new Integer(id));
	}

	@Override
	public PoiCategory getPoiCategoryByTitle(String title) {
		for (Integer key : this.categoryMap.keySet()) {
			if (this.categoryMap.get(key).getTitle().equalsIgnoreCase(title)) {
				return this.categoryMap.get(key);
			}
		}

		return null;
	}

	@Override
	public PoiCategory getRootCategory() {
		return this.rootCategory;
	}

}
