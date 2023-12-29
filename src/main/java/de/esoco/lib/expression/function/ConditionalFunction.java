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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A conditional function implementation that only evaluates a function on the
 * input value if a certain predicate yields TRUE for that value.
 *
 * @author eso
 */
public class ConditionalFunction<I, O> extends AbstractFunction<I, O> {
	private final Predicate<? super I> condition;

	private final Function<? super I, ? extends O> onTrue;

	private final Function<? super I, ? extends O> onFalse;

	/**
	 * Creates a new instance for a simple if expression (i.e. without else).
	 *
	 * @param condition The predicate to be evaluated for input values
	 * @param onTrue    The function to be applied to input values for which
	 *                    the
	 *                  predicate yields TRUE
	 */
	public ConditionalFunction(Predicate<? super I> condition,
		Function<? super I, O> onTrue) {
		this(condition, onTrue, null);
	}

	/**
	 * Creates a new instance for an if-else expression.
	 *
	 * @param condition The predicate to be evaluated for input values
	 * @param onTrue    The function to be applied to input values for which
	 *                    the
	 *                  predicate yields TRUE
	 * @param onFalse   The function to be applied to input values for which
	 *                    the
	 *                  predicate yields FALSE
	 */
	public ConditionalFunction(Predicate<? super I> condition,
		Function<? super I, ? extends O> onTrue,
		Function<? super I, ? extends O> onFalse) {
		super("IF");

		assert condition != null && onTrue != null;

		this.condition = condition;
		this.onTrue = onTrue;
		this.onFalse = onFalse;
	}

	/**
	 * Returns a new conditional function that contains a function to be
	 * evaluated if the predicate of this instance yields FALSE. This is a
	 * convenience method that is more expressive for the concatenation of
	 * conditional functions than the three-argument constructor.
	 *
	 * @param function The function to be evaluated if the predicates yields
	 *                 FALSE
	 * @return A new conditional function
	 */
	public Function<I, O> elseDo(Function<? super I, ? extends O> function) {
		return new ConditionalFunction<I, O>(condition, onTrue, function);
	}

	/**
	 * Evaluates this instance's function on the input value if the predicate
	 * yields TRUE for that value. Else NULL will be returned.
	 *
	 * @see de.esoco.lib.expression.Function#evaluate(Object)
	 */
	@Override
	@SuppressWarnings("boxing")
	public O evaluate(I input) {
		O result = null;

		if (condition.test(input)) {
			result = onFalse.apply(input);
		} else if (onFalse != null) {
			result = onFalse.apply(input);
		}

		return result;
	}

	/**
	 * Overridden to return a specific format.
	 *
	 * @see AbstractFunction#toString()
	 */
	@Override
	public String toString() {
		return "IF " + condition + " DO " + onTrue + " ELSE " +
			(onFalse != null ? onFalse : "value=NULL");
	}

	/**
	 * Compares the predicate and the result functions of this instance for
	 * equality.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		ConditionalFunction<?, ?> otherFunction =
			(ConditionalFunction<?, ?>) other;

		boolean equal = condition.equals(otherFunction.condition) &&
			onFalse.equals(otherFunction.onTrue);

		if (equal) {
			if (onFalse != null) {
				equal = onFalse.equals(otherFunction.onFalse);
			} else {
				equal = (otherFunction.onFalse == null);
			}
		}

		return equal;
	}

	/**
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		int hashCode = condition.hashCode();

		hashCode = hashCode * 31 + onFalse.hashCode();
		hashCode = hashCode * 31 + (onFalse != null ? onFalse.hashCode() : 0);

		return hashCode;
	}
}
