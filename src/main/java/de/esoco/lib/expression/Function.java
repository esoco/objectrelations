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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.FunctionChain;

import java.util.function.Consumer;


/********************************************************************
 * Interface for functions that return a value that will be derived from an
 * input value. The generic parameters define the types of the input value (I)
 * and the resulting output value (O). For most implementations the easiest way
 * to implement a function will be to subclass {@link AbstractFunction}.
 *
 * @author eso
 */
@FunctionalInterface
public interface Function<I, O> extends java.util.function.Function<I, O>
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * The placeholder string that is used to display the input value of a
	 * function in the result of the {@link Object#toString()} method.
	 */
	public static final String INPUT_PLACEHOLDER = "#";

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Evaluates the function on the input value and returns the resulting
	 * value. The declaration of this method does not contain a throws clause to
	 * make the execution of standard functions as simple as possible. In cases
	 * where a function implementation needs to signal a checked exception it
	 * should wrap it in an {@link FunctionException} which is a subclass of
	 * {@link RuntimeException}. In such a case the possibility of the
	 * occurrence of such exceptions should be documented appropriately in the
	 * function documentation.
	 *
	 * @param  rValue The input value of the function
	 *
	 * @return The resulting (output) value (may be NULL)
	 */
	public O evaluate(I rValue);

	/***************************************
	 * Creates an action that consumes the result of evaluating an input value.
	 *
	 * @param  fAction The action that consumes the function result
	 *
	 * @return A new action (and {@link Consumer}) for input values of this
	 *         function
	 */
	default Action<I> andFinally(Action<O> fAction)
	{
		return i -> fAction.accept(this.evaluate(i));
	}

	/***************************************
	 * Invokes {@link #evaluate(Object)}.
	 *
	 * @see java.util.function.Function#apply(Object)
	 */
	@Override
	default O apply(I rInput)
	{
		return evaluate(rInput);
	}

	/***************************************
	 * Returns a new function object that evaluates the result received from
	 * another function with this function.
	 *
	 * @param  fPrevious The function to produce this function's input values
	 *                   with
	 *
	 * @return A new instance of {@link FunctionChain}
	 */
	default <T> Function<T, O> from(Function<T, ? extends I> fPrevious)
	{
		return fPrevious.then(this);
	}

	/***************************************
	 * Returns the token that describes this function instance. The default
	 * implementation returns the simple name of the function class.
	 *
	 * @return The function token
	 */
	default String getToken()
	{
		return getClass().getSimpleName();
	}

	/***************************************
	 * Returns a predicate that evaluates the result of this function.
	 *
	 * @param  pCriteria The criteria predicate to evaluate the function result
	 *
	 * @return The function predicate
	 */
	default <T extends I> Predicate<T> is(Predicate<? super O> pCriteria)
	{
		return Predicates.when(this, pCriteria);
	}

	/***************************************
	 * Returns a new function object that evaluates the result of this function
	 * with another function and returns the result.
	 *
	 * @param  fNext The function to evaluate this function's output values with
	 *
	 * @return A new instance of {@link FunctionChain}
	 */
	default <T> Function<I, T> then(Function<? super O, T> fNext)
	{
		return Functions.chain(fNext, this);
	}
}
