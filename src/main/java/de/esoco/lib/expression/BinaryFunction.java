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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.DualFunctionChain;

import java.util.function.BiFunction;


/********************************************************************
 * Interface for binary functions that derived their result from two input
 * values. The generic parameters define the types of the left and right input
 * values (L, R) and the resulting output value (O).
 *
 * <p>To increase the usefulness of binary functions this interface extends the
 * standard (unary) {@link Function} interface which contains a single-argument
 * {@link Function#evaluate(Object)} method. To support the unary evaluation
 * this interface defines the method {@link #getRightValue()} that can be
 * overridden by subclasses to provide a default right value, for example.
 * Applications that only want to use the direct evaluation of binary functions
 * may ignore the methods for unary usage.</p>
 *
 * @author eso
 */
@FunctionalInterface
public interface BinaryFunction<L, R, O> extends Function<L, O>
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Takes a binary function that throws an exception and returns it as a
	 * function that can be executed without checked exception. This method is
	 * mainly intended to be used with lambdas that throw exceptions.
	 *
	 * @param  fChecked The checked binary function to wrap as unchecked
	 *
	 * @return The unchecked binary function
	 */
	public static <L, R, O> BiFunction<L, R, O> unchecked(
		ThrowingBinaryFunction<L, R, O> fChecked)
	{
		return fChecked;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Default implementation that invokes {@link #evaluate(Object, Object)}
	 * with the return value of {@link #getRightValue()} as the right argument.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	default public O evaluate(L rLeftValue)
	{
		return evaluate(rLeftValue, getRightValue());
	}

	/***************************************
	 * Evaluates the binary function on the left and right input values and
	 * returns the resulting value.
	 *
	 * @param  rLeftValue  The left value to evaluate
	 * @param  rRightValue The right value to evaluate
	 *
	 * @return The result of the evaluation
	 */
	public O evaluate(L rLeftValue, R rRightValue);

	/***************************************
	 * Returns a new function object that evaluates the output values received
	 * from two other function with this binary function.
	 *
	 * @param  fLeft  The function to product the left input values with
	 * @param  fRight The function to produce the right input values with
	 *
	 * @return A new instance of {@link DualFunctionChain}
	 */
	default public <A, B> BinaryFunction<A, B, O> from(
		Function<A, ? extends L> fLeft,
		Function<B, ? extends R> fRight)
	{
		return Functions.chain(this, fLeft, fRight);
	}

	/***************************************
	 * Returns the right value of this binary function. The default
	 * implementation always returns NULL.
	 *
	 * @return The right value
	 */
	default public R getRightValue()
	{
		return null;
	}

	/***************************************
	 * Returns a new function object that evaluates this function with the
	 * original input value and a right value from the output value received
	 * from another function.
	 *
	 * @param  fRight The function to produce the right input values with
	 *
	 * @return A new instance of {@link DualFunctionChain}
	 */
	default public <T> BinaryFunction<L, T, O> withRight(
		Function<T, ? extends R> fRight)
	{
		return from(Functions.identity(), fRight);
	}
}
