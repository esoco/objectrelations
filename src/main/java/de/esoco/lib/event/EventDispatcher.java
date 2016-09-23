//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
public class EventDispatcher<E extends Event<?>> implements Immutability,
															Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Set<EventHandler<E>> aEventHandlers = new LinkedHashSet<>();

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
	 * already part of this instance the call will be ignored. The generic
	 * argument is weakened to '? extends E' to allow the usage of handlers that
	 * use generic events but all event handlers in a dispatcher must always be
	 * based on the same (generic) event class or else a class cast exception
	 * will occur.
	 *
	 * @param rHandler The event handler to add
	 */
	@SuppressWarnings("unchecked")
	public void add(EventHandler<? extends E> rHandler)
	{
		aEventHandlers.add((EventHandler<E>) rHandler);
	}

	/***************************************
	 * @see EventHandler#handleEvent(Event)
	 */
	public void dispatch(E rEvent)
	{
		for (EventHandler<E> rHandler : aEventHandlers)
		{
			rHandler.handleEvent(rEvent);
		}
	}

	/***************************************
	 * Returns the number of event handlers that are registered in this
	 * instance.
	 *
	 * @return The event handler count
	 */
	public int getEventHandlerCount()
	{
		return aEventHandlers.size();
	}

	/***************************************
	 * Removes an event handler from this instance.
	 *
	 * @param rHandler The event handler to add
	 */
	public void remove(EventHandler<? extends E> rHandler)
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
