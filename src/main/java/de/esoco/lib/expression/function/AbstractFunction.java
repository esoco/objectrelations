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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;

import org.obrel.core.RelatedObject;


/********************************************************************
 * A base class for implementations of the Function interface. It contains an
 * implementation of the method {@link #from(Function)} that simply invokes
 * {@link Functions#chain(Function, Function)}.
 *
 * @author eso
 */
public abstract class AbstractFunction<I, O> extends RelatedObject
	implements Function<I, O>
{
	//~ Instance fields --------------------------------------------------------

	private final String sToken;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sToken A text describing this function class
	 */
	public AbstractFunction(String sToken)
	{
		// do not store in relation to prevent initialization cycle
		// with StandardProperties
		this.sToken = sToken;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Compares this function instance for equality with another object. Two
	 * functions are considered equal if they have exactly the same class, if
	 * their parameters are equal (this will be checked by invoking the method
	 * {@link #paramsEqual(AbstractFunction)}), and if they have equal relations
	 * (checked with {@link #relationsEqual(RelatedObject)}).
	 *
	 * <p>The description string of a function is not taken into account by this
	 * method and it is strongly advised that the description text is always the
	 * same for a certain function class. If subclasses need to generate
	 * instance-specific descriptions (e.g. based on function parameters) the
	 * description string should contain a template that is identical for all
	 * instances. This template can then be formatted accordingly in the
	 * toString() method, for example.</p>
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (rObject == null || getClass() != rObject.getClass())
		{
			return false;
		}

		AbstractFunction<?, ?> rOther = (AbstractFunction<?, ?>) rObject;

		return getToken().equals(rOther.getToken()) && paramsEqual(rOther) &&
			   relationsEqual(rOther);
	}

	/***************************************
	 * @see Function#from(Function)
	 */
	@Override
	public <T> Function<T, O> from(Function<T, ? extends I> rOther)
	{
		return rOther.then(this);
	}

	/***************************************
	 * Returns the token that describes this function.
	 *
	 * @return The function token
	 */
	@Override
	public String getToken()
	{
		return sToken;
	}

	/***************************************
	 * Calculates a hash code for this function instance. The hash code of a
	 * function is based on the function class and the the result of the methods
	 * {@link #paramsHashCode()} (which should be overridden by subclasses that
	 * define function parameters) and {@link #relationsHashCode()} which takes
	 * into account the relations that are set on a function accounts.
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int nHashCode = getToken().hashCode();

		nHashCode = 31 * nHashCode + paramsHashCode();
		nHashCode = 31 * nHashCode + relationsHashCode();

		return nHashCode;
	}

	/***************************************
	 * type as the element access function and the given value predicate. This
	 * serves as a shortcut to creating predicates for relation value based on
	 *
	 * @param  pCriteria The value comparison predicate
	 *
	 * @return The resulting element predicate
	 */
	@Override
	public <T extends I> Predicate<T> is(Predicate<? super O> pCriteria)
	{
		return Predicates.when(this, pCriteria);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public O result()
	{
		return evaluate(null);
	}

	/***************************************
	 * @see Function#then(Function)
	 */
	@Override
	public <T> Function<I, T> then(Function<? super O, T> rOther)
	{
		return Functions.chain(rOther, this);
	}

	/***************************************
	 * Returns the description of this function.
	 *
	 * @return A text describing this function
	 */
	@Override
	public String toString()
	{
		return getToken() + "(" + INPUT_PLACEHOLDER + ")";
	}

	/***************************************
	 * This method must be overridden by subclasses that define additional
	 * function parameters to compare these parameters for equality with another
	 * function instance. It will be invoked by this classes' implementation of
	 * {@link #equals(Object)} after the standard equality checks have been
	 * performed. A subclass can safely assume that the other function argument
	 * is not NULL and of exactly the same class as itself. The default
	 * implementation always returns TRUE.
	 *
	 * @param  rOther The other function to compare the parameters with
	 *
	 * @return TRUE if all parameters are equal to that of the other function
	 */
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		return true;
	}

	/***************************************
	 * This method must be overridden by subclasses that define additional
	 * function parameters to calculate a hash code for these parameters. It
	 * will be invoked by this classes' implementation of {@link #hashCode()} to
	 * calculate the function hash code. The default implementation always
	 * returns 0.
	 *
	 * @return The hash code for the parameter's of this instance
	 */
	protected int paramsHashCode()
	{
		return 0;
	}
}
