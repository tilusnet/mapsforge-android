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

import java.util.ArrayList;

/**
 * @param <I>
 *            Class holding heap data.
 * @param <K>
 *            heap key, used for comparing heap items.
 * 
 *            Slow implementation of a Binary heap like presented by Cormen et. al.
 */
public class BinaryMinHeap<I extends IBinaryHeapItem<K>, K extends Comparable<K>> {

	private ArrayList<I> array;
	private int size;

	/**
	 * @param initialSize
	 *            initial size of the array.
	 */
	public BinaryMinHeap(int initialSize) {
		this.array = new ArrayList<I>(initialSize);
		this.size = 0;
	}

	/**
	 * @param item
	 *            to insert.
	 */
	public void insert(I item) {
		item.setHeapIndex(size);
		array.add(item);
		size++;
		moveUpward(size - 1);
	}

	/**
	 * @param item
	 *            its key will be decreased.
	 * @param newKey
	 *            new key value.
	 */
	public void decreaseKey(I item, K newKey) {
		if (newKey.compareTo(item.getHeapKey()) <= 0) {
			item.setHeapKey(newKey);
			moveUpward(item.getHeapIndex());
		}
	}

	/**
	 * @return the item with minimum key value
	 */
	public I extractMin() {
		if (size == 0)
			return null;

		I root;
		if (size == 1) {
			root = array.remove(0);
			size--;
		} else {
			root = array.get(0);
			I last = array.remove(size - 1);
			array.set(0, last);
			last.setHeapIndex(0);
			size--;
			minHeapify(last);
		}
		return root;
	}

	/**
	 * removes all items from this heap.
	 */
	public void clear() {
		this.array.clear();
		this.size = 0;
	}

	/**
	 * @return true if siye is 0.
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * @return number of items in this heap.
	 */
	public int size() {
		return this.size;
	}

	private void minHeapify(I item) {
		int left = left(item.getHeapIndex());
		int right = right(item.getHeapIndex());
		I min = item;
		I l, r;
		l = r = null;
		if (left < size) {
			l = array.get(left);
		}
		if (right < size) {
			r = array.get(right(item.getHeapIndex()));
		}
		if (l != null && l.getHeapKey().compareTo(min.getHeapKey()) < 0) {
			min = l;
		}
		if (r != null && r.getHeapKey().compareTo(min.getHeapKey()) < 0) {
			min = r;
		}
		if (min != item) {
			swap(min, item);
			minHeapify(item);
		}
	}

	private void moveUpward(int idx) {
		if (idx > 0) {
			I parent = array.get(parent(idx));
			I child = array.get(idx);
			if (child.getHeapKey().compareTo(parent.getHeapKey()) < 0) {
				swap(parent, child);
				moveUpward(parent(idx));
			}
		}
	}

	private void swap(I a, I b) {
		array.set(a.getHeapIndex(), b);
		array.set(b.getHeapIndex(), a);
		int idxA = a.getHeapIndex();
		a.setHeapIndex(b.getHeapIndex());
		b.setHeapIndex(idxA);
	}

	private int parent(int idx) {
		return ((idx + 1) / 2) - 1;
	}

	private int left(int idx) {
		return ((idx + 1) * 2) - 1;
	}

	private int right(int idx) {
		return ((idx + 1) * 2) + 1 - 1;
	}
}
