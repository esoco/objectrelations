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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractFunction;

import org.obrel.core.RelatedObject;


/********************************************************************
 * A predicate that evaluates the result of applying a function to input objects
 * with another predicate. The generic parameters designate the types of the
 * target objects and the return value that is evaluated by the predicate,
 * respectively.
 *
 * @author eso
 */
public class FunctionPredicate<T, V> extends RelatedObject
	implements Predicate<T>
{
	//~ Instance fields --------------------------------------------------------

	private final Function<? super T, V> rFunction;
	private final Predicate<? super V>   rPredicate;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param  rFunction  The function to apply to input values
	 * @param  rPredicate The predicate to evaluate the result of the function
	 *                    with
	 *
	 * @throws IllegalArgumentException If either argument is NULL
	 */
	public FunctionPredicate(
		Function<? super T, V> rFunction,
		Predicate<? super V>   rPredicate)
	{
		if (rFunction == null)
		{
			throw new IllegalArgumentException("Function must not be NULL");
		}

		if (rPredicate == null)
		{
			throw new IllegalArgumentException("Predicate must not be NULL");
		}

		this.rFunction  = rFunction;
		this.rPredicate = rPredicate;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObj)
	{
		if (rObj == this)
		{
			return true;
		}

		if (rObj == null || rObj.getClass() != getClass())
		{
			return false;
		}

		FunctionPredicate<?, ?> rOther = (FunctionPredicate<?, ?>) rObj;

		return rPredicate.equals(rOther.rPredicate) &&
			   rFunction.equals(rOther.rFunction);
	}

	/***************************************
	 * Retrieves the field value from the target object and returns the result
	 * of the field predicate's evaluate method after invoking it on the field
	 * value.
	 *
	 * @param  rObject The target object to retrieve the field value from1
	 *
	 * @return The result of the field predicate evaluation
	 */
	@Override
	public Boolean evaluate(T rObject)
	{
		return rPredicate.evaluate(rFunction.evaluate(rObject));
	}

	/***************************************
	 * Returns the function that is evaluated by this instance.
	 *
	 * @return The function of this instance
	 */
	public final Function<? super T, V> getFunction()
	{
		return rFunction;
	}

	/***************************************
	 * Returns the predicate that is used by to evaluate the result of the
	 * function.
	 *
	 * @return The value predicate
	 */
	public final Predicate<? super V> getPredicate()
	{
		return rPredicate;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return 31 * (rPredicate.hashCode() + 31 * rFunction.hashCode());
	}

	/***************************************
	 * Creates a combined string representation from the function and predicate
	 * of this instance.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString()
	{
		return rPredicate.toString()
						 .replace(INPUT_PLACEHOLDER, rFunction.toString());
	}
}
