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
package de.esoco.lib.expression;

import de.esoco.lib.expression.predicate.AbstractBinaryPredicate;
import de.esoco.lib.expression.predicate.AbstractPredicate;
import de.esoco.lib.expression.predicate.Comparison.ElementOf;

import java.util.Arrays;
import java.util.Collection;


/********************************************************************
 * Contains several constants and factory methods for collection-related
 * predicates.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class CollectionPredicates
{
	//~ Static fields/initializers ---------------------------------------------

	/** Always returns true */
	private static final Predicate<Collection<?>> IS_EMPTY =
		new AbstractPredicate<Collection<?>>("IsEmpty")
		{
			@Override
			public final Boolean evaluate(Collection<?> rCollection)
			{
				return rCollection.isEmpty();
			}
		};

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private CollectionPredicates()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new binary predicate that evaluates to TRUE if a collection
	 * contains a certain element.
	 *
	 * @param  rElement The collection element to check for
	 *
	 * @return A new predicate
	 */
	public static <T, C extends Collection<T>> BinaryPredicate<C, T> contains(
		T rElement)
	{
		return new AbstractBinaryPredicate<C, T>(rElement, "Contains")
		{
			@Override
			public Boolean evaluate(C rCollection, T rElement)
			{
				return rCollection.contains(rElement);
			}
		};
	}

	/***************************************
	 * Returns a new predicate that checks if a target object is an element of
	 * the given collection.
	 *
	 * @param  rCollection The collection to check target objects against
	 *
	 * @return A new instance of the {@link ElementOf} predicate
	 */
	public static <T> Predicate<T> elementOf(Collection<?> rCollection)
	{
		return new ElementOf<T>(rCollection);
	}

	/***************************************
	 * Returns a predicate that checks if a target object is an element of the
	 * argument list of objects.
	 *
	 * @param  rValues The list of values to check target objects against
	 *
	 * @return A new instance of the {@link ElementOf} predicate
	 */
	public static <T> Predicate<T> elementOf(Object... rValues)
	{
		return new ElementOf<T>(Arrays.asList(rValues));
	}

	/***************************************
	 * Returns a predicate constant that evaluates to TRUE if a collection is
	 * empty.
	 *
	 * @return A predicate constant
	 */
	public static Predicate<Collection<?>> isEmpty()
	{
		return IS_EMPTY;
	}
}
