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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;


/********************************************************************
 * A generic event listener relation type. The generic parameters define the
 * type of the event listener and the event objects that are handled by the
 * subclass. A new instance is created with a binary consumer function that will
 * notify instances of the listener type of event objects.
 *
 * <p>The actual event handling can be performed in two different ways. For the
 * common and more simple case of event listener interfaces that contain just a
 * single event handling method the constructor can receive a binary consumer
 * function. This dispatch function will be used to notify listener when the
 * method {@link #notifyListeners(Object, Object)} is invoked.</p>
 *
 * <p>For more complex cases where an event listener interface consists of
 * multiple methods an application can use the alternate notification method
 * {@link #notifyListeners(Object, Object, BiConsumer)}. Here the event dispatch
 * function to invoke for each listener is given as the last argument. A
 * possible use would be to let an event type enum implement the dispatch
 * interface with the invocation of different event listener methods for each
 * type value.</p>
 *
 * @author eso
 */
@RelationTypeNamespace(RelationType.DEFAULT_NAMESPACE)
public class ListenerType<L, E> extends RelationType<List<L>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private BiConsumer<L, E> fEventDispatcher;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance. The event dispatcher argument may be NULL in
	 * which case the method {@link #notifyListeners(Object, Object,
	 * BiConsumer)} must be used for listener notification or else a {@link
	 * NullPointerException} will occur.
	 *
	 * @param fDispatcher A binary consumer that dispatches a certain event to a
	 *                    single listener
	 * @param rModifiers  The relation type modifiers for the new instance
	 */
	public ListenerType(
		BiConsumer<L, E>		fDispatcher,
		RelationTypeModifier... rModifiers)
	{
		super(null, r -> new ArrayList<L>(), rModifiers);

		fEventDispatcher = fDispatcher;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Notifies all event listeners that are registered with this relation type
	 * in the given source object of an event. The notification will be
	 * performed by invoking the event dispatch function that has been provided
	 * to the constructor.
	 *
	 * @param rSource The event source to notify the listeners of
	 * @param rEvent  The event object to send to the listeners
	 */
	public final void notifyListeners(Object rSource, E rEvent)
	{
		Relatable rRelatable = ObjectRelations.getRelatable(rSource);

		if (rRelatable.hasRelation(this))
		{
			for (L rListener : rRelatable.get(this))
			{
				fEventDispatcher.accept(rListener, rEvent);
			}
		}
	}

	/***************************************
	 * Notifies all event listeners that are registered with this relation type
	 * in the given source object by using the given event dispatcher. This
	 * variant allows to invoke varying event handler methods (e.g. for multiple
	 * event types) by using different dispatch functions.
	 *
	 * @param rSource     The event source to notify the listeners of
	 * @param rEvent      The event object to send to the listeners
	 * @param fDispatcher The event dispatch function to be used to notify the
	 *                    listeners
	 */
	public final void notifyListeners(Object		   rSource,
									  E				   rEvent,
									  BiConsumer<L, E> fDispatcher)
	{
		Relatable rRelatable = ObjectRelations.getRelatable(rSource);

		if (rRelatable.hasRelation(this))
		{
			for (L rListener : rRelatable.get(this))
			{
				fDispatcher.accept(rListener, rEvent);
			}
		}
	}
}
