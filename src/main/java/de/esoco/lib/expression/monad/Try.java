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
 * A {@link Monad} implementation for the attempted execution of value-supplying
 * operations that may fail with an exception. Whether the execution was
 * successful can be tested with {@link #isSuccess()}.
 *
 * <p>Executions can be performed in two different ways: either immediately by
 * creating an instance with {@link #now(ThrowingSupplier)} or lazily with
 * {@link #lazy(ThrowingSupplier)}. In the first case the provided supplier will
 * be evaluated upon creation of the try instance. In the case of lazy
 * executions the supplier will only be evaluated when the Try is consumed by
 * invoking one of the corresponding methods like {@link #orUse(Object)} or
 * {@link #orFail()}.</p>
 *
 * <p>The supplier of a try, even of a lazy one, is always only evaluated once.
 * Each subsequent consumption will yield the same result. If a supplier should
 * be re-evaluated on each call the {@link Call} monad can be used instead.</p>
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
	 * Returns a new instance that will perform a lazy evaluation of the given
	 * supplier. That means the supplier will only be queried if one of the
	 * consuming methods like {@link #orUse(Object)} is invoked to access the
	 * result. {@link #map(Function) Mapping} or {@link #flatMap(Function) flat
	 * mapping} a lazy try will create another unresolved, lazy instance.
	 *
	 * <p>An instance that evaluates the supplier immediately can be created
	 * with {@link #now(ThrowingSupplier)}.</p>
	 *
	 * @param  fSupplier The throwing supplier to evaluate lazily
	 *
	 * @return The new instance
	 */
	public static <T> Try<T> lazy(ThrowingSupplier<T> fSupplier)
	{
		return new Lazy<>(fSupplier);
	}

	/***************************************
	 * Returns a new instance that represents the immediate execution of the
	 * given supplier. The returned instance either represents a successful
	 * execution or a failure if the execution failed.
	 *
	 * <p>An instance that evaluates the supplier lazily on the first access can
	 * be created with {@link #lazy(ThrowingSupplier)}.</p>
	 *
	 * @param  fSupplier rValue The value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Try<T> now(ThrowingSupplier<T> fSupplier)
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
		return Try.now(
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
		Function<? super T, N> fMap);

	/***************************************
	 * Checks whether the execution was successful.
	 *
	 * @return TRUE if this try was executed successfully, FALSE if an exception
	 *         occurred
	 */
	public abstract boolean isSuccess();

	/***************************************
	 * A consuming operation that consumes an error if the execution failed.
	 * This can be used to define the alternative of a call to a monadic
	 * function like {@link #map(Function)}, {@link #flatMap(Function)}, and
	 * especially {@link #then(Consumer)} to handle the case of a failed try.
	 *
	 * @param fHandler The consumer of the the error that occurred
	 */
	public abstract void orElse(Consumer<Throwable> fHandler);

	/***************************************
	 * A consuming operation that either returns the result of a successful
	 * execution or throws the occurred exception if the execution failed.
	 *
	 * @see #orThrow(Function)
	 */
	public abstract T orFail() throws Throwable;

	/***************************************
	 * A consuming operation that either returns the result of a successful
	 * execution or throws an exception if the execution failed. Success can be
	 * tested in advance with {@link #isSuccess()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process results but a call to a consuming operation should
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
	 * A consuming operation that either returns the result of a successful
	 * execution or returns the given default value if the execution failed. If
	 * necessary, success can be tested before with {@link #isSuccess()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process results but a call to a consuming operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  rDefault The default value to return if the execution failed
	 *
	 * @return The result value
	 */
	public abstract T orUse(T rDefault);

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
	 * A semantic alternative to {@link #then(Consumer)}.
	 *
	 * @param  fConsumer The consumer to invoke
	 *
	 * @return The resulting try for chained invocations
	 */
	public final Try<T> ifSuccessful(Consumer<? super T> fConsumer)
	{
		return then(fConsumer);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R> Try<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Try.now(() -> fMap.apply(t)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Try<T> then(Consumer<? super T> fConsumer)
	{
		return (Try<T>) Monad.super.then(fConsumer);
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

		private final Throwable eError;

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
			Function<? super T, N> fMap)
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
			Function<Throwable, E> fMapException) throws E
		{
			throw fMapException.apply(eError);
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
	 * The implementation of lazy tries with deferred evaluation.
	 *
	 * @author eso
	 */
	static class Lazy<T> extends Try<T>
	{
		//~ Instance fields ----------------------------------------------------

		private ThrowingSupplier<T> fValueSupplier;

		private Option<Try<T>> aResult = Option.none();

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param fValueSupplier rValue The successfully created value
		 */
		Lazy(ThrowingSupplier<T> fValueSupplier)
		{
			this.fValueSupplier = fValueSupplier;
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
					Objects.equals(
					fValueSupplier,
					((Lazy<?>) rObject).fValueSupplier));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R, N extends Monad<R, Try<?>>> Try<R> flatMap(
			Function<? super T, N> fMap)
		{
			return new Lazy<>(
				() -> ((Try<R>) fMap.apply(getResult().orFail())).orFail());
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			return Objects.hashCode(
				aResult.exists() ? aResult.orFail() : fValueSupplier);
		}

		/***************************************
		 * Testing for success needs to perform the lazy evaluation.
		 *
		 * @see Try#isSuccess()
		 */
		@Override
		public final boolean isSuccess()
		{
			return getResult().isSuccess();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R> Try<R> map(Function<? super T, ? extends R> fMap)
		{
			return new Lazy<>(() -> fMap.apply(getResult().orFail()));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Consumer<Throwable> fHandler)
		{
			getResult().orElse(fHandler);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final T orFail() throws Throwable
		{
			return getResult().orFail();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <E extends Throwable> T orThrow(
			Function<Throwable, E> fMapException) throws E
		{
			return getResult().orThrow(fMapException);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orUse(T rDefault)
		{
			return getResult().orUse(rDefault);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return String.format(
				"%s[%s]",
				getClass().getSimpleName(),
				aResult.exists() ? aResult.orFail() : fValueSupplier);
		}

		/***************************************
		 * Performs the lazy evaluation of this instance if not performed
		 * before.
		 *
		 * @return A try of the (evaluated) result
		 */
		private Try<T> getResult()
		{
			if (!aResult.exists())
			{
				aResult = Option.of(Try.now(fValueSupplier));
			}

			return aResult.orFail();
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

		private final T rValue;

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
			Function<? super T, N> fMap)
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
			Function<Throwable, E> fMapException) throws E
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orUse(T rDefault)
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
