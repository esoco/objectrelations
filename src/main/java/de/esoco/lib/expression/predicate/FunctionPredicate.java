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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.function.AbstractFunction;


/********************************************************************
 * A predicate that evaluates the result of applying a function to input objects
 * with another predicate. The generic parameters designate the types of the
 * target objects and the return value that is evaluated by the predicate,
 * respectively.
 *
 * @author eso
 */
public class FunctionPredicate<T, V> extends AbstractPredicate<T>
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
		// use empty string because toString is overridden
		super("");

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

	/***************************************
	 * Compares the predicate and the function of this instance for equality.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		FunctionPredicate<?, ?> rOtherPredicate =
			(FunctionPredicate<?, ?>) rOther;

		return rPredicate.equals(rOtherPredicate.rPredicate) &&
			   rFunction.equals(rOtherPredicate.rFunction);
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return 31 * rPredicate.hashCode() + rFunction.hashCode();
	}
}
