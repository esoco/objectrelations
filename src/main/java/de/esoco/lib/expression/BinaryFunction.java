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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.DualFunctionChain;


/********************************************************************
 * Interface for binary functions that derived their result from two input
 * values. The generic parameters define the types of the left and right input
 * values (L, R) and the resulting output value (O). For most implementations
 * the easiest way to implement a binary function will be to create a subclass
 * of {@link AbstractBinaryFunction}.
 *
 * <p>To increase the usefulness of binary functions this interface extends the
 * standard (unary) {@link Function} interface which contains a single-argument
 * {@link Function#evaluate(Object)} method. To support the unary evaluation
 * this interface defines the method {@link #setRightValue(Object)} which
 * provides a way to set the right value of a binary function at runtime.
 * Applications that only want to use the direct evaluation of binary functions
 * may ignore the methods for unary usage.</p>
 *
 * @author eso
 */
public interface BinaryFunction<L, R, O> extends Function<L, O>
{
	//~ Methods ----------------------------------------------------------------

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
	 * from two other function with this binary function. Implementations should
	 * typically subclass {@link AbstractBinaryFunction} which already contains
	 * an implementation of this method.
	 *
	 * @param  fLeft  The function to product the left input values with
	 * @param  fRight The function to produce the right input values with
	 *
	 * @return A new instance of {@link DualFunctionChain}
	 */
	public <A, B> BinaryFunction<A, B, O> from(
		Function<A, ? extends L> fLeft,
		Function<B, ? extends R> fRight);

	/***************************************
	 * Returns the right value of this binary function.
	 *
	 * @return The right value
	 */
	public R getRightValue();

	/***************************************
	 * Sets the right value of this binary function.
	 *
	 * @param rRightValue The new right value
	 */
	public void setRightValue(R rRightValue);

	/***************************************
	 * Returns a new function object that evaluates this function with the
	 * original input value and a right value from the output value received
	 * from another function. Implementations should typically subclass {@link
	 * AbstractBinaryFunction} which already contains an implementation of this
	 * method.
	 *
	 * @param  fRight The function to produce the right input values with
	 *
	 * @return A new instance of {@link DualFunctionChain}
	 */
	public <T> BinaryFunction<L, T, O> withRight(
		Function<T, ? extends R> fRight);
}
