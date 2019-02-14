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


/********************************************************************
 * Contains factory methods for collection-related methods.
 *
 * @author eso
 */
public class CollectionFunctions
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private CollectionFunctions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new binary function that adds a value to an input collection.
	 *
	 * @param  rValue The default value to add for unary function invocations
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<? super T>> BinaryFunction<C, T,
																	  Boolean>
	add(T rValue)
	{
		return new AbstractBinaryFunction<C, T, Boolean>(
			rValue,
			"AddToCollection")
		{
			@Override
			@SuppressWarnings("boxing")
			public Boolean evaluate(C rCollection, T rValue)
			{
				return rCollection.add(rValue);
			}
		};
	}

	/***************************************
	 * Returns a function constant that wraps {@link Arrays#asList(Object...)}.
	 * This method always returns the same function instance. The returned
	 * function implements the {@link InvertibleFunction} interface so that it
	 * can also be used to convert a list into an array.
	 *
	 * @return A constant function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> InvertibleFunction<T[], List<T>> asList()
	{
		return InvertibleFunction.of(
			a -> Arrays.asList(a),
			l -> (T[]) l.toArray());
	}

	/***************************************
	 * Returns a new function instance that collects elements of a collection.
	 * Each element for which the given predicate evaluates to TRUE will be
	 * collected into a new collection of the same type as the input collection.
	 *
	 * @param  pCollect The predicate to evaluate the collection elements with
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Predicate<? super T>, C>
	collect(Predicate<? super T> pCollect)
	{
		return new AbstractBinaryFunction<C, Predicate<? super T>, C>(
			pCollect,
			"Collect")
		{
			@Override
			@SuppressWarnings("boxing")
			public C evaluate(C rCollection, Predicate<? super T> rPredicate)
			{
				C aResult = CollectionUtil.newCollectionLike(rCollection);

				for (T rValue : rCollection)
				{
					if (rPredicate.evaluate(rValue))
					{
						aResult.add(rValue);
					}
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * Returns a new function instance that collects all elements of input
	 * collections into a target collection of the same type. The function
	 * returns the target collection to support concatenation.
	 *
	 * @param  rTargetCollection The target collection
	 *
	 * @return A new function instance
	 */
	public static <E, T extends Collection<E>, C extends Collection<E>> Action<C>
	collectAllInto(final T rTargetCollection)
	{
		return c -> rTargetCollection.addAll(c);
	}

	/***************************************
	 * Returns a new function instance that collects it's input values into a
	 * collection. The function returns the collection to support concatenation.
	 *
	 * @param  rCollection The target collection
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> Action<T> collectInto(
		final C rCollection)
	{
		return v -> rCollection.add(v);
	}

	/***************************************
	 * Returns a function that returns the size of a collection.
	 *
	 * @return A function constant
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Collection<?>> Function<C, Integer>
	collectionSize()
	{
		return c -> c.size();
	}

	/***************************************
	 * Returns a function constant that creates a copy of a collection with the
	 * same type and contents. The input collection must be of a type that can
	 * be instantiated by reflection or else an exception will occur.
	 *
	 * @return A constant function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> Function<C, C> copyOfCollection()
	{
		return rInput ->
	   		{
	   			C aOutput = (C) ReflectUtil.newInstance(rInput.getClass());

	   			aOutput.addAll(rInput);

	   			return aOutput;
			   };
	}

	/***************************************
	 * @see #createList(Collection)
	 */
	@SafeVarargs
	public static <I, O> Function<I, List<O>> createList(
		final Function<? super I, O>... rFunctions)
	{
		// separate variable needed to prevent Java compiler error
		Collection<Function<? super I, O>> aFunctions =
			Arrays.asList(rFunctions);

		return createList(aFunctions);
	}

	/***************************************
	 * Returns a new function that creates a list of values from the result of
	 * applying several functions to an input object.
	 *
	 * @param  rFunctions The functions to evaluate input objects with
	 *
	 * @return A new function instance
	 */
	public static <I, O> Function<I, List<O>> createList(
		final Collection<Function<? super I, O>> rFunctions)
	{
		return new AbstractFunction<I, List<O>>("CreateList")
		{
			@Override
			public List<O> evaluate(I rInput)
			{
				List<O> aResult = new ArrayList<O>(rFunctions.size());

				for (Function<? super I, O> rFunction : rFunctions)
				{
					aResult.add(rFunction.evaluate(rInput));
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * Returns a new function instance that creates a mapping by deriving a key
	 * from the elements of an input collection into a new collection by
	 * evaluating them with a function. The entries in the resulting map will
	 * have the same order as the elements in the input collection.
	 *
	 * @param  fKey The function to generate the map keys with
	 *
	 * @return A new binary function instance
	 */
	public static <K, V> BinaryFunction<Collection<V>,
										Function<? super V, K>, Map<K, V>>
	createMap(Function<? super V, K> fKey)
	{
		return new AbstractBinaryFunction<Collection<V>,
										  Function<? super V, K>, Map<K, V>>(
			fKey,
			"CreateMap")
		{
			@Override
			public Map<K, V> evaluate(
				Collection<V>		   rInputCollection,
				Function<? super V, K> rFunction)
			{
				Map<K, V> aResult =
					new LinkedHashMap<K, V>(rInputCollection.size());

				for (V rValue : rInputCollection)
				{
					aResult.put(rFunction.evaluate(rValue), rValue);
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * @see #createStringList(boolean, Collection)
	 */
	@SafeVarargs
	public static <I> Function<I, List<String>> createStringList(
		boolean					  bMapNull,
		Function<? super I, ?>... rFunctions)
	{
		// separate variable needed to prevent Java compiler error
		Collection<Function<? super I, ?>> aFunctions =
			Arrays.asList(rFunctions);

		return createStringList(bMapNull, aFunctions);
	}

	/***************************************
	 * Returns a new function that creates a list of strings from the result of
	 * applying several functions to an input object. This is a convenience
	 * variant of {@link #createList(Function...)} that performs a string
	 * conversion on the result of each function evaluation. If the result of an
	 * evaluation is NULL and the boolean parameter is TRUE it will be mapped to
	 * an empty string, else the result will also contain NULL.
	 *
	 * @param  bMapNull   TRUE to map NULL values to an empty string
	 * @param  rFunctions The functions to evaluate input objects with
	 *
	 * @return A new function instance
	 */
	public static <I> Function<I, List<String>> createStringList(
		final boolean							 bMapNull,
		final Collection<Function<? super I, ?>> rFunctions)
	{
		return new AbstractFunction<I, List<String>>("CreateStringList")
		{
			@Override
			public List<String> evaluate(I rInput)
			{
				List<String> aResult = new ArrayList<String>(rFunctions.size());

				String sNullValue = bMapNull ? "" : null;

				for (Function<? super I, ?> rFunction : rFunctions)
				{
					Object rValue = rFunction.evaluate(rInput);

					aResult.add(
						rValue != null ? rValue.toString() : sNullValue);
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * Returns a new function instance that extracts a certain element from a
	 * list. If the index is -1 the last element of the list will be extracted.
	 * Otherwise, if the given index is invalid for the input list a runtime
	 * exception will be thrown.
	 *
	 * @param  nIndex The index of the element to extract or -1 for the last
	 *                element in the list
	 *
	 * @return A new function instance
	 */
	@SuppressWarnings("boxing")
	public static <T> BinaryFunction<List<T>, Integer, T> extractListElement(
		int nIndex)
	{
		return new AbstractBinaryFunction<List<T>, Integer, T>(
			nIndex,
			"ExtractListElement")
		{
			@Override
			public T evaluate(List<T> rList, Integer rIndex)
			{
				int nIndex = rIndex.intValue();

				return rList.remove(nIndex == -1 ? rList.size() - 1 : nIndex);
			}
		};
	}

	/***************************************
	 * Returns a new function instance that extracts a certain value from a map.
	 * If there is no mapping for the given key in the map the function will
	 * evaluate to NULL.
	 *
	 * @param  rKey The key of the value to extract
	 *
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<Map<K, V>, K, V> extractMapValue(K rKey)
	{
		return new AbstractBinaryFunction<Map<K, V>, K, V>(
			rKey,
			"ExtractMapValue")
		{
			@Override
			public V evaluate(Map<K, V> rMap, K rKey)
			{
				return rMap.remove(rKey);
			}
		};
	}

	/***************************************
	 * Returns a new function instance that searches for a certain element in a
	 * collection. The returned element will be the first in the collection's
	 * iteration order for which the given predicate evaluates to TRUE or NULL
	 * if none could be found.
	 *
	 * @param  rPredicate The predicate to evaluate the collection elements with
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Predicate<? super T>, T>
	find(Predicate<? super T> rPredicate)
	{
		return new AbstractBinaryFunction<C, Predicate<? super T>, T>(
			rPredicate,
			"Find")
		{
			@Override
			@SuppressWarnings("boxing")
			public T evaluate(C rCollection, Predicate<? super T> rPredicate)
			{
				for (T rValue : rCollection)
				{
					if (rPredicate.evaluate(rValue))
					{
						return rValue;
					}
				}

				return null;
			}
		};
	}

	/***************************************
	 * Returns a new function instance that evaluates each element of a
	 * collection with another function. The output value of the element
	 * function will be ignored. The output of the returned function will be the
	 * unchanged input collection.
	 *
	 * @param  rFunction The function to evaluate the collection elements with
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, Function<? super T,
																		  ?>, C>
	forEach(Function<T, ?> rFunction)
	{
		return new AbstractBinaryFunction<C, Function<? super T, ?>, C>(
			rFunction,
			"ForEach")
		{
			@Override
			public C evaluate(C rCollection, Function<? super T, ?> rFunction)
			{
				for (T rValue : rCollection)
				{
					rFunction.evaluate(rValue);
				}

				return rCollection;
			}
		};
	}

	/***************************************
	 * Returns a new function instance that returns an element with a certain
	 * index from a list. The list will be received as the function parameter.
	 * The returned function can also be used as a binary function to allow the
	 * use of variable indices. A variant of this function with a fixed list
	 * parameter can be obtained from {@link #getListElementAt(List)}.
	 *
	 * @param  nIndex The index of the element to return
	 *
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<List<T>, Integer, T> getListElement(
		int nIndex)
	{
		return new GetListElement<T>(nIndex);
	}

	/***************************************
	 * Returns a new function instance that returns an element from a certain
	 * list by it's index. This is a variant of {@link #getListElement(int)}
	 * with swapped parameters.
	 *
	 * @param  rList The list to return elements from
	 *
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<Integer, List<T>, T> getListElementAt(
		List<T> rList)
	{
		BinaryFunction<List<T>, Integer, T> rListElement = getListElement(0);

		return Functions.swapParams(rListElement, rList);
	}

	/***************************************
	 * Returns a new function instance that returns the value that is associated
	 * with a certain key from a map. A variant with swapped left and right
	 * parameters can be obtained from the method {@link #getMapValueFrom(Map)}.
	 *
	 * @param  rKey The map key of the value to return
	 *
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<Map<K, V>, K, V> getMapValue(K rKey)
	{
		return new GetMapValue<K, V>(rKey);
	}

	/***************************************
	 * Returns a new function instance that returns a value from a certain map.
	 * The key of the value to return will be the (left) input parameter of the
	 * returned function. This is the swapped parameter version of the function
	 * returned by {@link #getMapValue(Object)}.
	 *
	 * @param  rMap The map to retrieve the value from
	 *
	 * @return A new function instance
	 */
	public static <K, V> BinaryFunction<K, Map<K, V>, V> getMapValueFrom(
		Map<K, V> rMap)
	{
		return Functions.swapParams(new GetMapValue<K, V>(null), rMap);
	}

	/***************************************
	 * Returns a new function instance that determines the index of an element
	 * in a certain list. See {@link List#indexOf(Object)} for details. The
	 * returned function can also be used as a binary function with receives as
	 * the second parameter the list from which the index of the element in the
	 * first parameter shall be determined.
	 *
	 * <p>The function returned by {@link #indexOf(Object)} works similar but
	 * with exchanged parameters.</p>
	 *
	 * @param  rList The list to return the index of an element therein
	 *
	 * @return A new function instance
	 */
	public static <T> BinaryFunction<T, List<? super T>, Integer> indexIn(
		List<? super T> rList)
	{
		BinaryFunction<List<? super T>, T, Integer> rIndexOf = indexOf(null);

		return Functions.swapParams(rIndexOf, rList);
	}

	/***************************************
	 * Returns a new function instance that determines the index of a certain
	 * element in a list. See {@link List#indexOf(Object)} for details. The
	 * returned function can also be used as a binary function with has the
	 * element to get the index of as the second parameter.
	 *
	 * <p>The function returned by {@link #indexIn(List)} works similar but with
	 * exchanged parameters.</p>
	 *
	 * @param  rElement The element to return the index of
	 *
	 * @return A new function instance
	 */
	@SuppressWarnings("boxing")
	public static <T> BinaryFunction<List<? super T>, T, Integer> indexOf(
		T rElement)
	{
		return new AbstractBinaryFunction<List<? super T>, T, Integer>(
			rElement,
			"IndexOf")
		{
			@Override
			public Integer evaluate(List<? super T> rList, T rElement)
			{
				return rList.indexOf(rElement);
			}
		};
	}

	/***************************************
	 * Returns a new function instance that maps elements of a collection into a
	 * new collection by evaluating them with a function.
	 *
	 * @param  fMap The function to evaluate the collection elements with
	 *
	 * @return A new function instance
	 */
	public static <I, O, C extends Collection<I>> BinaryFunction<C, Function<? super I, O>,
																 Collection<O>>
	map(Function<? super I, O> fMap)
	{
		return new AbstractBinaryFunction<C, Function<? super I, O>,
										  Collection<O>>(fMap, "MapCollection")
		{
			@Override
			public Collection<O> evaluate(
				C					   rInputCollection,
				Function<? super I, O> rFunction)
			{
				@SuppressWarnings("unchecked")
				Collection<O> aResult =
					(Collection<O>) CollectionUtil.newCollectionLike(
						rInputCollection);

				for (I rValue : rInputCollection)
				{
					aResult.add(rFunction.evaluate(rValue));
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * Returns a function constant that creates a new list which contains the
	 * elements from another list that it receives as the input value.
	 *
	 * @return A constant function instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<List<T>, List<T>> newList()
	{
		return rList -> new ArrayList<T>(rList);
	}

	/***************************************
	 * Returns a new function instance that reduces a collection into a single
	 * value by evaluating all collection elements with a function.
	 *
	 * @param  fReduce The function to evaluate the collection elements with
	 *
	 * @return A new function instance
	 */
	public static <T, C extends Collection<T>> BinaryFunction<C, BinaryFunction<T, T, T>, T>
	reduce(BinaryFunction<T, T, T> fReduce)
	{
		return new AbstractBinaryFunction<C, BinaryFunction<T, T, T>, T>(
			fReduce,
			"Reduce")
		{
			@Override
			public T evaluate(C rCollection, BinaryFunction<T, T, T> rFunction)
			{
				T aResult = null;

				for (T rValue : rCollection)
				{
					aResult =
						aResult != null ? rFunction.evaluate(aResult, rValue)
										: rValue;
				}

				return aResult;
			}
		};
	}
}
