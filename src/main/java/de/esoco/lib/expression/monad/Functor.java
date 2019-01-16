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

import java.util.function.Consumer;
import java.util.function.Function;


/********************************************************************
 * Interface of functors that wrap values of type T and allow their mapping with
 * {@link #map(Function)}.
 *
 * @author eso
 */
public interface Functor<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Maps the wrapped value into a new functor instance.
	 *
	 * @param  fMap The value mapping function
	 *
	 * @return A mapped functor
	 */
	public <R> Functor<R> map(Function<? super T, ? extends R> fMap);

	/***************************************
	 * Consumes the value of this functor. This method is typically used at the
	 * end of a mapping chain for the final processing of the resulting value.
	 * The default implementation invokes {@link #map(Function)} with the
	 * argument consumer wrapped into a function. Some subclasses may be able to
	 * provide an optimized version. Furthermore subclasses should typically
	 * override this method with their own type as the return type.
	 *
	 * @param  fConsumer The consumer of the value
	 *
	 * @return The resulting functor for final chained invocations
	 */
	default public Functor<Void> then(Consumer<? super T> fConsumer)
	{
		return map(Functions.asFunction(fConsumer));
	}
}
