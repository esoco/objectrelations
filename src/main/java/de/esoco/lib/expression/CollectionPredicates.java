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
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private CollectionPredicates()
	{
	}

	//~ Static methods ---------------------------------------------------------

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
}
