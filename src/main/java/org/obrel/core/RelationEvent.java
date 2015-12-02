//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import org.obrel.type.StandardTypes;


/********************************************************************
 * An element event subclass that contains information related to relation
 * modifications. The invocation of listeners for such events will occur just
 * before an operation is actually performed on the affected relation. If an
 * implementation needs to prevent the change to the relation it may throw a
 * runtime exception but it is recommended that such listeners are only used in
 * special cases which should be well documented.
 *
 * <p>To register a relation event handler for element events it must be added
 * as a relation with the relation type {@link StandardTypes#RELATION_LISTENERS}
 * to an arbitrary related object or a relation type. The event handler may be
 * an arbitrary implementation of the {@link EventHandler} interface for
 * relation events or a supertype of {@link RelationEvent}. All registered
 * listeners will either be notified of relation modifications in the particular
 * object or (if registered on a relation type) of all changes to relations of
 * that type in any object.</p>
 *
 * <p>A listener's {@link EventHandler#handleEvent(Event) handleEvent()} method
 * will be invoked if a relation event occurs. The {@link RelationEvent}
 * parameter contains the informations that are relevant for the particular
 * event and can be queried with the respective methods of the event class. The
 * only element event type that will not occur for relation events is REMOVE_ALL
 * because relations always refer to exactly one object.</p>
 *
 * @author eso
 */
public class RelationEvent<T> extends ElementEvent<Relatable, Relation<T>, T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rType        The event type
	 * @param rSource      The event source
	 * @param rRelation    The relation that is affected by this event
	 * @param rUpdateValue The update value in the case of update events
	 */
	public RelationEvent(EventType   rType,
						 Relatable   rSource,
						 Relation<T> rRelation,
						 T			 rUpdateValue)
	{
		super(rType, rSource, rRelation, rUpdateValue);
	}
}
