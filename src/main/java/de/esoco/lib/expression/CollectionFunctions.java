//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.GetElement.GetListElement;
import de.esoco.lib.expression.function.GetElement.GetMapValue;
import de.esoco.lib.reflect.ReflectUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains factory methods for collection-related methods.
 *
 * @author eso
 */
public class CollectionFunctions {
	/**
	 * Private, only static use.
	 */
	private CollectionFunctions() {
	}

	/**
	 * Returns a new binary function that adds a value to an input collection.
	 *
	 * @param value The default value to add for unary function invocations
	 * @return A new function instance
	 */
	public static <T, C extends Collection<? super T>> BinaryFunction<C, T,
		Boolean> add(
		T value) {
		return new AbstractBinaryFunction<C, T, Boolean>(value,
			"AddToCollection") {
			@Override
			@SuppressWarnings("boxing")
			public Boolean evaluate(C collection, T value) {
				return collection.add(value);
			}
		};
	}

	/**
	 * Returns a function constant that wraps {@link Arrays#asList(Object...)}.
	 * This method always returns the same function instance. The returned
	 * function implements the {@link InvertibleFunction} interface so that it
	 * can also be used to convert a list into an array.
	 *
	 * @return A constant function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> InvertibleFunction<T[], List<T>> asList() {
		return InvertibleFunction.of(a -> Arrays.asList(a),
			l -> (T[]) l.toArray());
	}

	/**
	 * Returns a new function instance that collects elements of a collection.
	 * Each element for which the given predicate evaluates to TRUE will be
	 * collected into a new collection of the same type as the input
	 * collection.
	 *
	 * @param collect The predicate to evaluate the collection elements with
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Predicate<?
		super T>, C> collect(
		Predicate<? super T> collect) {
		return new AbstractBinaryFunction<C, Predicate<? super T>, C>(collect,
			"Collect") {
			@Override
			@SuppressWarnings("boxing")
			public C evaluate(C collection, Predicate<? super T> predicate) {
				C result = CollectionUtil.newCollectionLike(collection);

				for (T value : collection) {
					if (predicate.evaluate(value)) {
						result.add(value);
					}
				}

				return result;
			}
		};
	}

	/**
	 * Returns a new function instance that collects all elements of input
	 * collections into a target collection of the same type. The function
	 * returns the target collection to support concatenation.
	 *
	 * @param targetCollection The target collection
	 * @return A new function instance
	 */
	public static <E, T extends Collection<E>, C extends Collection<E>> Action<C> collectAllInto(
		final T targetCollection) {
		return c -> targetCollection.addAll(c);
	}

	/**
	 * Returns a new function instance that collects it's input values into a
	 * collection. The function returns the collection to support
	 * concatenation.
	 *
	 * @param collection The target collection
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> Action<T> collectInto(
		final C collection) {
		return v -> collection.add(v);
	}

	/**
	 * Returns a function that returns the size of a collection.
	 *
	 * @return A function constant
	 */
	public static <C extends Collection<?>> Function<C, Integer> collectionSize() {
		return c -> c.size();
	}

	/**
	 * Returns a function constant that creates a copy of a collection with the
	 * same type and contents. The input collection must be of a type that can
	 * be instantiated by reflection or else an exception will occur.
	 *
	 * @return A constant function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> Function<C, C> copyOfCollection() {
		return input -> {
			C output = (C) ReflectUtil.newInstance(input.getClass());

			output.addAll(input);

			return output;
		};
	}

	/**
	 * Returns a new function that creates a list of values from the result of
	 * applying several functions to an input object.
	 *
	 * @param functions The functions to evaluate input objects with
	 * @return A new function instance
	 */
	public static <I, O> Function<I, List<O>> createList(
		final Collection<Function<? super I, O>> functions) {
		return new AbstractFunction<I, List<O>>("CreateList") {
			@Override
			public List<O> evaluate(I input) {
				List<O> result = new ArrayList<O>(functions.size());

				for (Function<? super I, O> function : functions) {
					result.add(function.evaluate(input));
				}

				return result;
			}
		};
	}

	/**
	 * Returns a new function instance that creates a mapping by deriving a key
	 * from the elements of an input collection into a new collection by
	 * evaluating them with a function. The entries in the resulting map will
	 * have the same order as the elements in the input collection.
	 *
	 * @param key The function to generate the map keys with
	 * @return A new binary function instance
	 */
	public static <K, V> BinaryFunction<Collection<V>, Function<? super V, K>,
		Map<K, V>> createMap(
		Function<? super V, K> key) {
		return new AbstractBinaryFunction<Collection<V>, Function<? super V,
			K>, Map<K, V>>(
			key, "CreateMap") {
			@Override
			public Map<K, V> evaluate(Collection<V> inputCollection,
				Function<? super V, K> function) {
				Map<K, V> result =
					new LinkedHashMap<K, V>(inputCollection.size());

				for (V value : inputCollection) {
					result.put(function.evaluate(value), value);
				}

				return result;
			}
		};
	}

	/**
	 * Returns a new function that creates a list of strings from the result of
	 * applying several functions to an input object. This is a convenience
	 * variant of {@link #createList(Collection)} that performs a string
	 * conversion on the result of each function evaluation. If the result
	 * of an
	 * evaluation is NULL and the boolean parameter is TRUE it will be
	 * mapped to
	 * an empty string, else the result will also contain NULL.
	 *
	 * @param mapNull   TRUE to map NULL values to an empty string
	 * @param functions The functions to evaluate input objects with
	 * @return A new function instance
	 */
	public static <I> Function<I, List<String>> createStringList(
		final boolean mapNull,
		final Collection<Function<? super I, ?>> functions) {
		return new AbstractFunction<I, List<String>>("CreateStringList") {
			@Override
			public List<String> evaluate(I input) {
				List<String> result = new ArrayList<String>(functions.size());

				String nullValue = mapNull ? "" : null;

				for (Function<? super I, ?> function : functions) {
					Object value = function.evaluate(input);

					result.add(value != null ? value.toString() : nullValue);
				}

				return result;
			}
		};
	}

	/**
	 * Returns a new function instance that extracts a certain element from a
	 * list. If the index is -1 the last element of the list will be extracted.
	 * Otherwise, if the given index is invalid for the input list a runtime
	 * exception will be thrown.
	 *
	 * @param index The index of the element to extract or -1 for the last
	 *              element in the list
	 * @return A new function instance
	 */
	@SuppressWarnings("boxing")
	public static <T> BinaryFunction<List<T>, Integer, T> extractListElement(
		int index) {
		return new AbstractBinaryFunction<List<T>, Integer, T>(index,
			"ExtractListElement") {
			@Override
			public T evaluate(List<T> list, Integer index) {
				return list.remove(index == -1 ? list.size() - 1 : index);
			}
		};
	}

	/**
	 * Returns a new function instance that extracts a certain value from a
	 * map.
	 * If there is no mapping for the given key in the map the function will
	 * evaluate to NULL.
	 *
	 * @param key The key of the value to extract
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<Map<K, V>, K, V> extractMapValue(
		K key) {
		return new AbstractBinaryFunction<Map<K, V>, K, V>(key,
			"ExtractMapValue") {
			@Override
			public V evaluate(Map<K, V> map, K key) {
				return map.remove(key);
			}
		};
	}

	/**
	 * Returns a new function instance that searches for a certain element in a
	 * collection. The returned element will be the first in the collection's
	 * iteration order for which the given predicate evaluates to TRUE or NULL
	 * if none could be found.
	 *
	 * @param predicate The predicate to evaluate the collection elements with
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Predicate<?
		super T>, T> find(
		Predicate<? super T> predicate) {
		return new AbstractBinaryFunction<C, Predicate<? super T>, T>(predicate,
			"Find") {
			@Override
			@SuppressWarnings("boxing")
			public T evaluate(C collection, Predicate<? super T> predicate) {
				for (T value : collection) {
					if (predicate.evaluate(value)) {
						return value;
					}
				}

				return null;
			}
		};
	}

	/**
	 * Returns a new function instance that evaluates each element of a
	 * collection with another function. The output value of the element
	 * function will be ignored. The output of the returned function will be
	 * the
	 * unchanged input collection.
	 *
	 * @param function The function to evaluate the collection elements with
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Function<?
		super T, ?>, C> forEach(
		Function<T, ?> function) {
		return new AbstractBinaryFunction<C, Function<? super T, ?>, C>(
			function, "ForEach") {
			@Override
			public C evaluate(C collection, Function<? super T, ?> function) {
				for (T value : collection) {
					function.evaluate(value);
				}

				return collection;
			}
		};
	}

	/**
	 * Returns a new function instance that returns an element with a certain
	 * index from a list. The list will be received as the function parameter.
	 * The returned function can also be used as a binary function to allow the
	 * use of variable indices. A variant of this function with a fixed list
	 * parameter can be obtained from {@link #getListElementAt(List)}.
	 *
	 * @param index The index of the element to return
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<List<T>, Integer, T> getListElement(
		int index) {
		return new GetListElement<T>(index);
	}

	/**
	 * Returns a new function instance that returns an element from a certain
	 * list by it's index. This is a variant of {@link #getListElement(int)}
	 * with swapped parameters.
	 *
	 * @param list The list to return elements from
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<Integer, List<T>, T> getListElementAt(
		List<T> list) {
		BinaryFunction<List<T>, Integer, T> listElement = getListElement(0);

		return Functions.swapParams(listElement, list);
	}

	/**
	 * Returns a new function instance that returns the value that is
	 * associated
	 * with a certain key from a map. A variant with swapped left and right
	 * parameters can be obtained from the method
	 * {@link #getMapValueFrom(Map)}.
	 *
	 * @param key The map key of the value to return
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<Map<K, V>, K, V> getMapValue(K key) {
		return new GetMapValue<K, V>(key);
	}

	/**
	 * Returns a new function instance that returns a value from a certain map.
	 * The key of the value to return will be the (left) input parameter of the
	 * returned function. This is the swapped parameter version of the function
	 * returned by {@link #getMapValue(Object)}.
	 *
	 * @param map The map to retrieve the value from
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<K, Map<K, V>, V> getMapValueFrom(
		Map<K, V> map) {
		return Functions.swapParams(new GetMapValue<K, V>(null), map);
	}

	/**
	 * Returns a new function instance that determines the index of an element
	 * in a certain list. See {@link List#indexOf(Object)} for details. The
	 * returned function can also be used as a binary function with receives as
	 * the second parameter the list from which the index of the element in the
	 * first parameter shall be determined.
	 *
	 * <p>
	 * The function returned by {@link #indexOf(Object)} works similar but with
	 * exchanged parameters.
	 * </p>
	 *
	 * @param list The list to return the index of an element therein
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<T, List<? super T>, Integer> indexIn(
		List<? super T> list) {
		BinaryFunction<List<? super T>, T, Integer> indexOf = indexOf(null);

		return Functions.swapParams(indexOf, list);
	}

	/**
	 * Returns a new function instance that determines the index of a certain
	 * element in a list. See {@link List#indexOf(Object)} for details. The
	 * returned function can also be used as a binary function with has the
	 * element to get the index of as the second parameter.
	 *
	 * <p>
	 * The function returned by {@link #indexIn(List)} works similar but with
	 * exchanged parameters.
	 * </p>
	 *
	 * @param element The element to return the index of
	 * @return A new function instance
	 */
	@SuppressWarnings("boxing")
	public static <T> BinaryFunction<List<? super T>, T, Integer> indexOf(
		T element) {
		return new AbstractBinaryFunction<List<? super T>, T, Integer>(element,
			"IndexOf") {
			@Override
			public Integer evaluate(List<? super T> list, T element) {
				return list.indexOf(element);
			}
		};
	}

	/**
	 * Returns a new function instance that maps elements of a collection
	 * into a
	 * new collection by evaluating them with a function.
	 *
	 * @param map The function to evaluate the collection elements with
	 * @return A new function instance
	 */
	public static <I, O, C extends Collection<I>> BinaryFunction<C, Function<?
		super I, O>, Collection<O>> map(
		Function<? super I, O> map) {
		return new AbstractBinaryFunction<C, Function<? super I, O>,
			Collection<O>>(
			map, "MapCollection") {
			@Override
			public Collection<O> evaluate(C inputCollection,
				Function<? super I, O> function) {
				@SuppressWarnings("unchecked")
				Collection<O> result =
					(Collection<O>) CollectionUtil.newCollectionLike(
						inputCollection);

				for (I value : inputCollection) {
					result.add(function.evaluate(value));
				}

				return result;
			}
		};
	}

	/**
	 * Returns a function constant that creates a new list which contains the
	 * elements from another list that it receives as the input value.
	 *
	 * @return A constant function instance
	 */
	public static <T> Function<List<T>, List<T>> newList() {
		return list -> new ArrayList<T>(list);
	}

	/**
	 * Returns a new function instance that reduces a collection into a single
	 * value by evaluating all collection elements with a function.
	 *
	 * @param reduce The function to evaluate the collection elements with
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C,
		BinaryFunction<T, T, T>, T> reduce(
		BinaryFunction<T, T, T> reduce) {
		return new AbstractBinaryFunction<C, BinaryFunction<T, T, T>, T>(reduce,
			"Reduce") {
			@Override
			public T evaluate(C collection, BinaryFunction<T, T, T> function) {
				T result = null;

				for (T value : collection) {
					result = result != null ?
					         function.evaluate(result, value) :
					         value;
				}

				return result;
			}
		};
	}
}
