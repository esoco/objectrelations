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
import java.util.function.Supplier;
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
	 * A semantic alternative to {@link #ofRequired(Object)} that can be used as
	 * a static import.
	 *
	 * @param  rValue The value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Option<T> nonNull(T rValue)
	{
		return Option.ofRequired(rValue);
	}

	/***************************************
	 * Returns a new instance that wraps a certain value or {@link #none()} if
	 * the argument is NULL. To throw an exception if the value is NULL use
	 * {@link #ofRequired(Object)} instead.
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
	 *
	 * @throws NullPointerException If the given value is NULL
	 */
	public static <T> Option<T> ofRequired(T rValue)
	{
		Objects.requireNonNull(rValue);

		return Option.of(rValue);
	}

	/***************************************
	 * A semantic alternative to {@link #of(Object)} that can be used as a
	 * static import.
	 *
	 * @param  rValue The value to wrap
	 *
	 * @return The new instance
	 */
	public static <T> Option<T> option(T rValue)
	{
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
	 * Converts this option into another option by applying a mapping function
	 * that produces an option with the target type from the value of this
	 * instance.
	 *
	 * <p>Other than Java's {@link Optional} this implementation respects the
	 * monad laws of left and right identity as well as associativity. It does
	 * so by considering the not existing value {@link #none()} as equivalent to
	 * NULL. Therefore, if the mapping function is invoked on a non-existing
	 * option it will receive NULL as it's argument and should be able to handle
	 * it.</p>
	 *
	 * <p>If the mapping function throws a {@link NullPointerException} it will
	 * be caught and the returned option will be {@link #none()}. This allows to
	 * use functions that are not NULL-aware as an argument. It has the small
	 * limitation that NPEs caused by nested code will not be thrown.</p>
	 *
	 * @param  fMap The mapping function
	 *
	 * @return The resulting option
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R, N extends Monad<R, Option<?>>> Option<R> flatMap(
		Function<? super T, N> fMap)
	{
		try
		{
			return (Option<R>) fMap.apply(rValue);
		}
		catch (NullPointerException e)
		{
			return none();
		}
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
	 * A semantic alternative to {@link #then(Consumer)}.
	 *
	 * @param  fConsumer The consumer to invoke
	 *
	 * @return The resulting option for chained invocations
	 */
	public final Option<T> ifExists(Consumer<? super T> fConsumer)
	{
		return then(fConsumer);
	}

	/***************************************
	 * A convenience method for a fast test of options for existence and a
	 * certain value datatype. This is especially helpful for options that are
	 * declared with a generic or common type (like <code>Option&lt;?&gt;</code>
	 * or <code>Option&lt;Object&gt;</code>).
	 *
	 * @param  rDatatype The datatype to test the wrapped value against
	 *
	 * @return TRUE if this option exists and the value can be assigned to the
	 *         given datatype
	 */
	public final boolean is(Class<?> rDatatype)
	{
		return exists() && rDatatype.isAssignableFrom(rValue.getClass());
	}

	/***************************************
	 * Maps this option to another one containing the result of invoking a
	 * mapping function on this instance's value. Other than Java's {@link
	 * Optional} this implementation respects the monad laws. See the
	 * description of {@link #flatMap(Function)} for details.
	 *
	 * @param  fMap The mapping function
	 *
	 * @return The mapped option
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <R> Option<R> map(Function<? super T, ? extends R> fMap)
	{
		return flatMap(t -> Option.of(fMap.apply(t)));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void orElse(Consumer<Throwable> fHandler)
	{
		if (!exists())
		{
			fHandler.accept(new NullPointerException());
		}
	}

	/***************************************
	 * A variant of {@link #orElse(Consumer)} that simply executes some code if
	 * this option doesn't exist.
	 *
	 * @param fCode The code to execute
	 */
	public void orElse(Runnable fCode)
	{
		if (!exists())
		{
			fCode.run();
		}
	}

	/***************************************
	 * Throws a {@link NullPointerException} if this option does not exist.
	 *
	 * @see Functor#orFail()
	 */
	@Override
	public T orFail()
	{
		if (exists())
		{
			return rValue;
		}
		else
		{
			throw new NullPointerException();
		}
	}

	/***************************************
	 * Similar to the standard method {@link #orUse(Object)}, but with a
	 * supplier argument that can execute arbitrary code.
	 *
	 * @param  fSupplyDefault The supplier of the default value
	 *
	 * @return Either the existing value or the default provided by the supplier
	 */
	public T orGet(Supplier<T> fSupplyDefault)
	{
		return exists() ? rValue : fSupplyDefault.get();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Throwable> T orThrow(Function<Throwable, E> fMapException)
		throws E
	{
		if (exists())
		{
			return rValue;
		}
		else
		{
			throw fMapException.apply(new NullPointerException());
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T orUse(T rDefault)
	{
		return exists() ? rValue : rDefault;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Option<T> then(Consumer<? super T> fConsumer)
	{
		return exists() ? (Option<T>) Monad.super.then(fConsumer) : this;
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
		return exists() ? rValue.toString() : "[none]";
	}
}
