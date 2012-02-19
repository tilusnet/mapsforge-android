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
package org.mapsforge.routing.android.hh;

/**
 * An Interface for simple caching functionality. Each Cache item must have a unique identifier.
 * 
 * @param <I>
 *            The Type of items to be cached.
 */
interface Cache<I extends CacheItem> {

	/**
	 * Looks up a Cache Item.
	 * 
	 * @param id
	 *            The identifier of the cache Item to lookup.
	 * @return Returns the desired cache item, or null if the specified item is not present in
	 *         this cache.
	 */
	public I getItem(int id);

	/**
	 * Adds an item to the cache, this may result in removing other items, if the cache is full.
	 * 
	 * @param item
	 *            the item to add to the cache.
	 */
	public void putItem(I item);

	/**
	 * Removes all items from the cache.
	 */
	public void clear();

	/**
	 * @return number of cache items in cache
	 */
	public int size();

	/**
	 * @return byte siye of all items in cache.
	 */
	public int currentSizeBytes();

	public int maxSizeBytes();

}
