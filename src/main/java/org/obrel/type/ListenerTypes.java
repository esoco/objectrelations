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

import java.util.function.BiConsumer;

import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Contains standard event listener relation types and event listener type
 * factory methods.
 *
 * @author eso
 */
public class ListenerTypes
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A relation type for the registration of relation listeners on relatable
	 * objects. A relation listener is notified of all changes to relations of
	 * the parent object it is set on. Will be initialized automatically so it
	 * is not necessary to check for existence before accessing the relation.
	 */
	public static final RelationType<EventDispatcher<RelationEvent<?>>> RELATION_LISTENERS =
		newType(r -> new EventDispatcher<>());

	/**
	 * A relation type for the registration of event listeners on relation
	 * types. A relation type listener is notified of all changes to relations
	 * with the type it is set on. Setting a relation type listener on any other
	 * type of object than a relation type will have no effect. A {@link
	 * #RELATION_LISTENERS} on a relation type will have the same function as on
	 * any other object, i.e will be notified of changes to the (meta-relations)
	 * of the relation type.
	 *
	 * <p>Will be initialized automatically so it is not necessary to check for
	 * existence before accessing the relation.</p>
	 */
	public static final RelationType<EventDispatcher<RelationEvent<?>>> RELATION_TYPE_LISTENERS =
		newType(r -> new EventDispatcher<>());

	/**
	 * A relation type for the registration of update listeners on relations. A
	 * relation update listener is notified of all changes to the relation it is
	 * set on. Setting a relation update listener on any other type of object
	 * than a relation will have no effect. A {@link #RELATION_LISTENERS} on a
	 * relation will have the same function as on any other object, i.e will be
	 * notified of changes to the (meta-relations) of the relation.
	 *
	 * <p>Will be initialized automatically so it is not necessary to check for
	 * existence before accessing the relation.</p>
	 */
	public static final RelationType<EventDispatcher<RelationEvent<?>>> RELATION_UPDATE_LISTENERS =
		newType(r -> new EventDispatcher<>());

	static
	{
		RelationTypes.init(ListenerTypes.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private ListenerTypes()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new {@link ListenerType} instance without a default event
	 * dispatcher. The notification of event listeners must be done by providing
	 * an explicit event dispatch function to the method {@link
	 * ListenerType#notifyListeners(Object, Object, BiConsumer)}.
	 *
	 * @see ListenerType#ListenerType(BiConsumer, RelationTypeModifier...)
	 */
	public static <L, E> ListenerType<L, E> newListenerType(
		RelationTypeModifier... rModifiers)
	{
		return newListenerType(null, rModifiers);
	}

	/***************************************
	 * Creates a new {@link ListenerType} instance.
	 *
	 * @see ListenerType#ListenerType(BiConsumer, RelationTypeModifier...)
	 */
	public static <L, E> ListenerType<L, E> newListenerType(
		BiConsumer<L, E>		fDispatcher,
		RelationTypeModifier... rModifiers)
	{
		return new ListenerType<>(fDispatcher, rModifiers);
	}
}
