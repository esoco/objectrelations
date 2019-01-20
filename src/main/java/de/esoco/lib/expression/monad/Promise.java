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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/********************************************************************
 * A monad that promises to provide a value, typically asynchronously after some
 * background computation.
 *
 * @author eso
 */
public abstract class Promise<T> implements Monad<T, Promise<?>>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the possible states a promise can have. The state ACTIVE
	 * designates a promise that still performs an asynchronous operation, while
	 * all other states are set on completed promises.
	 */
	public enum State { ACTIVE, RESOLVED, CANCELLED, FAILED }

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new promise with an already resolved value.
	 *
	 * @param  rValue The resolved value
	 *
	 * @return The already resolved promise
	 */
	public static <T> Promise<T> of(T rValue)
	{
		return new ResolvedPromise<T>(rValue);
	}

	/***************************************
	 * Returns a new asynchronous promise for an value provided by a {@link
	 * CompletionStage} (e.g. a {@link CompletableFuture}).
	 *
	 * @param  rStage The completion stage that provides the value
	 *
	 * @return The new asynchronous promise
	 */
	public static <T> Promise<T> of(CompletionStage<T> rStage)
	{
		return new CompletionStagePromise<T>(rStage.thenApply(Promise::of));
	}

	/***************************************
	 * Returns a new <b>asynchronous</b> promise for an value provided by an
	 * instance of {@link Supplier}. This is just a shortcut to invoke {@link
	 * CompletableFuture#supplyAsync(Supplier)} with the given supplier.
	 *
	 * @param  fSupplier The supplier of the value
	 *
	 * @return The new asynchronous promise
	 */
	public static <T> Promise<T> of(Supplier<T> fSupplier)
	{
		return Promise.of(CompletableFuture.supplyAsync(fSupplier));
	}

	/***************************************
	 * Converts a stream of promises into a promise of a stream of resolved
	 * values. The returned promise will only complete after all promises in the
	 * input stream have been resolved.
	 *
	 * @param  rPromiseStream The stream to convert
	 *
	 * @return A new promise containing a stream of resolved values
	 */
	public static <T> Promise<Stream<T>> ofAll(
		Stream<Promise<T>> rPromiseStream)
	{
		// collect necessary to get the count which is needed to create a fully
		// asynchronous result promise
		List<Promise<T>> aPromises =
			rPromiseStream.collect(Collectors.toList());

		// list needs to be synchronized because the promises may run in
		// parallel in which aResult.add(t) will be invoked concurrently
		int     nCount  = aPromises.size();
		List<T> aResult = Collections.synchronizedList(new ArrayList<>(nCount));

		CompletableFuture<Stream<T>> aStage = new CompletableFuture<>();

		aPromises.forEach(
			rPromise ->
				rPromise.then(
					v ->
					{
						aResult.add(v);

						if (aResult.size() == nCount)
						{
							aStage.complete(aResult.stream());
						}
					}));

		return Promise.of(aStage);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Cancels the asynchronous execution of this promise.
	 *
	 * @return TRUE if this promise has been cancelled by the call, FALSE if it
	 *         had already been resolved or cancelled before or if the execution
	 *         has failed
	 */
	public abstract boolean cancel();

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public abstract <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
		Function<T, N> fMap);

	/***************************************
	 * Returns the current state of this promise. Due to the asynchronous nature
	 * of promises a returned state of {@link State#ACTIVE} is only a momentary
	 * value that may not be valid anymore after this method returns. In
	 * general, it is recommended to prefer the monadic functions to operate on
	 * the completion of promises.
	 *
	 * @return The current state of this promise
	 */
	public abstract State getState();

	/***************************************
	 * Defines the maximum time this promise may run before failing as
	 * unresolved.
	 *
	 * @param  nTime The time value
	 * @param  eUnit The time unit
	 *
	 * @return The resulting promise
	 */
	public abstract Promise<T> maxTime(long nTime, TimeUnit eUnit);

	/***************************************
	 * Returns a new promise that consumes the error of a failed promise. This
	 * method is non-blocking and will typically execute the given function
	 * asynchronously when the promise terminates because of an exception.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process values but a call to a terminal operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  fHandler The consumer of the the error that occurred
	 *
	 * @return The resulting promise
	 */
	public abstract Promise<T> onError(Consumer<Throwable> fHandler);

	/***************************************
	 * A terminal, blocking operation that consumes an error if the execution
	 * failed. This can be used to define the alternative of a call to a monadic
	 * function like {@link #map(Function)}, {@link #flatMap(Function)}, and
	 * especially {@link #then(Consumer)} to handle the case of a failed try.
	 *
	 * @param fHandler The consumer of the the error that occurred
	 */
	public abstract void orElse(Consumer<Throwable> fHandler);

	/***************************************
	 * A terminal, blocking operation that either returns the value of a
	 * resolved promise execution or throws the occurred exception if the
	 * promise failed.
	 *
	 * @see #orThrow(Throwable)
	 */
	public abstract T orFail() throws Throwable;

	/***************************************
	 * A terminal, blocking operation that either returns the value of a
	 * resolved execution or throws an exception if the promise failed. Success
	 * can be tested in advance with {@link #isSuccess()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process results but a call to a terminal operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  fCreateException A function that creates the exception to throw
	 *
	 * @return The result of the execution
	 *
	 * @throws E The argument exception in the case of a failure
	 */
	public abstract <E extends Throwable> T orThrow(
		Function<Throwable, E> fCreateException) throws E;

	/***************************************
	 * A terminal, blocking operation that either returns the result of a
	 * successful execution or returns the given default value if the execution
	 * failed. If necessary, success can be tested before with {@link
	 * #isSuccess()}.
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
	 * Checks whether this promise has been successfully resolved. If it returns
	 * TRUE accessing the resolved value with the terminal methods like {@link
	 * #orUse()}, {@link #orFail()}, {@link #orThrow(Function)}, or {@link
	 * #orElse(Consumer)} will not block and yield a valid result. This is just
	 * a shortcut for testing the state with <code>getState() ==
	 * RESOLVED</code>.
	 *
	 * @return TRUE if this promise
	 */
	public final boolean isResolved()
	{
		return getState() == State.RESOLVED;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <V, R, N extends Monad<V, Promise<?>>> Promise<R> join(
		N											  rOther,
		BiFunction<? super T, ? super V, ? extends R> fJoin)
	{
		return flatMap(t -> rOther.map(v -> fJoin.apply(t, v)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R> Promise<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Promise.of(fMap.apply(t)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Promise<Void> then(Consumer<? super T> fConsumer)
	{
		return map(Functions.asFunction(fConsumer));
	}

	/***************************************
	 * Returns an implementation of {@link Future} that is based on this
	 * promise.
	 *
	 * @return A future representing this promise
	 */
	public Future<T> toFuture()
	{
		return new PromiseFuture<>(this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format(
			"Promise[%s]",
			isResolved() ? orUse(null) : "unresolved");
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A future implementation that wraps a promise.
	 *
	 * @author eso
	 */
	public static class PromiseFuture<T> implements Future<T>
	{
		//~ Instance fields ----------------------------------------------------

		private final Promise<T> rPromise;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance that wraps a certain promise.
		 *
		 * @param rPromise The promise to wrap
		 */
		public PromiseFuture(Promise<T> rPromise)
		{
			this.rPromise = rPromise;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean cancel(boolean bMayInterruptIfRunning)
		{
			return rPromise.cancel();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			return getImpl(rPromise);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T get(long nTimeout, TimeUnit eUnit) throws InterruptedException,
														   ExecutionException,
														   TimeoutException
		{
			return getImpl(rPromise.maxTime(nTimeout, eUnit));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean isCancelled()
		{
			return rPromise.getState() == State.CANCELLED;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean isDone()
		{
			return rPromise.getState() != State.ACTIVE;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		private T getImpl(Promise<T> rPromise) throws InterruptedException,
													  ExecutionException
		{
			try
			{
				return rPromise.orFail();
			}
			catch (RuntimeException |
				   InterruptedException |
				   ExecutionException e)
			{
				throw e;
			}
			catch (Throwable e)
			{
				throw new ExecutionException(e);
			}
		}
	}

	/********************************************************************
	 * A promise implementation that wraps a {@link CompletionStage}. Final
	 * operations like {@link #await()} or {@link #isResolved()} will invoke the
	 * {@link CompletionStage#toCompletableFuture()} method and are therefore
	 * only compatible with the corresponding implementations (like {@link
	 * CompletableFuture} itself). But the monadic methods {@link
	 * #flatMap(Function)}, {@link #map(Function)}, and {@link #then(Consumer)}
	 * can be applied to any {@link CompletionStage} implementation.
	 *
	 * @author eso
	 */
	static class CompletionStagePromise<T> extends Promise<T>
	{
		//~ Instance fields ----------------------------------------------------

		// wraps another promise to simplify the implementation of flatMap
		private CompletionStage<Promise<T>> rStage;
		private long					    nMaxTime;
		private TimeUnit				    eTimeUnit;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rStage The completion stage to wrap
		 */
		CompletionStagePromise(CompletionStage<Promise<T>> rStage)
		{
			this(rStage, -1, null);
		}

		/***************************************
		 * Creates a new instance with a timeout.
		 *
		 * @param rStage    The completion stage to wrap
		 * @param nMaxTime  The maximum time to wait for resolving
		 * @param eTimeUnit The unit of the timeout value
		 */
		CompletionStagePromise(CompletionStage<Promise<T>> rStage,
							   long						   nMaxTime,
							   TimeUnit					   eTimeUnit)
		{
			this.rStage    = rStage;
			this.nMaxTime  = nMaxTime;
			this.eTimeUnit = eTimeUnit;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean cancel()
		{
			return rStage.toCompletableFuture().cancel(false);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
			Function<T, N> fMap)
		{
			return new CompletionStagePromise<>(
				rStage.thenApplyAsync(p -> p.flatMap(fMap)));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public State getState()
		{
			CompletableFuture<Promise<T>> rFuture =
				rStage.toCompletableFuture();

			if (rFuture.isDone())
			{
				if (rFuture.isCompletedExceptionally())
				{
					if (rFuture.isCancelled())
					{
						return State.CANCELLED;
					}
					else
					{
						return State.FAILED;
					}
				}
				else
				{
					return State.RESOLVED;
				}
			}
			else
			{
				return State.ACTIVE;
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Promise<T> maxTime(long nTime, TimeUnit eUnit)
		{
			return new CompletionStagePromise<>(rStage, nTime, eUnit);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Promise<T> onError(Consumer<Throwable> fHandler)
		{
			return new CompletionStagePromise<>(
				rStage.whenCompleteAsync(
					(t, e) ->
				{
					if (e != null)
					{
						fHandler.accept(e);
					}
				}));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Consumer<Throwable> fHandler)
		{
			try
			{
				resolveWithTimeout();
			}
			catch (Exception e)
			{
				fHandler.accept(e);
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orFail() throws Throwable
		{
			try
			{
				return resolveWithTimeout().orFail();
			}
			catch (Exception e)
			{
				throw e;
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <E extends Throwable> T orThrow(
			Function<Throwable, E> fCreateException) throws E
		{
			try
			{
				return resolveWithTimeout().orThrow(fCreateException);
			}
			catch (Exception e)
			{
				throw fCreateException.apply(e);
			}
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orUse(T rFailureResult)
		{
			try
			{
				return resolveWithTimeout().orUse(rFailureResult);
			}
			catch (Exception e)
			{
				return rFailureResult;
			}
		}

		/***************************************
		 * Blocks until this promise is resolved, respecting a possible timeout.
		 *
		 * @return The resolved value
		 *
		 * @throws Exception If the promise execution failed or a timeout has
		 *                   been reached
		 */
		private Promise<T> resolveWithTimeout() throws Exception
		{
			return nMaxTime == -1
				   ? rStage.toCompletableFuture().get()
				   : rStage.toCompletableFuture().get(nMaxTime, eTimeUnit);
		}
	}

	/********************************************************************
	 * A simple wrapper for an already resolved value.
	 *
	 * @author eso
	 */
	static class ResolvedPromise<T> extends Promise<T>
	{
		//~ Instance fields ----------------------------------------------------

		private T rValue;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates an instance that is already resolved with a certain value.
		 *
		 * @param rValue The resolved value
		 */
		public ResolvedPromise(T rValue)
		{
			this.rValue = rValue;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean cancel()
		{
			return false;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
			Function<T, N> fMap)
		{
			return (Promise<R>) fMap.apply(rValue);
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public State getState()
		{
			return State.RESOLVED;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Promise<T> maxTime(long nTime, TimeUnit eUnit)
		{
			return this;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public Promise<T> onError(Consumer<Throwable> fHandler)
		{
			return this;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Consumer<Throwable> fHandler)
		{
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T orFail() throws Throwable
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
	}
}
