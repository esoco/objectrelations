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
package org.obrel.core;

import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Function;

import org.obrel.type.ListenerTypes;


/********************************************************************
 * A wrapper for other relations that provides a view of the relation with a
 * different relation type and optionally a different datatype. Wrapper can also
 * be set on a different parent than the wrapped relation.
 */
public abstract class RelationWrapper<T, R, F extends Function<R, T>>
	extends Relation<T> implements EventHandler<RelationEvent<R>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final Relatable   rParent;
	private final Relation<R> rWrappedRelation;
	private final F			  fTargetConversion;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a view of a certain relation.
	 *
	 * @param rParent     The parent relatable this wrapper is set on
	 * @param rType       The relation type of this wrapper
	 * @param rWrapped    The relation to be wrapped
	 * @param fConversion A conversion function that converts the target of the
	 *                    wrapped relation into the datatype of this view's
	 *                    relation type
	 */
	RelationWrapper(Relatable		rParent,
					RelationType<T> rType,
					Relation<R>		rWrapped,
					F				fConversion)
	{
		super(rType);

		this.rParent	  = rParent;
		rWrappedRelation  = rWrapped;
		fTargetConversion = fConversion;

		rWrappedRelation.addUpdateListener(this);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the conversion function of this instance.
	 *
	 * @return The conversion function
	 */
	public final F getConversion()
	{
		return fTargetConversion;
	}

	/***************************************
	 * Returns the target of the wrapped relation as converted by the conversion
	 * function.
	 *
	 * @see Relation#getTarget()
	 */
	@Override
	public T getTarget()
	{
		return fTargetConversion.evaluate(rWrappedRelation.getTarget());
	}

	/***************************************
	 * Returns the wrapped relation.
	 *
	 * @return The wrapped relation
	 */
	public final Relation<R> getWrappedRelation()
	{
		return rWrappedRelation;
	}

	/***************************************
	 * Forwards events on the wrapped relation to listeners on this relation.
	 *
	 * @param rEvent The event that occurred on the wrapped relation
	 */
	@Override
	public void handleEvent(RelationEvent<R> rEvent)
	{
		if (rEvent.getType() == EventType.UPDATE)
		{
			T rUpdateValue =
				fTargetConversion.evaluate(rEvent.getUpdateValue());

			RelationEvent<T> rConvertedEvent =
				new RelationEvent<>(
					EventType.UPDATE,
					rParent,
					this,
					rUpdateValue,
					this);

			get(ListenerTypes.RELATION_UPDATE_LISTENERS).dispatch(
				rConvertedEvent);
		}
	}

	/***************************************
	 * Returns the parent this wrapper is set on.
	 *
	 * @return The parent value
	 */
	protected final Relatable getParent()
	{
		return rParent;
	}

	/***************************************
	 * Implemented to unregister the event listener on the wrapped relation.
	 *
	 * @see Relation#removed()
	 */
	@Override
	protected void removed()
	{
		rWrappedRelation.get(ListenerTypes.RELATION_UPDATE_LISTENERS)
						.remove(this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	boolean dataEqual(Relation<?> rOther)
	{
		return rWrappedRelation ==
			   ((RelationWrapper<?, ?, ?>) rOther).rWrappedRelation;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	int dataHashCode()
	{
		return rWrappedRelation.dataHashCode();
	}

	/***************************************
	 * Always throws an exception because views are readonly.
	 *
	 * @see Relation#setTarget(Object)
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		throw new UnsupportedOperationException(
			"View relation is readonly: " +
			this);
	}
}
