//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/********************************************************************
 * A function implementation that groups an arbitrary number of functions and
 * applies all of them successively to input values in the same order in which
 * they are added to the group. The list of functions of an instance can be
 * queried with {@link #getFunctions()} and it can be modified to change the
 * functions executed by an instance.
 *
 * @author eso
 */
public class FunctionGroup<I> extends AbstractFunction<I, I>
{
	//~ Instance fields --------------------------------------------------------

	private List<Function<? super I, ?>> aFunctions;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain set of functions to be executed.
	 * The given function list will be used directly by this instance so to make
	 * an immutable function group this list should be immutable.
	 *
	 * @param rFunctions The list of functions to be wrapped by this instance
	 */
	public FunctionGroup(List<Function<? super I, ?>> rFunctions)
	{
		super("FunctionGroup");

		aFunctions = rFunctions;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A factory method that creates a mutable function group.
	 *
	 * @param  fFirst               The first function to evaluate
	 * @param  rAdditionalFunctions Optional additional functions to evaluate
	 *
	 * @return A new function group instance with a mutable function list
	 */
	@SafeVarargs
	public static <I> FunctionGroup<I> of(
		Function<? super I, ?>    fFirst,
		Function<? super I, ?>... rAdditionalFunctions)
	{
		List<Function<? super I, ?>> aFunctions =
			new ArrayList<Function<? super I, ?>>();

		aFunctions.add(fFirst);

		if (rAdditionalFunctions != null && rAdditionalFunctions.length > 0)
		{
			aFunctions.addAll(Arrays.asList(rAdditionalFunctions));
		}

		return new FunctionGroup<>(aFunctions);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies all functions to the input value and returns the input value
	 * unchanged to support function chaining.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	public I evaluate(I rInput)
	{
		for (Function<? super I, ?> rFunction : aFunctions)
		{
			rFunction.evaluate(rInput);
		}

		return rInput;
	}

	/***************************************
	 * Returns the functions of this group. The returned list is a copy of the
	 * internal function list and can be manipulated freely.
	 *
	 * @return A new list containing the functions of this group
	 */
	public List<Function<? super I, ?>> getFunctions()
	{
		return new ArrayList<>(aFunctions);
	}

	/***************************************
	 * Returns TRUE if all functions in this group are equal.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		FunctionGroup<?> rOtherFunction = (FunctionGroup<?>) rOther;
		int				 nCount		    = aFunctions.size();

		if (nCount != rOtherFunction.aFunctions.size())
		{
			return false;
		}

		for (int i = 0; i < nCount; i++)
		{
			if (!aFunctions.get(i).equals(rOtherFunction.aFunctions.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * Calculates the combined hash code of all functions in this group.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		int nHashCode = 17;

		for (Function<? super I, ?> rFunction : aFunctions)
		{
			nHashCode = nHashCode * 31 + rFunction.hashCode();
		}

		return nHashCode;
	}
}
