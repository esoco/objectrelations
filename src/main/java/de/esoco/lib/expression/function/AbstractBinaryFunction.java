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

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Functions;


/********************************************************************
 * An abstract base implementation of the {@link BinaryFunction} interface. It
 * accepts the initial right value for unary function evaluations as a
 * constructor argument.
 *
 * @author eso
 */
public abstract class AbstractBinaryFunction<L, R, O>
	extends AbstractFunction<L, O> implements BinaryFunction<L, R, O>
{
	//~ Instance fields --------------------------------------------------------

	private R rRightValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a particular right value and the class name
	 * as the token.
	 *
	 * @param rRightValue The right value for unary evaluation
	 */
	public AbstractBinaryFunction(R rRightValue)
	{
		this(rRightValue, null);
	}

	/***************************************
	 * Creates a new instance with a particular right value and function token.
	 *
	 * @param rRightValue The right value for unary evaluation
	 * @param sToken      The function token
	 */
	public AbstractBinaryFunction(R rRightValue, String sToken)
	{
		super(sToken);

		this.rRightValue = rRightValue;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Implemented as final to delegate the evaluation of this function to the
	 * binary method {@link BinaryFunction#evaluate(Object, Object)}.
	 *
	 * @param  rLeftValue The left input value
	 *
	 * @return The result of evaluating the left and right values
	 */
	@Override
	public final O evaluate(L rLeftValue)
	{
		return evaluate(rLeftValue, rRightValue);
	}

	/***************************************
	 * @see BinaryFunction#getRightValue()
	 */
	@Override
	public final R getRightValue()
	{
		return rRightValue;
	}

	/***************************************
	 * Invokes {@link Functions#chainLeft(BinaryFunction, BinaryFunction)} with
	 * the other function first and then this.
	 *
	 * @see Functions#chainLeft(BinaryFunction, BinaryFunction)
	 */
	public <T> BinaryFunction<L, R, T> thenLeft(
		BinaryFunction<? super O, R, T> rOther)
	{
		return Functions.chainLeft(rOther, this);
	}

	/***************************************
	 * Invokes {@link Functions#chainRight(BinaryFunction, BinaryFunction)} with
	 * the other function first and then this.
	 *
	 * @see Functions#chainRight(BinaryFunction, BinaryFunction)
	 */
	public <T> BinaryFunction<L, R, T> thenRight(
		BinaryFunction<L, ? super O, T> rOther)
	{
		return Functions.chainRight(rOther, this);
	}

	/***************************************
	 * Overridden to format the raw function description as returned by the
	 * superclass method with the current right value by means of the method
	 * {@link String#format(String, Object...)}.
	 *
	 * @return A text describing this function instance
	 */
	@Override
	public String toString()
	{
		return getToken() + "(" + INPUT_PLACEHOLDER + ", " + rRightValue + ")";
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		AbstractBinaryFunction<?, ?, ?> rOtherFunction =
			(AbstractBinaryFunction<?, ?, ?>) rOther;

		return rRightValue != null
			   ? rRightValue.equals(rOtherFunction.rRightValue)
			   : rOtherFunction.rRightValue == null;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected int paramsHashCode()
	{
		return rRightValue != null ? rRightValue.hashCode() : 0;
	}
}
