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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.FunctionException;


/********************************************************************
 * A binary function base class that allows subclasses to throw arbitrary
 * exceptions from their evaluation. Any such exception will be mapped to a
 * runtime exception and then re-thrown from {@link #evaluate(Object, Object)}.
 * This method is overridden to be final, subclasses must implement the abstract
 * method {@link #evaluateWithException(Object, Object)} instead.
 *
 * @author eso
 */
public abstract class ExceptionMappingBinaryFunction<L, R, O>
	extends AbstractBinaryFunction<L, R, O>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractBinaryFunction#AbstractBinaryFunction(Object, String)
	 */
	public ExceptionMappingBinaryFunction(R rRightValue, String sToken)
	{
		super(rRightValue, sToken);
	}

	/***************************************
	 * @see AbstractBinaryFunction#AbstractBinaryFunction(Object, String,
	 *      boolean)
	 */
	public ExceptionMappingBinaryFunction(R		  rRightValue,
										  String  sToken,
										  boolean bImmutable)
	{
		super(rRightValue, sToken, bImmutable);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to invoke {@link #evaluateWithException(Object, Object)} and
	 * to convert any occurring exception into a runtime exception.
	 *
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public final O evaluate(L rLeftValue, R rRightValue)
	{
		try
		{
			return evaluateWithException(rLeftValue, rRightValue);
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
	 * error. The invoking {@link #evaluate(Object, Object)} method will convert
	 * such exceptions into runtime exceptions.
	 *
	 * @param  rLeftValue  The left input value for the evaluation
	 * @param  rRightValue The right input value for the evaluation
	 *
	 * @return The result of the evaluation
	 *
	 * @throws Exception If an error occurs
	 */
	protected abstract O evaluateWithException(L rLeftValue, R rRightValue)
		throws Exception;
}
