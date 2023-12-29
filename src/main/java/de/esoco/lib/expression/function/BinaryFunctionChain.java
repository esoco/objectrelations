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

/**
 * Implements a function chain for {@link BinaryFunction} instances that will
 * forward an identically typed left or right parameter to the chained function
 * call.
 *
 * @author eso
 */
public abstract class BinaryFunctionChain<L, R, V, O>
	extends AbstractBinaryFunction<L, R, O> {

	/**
	 * Creates a new instance.
	 */
	public BinaryFunctionChain() {
		super(null, null);
	}

	/**
	 * Returns the inner function that will be evaluated first.
	 *
	 * @return The inner function
	 */
	public abstract BinaryFunction<L, R, ? extends V> getInner();

	/**
	 * Returns the outer function that will be evaluated second with the result
	 * of the inner function.
	 *
	 * @return The outer function
	 */
	public abstract BinaryFunction<?, ?, O> getOuter();

	/**
	 * Returns a string representation of this function chain.
	 *
	 * @return A string representation of this chain
	 */
	@Override
	public String toString() {
		String result = getOuter().toString();

		if (result.indexOf(INPUT_PLACEHOLDER) >= 0) {
			result = result.replace(INPUT_PLACEHOLDER, getInner().toString());
		} else {
			result += "(" + getInner().toString() + ")";
		}

		return result;
	}

	/**
	 * Compares the left and right functions with that of another function.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		BinaryFunctionChain<?, ?, ?, ?> otherFunction =
			(BinaryFunctionChain<?, ?, ?, ?>) other;

		return getOuter().equals(otherFunction.getOuter()) &&
			getInner().equals(otherFunction.getInner());
	}

	/**
	 * Calculates the combined hash code of the left and right functions.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		return 31 * getOuter().hashCode() + getInner().hashCode();
	}

	/**
	 * Implementation of {@link BinaryFunctionChain} that chains the input on
	 * the left function value.
	 *
	 * @author eso
	 */
	public static class LeftFunctionChain<L, R, V, O>
		extends BinaryFunctionChain<L, R, V, O> {

		private final BinaryFunction<V, R, O> outer;

		private final BinaryFunction<L, R, ? extends V> inner;

		/**
		 * Creates a new instance that chains two functions together.
		 *
		 * @param outer The binary outer function
		 * @param inner The binary inner function
		 */
		public LeftFunctionChain(BinaryFunction<V, R, O> outer,
			BinaryFunction<L, R, ? extends V> inner) {
			this.outer = outer;
			this.inner = inner;
		}

		/**
		 * @see BinaryFunctionChain#evaluate(Object, Object)
		 */
		@Override
		public O evaluate(L leftValue, R rightValue) {
			return outer.evaluate(inner.evaluate(leftValue, rightValue),
				rightValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BinaryFunction<L, R, ? extends V> getInner() {
			return inner;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BinaryFunction<V, R, O> getOuter() {
			return outer;
		}
	}

	/**
	 * Implementation of {@link BinaryFunctionChain} that chains the input on
	 * the right function value.
	 *
	 * @author eso
	 */
	public static class RightFunctionChain<L, R, V, O>
		extends BinaryFunctionChain<L, R, V, O> {

		private final BinaryFunction<L, V, O> outer;

		private final BinaryFunction<L, R, ? extends V> inner;

		/**
		 * Creates a new instance that chains two functions together.
		 *
		 * @param outer The binary outer function
		 * @param inner The binary inner function
		 */
		public RightFunctionChain(BinaryFunction<L, V, O> outer,
			BinaryFunction<L, R, ? extends V> inner) {
			this.outer = outer;
			this.inner = inner;
		}

		/**
		 * @see BinaryFunctionChain#evaluate(Object, Object)
		 */
		@Override
		public O evaluate(L leftValue, R rightValue) {
			return outer.evaluate(leftValue,
				inner.evaluate(leftValue, rightValue));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BinaryFunction<L, R, ? extends V> getInner() {
			return inner;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BinaryFunction<L, V, O> getOuter() {
			return outer;
		}
	}
}
