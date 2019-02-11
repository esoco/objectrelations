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

	private final Predicate<? super I>			   pCondition;
	private final Function<? super I, ? extends O> fTrue;
	private final Function<? super I, ? extends O> fFalse;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a simple if expression (i.e. without else).
	 *
	 * @param pCondition The predicate to be evaluated for input values
	 * @param fTrue      The function to be applied to input values for which
	 *                   the predicate yields TRUE
	 */
	public ConditionalFunction(
		Predicate<? super I>   pCondition,
		Function<? super I, O> fTrue)
	{
		this(pCondition, fTrue, null);
	}

	/***************************************
	 * Creates a new instance for an if-else expression.
	 *
	 * @param pCondition The predicate to be evaluated for input values
	 * @param fTrue      The function to be applied to input values for which
	 *                   the predicate yields TRUE
	 * @param fFalse     The function to be applied to input values for which
	 *                   the predicate yields FALSE
	 */
	public ConditionalFunction(Predicate<? super I>				pCondition,
							   Function<? super I, ? extends O> fTrue,
							   Function<? super I, ? extends O> fFalse)
	{
		super("IF");

		assert pCondition != null && fTrue != null;

		this.pCondition = pCondition;
		this.fTrue	    = fTrue;
		this.fFalse     = fFalse;
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
		return new ConditionalFunction<I, O>(pCondition, fTrue, rFunction);
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

		if (pCondition.test(rInput))
		{
			rResult = fTrue.apply(rInput);
		}
		else if (fFalse != null)
		{
			rResult = fFalse.apply(rInput);
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
		return "IF " + pCondition + " DO " + fTrue + " ELSE " +
			   (fFalse != null ? fFalse : "value=NULL");
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
			pCondition.equals(rOtherFunction.pCondition) &&
			fTrue.equals(rOtherFunction.fTrue);

		if (bEqual)
		{
			if (fFalse != null)
			{
				bEqual = fFalse.equals(rOtherFunction.fFalse);
			}
			else
			{
				bEqual = (rOtherFunction.fFalse == null);
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
		int nHashCode = pCondition.hashCode();

		nHashCode = nHashCode * 31 + fTrue.hashCode();
		nHashCode = nHashCode * 31 + (fFalse != null ? fFalse.hashCode() : 0);

		return nHashCode;
	}
}
