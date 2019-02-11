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
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;


/********************************************************************
 * A {@link Monad} implementation for deferred (lazy) function calls that will
 * either supply a value or fail with an exception. Other than value-based
 * monads like {@link Option} or {@link Try} which are evaluated only once upon
 * creation, the supplier wrapped by a call will be evaluated each time one of
 * the consuming methods {@link #orUse(Object)}, {@link #orFail()}, {@link
 * #orThrow(Function)}, or {@link #orElse(Consumer)} is invoked. If a call is
 * mapped with {@link #map(Function)} or {@link #flatMap(Function)} the
 * resulting call will also only be evaluated when a consuming method is
 * invoked.
 *
 * <p>Values are not cached, hence each evaluation will invoke the wrapped
 * supplier again. If caching is needed the supplier should either perform
 * caching by itself or could be wrapped with {@link
 * Functions#cached(java.util.function.Supplier)}.</p>
 *
 * @author eso
 */
public class Call<T> implements Monad<T, Call<?>>
{
	//~ Instance fields --------------------------------------------------------

	private ThrowingSupplier<T> fSupplier;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param fSupplier The value supplier
	 */
	private Call(ThrowingSupplier<T> fSupplier)
	{
		this.fSupplier = fSupplier;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns an always failing call.
	 *
	 * @param  eError The error exception
	 *
	 * @return the new instance
	 */
	public static <T> Call<T> error(Exception eError)
	{
		return new Call<>(() -> { throw eError; });
	}

	/***************************************
	 * Returns a new instance that wraps a certain supplier.
	 *
	 * @param  fSupplier The value supplier
	 *
	 * @return The new instance
	 */
	public static <T> Call<T> of(ThrowingSupplier<T> fSupplier)
	{
		return new Call<>(fSupplier);
	}

	/***************************************
	 * Converts a collection of calls into a call that supplies a collection of
	 * the values from all calls. As with single-value calls repeated consuming
	 * of the returned call will re-evaluate all suppliers from the converted
	 * calls.
	 *
	 * @param  rCalls The call collection to convert
	 *
	 * @return A new call that evaluates the suppliers of all calls and returns
	 *         a collection of the results
	 */
	public static <T> Call<Collection<T>> ofAll(Collection<Call<T>> rCalls)
	{
		List<ThrowingSupplier<T>> aSuppliers =
			rCalls.stream().map(c -> c.fSupplier).collect(toList());

		return new Call<>(
			() -> aSuppliers.stream().map(f -> f.get()).collect(toList()));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V, R, N extends Monad<V, Call<?>>> Call<R> and(
		N											  rOther,
		BiFunction<? super T, ? super V, ? extends R> fJoin)
	{
		return (Call<R>) Monad.super.and(rOther, fJoin);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObject)
	{
		return this == rObject ||
			   (rObject instanceof Call &&
				Objects.equals(fSupplier, ((Call<?>) rObject).fSupplier));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R, N extends Monad<R, Call<?>>> Call<R> flatMap(
		Function<? super T, N> fMap)
	{
		return Call.of(
			() -> ((Call<R>) fMap.apply(fSupplier.tryGet())).orFail());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return Objects.hashCode(fSupplier);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R> Call<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Call.of(() -> fMap.apply(t)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void orElse(Consumer<Throwable> fHandler)
	{
		Try.now(fSupplier).orElse(fHandler);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T orFail() throws Throwable
	{
		return Try.now(fSupplier).orFail();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Throwable> T orThrow(Function<Throwable, E> fMapException)
		throws E
	{
		return Try.now(fSupplier).orThrow(fMapException);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T orUse(T rDefault)
	{
		return Try.now(fSupplier).orUse(rDefault);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Call<T> then(Consumer<? super T> fConsumer)
	{
		return (Call<T>) Monad.super.then(fConsumer);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format("%s[%s]", getClass().getSimpleName(), fSupplier);
	}

	/***************************************
	 * Executes this call and returns a {@link Try} representing the result.
	 *
	 * @return The resulting try
	 */
	public Try<T> toTry()
	{
		return Try.now(fSupplier);
	}
}
