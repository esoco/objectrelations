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

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.BinaryFunctionChain.LeftFunctionChain;
import de.esoco.lib.expression.function.BinaryFunctionChain.RightFunctionChain;
import de.esoco.lib.expression.function.BinaryFunctionGroup;
import de.esoco.lib.expression.function.CachingSupplier;
import de.esoco.lib.expression.function.ConditionalFunction;
import de.esoco.lib.expression.function.DualFunctionChain;
import de.esoco.lib.expression.function.FunctionChain;
import de.esoco.lib.expression.function.GetElement.GetField;
import de.esoco.lib.expression.function.GetElement.GetRelation;
import de.esoco.lib.expression.function.GetElement.GetRelationValue;
import de.esoco.lib.expression.function.GetElement.ReadField;
import de.esoco.lib.expression.function.Group;
import de.esoco.lib.expression.function.Print;
import de.esoco.lib.expression.function.SetElement.SetRelationValue;
import de.esoco.lib.expression.monad.Try;
import de.esoco.lib.reflect.ReflectUtil;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Contains factory methods to create standard functions.
 *
 * @author eso
 */
public class Functions {
	/**
	 * A runnable instance that does nothing.
	 */
	public static final Runnable NO_OPERATION = () -> {
	};

	/**
	 * Private, only static use.
	 */
	private Functions() {
	}

	/**
	 * Converts a {@link Function} into an {@link Action} that ignores the
	 * result value.
	 *
	 * @param function The function to convert
	 * @return A new action
	 */
	public static <T> Action<T> asAction(Function<T, ?> function) {
		return function::evaluate;
	}

	/**
	 * Converts a {@link java.util.function.Function} into a {@link Consumer}.
	 *
	 * @param function The function to convert
	 * @return The resulting consumer
	 */
	public static <T> Consumer<T> asConsumer(
		java.util.function.Function<T, ?> function) {
		return function::apply;
	}

	/**
	 * Converts a {@link Consumer} into a of
	 * {@link java.util.function.Function}
	 * that returns the input value.
	 *
	 * @param consumer The consumer to convert
	 * @return The resulting function
	 */
	public static <T> java.util.function.Function<T, T> asFunction(
		Consumer<T> consumer) {
		return v -> {
			consumer.accept(v);

			return v;
		};
	}

	/**
	 * Converts a {@link BiConsumer} into a of {@link BiFunction} that returns
	 * the first input value.
	 *
	 * @param consumer The consumer to convert
	 * @return The resulting function
	 */
	public static <T, U> BiFunction<T, U, T> asFunction(
		BiConsumer<T, U> consumer) {
		return (t, u) -> {
			consumer.accept(t, u);

			return t;
		};
	}

	/**
	 * Converts a {@link Supplier} into a of
	 * {@link java.util.function.Function}
	 * that ignores input values.
	 *
	 * @param supplier The suppler to convert
	 * @return The resulting function
	 */
	public static <I, O> java.util.function.Function<I, O> asFunction(
		Supplier<O> supplier) {
		return i -> supplier.get();
	}

	/**
	 * Converts a {@link Runnable} into a of
	 * {@link java.util.function.Function}
	 * that ignores input values and returns a Void result.
	 *
	 * @param runnable The runnable to convert
	 * @return The resulting function
	 */
	public static <T> java.util.function.Function<T, Void> asFunction(
		Runnable runnable) {
		return v -> {
			runnable.run();

			return null;
		};
	}

	/**
	 * Returns a function that invokes {@link Object#toString()} on the input
	 * value.
	 *
	 * @return A function constant that converts input objects to strings
	 */
	public static <T> Function<T, String> asString() {
		return v -> v != null ? v.toString() : "null";
	}

	/**
	 * @see CachingSupplier#cached(Supplier)
	 */
	public static <T> CachingSupplier<T> cached(Supplier<T> supplyValue) {
		return CachingSupplier.cached(supplyValue);
	}

	/**
	 * Chains two functions together. The output value that is created by
	 * evaluating an input value with the inner function will be evaluated by
	 * the outer function and the result will be returned. This is
	 * equivalent to
	 * {@code return outer.evaluate(inner.evaluate(inputValue))}. The generic
	 * parameter 'V' designates the intermediate value that is exchanged
	 * between
	 * the two functions.
	 *
	 * @param outer The outer function
	 * @param inner The inner function
	 * @return A new instance of {@link FunctionChain}
	 */
	public static <I, V, O> Function<I, O> chain(Function<V, O> outer,
		Function<I, ? extends V> inner) {
		return new FunctionChain<I, V, O>(outer, inner);
	}

	/**
	 * Chains a binary functions together with two other functions. The output
	 * values that are created by evaluating the left and right input values
	 * with the left and right functions will be evaluated by the binary outer
	 * function and the result will be returned. This is equivalent to
	 * {@code return outer.evaluate(left.evaluate(leftValue),
	 * right.evaluate(rightValue))}. The generic parameters 'V' and 'W'
	 * designate the intermediate values that are exchanged between the
	 * functions.
	 *
	 * @param outer The binary outer function
	 * @param left  The function to evaluate the left input value with
	 * @param right The function to evaluate the right input value with
	 * @return A new instance of {@link FunctionChain}
	 */
	public static <L, R, V, W, O> BinaryFunction<L, R, O> chain(
		BinaryFunction<V, W, O> outer, Function<L, ? extends V> left,
		Function<R, ? extends W> right) {
		return new DualFunctionChain<L, R, V, W, O>(outer, left, right);
	}

	/**
	 * Chains two binary functions together on their left value so that the
	 * left
	 * value of the outer function is generated by the inner function and the
	 * right value is forwarded unmodified.
	 *
	 * @param outer The outer function
	 * @param inner The inner function
	 * @return A new binary function chain
	 */
	public static <L, R, V, O> BinaryFunction<L, R, O> chainLeft(
		BinaryFunction<V, R, O> outer,
		BinaryFunction<L, R, ? extends V> inner) {
		return new LeftFunctionChain<>(outer, inner);
	}

	/**
	 * Chains two binary functions together on their right value so that the
	 * right value of the outer function is generated by the inner function and
	 * the left value is forwarded unmodified.
	 *
	 * @param outer The outer function
	 * @param inner The inner function
	 * @return A new binary function chain
	 */
	public static <L, R, V, O> BinaryFunction<L, R, O> chainRight(
		BinaryFunction<L, V, O> outer,
		BinaryFunction<L, R, ? extends V> inner) {
		return new RightFunctionChain<>(outer, inner);
	}

	/**
	 * Coerces a function instance to certain generic parameters. This method
	 * should be used with caution and only under special circumstances, e.g.
	 * for the parsing of functions without evaluation.
	 *
	 * @param function The function to coerce
	 * @return The function coerced to the given generic parameters
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> Function<I, O> coerce(Function<?, ?> function) {
		return (Function<I, O>) function;
	}

	/**
	 * Returns a new comparator instance that applies a function to input
	 * objects to determine the actual comparable values. NULL results of the
	 * compare function are supported.
	 *
	 * @param getComparable The function to be applied to input objects to
	 *                      obtain the comparable values
	 * @return A new {@link Comparator} instance
	 */
	public static <T, C extends Comparable<C>> Comparator<T> compare(
		final Function<? super T, C> getComparable) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				C c1 = getComparable.evaluate(o1);
				C c2 = getComparable.evaluate(o2);

				return c1 != null && c2 != null ?
				       c1.compareTo(c2) :
				       c1 == null && c2 == null ? 0 : c2 == null ? 1 : -1;
			}
		};
	}

	/**
	 * Returns a single function that applies several consumers successively to
	 * input values in the order in which they appear in the argument list. The
	 * return value of the resulting function will be the input value to
	 * support
	 * function chaining.
	 *
	 * @param first               The first consumer to apply to input values
	 * @param additionalFunctions Additional consumer to apply to input values
	 * @return A new function that evaluates all argument functions
	 */
	@SafeVarargs
	public static <I> Function<I, I> doAll(Consumer<? super I> first,
		Consumer<? super I>... additionalFunctions) {
		return Group.of(first, additionalFunctions);
	}

	/**
	 * Returns a single binary function that will apply several binary
	 * functions
	 * successively to left and right input values in the order in which they
	 * appear in the argument function list. The return value will be the
	 * left-side input value to support function chaining.
	 *
	 * @param rightValue The default right-side value for unary function
	 *                   invocations
	 * @param first      The first function to apply to input values
	 * @param second     The second function to apply to input values
	 * @param functions  The binary functions to apply to input values
	 * @return The unchanged left-side input value
	 */
	@SafeVarargs
	public static <L, R> BinaryFunction<L, R, L> doAll(R rightValue,
		BinaryFunction<? super L, ? super R, ?> first,
		BinaryFunction<? super L, ? super R, ?> second,
		BinaryFunction<? super L, ? super R, ?>... functions) {
		List<BinaryFunction<? super L, ? super R, ?>> functionList =
			new ArrayList<BinaryFunction<? super L, ? super R, ?>>();

		functionList.add(first);
		functionList.add(second);

		if (functions != null) {
			functionList.addAll(Arrays.asList(functions));
		}

		return new BinaryFunctionGroup<L, R>(rightValue, functionList);
	}

	/**
	 * Returns a new conditional function that will evaluate input values
	 * with a
	 * certain function if a predicate for this value yields TRUE. If the
	 * predicate yields FALSE no function will be evaluated. Conditional
	 * functions that also handle a predicate result of FALSE can be created
	 * either by invoking {@link #doIfElse(Predicate, Function, Function)}
	 * or by
	 * invoking {@link ConditionalFunction#elseDo(java.util.function.Function)}
	 * on the returned conditional function. The latter variant will create one
	 * intermediate function and should therefore not be used in time-critical
	 * code.
	 *
	 * @param predicate The predicate to evaluate
	 * @param function  The function to evaluate input values with if the
	 *                  predicate yields TRUE
	 * @return A new function for the conditional evaluation of a function
	 */
	public static <I, O> ConditionalFunction<I, O> doIf(
		Predicate<? super I> predicate, Function<? super I, O> function) {
		return new ConditionalFunction<>(predicate, function);
	}

	/**
	 * A variant of {@link #doIf(Predicate, Function)} for consumers.
	 *
	 * @see #doIf(Predicate, Function)
	 */
	public static <I> Consumer<I> doIf(Predicate<? super I> condition,
		Consumer<? super I> consumer) {
		return asConsumer(new ConditionalFunction<I, I>(condition, i -> {
			consumer.accept(i);

			return i;
		}));
	}

	/**
	 * Returns a new function that will evaluate an input value with a certain
	 * function if the result of a predicate for this value is TRUE. If the
	 * predicate is FALSE another function will be evaluated instead.
	 * Provides a
	 * more readable way to create instances of {@link ConditionalFunction}.
	 *
	 * @param predicate The predicate to evaluate
	 * @param onIf      The function to evaluate input values with if the
	 *                  predicate yields TRUE
	 * @param onElse    The function to evaluate input values with if the
	 *                  predicate yields FALSE
	 * @return A new function for the conditional evaluation of functions
	 */
	public static <I, O> Function<I, O> doIfElse(Predicate<? super I> predicate,
		Function<? super I, ? extends O> onIf,
		Function<? super I, ? extends O> onElse) {
		return new ConditionalFunction<>(predicate, onIf, onElse);
	}

	/**
	 * A variant of {@link #doIfElse(Predicate, Function, Function)} for
	 * consumers.
	 *
	 * @see #doIfElse(Predicate, Function, Function)
	 */
	public static <I> Consumer<I> doIfElse(Predicate<? super I> condition,
		Consumer<? super I> onIf, Consumer<? super I> onElse) {
		return asConsumer(new ConditionalFunction<I, I>(condition, i -> {
			onIf.accept(i);

			return i;
		}, i -> {
			onElse.accept(i);

			return i;
		}));
	}

	/**
	 * Returns a new binary function instance that throws a certain exception
	 * when it's invoked. The exception's error message will be created by
	 * invoking {@link String#format(String, Object...)} with the given message
	 * string and the function's input value as the arguments. As the method
	 * {@link Function#evaluate(Object)} cannot throw checked exceptions the
	 * created exception will be wrapped in an instance of the special runtime
	 * exception class {@link FunctionException}.
	 *
	 * @param message        The error message with optional format elements
	 * @param exceptionClass The class of the exception to throw
	 * @return A new function instance
	 */
	public static <I, O> BinaryFunction<I, String, O> error(String message,
		final Class<? extends Exception> exceptionClass) {
		return new AbstractBinaryFunction<I, String, O>(message, "error") {
			final Constructor<? extends Exception> constructor =
				ReflectUtil.getPublicConstructor(exceptionClass, String.class);

			@Override
			public O evaluate(I value, String message) {
				Exception error;

				message = String.format(message, value);

				try {
					error = constructor.newInstance(message);
				} catch (Exception e) {
					throw new IllegalStateException(
						"Could not create " + exceptionClass, e);
				}

				if (error instanceof RuntimeException) {
					throw (RuntimeException) error;
				} else {
					throw new FunctionException(this, error);
				}
			}
		};
	}

	/**
	 * A helper method that returns the function in a {@link FunctionChain}
	 * that
	 * will be invoked first. It is found by recursively traversing the tree of
	 * functions by means of the method {@link FunctionChain#getInner()}.
	 *
	 * @param function The function to start searching at
	 * @return The first invoked function in a chain
	 * @see #lastInChain(Function)
	 */
	public static Function<?, ?> firstInChain(Function<?, ?> function) {
		if (function instanceof FunctionChain<?, ?, ?>) {
			function =
				firstInChain(((FunctionChain<?, ?, ?>) function).getInner());
		}

		return function;
	}

	/**
	 * Returns a new instance of {@link GetField}.
	 *
	 * @param field The name of the field to get
	 * @return A new function instance
	 */
	public static <I, O> GetField<I, O> getField(String field) {
		return new GetField<I, O>(field);
	}

	/**
	 * Returns a new instance of {@link GetRelation}.
	 *
	 * @param type The type of the relation to return
	 * @return A new function instance
	 */
	public static <I extends Relatable, O> Function<I, Relation<O>> getRelation(
		RelationType<O> type) {
		return new GetRelation<I, O>(type);
	}

	/**
	 * Returns a new instance of {@link GetRelationValue}.
	 *
	 * @param type The type of the relation value to return
	 * @return A new function instance
	 */
	public static <I extends Relatable, O> Function<I, O> getRelationValue(
		RelationType<O> type) {
		return new GetRelationValue<I, O>(type);
	}

	/**
	 * Returns an invertible identity function that returns the input value.
	 *
	 * @return A constant identity function
	 */
	public static <T> InvertibleFunction<T, T> identity() {
		return new Identity<>();
	}

	/**
	 * A convenience method to create a new {@link #error(String, Class)}
	 * function that throws an {@link IllegalArgumentException}.
	 *
	 * @see #error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> illegalArgument(
		String message) {
		return error(message, IllegalArgumentException.class);
	}

	/**
	 * A convenience method to create a new {@link #error(String, Class)}
	 * function that throws an {@link IllegalStateException}.
	 *
	 * @see #error(String, Class)
	 */
	public static <I, O> BinaryFunction<I, String, O> illegalState(
		String message) {
		return error(message, IllegalStateException.class);
	}

	/**
	 * Returns a new invertible function instance that is the inversion of
	 * another invertible function. That means the input parameter becomes the
	 * output parameter and vice versa.
	 *
	 * @param toInvert The function to invert
	 * @return A new invertible function instance
	 */
	public static <I, O> InvertibleFunction<O, I> invert(
		final InvertibleFunction<I, O> toInvert) {
		return InvertibleFunction.of(toInvert::invert, toInvert::evaluate);
	}

	/**
	 * A helper method that returns the function in a {@link FunctionChain}
	 * that
	 * will be invoked last. It is found by recursively traversing the tree of
	 * functions by means of the method {@link FunctionChain#getOuter()}.
	 *
	 * @param function The function to start searching at
	 * @return The first invoked function in a chain
	 * @see #firstInChain(Function)
	 */
	public static Function<?, ?> lastInChain(Function<?, ?> function) {
		if (function instanceof FunctionChain<?, ?, ?>) {
			function =
				lastInChain(((FunctionChain<?, ?, ?>) function).getOuter());
		}

		return function;
	}

	/**
	 * A helper method that measures the execution time of a {@link Runnable}
	 * object.
	 *
	 * @param description  The description of the measured code
	 * @param profiledCode The code to measure the execution time of
	 */
	public static void measure(String description, Runnable profiledCode) {
		long t = System.currentTimeMillis();

		profiledCode.run();

		t = System.currentTimeMillis() - t;
		System.out.printf("[TIME] %s: %d.%03ds\n", description, t / 1000,
			t % 1000);
	}

	/**
	 * Returns a new function instance that prints input values to a certain
	 * print writer without a trailing linefeed.
	 *
	 * @param out    The writer to print input value to
	 * @param format The format string to be applied to input values with
	 *               {@link String#format(String, Object...)}
	 * @return A new print function instance
	 */
	public static <T> BinaryFunction<T, PrintWriter, T> print(PrintWriter out,
		String format) {
		return new Print<T>(out, format, false);
	}

	/**
	 * Returns a type-safe variant of a static function instance that prints
	 * input values to System.out in a certain format with a trailing linefeed.
	 *
	 * @param format The format string to be applied to input values with
	 *               {@link String#format(String, Object...)}
	 * @return A function to print to System.out with linefeed
	 */
	public static <T> Function<T, T> println(String format) {
		return new Print<T>(format);
	}

	/**
	 * Returns a new function instance that prints input values to a certain
	 * print writer with a trailing linefeed.
	 *
	 * @param out    The writer to print input value to
	 * @param format The format string to be applied to input values with
	 *               {@link String#format(String, Object...)}
	 * @return A new print function instance
	 */
	public static <T> BinaryFunction<T, PrintWriter, T> println(PrintWriter out,
		String format) {
		return new Print<T>(out, format, true);
	}

	/**
	 * Returns a new instance of {@link ReadField}.
	 *
	 * @param fieldName The name of the field to return
	 * @return A new function instance
	 */
	public static <I, O> ReadField<I, O> readField(String fieldName) {
		return new ReadField<I, O>(fieldName);
	}

	/**
	 * Returns a new instance of {@link SetRelationValue}.
	 *
	 * @param type  The type of the relation to read the value from
	 * @param value The relation value to set
	 * @return A new function instance
	 */
	public static <T extends Relatable, V> BinaryFunction<T, V, T> setRelationValue(
		RelationType<V> type, V value) {
		return new SetRelationValue<T, V>(type, value);
	}

	/**
	 * Returns a consumer that invokes {@link Thread#sleep(long)} with the time
	 * in milliseconds that it receives as the input value. An interruption
	 * exception that occurs during the sleep will be ignored.
	 *
	 * @return A function constant that causes the current thread to sleep
	 */
	public static Consumer<Long> sleep() {
		return time -> Try.run(() -> Thread.sleep(time)).orUse(null);
	}

	/**
	 * Returns a new binary function that swaps the left and right
	 * parameters of
	 * another binary function. This allows to use such functions as unary
	 * functions with a fixed left parameter instead of the right parameter
	 * without the need for a re-implementation.
	 *
	 * @param function      The original binary function
	 * @param newRightValue The right value of the new binary function with the
	 *                      datatype of the left parameter of the original
	 *                      function
	 * @return The new binary function
	 */
	public static <L, R, O> BinaryFunction<R, L, O> swapParams(
		final BinaryFunction<L, R, O> function, L newRightValue) {
		return new AbstractBinaryFunction<R, L, O>(newRightValue,
			function.getToken()) {
			@Override
			public O evaluate(R leftValue, L rightValue) {
				return function.evaluate(rightValue, leftValue);
			}

			@Override
			public String toString() {
				return getToken() + "(" + getRightValue() + ", " +
					INPUT_PLACEHOLDER + ")";
			}
		};
	}

	/**
	 * Returns a function that returns the current system time in the UNIX time
	 * format (i.e. seconds since January 1, 1970). This is the same value as
	 * the result of {@link System#currentTimeMillis()} divided by 1000.
	 *
	 * @return A function constant that returns the current UNIX timestamp
	 */
	public static Supplier<Long> unixTimestamp() {
		return () -> System.currentTimeMillis() / 1000;
	}

	/**
	 * Returns a new function instance that always returns a certain value. The
	 * input value will always be ignored but is identified by the generic type
	 * I so that a specific type can be inferred when assigning the returned
	 * function to a typed variable.
	 *
	 * @param value The value to be returned by the function
	 * @return A new function instance
	 */
	public static <I, O> Function<I, O> value(final O value) {
		return new AbstractFunction<I, O>("value") {
			@Override
			public O evaluate(I input) {
				return value;
			}

			@Override
			public String toString() {
				return "value=" + value;
			}
		};
	}

	public static class Identity<T> extends AbstractFunction<T, T>
		implements InvertibleFunction<T, T> {

		public Identity() {
			super("Identity");
		}

		@Override
		public T evaluate(T value) {
			return value;
		}

		@Override
		public T invert(T value) {
			return value;
		}
	}
}
