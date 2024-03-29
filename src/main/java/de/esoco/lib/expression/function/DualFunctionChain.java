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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Function;

/**
 * Implements a function chain that invokes a {@link BinaryFunction} with the
 * results of evaluating the left and right values with separate functions.
 *
 * @author eso
 */
public class DualFunctionChain<L, R, V, W, O>
	extends AbstractBinaryFunction<L, R, O> {

	private static final String RIGHT_FUNCTION_PLACEHOLDER = "$$";

	private final BinaryFunction<V, W, O> outer;

	private final Function<L, ? extends V> left;

	private final Function<R, ? extends W> right;

	/**
	 * Creates a new instance that chains two functions together.
	 *
	 * @param outer The binary outer function
	 * @param left  The function to evaluate left input values with
	 * @param right The function to evaluate right input values with
	 */
	public DualFunctionChain(final BinaryFunction<V, W, O> outer,
		final Function<L, ? extends V> left,
		final Function<R, ? extends W> right) {
		super(null, RIGHT_FUNCTION_PLACEHOLDER);

		this.outer = outer;
		this.left = left;
		this.right = right;
	}

	/**
	 * @see BinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public O evaluate(L leftValue, R rightValue) {
		return outer.evaluate(left.evaluate(leftValue),
			right.evaluate(rightValue));
	}

	/**
	 * Returns the left function of this chain.
	 *
	 * @return The left function
	 */
	public final Function<V, O> getLeft() {
		return outer;
	}

	/**
	 * Returns the outer function of this chain.
	 *
	 * @return The outer function
	 */
	public BinaryFunction<V, W, O> getOuter() {
		return outer;
	}

	/**
	 * Returns the right function of this chain.
	 *
	 * @return The right function
	 */
	public final Function<L, ? extends V> getRight() {
		return left;
	}

	/**
	 * Returns a string representation of this function chain.
	 *
	 * @return A string representation of this chain
	 */
	@Override
	public String toString() {
		return outer
			.toString()
			.replace(INPUT_PLACEHOLDER, left.toString())
			.replace(RIGHT_FUNCTION_PLACEHOLDER, right.toString());
	}

	/**
	 * Compares the chained functions with that of another function.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		DualFunctionChain<?, ?, ?, ?, ?> otherFunction =
			(DualFunctionChain<?, ?, ?, ?, ?>) other;

		return outer.equals(otherFunction.left) &&
			left.equals(otherFunction.right);
	}

	/**
	 * Calculates the combined hash code of the chained functions.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		return 31 * (31 * outer.hashCode() + left.hashCode()) +
			right.hashCode();
	}
}
