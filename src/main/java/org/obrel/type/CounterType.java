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
package org.obrel.type;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.MathFunctions;
import de.esoco.lib.expression.Predicate;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import static de.esoco.lib.expression.Functions.value;

/**
 * An automatic relation type that counts occurrences of other relations.
 *
 * @author eso
 */
public class CounterType<N extends Number> extends AutomaticType<N> {

	private static final long serialVersionUID = 1L;

	private final Predicate<RelationEvent<?>> count;

	private final Function<N, N> increment;

	/**
	 * Creates a new instance.
	 *
	 * @param name         The name of this type
	 * @param initialValue The value to start counting at
	 * @param count        A predicate that determines what to count
	 * @param increment    A function that increments the number value
	 * @param modifiers    The relation type modifiers
	 */
	@SuppressWarnings("unchecked")
	public CounterType(String name, N initialValue,
		Predicate<RelationEvent<?>> count, Function<N, N> increment,
		RelationTypeModifier... modifiers) {
		super(name, (Class<N>) initialValue.getClass(), value(initialValue),
			modifiers);

		this.count = count;
		this.increment = increment;
	}

	/**
	 * Factory method for a counter with an arbitrary number type that is
	 * initialized by {@link RelationTypes#init(Class...)}.
	 *
	 * @param initialValue The value to start counting at
	 * @param count        A predicate that determines what to count
	 * @param increment    A function that increments the number value
	 * @param modifiers    The relation type modifiers
	 * @return The new instance
	 */
	public static <N extends Number> CounterType<N> newCounter(N initialValue,
		Predicate<RelationEvent<?>> count, Function<N, N> increment,
		RelationTypeModifier... modifiers) {
		return new CounterType<>(null, initialValue, count, increment,
			modifiers);
	}

	/**
	 * Factory method for an integer counter that starts at zero and is
	 * initialized by {@link RelationTypes#init(Class...)}.
	 *
	 * @param count     A predicate that determines what to count
	 * @param modifiers The relation type modifiers
	 * @return The new instance
	 */
	public static CounterType<Integer> newIntCounter(
		Predicate<RelationEvent<?>> count, RelationTypeModifier... modifiers) {
		return newCounter(Integer.valueOf(0), count, MathFunctions.add(1),
			modifiers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processEvent(RelationEvent<?> event) {
		if (count.test(event)) {
			Relation<N> count = event.getEventScope().getRelation(this);

			setRelationTarget(count, increment.apply(count.getTarget()));
		}
	}
}
