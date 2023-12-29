//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.predicate;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.FunctionChain;

/**
 * A subclass of {@link FunctionChain} for predicates that also implements the
 * predicate interface. This allows to create a new predicate instance from the
 * chaining of a predicate with a function.
 *
 * @author eso
 */
public class PredicateChain<T, V> extends FunctionChain<T, V, Boolean>
	implements Predicate<T> {

	/**
	 * Creates a new instance.
	 *
	 * @param outer The predicate that evaluates the inner function results
	 * @param inner The function that produces the predicate input values
	 */
	public PredicateChain(Predicate<V> outer, Function<T, ? extends V> inner) {
		super(outer, inner);
	}

	/**
	 * @see Predicate#and(Predicate)
	 */
	@Override
	public <O extends T> Predicate<O> and(Predicate<? super T> other) {
		return Predicates.and(this, other);
	}

	/**
	 * @see Predicate#from(Function)
	 */
	@Override
	public <I> Predicate<I> from(Function<I, ? extends T> function) {
		return Predicates.chain(this, function);
	}

	/**
	 * @see Predicate#or(Predicate)
	 */
	@Override
	public <O extends T> Predicate<O> or(Predicate<? super T> other) {
		return Predicates.or(this, other);
	}
}
