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

/**
 * Base class for joins of two predicates (like logical combinations).
 * Subclasses must implement the {@link Function#evaluate(Object)} method to
 * implement the actual join of the two predicates in the correct order. The
 * predicates can be queried with the methods {@link #getLeft()} and
 * {@link #getRight()}.
 *
 * @author eso
 */
public abstract class PredicateJoin<T> implements Predicate<T> {

	private final Predicate<? super T> left;

	private final Predicate<? super T> right;

	private final String joinToken;

	/**
	 * Creates a new instance.
	 *
	 * @param left      The left predicate that will be evaluated first
	 * @param right     The right predicate
	 * @param joinToken A string token that describes the join
	 */
	public PredicateJoin(Predicate<? super T> left, Predicate<? super T> right,
		String joinToken) {
		if (left == null || right == null) {
			throw new IllegalArgumentException("Predicates must not be NULL");
		}

		this.left = left;
		this.right = right;
		this.joinToken = joinToken;
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

		PredicateJoin<?> other = (PredicateJoin<?>) obj;

		return left.equals(other.left) && right.equals(other.right);
	}

	/**
	 * Implemented as final, subclasses must implement the abstract method
	 * {@link #evaluate(Predicate, Predicate, Object)} instead.
	 *
	 * @param value The value to be evaluated by the join predicates
	 * @return The result of the evaluation
	 */
	@Override
	public final Boolean evaluate(T value) {
		return evaluate(left, right, value);
	}

	/**
	 * Returns the left predicate of this join.
	 *
	 * @return The left predicate
	 */
	public final Predicate<? super T> getLeft() {
		return left;
	}

	/**
	 * Returns the right predicate of this instance.
	 *
	 * @return The right predicate
	 */
	public final Predicate<? super T> getRight() {
		return right;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 * (left.hashCode() + 31 * right.hashCode());
	}

	/**
	 * Returns a string representation of this join.
	 *
	 * @return A string representation of this join
	 */
	@Override
	public String toString() {
		return "(" + left + " " + joinToken + " " + right + ")";
	}

	/**
	 * Must be implemented by subclasses for the actual evaluation of this
	 * join.
	 *
	 * @param left  The left predicate
	 * @param right The right predicate
	 * @param value The value to be evaluated by the predicates
	 * @return The result of the combined evaluation
	 */
	protected abstract Boolean evaluate(Predicate<? super T> left,
		Predicate<? super T> right, T value);
}
