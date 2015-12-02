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

import de.esoco.lib.expression.ElementAccessFunction;
import de.esoco.lib.expression.Predicate;


/********************************************************************
 * A function predicate subclass that can only be created with element access
 * functions that are implementations of {@link ElementAccessFunction}. It also
 * has a method to query the element descriptor of such functions. The generic
 * parameters designate the types of the target objects and the return value
 * that is evaluated by the predicate, respectively.
 *
 * @author eso
 */
public class ElementPredicate<T, V> extends FunctionPredicate<T, V>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that evaluates the value of a certain element of
	 * target objects with the given predicate.
	 *
	 * @param  fElementAccess The element access function
	 * @param  pValue         The predicate to evaluate the element value with
	 *
	 * @throws IllegalArgumentException If either argument is NULL
	 */
	public ElementPredicate(
		ElementAccessFunction<?, ? super T, V> fElementAccess,
		Predicate<? super V>				   pValue)
	{
		super(fElementAccess, pValue);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the element descriptor of the element access function used by
	 * this instance.
	 *
	 * @return The element descriptor of the element access function
	 */
	public final Object getElementDescriptor()
	{
		return ((ElementAccessFunction<?, ?, ?>) getFunction())
			   .getElementDescriptor();
	}
}
