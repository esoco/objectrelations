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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.GetElement.GetField;
import de.esoco.lib.expression.function.GetElement.ReadField;
import de.esoco.lib.expression.predicate.BinaryPredicateChain;
import de.esoco.lib.expression.predicate.Comparison;
import de.esoco.lib.expression.predicate.Comparison.EqualTo;
import de.esoco.lib.expression.predicate.Comparison.GreaterOrEqual;
import de.esoco.lib.expression.predicate.Comparison.GreaterThan;
import de.esoco.lib.expression.predicate.Comparison.LessOrEqual;
import de.esoco.lib.expression.predicate.Comparison.LessThan;
import de.esoco.lib.expression.predicate.Comparison.Matching;
import de.esoco.lib.expression.predicate.Comparison.SameAs;
import de.esoco.lib.expression.predicate.ElementPredicate;
import de.esoco.lib.expression.predicate.FunctionPredicate;
import de.esoco.lib.expression.predicate.PredicateChain;
import de.esoco.lib.expression.predicate.PredicateJoin;
import de.esoco.lib.expression.predicate.ThrowingBinaryPredicate;
import de.esoco.lib.expression.predicate.ThrowingPredicate;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import java.util.regex.Pattern;

/**
 * Contains factory methods that return standard predicates.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class Predicates {
	/**
	 * Always returns true
	 */
	private static final Predicate<?> TRUE = v -> true;

	/**
	 * Always returns false
	 */
	private static final Predicate<?> FALSE = v -> false;

	/**
	 * Tests if a value is null
	 */
	private static final Predicate<?> IS_NULL = new EqualTo<Object>(null) {
		@Override
		public Boolean evaluate(Object value, Object ignored) {
			return value == null;
		}
	};

	/**
	 * Tests if a value is not null
	 */
	private static final Predicate<?> NOT_NULL = not(IS_NULL);

	/**
	 * Private, only static use.
	 */
	private Predicates() {
	}

	/**
	 * Returns a predicate that will always yield FALSE.
	 *
	 * @return A predicate constant that always returns FALSE
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysFalse() {
		return (Predicate<T>) FALSE;
	}

	/**
	 * Returns a predicate that will always yield TRUE.
	 *
	 * @return A predicate constant that always returns TRUE
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysTrue() {
		return (Predicate<T>) TRUE;
	}

	/**
	 * Creates a new predicate that combines two other predicates with a
	 * logical
	 * and expression. The second predicate will only be evaluated if the first
	 * one evaluates as TRUE. Either of the two predicates can be NULL in which
	 * case the other predicate will be returned. This can be used to
	 * dynamically chain predicates without the need to check for NULL values.
	 * If both predicates are null NULL will be returned.
	 *
	 * @param first  The first predicate
	 * @param second The second predicate
	 * @return A new predicate combining the arguments with a logical and
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> and(Predicate<? super T> first,
		Predicate<? super T> second) {
		if (second == null) {
			return (Predicate<T>) first;
		} else if (first == null) {
			return (Predicate<T>) second;
		}

		return new And<T>(first, second);
	}

	/**
	 * A specialized variant of {@link Functions#chain(Function, Function)}
	 * that
	 * creates a new predicate instead of a function. This allows to use the
	 * result of chaining a predicate with a function as a predicate again.
	 *
	 * @param outer The predicate that evaluates the inner function results
	 * @param inner The function that produces the predicate input values
	 * @return A new instance of {@link PredicateChain}
	 */
	public static <T, I> Predicate<T> chain(final Predicate<I> outer,
		final Function<T, ? extends I> inner) {
		return new PredicateChain<T, I>(outer, inner);
	}

	/**
	 * A specialized variant of
	 * {@link Functions#chain(BinaryFunction, Function, Function)} that creates
	 * a new binary predicate instead of a function. This allows to use the
	 * result of chaining a binary predicate with two functions as a predicate
	 * again.
	 *
	 * @param outer The predicate that evaluates the results of the left and
	 *              right functions
	 * @param left  The function that produces the left predicate input
	 * @param right The function that produces the right predicate input
	 * @return A new instance of {@link BinaryPredicateChain}
	 */
	public static <L, R, V, W> BinaryPredicate<L, R> chain(
		final BinaryPredicate<V, W> outer, final Function<L, ? extends V> left,
		final Function<R, ? extends W> right) {
		return new BinaryPredicateChain<L, R, V, W>(outer, left, right);
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * by means of the {@link Object#equals(Object)} method. The predicate
	 * yields TRUE if the tested object is equal to the other object. NULL
	 * values for both objects are allowed.
	 *
	 * @param value The value to compare the predicate targets with
	 * @return A new instance of the {@link EqualTo} predicate
	 */
	public static <T> Comparison<T, Object> equalTo(Object value) {
		return new EqualTo<T>(value);
	}

	/**
	 * A helper method that returns the first predicate that is not an instance
	 * of {@link PredicateJoin}. It is found by recursively traversing the tree
	 * of predicates by means of the method {@link PredicateJoin#getLeft()}.
	 * The
	 * first predicate that is not a join will be returned.
	 *
	 * @param predicate The predicate to start traversing the tree at
	 * @return The first non-join predicate
	 */
	public static Predicate<?> firstInChain(Predicate<?> predicate) {
		if (predicate instanceof PredicateJoin<?>) {
			predicate = firstInChain(((PredicateJoin<?>) predicate).getLeft());
		}

		return predicate;
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is greater than or equal to the
	 * other object.
	 *
	 * @param value The value to compare the predicate's argument with
	 * @return A new instance of the {@link GreaterOrEqual} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> greaterOrEqual(
		T value) {
		return new GreaterOrEqual<T>(value);
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is greater than the other object.
	 *
	 * @param value The value to compare the predicate's argument with
	 * @return A new instance of the {@link GreaterThan} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> greaterThan(
		T value) {
		return new GreaterThan<T>(value);
	}

	/**
	 * Creates an {@link ElementPredicate} for a certain field in target
	 * objects. It creates an instance of the function {@link ReadField} to
	 * retrieve the field value from target objects.
	 *
	 * <p>
	 * <b>Attention:</b> if the generic value type (V) that is defined by the
	 * predicate is not of type Object the field value will be cast to that
	 * type
	 * at runtime. If the types do not match a ClassCastException will be
	 * thrown
	 * when evaluating the predicate.
	 * </p>
	 *
	 * @param field     The name of the field to evaluate the value of
	 * @param predicate The predicate to evaluate the field value with
	 * @return A new instance of {@link ElementPredicate} for field access
	 */
	public static <T, V> ElementPredicate<T, V> ifField(String field,
		Predicate<V> predicate) {
		return new ElementPredicate<T, V>(new ReadField<T, V>(field),
			predicate);
	}

	/**
	 * Creates an {@link ElementPredicate} for a certain property in target
	 * objects. It creates an instance of the function {@link GetField} to
	 * retrieve the property value from target objects.
	 *
	 * <p>
	 * <b>Attention:</b> if the generic value type (V) that is defined by the
	 * predicate is not of type Object the property value will be cast to that
	 * type at runtime. If the types do not match a ClassCastException will be
	 * thrown when evaluating the predicate.
	 * </p>
	 *
	 * @param property  The name of the property to evaluate the value of
	 * @param predicate The predicate to evaluate the field value with
	 * @return A new instance of {@link ElementPredicate} for field access
	 */
	public static <T, V> ElementPredicate<T, V> ifProperty(String property,
		Predicate<V> predicate) {
		return new ElementPredicate<T, V>(new GetField<T, V>(property),
			predicate);
	}

	/**
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * property relation in related objects. This is just a property-specific
	 * name for the method {@link #ifRelation(RelationType, Predicate)}.
	 *
	 * @see #ifRelation(RelationType, Predicate)
	 */
	public static <T extends Relatable, V> Predicate<T> ifProperty(
		RelationType<V> type, Predicate<? super V> predicate) {
		return ifRelation(type, predicate);
	}

	/**
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * relation in related objects.
	 *
	 * @param type      The relation type to evaluate the target value of
	 * @param predicate The predicate to evaluate the field value with
	 * @return A new instance of {@link ElementPredicate} for relation access
	 */
	public static <T extends Relatable, V> ElementPredicate<T, V> ifRelation(
		RelationType<V> type, Predicate<? super V> predicate) {
		return new ElementPredicate<T, V>(type, predicate);
	}

	/**
	 * Returns a predicate that tests if the target object is NULL.
	 *
	 * @return A constant predicate that yields TRUE if the target object is
	 * NULL
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> isNull() {
		return (Predicate<T>) IS_NULL;
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is less than or equal to the other
	 * object.
	 *
	 * @param value The value to compare the predicate's argument with
	 * @return A new instance of the {@link LessOrEqual} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> lessOrEqual(
		T value) {
		return new LessOrEqual<T>(value);
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is less than the other object.
	 *
	 * @param value The value to compare the predicate's argument with
	 * @return A new instance of the {@link LessThan} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> lessThan(T value) {
		return new LessThan<T>(value);
	}

	/**
	 * Shortcut method to create a predicate that matches input values
	 * against a
	 * regular expression pattern.
	 *
	 * @param regularExpression The regular expression pattern string
	 * @return A new instance of the {@link Matching} predicate
	 */
	public static <T> Predicate<T> matching(String regularExpression) {
		return new Matching<T>(regularExpression);
	}

	/**
	 * Shortcut method to create a predicate that matches input values
	 * against a
	 * regular expression pattern.
	 *
	 * @param pattern The regular expression pattern
	 * @return A new instance of the {@link Matching} predicate
	 */
	public static <T> Predicate<T> matching(Pattern pattern) {
		return new Matching<T>(pattern);
	}

	/**
	 * Returns the logical negation of a particular predicate.
	 *
	 * @param predicate The predicate to negate
	 * @return A new predicate with a logical NOT expression
	 */
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return new Not<T>(predicate);
	}

	/**
	 * Returns a predicate that tests if the target object is not null.
	 *
	 * @return A predicate constant that returns TRUE if the target object is
	 * not NULL
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> notNull() {
		return (Predicate<T>) NOT_NULL;
	}

	/**
	 * Creates a new predicate that combines two other predicates with a
	 * logical
	 * or expression. The second predicate will only be evaluated if the first
	 * one evaluates as FALSE. Either of the two predicates can be NULL in
	 * which
	 * case the other predicate will be returned. This can be used to
	 * dynamically chain predicates without the need to check for NULL values.
	 * If both predicates are null NULL will be returned.
	 *
	 * @param first  The first predicate
	 * @param second The second predicate
	 * @return A new predicate combining the arguments with a logical or
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> or(Predicate<? super T> first,
		Predicate<? super T> second) {
		if (second == null) {
			return (Predicate<T>) first;
		} else if (first == null) {
			return (Predicate<T>) second;
		}

		return new Or<T>(first, second);
	}

	/**
	 * Returns a predicate that compares the target object with another object
	 * by means of the comparison operator ('=='). The predicate yields TRUE if
	 * the tested object is identical to the other object. NULL values for both
	 * objects are allowed.
	 *
	 * @param value The value to compare the predicate targets with
	 * @return A new instance of the {@link SameAs} predicate
	 */
	public static <T> Comparison<T, Object> sameAs(Object value) {
		return new SameAs<T>(value);
	}

	/**
	 * Takes a predicate that throws an exception and returns it as a predicate
	 * that can be executed without a checked exception. This method is mainly
	 * intended to be used with lambdas that throw exceptions.
	 *
	 * @param checked The checked predicate to wrap as unchecked
	 * @return The unchecked predicate
	 */
	public static <T> Predicate<T> unchecked(ThrowingPredicate<T> checked) {
		return checked;
	}

	/**
	 * Takes a binary predicate that throws an exception and returns it as a
	 * binary predicate that can be executed without a checked exception. This
	 * method is mainly intended to be used with lambdas that throw exceptions.
	 *
	 * @param checked The checked predicate to wrap as unchecked
	 * @return The unchecked predicate
	 */
	public static <L, R> BinaryPredicate<L, R> unchecked(
		ThrowingBinaryPredicate<L, R> checked) {
		return checked;
	}

	/**
	 * Returns a new predicate instance that counts down from a certain
	 * value to
	 * zero and returns TRUE while the value is still greater than zero and
	 * FALSE as soon as it has reached zero.
	 *
	 * @param value The value to count to zero from
	 * @return A new predicate instance
	 */
	public static <T> Predicate<T> untilCountDown(int value) {
		return new Predicate<T>() {
			int countValue = value;

			@Override
			public Boolean evaluate(T value) {
				return countValue-- > 0;
			}
		};
	}

	/**
	 * Returns a new instance of {@link FunctionPredicate} that evaluates the
	 * result of applying a function to input objects with a predicate.
	 *
	 * @param function  The function to apply to input values
	 * @param predicate The predicate to evaluate the function result with
	 * @return A new predicate
	 */
	public static <I, O, T extends I> Predicate<T> when(Function<I, O> function,
		Predicate<? super O> predicate) {
		return new FunctionPredicate<T, O>(function, predicate);
	}

	// ~ Inner Classes
	// ----------------------------------------------------------

	/**
	 * Inner class for a logical AND between two predicates.
	 *
	 * @author eso
	 */
	public static class And<T> extends PredicateJoin<T> {
		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * @see PredicateJoin#PredicateJoin(Predicate, Predicate, String)
		 */
		And(Predicate<? super T> left, Predicate<? super T> right) {
			super(left, right, "&&");
		}

		// ~ Methods
		// ------------------------------------------------------------

		/**
		 * Evaluates the predicates of this instance with a logical AND
		 * expression. The right predicate will only be evaluated if the left
		 * one evaluates as TRUE.
		 *
		 * @see PredicateJoin#evaluate(Predicate, Predicate, Object)
		 */
		@Override
		protected Boolean evaluate(Predicate<? super T> left,
			Predicate<? super T> right, T value) {
			return left.evaluate(value) && right.evaluate(value);
		}
	}

	/**
	 * A predicate that inverts the result another predicate.
	 */
	public static class Not<T> implements Predicate<T> {
		// ~ Instance fields
		// ----------------------------------------------------

		private final Predicate<T> predicate;

		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * Creates a new instance.
		 *
		 * @param predicate The predicate to invert
		 */
		public Not(Predicate<T> predicate) {
			this.predicate = predicate;
		}

		// ~ Methods
		// ------------------------------------------------------------

		/**
		 * Returns the logical inversion of the result of the evaluate() method
		 * of the underlying predicate.
		 *
		 * @param target The target value to be evaluated by the predicate
		 * @return The inverted result of the evaluation
		 */
		@Override
		public Boolean evaluate(T target) {
			return !predicate.evaluate(target);
		}

		/**
		 * Returns the predicate that is inverted by this instance.
		 *
		 * @return The inverted predicate
		 */
		public final Predicate<T> getInvertedPredicate() {
			return predicate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return getToken() + " " + predicate;
		}
	}

	/**
	 * Implementation of a logical OR between two predicates.
	 */
	public static class Or<T> extends PredicateJoin<T> {
		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * @see PredicateJoin#PredicateJoin(Predicate, Predicate, String)
		 */
		Or(Predicate<? super T> left, Predicate<? super T> right) {
			super(left, right, "||");
		}

		// ~ Methods
		// ------------------------------------------------------------

		/**
		 * Evaluates the predicates of this instance with a logical OR
		 * expression. The right predicate will only be evaluated if the left
		 * one evaluates as FALSE.
		 *
		 * @see PredicateJoin#evaluate(Predicate, Predicate, Object)
		 */
		@Override
		protected Boolean evaluate(Predicate<? super T> left,
			Predicate<? super T> right, T value) {
			return left.evaluate(value) || right.evaluate(value);
		}
	}
}
