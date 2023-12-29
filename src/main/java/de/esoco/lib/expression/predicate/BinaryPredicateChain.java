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

import de.esoco.lib.expression.BinaryPredicate;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;
import de.esoco.lib.expression.function.DualFunctionChain;

/**
 * A subclass of {@link DualFunctionChain} for predicates that also implements
 * the binary predicate interface. This allows to create a new predicate
 * instance from the chaining of a binary predicate with a function.
 *
 * @author eso
 */
public class BinaryPredicateChain<L, R, V, W>
	extends DualFunctionChain<L, R, V, W, Boolean>
	implements BinaryPredicate<L, R> {

	/**
	 * Creates a new instance.
	 *
	 * @param outer The predicate that evaluates the results of the left and
	 *              right functions
	 * @param left  The function that produces the left predicate input
	 * @param right The function that produces the right predicate input
	 */
	public BinaryPredicateChain(BinaryPredicate<V, W> outer,
		Function<L, ? extends V> left, Function<R, ? extends W> right) {
		super(outer, left, right);
	}

	/**
	 * @see Predicate#and(Predicate)
	 */
	@Override
	public <O extends L> Predicate<O> and(Predicate<? super L> other) {
		return Predicates.and(this, other);
	}

	/**
	 * @see Predicate#from(Function)
	 */
	@Override
	public <I> Predicate<I> from(Function<I, ? extends L> function) {
		return Predicates.chain(this, function);
	}

	/**
	 * @see BinaryPredicate#from(Function, Function)
	 */
	@Override
	public <A, B> BinaryPredicate<A, B> from(Function<A, ? extends L> left,
		Function<B, ? extends R> right) {
		return Predicates.chain(this, left, right);
	}

	/**
	 * @see Predicate#or(Predicate)
	 */
	@Override
	public <O extends L> Predicate<O> or(Predicate<? super L> other) {
		return Predicates.or(this, other);
	}
}
