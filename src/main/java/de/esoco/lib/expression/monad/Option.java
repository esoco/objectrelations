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
 * A {@link Monad} implementation for optional values.
 *
 * @author eso
 */
public class Option<T> implements Monad<T, Option<?>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Option<?> NONE = new Option<>(null);

	//~ Instance fields --------------------------------------------------------

	private T rValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rValue The value to wrap
	 */
	private Option(T rValue)
	{
		this.rValue = rValue;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a instance for an undefined value. It's {@link #exists()} method
	 * will always return FALSE.
	 *
	 * @return The nothing value
	 */
	@SuppressWarnings("unchecked")
	public static <T> Option<T> none()
	{
		return (Option<T>) NONE;
	}

	/***************************************
	 * Returns a new instance that wraps a certain value or {@link #none()} if
	 * the argument is NULL.
	 *
	 * @param  rValue The value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Option<T> of(T rValue)
	{
		return rValue != null ? new Option<>(rValue) : none();
	}

	/***************************************
	 * Converts a collection of options into either an existing option of a
	 * collection of values if all options in the collection exist or into
	 * {@link #none()} if one or more options in the collection do not exist.
	 *
	 * <p>Other than {@link #ofExisting(Stream)} this transformation cannot be
	 * performed on a stream (of possibly indefinite size) because existence
	 * needs to be determined upon invocation.</p>
	 *
	 * @param  rOptions The collection of options to convert
	 *
	 * @return A new option of a collection of the values of all options or
	 *         {@link #none()} if one or more options do not exist
	 */
	public static <T> Option<Collection<T>> ofAll(
		Collection<Option<T>> rOptions)
	{
		Optional<Option<T>> aMissing =
			rOptions.stream().filter(o -> !o.exists()).findFirst();

		return aMissing.isPresent()
			   ? none()
			   : Option.of(
			rOptions.stream().map(o -> o.orFail()).collect(toList()));
	}

	/***************************************
	 * Converts a stream of options into an option of a stream of existing
	 * values.
	 *
	 * @param  rOptions The stream to convert
	 *
	 * @return A new option containing a stream of existing values
	 */
	public static <T> Option<Stream<T>> ofExisting(Stream<Option<T>> rOptions)
	{
		return Option.of(rOptions.filter(Option::exists).map(o -> o.rValue));
	}

	/***************************************
	 * Returns a new instance with the same state as a Java {@link Optional}.
	 *
	 * @param  rOptional The input value
	 *
	 * @return The new instance
	 */
	public static <T> Option<T> ofOptional(Optional<T> rOptional)
	{
		return rOptional.isPresent() ? new Option<>(rOptional.get()) : none();
	}

	/***************************************
	 * Returns a new instance that wraps a certain value which must not be NULL.
	 *
	 * @param  rValue The required value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Option<T> ofRequired(T rValue)
	{
		Objects.requireNonNull(rValue);

		return Option.of(rValue);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V, R, N extends Monad<V, Option<?>>> Option<R> and(
		N											  rOther,
		BiFunction<? super T, ? super V, ? extends R> fJoin)
	{
		return (Option<R>) Monad.super.and(rOther, fJoin);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rObject)
	{
		return this == rObject ||
			   (rObject instanceof Option &&
				Objects.equals(rValue, ((Option<?>) rObject).rValue));
	}

	/***************************************
	 * Test whether this option represents an existing value.
	 *
	 * @return TRUE if this option exists, FALSE if it is undefined ({@link
	 *         #none()})
	 */
	public final boolean exists()
	{
		// test for NONE and not for rValue == NULL as then() needs to create
		// an Option<Void> with a NULL value
		return this != NONE;
	}

	/***************************************
	 * Filter this option according to the given criteria by returning an option
	 * that exists depending on whether the value fulfills the criteria.
	 *
	 * @param  pCriteria A predicate defining the filter criteria
	 *
	 * @return The resulting option
	 */
	@SuppressWarnings("unchecked")
	public Option<T> filter(Predicate<T> pCriteria)
	{
		return flatMap(v -> pCriteria.test(v) ? this : none());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R, N extends Monad<R, Option<?>>> Option<R> flatMap(
		Function<T, N> fMap)
	{
		return exists() ? (Option<R>) fMap.apply(rValue) : none();
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
	@SuppressWarnings("unchecked")
	public <R> Option<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Option.of(fMap.apply(t)));
	}

	/***************************************
	 * A consuming operation that executes some code if this option doesn't
	 * exist. This can be used to define the alternative of a call to a monadic
	 * function like {@link #map(Function)}, {@link #flatMap(Function)}, and
	 * especially {@link #then(Consumer)} to handle the case of an empty option.
	 *
	 * @param fAction The code to execute
	 */
	public void orElse(Runnable fAction)
	{
		if (!exists())
		{
			fAction.run();
		}
	}

	/***************************************
	 * A consuming operation that either returns the value of this option if it
	 * exists or throws a {@link NullPointerException}.
	 *
	 * @see #orThrow(Throwable)
	 */
	public <E extends Throwable> T orFail()
	{
		return orThrow(new NullPointerException());
	}

	/***************************************
	 * A consuming operation that either returns the value of this option or
	 * throws the given exception if the option doesn't exist. The presence of a
	 * value can be tested in advance with {@link #exists()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process values but a call to a consuming operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  eException The exception to throw
	 *
	 * @return The result of the execution
	 *
	 * @throws E The argument exception in the case of a failure
	 */
	public <E extends Throwable> T orThrow(E eException) throws E
	{
		if (!exists())
		{
			throw eException;
		}

		return rValue;
	}

	/***************************************
	 * A consuming operation that either returns an existing value or the given
	 * default value if the execution failed. If necessary, existence can be
	 * tested before with {@link #exists()}.
	 *
	 * <p>In general, calls to the monadic functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred to process values but a call to a consuming operation should
	 * typically appear at the end of a chain.</p>
	 *
	 * @param  rDefault The value to return if the value doesn't exist
	 *
	 * @return The result value
	 */
	public T orUse(T rDefault)
	{
		return exists() ? rValue : rDefault;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Option<Void> then(Consumer<? super T> fConsumer)
	{
		return exists()
			   ? flatMap(
			t ->
			{
				fConsumer.accept(t);

				// define an Option<Void> but don't return NONE
				return new Option<>(null);
			}) : none();
	}

	/***************************************
	 * Returns an {@link Optional} instance that represents this instance.
	 *
	 * @return The optional instance
	 */
	public Optional<T> toOptional()
	{
		return Optional.ofNullable(rValue);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return exists() ? rValue.toString() : "none";
	}
}
