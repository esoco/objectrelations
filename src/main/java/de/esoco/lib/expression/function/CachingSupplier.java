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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.ThrowingSupplier;

import java.util.function.Predicate;
import java.util.function.Supplier;


/********************************************************************
 * An implementation of {@link ThrowingSupplier} (which extends {@link
 * Supplier}) that caches values returned by other suppliers (whether throwing
 * or not) depending on a certain condition.
 *
 * @author eso
 */
public class CachingSupplier<T> implements ThrowingSupplier<T>
{
	//~ Instance fields --------------------------------------------------------

	private final ThrowingSupplier<T> fSupplyValue;

	private final Predicate<? super T> pCheckInvalid;

	private T rCachedValue = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param fSupplyValue  The value-providing supplier to wrap
	 * @param pCheckInvalid A predicate that determines whether the currently
	 *                      cached value is no longer valid and needs to be
	 *                      re-queried from the wrapped supplier
	 */
	private CachingSupplier(
		ThrowingSupplier<T>  fSupplyValue,
		Predicate<? super T> pCheckInvalid)
	{
		this.fSupplyValue  = fSupplyValue;
		this.pCheckInvalid = pCheckInvalid;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that returns a new {@link CachingSupplier} that caches the
	 * value returned by a supplier indefinitely. That means that after querying
	 * the wrapped supplier once it will never again be invoked. This can be
	 * modified by setting an expiration condition of the last acquired value
	 * with {@link #until(Predicate)}.
	 *
	 * @param  fSupplyValue The value-providing supplier to wrap
	 *
	 * @return The new instance
	 */
	public static <T> CachingSupplier<T> cached(Supplier<T> fSupplyValue)
	{
		return new CachingSupplier<>(
			() -> fSupplyValue.get(),
			Predicates.alwaysFalse());
	}

	/***************************************
	 * Factory method that returns a new {@link CachingSupplier} that caches the
	 * value returned by a exception-throwing supplier indefinitely. That means
	 * that after querying the wrapped supplier once it will never again be
	 * invoked. This can be modified by setting an expiration condition of the
	 * last acquired value with {@link #until(Predicate)}.
	 *
	 * @param  fSupplyValue The value-providing supplier to wrap
	 *
	 * @return The new instance
	 */
	public static <T> CachingSupplier<T> cached(
		ThrowingSupplier<T> fSupplyValue)
	{
		return new CachingSupplier<>(fSupplyValue, Predicates.alwaysFalse());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T tryGet() throws Exception
	{
		if (rCachedValue == null || pCheckInvalid.test(rCachedValue))
		{
			rCachedValue = fSupplyValue.tryGet();
		}

		return rCachedValue;
	}

	/***************************************
	 * Returns a new instance that caches the value that is returned by the
	 * wrapped supplier until a certain condition is met.
	 *
	 * @param  pCheckInvalid A predicate that determines whether the currently
	 *                       cached value is no longer valid and needs to be
	 *                       re-queried from the wrapped supplier
	 *
	 * @return The new instance
	 */
	public CachingSupplier<T> until(Predicate<? super T> pCheckInvalid)
	{
		return new CachingSupplier<>(fSupplyValue, pCheckInvalid);
	}
}
