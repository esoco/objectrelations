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
import de.esoco.lib.expression.Producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/********************************************************************
 * A monad that promises to provide a value, typically in the future after some
 * background computation.
 *
 * @author eso
 */
public class Promise<T> implements Monad<T, Promise<?>>
{
	//~ Instance fields --------------------------------------------------------

	private Option<T>		    aResult			 = Option.none();
	private Option<Consumer<T>> aResolveListener = Option.none();

	private final CountDownLatch aCompletionSignal = new CountDownLatch(1);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	private Promise()
	{
	}

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
		return Promise.of(Producer.of(rValue));
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
	public static <T> Promise<T> of(Supplier<T> fSupplier)
	{
		return Promise.of(CompletableFuture.supplyAsync(fSupplier));
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
		return Promise.of(Producer.of(rStage));
	}

	/***************************************
	 * Returns a new (typically asynchronous) promise that will yield the value
	 * provided by a certain producer.
	 *
	 * @param  fProducer A producer that resolves the promise
	 *
	 * @return The new promise
	 */
	public static <T> Promise<T> of(Producer<T> fProducer)
	{
		Objects.requireNonNull(fProducer);

		Promise<T> aPromise = new Promise<>();

		fProducer.onProduced(aPromise::resolve);

		return aPromise;
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
		// list is needed to get the count which is needed to create a fully
		// asynchronous result promise
		List<Promise<T>> aPromises =
			rPromiseStream.collect(Collectors.toList());

		// list needs to be synchronized because the promises may run in
		// parallel in which aResult.add(t) will be invoked concurrently
		int     nCount  = aPromises.size();
		List<T> aResult = Collections.synchronizedList(new ArrayList<>(nCount));

		Producer<Stream<T>> aProducer =
			fReceiver ->
			{
				aPromises.forEach(
					p -> p.then(
							t ->
							{
								aResult.add(t);

								if (aResult.size() == nCount)
								{
									fReceiver.accept(aResult.stream());
								}
							}));
			};

		return Promise.of(aProducer);
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
	public T await()
	{
		try
		{
			aCompletionSignal.await();

			return aResult.get();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (!(rObject instanceof Promise))
		{
			return false;
		}

		Promise<?> rOther = (Promise<?>) rObject;

		return Objects.equals(aResolveListener, rOther.aResolveListener) &&
			   Objects.equals(aResult, rOther.aResult);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R, N extends Monad<R, Promise<?>>> Promise<R> flatMap(
		Function<T, N> fMap)
	{
		Promise<R> aFlattenedPromise = new Promise<>();

		Producer<N> aProducer =
			receiver -> this.then(t -> receiver.accept(fMap.apply(t)));

		// must be resolved indirectly because this.then() may be executed
		// asynchronously and is therefore not available at this point
		aProducer.onProduced(
			rPromise -> rPromise.then(aFlattenedPromise::resolve));

		return aFlattenedPromise;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return 17 * aResolveListener.hashCode() + aResult.hashCode();
	}

	/***************************************
	 * Checks whether this promise is already resolved. If TRUE retrieving the
	 * value with {@link #await()} will not block.
	 *
	 * @return The resolved
	 */
	public boolean isResolved()
	{
		return aResult.exists();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <V, R, N extends Monad<V, Promise<?>>> Promise<R> join(
		N					rOther,
		BiFunction<T, V, R> fJoin)
	{
		return flatMap(t -> rOther.map(v -> fJoin.apply(t, v)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R> Promise<R> map(Function<T, R> fMap)
	{
		return Promise.of(
			receiver -> onResolve(t -> receiver.accept(fMap.apply(t))));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Promise<Void> then(Consumer<T> fConsumer)
	{
		aResolveListener = Option.of(fConsumer);

		return map(Functions.asFunction(fConsumer));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return isResolved() ? aResult.get().toString()
							: getClass().getSimpleName();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	private void onResolve(Consumer<T> fConsumer)
	{
		if (isResolved())
		{
			fConsumer.accept(aResult.get());
		}
		else
		{
			aResolveListener = Option.of(fConsumer);
		}
	}

	/***************************************
	 * Resolves this promise with the given value.
	 *
	 * @param rValue The resolved value
	 */
	private void resolve(T rValue)
	{
		if (!aResult.exists())
		{
			aResult = Option.of(rValue);
			aCompletionSignal.countDown();
			aResolveListener.then(f -> f.accept(rValue));
		}
	}
}
