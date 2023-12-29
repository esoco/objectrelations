//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.datatype.Pair;
import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.CollectionFunctions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.text.TextUtil;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.filter.RelationComparator;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions for the handling of collections.
 *
 * @author eso
 */
public class CollectionUtil {
	private static final BinaryFunction<?, ?, ?> COLLECT =
		CollectionFunctions.collect(null);

	private static final BinaryFunction<?, ?, ?> FIND =
		CollectionFunctions.find(null);

	private static final BinaryFunction<?, ?, ?> MAP =
		CollectionFunctions.map(null);

	private static final BinaryFunction<?, ?, ?> CREATE_MAP =
		CollectionFunctions.createMap(null);

	/**
	 * A comparator constant that compares enum by their names.
	 */
	public static Comparator<Enum<?>> ENUM_COMPARATOR =
		new Comparator<Enum<?>>() {
			@Override
			public int compare(Enum<?> e1, Enum<?> e2) {
				return e1.name().compareTo(e2.name());
			}
		};

	/**
	 * Private constructor, only static use.
	 */
	private CollectionUtil() {
	}

	/**
	 * Adds the trailing argument objects into the argument collection and
	 * returns it (for convenience).
	 *
	 * @param collection The collection to add the values to
	 * @param values     The values to be added
	 * @return The argument collection
	 */
	@SafeVarargs
	public static <T, C extends Collection<? super T>> C add(C collection,
		T... values) {
		Collections.addAll(collection, values);

		return collection;
	}

	/**
	 * Adds a number of instances of a certain type to a collection.
	 *
	 * @param collection The collection to add the values to
	 * @param count      The number of values to add
	 * @param value      The value to add
	 * @return The argument collection
	 */
	public static <T, C extends Collection<? super T>> C add(C collection,
		int count, T value) {
		while (count-- > 0) {
			collection.add(value);
		}

		return collection;
	}

	/**
	 * Adds all objects returned by an Iterable instance into the argument
	 * collection and returns it (for convenience).
	 *
	 * @param collection The collection to add the values to
	 * @param values     The values to be added
	 * @return The collection
	 */
	public static <T, C extends Collection<T>> C addAll(C collection,
		Iterable<T> values) {
		for (T value : values) {
			collection.add(value);
		}

		return collection;
	}

	/**
	 * Parses a single map entry and adds it to a map. An existing entry with
	 * the same key will be replaced. The string to be parsed must either have
	 * the format &lt;key&gt;=&lt;value&gt; or &lt;key&gt;:&lt;value&gt;.
	 *
	 * <p>
	 * The key and value elements will be parsed by means of the method
	 * {@link TextUtil#parseObject(String)}, which will throw an
	 * IllegalArgumentException on invalid elements. The key must have a length
	 * of at least one character, the value may be empty. See the documentation
	 * of {@link TextUtil#parseObject(String)}for allowed key and value formats
	 * and the corresponding data types. Any whitespace characters surrounding
	 * either the key or the value will be removed.
	 * </p>
	 *
	 * <p>
	 * The last arguments define the datatype classes of the key and value,
	 * respectively. The parsed key and value objects will be cast to this
	 * types
	 * before being inserted into the map. Therefore any type mismatch will
	 * cause a ClassCastException to be thrown.
	 * </p>
	 *
	 * @param targetMap The map to insert the parsed key/value pair into
	 * @param entry     The entry string to be parsed
	 * @param keyType   The datatype of the key
	 * @param valueType The datatype of the value
	 * @throws IllegalArgumentException If the parsing of the map entry fails
	 */
	public static <K, V> void addMapEntry(Map<K, V> targetMap, String entry,
		Class<K> keyType, Class<V> valueType) {
		Matcher matcher = splitMapEntry(entry);
		Object key = TextUtil.parseObject(matcher.group(1));
		Object value = TextUtil.parseObject(matcher.group(2));

		targetMap.put(keyType.cast(key), valueType.cast(value));
	}

	/**
	 * Applies a certain function to all elements of a collection.
	 *
	 * @param values   The collection of the elements to apply the function to
	 * @param function The function to apply to the elements
	 */
	public static <T> void apply(Collection<T> values,
		Function<? super T, ?> function) {
		for (T value : values) {
			function.evaluate(value);
		}
	}

	/**
	 * Collects each element from a collection for which the given predicate
	 * evaluates to TRUE into a new collection of the same type as the input
	 * collection. Uses a static instance of a function returned by the method
	 * {@link CollectionFunctions#collect(Predicate)}.
	 *
	 * @param collection The collection to collect elements from
	 * @param collect    The predicate to test the collection elements with
	 * @return A new collection containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C collect(C collection,
		Predicate<? super T> collect) {
		return ((BinaryFunction<C, Predicate<? super T>, C>) COLLECT).evaluate(
			collection, collect);
	}

	/**
	 * Collects all values from a map with certain keys into a new list.
	 *
	 * @param fromMap The map to collect the values from
	 * @param keys    A collection of map keys
	 * @return A new list containing the map values that match the given keys
	 */
	public static <K, V, C extends Collection<? extends K>> List<V> collect(
		Map<K, V> fromMap, C keys) {
		ArrayList<V> values = new ArrayList<V>();

		for (K key : keys) {
			if (fromMap.containsKey(key)) {
				values.add(fromMap.get(key));
			}
		}

		return values;
	}

	/**
	 * Collects a list of objects into a map of sub-lists stored under a
	 * certain
	 * key. The key is determined by applying a function to each object. If an
	 * ordering criterion is given the keys of the map will also be ordered by
	 * the same criterion.
	 *
	 * @param input      The list of objects to collect
	 * @param key        The function that generates the key of an object
	 * @param comparator A comparator that defines the ordering of the map or
	 *                   NULL for none
	 * @return A map containing the lists of collected objects, ordered by keys
	 */
	public static <K, V> Map<K, List<V>> collect(List<V> input,
		Function<? super V, K> key, Comparator<? super V> comparator) {
		Map<K, List<V>> map;

		if (comparator != null) {
			Collections.sort(input, comparator);
			map = new LinkedHashMap<>();
		} else {
			map = new HashMap<>();
		}

		for (V value : input) {
			collectInto(map, key.evaluate(value), value);
		}

		return map;
	}

	/**
	 * Collects values into lists which in turn are stored in a mapping from
	 * keys to value lists. If no list exists for a certain key it will be
	 * created (as an {@link ArrayList} and put into the map.
	 *
	 * @param map   The map to collect the value lists into
	 * @param key   The key to collect the value under
	 * @param value the value to collect
	 */
	public static <K, V> void collectInto(Map<K, List<V>> map, K key,
		V value) {
		List<V> values = map.get(key);

		if (values == null) {
			values = new ArrayList<>();
			map.put(key, values);
		}

		values.add(value);
	}

	/**
	 * Creates a new collection from an existing collection and one or more
	 * additional elements.
	 *
	 * @param collection The collection to combine with the additional values
	 * @param values     The additional values to combine with the collection
	 * @see #combine(Collection, Collection, Collection...)
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C combine(C collection,
		T... values) {
		return combine(collection, Arrays.asList(values));
	}

	/**
	 * Combines two or more collections into a new one. The new collection will
	 * be created by invoking {@link #newCollectionLike(Collection)} with the
	 * first input collection. The iteration order of the result depends on the
	 * types of the input collections.
	 *
	 * @param first      The first collection to combine
	 * @param second     The second collection to combine
	 * @param additional Additional collections to combine
	 * @return A new collection instance
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C combine(C first,
		Collection<? extends T> second,
		Collection<? extends T>... additional) {
		C newCollection = newCollectionLike(first);

		newCollection.addAll(first);
		newCollection.addAll(second);

		for (Collection<? extends T> collection : additional) {
			newCollection.addAll(collection);
		}

		return newCollection;
	}

	/**
	 * Combines two or more maps into a new one. If the first input map is an
	 * instance of {@link LinkedHashMap} the resulting map will be of the same
	 * type, otherwise it will be a regular map.
	 *
	 * @param first      The first map to combine
	 * @param second     The second map to combine
	 * @param additional Additional maps to combine
	 * @return A new collection instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> combine(Map<K, V> first,
		Map<? extends K, ? extends V> second,
		Map<? extends K, ? extends V>... additional) {
		int count = first.size() + second.size();
		Map<K, V> newMap = first instanceof LinkedHashMap ?
		                   new LinkedHashMap<K, V>(count) :
		                   new HashMap<K, V>(count);

		newMap.putAll(first);
		newMap.putAll(second);

		for (Map<? extends K, ? extends V> map : additional) {
			newMap.putAll(map);
		}

		return newMap;
	}

	/**
	 * An implementation of the method {@link Collection#contains(Object)} for
	 * arrays. Returns TRUE if the given array contains the searched object
	 * which is verified by an identity comparison (NOT by invoking equals()).
	 *
	 * @param array  The array to search
	 * @param search The element to search for
	 * @return TRUE if the array contains the searched object at least once
	 */
	public static <T> boolean contains(T[] array, T search) {
		for (T element : array) {
			if (element == search) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Converts the elements of a list by applying a certain function to each
	 * element and replacing it with the function result.
	 *
	 * @param values     The list containing the values to be converted
	 * @param conversion The function to apply to the list elements
	 */
	public static <T> void convert(List<T> values,
		Function<? super T, ? extends T> conversion) {
		for (int i = 0; i < values.size(); i++) {
			values.set(i, conversion.evaluate(values.get(i)));
		}
	}

	/**
	 * Counts the elements of a collection that have the same key. The key is
	 * derived from the collection elements by applying a function to an
	 * element. The result is a mapping from keys to integer counts.
	 *
	 * @param input  The collection to count the elements of
	 * @param getKey The function that generates the key of an element
	 * @return A mapping from keys to the respective element counts
	 */
	public static <K, V, C extends Collection<V>> Map<K, Integer> count(C input,
		Function<? super V, K> getKey) {
		Map<K, Integer> countMap = new HashMap<>();

		for (V value : input) {
			K key = getKey.evaluate(value);
			Integer count = countMap.get(key);

			if (count == null) {
				count = 0;
			}

			countMap.put(key, count + 1);
		}

		return countMap;
	}

	/**
	 * Creates a map by evaluating each element in a collection with a function
	 * that generates the corresponding key. Uses a static function instance
	 * created by {@link CollectionFunctions#createMap(Function)}.
	 *
	 * @param collection The collection to create the map from
	 * @param key        The function that generates the map keys
	 * @return An ordered map containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, C extends Collection<V>> Map<K, V> createMap(
		C collection, Function<? super V, K> key) {
		return ((BinaryFunction<C, Function<? super V, K>, Map<K, V>>) CREATE_MAP).evaluate(
			collection, key);
	}

	/**
	 * Returns a typed reference to {@link #ENUM_COMPARATOR} which compares
	 * enum
	 * instances by their names instead of their order.
	 *
	 * @return The enum comparator
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Comparator<E> enumComparator() {
		return (Comparator<E>) ENUM_COMPARATOR;
	}

	/**
	 * Searches for a certain element in a collection. The returned element
	 * will
	 * be the first in the collection's iteration order for which the given
	 * predicate evaluates to TRUE or NULL if none could be found. Based on a
	 * static function returned by {@link CollectionFunctions#find(Predicate)}.
	 *
	 * @param collection The collection to search in
	 * @param criteria   The predicate to test the collection elements with
	 * @return The resulting object or NULL if none could be found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T find(Collection<T> collection,
		Predicate<? super T> criteria) {
		return ((BinaryFunction<Collection<T>, Predicate<? super T>, T>) FIND).evaluate(
			collection, criteria);
	}

	/**
	 * Returns the first element from an arbitrary {@link Iterable} object.
	 *
	 * @param iterable The iterable object
	 * @return The first element in the iteration order or NULL if the input
	 * object is empty
	 */
	public static <T> T firstElementOf(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		T result = null;

		if (iterator.hasNext()) {
			result = iterator.next();
		}

		return result;
	}

	/**
	 * Collects all argument objects into a list that cannot be modified.
	 * Convenience method for invoking
	 * Collections.unmodifiableList({@link Arrays#asList(Object...)}).
	 *
	 * @param values The values to be collected
	 * @return A new unmodifiable list containing the argument values
	 */
	@SafeVarargs
	public static <T> List<T> fixedListOf(T... values) {
		return Collections.unmodifiableList(Arrays.asList(values));
	}

	/**
	 * Collects all argument key-value pairs into an ordered map that cannot be
	 * modified. Invokes {@link Collections#unmodifiableMap(Map)} on the result
	 * of {@link #mapOf(Pair...)}).
	 *
	 * @param entries Pairs containing the map entries
	 * @return A new unmodifiable map containing the argument entries
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> fixedMapOf(Pair<K, V>... entries) {
		return Collections.unmodifiableMap(mapOf(entries));
	}

	/**
	 * Collects all argument key-value pairs into an ordered map that cannot be
	 * modified. Invokes {@link Collections#unmodifiableMap(Map)} on the result
	 * of {@link #fixedMapOf(Pair...)}).
	 *
	 * @param entries Pairs containing the map entries
	 * @return A new unmodifiable map containing the argument entries
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> fixedOrderedMapOf(Pair<K, V>... entries) {
		return Collections.unmodifiableMap(orderedMapOf(entries));
	}

	/**
	 * Collects all argument objects into an ordered set that cannot be
	 * modified. Invokes {@link Collections#unmodifiableSet(Set)} on the result
	 * of {@link #fixedSetOf(Object...)}).
	 *
	 * @param values The values to be collected
	 * @return A new unmodifiable set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> fixedOrderedSetOf(T... values) {
		return Collections.unmodifiableSet(orderedSetOf(values));
	}

	/**
	 * Collects all argument objects into an ordered set that cannot be
	 * modified. Invokes {@link Collections#unmodifiableSet(Set)} on the result
	 * of {@link #setOf(Object...)}).
	 *
	 * @param values The values to be collected
	 * @return A new unmodifiable set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> fixedSetOf(T... values) {
		return Collections.unmodifiableSet(setOf(values));
	}

	/**
	 * Returns a value from a collection at a certain position, relative to the
	 * iteration order. The first position is zero.
	 *
	 * @param collection The collection to return the value from
	 * @param index      The position index
	 * @return The corresponding value
	 * @throws IndexOutOfBoundsException If the index is invalid for the
	 *                                   collection
	 */
	public static <T, C extends Collection<T>> T get(C collection, int index) {
		if (index < 0 || index >= collection.size()) {
			throw new IndexOutOfBoundsException("Invalid index: " + index);
		}

		for (T value : collection) {
			if (index-- == 0) {
				return value;
			}
		}

		return null;
	}

	/**
	 * Returns the position of a certain element in a collection.
	 *
	 * @param collection The collection to search the element in
	 * @param element    The element to search
	 * @return The position index of the value or -1 if not found
	 */
	public static <T, C extends Collection<T>> int indexOf(C collection,
		T element) {
		int index = -1;

		for (T value : collection) {
			index++;

			if (element == value) {
				break;
			}
		}

		return index;
	}

	/**
	 * @see #insert(List, Object, Collection)
	 */
	@SafeVarargs
	public static <T, L extends List<? super T>> void insert(L list,
		T beforeValue, T... newValues) {
		insert(list, beforeValue, Arrays.asList(newValues));
	}

	/**
	 * Inserts a collection of values before another value in a list. If the
	 * value to insert before doesn't exist the new values will be added to the
	 * end of the list.
	 *
	 * @param list        The list to add the value to
	 * @param beforeValue The value to insert the new value before
	 * @param newValues   The value to insert
	 */
	public static <T, L extends List<? super T>> void insert(L list,
		T beforeValue, Collection<T> newValues) {
		int pos = list.indexOf(beforeValue);

		if (pos == -1) {
			pos = list.size();
		}

		list.addAll(pos, newValues);
	}

	/**
	 * Returns a new collection that contains the intersection of two
	 * collections, i.e. only the elements that are contained in both
	 * collections. The returned collection will be of the same type as the
	 * first input collection.
	 *
	 * @param first  The first collection to intersect
	 * @param second The second collection to intersect
	 * @return A new collection containing the intersection of the arguments
	 * (may be empty but will never be NULL)
	 */
	public static <T, C extends Collection<T>> C intersect(C first, C second) {
		C result = newCollectionLike(first);

		for (T value : first) {
			if (second.contains(value)) {
				result.add(value);
			}
		}

		return result;
	}

	/**
	 * Checks if two Collections intersect. Returns true if at least one
	 * element
	 * is contained in both collections.
	 *
	 * @param first  The first collection
	 * @param second The second collection
	 * @return TRUE if the collections intersect
	 */
	public static boolean intersects(Collection<?> first,
		Collection<?> second) {
		for (Object object : second) {
			if (first.contains(object)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Copies an array and additional elements of the same type into a new
	 * array
	 * of that type and returns it.
	 *
	 * @param array The original array; it's elements will be copied to the
	 *              start of the new array
	 * @param add   The elements to add; these will be copied to the end of the
	 *              new array
	 * @return A new array, containing both the original array and the
	 * additional elements
	 */
	@SafeVarargs
	public static <T> T[] join(T[] array, T... add) {
		@SuppressWarnings("unchecked")
		T[] result =
			(T[]) Array.newInstance(array.getClass().getComponentType(),
				array.length + add.length);

		System.arraycopy(array, 0, result, 0, array.length);
		System.arraycopy(add, 0, result, array.length, add.length);

		return result;
	}

	/**
	 * Copies multiple arrays into a new array of the same type and returns it.
	 * Invokes the method {@link #join(Object[], Object...)} for each array.
	 *
	 * @param first  The first array
	 * @param second The second array
	 * @param arrays Optionally additional arrays
	 * @return A new array, containing all elements of the original arrays in
	 * the order of the arguments
	 */
	@SafeVarargs
	public static <T> T[] join(T[] first, T[] second, T[]... arrays) {
		T[] result = join(first, second);

		for (T[] array : arrays) {
			result = join(result, array);
		}

		return result;
	}

	/**
	 * Collects all elements from the iterable argument's iterator into a new
	 * list and returns the list.
	 *
	 * @param values An iterable returning an iterator over the values to be
	 *               collected
	 * @return A new list containing the argument values
	 */
	public static <T> List<T> listOf(Iterable<T> values) {
		return addAll(new ArrayList<T>(), values);
	}

	/**
	 * Collects all argument objects into a new list and returns the list.
	 *
	 * @param values The values to be collected
	 * @return A new list containing the argument values
	 */
	@SafeVarargs
	public static <T> List<T> listOf(T... values) {
		return add(new ArrayList<T>(), values);
	}

	/**
	 * Maps each element from a collection with a function and places it into a
	 * new collection of the same type as the input collection. Based on a
	 * static instance of {@link CollectionFunctions#map(Function)}.
	 *
	 * @param collection The collection to map
	 * @param mapping    The mapping function
	 * @return A new collection containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <I, O, C extends Collection<I>> Collection<O> map(
		C collection, Function<? super I, O> mapping) {
		return ((BinaryFunction<C, Function<? super I, O>, Collection<O>>) MAP).evaluate(
			collection, mapping);
	}

	/**
	 * A convenience variant of {@link #map(Collection, Function)} for lists.
	 * this is necessary because it is not possible to couple the type of the
	 * input collection with that of the output collection because collections
	 * are generic types.
	 *
	 * @see #map(Collection, Function)
	 */
	@SuppressWarnings("unchecked")
	public static <I, O, L extends List<I>> List<O> map(L list,
		Function<? super I, O> mapping) {
		return ((BinaryFunction<L, Function<? super I, O>, List<O>>) MAP).evaluate(
			list, mapping);
	}

	/**
	 * Creates an unordered map that is filled from the argument pairs.
	 *
	 * @param entries The map entry pairs
	 * @return A new unordered map instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mapOf(Pair<K, V>... entries) {
		Map<K, V> map = new HashMap<K, V>(entries.length);

		for (Pair<K, V> entry : entries) {
			map.put(entry.first(), entry.second());
		}

		return map;
	}

	/**
	 * Returns a new collection with the same logical type (i.e. collection
	 * interface) as another collection if it can be determined. Otherwise a
	 * new
	 * {@link LinkedHashSet} will be returned as an order-preserving
	 * collection.
	 *
	 * @param collection The other collection
	 * @return The new collection
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C newCollectionLike(
		C collection) {
		C result;

		if (collection instanceof List) {
			result = (C) new ArrayList<T>();
		} else if (collection instanceof LinkedHashSet) {
			result = (C) new LinkedHashSet<T>();
		} else if (collection instanceof TreeSet) {
			result = (C) new TreeSet<T>();
		} else if (collection instanceof LinkedList) {
			result = (C) new LinkedList<T>();
		} else if (collection instanceof Set) {
			result = (C) new HashSet<T>();
		} else {
			result = (C) new LinkedHashSet<T>();
		}

		return result;
	}

	/**
	 * Returns the next element in a collection. The collection will be
	 * searched
	 * for the given element and if it is found, the element immediately after
	 * it (in the collection's iteration order) will be returned. If the wrap
	 * parameter is TRUE and the searched element is the last one in the
	 * collection the first element will be returned, even if it is the same as
	 * the searched element. The result will be NULL if the collection is
	 * empty,
	 * if the searched element is not found, or if it is the last element and
	 * wrap is FALSE.
	 *
	 * @param collection The collection to search
	 * @param element    The element to search for
	 * @param wrap       If FALSE NULL will be returned as the next element of
	 *                   the last one, else the first element will be returned
	 * @return The next element or NULL for none
	 */
	public static <E> E next(Collection<E> collection, E element,
		boolean wrap) {
		Iterator<E> i = collection.iterator();
		E first = null;

		while (i.hasNext()) {
			E current = i.next();

			if (first == null) {
				first = current;
			}

			if (current == element) {
				if (i.hasNext()) {
					return i.next();
				} else if (wrap) {
					return first;
				}
			}
		}

		return null;
	}

	/**
	 * Creates an ordered map that is filled from the argument pairs.
	 *
	 * @param entries The map entry pairs
	 * @return A new ordered map instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> orderedMapOf(Pair<K, V>... entries) {
		Map<K, V> map = new LinkedHashMap<K, V>(entries.length);

		for (Pair<K, V> entry : entries) {
			map.put(entry.first(), entry.second());
		}

		return map;
	}

	/**
	 * Collects all elements from the iterable argument's iterator into a new
	 * ordered set and returns the set.
	 *
	 * @param values An iterable returning an iterator over the values to be
	 *               collected
	 * @return A new ordered set containing the argument values
	 */
	public static <T> Set<T> orderedSetOf(Iterable<T> values) {
		return addAll(new LinkedHashSet<T>(), values);
	}

	/**
	 * Collects all argument objects into a new ordered set and returns the
	 * set.
	 *
	 * @param values The values to be collected
	 * @return A new ordered set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> orderedSetOf(T... values) {
		return add(new LinkedHashSet<T>(), values);
	}

	/**
	 * Parses a list of map entries and returns a new object map containing
	 * them.
	 *
	 * @see #parseMap(Class, Class, String...)
	 */
	public static Map<Object, Object> parseMap(String... entries) {
		return parseMap(Object.class, Object.class, entries);
	}

	/**
	 * Convenience method that parses a string containing a list of map entries
	 * in the format "map entry 1&lt;<i>separator</i>&gt;map entry 2&lt;<i>
	 * separator</i>&gt;..." and returns a new map containing the parsed
	 * entries. Neither keys nor values can contain the separator character.
	 * Any
	 * whitespace characters surrounding the map entries will be removed.
	 *
	 * @param entries    The entry string to be parsed
	 * @param cSeparator The separator character between key value pairs (will
	 *                   be used in a call to <code>Pattern.split()</code>).
	 * @return A new map containing the parsed entries
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 * @see #parseMap(String...)
	 * @see Pattern#split(java.lang.CharSequence)
	 */
	public static Map<Object, Object> parseMap(String entries,
		char cSeparator) {
		return parseMap(entries.split("" + cSeparator));
	}

	/**
	 * Parses a list of map entries and returns a new map containing the parsed
	 * entries. The resulting map will return it's entries in the same order in
	 * which they are defined in the variable argument list.
	 *
	 * <p>
	 * The first two parameters define the data types of the key and value
	 * elements, respectively. Each parsed key and value will be cast to this
	 * types. Therefore any type mismatch will cause a ClassCastException to be
	 * thrown.
	 * </p>
	 *
	 * @param keyType   The datatype of the key
	 * @param valueType The datatype of the value
	 * @param entries   The list of entry strings to be parsed
	 * @return A new map containing the parsed entries
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 * @see #parseMapEntries(Map, Class, Class, String...)
	 */
	public static <K, V> Map<K, V> parseMap(Class<K> keyType,
		Class<V> valueType, String... entries) {
		Map<K, V> map = new LinkedHashMap<K, V>();

		parseMapEntries(map, keyType, valueType, entries);

		return map;
	}

	/**
	 * Parses an array of map entries and inserts them into a map. Existing
	 * entries with equal keys will be replaced. The strings to be parsed must
	 * be of the format &lt;key&gt;=&lt;value&gt; or&lt;key&gt;:&lt;value&gt;.
	 * Both the key and the value elements must be parsable by the method
	 * {@link TextUtil#parseObject(String)}, else an IllegalArgumentException
	 * will be thrown.
	 *
	 * @param entries   The entry strings to be parsed
	 * @param targetMap The map to insert the parsed key/value pairs into
	 * @param keyType   The datatype of the key
	 * @param valueType The datatype of the value
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 * @see #addMapEntry(Map, String, Class, Class)
	 */
	public static <K, V> void parseMapEntries(Map<K, V> targetMap,
		Class<K> keyType, Class<V> valueType, String... entries) {
		for (String entry : entries) {
			addMapEntry(targetMap, entry, keyType, valueType);
		}
	}

	/**
	 * Parses a list of map entries and returns a new string map containing
	 * them. Each entry will be split into key and value strings by means of
	 * the
	 * method {@link #splitMapEntry(String)} which will then be added to the
	 * map
	 * without further conversion.
	 *
	 * @param entries The map entries to parse
	 * @return A new string map containing the key-value pairs from the entries
	 */
	public static Map<String, String> parseStringMap(String... entries) {
		Map<String, String> map = new LinkedHashMap<String, String>();

		for (String entry : entries) {
			Matcher matcher = splitMapEntry(entry);

			map.put(matcher.group(1), matcher.group(2));
		}

		return map;
	}

	/**
	 * Returns the previous element in a collection. The collection will be
	 * searched for the given element and if it is found, the element
	 * immediately before it (in the collection's iteration order) will be
	 * returned. If the wrap parameter is TRUE and the searched element is the
	 * first one in the collection the last element will be returned, even
	 * if it
	 * is the same as the searched element. The result will be NULL if the
	 * collection is empty, if the searched element is not found, or if it is
	 * the first element and wrap is FALSE.
	 *
	 * @param collection The collection to search
	 * @param element    The element to search for
	 * @param wrap       If FALSE NULL will be returned as the previous element
	 *                   of the first one, else the last element will be
	 *                   returned
	 * @return The previous element or NULL for none
	 */
	public static <E> E previous(Collection<E> collection, E element,
		boolean wrap) {
		Iterator<E> i = collection.iterator();
		E previous = null;

		while (i.hasNext()) {
			E current = i.next();

			if (current == element) {
				if (previous != null) {
					return previous;
				} else if (wrap) {
					while (i.hasNext()) {
						current = i.next();
					}

					return current;
				}
			}

			previous = current;
		}

		return null;
	}

	/**
	 * Prints a map formatted over multiple lines to a certain print writer
	 * with
	 * a certain indentation.
	 *
	 * @param data The map to print
	 * @param out  The output stream
	 */
	public static void print(Map<?, ?> data, PrintStream out) {
		print(data, out, "");
	}

	/**
	 * Prints a map formatted over multiple lines to a certain print writer
	 * with
	 * a certain indentation.
	 *
	 * @param data   The map to print
	 * @param out    The output stream
	 * @param indent The indentation for this recursion
	 */
	private static void print(Object data, PrintStream out, String indent) {
		if (data instanceof Map) {
			for (Entry<?, ?> entry : ((Map<?, ?>) data).entrySet()) {
				Object key = entry.getKey();

				out.print(indent);
				out.print(key);
				out.print(": ");

				print(entry.getValue(), out, indent + '\t');
			}
		} else if (data instanceof Collection) {
			for (Object value : (Collection<?>) data) {
				print(value, out, indent + '\t');
				out.print(", ");
			}
		} else {
			out.print(data);
		}

		out.println();
	}

	/**
	 * @see #removeAll(Map, Collection)
	 */
	@SuppressWarnings("unchecked")
	public static <K> void removeAll(Map<K, ?> map, K... keys) {
		removeAll(map, Arrays.asList(keys));
	}

	/**
	 * Removes multiple entries from a map which are identified by a collection
	 * of keys.
	 *
	 * @param map  The map to remove the elements from
	 * @param keys The collection of keys to remove
	 */
	public static <K> void removeAll(Map<K, ?> map, Collection<K> keys) {
		for (K key : keys) {
			map.remove(key);
		}
	}

	/**
	 * Collects all elements from the iterable argument's iterator into a new
	 * set and returns the set.
	 *
	 * @param values An iterable returning an iterator over the values to be
	 *               collected
	 * @return A new set containing the argument values
	 */
	public static <T> Set<T> setOf(Iterable<T> values) {
		return addAll(new HashSet<T>(), values);
	}

	/**
	 * Collects all argument objects into a new set and returns the set.
	 *
	 * @param values The values to be collected
	 * @return A new set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> setOf(T... values) {
		return add(new HashSet<T>(), values);
	}

	/**
	 * Sorts the elements of a map by the map key and returns an new ordered
	 * map
	 * instance (currently {@link LinkedHashMap}) that contains the ordered
	 * entries. The input map will not be modified by this call.
	 *
	 * @param inputMap The input map to sort
	 * @return A new ordered map instance
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sort(
		Map<K, V> inputMap) {
		return sort(inputMap, null);
	}

	/**
	 * Sorts the elements of a map by the map key and returns an new ordered
	 * map
	 * instance (currently {@link LinkedHashMap}) that contains the ordered
	 * entries. The input map will not be modified by this call.
	 *
	 * @param inputMap   The input map to sort
	 * @param comparator The comparator to sort by or NULL for the default
	 *                   (natural) order
	 * @return A new ordered map instance
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sort(
		Map<K, V> inputMap, Comparator<K> comparator) {
		Map<K, V> sortedMap = new LinkedHashMap<K, V>(inputMap.size());
		List<K> sortedKeys = new ArrayList<K>(inputMap.keySet());

		Collections.sort(sortedKeys, comparator);

		for (K key : sortedKeys) {
			sortedMap.put(key, inputMap.get(key));
		}

		return sortedMap;
	}

	/**
	 * Sorts the argument list of relatable objects by a certain relation type.
	 *
	 * @param list  The list of relatable objects to sort
	 * @param types The relation type to sort the objects by
	 */
	@SafeVarargs
	public static <T extends Relatable> void sortBy(List<T> list,
		RelationType<? extends Comparable<?>>... types) {
		Collections.sort(list, new RelationComparator<T>(types));
	}

	/**
	 * Splits a single map entry into key and value strings. Returns a regular
	 * expression {@link Matcher} that contains the resulting key and value
	 * strings in the first and second group, respectively. Key and value must
	 * be separated by either a equal sign '=' or by a colon ':'. The first
	 * occurrence of one of these characters delimits the key value. Any
	 * further
	 * of these characters will be returned as part of the value group.
	 *
	 * @param entry The map entry string to be parsed
	 * @return A regular expression matcher initialized to return key and value
	 * in groups 1 and 2
	 * @throws IllegalArgumentException If the parsing of the map entry fails
	 */
	public static Matcher splitMapEntry(String entry) {
		Matcher matcher =
			Pattern.compile("\\s*(.+)\\s*[:=]\\s*(.*)\\s*").matcher(entry);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid map entry: " + entry);
		}

		return matcher;
	}

	/**
	 * Returns a new collection that contains the subtraction of two
	 * collections, i.e. the elements that are only contained in the first
	 * argument collection. The returned collection will be of the same type as
	 * the first input collection.
	 *
	 * @param first  The collection to subtract the second from
	 * @param second The collection to subtract from the first
	 * @return A new collection containing the intersection of the arguments
	 * (may be empty but will never be NULL)
	 */
	public static <T, C extends Collection<T>> C subtract(C first, C second) {
		C result = newCollectionLike(first);

		result.addAll(first);
		result.removeAll(second);

		return result;
	}

	/**
	 * Convenience method to return a string representation of an object array.
	 * Invokes {@link Arrays#asList(Object...)} to convert the input argument
	 * into an iterable object.
	 *
	 * @see #toString(Iterable, String)
	 */
	public static String toString(Object[] input, String separator) {
		return toString(Arrays.asList(input), separator);
	}

	/**
	 * Returns a string representation of the elements in an {@link Iterable}
	 * instance. The additional parameter defines the string that will be
	 * inserted between the elements. The separator will only be added between
	 * elements, not after the last element.
	 *
	 * @param input     An Iterable instance containing the input objects
	 * @param separator The string to be inserted between elements
	 * @return A string representing the iterated elements
	 */
	public static String toString(Iterable<?> input, String separator) {
		StringBuilder sb = new StringBuilder();

		for (Object element : input) {
			sb.append(element);
			sb.append(separator);
		}

		if (sb.length() >= separator.length()) {
			sb.setLength(sb.length() - separator.length());
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the elements in an {@link Iterable}
	 * object. Each element will be converted to a string with the given
	 * function. The string parameter defines the separator string that will be
	 * inserted between the elements. It will only be added between elements,
	 * not after the last element.
	 *
	 * @param input             The {@link Iterable} instance
	 * @param elementConversion A function to convert the iterated elements
	 * @param separator         The string to be inserted between elements
	 * @return A string representing the iterated elements
	 */
	public static <T> String toString(Iterable<T> input,
		Function<? super T, ?> elementConversion, String separator) {
		StringBuilder sb = new StringBuilder();

		for (T element : input) {
			sb.append(elementConversion.evaluate(element));
			sb.append(separator);
		}

		if (sb.length() >= separator.length()) {
			sb.setLength(sb.length() - separator.length());
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the contents of a map. The two
	 * additional parameters define the strings that will be inserted between
	 * keys as well as values and map entries, respectively. The map entry
	 * separator will only be added between entries, not after the last entry.
	 *
	 * @param map       The map containing the source keys and values
	 * @param junction  The string to be inserted between keys and values
	 * @param separator The string to be inserted between map entries
	 * @return A string representing the map contents
	 */
	public static String toString(Map<?, ?> map, String junction,
		String separator) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append(junction);
			sb.append(entry.getValue());
			sb.append(separator);
		}

		if (sb.length() >= separator.length()) {
			sb.setLength(sb.length() - separator.length());
		}

		return sb.toString();
	}

	/**
	 * Transforms a collection by returning a new collection that contains the
	 * results of applying a certain function to all elements of the input
	 * collection. The class of the returned collection will be the same as
	 * that
	 * of the input collection, with element types defined by the output of the
	 * transformation function. The new collection instance will be created by
	 * invoking the method {@link Class#newInstance()} on the input
	 * collection's
	 * class. Therefore the class must provide a no-argument constructor (which
	 * is the case for all standard collection classes in the java packages).
	 *
	 * @param input    The collection containing the input values
	 * @param function The function to apply to the collection elements
	 * @return The resulting collection (may be empty but will never be NULL)
	 * @throws IllegalArgumentException If creating the result collection
	 *                                  failed
	 */
	public static <I, O> Collection<O> transform(Collection<I> input,
		Function<? super I, O> function) {
		try {
			@SuppressWarnings("unchecked")
			Collection<O> result =
				input.getClass().getConstructor().newInstance();

			for (I t : input) {
				result.add(function.evaluate(t));
			}

			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Could not create result collection", e);
		}
	}
}
