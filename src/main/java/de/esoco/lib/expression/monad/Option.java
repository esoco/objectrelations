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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


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
	 * @param rValue The Value to wrap
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
		return rValue != null;
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
	 * Returns the value of this option. This will yield NULL for an empty
	 * option which can be tested before with a call to {@link #exists()}. In
	 * general calls to the monadic chaining functions {@link #map(Function)},
	 * {@link #flatMap(Function)}, or {@link #then(Consumer)} should be
	 * preferred as they prevent accidental access to non-existing values.
	 *
	 * @return The value or NULL if it doesn't exist
	 */
	public final T get()
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
	public <V, R, N extends Monad<V, Option<?>>> Option<R> join(
		N					rOther,
		BiFunction<T, V, R> fJoin)
	{
		return flatMap(t -> rOther.map(v -> fJoin.apply(t, v)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R> Option<R> map(Function<T, R> fMap)
	{
		return exists() ? new Option<>(fMap.apply(rValue)) : none();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void then(Consumer<T> fConsumer)
	{
		if (exists())
		{
			fConsumer.accept(rValue);
		}
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
