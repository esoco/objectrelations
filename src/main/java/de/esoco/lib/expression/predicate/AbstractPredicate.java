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
package de.esoco.lib.expression.predicate;

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractFunction;


/********************************************************************
 * An abstract base class for predicates. It implements the methods {@link
 * #and(Predicate)} and {@link #or(Predicate)}.
 *
 * @author eso
 */
public abstract class AbstractPredicate<T> extends AbstractFunction<T, Boolean>
	implements Predicate<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#AbstractFunction()
	 */
	public AbstractPredicate()
	{
	}

	/***************************************
	 * @see AbstractFunction#AbstractFunction(String)
	 */
	public AbstractPredicate(String sToken)
	{
		super(sToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to implement a predicate-specific formatting.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString()
	{
		return INPUT_PLACEHOLDER + " " + getToken();
	}
}
