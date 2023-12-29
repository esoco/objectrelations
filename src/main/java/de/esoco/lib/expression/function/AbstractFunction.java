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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Functions;

import java.io.Serializable;
import java.util.Objects;

/**
 * A base class for implementations of the Function interface. It contains an
 * implementation of the method {@link #from(Function)} that simply invokes
 * {@link Functions#chain(Function, Function)}.
 *
 * @author eso
 */
public abstract class AbstractFunction<I, O>
	implements Function<I, O>, Serializable {

	private String token;

	/**
	 * Creates a new instance.
	 *
	 * @param token A text describing this function class
	 */
	public AbstractFunction(String token) {
		// do not store in relation to prevent initialization cycle
		// with StandardProperties
		this.token = token;
	}

	/**
	 * Compares this function instance for equality with another object. Two
	 * functions are considered equal if they have exactly the same class, if
	 * their parameters are equal (this will be checked by invoking the method
	 * {@link #paramsEqual(AbstractFunction)}).
	 *
	 * <p>The description string of a function is not taken into account by
	 * this method and it is strongly advised that the description text is
	 * always the same for a certain function class. If subclasses need to
	 * generate instance-specific descriptions (e.g. based on function
	 * parameters) the description string should contain a template that is
	 * identical for all instances. This template can then be formatted
	 * accordingly in the toString() method, for example.</p>
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		AbstractFunction<?, ?> other = (AbstractFunction<?, ?>) object;

		return Objects.equals(getToken(), getToken()) && paramsEqual(other);
	}

	/**
	 * Returns the token that describes this function.
	 *
	 * @return The function token
	 */
	@Override
	public String getToken() {
		if (token == null) {
			token = getClass().getSimpleName();
		}

		return token;
	}

	/**
	 * Calculates a hash code for this function instance. The hash code of a
	 * function is based on the function class and the the result of the
	 * methods
	 * {@link #paramsHashCode()} (which should be overridden by subclasses that
	 * define function parameters).
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * (getToken().hashCode() + 31 + paramsHashCode());
	}

	/**
	 * Returns the description of this function.
	 *
	 * @return A text describing this function
	 */
	@Override
	public String toString() {
		return getToken() + "(" + INPUT_PLACEHOLDER + ")";
	}

	/**
	 * This method must be overridden by subclasses that define additional
	 * function parameters to compare these parameters for equality with
	 * another
	 * function instance. It will be invoked by this classes' implementation of
	 * {@link #equals(Object)} after the standard equality checks have been
	 * performed. A subclass can safely assume that the other function argument
	 * is not NULL and of exactly the same class as itself. The default
	 * implementation always returns TRUE.
	 *
	 * @param other The other function to compare the parameters with
	 * @return TRUE if all parameters are equal to that of the other function
	 */
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		return true;
	}

	/**
	 * This method must be overridden by subclasses that define additional
	 * function parameters to calculate a hash code for these parameters. It
	 * will be invoked by this classes' implementation of
	 * {@link #hashCode()} to
	 * calculate the function hash code. The default implementation always
	 * returns 0.
	 *
	 * @return The hash code for the parameter's of this instance
	 */
	protected int paramsHashCode() {
		return 0;
	}
}
