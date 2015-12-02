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
import de.esoco.lib.expression.function.AbstractBinaryFunction;


/********************************************************************
 * An abstract predicate implementation for binary predicates. See the base
 * class {@link AbstractBinaryFunction} for details.
 *
 * @author eso
 */
public abstract class AbstractBinaryPredicate<L, R>
	extends AbstractBinaryFunction<L, R, Boolean>
	implements BinaryPredicate<L, R>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new immutable instance with a particular right value and
	 * description.
	 *
	 * @see AbstractBinaryFunction#AbstractBinaryFunction(Object, String)
	 */
	public AbstractBinaryPredicate(R rRightValue, String sDescription)
	{
		super(rRightValue, sDescription);
	}

	/***************************************
	 * Creates a new instance with a particular right value, description, and
	 * immutable state.
	 *
	 * @see AbstractBinaryFunction#AbstractBinaryFunction(Object, String,
	 *      boolean)
	 */
	public AbstractBinaryPredicate(R	   rRightValue,
								   String  sToken,
								   boolean bImmutable)
	{
		super(rRightValue, sToken, bImmutable);
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

	/***************************************
	 * Overridden to implement a predicate-specific formatting.
	 *
	 * @see AbstractBinaryFunction#toString()
	 */
	@Override
	public String toString()
	{
		return INPUT_PLACEHOLDER + " " + getToken() + " " + getRightValue();
	}
}
