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


/********************************************************************
 * An automatic relation type that counts occurrences of other relations.
 *
 * @author eso
 */
public class CounterType<N extends Number> extends AutomaticType<N>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final Predicate<RelationEvent<?>> pCount;
	private final Function<N, N>			  fIncrement;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName         The name of this type
	 * @param rInitialValue The value to start counting at
	 * @param pCount        A predicate that determines what to count
	 * @param fIncrement    A function that increments the number value
	 * @param rModifiers    The relation type modifiers
	 */
	@SuppressWarnings("unchecked")
	public CounterType(String					   sName,
					   N						   rInitialValue,
					   Predicate<RelationEvent<?>> pCount,
					   Function<N, N>			   fIncrement,
					   RelationTypeModifier...     rModifiers)
	{
		super(
			sName,
			(Class<N>) rInitialValue.getClass(),
			value(rInitialValue),
			rModifiers);

		this.pCount     = pCount;
		this.fIncrement = fIncrement;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method for a counter with an arbitrary number type that is
	 * initialized by {@link RelationTypes#init(Class...)}.
	 *
	 * @param  rInitialValue The value to start counting at
	 * @param  pCount        A predicate that determines what to count
	 * @param  fIncrement    A function that increments the number value
	 * @param  rModifiers    The relation type modifiers
	 *
	 * @return The new instance
	 */
	public static <N extends Number> CounterType<N> newCounter(
		N							rInitialValue,
		Predicate<RelationEvent<?>> pCount,
		Function<N, N>				fIncrement,
		RelationTypeModifier...     rModifiers)
	{
		return new CounterType<>(
			null,
			rInitialValue,
			pCount,
			fIncrement,
			rModifiers);
	}

	/***************************************
	 * Factory method for an integer counter that starts at zero and is
	 * initialized by {@link RelationTypes#init(Class...)}.
	 *
	 * @param  pCount     A predicate that determines what to count
	 * @param  rModifiers The relation type modifiers
	 *
	 * @return The new instance
	 */
	public static CounterType<Integer> newIntCounter(
		Predicate<RelationEvent<?>> pCount,
		RelationTypeModifier...     rModifiers)
	{
		return newCounter(
			Integer.valueOf(0),
			pCount,
			MathFunctions.add(1),
			rModifiers);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void processEvent(RelationEvent<?> rEvent)
	{
		if (pCount.test(rEvent))
		{
			Relation<N> rCount = rEvent.getEventScope().getRelation(this);

			setRelationTarget(rCount, fIncrement.apply(rCount.getTarget()));
		}
	}
}
