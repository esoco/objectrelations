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

/**
 * A predicate that evaluates the result of applying a function to input objects
 * with another predicate. The generic parameters designate the types of the
 * target objects and the return value that is evaluated by the predicate,
 * respectively.
 *
 * @author eso
 */
public class FunctionPredicate<T, V> extends RelatedObject
	implements Predicate<T> {

	private final Function<? super T, V> function;

	private final Predicate<? super V> predicate;

	/**
	 * Creates a new instance.
	 *
	 * @param function  The function to apply to input values
	 * @param predicate The predicate to evaluate the result of the function
	 *                  with
	 * @throws IllegalArgumentException If either argument is NULL
	 */
	public FunctionPredicate(Function<? super T, V> function,
		Predicate<? super V> predicate) {
		if (function == null) {
			throw new IllegalArgumentException("Function must not be NULL");
		}

		if (predicate == null) {
			throw new IllegalArgumentException("Predicate must not be NULL");
		}

		this.function = function;
		this.predicate = predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		FunctionPredicate<?, ?> other = (FunctionPredicate<?, ?>) obj;

		return predicate.equals(other.predicate) &&
			function.equals(other.function);
	}

	/**
	 * Retrieves the field value from the target object and returns the result
	 * of the field predicate's evaluate method after invoking it on the field
	 * value.
	 *
	 * @param object The target object to retrieve the field value from1
	 * @return The result of the field predicate evaluation
	 */
	@Override
	public Boolean evaluate(T object) {
		return predicate.evaluate(function.evaluate(object));
	}

	/**
	 * Returns the function that is evaluated by this instance.
	 *
	 * @return The function of this instance
	 */
	public final Function<? super T, V> getFunction() {
		return function;
	}

	/**
	 * Returns the predicate that is used by to evaluate the result of the
	 * function.
	 *
	 * @return The value predicate
	 */
	public final Predicate<? super V> getPredicate() {
		return predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 * (predicate.hashCode() + 31 * function.hashCode());
	}

	/**
	 * Creates a combined string representation from the function and predicate
	 * of this instance.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString() {
		return predicate
			.toString()
			.replace(INPUT_PLACEHOLDER, function.toString());
	}
}
