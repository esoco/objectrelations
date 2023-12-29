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

/**
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
	implements EventHandler<RelationEvent<?>> {

	private static final long serialVersionUID = 1L;

	/**
	 * @see RelationType#RelationType(RelationTypeModifier...)
	 */
	public AutomaticType(RelationTypeModifier... modifiers) {
		super(modifiers);
	}

	/**
	 * @see RelationType#RelationType(String, Class, RelationTypeModifier...)
	 */
	public AutomaticType(String name, Class<? super T> targetType,
		RelationTypeModifier... modifiers) {
		super(name, targetType, modifiers);
	}

	/**
	 * @see RelationType#RelationType(Function, Function,
	 * RelationTypeModifier...)
	 */
	public AutomaticType(Function<? super Relatable, ? super T> defaultValue,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		super(defaultValue, initialValue, modifiers);
	}

	/**
	 * @see RelationType#RelationType(String, Class, Function,
	 * RelationTypeModifier...)
	 */
	public AutomaticType(String name, Class<? super T> targetType,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		super(name, targetType, initialValue, modifiers);
	}

	/**
	 * @see RelationType#RelationType(String, Class, Function, Function,
	 * RelationTypeModifier...)
	 */
	public AutomaticType(String name, Class<? super T> targetType,
		Function<? super Relatable, ? super T> defaultValue,
		Function<? super Relatable, ? super T> initialValue,
		RelationTypeModifier... modifiers) {
		super(name, targetType, defaultValue, initialValue, modifiers);
	}

	/**
	 * Implemented to invoke {@link #processEvent(RelationEvent)} if the
	 * relation event is not for this type.
	 *
	 * @see EventHandler#handleEvent(de.esoco.lib.event.Event)
	 */
	@Override
	public final void handleEvent(RelationEvent<?> event) {
		if (event.getElement().getType() != this) {
			processEvent(event);
		}
	}

	/**
	 * Overridden to add this instance as the relation listener if it
	 * implements
	 * the {@link EventHandler} interface. Also checks the modifiers of this
	 * type and invokes {@link #protectTarget(Relatable, Relation)} it is
	 * either
	 * {@link RelationTypeModifier#FINAL} or {@link
	 * RelationTypeModifier#READONLY}.
	 *
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	protected Relation<T> addRelation(Relatable parent, Relation<T> relation) {
		super.addRelation(parent, relation);

		if (!(relation instanceof RelationWrapper)) {
			if (hasModifier(FINAL) || hasModifier(READONLY)) {
				protectTarget(parent, relation);
			}

			registerRelationListener(parent, this);
		}

		return relation;
	}

	/**
	 * Overridden to remove this instance as the relation listener if it
	 * implements the {@link EventHandler} interface.
	 *
	 * @see RelationType#deleteRelation(Relatable, Relation)
	 */
	@Override
	protected void deleteRelation(Relatable parent, Relation<?> relation) {
		removeRelationListener(parent, this);

		super.deleteRelation(parent, relation);
	}

	/**
	 * Returns the event listener relation type for a certain parent object.
	 *
	 * @param parent The parent relatable
	 * @return The corresponding event listener relation type
	 */
	protected RelationType<EventDispatcher<RelationEvent<?>>> getListenerType(
		Relatable parent) {
		RelationType<EventDispatcher<RelationEvent<?>>> listenerType =
			ListenerTypes.RELATION_LISTENERS;

		if (parent instanceof RelationType) {
			listenerType = ListenerTypes.RELATION_TYPE_LISTENERS;
		} else if (parent instanceof Relation) {
			listenerType = ListenerTypes.RELATION_UPDATE_LISTENERS;
		}

		return listenerType;
	}

	/**
	 * Processes a relation event to perform the automatic function of this
	 * type. Will only be invoked if the relation event is not for this type.
	 * The source of the event will be the relatable object on which the
	 * relation has been modified, not the one on which this automatic type
	 * relation has been set (an object, relation, or relation type). The
	 * latter
	 * is available from {@link RelationEvent#getEventScope()}.
	 *
	 * @param event The event to process
	 */
	protected abstract void processEvent(RelationEvent<?> event);

	/**
	 * Will be invoked if this type has one of the modifiers {@link
	 * RelationTypeModifier#FINAL} or {@link RelationTypeModifier#READONLY}.
	 * Can
	 * be implemented to prevent external modifications of the relation with
	 * this type (e.g. by wrapping a collection in an immutable variant). The
	 * default implementation does nothing.
	 *
	 * @param parent   The parent of the relation
	 * @param relation The relation to protect the target of
	 */
	protected void protectTarget(Relatable parent, Relation<T> relation) {
	}

	/**
	 * Registers the relation listener that performs the automatic function of
	 * this relation type. Depending on the type of the parent object the
	 * listener will either be registered as a relation type listener, a
	 * relation update listener, or a relation listener for all other relatable
	 * objects.
	 *
	 * @param parent   The parent relatable to register the listener on
	 * @param listener The listener to register
	 */
	protected void registerRelationListener(Relatable parent,
		EventHandler<RelationEvent<?>> listener) {
		parent.get(getListenerType(parent)).add(listener);
	}

	/**
	 * Removes a relation listener that had previously been registered with
	 * {@link #registerRelationListener(Relatable, EventHandler)}.
	 *
	 * @param parent   The parent relatable to remove the listener from
	 * @param listener The listener to remove
	 */
	protected void removeRelationListener(Relatable parent,
		EventHandler<RelationEvent<?>> listener) {
		parent.get(getListenerType(parent)).remove(listener);
	}
}
