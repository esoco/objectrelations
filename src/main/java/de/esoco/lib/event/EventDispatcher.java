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
package de.esoco.lib.event;

import de.esoco.lib.property.Immutability;

import java.io.Serializable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/********************************************************************
 * An event handler implementation that dispatches events to a set of event
 * handlers.
 *
 * @author eso
 */
public class EventDispatcher<E extends Event<?>> implements EventHandler<E>,
															Immutability,
															Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Set<EventHandler<E>> aEventHandlers =
		new LinkedHashSet<EventHandler<E>>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public EventDispatcher()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new event handler to this instance. If the given handler is
	 * already part of this instance the call will be ignored.
	 *
	 * @param rHandler The event handler to add
	 */
	public void add(EventHandler<E> rHandler)
	{
		aEventHandlers.add(rHandler);
	}

	/***************************************
	 * @see EventHandler#handleEvent(Event)
	 */
	@Override
	public void handleEvent(E rEvent)
	{
		for (EventHandler<E> rHandler : aEventHandlers)
		{
			rHandler.handleEvent(rEvent);
		}
	}

	/***************************************
	 * Removes an event handler from this instance.
	 *
	 * @param rHandler The event handler to add
	 */
	public void remove(EventHandler<E> rHandler)
	{
		aEventHandlers.remove(rHandler);
	}

	/***************************************
	 * @see Immutability#setImmutable()
	 */
	@Override
	public void setImmutable()
	{
		aEventHandlers = Collections.unmodifiableSet(aEventHandlers);
	}
}
