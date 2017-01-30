//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypeModifier.READONLY;
import static org.obrel.type.StandardTypes.RELATION_LISTENERS;
import static org.obrel.type.StandardTypes.RELATION_TYPE_LISTENERS;
import static org.obrel.type.StandardTypes.RELATION_UPDATE_LISTENERS;


/********************************************************************
 * A base class for relation types that update their target or other state
 * automatically based on other relations of the parent object it is set on. The
 * typical way to create such a type is to let it implement the interface {@link
 * EventHandler} for {@link RelationEvent RelationEvent&lt;?&gt;} and then
 * register this relation type as the event handler for all other relations with
 * {@link #registerRelationListener(Relatable, EventHandler)}. If a subclass
 * implements the interface the registration is done automatically in the
 * overridden method {@link #addRelation(Relatable, Relation)} and undone with
 * {@link #removeRelationListener(Relatable, EventHandler)} in {@link
 * #deleteRelation(Relatable, Relation)}.
 *
 * <p>Because the event listener management methods take the event listener as
 * the argument it is not necessary to let the relation type implement the
 * listener interface directly but to use a separate listener object instead.
 * But in that case the listener instance must be stored somewhere so that it
 * can be removed later if necessary. This could for example be done with a
 * (private) meta-relation on the relation of this type (which is an argument to
 * both the add and delete relation methods).</p>
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
	 * {@inheritDoc}
	 */
	public AutomaticType(RelationTypeModifier... rModifiers)
	{
		super(rModifiers);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	public AutomaticType(String					 sName,
						 Class<? super T>		 rTargetType,
						 RelationTypeModifier... rModifiers)
	{
		super(sName, rTargetType, rModifiers);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	public AutomaticType(T										rDefaultValue,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(rDefaultValue, fInitialValue, rModifiers);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	public AutomaticType(String									sName,
						 Class<? super T>						rTargetType,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(sName, rTargetType, fInitialValue, rModifiers);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	public AutomaticType(String									sName,
						 Class<? super T>						rTargetType,
						 T										rDefaultValue,
						 Function<? super Relatable, ? super T> fInitialValue,
						 RelationTypeModifier... 				rModifiers)
	{
		super(sName, rTargetType, rDefaultValue, fInitialValue, rModifiers);
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

		if (hasModifier(FINAL) || hasModifier(READONLY))
		{
			protectTarget(rParent, rRelation);
		}

		registerRelationListener(rParent, this);

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
		RelationType<EventDispatcher<RelationEvent<?>>> rEventScope =
			RELATION_LISTENERS;

		if (rParent instanceof RelationType)
		{
			rEventScope = RELATION_TYPE_LISTENERS;
		}
		else if (rParent instanceof Relation)
		{
			rEventScope = RELATION_UPDATE_LISTENERS;
		}

		return rEventScope;
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
