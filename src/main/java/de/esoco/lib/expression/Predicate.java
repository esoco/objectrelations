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

/********************************************************************
 * Interface for predicate functions that return a boolean value that is derived
 * from a target object.
 *
 * @author eso
 */
@FunctionalInterface
public interface Predicate<T> extends Function<T, Boolean>,
									  java.util.function.Predicate<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a new predicate with a logical AND expression for this and
	 * another instance. The other predicate will only be evaluated if this
	 * predicate yields TRUE.
	 *
	 * @param  rOther The other instance
	 *
	 * @return A new predicate with a logical AND expression
	 */
	default <O extends T> Predicate<O> and(Predicate<? super T> rOther)
	{
		return Predicates.and(this, rOther);
	}

	/***************************************
	 * Re-defined from {@link Function#from(Function)} to return a new predicate
	 * instead of a function so that the result can still be used as a
	 * predicate.
	 *
	 * @param  rFunction A function that produces input for this predicate
	 *
	 * @return A new predicate that first applies the argument function to input
	 *         values and then evaluates the result with this predicate
	 */
	@Override
	default <V> Predicate<V> from(Function<V, ? extends T> rFunction)
	{
		return Predicates.chain(this, rFunction);
	}

	/***************************************
	 * Returns a new predicate with a logical OR expression for this and another
	 * instance. The other predicate will only be evaluated if this predicate
	 * yields FALSE.
	 *
	 * @param  rOther The other instance
	 *
	 * @return A new predicate with a logical OR expression
	 */
	default <O extends T> Predicate<O> or(Predicate<? super T> rOther)
	{
		return Predicates.or(this, rOther);
	}

	/***************************************
	 * Invokes {@link #evaluate(Object)}.
	 *
	 * @see java.util.function.Predicate#test(Object)
	 */
	@Override
	default boolean test(T rValue)
	{
		return evaluate(rValue);
	}
}
