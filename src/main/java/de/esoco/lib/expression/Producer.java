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
package de.esoco.lib.expression;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;


/********************************************************************
 * An interface for objects that produce values of a certain type that will be
 * handled by consumers that are registered through {@link
 * #registerReceiver(Consumer)}. This is similar to the Supplier interface, but
 * mainly intended for asynchronous use. In cases where a value is already
 * available the factory method {@link #of(Object)} can be used to wrap the
 * value in a producer and notify receivers immediately.
 *
 * <p>The most common producer implementations notify a single receiver of a
 * single value when it becomes available. But it is also possible to implement
 * producers that produce multiple values (or even an endless stream of such)
 * and/or allow the registration of multiple receivers. In such cases the
 * extended functionality should be documented accordingly.</p>
 *
 * @author eso
 */
@FunctionalInterface
public interface Producer<T>
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new producer that notifies registered receivers immediately of
	 * a certain value.
	 *
	 * @param  rValue The value to notify receivers of
	 *
	 * @return The new producer
	 */
	public static <T> Producer<T> of(T rValue)
	{
		return f -> f.accept(rValue);
	}

	/***************************************
	 * Returns a new producer that notifies registered receivers immediately of
	 * a value provided by a supplier.
	 *
	 * @param  fSupplier The supplier of the value to notify receivers of
	 *
	 * @return The new producer
	 */
	public static <T> Producer<T> of(Supplier<T> fSupplier)
	{
		return f -> f.accept(fSupplier.get());
	}

	/***************************************
	 * Returns a new producer that notifies registered receivers when a value
	 * from a {@link CompletionStage} (e.g. a {@link CompletableFuture}) becomes
	 * available.
	 *
	 * @param  rStage The completion stage to notify receivers of
	 *
	 * @return The new producer
	 */
	public static <T> Producer<T> of(CompletionStage<T> rStage)
	{
		return f -> rStage.thenAccept(f);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Registers a consumer that will receive a produced value when it becomes
	 * available. Whether multiple registrations are possible or not is not part
	 * of the method specification and only depends on the implementation.
	 * Callers should not assume so until it is documented otherwise.
	 *
	 * @param fReceiver The receiving consumer
	 */
	public void registerReceiver(Consumer<T> fReceiver);
}
