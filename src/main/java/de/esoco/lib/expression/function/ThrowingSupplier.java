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
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that allows to declare a throwing supplier from a lambda
	 * expression that is mapped to a regular supplier. Otherwise an anonymous
	 * inner class expression would be needed because of the similar signatures
	 * of throwing and non-throwing suppliers.
	 *
	 * @param  fThrowing The throwing supplier expression
	 *
	 * @return The resulting function
	 */
	public static <T> Supplier<T> of(ThrowingSupplier<T> fThrowing)
	{
		return fThrowing;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Replaces {@link #get()} and allows implementations to throw an exception.
	 *
	 * @return The function result
	 *
	 * @throws Throwable If the invocation fails
	 */
	public T tryGet() throws Throwable;

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #tryGet()} and to convert occurring exceptions
	 * into {@link FunctionException}.
	 *
	 * @see Supplier#get()
	 */
	@Override
	default T get()
	{
		try
		{
			return tryGet();
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw new FunctionException(this, e);
		}
	}
}
