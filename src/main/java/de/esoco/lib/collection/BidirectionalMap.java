//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a bidirectional mapping between keys and values. Internally
 * this mapping is realized with two instances of standard one-way maps, one for
 * the normal key-to-value mapping and another for the reverse mapping. Like the
 * standard maps from the collections package this implementation performs no
 * thread synchronization. For the methods of the standard map interface
 * thread-safety can be achieved by wrapping a bidirectional map with the method
 * {@link java.util.Collections#synchronizedMap(java.util.Map)}. For the
 * extended bidirectional methods an application must implement thread-safe
 * modifications by itself.
 *
 * <p>
 * <b>Attention:</b> This implementation is NOT safe for modifications by
 * element views (like {@link #keySet()}, {@link #values()}, or {@link
 * #entrySet()}) or iterators of these. Modifying a bidirectional map through
 * such a view may cause unpredictable behavior. Applications should always use
 * the direct modifying methods of this class instead.
 * </p>
 *
 * @author eso
 */
public class BidirectionalMap<K, V> implements Map<K, V> {
	private final Map<K, V> keyToValueMap;

	private final Map<V, K> valueToKeyMap;

	/**
	 * Creates a new bidirectional map instance that uses instances of {@link
	 * HashMap} for both mapping directions.
	 */
	public BidirectionalMap() {
		keyToValueMap = new HashMap<K, V>();
		valueToKeyMap = new HashMap<V, K>();
	}

	/**
	 * Creates a new bidirectional map instance with (optionally) distinct map
	 * types for the different mapping directions.
	 *
	 * @param keyToValueMapType The class of the internal map for the
	 *                          key-to-value mapping
	 * @param valueToKeyMapType The class of the internal map for the
	 *                          value-to-key mapping
	 */
	public BidirectionalMap(Class<? extends Map<K, V>> keyToValueMapType,
		Class<? extends Map<V, K>> valueToKeyMapType) {
		try {
			keyToValueMap = keyToValueMapType.getConstructor().newInstance();
			valueToKeyMap = valueToKeyMapType.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid map class", e);
		}
	}

	/**
	 * @see Map#clear()
	 */
	@Override
	public void clear() {
		keyToValueMap.clear();
		valueToKeyMap.clear();
	}

	/**
	 * @see Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		return keyToValueMap.containsKey(key);
	}

	/**
	 * For better performance this lookup is done on the reverse mapping.
	 *
	 * @see Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return valueToKeyMap.containsKey(value);
	}

	/**
	 * @see Map#entrySet()
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return keyToValueMap.entrySet();
	}

	/**
	 * @see Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		return keyToValueMap.get(key);
	}

	/**
	 * Returns the key for a value from the reverse mapping.
	 *
	 * @param value An arbitrary value object to get the key for
	 * @return The key for the given value or NULL if the value does not exist
	 */
	public K getKey(Object value) {
		return valueToKeyMap.get(value);
	}

	/**
	 * @see Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return keyToValueMap.isEmpty();
	}

	/**
	 * @see Map#keySet()
	 */
	@Override
	public Set<K> keySet() {
		return keyToValueMap.keySet();
	}

	/**
	 * Adds the key/value pair to the standard and reverse mappings. Because
	 * mapping keys must be unique an existing mapping will be replaced with
	 * the
	 * new one. This will happen in both mapping directions so that it is
	 * possible that two mappings are affected by a single put operation and
	 * the
	 * size of this bidirectional map decreases by one. For example, if the map
	 * contains two mappings (1, test1) and (2, test2) and a put(2, "test1") is
	 * invoked, the resulting map contents is (2, test1) because the mapping
	 * must be unique in BOTH directions. Therefore applications must make sure
	 * that added mappings are unique in both directions unless the desribed
	 * behavior is acceptable.
	 *
	 * @param key   The mapping key (for the standard mapping direction)
	 * @param value The mapping value (for the standard mapping direction)
	 * @return The first (in case two mappings are affected) value that has
	 * been
	 * replaced or NULL if it is a new entry
	 */
	@Override
	public V put(K key, V value) {
		V replacedValue = keyToValueMap.put(key, value);

		if (replacedValue != null) {
			valueToKeyMap.remove(replacedValue);
		}

		K replacedKey = valueToKeyMap.put(value, key);

		if (replacedKey != null) {
			keyToValueMap.remove(replacedKey);

			if (replacedValue == null) {
				replacedValue = value;
			}
		}

		return replacedValue;
	}

	/**
	 * @see Map#putAll(Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> e : map.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	/**
	 * @see Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		V removed = keyToValueMap.remove(key);

		if (removed != null) {
			valueToKeyMap.remove(removed);
		}

		return removed;
	}

	/**
	 * Return the entry set of the reverse mapping.
	 *
	 * @return A set view of the reverse mappings in this map
	 */
	public Set<Entry<V, K>> reverseEntrySet() {
		return valueToKeyMap.entrySet();
	}

	/**
	 * @see Map#size()
	 */
	@Override
	public int size() {
		return keyToValueMap.size();
	}

	/**
	 * Returns a string description of this instance.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[" + keyToValueMap + "," +
			valueToKeyMap + "]";
	}

	/**
	 * @see Map#values()
	 */
	@Override
	public Collection<V> values() {
		return valueToKeyMap.keySet();
	}
}
