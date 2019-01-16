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

	/***************************************
	 * Returns a new asynchronous promise for an value provided by an instance
	 * of {@link Supplier}. This is just a shortcut to invoke {@link
	 * CompletableFuture#supplyAsync(Supplier)} with the given supplier.
	 *
	 * @param  fSupplier The supplier of the value
	 *
	 * @return The new asynchronous promise
	 */
	public static <T> Promise<T> ofAsync(Supplier<T> fSupplier)
	{
		return Promise.of(CompletableFuture.supplyAsync(fSupplier));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Awaits for this promise to be resolved. This will block the current
	 * thread until the promised value becomes available. For a non-blocking use
	 * it is recommended to used the monadic mapping functions or {@link
	 * #then(Consumer)}.
	 *
	 * @return The promised value
	 *
	 * @throws RuntimeException If the waiting is interrupted
	 */
	public abstract T await();

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public abstract <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
		Function<T, N> fMap);

	/***************************************
	 * Checks whether this promise is already resolved. If TRUE retrieving the
	 * value with {@link #await()} will not block.
	 *
	 * @return The resolved
	 */
	public abstract boolean isResolved();

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
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format(
			"Promise[%s]",
			isResolved() ? await() : "unresolved");
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A promise implementation based on {@link CompletionStage}.
	 *
	 * @author eso
	 */
	static class CompletionStagePromise<T> extends Promise<T>
	{
		//~ Instance fields ----------------------------------------------------

		// wraps another promise to simplify the implementation of flatMap
		private CompletionStage<Promise<T>> rStage;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rStage The completion stage to wrap
		 */
		CompletionStagePromise(CompletionStage<Promise<T>> rStage)
		{
			this.rStage = rStage;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public T await()
		{
			return rStage.toCompletableFuture().join().await();
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
			Function<T, N> fMap)
		{
			return new CompletionStagePromise<>(
				rStage.thenApply(p -> p.flatMap(fMap)));
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public boolean isResolved()
		{
			return rStage.toCompletableFuture().isDone();
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
		public T await()
		{
			return rValue;
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
		public boolean isResolved()
		{
			return true;
		}
	}
}
