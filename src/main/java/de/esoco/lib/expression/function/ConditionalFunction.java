//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.function;

import java.util.function.Function;
import java.util.function.Predicate;


/********************************************************************
 * A conditional function implementation that only evaluates a function on the
 * input value if a certain predicate yields TRUE for that value.
 *
 * @author eso
 */
public class ConditionalFunction<I, O> extends AbstractFunction<I, O>
{
	//~ Instance fields --------------------------------------------------------

	private final Predicate<? super I>			   rPredicate;
	private final Function<? super I, ? extends O> rTrueFunction;
	private final Function<? super I, ? extends O> rFalseFunction;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a simple if expression (i.e. without else).
	 *
	 * @param rPredicate The predicate to be evaluated for input values
	 * @param rFunction  The function to be applied to input values for which
	 *                   the predicate yields TRUE
	 */
	public ConditionalFunction(
		Predicate<? super I>   rPredicate,
		Function<? super I, O> rFunction)
	{
		this(rPredicate, rFunction, null);
	}

	/***************************************
	 * Creates a new instance for an if-else expression.
	 *
	 * @param rPredicate     The predicate to be evaluated for input values
	 * @param rTrueFunction  The function to be applied to input values for
	 *                       which the predicate yields TRUE
	 * @param rFalseFunction The function to be applied to input values for
	 *                       which the predicate yields FALSE
	 */
	public ConditionalFunction(Predicate<? super I>				rPredicate,
							   Function<? super I, ? extends O> rTrueFunction,
							   Function<? super I, ? extends O> rFalseFunction)
	{
		super("IF");

		assert rPredicate != null && rTrueFunction != null;

		this.rPredicate     = rPredicate;
		this.rTrueFunction  = rTrueFunction;
		this.rFalseFunction = rFalseFunction;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a new conditional function that contains a function to be
	 * evaluated if the predicate of this instance yields FALSE. This is a
	 * convenience method that is more expressive for the concatenation of
	 * conditional functions than the three-argument constructor.
	 *
	 * @param  rFunction The function to be evaluated if the predicates yields
	 *                   FALSE
	 *
	 * @return A new conditional function
	 */
	public Function<I, O> elseDo(Function<? super I, ? extends O> rFunction)
	{
		return new ConditionalFunction<I, O>(
			rPredicate,
			rTrueFunction,
			rFunction);
	}

	/***************************************
	 * Evaluates this instance's function on the input value if the predicate
	 * yields TRUE for that value. Else NULL will be returned.
	 *
	 * @see de.esoco.lib.expression.Function#evaluate(Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public O evaluate(I rInput)
	{
		O rResult = null;

		if (rPredicate.test(rInput))
		{
			rResult = rTrueFunction.apply(rInput);
		}
		else if (rFalseFunction != null)
		{
			rResult = rFalseFunction.apply(rInput);
		}

		return rResult;
	}

	/***************************************
	 * Overridden to return a specific format.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString()
	{
		return "IF " + rPredicate + " DO " + rTrueFunction + " ELSE " +
			   (rFalseFunction != null ? rFalseFunction : "value=NULL");
	}

	/***************************************
	 * Compares the predicate and the result functions of this instance for
	 * equality.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		ConditionalFunction<?, ?> rOtherFunction =
			(ConditionalFunction<?, ?>) rOther;

		boolean bEqual =
			rPredicate.equals(rOtherFunction.rPredicate) &&
			rTrueFunction.equals(rOtherFunction.rTrueFunction);

		if (bEqual)
		{
			if (rFalseFunction != null)
			{
				bEqual = rFalseFunction.equals(rOtherFunction.rFalseFunction);
			}
			else
			{
				bEqual = (rOtherFunction.rFalseFunction == null);
			}
		}

		return bEqual;
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		int nHashCode = rPredicate.hashCode();

		nHashCode = nHashCode * 31 + rTrueFunction.hashCode();
		nHashCode =
			nHashCode * 31 +
			(rFalseFunction != null ? rFalseFunction.hashCode() : 0);

		return nHashCode;
	}
}
