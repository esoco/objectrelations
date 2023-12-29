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

import de.esoco.lib.expression.BinaryFunction;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Base class for predicates that perform a comparison of target values with a
 * certain compare value.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public abstract class Comparison<L, R> extends AbstractBinaryPredicate<L, R> {

	/**
	 * Creates a new immutable instance that compares target objects with a
	 * certain value.
	 *
	 * @param value The value to compare the target objects of evaluations
	 *              with
	 * @param token A string description of this comparison
	 */
	public Comparison(R value, String token) {
		super(value, token);
	}

	/**
	 * Checks whether an input value is an element of a certain collection.
	 */
	public static class ElementOf<T> extends Comparison<T, Collection<?>> {

		/**
		 * Creates a new instance.
		 *
		 * @param value The collection to check input values against
		 */
		public ElementOf(Collection<?> value) {
			super(value, "ElementOf");
		}

		/**
		 * Returns TRUE if the target value is an element of the collection of
		 * this instance.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T value, Collection<?> collection) {
			return collection.contains(value);
		}
	}

	/**
	 * Compares target values for equality with the compare value by means of
	 * the method {@link Object#equals(Object)}.
	 */
	public static class EqualTo<T> extends Comparison<T, Object> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public EqualTo(Object value) {
			super(value, "=");
		}

		/**
		 * Returns TRUE if the target value is equal to the compare value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, Object rightValue) {
			return Objects.equals(leftValue, rightValue);
		}
	}

	/**
	 * Checks whether a target value is greater than or equal to the compare
	 * value.
	 */
	public static class GreaterOrEqual<T extends Comparable<T>>
		extends Comparison<T, T> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public GreaterOrEqual(T value) {
			super(value, ">=");
		}

		/**
		 * Returns TRUE if the target value is greater than or equal to the
		 * compare value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, T rightValue) {
			return leftValue.compareTo(rightValue) >= 0;
		}
	}

	/**
	 * Checks whether a target value is greater than the compare value.
	 */
	public static class GreaterThan<T extends Comparable<T>>
		extends Comparison<T, T> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public GreaterThan(T value) {
			super(value, ">");
		}

		/**
		 * Returns TRUE if the target value is greater than the compare value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, T rightValue) {
			return leftValue.compareTo(rightValue) > 0;
		}
	}

	/**
	 * Checks whether a target value is less than or equal to the compare
	 * value.
	 */
	public static class LessOrEqual<T extends Comparable<T>>
		extends Comparison<T, T> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public LessOrEqual(T value) {
			super(value, "<=");
		}

		/**
		 * Returns TRUE if the target value is less than or equal to the
		 * compare
		 * value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, T rightValue) {
			return leftValue.compareTo(rightValue) <= 0;
		}
	}

	/**
	 * Checks whether a target value is less than the compare value.
	 */
	public static class LessThan<T extends Comparable<T>>
		extends Comparison<T, T> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public LessThan(T value) {
			super(value, "<");
		}

		/**
		 * Returns TRUE if the target value is less than the compare value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, T rightValue) {
			return leftValue.compareTo(rightValue) < 0;
		}
	}

	/**
	 * Matches input values against a regular expression pattern. The string to
	 * match will be determined by invoking the toString() method on input
	 * values.
	 *
	 * @author eso
	 */
	public static class Matching<T> extends Comparison<T, Pattern> {

		/**
		 * Creates a new instance from a certain regular expression.
		 *
		 * @param regularExpression The regular expression
		 */
		public Matching(String regularExpression) {
			this(Pattern.compile(regularExpression));
		}

		/**
		 * Creates a new instance from a certain regular expression pattern.
		 *
		 * @param pattern The regular expression
		 */
		public Matching(Pattern pattern) {
			super(pattern, "matches");
		}

		/**
		 * Converts the input value to a string by invoking toString() and
		 * matches it against the given regular expression pattern.
		 *
		 * @param input   The input value to evaluate
		 * @param pattern The pattern to match the input value against
		 * @return TRUE if the regular expression matches the input value's
		 * string representation
		 */
		@Override
		public Boolean evaluate(T input, Pattern pattern) {
			return pattern.matcher(input.toString()).matches();
		}
	}

	/**
	 * Compares target values for identity with the compare value.
	 */
	public static class SameAs<T> extends Comparison<T, Object> {

		/**
		 * @see Comparison#Comparison(Object, String)
		 */
		public SameAs(Object value) {
			super(value, "==");
		}

		/**
		 * Returns TRUE if the target value is equal to the compare value.
		 *
		 * @see BinaryFunction#evaluate(Object, Object)
		 */
		@Override
		public Boolean evaluate(T leftValue, Object rightValue) {
			return leftValue == rightValue;
		}
	}
}
