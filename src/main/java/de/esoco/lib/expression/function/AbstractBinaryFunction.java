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
import de.esoco.lib.expression.Functions;


/********************************************************************
 * An abstract base implementation of the {@link BinaryFunction} interface. It
 * accepts the initial right value for unary function evaluations as a
 * constructor argument. The right value can be modified later by invoking the
 * method {@link #setRightValue(Object)} unless an instance has been set to be
 * immutable through a constructor parameter.
 *
 * @author eso
 */
public abstract class AbstractBinaryFunction<L, R, O>
	extends AbstractFunction<L, O> implements BinaryFunction<L, R, O>
{
	//~ Instance fields --------------------------------------------------------

	private R			  rRightValue;
	private final boolean bImmutable;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new immutable instance with a particular right value and
	 * function description.
	 *
	 * @see #AbstractBinaryFunction(Object, String, boolean)
	 */
	public AbstractBinaryFunction(R rRightValue, String sToken)
	{
		this(rRightValue, sToken, true);
	}

	/***************************************
	 * Creates a new instance with a particular right value and function
	 * description.
	 *
	 * <p>The boolean parameter allows to control whether the function will
	 * initially be immutable, i.e. if it's right value can be modified by means
	 * of the method {@link #setRightValue(Object)}.</p>
	 *
	 * @param rRightValue The right value of function input
	 * @param sToken      A text describing the function
	 * @param bImmutable  TRUE to prevent changes of the right value
	 */
	public AbstractBinaryFunction(R		  rRightValue,
								  String  sToken,
								  boolean bImmutable)
	{
		super(sToken);

		this.rRightValue = rRightValue;
		this.bImmutable  = bImmutable;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Implemented as final to delegate the evaluation of this function to the
	 * binary method {@link #evaluate(Object, Object)}.
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
	 * @see BinaryFunction#from(Function)
	 */
	@Override
	public <A, B> BinaryFunction<A, B, O> from(
		Function<A, ? extends L> rLeft,
		Function<B, ? extends R> rRight)
	{
		return Functions.chain(this, rLeft, rRight);
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
	 * Returns the immutable state of this instance. See the documentation of
	 * the constructor {@link #AbstractBinaryFunction(Object, String, boolean)}
	 * for details.
	 *
	 * @return The immutable stable
	 */
	public final boolean isImmutable()
	{
		return bImmutable;
	}

	/***************************************
	 * Sets the right value for the evaluation of this function. If it has been
	 * set to be immutable with the corresponding constructor parameter an
	 * exception will be thrown instead.
	 *
	 * @param  rValue The new right value
	 *
	 * @throws IllegalStateException If this function has been made immutable
	 */
	@Override
	public final void setRightValue(R rValue)
	{
		if (bImmutable)
		{
			throw new IllegalStateException("Binary function is immutable: " +
											this);
		}

		rRightValue = rValue;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	public <T> BinaryFunction<L, R, T> thenLeft(
		BinaryFunction<? super O, R, T> rOther)
	{
		return Functions.chainLeft(rOther, this);
	}

	/***************************************
	 * {@inheritDoc}
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
	public <T> BinaryFunction<L, T, O> withRight(
		Function<T, ? extends R> fRight)
	{
		return from(Functions.<L>identity(), fRight);
	}

	/***************************************
	 * Compares the right value and immutable flag with that of the other
	 * function.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		AbstractBinaryFunction<?, ?, ?> rOtherFunction =
			(AbstractBinaryFunction<?, ?, ?>) rOther;

		boolean bEqual = (bImmutable == rOtherFunction.bImmutable);

		if (bEqual)
		{
			if (rRightValue != null)
			{
				bEqual = rRightValue.equals(rOtherFunction.rRightValue);
			}
			else
			{
				bEqual = rOtherFunction.rRightValue == null;
			}
		}

		return bEqual;
	}

	/***************************************
	 * Generates a hash code from the right value and the immutable flag.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return (rRightValue != null ? rRightValue.hashCode() : 0) +
			   (bImmutable ? 1 : 0);
	}
}
