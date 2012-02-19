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

import gnu.trove.list.TLinkable;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This Cache uses the Least Recently Used heuristic. It is implemented by a list of cache
 * items, which are always sorted by the time stamps of the last access to a each item.
 * PutItem() and getItem() both run in O(1), together with the clear() operation, each operation
 * takes O(1) amortized time.
 * 
 * @param <I>
 *            the type of items to be cached.
 */
final class LRUCache<I extends CacheItem> implements Cache<I> {
	/**
	 * list of cache items sorted by time stamp of last access.
	 */
	private final TLinkedList<ListNode<I>> list;
	/**
	 * map cache item id to list node.
	 */
	private final TIntObjectHashMap<ListNode<I>> map;
	/**
	 * the byte size threshold for this cache.
	 */
	private int cacheSizeBytes;
	/**
	 * the sum of byte sizes of all items currently cached in.
	 */
	private int currentSizeBytes;

	/**
	 * Construct a LRU Cache with the specified maximum size.
	 * 
	 * @param cacheSizeBytes
	 *            Threshold for byte size of the complete cache.
	 */
	public LRUCache(int cacheSizeBytes) {
		this.list = new TLinkedList<ListNode<I>>();
		this.map = new TIntObjectHashMap<ListNode<I>>();
		this.cacheSizeBytes = cacheSizeBytes;
		this.currentSizeBytes = 0;
	}

	@Override
	public void clear() {
		this.list.clear();
		this.map.clear();
		this.currentSizeBytes = 0;
	}

	@Override
	public synchronized I getItem(int id) {
		ListNode<I> ci = map.get(id);
		if (ci != null) {
			list.remove(ci);
			list.addFirst(ci);
			return ci.item;
		}

		return null;
	}

	@Override
	public synchronized void putItem(I item) {
		ListNode<I> ci = new ListNode<I>(item);
		list.addFirst(ci);
		map.put(item.getId(), ci);
		currentSizeBytes += item.getSizeBytes();
		while (currentSizeBytes > cacheSizeBytes) {
			ListNode<I> last = list.removeLast();
			map.remove(last.item.getId());
			currentSizeBytes -= last.item.getSizeBytes();
		}
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public int currentSizeBytes() {
		return currentSizeBytes;
	}

	@Override
	public int maxSizeBytes() {
		return cacheSizeBytes;
	}

	/**
	 * This way of implementing list nodes allows for fast removal and insertion when the list
	 * nodes are known. This is the key to provide each LRU operation in O(1).
	 * 
	 * @param <I>
	 *            obvious.
	 */
	private static class ListNode<I> implements TLinkable<ListNode<I>> {
		private static final long serialVersionUID = 1L;
		final I item;
		ListNode<I> next;
		ListNode<I> prev;

		public ListNode(I item) {
			this.item = item;
			this.next = null;
			this.prev = null;
		}

		@Override
		public ListNode<I> getNext() {
			return this.next;
		}

		@Override
		public ListNode<I> getPrevious() {
			return this.prev;
		}

		@Override
		public void setNext(ListNode<I> next) {
			this.next = next;
		}

		@Override
		public void setPrevious(ListNode<I> prev) {
			this.prev = prev;
		}
	}
}
