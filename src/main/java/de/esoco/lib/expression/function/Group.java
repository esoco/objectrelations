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


/********************************************************************
 * A function implementation that groups an arbitrary number of consumers and
 * applies all of them successively to input values in the same order in which
 * they are added to the group. The list of the consumers in a group can be
 * queried with {@link #getMembers()}.
 *
 * @author eso
 */
public class Group<I> extends AbstractFunction<I, I>
{
	//~ Instance fields --------------------------------------------------------

	private List<Consumer<? super I>> aConsumers;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain set of consumers to be executed.
	 * The given list will be copied by this instance.
	 *
	 * @param rFunctions The list of consumers
	 */
	public Group(Collection<Consumer<? super I>> rFunctions)
	{
		super(Group.class.getSimpleName());

		aConsumers = new ArrayList<>(rFunctions);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Create a new group from a set of consumers.
	 *
	 * @param  fFirst               The first consumer to evaluate
	 * @param  rAdditionalConsumers rAdditionalFunctions Optional additional
	 *                              consumers to evaluate
	 *
	 * @return A new function group instance
	 */
	@SafeVarargs
	public static <I> Group<I> of(
		Consumer<? super I>    fFirst,
		Consumer<? super I>... rAdditionalConsumers)
	{
		List<Consumer<? super I>> aFunctions = new ArrayList<>();

		aFunctions.add(fFirst);

		if (rAdditionalConsumers != null && rAdditionalConsumers.length > 0)
		{
			aFunctions.addAll(Arrays.asList(rAdditionalConsumers));
		}

		return new Group<>(aFunctions);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies all consumers to the input value and returns the input value
	 * unchanged to support function chaining.
	 *
	 * @see de.esoco.lib.expression.Function#evaluate(Object)
	 */
	@Override
	public I evaluate(I rInput)
	{
		for (Consumer<? super I> fConsumer : aConsumers)
		{
			fConsumer.accept(rInput);
		}

		return rInput;
	}

	/***************************************
	 * Returns the members of this group. The returned list is a copy of the
	 * internal list and can be manipulated freely.
	 *
	 * @return A new list containing the members of this group
	 */
	public List<Consumer<? super I>> getMembers()
	{
		return new ArrayList<>(aConsumers);
	}

	/***************************************
	 * Returns TRUE if all consumers in this group are equal.
	 *
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		Group<?> rOtherFunction = (Group<?>) rOther;
		int		 nCount		    = aConsumers.size();

		if (nCount != rOtherFunction.aConsumers.size())
		{
			return false;
		}

		for (int i = 0; i < nCount; i++)
		{
			if (!aConsumers.get(i).equals(rOtherFunction.aConsumers.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * Calculates the combined hash code of all consumers in this group.
	 *
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		int nHashCode = 17;

		for (Consumer<? super I> fConsumer : aConsumers)
		{
			nHashCode = nHashCode * 31 + fConsumer.hashCode();
		}

		return nHashCode;
	}
}
