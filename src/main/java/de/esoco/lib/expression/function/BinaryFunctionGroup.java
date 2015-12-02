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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Function;

import java.util.List;


/********************************************************************
 * A binary function implementation that groups an arbitrary number of binary
 * functions and applies all of them successively to input values in the same
 * order in which they are added to the group.
 *
 * @author eso
 */
public class BinaryFunctionGroup<L, R> extends AbstractBinaryFunction<L, R, L>
{
	//~ Instance fields --------------------------------------------------------

	private List<BinaryFunction<? super L, ? super R, ?>> rFunctions;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain number of functions.
	 *
	 * @param rRightValue The default value for unary function invocations
	 * @param rFunctions  The functions to group in this instance
	 */
	public BinaryFunctionGroup(
		R											  rRightValue,
		List<BinaryFunction<? super L, ? super R, ?>> rFunctions)
	{
		super(rRightValue, "Group" + rFunctions);

		this.rFunctions = rFunctions;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies all functions to the left and right input values and returns the
	 * left input value unchanged to support function chaining.
	 *
	 * @see BinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public L evaluate(L rLeft, R rRight)
	{
		for (BinaryFunction<? super L, ? super R, ?> rFunction : rFunctions)
		{
			rFunction.evaluate(rLeft, rRight);
		}

		return rLeft;
	}

	/***************************************
	 * Returns TRUE if all functions in this group are equal.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		BinaryFunctionGroup<?, ?> rOtherFunction =
			(BinaryFunctionGroup<?, ?>) rOther;
		int						  nCount		 = rFunctions.size();

		if (nCount != rOtherFunction.rFunctions.size())
		{
			return false;
		}

		for (int i = 0; i < nCount; i++)
		{
			if (!rFunctions.get(i).equals(rOtherFunction.rFunctions.get(i)))
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

		for (Function<? super L, ?> rFunction : rFunctions)
		{
			nHashCode = nHashCode * 31 + rFunction.hashCode();
		}

		return nHashCode;
	}
}
