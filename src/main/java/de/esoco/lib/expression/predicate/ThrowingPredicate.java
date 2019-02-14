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
package de.esoco.lib.expression.predicate;

import de.esoco.lib.expression.FunctionException;
import de.esoco.lib.expression.Predicate;

/********************************************************************
 * A {@link Predicate} extension that allows implementations to throw checked
 * exceptions. If an exception occurs it will be converted into a runtime
 * exception of the type {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingPredicate<T> extends Predicate<T>
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that allows to declare a throwing predicate from a lambda
	 * expression that is mapped to a regular predicate. Otherwise an anonymous
	 * inner class expression would be needed because of the similar signatures
	 * of throwing and non-throwing predicate.
	 *
	 * @param  fThrowing The throwing predicate expression
	 *
	 * @return The resulting predicate
	 */
	public static <T> Predicate<T> of(ThrowingPredicate<T> fThrowing)
	{
		return fThrowing;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #tryTest(Object)} and to convert occurring
	 * exceptions into {@link FunctionException}.
	 *
	 * @see Predicate#evaluate(Object)
	 */
	@Override
	default public Boolean evaluate(T rValue)
	{
		try
		{
			return tryTest(rValue);
		}
		catch (Throwable e)
		{
			throw (e instanceof RuntimeException)
				  ? (RuntimeException) e : new FunctionException(this, e);
		}
	}

	/***************************************
	 * Replaces {@link #evaluate(Object)} and allows implementations to throw an
	 * exception.
	 *
	 * @param  rValue rInput The value to check
	 *
	 * @return The function result
	 *
	 * @throws Throwable On errors
	 */
	public Boolean tryTest(T rValue) throws Throwable;
}
