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


/********************************************************************
 * A {@link Runnable} extension that maps any occurring exception to a runtime
 * {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingRunnable extends Runnable
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that allows to declare a throwing runnable from a lambda
	 * expression that is mapped to a regular runnable. Otherwise an anonymous
	 * inner class expression would be needed because of the similar signatures
	 * of the throwing and non-throwing runnable interfaces
	 *
	 * @param  fThrowing The throwing runnable expression
	 *
	 * @return The resulting function
	 */
	public static Runnable of(ThrowingRunnable fThrowing)
	{
		return fThrowing;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to forward the invocation to the actual function
	 * implementation in {@link #tryRun()} and to convert occurring exceptions
	 * into {@link FunctionException}.
	 *
	 * @see Runnable#run()
	 */
	@Override
	default public void run()
	{
		try
		{
			tryRun();
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
	 * An alternative to {@link #run()} that is allowed to throw any kind of
	 * exception.
	 *
	 * @throws Throwable If the invocation fails
	 */
	public void tryRun() throws Throwable;
}
