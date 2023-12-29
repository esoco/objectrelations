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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Functions;

import java.util.Objects;

/**
 * An abstract base implementation of the {@link BinaryFunction} interface. It
 * accepts the initial right value for unary function evaluations as a
 * constructor argument.
 *
 * @author eso
 */
public abstract class AbstractBinaryFunction<L, R, O>
	extends AbstractFunction<L, O> implements BinaryFunction<L, R, O> {

	private final R rightValue;

	/**
	 * Creates a new instance with a particular right value and the class name
	 * as the token.
	 *
	 * @param rightValue The right value for unary evaluation
	 */
	public AbstractBinaryFunction(R rightValue) {
		this(rightValue, null);
	}

	/**
	 * Creates a new instance with a particular right value and function token.
	 *
	 * @param rightValue The right value for unary evaluation
	 * @param token      The function token
	 */
	public AbstractBinaryFunction(R rightValue, String token) {
		super(token);

		this.rightValue = rightValue;
	}

	/**
	 * Implemented as final to delegate the evaluation of this function to the
	 * binary method {@link BinaryFunction#evaluate(Object, Object)}.
	 *
	 * @param leftValue The left input value
	 * @return The result of evaluating the left and right values
	 */
	@Override
	public final O evaluate(L leftValue) {
		return evaluate(leftValue, rightValue);
	}

	/**
	 * @see BinaryFunction#getRightValue()
	 */
	@Override
	public final R getRightValue() {
		return rightValue;
	}

	/**
	 * Invokes {@link Functions#chainLeft(BinaryFunction, BinaryFunction)} with
	 * the other function first and then this.
	 *
	 * @see Functions#chainLeft(BinaryFunction, BinaryFunction)
	 */
	public <T> BinaryFunction<L, R, T> thenLeft(
		BinaryFunction<? super O, R, T> other) {
		return Functions.chainLeft(other, this);
	}

	/**
	 * Invokes {@link Functions#chainRight(BinaryFunction, BinaryFunction)}
	 * with
	 * the other function first and then this.
	 *
	 * @see Functions#chainRight(BinaryFunction, BinaryFunction)
	 */
	public <T> BinaryFunction<L, R, T> thenRight(
		BinaryFunction<L, ? super O, T> other) {
		return Functions.chainRight(other, this);
	}

	/**
	 * Overridden to format the raw function description as returned by the
	 * superclass method with the current right value by means of the method
	 * {@link String#format(String, Object...)}.
	 *
	 * @return A text describing this function instance
	 */
	@Override
	public String toString() {
		return getToken() + "(" + INPUT_PLACEHOLDER + ", " + rightValue + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		AbstractBinaryFunction<?, ?, ?> otherFunction =
			(AbstractBinaryFunction<?, ?, ?>) other;

		return Objects.equals(rightValue, otherFunction.rightValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int paramsHashCode() {
		return rightValue != null ? rightValue.hashCode() : 0;
	}
}
