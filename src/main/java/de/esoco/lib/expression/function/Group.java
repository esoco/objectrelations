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
package de.esoco.lib.expression.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * A function implementation that groups an arbitrary number of consumers and
 * applies all of them successively to input values in the same order in which
 * they are added to the group. The list of the consumers in a group can be
 * queried with {@link #getMembers()}.
 *
 * @author eso
 */
public class Group<I> extends AbstractFunction<I, I> {

	private final List<Consumer<? super I>> consumers;

	/**
	 * Creates a new instance with a certain set of consumers to be executed.
	 * The given list will be copied by this instance.
	 *
	 * @param functions The list of consumers
	 */
	public Group(Collection<Consumer<? super I>> functions) {
		super(Group.class.getSimpleName());

		consumers = new ArrayList<>(functions);
	}

	/**
	 * Create a new group from a set of consumers.
	 *
	 * @param first               The first consumer to evaluate
	 * @param additionalConsumers additionalFunctions Optional additional
	 *                            consumers to evaluate
	 * @return A new function group instance
	 */
	@SafeVarargs
	public static <I> Group<I> of(Consumer<? super I> first,
		Consumer<? super I>... additionalConsumers) {
		List<Consumer<? super I>> functions = new ArrayList<>();

		functions.add(first);

		if (additionalConsumers != null && additionalConsumers.length > 0) {
			functions.addAll(Arrays.asList(additionalConsumers));
		}

		return new Group<>(functions);
	}

	/**
	 * Applies all consumers to the input value and returns the input value
	 * unchanged to support function chaining.
	 *
	 * @see de.esoco.lib.expression.Function#evaluate(Object)
	 */
	@Override
	public I evaluate(I input) {
		for (Consumer<? super I> consumer : consumers) {
			consumer.accept(input);
		}

		return input;
	}

	/**
	 * Returns the members of this group. The returned list is a copy of the
	 * internal list and can be manipulated freely.
	 *
	 * @return A new list containing the members of this group
	 */
	public List<Consumer<? super I>> getMembers() {
		return new ArrayList<>(consumers);
	}

	/**
	 * Returns TRUE if all consumers in this group are equal.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		Group<?> otherFunction = (Group<?>) other;
		int count = consumers.size();

		if (count != otherFunction.consumers.size()) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			if (!consumers.get(i).equals(otherFunction.consumers.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Calculates the combined hash code of all consumers in this group.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		int hashCode = 17;

		for (Consumer<? super I> consumer : consumers) {
			hashCode = hashCode * 31 + consumer.hashCode();
		}

		return hashCode;
	}
}
