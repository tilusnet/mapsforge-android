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

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This Cache implementation has the most simple strategy. No CacheItem will ever be removed
 * from the cache, thus the cache can only grow with regard to its size.
 * 
 * @param <I>
 *            The object type to be cached.
 */
final class DummyCache<I extends CacheItem> implements Cache<I> {

	private TIntObjectHashMap<I> map;
	private int sizeBytes;

	/**
	 * Create the cache.
	 */
	public DummyCache() {
		this.map = new TIntObjectHashMap<I>();
	}

	@Override
	public I getItem(int id) {
		return map.get(id);
	}

	@Override
	public void putItem(I item) {
		map.put(item.getId(), item);
		sizeBytes += item.getSizeBytes();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public int currentSizeBytes() {
		return sizeBytes;
	}

	@Override
	public int maxSizeBytes() {
		return Integer.MAX_VALUE;
	}

}
