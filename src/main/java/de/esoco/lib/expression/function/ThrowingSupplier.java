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

import de.esoco.lib.expression.FunctionException;

import java.util.function.Supplier;


/********************************************************************
 * A {@link Supplier} extension that maps any occurring exception to a runtime
 * {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #getWithException()} and to convert occurring
	 * exceptions into {@link FunctionException}.
	 *
	 * @see Supplier#get()
	 */
	@Override
	default public T get()
	{
		try
		{
			return getWithException();
		}
		catch (Throwable e)
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
	 * Replaces {@link #get()} and allows implementations to throw an exception.
	 *
	 * @return The function result
	 *
	 * @throws Throwable If the invocation fails
	 */
	public T getWithException() throws Throwable;
}
