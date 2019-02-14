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

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.FunctionException;


/********************************************************************
 * A sub-interface that allows implementations to throw checked exceptions. If
 * an exception occurs it will be converted into a runtime exception of the type
 * {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingBinaryFunction<L, R, O> extends BinaryFunction<L, R, O>
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that allows to declare a throwing function from a lambda
	 * expression that is mapped to a regular function. Otherwise an anonymous
	 * inner class expression would be needed because of the similar signatures
	 * of throwing and non-throwing functions.
	 *
	 * @param  fThrowing The throwing function expression
	 *
	 * @return The resulting function
	 */
	public static <L, R, O> BinaryFunction<L, R, O> of(
		ThrowingBinaryFunction<L, R, O> fThrowing)
	{
		return fThrowing;
	}

	//~ Methods ----------------------------------------------------------------

	// ~ Methods ------------------------------------------------------------

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #tryApply(Object, Object)} and to convert
	 * occurring exceptions into {@link FunctionException}.
	 *
	 * @see BinaryFunction#evaluate(Object, Object)
	 */
	@Override
	default public O evaluate(L rLeft, R rRight)
	{
		try
		{
			return tryApply(rLeft, rRight);
		}
		catch (Throwable e)
		{
			throw (e instanceof RuntimeException)
				  ? (RuntimeException) e : new FunctionException(this, e);
		}
	}

	/***************************************
	 * A variant of {@link #evaluate(Object)} that allows implementations to
	 * throw an exception.
	 *
	 * @param  rLeft  The left argument
	 * @param  rRight The right argument
	 *
	 * @return The function result
	 *
	 * @throws Throwable Any kind of exception may be thrown
	 */
	public O tryApply(L rLeft, R rRight) throws Throwable;
}
