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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.FunctionChain;

import java.util.function.Consumer;

/**
 * A function sub-interface for the implementation of actions that have no
 * result. Implementations must implement the {@link #execute(Object)} instead
 * of {@link #evaluate(Object)}.
 *
 * @author eso
 */
@FunctionalInterface
public interface Action<T> extends Function<T, Void>, Consumer<T> {

	/**
	 * Takes an action that throws an exception and returns it as an action
	 * that
	 * can be executed without a checked exception. This method is mainly
	 * intended to be used with lambdas that throw exceptions.
	 *
	 * @param checked The checked action to wrap as unchecked
	 * @return The unchecked action
	 */
	static <T, E extends Exception> Action<T> unchecked(
		ThrowingAction<T, E> checked) {
		return checked;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void accept(T value) {
		evaluate(value);
	}

	/**
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	default Void evaluate(T value) {
		execute(value);

		return null;
	}

	/**
	 * this method must be implemented with the action functionality.
	 *
	 * @param value The value to execute the action upon
	 */
	void execute(T value);

	/**
	 * Returns a new function object that evaluates the result received from
	 * another function with this function. Implementations should typically
	 * subclass {@link AbstractFunction} which already contains an
	 * implementation of this method.
	 *
	 * @param previous The function to produce this function's input values
	 *                 with
	 * @return A new instance of {@link FunctionChain}
	 */
	@Override
	default <O> Action<O> from(Function<O, ? extends T> previous) {
		return Functions.asAction(Functions.chain(this, previous));
	}

	/**
	 * A sub-interface that allows implementations to throw checked exceptions.
	 * If an exception occurs it will be converted into a runtime exception of
	 * the type {@link FunctionException}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	interface ThrowingAction<T, E extends Exception> extends Action<T> {

		/**
		 * Replaces {@link #evaluate(Object)} and allows implementations to
		 * throw an exception.
		 *
		 * @param value The input value
		 * @throws E An exception in the case of errors
		 */
		void evaluateWithException(T value) throws E;

		/**
		 * Overridden to forward the invocation to the actual function
		 * implementation in {@link #evaluateWithException(Object)} and to
		 * convert occurring exceptions into {@link FunctionException}.
		 *
		 * @see Action#execute(Object)
		 */
		@Override
		default void execute(T value) {
			try {
				evaluateWithException(value);
			} catch (Exception e) {
				throw (e instanceof RuntimeException) ?
				      (RuntimeException) e :
				      new FunctionException(this, e);
			}
		}
	}
}
