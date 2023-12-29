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

import de.esoco.lib.expression.BinaryPredicate;
import de.esoco.lib.expression.FunctionException;

/**
 * A sub-interface that allows implementations to throw checked exceptions. If
 * an exception occurs it will be converted into a runtime exception of the type
 * {@link FunctionException}.
 *
 * @author eso
 */
@FunctionalInterface
public interface ThrowingBinaryPredicate<L, R> extends BinaryPredicate<L, R> {

	/**
	 * Factory method that allows to declare a throwing predicate from a lambda
	 * expression that is mapped to a regular predicate. Otherwise an anonymous
	 * inner class expression would be needed because of the similar signatures
	 * of throwing and non-throwing predicate.
	 *
	 * @param throwing The throwing predicate expression
	 * @return The resulting predicate
	 */
	static <L, R> BinaryPredicate<L, R> of(
		ThrowingBinaryPredicate<L, R> throwing) {
		return throwing;
	}

	/**
	 * Overridden to forward the invocation to the actual implementation in
	 * {@link #tryTest(Object, Object)} and to convert occurring exceptions
	 * into
	 * {@link FunctionException}.
	 *
	 * @see BinaryPredicate#evaluate(Object, Object)
	 */
	@Override
	default Boolean evaluate(L left, R right) {
		try {
			return tryTest(left, right);
		} catch (Throwable e) {
			throw (e instanceof RuntimeException) ?
			      (RuntimeException) e :
			      new FunctionException(this, e);
		}
	}

	/**
	 * Replaces {@link #evaluate(Object)} and allows implementations to
	 * throw an
	 * exception.
	 *
	 * @param left  The first argument
	 * @param right The second argument
	 * @return The function result
	 * @throws Throwable On errors
	 */
	Boolean tryTest(L left, R right) throws Throwable;
}
