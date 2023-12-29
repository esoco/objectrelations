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

/**
 * An implementation of {@link ThrowingSupplier} (which extends {@link
 * Supplier}) that caches values returned by other suppliers (whether throwing
 * or not) depending on a certain condition.
 *
 * @author eso
 */
public class CachingSupplier<T> implements ThrowingSupplier<T> {

	private final ThrowingSupplier<T> supplyValue;

	private final Predicate<? super T> checkInvalid;

	private T cachedValue = null;

	/**
	 * Creates a new instance.
	 *
	 * @param supplyValue  The value-providing supplier to wrap
	 * @param checkInvalid A predicate that determines whether the currently
	 *                     cached value is no longer valid and needs to be
	 *                     re-queried from the wrapped supplier
	 */
	private CachingSupplier(ThrowingSupplier<T> supplyValue,
		Predicate<? super T> checkInvalid) {
		this.supplyValue = supplyValue;
		this.checkInvalid = checkInvalid;
	}

	/**
	 * Factory method that returns a new {@link CachingSupplier} that caches
	 * the
	 * value returned by a supplier indefinitely. That means that after
	 * querying
	 * the wrapped supplier once it will never again be invoked. This can be
	 * modified by setting an expiration condition of the last acquired value
	 * with {@link #until(Predicate)}.
	 *
	 * @param supplyValue The value-providing supplier to wrap
	 * @return The new instance
	 */
	public static <T> CachingSupplier<T> cached(Supplier<T> supplyValue) {
		return new CachingSupplier<>(() -> supplyValue.get(),
			Predicates.alwaysFalse());
	}

	/**
	 * Factory method that returns a new {@link CachingSupplier} that caches
	 * the
	 * value returned by a exception-throwing supplier indefinitely. That means
	 * that after querying the wrapped supplier once it will never again be
	 * invoked. This can be modified by setting an expiration condition of the
	 * last acquired value with {@link #until(Predicate)}.
	 *
	 * @param supplyValue The value-providing supplier to wrap
	 * @return The new instance
	 */
	public static <T> CachingSupplier<T> cached(
		ThrowingSupplier<T> supplyValue) {
		return new CachingSupplier<>(supplyValue, Predicates.alwaysFalse());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T tryGet() throws Exception {
		if (cachedValue == null || checkInvalid.test(cachedValue)) {
			cachedValue = supplyValue.tryGet();
		}

		return cachedValue;
	}

	/**
	 * Returns a new instance that caches the value that is returned by the
	 * wrapped supplier until a certain condition is met.
	 *
	 * @param checkInvalid A predicate that determines whether the currently
	 *                     cached value is no longer valid and needs to be
	 *                     re-queried from the wrapped supplier
	 * @return The new instance
	 */
	public CachingSupplier<T> until(Predicate<? super T> checkInvalid) {
		return new CachingSupplier<>(supplyValue, checkInvalid);
	}
}
