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
	public <R> Functor<R> map(Function<T, R> fMap);

	/***************************************
	 * Consumes the value of this functor. This method is typically used as the
	 * end of a mapping chain for the final processing of the resulting value.
	 *
	 * <p>The default implementation is based on {@link #map(Function)}. As this
	 * requires two unnecessary wrappings (consumer to function, result functor
	 * of map()) implementations are advised to provide an optimized version.
	 * Furthermore, implementations that wait for the production of the actual
	 * result value (like futures or promises) may need to override this method
	 * to first await the completion of the chain before consuming.</p>
	 *
	 * @param fConsumer The consumer of the value
	 */
	default void then(Consumer<T> fConsumer)
	{
		map(v -> Functions.asFunction(fConsumer).apply(v));
	}
}
