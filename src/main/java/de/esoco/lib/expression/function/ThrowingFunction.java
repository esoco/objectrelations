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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.FunctionException;


/********************************************************************
 * A sub-interface that allows implementations to throw checked exceptions. If
 * an exception occurs it will be converted into a runtime exception of the type
 * {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingFunction<I, O> extends Function<I, O>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #evaluateWithException(Object)} and to convert
	 * occurring exceptions into {@link FunctionException}.
	 *
	 * @see Function#evaluate(Object)
	 */
	@Override
	default public O evaluate(I rInput)
	{
		try
		{
			return evaluateWithException(rInput);
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
			{
				throw (RuntimeException) e;
			}
			else
			{
				throw new FunctionException(this, e);
			}
		}
	}

	/***************************************
	 * Replaces {@link #evaluate(Object)} and allows implementations to throw an
	 * exception.
	 *
	 * @param  rInput The input value
	 *
	 * @return The function result
	 *
	 * @throws Exception An exception in the case of errors
	 */
	public O evaluateWithException(I rInput) throws Exception;
}
