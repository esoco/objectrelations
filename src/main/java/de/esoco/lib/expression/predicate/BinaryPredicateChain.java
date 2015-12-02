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
package de.esoco.lib.expression.predicate;

import de.esoco.lib.expression.BinaryPredicate;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.DualFunctionChain;


/********************************************************************
 * A subclass of {@link DualFunctionChain} for predicates that also implements
 * the binary predicate interface. This allows to create a new predicate
 * instance from the chaining of a binary predicate with a function.
 *
 * @author eso
 */
public class BinaryPredicateChain<L, R, V, W>
	extends DualFunctionChain<L, R, V, W, Boolean>
	implements BinaryPredicate<L, R>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rOuter The predicate that evaluates the results of the left and
	 *               right functions
	 * @param rLeft  The function that produces the left predicate input
	 * @param rRight The function that produces the right predicate input
	 */
	public BinaryPredicateChain(BinaryPredicate<V, W>    rOuter,
								Function<L, ? extends V> rLeft,
								Function<R, ? extends W> rRight)
	{
		super(rOuter, rLeft, rRight);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Predicate#and(Predicate)
	 */
	@Override
	public <O extends L> Predicate<O> and(Predicate<? super L> rOther)
	{
		return Predicates.and(this, rOther);
	}

	/***************************************
	 * @see Predicate#from(Function)
	 */
	@Override
	public <I> Predicate<I> from(Function<I, ? extends L> rFunction)
	{
		return Predicates.chain(this, rFunction);
	}

	/***************************************
	 * @see BinaryPredicate#from(Function, Function)
	 */
	@Override
	public <A, B> BinaryPredicate<A, B> from(
		Function<A, ? extends L> rLeft,
		Function<B, ? extends R> rRight)
	{
		return Predicates.chain(this, rLeft, rRight);
	}

	/***************************************
	 * @see Predicate#or(Predicate)
	 */
	@Override
	public <O extends L> Predicate<O> or(Predicate<? super L> rOther)
	{
		return Predicates.or(this, rOther);
	}
}
