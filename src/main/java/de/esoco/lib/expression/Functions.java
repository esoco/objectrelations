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

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.AbstractInvertibleFunction;
import de.esoco.lib.expression.function.BinaryFunctionChain.LeftFunctionChain;
import de.esoco.lib.expression.function.BinaryFunctionChain.RightFunctionChain;
import de.esoco.lib.expression.function.BinaryFunctionGroup;
import de.esoco.lib.expression.function.ConditionalFunction;
import de.esoco.lib.expression.function.DualFunctionChain;
import de.esoco.lib.expression.function.FunctionChain;
import de.esoco.lib.expression.function.FunctionGroup;
import de.esoco.lib.expression.function.GetElement.GetField;
import de.esoco.lib.expression.function.GetElement.GetRelation;
import de.esoco.lib.expression.function.GetElement.GetRelationValue;
import de.esoco.lib.expression.function.GetElement.ReadField;
import de.esoco.lib.expression.function.Invert;
import de.esoco.lib.expression.function.Print;
import de.esoco.lib.expression.function.SetElement.SetRelationValue;
import de.esoco.lib.expression.function.ThrowingConsumer;
import de.esoco.lib.expression.function.ThrowingFunction;
import de.esoco.lib.expression.function.ThrowingSupplier;
import de.esoco.lib.reflect.ReflectUtil;

import java.io.PrintWriter;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;


/********************************************************************
 * Contains factory methods to create standard functions.
 *
 * @author eso
 */
public class Functions
{
	//~ Static fields/initializers ---------------------------------------------

	/** A runnable instance that does nothing. */
	public static final Runnable NO_OPERATION = () ->{};

	private static final InvertibleFunction<Object, Object> IDENTITY =
		new AbstractInvertibleFunction<Object, Object>("Identity")
		{
			/***************************************
			 * @see Function#evaluate(Object)
			 */
			@Override
			public Object evaluate(Object rValue)
			{
				return rValue;
			}

			/***************************************
			 * @see InvertibleFunction#invert(Object)
			 */
			@Override
			public Object invert(Object rValue)
			{
				return rValue;
			}
		};

	private static final Function<Number, Number> THREAD_SLEEP =
		new AbstractFunction<Number, Number>("ThreadSleep")
		{
			@Override
			public Number evaluate(Number nTime)
			{
				try
				{
					Thread.sleep(nTime.longValue());
				}
				catch (InterruptedException e)
				{ // just terminate; interruption must be handled elsewhere
				}

				return nTime;
			}
		};

	private static final Function<Object, Object> THREAD_YIELD =
		new AbstractFunction<Object, Object>("ThreadYield")
		{
			@Override
			public Object evaluate(Object rValue)
			{
				Thread.yield();

				return rValue;
			}
		};

	private static final Function<Object, Long> CURRENT_TIME_MILLIS =
		new AbstractFunction<Object, Long>("CurrentTimeMillis")
		{
			@Override
			@SuppressWarnings("boxing")
			public Long evaluate(Object rValue)
			{
				return System.currentTimeMillis();
			}
		};

	private static final Function<Object, Integer> UNIX_TIMESTAMP =
		new AbstractFunction<Object, Integer>("UnixTimestamp")
		{
			@Override
			@SuppressWarnings("boxing")
			public Integer evaluate(Object rValue)
			{
				return (int) (System.currentTimeMillis() / 1000);
			}
		};

	private static final Function<RelationType<?>, String> GET_RELATION_TYPE_SIMPLE_NAME =
		new AbstractFunction<RelationType<?>, String>("GetSimpleName")
		{
			@Override
			public String evaluate(RelationType<?> rRelationType)
			{
				return rRelationType.getSimpleName();
			}
		};

	private static final Function<String, RelationType<?>> GET_RELATION_TYPE =
		new AbstractFunction<String, RelationType<?>>("GetRelationType")
		{
			@Override
			public RelationType<?> evaluate(String sTypeName)
			{
				return RelationType.valueOf(sTypeName);
			}
		};

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Functions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Converts a function into an action that ignores the return value.
	 *
	 * @param  rFunction The function to convert
	 *
	 * @return A new action
	 */
	public static <T> Action<T> asAction(Function<T, ?> rFunction)
	{
		return v -> rFunction.evaluate(v);
	}

	/***************************************
	 * Returns a function that invokes {@link Object#toString()} on the input
	 * value.
	 *
	 * @return A function constant that converts input objects to strings
	 */
	public static <T> Function<T, String> asString()
	{
		return v -> v != null ? v.toString() : "null";
	}

	/***************************************
	 * Chains two functions together. The output value that is created by
	 * evaluating an input value with the inner function will be evaluated by
	 * the outer function and the result will be returned. This is equivalent to
	 * {@code return rOuter.evaluate(rInner.evaluate(rInputValue))}. The generic
	 * parameter 'V' designates the intermediate value that is exchanged between
	 * the two functions.
	 *
	 * @param  rOuter The outer function
	 * @param  rInner The inner function
	 *
	 * @return A new instance of {@link FunctionChain}
	 */
	public static <I, V, O> Function<I, O> chain(
		Function<V, O>			 rOuter,
		Function<I, ? extends V> rInner)
	{
		return new FunctionChain<I, V, O>(rOuter, rInner);
	}

	/***************************************
	 * Chains a binary functions together with two other functions. The output
	 * values that are created by evaluating the left and right input values
	 * with the left and right functions will be evaluated by the binary outer
	 * function and the result will be returned. This is equivalent to {@code
	 * return rOuter.evaluate(rLeft.evaluate(rLeftValue),
	 * rRight.evaluate(rRightValue))}. The generic parameters 'V' and 'W'
	 * designate the intermediate values that are exchanged between the
	 * functions.
	 *
	 * @param  rOuter The binary outer function
	 * @param  rLeft  The function to evaluate the left input value with
	 * @param  rRight The function to evaluate the right input value with
	 *
	 * @return A new instance of {@link FunctionChain}
	 */
	public static <L, R, V, W, O> BinaryFunction<L, R, O> chain(
		BinaryFunction<V, W, O>  rOuter,
		Function<L, ? extends V> rLeft,
		Function<R, ? extends W> rRight)
	{
		return new DualFunctionChain<L, R, V, W, O>(rOuter, rLeft, rRight);
	}

	/***************************************
	 * Chains two binary functions together on their left value so that the left
	 * value of the outer function is generated by the inner function and the
	 * right value is forwarded unmodified.
	 *
	 * @param  rOuter The outer function
	 * @param  rInner The inner function
	 *
	 * @return A new binary function chain
	 */
	public static <L, R, V, O> BinaryFunction<L, R, O> chainLeft(
		BinaryFunction<V, R, O>			  rOuter,
		BinaryFunction<L, R, ? extends V> rInner)
	{
		return new LeftFunctionChain<>(rOuter, rInner);
	}

	/***************************************
	 * Chains two binary functions together on their right value so that the
	 * right value of the outer function is generated by the inner function and
	 * the left value is forwarded unmodified.
	 *
	 * @param  rOuter The outer function
	 * @param  rInner The inner function
	 *
	 * @return A new binary function chain
	 */
	public static <L, R, V, O> BinaryFunction<L, R, O> chainRight(
		BinaryFunction<L, V, O>			  rOuter,
		BinaryFunction<L, R, ? extends V> rInner)
	{
		return new RightFunctionChain<>(rOuter, rInner);
	}

	/***************************************
	 * Coerces a function instance to certain generic parameters. This method
	 * should be used with caution and only under special circumstances, e.g.
	 * for the parsing of functions without evaluation.
	 *
	 * @param  fFunction The function to coerce
	 *
	 * @return The function coerced to the given generic parameters
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> Function<I, O> coerce(Function<?, ?> fFunction)
	{
		return (Function<I, O>) fFunction;
	}

	/***************************************
	 * Returns a new comparator instance that applies a function to input
	 * objects to determine the actual comparable values. NULL results of the
	 * compare function are supported.
	 *
	 * @param  fGetComparable The function to be applied to input objects to
	 *                        obtain the comparable values
	 *
	 * @return A new {@link Comparator} instance
	 */
	public static <T, C extends Comparable<C>> Comparator<T> compare(
		final Function<? super T, C> fGetComparable)
	{
		return new Comparator<T>()
		{
			@Override
			public int compare(T o1, T o2)
			{
				C c1 = fGetComparable.evaluate(o1);
				C c2 = fGetComparable.evaluate(o2);

				return c1 != null && c2 != null
					   ? c1.compareTo(c2)
					   : c1 == null && c2 == null ? 0 : c2 == null ? 1 : -1;
			}
		};
	}

	/***************************************
	 * Returns a function that returns the current system time as returned by
	 * {@link System#currentTimeMillis()}.
	 *
	 * @return A function constant that returns the current system time
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<T, Long> currentTimeMillis()
	{
		return (Function<T, Long>) CURRENT_TIME_MILLIS;
	}

	/***************************************
	 * Returns a single function that will apply several functions successively
	 * to input values in the order in which they appear in the argument
	 * function list. The return value of the resulting function will be the
	 * input value to support function chaining.
	 *
	 * @param  fFirst               The first function to apply to input values
	 * @param  rAdditionalFunctions Additional functions to apply to input
	 *                              values
	 *
	 * @return A new function that evaluates all argument functions
	 */
	@SafeVarargs
	public static <I> Function<I, I> doAll(
		Function<? super I, ?>    fFirst,
		Function<? super I, ?>... rAdditionalFunctions)
	{
		return FunctionGroup.of(fFirst, rAdditionalFunctions);
	}

	/***************************************
	 * Returns a single binary function that will apply several binary functions
	 * successively to left and right input values in the order in which they
	 * appear in the argument function list. The return value will be the
	 * left-side input value to support function chaining.
	 *
	 * @param  rRightValue The default right-side value for unary function
	 *                     invocations
	 * @param  fFirst      The first function to apply to input values
	 * @param  fSecond     The second function to apply to input values
	 * @param  rFunctions  The binary functions to apply to input values
	 *
	 * @return The unchanged left-side input value
	 */
	@SafeVarargs
	public static <L, R> BinaryFunction<L, R, L> doAll(
		R										   rRightValue,
		BinaryFunction<? super L, ? super R, ?>    fFirst,
		BinaryFunction<? super L, ? super R, ?>    fSecond,
		BinaryFunction<? super L, ? super R, ?>... rFunctions)
	{
		List<BinaryFunction<? super L, ? super R, ?>> aFunctions =
			new ArrayList<BinaryFunction<? super L, ? super R, ?>>();

		aFunctions.add(fFirst);
		aFunctions.add(fSecond);

		if (rFunctions != null)
		{
			aFunctions.addAll(Arrays.asList(rFunctions));
		}

		return new BinaryFunctionGroup<L, R>(rRightValue, aFunctions);
	}

	/***************************************
	 * Returns a new conditional function that will evaluate input values with a
	 * certain function if a predicate for this value yields TRUE. If the
	 * predicate yields FALSE no function will be evaluated. Conditional
	 * functions that also handle a predicate result of FALSE can be created
	 * either by invoking {@link #doIfElse(Predicate, Function, Function)} or by
	 * invoking {@link ConditionalFunction#elseDo(Function)} on the returned
	 * conditional function. The latter variant will create one intermediate
	 * function and should therefore not be used in time-critical code.
	 *
	 * @param  rPredicate The predicate to evaluate
	 * @param  rFunction  The function to evaluate input values with if the
	 *                    predicate yields TRUE
	 *
	 * @return A new function for the conditional evaluation of a function
	 */
	public static <I, O> ConditionalFunction<I, O> doIf(
		Predicate<? super I>   rPredicate,
		Function<? super I, O> rFunction)
	{
		return new ConditionalFunction<I, O>(rPredicate, rFunction);
	}

	/***************************************
	 * Returns a new function that will evaluate an input value with a certain
	 * function if the result of a predicate for this value is TRUE. If the
	 * predicate is FALSE another function will be evaluated instead. Provides a
	 * more readable way to create instances of {@link ConditionalFunction}.
	 *
	 * @param  rPredicate The predicate to evaluate
	 * @param  rIf        The function to evaluate input values with if the
	 *                    predicate yields TRUE
	 * @param  rElse      The function to evaluate input values with if the
	 *                    predicate yields FALSE
	 *
	 * @return A new function for the conditional evaluation of functions
	 */
	public static <I, O> Function<I, O> doIfElse(
		Predicate<? super I>			 rPredicate,
		Function<? super I, ? extends O> rIf,
		Function<? super I, ? extends O> rElse)
	{
		return new ConditionalFunction<I, O>(rPredicate, rIf, rElse);
	}

	/***************************************
	 * Returns a new binary function instance that throws a certain exception
	 * when it's invoked. The exception's error message will be created by
	 * invoking {@link String#format(String, Object...)} with the given message
	 * string and the function's input value as the arguments. As the method
	 * {@link Function#evaluate(Object)} cannot throw checked exceptions the
	 * created exception will be wrapped in an instance of the special runtime
	 * exception class {@link FunctionException}.
	 *
	 * @param  sMessage        The error message with optional format elements
	 * @param  rExceptionClass The class of the exception to throw
	 *
	 * @return A new function instance
	 */
	public static <I, O> BinaryFunction<I, String, O> error(
		String							 sMessage,
		final Class<? extends Exception> rExceptionClass)
	{
		return new AbstractBinaryFunction<I, String, O>(sMessage, "error")
		{
			Constructor<? extends Exception> rConstructor =
				ReflectUtil.getPublicConstructor(rExceptionClass, String.class);

			@Override
			public O evaluate(I rValue, String sMessage)
			{
				Exception eError;

				sMessage = String.format(sMessage, rValue);

				try
				{
					eError = rConstructor.newInstance(sMessage);
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Could not create " +
													rExceptionClass,
													e);
				}

				if (eError instanceof RuntimeException)
				{
					throw (RuntimeException) eError;
				}
				else
				{
					throw new FunctionException(this, eError);
				}
			}
		};
	}

	/***************************************
	 * A helper method that returns the function in a {@link FunctionChain} that
	 * will be invoked first. It is found by recursively traversing the tree of
	 * functions by means of the method {@link FunctionChain#getInner()}.
	 *
	 * @param  rFunction The function to start searching at
	 *
	 * @return The first invoked function in a chain
	 *
	 * @see    #lastInChain(Function)
	 */
	public static Function<?, ?> firstInChain(Function<?, ?> rFunction)
	{
		if (rFunction instanceof FunctionChain<?, ?, ?>)
		{
			rFunction =
				firstInChain(((FunctionChain<?, ?, ?>) rFunction).getInner());
		}

		return rFunction;
	}

	/***************************************
	 * Returns a new instance of {@link GetField}.
	 *
	 * @param  sField The name of the field to get
	 *
	 * @return A new function instance
	 */
	public static <I, O> GetField<I, O> getField(String sField)
	{
		return new GetField<I, O>(sField);
	}

	/***************************************
	 * Returns a new instance of {@link GetRelation}.
	 *
	 * @param  rType The type of the relation to return
	 *
	 * @return A new function instance
	 */
	public static <I extends Relatable, O> Function<I, Relation<O>> getRelation(
		RelationType<O> rType)
	{
		return new GetRelation<I, O>(rType);
	}

	/***************************************
	 * Returns a static function that returns the relation type instance for a
	 * certain name by invoking {@link RelationType#valueOf(String)}.
	 *
	 * @return A static function instance
	 */
	public static final Function<String, RelationType<?>> getRelationType()
	{
		return GET_RELATION_TYPE;
	}

	/***************************************
	 * Returns a static function that returns the simple name of a relation type
	 * as returned by {@link RelationType#getSimpleName()}.
	 *
	 * @return A static function instance
	 */
	public static final Function<RelationType<?>, String>
	getRelationTypeSimpleName()
	{
		return GET_RELATION_TYPE_SIMPLE_NAME;
	}

	/***************************************
	 * Returns a new instance of {@link GetRelationValue}.
	 *
	 * @param  rType The type of the relation value to return
	 *
	 * @return A new function instance
	 */
	public static <I extends Relatable, O> Function<I, O> getRelationValue(
		RelationType<O> rType)
	{
		return new GetRelationValue<I, O>(rType);
	}

	/***************************************
	 * Returns an invertible identity function that returns the input value.
	 *
	 * @return A constant identity function
	 */
	@SuppressWarnings("unchecked")
	public static <T> InvertibleFunction<T, T> identity()
	{
		return (InvertibleFunction<T, T>) IDENTITY;
	}

	/***************************************
	 * A convenience method to create a new {@link #error(String, Class)}
	 * function that throws an {@link IllegalArgumentException}.
	 *
	 * @see #error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> illegalArgument(
		String sMessage)
	{
		return error(sMessage, IllegalArgumentException.class);
	}

	/***************************************
	 * A convenience method to create a new {@link #error(String, Class)}
	 * function that throws an {@link IllegalStateException}.
	 *
	 * @see #error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> illegalState(
		String sMessage)
	{
		return error(sMessage, IllegalStateException.class);
	}

	/***************************************
	 * Returns a new invertible function instance that is the inversion of
	 * another invertible function. That means the input parameter becomes the
	 * output parameter and vice versa.
	 *
	 * @param  rFunction The function to invert
	 *
	 * @return A new invertible function instance
	 */
	public static <I, O> InvertibleFunction<O, I> invert(
		final InvertibleFunction<I, O> rFunction)
	{
		return new Invert<O, I>(rFunction);
	}

	/***************************************
	 * A helper method that returns the function in a {@link FunctionChain} that
	 * will be invoked last. It is found by recursively traversing the tree of
	 * functions by means of the method {@link FunctionChain#getOuter()}.
	 *
	 * @param  rFunction The function to start searching at
	 *
	 * @return The first invoked function in a chain
	 *
	 * @see    #firstInChain(Function)
	 */
	public static Function<?, ?> lastInChain(Function<?, ?> rFunction)
	{
		if (rFunction instanceof FunctionChain<?, ?, ?>)
		{
			rFunction =
				lastInChain(((FunctionChain<?, ?, ?>) rFunction).getOuter());
		}

		return rFunction;
	}

	/***************************************
	 * A helper method that measures the execution time of a {@link Runnable}
	 * object.
	 *
	 * @param sDescription  The description of the measured code
	 * @param rProfiledCode The code to measure the execution time of
	 */
	@SuppressWarnings("boxing")
	public static void measure(String sDescription, Runnable rProfiledCode)
	{
		long t = System.currentTimeMillis();

		rProfiledCode.run();

		t = System.currentTimeMillis() - t;
		System.out.printf("[TIME] %s: %d.%03ds\n",
						  sDescription,
						  t / 1000,
						  t % 1000);
	}

	/***************************************
	 * Returns a new function instance that prints input values to a certain
	 * print writer without a trailing linefeed.
	 *
	 * @param  rOut    The writer to print input value to
	 * @param  sFormat The format string to be applied to input values with
	 *                 {@link String#format(String, Object...)}
	 *
	 * @return A new print function instance
	 */
	public static <T> BinaryFunction<T, PrintWriter, T> print(
		PrintWriter rOut,
		String		sFormat)
	{
		return new Print<T>(rOut, sFormat, false);
	}

	/***************************************
	 * Returns a type-safe variant of a static function instance that prints
	 * input values to System.out in a certain format with a trailing linefeed.
	 *
	 * @param  sFormat The format string to be applied to input values with
	 *                 {@link String#format(String, Object...)}
	 *
	 * @return A function to print to System.out with linefeed
	 */
	public static <T> Function<T, T> println(String sFormat)
	{
		return new Print<T>(sFormat);
	}

	/***************************************
	 * Returns a new function instance that prints input values to a certain
	 * print writer with a trailing linefeed.
	 *
	 * @param  rOut    The writer to print input value to
	 * @param  sFormat The format string to be applied to input values with
	 *                 {@link String#format(String, Object...)}
	 *
	 * @return A new print function instance
	 */
	public static <T> BinaryFunction<T, PrintWriter, T> println(
		PrintWriter rOut,
		String		sFormat)
	{
		return new Print<T>(rOut, sFormat, true);
	}

	/***************************************
	 * Returns a new instance of {@link ReadField}.
	 *
	 * @param  sFieldName The name of the field to return
	 *
	 * @return A new function instance
	 */
	public static <I, O> ReadField<I, O> readField(String sFieldName)
	{
		return new ReadField<I, O>(sFieldName);
	}

	/***************************************
	 * Returns a new instance of {@link SetRelationValue}.
	 *
	 * @param  rType  The type of the relation to read the value from
	 * @param  rValue The relation value to set
	 *
	 * @return A new function instance
	 */
	public static <T extends Relatable, V> BinaryFunction<T, V, T>
	setRelationValue(RelationType<V> rType, V rValue)
	{
		return new SetRelationValue<T, V>(rType, rValue);
	}

	/***************************************
	 * Returns a function that invokes {@link Thread#sleep(long)} with the time
	 * in milliseconds that it receives as the input value. The input value of
	 * the function will be returned unchanged to allow function chaining.
	 *
	 * @return A function constant that causes the current thread to sleep
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> Function<T, T> sleep()
	{
		return (Function<T, T>) THREAD_SLEEP;
	}

	/***************************************
	 * Returns a new binary function that swaps the left and right parameters of
	 * another binary function. This allows to use such functions as unary
	 * functions with a fixed left parameter instead of the right parameter
	 * without the need for a re-implementation.
	 *
	 * @param  rFunction      The original binary function
	 * @param  rNewRightValue The right value of the new binary function with
	 *                        the datatype of the left parameter of the original
	 *                        function
	 *
	 * @return The new binary function
	 */
	public static <L, R, O> BinaryFunction<R, L, O> swapParams(
		final BinaryFunction<L, R, O> rFunction,
		L							  rNewRightValue)
	{
		return new AbstractBinaryFunction<R, L, O>(rNewRightValue,
												   rFunction.getToken())
		{
			@Override
			public O evaluate(R rLeftValue, L rRightValue)
			{
				return rFunction.evaluate(rRightValue, rLeftValue);
			}

			@Override
			public String toString()
			{
				return getToken() + "(" + getRightValue() + ", " +
					   INPUT_PLACEHOLDER + ")";
			}
		};
	}

	/***************************************
	 * Returns an unchecked function that evaluates a function in a
	 * try-with-resource code block with an {@link AutoCloseable} resource that
	 * is created by another functions.
	 *
	 * @param  fOpenResource  An unchecked function that opens a resource
	 *                        derived from the function input
	 * @param  fProduceResult An unchecked function that creates the function
	 *                        result from the resource
	 *
	 * @return A new unchecked function instance
	 */
	public static <I, R extends AutoCloseable, O> Function<I, O> tryWith(
		ThrowingFunction<I, R> fOpenResource,
		ThrowingFunction<R, O> fProduceResult)
	{
		return unchecked(i ->
			 			{
			 				try (R rResource = fOpenResource.evaluate(i))
			 				{
			 					return fProduceResult.evaluate(rResource);
			 				}
						 });
	}

	/***************************************
	 * Takes a consumer that throws an exception and returns it as a consumer
	 * that can be executed without a checked exception. This method is mainly
	 * intended to be used for lambdas that throw exceptions.
	 *
	 * @param  fChecked The checked function to wrap as unchecked
	 *
	 * @return The unchecked function
	 */
	public static <T> Consumer<T> unchecked(ThrowingConsumer<T> fChecked)
	{
		return fChecked;
	}

	/***************************************
	 * Takes a supplier that throws an exception and returns it as a supplier
	 * that can be executed without a checked exception. This method is mainly
	 * intended to be used for lambdas that throw exceptions.
	 *
	 * @param  fChecked The checked function to wrap as unchecked
	 *
	 * @return The unchecked function
	 */
	public static <T> Supplier<T> unchecked(ThrowingSupplier<T> fChecked)
	{
		return fChecked;
	}

	/***************************************
	 * Takes a function that throws an exception and returns it as a function
	 * that can be executed without a checked exception. This method is mainly
	 * intended to be used for lambdas that throw exceptions.
	 *
	 * @param  fChecked The checked function to wrap as unchecked
	 *
	 * @return The unchecked function
	 */
	public static <I, O> Function<I, O> unchecked(
		ThrowingFunction<I, O> fChecked)
	{
		return fChecked;
	}

	/***************************************
	 * Returns a function that returns the current system time in the UNIX time
	 * format (i.e. seconds since January 1, 1970). This is the same value as
	 * the result of {@link System#currentTimeMillis()} divided by 1000.
	 *
	 * @return A function constant that returns the current UNIX timestamp
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<T, Integer> unixTimestamp()
	{
		return (Function<T, Integer>) UNIX_TIMESTAMP;
	}

	/***************************************
	 * Returns a new function instance that always returns a certain value. The
	 * input value will always be ignored but is identified by the generic type
	 * I so that a specific type can be inferred when assigning the returned
	 * function to a typed variable.
	 *
	 * @param  rValue The value to be returned by the function
	 *
	 * @return A new function instance
	 */
	public static <I, O> Function<I, O> value(final O rValue)
	{
		return new AbstractFunction<I, O>("value")
		{
			@Override
			public O evaluate(I rInput)
			{
				return rValue;
			}

			@Override
			public String toString()
			{
				return "value=" + rValue;
			}
		};
	}

	/***************************************
	 * Returns a function that invokes {@link Thread#yield()}. The input value
	 * of the function will be returned unchanged to allow function chaining.
	 *
	 * @return A function constant that causes the current thread to yield
	 *         execution to other threads
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<T, T> yield()
	{
		return (Function<T, T>) THREAD_YIELD;
	}
}
