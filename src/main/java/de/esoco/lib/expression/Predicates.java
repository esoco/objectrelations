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

import java.util.regex.Pattern;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * Contains factory methods that return standard predicates.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class Predicates
{
	//~ Static fields/initializers ---------------------------------------------

	/** Always returns true */
	private static final Predicate<?> TRUE = v -> true;

	/** Always returns false */
	private static final Predicate<?> FALSE = v -> false;

	/** Tests if a value is null */
	private static final Predicate<?> IS_NULL =
		new EqualTo<Object>(null)
		{
			@Override
			public final Boolean evaluate(Object rValue, Object rNull)
			{
				return rValue == null;
			}
		};

	/** Tests if a value is not null */
	private static final Predicate<?> NOT_NULL = not(IS_NULL);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Predicates()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a predicate that will always yield FALSE.
	 *
	 * @return A predicate constant that always returns FALSE
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysFalse()
	{
		return (Predicate<T>) FALSE;
	}

	/***************************************
	 * Returns a predicate that will always yield TRUE.
	 *
	 * @return A predicate constant that always returns TRUE
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysTrue()
	{
		return (Predicate<T>) TRUE;
	}

	/***************************************
	 * Creates a new predicate that combines two other predicates with a logical
	 * and expression. The second predicate will only be evaluated if the first
	 * one evaluates as TRUE. Either of the two predicates can be NULL in which
	 * case the other predicate will be returned. This can be used to
	 * dynamically chain predicates without the need to check for NULL values.
	 * If both predicates are null NULL will be returned.
	 *
	 * @param  rFirst  The first predicate
	 * @param  rSecond The second predicate
	 *
	 * @return A new predicate combining the arguments with a logical and
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> and(
		Predicate<? super T> rFirst,
		Predicate<? super T> rSecond)
	{
		if (rSecond == null)
		{
			return (Predicate<T>) rFirst;
		}
		else if (rFirst == null)
		{
			return (Predicate<T>) rSecond;
		}

		return new And<T>(rFirst, rSecond);
	}

	/***************************************
	 * A specialized variant of {@link Functions#chain(Function, Function)} that
	 * creates a new predicate instead of a function. This allows to use the
	 * result of chaining a predicate with a function as a predicate again.
	 *
	 * @param  rOuter The predicate that evaluates the inner function results
	 * @param  rInner The function that produces the predicate input values
	 *
	 * @return A new instance of {@link PredicateChain}
	 */
	public static <T, I> Predicate<T> chain(
		final Predicate<I>			   rOuter,
		final Function<T, ? extends I> rInner)
	{
		return new PredicateChain<T, I>(rOuter, rInner);
	}

	/***************************************
	 * A specialized variant of {@link Functions#chain(BinaryFunction, Function,
	 * Function)} that creates a new binary predicate instead of a function.
	 * This allows to use the result of chaining a binary predicate with two
	 * functions as a predicate again.
	 *
	 * @param  rOuter The predicate that evaluates the results of the left and
	 *                right functions
	 * @param  rLeft  The function that produces the left predicate input
	 * @param  rRight The function that produces the right predicate input
	 *
	 * @return A new instance of {@link BinaryPredicateChain}
	 */
	public static <L, R, V, W> BinaryPredicate<L, R> chain(
		final BinaryPredicate<V, W>    rOuter,
		final Function<L, ? extends V> rLeft,
		final Function<R, ? extends W> rRight)
	{
		return new BinaryPredicateChain<L, R, V, W>(rOuter, rLeft, rRight);
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * by means of the {@link Object#equals(Object)} method. The predicate
	 * yields TRUE if the tested object is equal to the other object. NULL
	 * values for both objects are allowed.
	 *
	 * @param  rValue The value to compare the predicate targets with
	 *
	 * @return A new instance of the {@link EqualTo} predicate
	 */
	public static <T> Comparison<T, Object> equalTo(Object rValue)
	{
		return new EqualTo<T>(rValue);
	}

	/***************************************
	 * A helper method that returns the first predicate that is not an instance
	 * of {@link PredicateJoin}. It is found by recursively traversing the tree
	 * of predicates by means of the method {@link PredicateJoin#getLeft()}. The
	 * first predicate that is not a join will be returned.
	 *
	 * @param  rPredicate The predicate to start traversing the tree at
	 *
	 * @return The first non-join predicate
	 */
	public static Predicate<?> firstInChain(Predicate<?> rPredicate)
	{
		if (rPredicate instanceof PredicateJoin<?>)
		{
			rPredicate =
				firstInChain(((PredicateJoin<?>) rPredicate).getLeft());
		}

		return rPredicate;
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is greater than or equal to the
	 * other object.
	 *
	 * @param  rValue The value to compare the predicate's argument with
	 *
	 * @return A new instance of the {@link GreaterOrEqual} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> greaterOrEqual(
		T rValue)
	{
		return new GreaterOrEqual<T>(rValue);
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is greater than the other object.
	 *
	 * @param  rValue The value to compare the predicate's argument with
	 *
	 * @return A new instance of the {@link GreaterThan} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> greaterThan(
		T rValue)
	{
		return new GreaterThan<T>(rValue);
	}

	/***************************************
	 * Creates an {@link ElementPredicate} for a certain field in target
	 * objects. It creates an instance of the function {@link ReadField} to
	 * retrieve the field value from target objects.
	 *
	 * <p><b>Attention:</b> if the generic value type (V) that is defined by the
	 * predicate is not of type Object the field value will be cast to that type
	 * at runtime. If the types do not match a ClassCastException will be thrown
	 * when evaluating the predicate.</p>
	 *
	 * @param  sField     The name of the field to evaluate the value of
	 * @param  rPredicate The predicate to evaluate the field value with
	 *
	 * @return A new instance of {@link ElementPredicate} for field access
	 */
	public static <T, V> ElementPredicate<T, V> ifField(
		String		 sField,
		Predicate<V> rPredicate)
	{
		return new ElementPredicate<T, V>(
			new ReadField<T, V>(sField),
			rPredicate);
	}

	/***************************************
	 * Creates an {@link ElementPredicate} for a certain property in target
	 * objects. It creates an instance of the function {@link GetField} to
	 * retrieve the property value from target objects.
	 *
	 * <p><b>Attention:</b> if the generic value type (V) that is defined by the
	 * predicate is not of type Object the property value will be cast to that
	 * type at runtime. If the types do not match a ClassCastException will be
	 * thrown when evaluating the predicate.</p>
	 *
	 * @param  sProperty  The name of the property to evaluate the value of
	 * @param  rPredicate The predicate to evaluate the field value with
	 *
	 * @return A new instance of {@link ElementPredicate} for field access
	 */
	public static <T, V> ElementPredicate<T, V> ifProperty(
		String		 sProperty,
		Predicate<V> rPredicate)
	{
		return new ElementPredicate<T, V>(
			new GetField<T, V>(sProperty),
			rPredicate);
	}

	/***************************************
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * property relation in related objects. This is just a property-specific
	 * name for the method {@link #ifRelation(RelationType, Predicate)}.
	 *
	 * @see #ifRelation(RelationType, Predicate)
	 */
	public static <T extends Relatable, V> Predicate<T> ifProperty(
		RelationType<V>		 rType,
		Predicate<? super V> rPredicate)
	{
		return ifRelation(rType, rPredicate);
	}

	/***************************************
	 * Creates an {@link ElementPredicate} to evaluate a predicate on a certain
	 * relation in related objects.
	 *
	 * @param  rType      The relation type to evaluate the target value of
	 * @param  rPredicate The predicate to evaluate the field value with
	 *
	 * @return A new instance of {@link ElementPredicate} for relation access
	 */
	public static <T extends Relatable, V> ElementPredicate<T, V> ifRelation(
		RelationType<V>		 rType,
		Predicate<? super V> rPredicate)
	{
		return new ElementPredicate<T, V>(rType, rPredicate);
	}

	/***************************************
	 * Returns a predicate that tests if the target object is NULL.
	 *
	 * @return A constant predicate that yields TRUE if the target object is
	 *         NULL
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> isNull()
	{
		return (Predicate<T>) IS_NULL;
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is less than or equal to the other
	 * object.
	 *
	 * @param  rValue The value to compare the predicate's argument with
	 *
	 * @return A new instance of the {@link LessOrEqual} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> lessOrEqual(
		T rValue)
	{
		return new LessOrEqual<T>(rValue);
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * and yields TRUE if the tested object is less than the other object.
	 *
	 * @param  rValue The value to compare the predicate's argument with
	 *
	 * @return A new instance of the {@link LessThan} predicate
	 */
	public static <T extends Comparable<T>> Comparison<T, T> lessThan(T rValue)
	{
		return new LessThan<T>(rValue);
	}

	/***************************************
	 * Shortcut method to create a predicate that matches input values against a
	 * regular expression pattern.
	 *
	 * @param  sRegularExpression The regular expression pattern string
	 *
	 * @return A new instance of the {@link Matching} predicate
	 */
	public static <T> Predicate<T> matching(String sRegularExpression)
	{
		return new Matching<T>(sRegularExpression);
	}

	/***************************************
	 * Shortcut method to create a predicate that matches input values against a
	 * regular expression pattern.
	 *
	 * @param  rPattern The regular expression pattern
	 *
	 * @return A new instance of the {@link Matching} predicate
	 */
	public static <T> Predicate<T> matching(Pattern rPattern)
	{
		return new Matching<T>(rPattern);
	}

	/***************************************
	 * Returns the logical negation of a particular predicate.
	 *
	 * @param  rPredicate The predicate to negate
	 *
	 * @return A new predicate with a logical NOT expression
	 */
	public static <T> Predicate<T> not(Predicate<T> rPredicate)
	{
		return new Not<T>(rPredicate);
	}

	/***************************************
	 * Returns a predicate that tests if the target object is not null.
	 *
	 * @return A predicate constant that returns TRUE if the target object is
	 *         not NULL
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> notNull()
	{
		return (Predicate<T>) NOT_NULL;
	}

	/***************************************
	 * Creates a new predicate that combines two other predicates with a logical
	 * or expression. The second predicate will only be evaluated if the first
	 * one evaluates as FALSE. Either of the two predicates can be NULL in which
	 * case the other predicate will be returned. This can be used to
	 * dynamically chain predicates without the need to check for NULL values.
	 * If both predicates are null NULL will be returned.
	 *
	 * @param  rFirst  The first predicate
	 * @param  rSecond The second predicate
	 *
	 * @return A new predicate combining the arguments with a logical or
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> or(
		Predicate<? super T> rFirst,
		Predicate<? super T> rSecond)
	{
		if (rSecond == null)
		{
			return (Predicate<T>) rFirst;
		}
		else if (rFirst == null)
		{
			return (Predicate<T>) rSecond;
		}

		return new Or<T>(rFirst, rSecond);
	}

	/***************************************
	 * Returns a predicate that compares the target object with another object
	 * by means of the comparison operator ('=='). The predicate yields TRUE if
	 * the tested object is identical to the other object. NULL values for both
	 * objects are allowed.
	 *
	 * @param  rValue The value to compare the predicate targets with
	 *
	 * @return A new instance of the {@link SameAs} predicate
	 */
	public static <T> Comparison<T, Object> sameAs(Object rValue)
	{
		return new SameAs<T>(rValue);
	}

	/***************************************
	 * Takes a predicate that throws an exception and returns it as a predicate
	 * that can be executed without a checked exception. This method is mainly
	 * intended to be used with lambdas that throw exceptions.
	 *
	 * @param  pChecked The checked predicate to wrap as unchecked
	 *
	 * @return The unchecked predicate
	 */
	public static <T> Predicate<T> unchecked(ThrowingPredicate<T> pChecked)
	{
		return pChecked;
	}

	/***************************************
	 * Takes a binary predicate that throws an exception and returns it as a
	 * binary predicate that can be executed without a checked exception. This
	 * method is mainly intended to be used with lambdas that throw exceptions.
	 *
	 * @param  pChecked The checked predicate to wrap as unchecked
	 *
	 * @return The unchecked predicate
	 */
	public static <L, R> BinaryPredicate<L, R> unchecked(
		ThrowingBinaryPredicate<L, R> pChecked)
	{
		return pChecked;
	}

	/***************************************
	 * Returns a new predicate instance that counts down from a certain value to
	 * zero and returns TRUE while the value is still greater than zero and
	 * FALSE as soon as it has reached zero.
	 *
	 * @param  nValue The value to count to zero from
	 *
	 * @return A new predicate instance
	 */
	public static <T> Predicate<T> untilCountDown(int nValue)
	{
		return new Predicate<T>()
		{
			int nCountValue = nValue;

			@Override
			public Boolean evaluate(T rValue)
			{
				return nCountValue-- > 0;
			}
		};
	}

	/***************************************
	 * Returns a new instance of {@link FunctionPredicate} that evaluates the
	 * result of applying a function to input objects with a predicate.
	 *
	 * @param  rFunction  The function to apply to input values
	 * @param  rPredicate The predicate to evaluate the function result with
	 *
	 * @return A new predicate
	 */
	public static <I, O, T extends I> Predicate<T> when(
		Function<I, O>		 rFunction,
		Predicate<? super O> rPredicate)
	{
		return new FunctionPredicate<T, O>(rFunction, rPredicate);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Inner class for a logical AND between two predicates.
	 *
	 * @author eso
	 */
	public static class And<T> extends PredicateJoin<T>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * @see PredicateJoin#PredicateJoin(Predicate, Predicate, String)
		 */
		And(Predicate<? super T> rLeft, Predicate<? super T> rRight)
		{
			super(rLeft, rRight, "&&");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Evaluates the predicates of this instance with a logical AND
		 * expression. The right predicate will only be evaluated if the left
		 * one evaluates as TRUE.
		 *
		 * @see PredicateJoin#evaluate(Predicate, Predicate, Object)
		 */
		@Override
		protected Boolean evaluate(Predicate<? super T> rLeft,
								   Predicate<? super T> rRight,
								   T					rValue)
		{
			return rLeft.evaluate(rValue) && rRight.evaluate(rValue);
		}
	}

	/********************************************************************
	 * A predicate that inverts the result another predicate.
	 */
	public static class Not<T> implements Predicate<T>
	{
		//~ Instance fields ----------------------------------------------------

		private final Predicate<T> rPredicate;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rPredicate The predicate to invert
		 */
		public Not(Predicate<T> rPredicate)
		{
			this.rPredicate = rPredicate;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the logical inversion of the result of the evaluate() method
		 * of the underlying predicate.
		 *
		 * @param  rTarget The target value to be evaluated by the predicate
		 *
		 * @return The inverted result of the evaluation
		 */
		@Override
		public Boolean evaluate(T rTarget)
		{
			return !rPredicate.evaluate(rTarget);
		}

		/***************************************
		 * Returns the predicate that is inverted by this instance.
		 *
		 * @return The inverted predicate
		 */
		public final Predicate<T> getInvertedPredicate()
		{
			return rPredicate;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return getToken() + " " + rPredicate;
		}
	}

	/********************************************************************
	 * Implementation of a logical OR between two predicates.
	 */
	public static class Or<T> extends PredicateJoin<T>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * @see PredicateJoin#PredicateJoin(Predicate, Predicate, String)
		 */
		Or(Predicate<? super T> rLeft, Predicate<? super T> rRight)
		{
			super(rLeft, rRight, "||");
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Evaluates the predicates of this instance with a logical OR
		 * expression. The right predicate will only be evaluated if the left
		 * one evaluates as FALSE.
		 *
		 * @see PredicateJoin#evaluate(Predicate, Predicate, Object)
		 */
		@Override
		protected Boolean evaluate(Predicate<? super T> rLeft,
								   Predicate<? super T> rRight,
								   T					rValue)
		{
			return rLeft.evaluate(rValue) || rRight.evaluate(rValue);
		}
	}
}
