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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.AbstractFunction;


/********************************************************************
 * An abstract base class for predicates. It implements the methods {@link
 * #and(Predicate)} and {@link #or(Predicate)}.
 *
 * @author eso
 */
public abstract class AbstractPredicate<T> extends AbstractFunction<T, Boolean>
	implements Predicate<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain description string.
	 *
	 * @param sToken The description of this predicate
	 */
	public AbstractPredicate(String sToken)
	{
		super(sToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Predicate#and(Predicate)
	 */
	@Override
	public <O extends T> Predicate<O> and(Predicate<? super T> rOther)
	{
		return Predicates.and(this, rOther);
	}

	/***************************************
	 * Overridden to return a predicate instead of a function so that the result
	 * can still be used as a predicate.
	 *
	 * @param  rFunction A function that produces input for this predicate
	 *
	 * @return A new predicate that evaluates the result of the argument
	 *         function
	 */
	@Override
	public <I> Predicate<I> from(Function<I, ? extends T> rFunction)
	{
		return Predicates.chain(this, rFunction);
	}

	/***************************************
	 * @see Predicate#or(Predicate)
	 */
	@Override
	public <O extends T> Predicate<O> or(Predicate<? super T> rOther)
	{
		return Predicates.or(this, rOther);
	}

	/***************************************
	 * Overridden to implement a predicate-specific formatting.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString()
	{
		return INPUT_PLACEHOLDER + " " + getToken();
	}
}
