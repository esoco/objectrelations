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

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.filter.RelationComparator;


/********************************************************************
 * Utility functions for the handling of collections.
 *
 * @author eso
 */
public class CollectionUtil
{
	//~ Static fields/initializers ---------------------------------------------

	/** A comparator constant that compares enum by their names. */
	public static Comparator<Enum<?>> ENUM_COMPARATOR =
		new Comparator<Enum<?>>()
		{
			@Override
			public int compare(Enum<?> e1, Enum<?> e2)
			{
				return e1.name().compareTo(e2.name());
			}
		};

	private static final BinaryFunction<?, ?, ?> COLLECT =
		CollectionFunctions.collect(null);

	private static final BinaryFunction<?, ?, ?> FIND =
		CollectionFunctions.find(null);

	private static final BinaryFunction<?, ?, ?> MAP =
		CollectionFunctions.map(null);

	private static final BinaryFunction<?, ?, ?> CREATE_MAP =
		CollectionFunctions.createMap(null);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private constructor, only static use.
	 */
	private CollectionUtil()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Adds the trailing argument objects into the argument collection and
	 * returns it (for convenience).
	 *
	 * @param  rCollection The collection to add the values to
	 * @param  rValues     The values to be added
	 *
	 * @return The argument collection
	 */
	@SafeVarargs
	public static <T, C extends Collection<? super T>> C add(
		C    rCollection,
		T... rValues)
	{
		for (T value : rValues)
		{
			rCollection.add(value);
		}

		return rCollection;
	}

	/***************************************
	 * Adds a number of instances of a certain type to a collection.
	 *
	 * @param  rCollecion The collection to add the values to
	 * @param  nCount     The number of values to add
	 * @param  rValue     The value to add
	 *
	 * @return The argument collection
	 */
	public static <T, C extends Collection<? super T>> C add(C   rCollecion,
															 int nCount,
															 T   rValue)
	{
		while (nCount-- > 0)
		{
			rCollecion.add(rValue);
		}

		return rCollecion;
	}

	/***************************************
	 * Adds all objects returned by an Iterable instance into the argument
	 * collection and returns it (for convenience).
	 *
	 * @param  rCollection The collection to add the values to
	 * @param  rValues     The values to be added
	 *
	 * @return The collection
	 */
	public static <T, C extends Collection<T>> C addAll(
		C			rCollection,
		Iterable<T> rValues)
	{
		for (T value : rValues)
		{
			rCollection.add(value);
		}

		return rCollection;
	}

	/***************************************
	 * Parses a single map entry and adds it to a map. An existing entry with
	 * the same key will be replaced. The string to be parsed must either have
	 * the format &lt;key&gt;=&lt;value&gt; or &lt;key&gt;:&lt;value&gt;.
	 *
	 * <p>The key and value elements will be parsed by means of the method
	 * {@link TextUtil#parseObject(String)}, which will throw an
	 * IllegalArgumentException on invalid elements. The key must have a length
	 * of at least one character, the value may be empty. See the documentation
	 * of {@link TextUtil#parseObject(String)}for allowed key and value formats
	 * and the corresponding datatypes. Any whitespace characters surrounding
	 * either the key or the value will be removed.</p>
	 *
	 * <p>The last arguments define the datatype classes of the key and value,
	 * respectively. The parsed key and value objects will be cast to this types
	 * before being inserted into the map. Therefore any type mismatch will
	 * cause a ClassCastException to be thrown.</p>
	 *
	 * @param  rTargetMap The map to insert the parsed key/value pair into
	 * @param  sEntry     The entry string to be parsed
	 * @param  rKeyType   The datatype of the key
	 * @param  rValueType The datatype of the value
	 *
	 * @throws IllegalArgumentException If the parsing of the map entry fails
	 */
	public static <K, V> void addMapEntry(Map<K, V> rTargetMap,
										  String    sEntry,
										  Class<K>  rKeyType,
										  Class<V>  rValueType)
	{
		Matcher aMatcher = splitMapEntry(sEntry);
		Object  aKey     = TextUtil.parseObject(aMatcher.group(1));
		Object  aValue   = TextUtil.parseObject(aMatcher.group(2));

		rTargetMap.put(rKeyType.cast(aKey), rValueType.cast(aValue));
	}

	/***************************************
	 * Applies a certain function to all elements of a collection.
	 *
	 * @param rValues   The collection of the elements to apply the function to
	 * @param fFunction The function to apply to the elements
	 */
	public static <T> void apply(
		Collection<T>		   rValues,
		Function<? super T, ?> fFunction)
	{
		for (T rValue : rValues)
		{
			fFunction.evaluate(rValue);
		}
	}

	/***************************************
	 * Collects each element from a collection for which the given predicate
	 * evaluates to TRUE into a new collection of the same type as the input
	 * collection. Uses a static instance of a function returned by the method
	 * {@link CollectionFunctions#collect(Predicate)}.
	 *
	 * @param  rCollection The collection to collect elements from
	 * @param  pCollect    The predicate to test the collection elements with
	 *
	 * @return A new collection containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C collect(
		C					 rCollection,
		Predicate<? super T> pCollect)
	{
		return ((BinaryFunction<C, Predicate<? super T>, C>) COLLECT).evaluate(rCollection,
																			   pCollect);
	}

	/***************************************
	 * Collects all values from a map with certain keys into a new list.
	 *
	 * @param  rFromMap The map to collect the values from
	 * @param  rKeys    A collection of map keys
	 *
	 * @return A new list containing the map values that match the given keys
	 */
	public static <K, V, C extends Collection<? extends K>> List<V> collect(
		Map<K, V> rFromMap,
		C		  rKeys)
	{
		ArrayList<V> rValues = new ArrayList<V>();

		for (K rKey : rKeys)
		{
			if (rFromMap.containsKey(rKey))
			{
				rValues.add(rFromMap.get(rKey));
			}
		}

		return rValues;
	}

	/***************************************
	 * Collects a list of objects into a map of sub-lists stored under a certain
	 * key. The key is determined by applying a function to each object. If an
	 * ordering criterion is given the keys of the map will also be ordered by
	 * the same criterion.
	 *
	 * @param  rInput      The list of objects to collect
	 * @param  fKey        The function that generates the key of an object
	 * @param  rComparator A comparator that defines the ordering of the map or
	 *                     NULL for none
	 *
	 * @return A map containing the lists of collected objects, ordered by keys
	 */
	public static <K, V> Map<K, List<V>> collect(
		List<V>				   rInput,
		Function<? super V, K> fKey,
		Comparator<? super V>  rComparator)
	{
		Map<K, List<V>> aMap;

		if (rComparator != null)
		{
			Collections.sort(rInput, rComparator);
			aMap = new LinkedHashMap<>();
		}
		else
		{
			aMap = new HashMap<>();
		}

		for (V rValue : rInput)
		{
			collectInto(aMap, fKey.evaluate(rValue), rValue);
		}

		return aMap;
	}

	/***************************************
	 * Collects values into lists which in turn are stored in a mapping from
	 * keys to value lists. If no list exists for a certain key it will be
	 * created (as an {@link ArrayList} and put into the map.
	 *
	 * @param rMap   The map to collect the value lists into
	 * @param rKey   The key to collect the value under
	 * @param rValue the value to collect
	 */
	public static <K, V> void collectInto(Map<K, List<V>> rMap,
										  K				  rKey,
										  V				  rValue)
	{
		List<V> rValues = rMap.get(rKey);

		if (rValues == null)
		{
			rValues = new ArrayList<>();
			rMap.put(rKey, rValues);
		}

		rValues.add(rValue);
	}

	/***************************************
	 * Creates a new collection from an existing collection and one or more
	 * additional elements.
	 *
	 * @param rCollection The collection to combine with the additional values
	 * @param rValues     The additional values to combine with the collection
	 *
	 * @see   #combine(Collection, Collection, Collection...)
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C combine(
		C    rCollection,
		T... rValues)
	{
		return combine(rCollection, Arrays.asList(rValues));
	}

	/***************************************
	 * Combines two or more collections into a new one. The new collection will
	 * be created by invoking {@link #newCollectionLike(Collection)} with the
	 * first input collection. The iteration order of the result depends on the
	 * types of the input collections.
	 *
	 * @param  rFirst      The first collection to combine
	 * @param  rSecond     The second collection to combine
	 * @param  rAdditional Additional collections to combine
	 *
	 * @return A new collection instance
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C combine(
		C						   rFirst,
		Collection<? extends T>    rSecond,
		Collection<? extends T>... rAdditional)
	{
		C aNewCollection = newCollectionLike(rFirst);

		aNewCollection.addAll(rFirst);
		aNewCollection.addAll(rSecond);

		for (Collection<? extends T> rCollection : rAdditional)
		{
			aNewCollection.addAll(rCollection);
		}

		return aNewCollection;
	}

	/***************************************
	 * Combines two or more maps into a new one. If the first input map is an
	 * instance of {@link LinkedHashMap} the resulting map will be of the same
	 * type, otherwise it will be a regular map.
	 *
	 * @param  rFirst      The first map to combine
	 * @param  rSecond     The second map to combine
	 * @param  rAdditional Additional maps to combine
	 *
	 * @return A new collection instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> combine(
		Map<K, V>						 rFirst,
		Map<? extends K, ? extends V>    rSecond,
		Map<? extends K, ? extends V>... rAdditional)
	{
		int		  nCount  = rFirst.size() + rSecond.size();
		Map<K, V> aNewMap =
			rFirst instanceof LinkedHashMap ? new LinkedHashMap<K, V>(nCount)
											: new HashMap<K, V>(nCount);

		aNewMap.putAll(rFirst);
		aNewMap.putAll(rSecond);

		for (Map<? extends K, ? extends V> rMap : rAdditional)
		{
			aNewMap.putAll(rMap);
		}

		return aNewMap;
	}

	/***************************************
	 * An implementation of the method {@link Collection#contains(Object)} for
	 * arrays. Returns TRUE if the given array contains the searched object
	 * which is verified by an identity comparison (NOT by invoking equals()).
	 *
	 * @param  rArray  The array to search
	 * @param  rSearch The element to search for
	 *
	 * @return TRUE if the array contains the searched object at least once
	 */
	public static <T> boolean contains(T[] rArray, T rSearch)
	{
		for (T rElement : rArray)
		{
			if (rElement == rSearch)
			{
				return true;
			}
		}

		return false;
	}

	/***************************************
	 * Converts the elements of a list by applying a certain function to each
	 * element and replacing it with the function result.
	 *
	 * @param rValues     The list containing the values to be converted
	 * @param fConversion The function to apply to the list elements
	 */
	public static <T> void convert(
		List<T>							 rValues,
		Function<? super T, ? extends T> fConversion)
	{
		for (int i = 0; i < rValues.size(); i++)
		{
			rValues.set(i, fConversion.evaluate(rValues.get(i)));
		}
	}

	/***************************************
	 * Counts the elements of a collection that have the same key. The key is
	 * derived from the collection elements by applying a function to an
	 * element. The result is a mapping from keys to integer counts.
	 *
	 * @param  rInput The collection to count the elements of
	 * @param  fKey   The function that generates the key of an element
	 *
	 * @return A mapping from keys to the respective element counts
	 */
	@SuppressWarnings("boxing")
	public static <K, V, C extends Collection<V>> Map<K, Integer> count(
		C					   rInput,
		Function<? super V, K> fKey)
	{
		Map<K, Integer> aCountMap = new HashMap<>();

		for (V rValue : rInput)
		{
			K	    aKey   = fKey.evaluate(rValue);
			Integer rCount = aCountMap.get(aKey);

			if (rCount == null)
			{
				rCount = 0;
			}

			aCountMap.put(aKey, rCount + 1);
		}

		return aCountMap;
	}

	/***************************************
	 * Creates a map by evaluating each element in a collection with a function
	 * that generates the corresponding key. Uses a static function instance
	 * created by {@link CollectionFunctions#createMap(Function)}.
	 *
	 * @param  rCollection The collection to create the map from
	 * @param  fKey        The function that generates the map keys
	 *
	 * @return An ordered map containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, C extends Collection<V>> Map<K, V> createMap(
		C					   rCollection,
		Function<? super V, K> fKey)
	{
		return ((BinaryFunction<C, Function<? super V, K>, Map<K, V>>)
				CREATE_MAP).evaluate(rCollection, fKey);
	}

	/***************************************
	 * Returns a typed reference to {@link #ENUM_COMPARATOR} which compares enum
	 * instances by their names instead of their order.
	 *
	 * @return The enum comparator
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Comparator<E> enumComparator()
	{
		return (Comparator<E>) ENUM_COMPARATOR;
	}

	/***************************************
	 * Searches for a certain element in a collection. The returned element will
	 * be the first in the collection's iteration order for which the given
	 * predicate evaluates to TRUE or NULL if none could be found. Based on a
	 * static function returned by {@link CollectionFunctions#find(Predicate)}.
	 *
	 * @param  rCollection The collection to search in
	 * @param  pCriteria   The predicate to test the collection elements with
	 *
	 * @return The resulting object or NULL if none could be found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T find(
		Collection<T>		 rCollection,
		Predicate<? super T> pCriteria)
	{
		return ((BinaryFunction<Collection<T>, Predicate<? super T>, T>) FIND)
			   .evaluate(rCollection, pCriteria);
	}

	/***************************************
	 * Returns the first element from an arbitrary {@link Iterable} object.
	 *
	 * @param  rIterable The iterable object
	 *
	 * @return The first element in the iteration order or NULL if the input
	 *         object is empty
	 */
	public static <T> T firstElementOf(Iterable<T> rIterable)
	{
		Iterator<T> rIterator = rIterable.iterator();
		T		    rResult   = null;

		if (rIterator.hasNext())
		{
			rResult = rIterator.next();
		}

		return rResult;
	}

	/***************************************
	 * Collects all argument objects into a list that cannot be modified.
	 * Convenience method for invoking Collections.unmodifiableList({@link
	 * Arrays#asList(Object...)}).
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new unmodifiable list containing the argument values
	 */
	@SafeVarargs
	public static <T> List<T> fixedListOf(T... rValues)
	{
		return Collections.unmodifiableList(Arrays.asList(rValues));
	}

	/***************************************
	 * Collects all argument key-value pairs into an ordered map that cannot be
	 * modified. Invokes {@link Collections#unmodifiableMap(Map)} on the result
	 * of {@link #mapOf(Pair...)}).
	 *
	 * @param  rEntries Pairs containing the map entries
	 *
	 * @return A new unmodifiable map containing the argument entries
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> fixedMapOf(Pair<K, V>... rEntries)
	{
		return Collections.unmodifiableMap(mapOf(rEntries));
	}

	/***************************************
	 * Collects all argument key-value pairs into an ordered map that cannot be
	 * modified. Invokes {@link Collections#unmodifiableMap(Map)} on the result
	 * of {@link #fixedMapOf(Pair...)}).
	 *
	 * @param  rEntries Pairs containing the map entries
	 *
	 * @return A new unmodifiable map containing the argument entries
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> fixedOrderedMapOf(Pair<K, V>... rEntries)
	{
		return Collections.unmodifiableMap(orderedMapOf(rEntries));
	}

	/***************************************
	 * Collects all argument objects into an ordered set that cannot be
	 * modified. Invokes {@link Collections#unmodifiableSet(Set)} on the result
	 * of {@link #fixedSetOf(Object...)}).
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new unmodifiable set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> fixedOrderedSetOf(T... rValues)
	{
		return Collections.unmodifiableSet(orderedSetOf(rValues));
	}

	/***************************************
	 * Collects all argument objects into an ordered set that cannot be
	 * modified. Invokes {@link Collections#unmodifiableSet(Set)} on the result
	 * of {@link #setOf(Object...)}).
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new unmodifiable set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> fixedSetOf(T... rValues)
	{
		return Collections.unmodifiableSet(setOf(rValues));
	}

	/***************************************
	 * Returns a value from a collection at a certain position, relative to the
	 * iteration order. The first position is zero.
	 *
	 * @param  rCollection The collection to return the value from
	 * @param  nIndex      The position index
	 *
	 * @return The corresponding value
	 *
	 * @throws IndexOutOfBoundsException If the index is invalid for the
	 *                                   collection
	 */
	public static <T, C extends Collection<T>> T get(C rCollection, int nIndex)
	{
		if (nIndex < 0 || nIndex >= rCollection.size())
		{
			throw new IndexOutOfBoundsException("Invalid index: " + nIndex);
		}

		for (T rValue : rCollection)
		{
			if (nIndex-- == 0)
			{
				return rValue;
			}
		}

		return null;
	}

	/***************************************
	 * Returns the position of a certain element in a collection.
	 *
	 * @param  rCollection The collection to search the element in
	 * @param  rElement    The element to search
	 *
	 * @return The position index of the value or -1 if not found
	 */
	public static <T, C extends Collection<T>> int indexOf(
		C rCollection,
		T rElement)
	{
		int nIndex = -1;

		for (T rValue : rCollection)
		{
			nIndex++;

			if (rElement == rValue)
			{
				break;
			}
		}

		return nIndex;
	}

	/***************************************
	 * @see #insert(List, Object, Collection)
	 */
	@SafeVarargs
	public static <T, L extends List<? super T>> void insert(L    rList,
															 T    rBeforeValue,
															 T... rNewValues)
	{
		insert(rList, rBeforeValue, Arrays.asList(rNewValues));
	}

	/***************************************
	 * Inserts a collection of values before another value in a list. If the
	 * value to insert before doesn't exist the new values will be added to the
	 * end of the list.
	 *
	 * @param rList        The list to add the value to
	 * @param rBeforeValue The value to insert the new value before
	 * @param rNewValues   The value to insert
	 */
	public static <T, L extends List<? super T>> void insert(
		L			  rList,
		T			  rBeforeValue,
		Collection<T> rNewValues)
	{
		int nPos = rList.indexOf(rBeforeValue);

		if (nPos == -1)
		{
			nPos = rList.size();
		}

		rList.addAll(nPos, rNewValues);
	}

	/***************************************
	 * Returns a new collection that contains the intersection of two
	 * collections, i.e. only the elements that are contained in both
	 * collections. The returned collection will be of the same type as the
	 * first input collection.
	 *
	 * @param  rFirst  The first collection to intersect
	 * @param  rSecond The second collection to intersect
	 *
	 * @return A new collection containing the intersection of the arguments
	 *         (may be empty but will never be NULL)
	 */
	public static <T, C extends Collection<T>> C intersect(C rFirst, C rSecond)
	{
		C aResult = newCollectionLike(rFirst);

		for (T rValue : rFirst)
		{
			if (rSecond.contains(rValue))
			{
				aResult.add(rValue);
			}
		}

		return aResult;
	}

	/***************************************
	 * Checks if two Collections intersect. Returns true if at least one element
	 * is contained in both collections.
	 *
	 * @param  rFirst  The first collection
	 * @param  rSecond The second collection
	 *
	 * @return TRUE if the collections intersect
	 */
	public static boolean intersects(
		Collection<?> rFirst,
		Collection<?> rSecond)
	{
		for (Object rObject : rSecond)
		{
			if (rFirst.contains(rObject))
			{
				return true;
			}
		}

		return false;
	}

	/***************************************
	 * Copies an array and additional elements of the same type into a new array
	 * of that type and returns it.
	 *
	 * @param  rArray The original array; it's elements will be copied to the
	 *                start of the new array
	 * @param  rAdd   The elements to add; these will be copied to the end of
	 *                the new array
	 *
	 * @return A new array, containing both the original array and the
	 *         additional elements
	 */
	@SafeVarargs
	public static <T> T[] join(T[] rArray, T... rAdd)
	{
		@SuppressWarnings("unchecked")
		T[] aResult =
			(T[]) Array.newInstance(rArray.getClass().getComponentType(),
									rArray.length + rAdd.length);

		System.arraycopy(rArray, 0, aResult, 0, rArray.length);
		System.arraycopy(rAdd, 0, aResult, rArray.length, rAdd.length);

		return aResult;
	}

	/***************************************
	 * Copies multiple arrays into a new array of the same type and returns it.
	 * Invokes the method {@link #join(Object[], Object...)} for each array.
	 *
	 * @param  rFirst  The first array
	 * @param  rSecond The second array
	 * @param  rArrays Optionally additional arrays
	 *
	 * @return A new array, containing all elements of the original arrays in
	 *         the order of the arguments
	 */
	@SafeVarargs
	public static <T> T[] join(T[] rFirst, T[] rSecond, T[]... rArrays)
	{
		T[] aResult = join(rFirst, rSecond);

		for (T[] rArray : rArrays)
		{
			aResult = join(aResult, rArray);
		}

		return aResult;
	}

	/***************************************
	 * Collects all elements from the iterable argument's iterator into a new
	 * list and returns the list.
	 *
	 * @param  rValues An iterable returning an iterator over the values to be
	 *                 collected
	 *
	 * @return A new list containing the argument values
	 */
	public static <T> List<T> listOf(Iterable<T> rValues)
	{
		return addAll(new ArrayList<T>(), rValues);
	}

	/***************************************
	 * Collects all argument objects into a new list and returns the list.
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new list containing the argument values
	 */
	@SafeVarargs
	public static <T> List<T> listOf(T... rValues)
	{
		return add(new ArrayList<T>(), rValues);
	}

	/***************************************
	 * Maps each element from a collection with a function and places it into a
	 * new collection of the same type as the input collection. Based on a
	 * static instance of {@link CollectionFunctions#map(Function)}.
	 *
	 * @param  rCollection The collection to map
	 * @param  fMapping    The mapping function
	 *
	 * @return A new collection containing the result
	 */
	@SuppressWarnings("unchecked")
	public static <I, O, C extends Collection<I>> Collection<O> map(
		C					   rCollection,
		Function<? super I, O> fMapping)
	{
		return ((BinaryFunction<C, Function<? super I, O>, Collection<O>>) MAP)
			   .evaluate(rCollection, fMapping);
	}

	/***************************************
	 * A convenience variant of {@link #map(Collection, Function)} for lists.
	 * this is necessary because it is not possible to couple the type of the
	 * input collection with that of the output collection because collections
	 * are generic types.
	 *
	 * @see #map(Collection, Function)
	 */
	@SuppressWarnings("unchecked")
	public static <I, O, L extends List<I>> List<O> map(
		L					   rList,
		Function<? super I, O> fMapping)
	{
		return ((BinaryFunction<L, Function<? super I, O>, List<O>>) MAP)
			   .evaluate(rList, fMapping);
	}

	/***************************************
	 * Creates an unordered map that is filled from the argument pairs.
	 *
	 * @param  rEntries The map entry pairs
	 *
	 * @return A new unordered map instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mapOf(Pair<K, V>... rEntries)
	{
		Map<K, V> aMap = new HashMap<K, V>(rEntries.length);

		for (Pair<K, V> rEntry : rEntries)
		{
			aMap.put(rEntry.first(), rEntry.second());
		}

		return aMap;
	}

	/***************************************
	 * Returns a new collection with the same logical type (i.e. collection
	 * interface) as another collection if it can be determined. Otherwise a new
	 * {@link LinkedHashSet} will be returned as an order-preserving collection.
	 *
	 * @param  rCollection The other collection
	 *
	 * @return The new collection
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> C newCollectionLike(
		C rCollection)
	{
		C rResult;

		if (rCollection instanceof List)
		{
			rResult = (C) new ArrayList<T>();
		}
		else if (rCollection instanceof LinkedHashSet)
		{
			rResult = (C) new LinkedHashSet<T>();
		}
		else if (rCollection instanceof TreeSet)
		{
			rResult = (C) new TreeSet<T>();
		}
		else if (rCollection instanceof LinkedList)
		{
			rResult = (C) new LinkedList<T>();
		}
		else if (rCollection instanceof Set)
		{
			rResult = (C) new HashSet<T>();
		}
		else
		{
			rResult = (C) new LinkedHashSet<T>();
		}

		return rResult;
	}

	/***************************************
	 * Returns the next element in a collection. The collection will be searched
	 * for the given element and if it is found, the element immediately after
	 * it (in the collection's iteration order) will be returned. If the bWrap
	 * parameter is TRUE and the searched element is the last one in the
	 * collection the first element will be returned, even if it is the same as
	 * the searched element. The result will be NULL if the collection is empty,
	 * if the searched element is not found, or if it is the last element and
	 * bWrap is FALSE.
	 *
	 * @param  rCollection The collection to search
	 * @param  rElement    The element to search for
	 * @param  bWrap       If FALSE NULL will be returned as the next element of
	 *                     the last one, else the first element will be returned
	 *
	 * @return The next element or NULL for none
	 */
	public static <E> E next(Collection<E> rCollection,
							 E			   rElement,
							 boolean	   bWrap)
	{
		Iterator<E> i	   = rCollection.iterator();
		E		    rFirst = null;

		while (i.hasNext())
		{
			E rCurrent = i.next();

			if (rFirst == null)
			{
				rFirst = rCurrent;
			}

			if (rCurrent == rElement)
			{
				if (i.hasNext())
				{
					return i.next();
				}
				else if (bWrap)
				{
					return rFirst;
				}
			}
		}

		return null;
	}

	/***************************************
	 * Creates an ordered map that is filled from the argument pairs.
	 *
	 * @param  rEntries The map entry pairs
	 *
	 * @return A new ordered map instance
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> orderedMapOf(Pair<K, V>... rEntries)
	{
		Map<K, V> aMap = new LinkedHashMap<K, V>(rEntries.length);

		for (Pair<K, V> rEntry : rEntries)
		{
			aMap.put(rEntry.first(), rEntry.second());
		}

		return aMap;
	}

	/***************************************
	 * Collects all elements from the iterable argument's iterator into a new
	 * ordered set and returns the set.
	 *
	 * @param  rValues An iterable returning an iterator over the values to be
	 *                 collected
	 *
	 * @return A new ordered set containing the argument values
	 */
	public static <T> Set<T> orderedSetOf(Iterable<T> rValues)
	{
		return addAll(new LinkedHashSet<T>(), rValues);
	}

	/***************************************
	 * Collects all argument objects into a new ordered set and returns the set.
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new ordered set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> orderedSetOf(T... rValues)
	{
		return add(new LinkedHashSet<T>(), rValues);
	}

	/***************************************
	 * Parses a list of map entries and returns a new object map containing
	 * them.
	 *
	 * @see #parseMap(Class, Class, String...)
	 */
	public static Map<Object, Object> parseMap(String... rEntries)
	{
		return parseMap(Object.class, Object.class, rEntries);
	}

	/***************************************
	 * Convenience method that parses a string containing a list of map entries
	 * in the format "map entry 1&lt;<i>separator</i>&gt;map entry 2&lt;<i>
	 * separator</i>&gt;..." and returns a new map containing the parsed
	 * entries. Neither keys nor values can contain the separator character. Any
	 * whitespace characters surrounding the map entries will be removed.
	 *
	 * @param  sEntries   The entry string to be parsed
	 * @param  cSeparator The separator character between key value pairs (will
	 *                    be used in a call to <code>Pattern.split()</code>).
	 *
	 * @return A new map containing the parsed entries
	 *
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 *
	 * @see    #parseMap(String...)
	 * @see    Pattern#split(java.lang.CharSequence)
	 */
	public static Map<Object, Object> parseMap(String sEntries, char cSeparator)
	{
		return parseMap(sEntries.split("" + cSeparator));
	}

	/***************************************
	 * Parses a list of map entries and returns a new map containing the parsed
	 * entries. The resulting map will return it's entries in the same order in
	 * which they are defined in the variable argument list.
	 *
	 * <p>The first two parameters define the datatypes of the key and value
	 * elements, respectively. Each parsed key and value will be cast to this
	 * types. Therefore any type mismatch will cause a ClassCastException to be
	 * thrown.</p>
	 *
	 * @param  rKeyType   The datatype of the key
	 * @param  rValueType The datatype of the value
	 * @param  rEntries   The list of entry strings to be parsed
	 *
	 * @return A new map containing the parsed entries
	 *
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 *
	 * @see    #parseMapEntries(Map, Class, Class, String...)
	 */
	public static <K, V> Map<K, V> parseMap(Class<K>  rKeyType,
											Class<V>  rValueType,
											String... rEntries)
	{
		Map<K, V> aMap = new LinkedHashMap<K, V>();

		parseMapEntries(aMap, rKeyType, rValueType, rEntries);

		return aMap;
	}

	/***************************************
	 * Parses an array of map entries and inserts them into a map. Existing
	 * entries with equal keys will be replaced. The strings to be parsed must
	 * be of the format &lt;key&gt;=&lt;value&gt; or&lt;key&gt;:&lt;value&gt;.
	 * Both the key and the value elements must be parseable by the method
	 * {@link TextUtil#parseObject(String)}, else an IllegalArgumentException
	 * will be thrown.
	 *
	 * @param  rEntries   The entry strings to be parsed
	 * @param  rTargetMap The map to insert the parsed key/value pairs into
	 * @param  rKeyType   The datatype of the key
	 * @param  rValueType The datatype of the value
	 *
	 * @throws IllegalArgumentException If the parsing of a map entry fails
	 *
	 * @see    #addMapEntry(Map, String, Class, Class)
	 */
	public static <K, V> void parseMapEntries(Map<K, V> rTargetMap,
											  Class<K>  rKeyType,
											  Class<V>  rValueType,
											  String... rEntries)
	{
		for (String sEntry : rEntries)
		{
			addMapEntry(rTargetMap, sEntry, rKeyType, rValueType);
		}
	}

	/***************************************
	 * Parses a list of map entries and returns a new string map containing
	 * them. Each entry will be split into key and value strings by means of the
	 * method {@link #splitMapEntry(String)} which will then be added to the map
	 * without further conversion.
	 *
	 * @param  rEntries The map entries to parse
	 *
	 * @return A new string map containing the key-value pairs from the entries
	 */
	public static Map<String, String> parseStringMap(String... rEntries)
	{
		Map<String, String> aMap = new LinkedHashMap<String, String>();

		for (String sEntry : rEntries)
		{
			Matcher aMatcher = splitMapEntry(sEntry);

			aMap.put(aMatcher.group(1), aMatcher.group(2));
		}

		return aMap;
	}

	/***************************************
	 * Returns the previous element in a collection. The collection will be
	 * searched for the given element and if it is found, the element
	 * immediately before it (in the collection's iteration order) will be
	 * returned. If the bWrap parameter is TRUE and the searched element is the
	 * first one in the collection the last element will be returned, even if it
	 * is the same as the searched element. The result will be NULL if the
	 * collection is empty, if the searched element is not found, or if it is
	 * the first element and bWrap is FALSE.
	 *
	 * @param  rCollection The collection to search
	 * @param  rElement    The element to search for
	 * @param  bWrap       If FALSE NULL will be returned as the previous
	 *                     element of the first one, else the last element will
	 *                     be returned
	 *
	 * @return The previous element or NULL for none
	 */
	public static <E> E previous(Collection<E> rCollection,
								 E			   rElement,
								 boolean	   bWrap)
	{
		Iterator<E> i		  = rCollection.iterator();
		E		    rPrevious = null;

		while (i.hasNext())
		{
			E rCurrent = i.next();

			if (rCurrent == rElement)
			{
				if (rPrevious != null)
				{
					return rPrevious;
				}
				else if (bWrap)
				{
					while (i.hasNext())
					{
						rCurrent = i.next();
					}

					return rCurrent;
				}
			}

			rPrevious = rCurrent;
		}

		return null;
	}

	/***************************************
	 * Prints a map formatted over multiple lines to a certain print writer with
	 * a certain indentation.
	 *
	 * @param rData The map to print
	 * @param rOut  The output stream
	 */
	public static void print(Map<?, ?> rData, PrintStream rOut)
	{
		print(rData, rOut, "");
	}

	/***************************************
	 * @see #removeAll(Map, Collection)
	 */
	@SuppressWarnings("unchecked")
	public static <K> void removeAll(Map<K, ?> rMap, K... rKeys)
	{
		removeAll(rMap, Arrays.asList(rKeys));
	}

	/***************************************
	 * Removes multiple entries from a map which are identified by a collection
	 * of keys.
	 *
	 * @param rMap  The map to remove the elements from
	 * @param rKeys The collection of keys to remove
	 */
	public static <K> void removeAll(Map<K, ?> rMap, Collection<K> rKeys)
	{
		for (K rKey : rKeys)
		{
			rMap.remove(rKey);
		}
	}

	/***************************************
	 * Collects all elements from the iterable argument's iterator into a new
	 * set and returns the set.
	 *
	 * @param  rValues An iterable returning an iterator over the values to be
	 *                 collected
	 *
	 * @return A new set containing the argument values
	 */
	public static <T> Set<T> setOf(Iterable<T> rValues)
	{
		return addAll(new HashSet<T>(), rValues);
	}

	/***************************************
	 * Collects all argument objects into a new set and returns the set.
	 *
	 * @param  rValues The values to be collected
	 *
	 * @return A new set containing the argument values
	 */
	@SafeVarargs
	public static <T> Set<T> setOf(T... rValues)
	{
		return add(new HashSet<T>(), rValues);
	}

	/***************************************
	 * Sorts the elements of a map by the map key and returns an new ordered map
	 * instance (currently {@link LinkedHashMap}) that contains the ordered
	 * entries. The input map will not be modified by this call.
	 *
	 * @param  rInputMap The input map to sort
	 *
	 * @return A new ordered map instance
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sort(
		Map<K, V> rInputMap)
	{
		return sort(rInputMap, null);
	}

	/***************************************
	 * Sorts the elements of a map by the map key and returns an new ordered map
	 * instance (currently {@link LinkedHashMap}) that contains the ordered
	 * entries. The input map will not be modified by this call.
	 *
	 * @param  rInputMap   The input map to sort
	 * @param  rComparator The comparator to sort by or NULL for the default
	 *                     (natural) order
	 *
	 * @return A new ordered map instance
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sort(
		Map<K, V>	  rInputMap,
		Comparator<K> rComparator)
	{
		Map<K, V> aSortedMap  = new LinkedHashMap<K, V>(rInputMap.size());
		List<K>   aSortedKeys = new ArrayList<K>(rInputMap.keySet());

		Collections.sort(aSortedKeys, rComparator);

		for (K rKey : aSortedKeys)
		{
			aSortedMap.put(rKey, rInputMap.get(rKey));
		}

		return aSortedMap;
	}

	/***************************************
	 * Sorts the argument list of relatable objects by a certain relation type.
	 *
	 * @param rList  The list of relatable objects to sort
	 * @param rTypes The relation type to sort the objects by
	 */
	@SafeVarargs
	public static <T extends Relatable> void sortBy(
		List<T>									 rList,
		RelationType<? extends Comparable<?>>... rTypes)
	{
		Collections.sort(rList, new RelationComparator<T>(rTypes));
	}

	/***************************************
	 * Splits a single map entry into key and value strings. Returns a regular
	 * expression {@link Matcher} that contains the resulting key and value
	 * strings in the first and second group, respectively. Key and value must
	 * be separated by either a equal sign '=' or by a colon ':'. The first
	 * occurrence of one of these characters delimits the key value. Any further
	 * of these characters will be returned as part of the value group.
	 *
	 * @param  sEntry The map entry string to be parsed
	 *
	 * @return A regular expression matcher initialized to return key and value
	 *         in groups 1 and 2
	 *
	 * @throws IllegalArgumentException If the parsing of the map entry fails
	 */
	public static Matcher splitMapEntry(String sEntry)
	{
		Matcher aMatcher =
			Pattern.compile("\\s*(.+)\\s*[:=]\\s*(.*)\\s*").matcher(sEntry);

		if (!aMatcher.matches())
		{
			throw new IllegalArgumentException("Invalid map entry: " + sEntry);
		}

		return aMatcher;
	}

	/***************************************
	 * Returns a new collection that contains the subtraction of two
	 * collections, i.e. the elements that are only contained in the first
	 * argument collection. The returned collection will be of the same type as
	 * the first input collection.
	 *
	 * @param  rFirst  The collection to subtract the second from
	 * @param  rSecond The collection to subtract from the first
	 *
	 * @return A new collection containing the intersection of the arguments
	 *         (may be empty but will never be NULL)
	 */
	public static <T, C extends Collection<T>> C subtract(C rFirst, C rSecond)
	{
		C aResult = newCollectionLike(rFirst);

		aResult.addAll(rFirst);
		aResult.removeAll(rSecond);

		return aResult;
	}

	/***************************************
	 * Convenience method to return a string representation of an object array.
	 * Invokes {@link Arrays#asList(Object...)} to convert the input argument
	 * into an iterable object.
	 *
	 * @see #toString(Iterable, String)
	 */
	public static String toString(Object[] rInput, String sSeparator)
	{
		return toString(Arrays.asList(rInput), sSeparator);
	}

	/***************************************
	 * Returns a string representation of the elements in an {@link Iterable}
	 * instance. The additional parameter defines the string that will be
	 * inserted between the elements. The separator will only be added between
	 * elements, not after the last element.
	 *
	 * @param  rInput     An Iterable instance containing the input objects
	 * @param  sSeparator The string to be inserted between elements
	 *
	 * @return A string representing the iterated elements
	 */
	public static String toString(Iterable<?> rInput, String sSeparator)
	{
		StringBuilder sb = new StringBuilder();

		for (Object rElement : rInput)
		{
			sb.append(rElement);
			sb.append(sSeparator);
		}

		if (sb.length() >= sSeparator.length())
		{
			sb.setLength(sb.length() - sSeparator.length());
		}

		return sb.toString();
	}

	/***************************************
	 * Returns a string representation of the elements in an {@link Iterable}
	 * object. Each element will be converted to a string with the given
	 * function. The string parameter defines the separator string that will be
	 * inserted between the elements. It will only be added between elements,
	 * not after the last element.
	 *
	 * @param  rInput             The {@link Iterable} instance
	 * @param  rElementConversion A function to convert the iterated elements
	 * @param  sSeparator         The string to be inserted between elements
	 *
	 * @return A string representing the iterated elements
	 */
	public static <T> String toString(Iterable<T>			 rInput,
									  Function<? super T, ?> rElementConversion,
									  String				 sSeparator)
	{
		StringBuilder sb = new StringBuilder();

		for (T rElement : rInput)
		{
			sb.append(rElementConversion.evaluate(rElement));
			sb.append(sSeparator);
		}

		if (sb.length() >= sSeparator.length())
		{
			sb.setLength(sb.length() - sSeparator.length());
		}

		return sb.toString();
	}

	/***************************************
	 * Returns a string representation of the contents of a map. The two
	 * additional parameters define the strings that will be inserted between
	 * keys as well as values and map entries, respectively. The map entry
	 * separator will only be added between entries, not after the last entry.
	 *
	 * @param  rMap       The map containing the source keys and values
	 * @param  sJunction  The string to be inserted between keys and values
	 * @param  sSeparator The string to be inserted between map entries
	 *
	 * @return A string representing the map contents
	 */
	public static String toString(Map<?, ?> rMap,
								  String    sJunction,
								  String    sSeparator)
	{
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<?, ?> rEntry : rMap.entrySet())
		{
			sb.append(rEntry.getKey());
			sb.append(sJunction);
			sb.append(rEntry.getValue());
			sb.append(sSeparator);
		}

		if (sb.length() >= sSeparator.length())
		{
			sb.setLength(sb.length() - sSeparator.length());
		}

		return sb.toString();
	}

	/***************************************
	 * Transforms a collection by returning a new collection that contains the
	 * results of applying a certain function to all elements of the input
	 * collection. The class of the returned collection will be the same as that
	 * of the input collection, with element types defined by the output of the
	 * transformation function. The new collection instance will be created by
	 * invoking the method {@link Class#newInstance()} on the input collection's
	 * class. Therefore the class must provide a no-argument constructor (which
	 * is the case for all standard collection classes in the java packages).
	 *
	 * @param  rInput    The collection containing the input values
	 * @param  rFunction The function to apply to the collection elements
	 *
	 * @return The resulting collection (may be empty but will never be NULL)
	 *
	 * @throws IllegalArgumentException If creating the result collection failed
	 */
	public static <I, O> Collection<O> transform(
		Collection<I>		   rInput,
		Function<? super I, O> rFunction)
	{
		try
		{
			@SuppressWarnings("unchecked")
			Collection<O> aResult = rInput.getClass().newInstance();

			for (I t : rInput)
			{
				aResult.add(rFunction.evaluate(t));
			}

			return aResult;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not create result collection",
											   e);
		}
	}

	/***************************************
	 * Prints a map formatted over multiple lines to a certain print writer with
	 * a certain indentation.
	 *
	 * @param rData   The map to print
	 * @param rOut    The output stream
	 * @param sIndent The indentation for this recursion
	 */
	private static void print(Object rData, PrintStream rOut, String sIndent)
	{
		if (rData instanceof Map)
		{
			for (Entry<?, ?> rEntry : ((Map<?, ?>) rData).entrySet())
			{
				Object sKey = rEntry.getKey();

				rOut.print(sIndent);
				rOut.print(sKey);
				rOut.print(": ");

				print(rEntry.getValue(), rOut, sIndent + '\t');
			}
		}
		else if (rData instanceof Collection)
		{
			for (Object rValue : (Collection<?>) rData)
			{
				print(rValue, rOut, sIndent + '\t');
				rOut.print(", ");
			}
		}
		else
		{
			rOut.print(rData);
		}

		rOut.println();
	}
}
