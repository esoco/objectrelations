//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Function;


/********************************************************************
 * Implements a function chain that evaluates an outer function with the result
 * of an inner function and then returns the result of the outer function.
 *
 * @author eso
 */
public class FunctionChain<I, V, O> extends AbstractFunction<I, O>
{
	//~ Instance fields --------------------------------------------------------

	private final Function<V, O>		   rOuter;
	private final Function<I, ? extends V> rInner;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that chains two functions together.
	 *
	 * @param rOuter The left function
	 * @param rInner The right function
	 */
	public FunctionChain(Function<V, O>			  rOuter,
						 Function<I, ? extends V> rInner)
	{
		super(".");

		this.rOuter = rOuter;
		this.rInner = rInner;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Function#evaluate(Object)
	 */
	@Override
	public O evaluate(I rInput)
	{
		return rOuter.evaluate(rInner.evaluate(rInput));
	}

	/***************************************
	 * Returns the inner function of this chain which is evaluated first and
	 * produces the output that is used as the input of the outer function.
	 *
	 * @return The inner function
	 */
	public final Function<I, ? extends V> getInner()
	{
		return rInner;
	}

	/***************************************
	 * Returns the outer function of this chain which is evaluated last with the
	 * result of the inner function as the input.
	 *
	 * @return The outer function
	 */
	public final Function<V, O> getOuter()
	{
		return rOuter;
	}

	/***************************************
	 * Returns a string representation of this function chain.
	 *
	 * @return A string representation of this chain
	 */
	@Override
	public String toString()
	{
		String sResult = rOuter.toString();

		if (sResult.indexOf(INPUT_PLACEHOLDER) >= 0)
		{
			sResult = sResult.replace(INPUT_PLACEHOLDER, rInner.toString());
		}
		else
		{
			sResult += "(" + rInner.toString() + ")";
		}

		return sResult;
	}

	/***************************************
	 * Compares the left and right functions with that of another function.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		FunctionChain<?, ?, ?> rOtherFunction = (FunctionChain<?, ?, ?>) rOther;

		return rOuter.equals(rOtherFunction.rOuter) &&
			   rInner.equals(rOtherFunction.rInner);
	}

	/***************************************
	 * Calculates the combined hash code of the left and right functions.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return 31 * rOuter.hashCode() + rInner.hashCode();
	}
}
