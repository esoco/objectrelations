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
package de.esoco.lib.expression.predicate;

import de.esoco.lib.expression.FunctionException;
import de.esoco.lib.expression.function.AbstractFunction;


/********************************************************************
 * A predicate base class that allows subclasses to throw arbitrary exceptions
 * from their evaluation. Any such exception will be mapped to a runtime
 * exception and then re-thrown from the {@link #evaluate(Object)} method. This
 * method is overridden to be final, subclasses must implement the abstract
 * method {@link #evaluateWithException(Object)} instead.
 *
 * @author eso
 */
public abstract class ExceptionMappingPredicate<I> extends AbstractPredicate<I>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractPredicate#AbstractPredicate(String)
	 */
	public ExceptionMappingPredicate(String sToken)
	{
		super(sToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to invoke {@link #evaluateWithException(Object)} and to
	 * convert any occurring exception into a runtime exception.
	 *
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public final Boolean evaluate(I rValue)
	{
		try
		{
			return evaluateWithException(rValue);
		}
		catch (Exception e)
		{
			throw (e instanceof RuntimeException)
				  ? (RuntimeException) e : new FunctionException(this, e);
		}
	}

	/***************************************
	 * This method must be implemented by subclasses to perform the function
	 * evaluation. The method may throw any type of exception to signal an
	 * error. The invoking {@link #evaluate(Object)} method will convert such
	 * exceptions into runtime exceptions.
	 *
	 * @param  rValue The input value for the evaluation
	 *
	 * @return The result of the evaluation
	 *
	 * @throws Exception If an error occurs
	 */
	protected abstract Boolean evaluateWithException(I rValue) throws Exception;
}
