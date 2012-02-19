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
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation.util.prioQueue;

/**
 * To be used with the binary heap implementation. Each heap item can only be added to one heap.
 * 
 * @param <K>
 *            class of the key
 */
public interface IBinaryHeapItem<K extends Comparable<K>> {

	/**
	 * @return index within the array of the heap.
	 */
	public int getHeapIndex();

	/**
	 * Must only be used by the heap while element is enqueued.
	 * 
	 * @param idx
	 *            position within the array based heap.
	 */
	public void setHeapIndex(int idx);

	/**
	 * Must only be used by the heap while element is enqueued.
	 * 
	 * @param key
	 *            key of the heap item.
	 */
	public void setHeapKey(K key);

	/**
	 * @return the key of this item.
	 */
	public K getHeapKey();
}
