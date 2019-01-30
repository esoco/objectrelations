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
package de.esoco.lib.expression.monad;

import de.esoco.lib.expression.Functions;
import de.esoco.lib.expression.function.ThrowingSupplier;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


/********************************************************************
 * A {@link Monad} implementation for the attempted execution of operations that
 * may fail with an exception. New instances are created through the factory
 * method like {@link #of(ThrowingSupplier)}.
 *
 * @author eso
 */
public abstract class Try<T> implements Monad<T, Try<?>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	Try()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns an instance that represents a failed execution.
	 *
	 * @param  eError The error exception
	 *
	 * @return The failure instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Try<T> failure(Throwable eError)
	{
		return new Failure<>(eError);
	}

	/***************************************
	 * Executes the given supplier and returns an instance that either
	 * represents a successful execution or a failure if the execution failed.
	 *
	 * @param  fSupplier rValue The value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Try<T> of(ThrowingSupplier<T> fSupplier)
	{
		try
		{
			return new Success<>(fSupplier.tryGet());
		}
		catch (Throwable e)
		{
			return new Failure<>(e);
		}
	}

	/***************************************
	 * Converts a collection of attempted executions into either a successful
	 * try of a collection of values if all tries in the collection were
	 * successful or into a failed try if one or more tries in the collection
	 * have failed. The error of the failure will be that of the first failed
	 * try.
	 *
	 * <p>Other than {@link #ofSuccessful(Stream)} this transformation cannot be
	 * performed on a stream (of possibly indefinite size) because success or
	 * failure needs to be determined upon invocation.</p>
	 *
	 * @param  rTries The collection of tries to convert
	 *
	 * @return A new successful try of a collection of the values of all tries
	 *         or a failure if one or more tries failed
	 */
	public static <T> Try<Collection<T>> ofAll(Collection<Try<T>> rTries)
	{
		Optional<Try<T>> aFailure =
			rTries.stream().filter(t -> !t.isSuccess()).findFirst();

		return aFailure.isPresent()
			   ? failure(((Failure<?>) aFailure.get()).eError)
			   : success(
			rTries.stream()
			.map(t -> t.orThrow(e -> new AssertionError(e)))
			.collect(toList()));
	}

	/***************************************
	 * Converts a stream of attempted executions into a try of a stream of
	 * successful values.
	 *
	 * @param  rTries The stream of tries to convert
	 *
	 * @return A new try that contains a stream of successful values
	 */
	public static <T> Try<Stream<T>> ofSuccessful(Stream<Try<T>> rTries)
	{
		return Try.of(
			() ->
				rTries.filter(Try::isSuccess)
				.map(t -> t.orThrow(e -> new AssertionError(e))));
	}

	/***************************************
	 * Returns an instance that represents a failed execution.
	 *
	 * @param  rValue eError The error exception
	 *
	 * @return The failure instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Try<T> success(T rValue)
	{
		return new Success<>(rValue);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public abstract <R, N extends Monad<R, Try<?>>> Try<R> flatMap(
		Function<T, N> fMap);

	/***************************************
	 * Checks whether the execution was successful.
	 *
	 * @return TRUE if this try was executed successfully, FALSE if an exception
	 *         occurred
	 */
	public abstract boolean isSuccess();

	/***************************************
	 * A terminal operation that consumes an error if the execution failed. This
	 * can be used to define the alternative of a call to a monadic function
	 * like {@link #map(Function)}, {@link #flatMap(Function)}, and especially
	 * {@link #then(Consumer)} to handle the case of a failed try.
	 *
	 * @param fHandler The consumer of the the error that occurred
	 */
	public abstract void orElse(Consumer<Throwable> fHandler);

	/***************************************
	 * A terminal operation that either returns the result of a successful
	 * execution or throws the occurred exception if the execution failed.
	 *
	 * @see #orThrow(Function)
	 */
	public abstract T orFail() throws Throwable;

	/***************************************
	 * A terminal operation that either returns the result of a successful
	 * execution or throws an exception if the execution failed. Success can be
	 * tested in advance with {@link #isSuccess()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process results but a call to a terminal operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  fMapException A function that maps the original exception
	 *
	 * @return The result of the execution
	 *
	 * @throws E The argument exception in the case of a failure
	 */
	public abstract <E extends Throwable> T orThrow(
		Function<Throwable, E> fMapException) throws E;

	/***************************************
	 * A terminal operation that either returns the result of a successful
	 * execution or returns the given default value if the execution failed. If
	 * necessary, success can be tested before with {@link #isSuccess()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process results but a call to a terminal operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  rFailureResult The value to return if the execution failed
	 *
	 * @return The result value
	 */
	public abstract T orUse(T rFailureResult);

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V, R, N extends Monad<V, Try<?>>> Try<R> and(
		N											  rOther,
		BiFunction<? super T, ? super V, ? extends R> fJoin)
	{
		return (Try<R>) Monad.super.and(rOther, fJoin);
	}

	/***************************************
	 * Filter this try according to the given criteria by returning a try that
	 * is successful if this try is successful and the wrapped value fulfills
	 * the criteria, or a failure otherwise.
	 *
	 * @param  pCriteria A predicate defining the filter criteria
	 *
	 * @return The resulting try
	 */
	@SuppressWarnings("unchecked")
	public Try<T> filter(Predicate<T> pCriteria)
	{
		return flatMap(
			v -> pCriteria.test(v)
				? success(v)
				: failure(new Exception("Criteria not met by " + v)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R> Try<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Try.of(() -> fMap.apply(t)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Try<Void> then(Consumer<? super T> fConsumer)
	{
		return map(Functions.asFunction(fConsumer));
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * The implementation of successful tries.
	 *
	 * @author eso
	 */
	static class Failure<T> extends Try<T>
	{
		//~ Instance fields ----------------------------------------------------

		private Throwable eError;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param eError rValue The successfully created value
		 */
		Failure(Throwable eError)
		{
			this.eError = eError;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object rObject)
		{
			return this == rObject ||
				   (rObject instanceof Failure &&
					Objects.equals(eError, ((Failure<?>) rObject).eError));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <R, N extends Monad<R, Try<?>>> Try<R> flatMap(
			Function<T, N> fMap)
		{
			return (Try<R>) this;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			return Objects.hashCode(eError);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final boolean isSuccess()
		{
			return false;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Consumer<Throwable> fHandler)
		{
			fHandler.accept(eError);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final T orFail() throws Throwable
		{
			throw eError;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <E extends Throwable> T orThrow(
			Function<Throwable, E> fCreateException) throws E
		{
			throw fCreateException.apply(eError);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orUse(T rFailureResult)
		{
			return rFailureResult;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "Failure[" + eError.getMessage() + "]";
		}
	}

	/********************************************************************
	 * The implementation of successful tries.
	 *
	 * @author eso
	 */
	static class Success<T> extends Try<T>
	{
		//~ Instance fields ----------------------------------------------------

		private T rValue;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rValue The successfully created value
		 */
		Success(T rValue)
		{
			this.rValue = rValue;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object rObject)
		{
			return this == rObject ||
				   (rObject instanceof Success &&
					Objects.equals(rValue, ((Success<?>) rObject).rValue));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R, N extends Monad<R, Try<?>>> Try<R> flatMap(
			Function<T, N> fMap)
		{
			return (Try<R>) fMap.apply(rValue);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			return Objects.hashCode(rValue);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final boolean isSuccess()
		{
			return true;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Consumer<Throwable> fHandler)
		{
			// ignored on success
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final T orFail()
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <E extends Throwable> T orThrow(
			Function<Throwable, E> fCreateException) throws E
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orUse(T rFailureResult)
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "Success[" + rValue + "]";
		}
	}
}
