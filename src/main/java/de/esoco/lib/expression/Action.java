//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.util.function.Consumer;


/********************************************************************
 * A function sub-interface for the implementation of actions that have no
 * result. Implementations must implement the {@link #execute(Object)} instead
 * of {@link #evaluate(Object)}.
 *
 * @author eso
 */
public interface Action<T> extends Function<T, Void>, Consumer<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * this method must be implemented with the action functionality.
	 *
	 * @param rValue The value to execute the action upon
	 */
	public abstract void execute(T rValue);

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	default void accept(T rValue)
	{
		evaluate(rValue);
	}

	/***************************************
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	default Void evaluate(T rValue)
	{
		execute(rValue);

		return null;
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * A sub-interface that allows implementations to throw checked exceptions.
	 * If an exception occurs it will be converted into a runtime exception of
	 * the type {@link FunctionException}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	public static interface ThrowingAction<T, E extends Exception>
		extends Action<T>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Replaces {@link #evaluate(Object)} and allows implementations to
		 * throw an exception.
		 *
		 * @param  rValue The input value
		 *
		 * @throws E An exception in the case of errors
		 */
		public void evaluateWithException(T rValue) throws E;

		/***************************************
		 * Overridden to forward the invocation to the actual function
		 * implementation in {@link #evaluateWithException(Object)} and to
		 * convert occurring exceptions into {@link FunctionException}.
		 *
		 * @see Action#execute(Object)
		 */
		@Override
		default public void execute(T rValue)
		{
			try
			{
				evaluateWithException(rValue);
			}
			catch (Exception e)
			{
				throw (e instanceof RuntimeException)
					  ? (RuntimeException) e : new FunctionException(this, e);
			}
		}
	}
}
