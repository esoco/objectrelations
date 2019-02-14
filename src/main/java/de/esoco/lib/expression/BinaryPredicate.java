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
 * Interface for binary predicates that derive their result from two input
 * values. The generic parameters define the types of the left and right input
 * values (L, R).
 *
 * @author eso
 */
public interface BinaryPredicate<L, R> extends Predicate<L>,
											   BinaryFunction<L, R, Boolean>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Default implementation that invokes {@link #evaluate(Object, Object)}
	 * with the return value of {@link #getRightValue()} as the right argument.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	default public Boolean evaluate(L rLeftValue)
	{
		return evaluate(rLeftValue, getRightValue());
	}

	/***************************************
	 * Re-defined from {@link BinaryFunction#from(Function, Function)} to return
	 * a new binary predicate instead of a function so that the result can still
	 * be used as a predicate.
	 *
	 * @param  rLeft  A function that produces the left value of this predicate
	 * @param  rRight A function that produces the right value of this predicate
	 *
	 * @return A new predicate that first applies the argument functions to
	 *         input values and then evaluates the result with this predicate
	 */
	@Override
	default public <A, B> BinaryPredicate<A, B> from(
		Function<A, ? extends L> rLeft,
		Function<B, ? extends R> rRight)
	{
		return Predicates.chain(this, rLeft, rRight);
	}
}
