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
import de.esoco.lib.expression.function.AbstractFunction;


/********************************************************************
 * Base class for joins of two predicates (like logical combinations).
 * Subclasses must implement the {@link Function#evaluate(Object)} method to
 * implement the actual join of the two predicates in the correct order. The
 * predicates can be queried with the methods {@link #getLeft()} and {@link
 * #getRight()}.
 *
 * @author eso
 */
public abstract class PredicateJoin<T> extends AbstractPredicate<T>
{
	//~ Instance fields --------------------------------------------------------

	private final Predicate<? super T> rLeft;
	private final Predicate<? super T> rRight;
	private final String			   sJoinToken;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rLeft      The left predicate that will be evaluated first
	 * @param rRight     The right predicate
	 * @param sJoinToken A string token that describes the join
	 */
	public PredicateJoin(Predicate<? super T> rLeft,
						 Predicate<? super T> rRight,
						 String				  sJoinToken)
	{
		// use empty string because toString is overridden
		super("");

		if (rLeft == null || rRight == null)
		{
			throw new IllegalArgumentException("Predicates must not be NULL");
		}

		this.rLeft	    = rLeft;
		this.rRight     = rRight;
		this.sJoinToken = sJoinToken;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Implemented as final, subclasses must implement the abstract method
	 * {@link #evaluate(Predicate, Predicate, Object)} instead.
	 *
	 * @param  rValue The value to be evaluated by the join predicates
	 *
	 * @return The result of the evaluation
	 */
	@Override
	public final Boolean evaluate(T rValue)
	{
		return evaluate(rLeft, rRight, rValue);
	}

	/***************************************
	 * Returns the left predicate of this join.
	 *
	 * @return The left predicate
	 */
	public final Predicate<? super T> getLeft()
	{
		return rLeft;
	}

	/***************************************
	 * Returns the right predicate of this instance.
	 *
	 * @return The right predicate
	 */
	public final Predicate<? super T> getRight()
	{
		return rRight;
	}

	/***************************************
	 * Returns a string representation of this join.
	 *
	 * @return A string representation of this join
	 */
	@Override
	public String toString()
	{
		return "(" + rLeft + " " + sJoinToken + " " + rRight + ")";
	}

	/***************************************
	 * Must be implemented by subclasses for the actual evaluation of this join.
	 *
	 * @param  rLeft  The left predicate
	 * @param  rRight The right predicate
	 * @param  rValue The value to be evaluated by the predicates
	 *
	 * @return The result of the combined evaluation
	 */
	protected abstract Boolean evaluate(Predicate<? super T> rLeft,
										Predicate<? super T> rRight,
										T					 rValue);

	/***************************************
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		PredicateJoin<?> rOtherPredicate = (PredicateJoin<?>) rOther;

		return rLeft.equals(rOtherPredicate.rLeft) &&
			   rRight.equals(rOtherPredicate.rRight);
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return 31 * rLeft.hashCode() + rRight.hashCode();
	}
}
