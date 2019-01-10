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

import java.util.function.BiFunction;
import java.util.function.Function;


/********************************************************************
 * Interface of {@link Functor} extensions that implement monads by providing a
 * {@link #flatMap(Function)} method to transform them into other monads.
 *
 * @author eso
 */
public interface Monad<T, M extends Monad<?, M>> extends Functor<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Maps this instance into another monad by applying a mapping function to
	 * the enclosed value that produces the new monad. Implementations should
	 * override the return type to their specific type because the Java type
	 * system provides no way to generically define the method return type
	 * needed for this case.
	 *
	 * @param  fMap The monad mapping function
	 *
	 * @return The mapped and flattened monad
	 */
	public <R, N extends Monad<R, M>> Monad<R, M> flatMap(Function<T, N> fMap);

	/***************************************
	 * Joins the value of this monad with that of another monad into a new monad
	 * of the same type. This is done by invoking {@link #flatMap(Function)} on
	 * this instance and "lifting" the other monad by invoking it's {@link
	 * #map(Function)} method. Therefore this type of method is sometimes also
	 * called "lift" or "liftM2".
	 *
	 * <p>Implementations can simply implement this as <code>flatMap(t -&gt;
	 * rOther.map(v -&gt; fJoin.apply(t, v)))</code>. The explicit
	 * implementation in subclasses is only necessary to allow them override the
	 * return type because of the limitations of Java's generic type system.</p>
	 *
	 * @param  rOther The other monad to join with
	 * @param  fJoin  The binary function that performs the value join
	 *
	 * @return The new joined monad
	 */
	@SuppressWarnings("unchecked")
	public <V, R, N extends Monad<V, M>> Monad<R, M> join(
		N					rOther,
		BiFunction<T, V, R> fJoin);

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public <R> Monad<R, M> map(Function<T, R> fMap);
}
