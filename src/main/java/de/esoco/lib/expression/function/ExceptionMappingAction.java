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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.FunctionException;


/********************************************************************
 * A base class for {@link Action} implementations that allows subclasses to
 * throw exceptions from their evaluation. Any such exception will be mapped to
 * a runtime {@link FunctionException} and then re-thrown from the {@link
 * #execute(Object) execute} method. This method is overridden to be final,
 * subclasses must implement the abstract method {@link
 * #executeWithException(Object)} instead.
 *
 * @author eso
 */
public abstract class ExceptionMappingAction<I> extends AbstractAction<I>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#AbstractFunction(String)
	 */
	public ExceptionMappingAction(String sToken)
	{
		super(sToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to invoke {@link #executeWithException(Object)} and to convert
	 * any occurring exception into a runtime exception.
	 *
	 * @see AbstractAction#execute(Object)
	 */
	@Override
	public final void execute(I rValue)
	{
		try
		{
			executeWithException(rValue);
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
	 * error. The invoking {@link #execute(Object) execute} method will convert
	 * such an exception into a runtime {@link FunctionException}.
	 *
	 * @param  rValue The input value for the execution
	 *
	 * @throws Exception If an error occurs
	 */
	protected abstract void executeWithException(I rValue) throws Exception;
}
