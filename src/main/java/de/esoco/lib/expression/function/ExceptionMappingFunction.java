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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.Function.ThrowingFunction;


/********************************************************************
 * A function base class that allows subclasses to throw arbitrary exceptions
 * from their evaluation. Any such exception will be mapped to a runtime
 * exception and then re-thrown from the {@link #evaluate(Object)} method. This
 * method is overridden to be final, subclasses must implement the abstract
 * method {@link #evaluateWithException(Object)} instead.
 *
 * @author eso
 */
public abstract class ExceptionMappingFunction<I, O>
	extends AbstractFunction<I, O> implements ThrowingFunction<I, O, Exception>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#AbstractFunction(String)
	 */
	public ExceptionMappingFunction(String sToken)
	{
		super(sToken);
	}
}
