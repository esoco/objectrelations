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
package org.obrel.core;

import de.esoco.lib.event.ElementEvent;
import de.esoco.lib.event.EventHandler;

import org.obrel.type.ListenerTypes;


/********************************************************************
 * An element event subclass that contains information related to relation
 * modifications. The invocation of listeners for such events will occur just
 * before an operation is actually performed on the affected relation. If an
 * implementation needs to prevent the change to the relation it may throw a
 * runtime exception but it is recommended that such listeners are only used in
 * special cases which should be well documented.
 *
 * <p>To register a relation event listener for events it must be added as a
 * relation with the relation type {@link ListenerTypes#RELATION_LISTENERS} to
 * an arbitrary related object or a relation type. It may also be registered on
 * a relation with {@link ListenerTypes#RELATION_UPDATE_LISTENERS} or a relation
 * type with {@link ListenerTypes#RELATION_TYPE_LISTENERS} in which cases the
 * listener will be informed of changes to the particular relation or relation
 * type.</p>
 *
 * <p>The event handler may be an arbitrary implementation of the {@link
 * EventHandler} interface for relation events or a super-type of {@link
 * RelationEvent}. All registered listeners will either be notified of relation
 * modifications in the particular object or (if registered on a relation type)
 * of all changes to relations of that type in any object.</p>
 *
 * <p>A listener's {@link EventHandler#handleEvent(de.esoco.lib.event.Event)
 * handleEvent()} method will be invoked if a relation event occurs. The {@link
 * RelationEvent} parameter contains the informations that are relevant for the
 * particular event and can be queried with the respective methods of the event
 * class.</p>
 *
 * @author eso
 */
public class RelationEvent<T> extends ElementEvent<Relatable, Relation<T>, T>
{
	//~ Instance fields --------------------------------------------------------

	private final Relatable rEventScope;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rType        The event type
	 * @param rSource      The relatable event source on which the relation has
	 *                     been modified
	 * @param rRelation    The relation that is affected by this event
	 * @param rUpdateValue The update value in the case of update events
	 * @param rEventScope  The relatable object that defines the event scope
	 */
	public RelationEvent(EventType   rType,
						 Relatable   rSource,
						 Relation<T> rRelation,
						 T			 rUpdateValue,
						 Relatable   rEventScope)
	{
		super(rType, rSource, rRelation, rUpdateValue);

		this.rEventScope = rEventScope;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the relatable that defines the event scope. Depending on the
	 * affected event listener this is either the same as the source or in the
	 * case of {@link ListenerTypes#RELATION_UPDATE_LISTENERS} the corresponding
	 * relation or for {@link ListenerTypes#RELATION_TYPE_LISTENERS} the
	 * relation type.
	 *
	 * @return The eventScope value
	 */
	public final Relatable getEventScope()
	{
		return rEventScope;
	}
}
