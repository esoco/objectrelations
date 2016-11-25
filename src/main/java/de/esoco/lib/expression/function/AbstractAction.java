//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.expression.Function;


/********************************************************************
 * A convenience base class to implement actions. Actions are functions that
 * don't yield a result and therefore return no value from their evaluation.
 * Instead of {@link Function#evaluate(Object)} subclasses must implement the
 * method {@link #execute(Object)} which has no return value.
 *
 * @author eso
 */
public abstract class AbstractAction<T> extends AbstractFunction<T, Void>
	implements Action<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#AbstractFunction(String)
	 */
	public AbstractAction(String sToken)
	{
		super(sToken);
	}
}
