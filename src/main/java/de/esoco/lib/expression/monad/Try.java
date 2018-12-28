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
package de.esoco.lib.expression.monad;

import de.esoco.lib.expression.function.ThrowingSupplier;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


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
	 * Converts a stream of attempted executions into a try of creating a stream
	 * of successful values.
	 *
	 * @param  rTries The stream of tries to convert
	 *
	 * @return A new try that creates a stream of existing values
	 */
	public static <T> Try<Stream<T>> ofSuccessful(Stream<Try<T>> rTries)
	{
		return Try.of(
			() -> rTries.filter(Try::isSuccess).map(t -> t.getOrReturn(null)));
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
	 * Returns the result of a successful execution or returns a default if the
	 * execution failed. Success can be tested before with {@link #isSuccess()}.
	 * In general calls to the monadic chaining functions {@link
	 * #map(Function)}, {@link #flatMap(Function)}, or {@link #then(Consumer)}
	 * should be preferred as they prevent accidental access to a failed
	 * execution.
	 *
	 * @param  rFailureResult The value to return if the execution failed
	 *
	 * @return The result value
	 */
	public abstract T getOrReturn(T rFailureResult);

	/***************************************
	 * Returns the result of a successful execution or throws an exception if
	 * the execution failed. To prevent the exception success should be tested
	 * first with {@link #isSuccess()}. In general calls to the monadic chaining
	 * functions {@link #map(Function)}, {@link #flatMap(Function)}, or {@link
	 * #then(Consumer)} should be preferred as they prevent accidental access to
	 * a failed execution.
	 *
	 * @return The result of the execution
	 *
	 * @throws Throwable The exception that occurred in the case of a failure
	 */
	public abstract T getOrThrow() throws Throwable;

	/***************************************
	 * Checks whether the execution was successful.
	 *
	 * @return TRUE if this try was executed successfully, FALSE if an exception
	 *         occurred
	 */
	public abstract boolean isSuccess();

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public abstract <R> Try<R> map(Function<T, R> fMap);

	/***************************************
	 * Filter this try according to the given criteria by returning a try that
	 * is successful depending on whether the wrapped value fulfills the
	 * criteria.
	 *
	 * @param  pCriteria A predicate defining the filter criteria
	 *
	 * @return The resulting try
	 */
	@SuppressWarnings("unchecked")
	public Try<T> filter(Predicate<T> pCriteria)
	{
		return flatMap(
			v -> pCriteria.test(v) ? success(v)
								   : failure(new Exception("Criteria not met")));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <V, R, N extends Monad<V, Try<?>>> Try<R> join(
		N					rOther,
		BiFunction<T, V, R> fJoin)
	{
		return flatMap(t -> rOther.map(v -> fJoin.apply(t, v)));
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
		public T getOrReturn(T rFailureResult)
		{
			return rFailureResult;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final T getOrThrow() throws Throwable
		{
			throw eError;
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
		@SuppressWarnings("unchecked")
		public <R> Try<R> map(Function<T, R> fMap)
		{
			return (Try<R>) this;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void then(Consumer<T> fConsumer)
		{
			// ignored for errors
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
		public T getOrReturn(T rFailureResult)
		{
			return rValue;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public final T getOrThrow()
		{
			return rValue;
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
		@SuppressWarnings("unchecked")
		public <R> Try<R> map(Function<T, R> fMap)
		{
			return Try.of(() -> fMap.apply(rValue));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void then(Consumer<T> fConsumer)
		{
			fConsumer.accept(rValue);
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
