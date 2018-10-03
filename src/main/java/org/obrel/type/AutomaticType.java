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
package org.obrel.type;

import de.esoco.lib.event.EventDispatcher;
import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Function;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationWrapper;

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypeModifier.READONLY;


/********************************************************************
 * A base class for relation types that update their target or other state
 * automatically based on other relations of the parent object it is set on.
 * This is achieved by registering the type instance as an event lister for
 * relation events on the parent of the relation with this type.
 *
 * <p>If an automatic type is used as a view or and alias it will not have an
 * automatic functionality but will work like a normal relation type.</p>
 *
 * @author eso
 */
public abstract class AutomaticType<T> extends RelationType<T>
	implements EventHandler<RelationEvent<?>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see RelationType#RelationType(RelationTypeModifier...)
	 */
	public AutomaticType(RelationTypeModifier... rModifiers)
	{
		super(rModifiers);
	}

	/***************************************
	 * @see RelationType#RelationType(String, Class, RelationTypeModifier...)
	 */
	public AutomaticType(String					 sName,
						 Class<? super T>		 rTargetType,
						 RelationTypeModifier... rModifiers)
	{
		super(sName, rTargetType, rModifiers);
	}

	/***************************************
	 * @see RelationType#RelationType(Function, Function,
	 *      RelationTypeModifier...)
	 */
	public AutomaticType(Function<? super Relatable, ? super T> fDefaultValue,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(fDefaultValue, fInitialValue, rModifiers);
	}

	/***************************************
	 * @see RelationType#RelationType(String, Class, Function,
	 *      RelationTypeModifier...)
	 */
	public AutomaticType(String									sName,
						 Class<? super T>						rTargetType,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(sName, rTargetType, fInitialValue, rModifiers);
	}

	/***************************************
	 * @see RelationType#RelationType(String, Class, Function, Function,
	 *      RelationTypeModifier...)
	 */
	public AutomaticType(String									sName,
						 Class<? super T>						rTargetType,
						 Function<? super Relatable, ? super T> fDefaultValue,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(sName, rTargetType, fDefaultValue, fInitialValue, rModifiers);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Implemented to invoke {@link #processEvent(RelationEvent)} if the
	 * relation event is not for this type.
	 *
	 * @see EventHandler#handleEvent(de.esoco.lib.event.Event)
	 */
	@Override
	public final void handleEvent(RelationEvent<?> rEvent)
	{
		if (rEvent.getElement().getType() != this)
		{
			processEvent(rEvent);
		}
	}

	/***************************************
	 * Processes a relation event to perform the automatic function of this
	 * type. Will only be invoked if the relation event is not for this type.
	 * The source of the event will be the relatable object on which the
	 * relation has been modified, not the one on which this automatic type
	 * relation has been set (an object, relation, or relation type). The latter
	 * is available from {@link RelationEvent#getEventScope()}.
	 *
	 * @param rEvent The event to process
	 */
	protected abstract void processEvent(RelationEvent<?> rEvent);

	/***************************************
	 * Overridden to add this instance as the relation listener if it implements
	 * the {@link EventHandler} interface. Also checks the modifiers of this
	 * type and invokes {@link #protectTarget(Relatable, Relation)} it is either
	 * {@link RelationTypeModifier#FINAL} or {@link
	 * RelationTypeModifier#READONLY}.
	 *
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	protected Relation<T> addRelation(Relatable rParent, Relation<T> rRelation)
	{
		super.addRelation(rParent, rRelation);

		if (!(rRelation instanceof RelationWrapper))
		{
			if (hasModifier(FINAL) || hasModifier(READONLY))
			{
				protectTarget(rParent, rRelation);
			}

			registerRelationListener(rParent, this);
		}

		return rRelation;
	}

	/***************************************
	 * Overridden to remove this instance as the relation listener if it
	 * implements the {@link EventHandler} interface.
	 *
	 * @see RelationType#deleteRelation(Relatable, Relation)
	 */
	@Override
	protected void deleteRelation(Relatable rParent, Relation<?> rRelation)
	{
		removeRelationListener(rParent, this);

		super.deleteRelation(rParent, rRelation);
	}

	/***************************************
	 * Returns the event listener relation type for a certain parent object.
	 *
	 * @param  rParent The parent relatable
	 *
	 * @return The corresponding event listener relation type
	 */
	protected RelationType<EventDispatcher<RelationEvent<?>>> getListenerType(
		Relatable rParent)
	{
		RelationType<EventDispatcher<RelationEvent<?>>> rListenerType =
			ListenerTypes.RELATION_LISTENERS;

		if (rParent instanceof RelationType)
		{
			rListenerType = ListenerTypes.RELATION_TYPE_LISTENERS;
		}
		else if (rParent instanceof Relation)
		{
			rListenerType = ListenerTypes.RELATION_UPDATE_LISTENERS;
		}

		return rListenerType;
	}

	/***************************************
	 * Will be invoked if this type has one of the modifiers {@link
	 * RelationTypeModifier#FINAL} or {@link RelationTypeModifier#READONLY}. Can
	 * be implemented to prevent external modifications of the relation with
	 * this type (e.g. by wrapping a collection in an immutable variant). The
	 * default implementation does nothing.
	 *
	 * @param rParent   The parent of the relation
	 * @param rRelation The relation to protect the target of
	 */
	protected void protectTarget(Relatable rParent, Relation<T> rRelation)
	{
	}

	/***************************************
	 * Registers the relation listener that performs the automatic function of
	 * this relation type. Depending on the type of the parent object the
	 * listener will either be registered as a relation type listener, a
	 * relation update listener, or a relation listener for all other relatable
	 * objects.
	 *
	 * @param rParent   The parent relatable to register the listener on
	 * @param rListener The listener to register
	 */
	protected void registerRelationListener(
		Relatable					   rParent,
		EventHandler<RelationEvent<?>> rListener)
	{
		rParent.get(getListenerType(rParent)).add(rListener);
	}

	/***************************************
	 * Removes a relation listener that had previously been registered with
	 * {@link #registerRelationListener(Relatable, EventHandler)}.
	 *
	 * @param rParent   The parent relatable to remove the listener from
	 * @param rListener The listener to remove
	 */
	protected void removeRelationListener(
		Relatable					   rParent,
		EventHandler<RelationEvent<?>> rListener)
	{
		rParent.get(getListenerType(rParent)).remove(rListener);
	}
}
