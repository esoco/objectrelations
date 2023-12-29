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

/**
 * An event handler implementation that chains multiple event handlers and
 * propagates incoming events to all registered handlers. Works similar to the
 * AWT event multicaster.
 *
 * @author eso
 */
public class EventMulticaster<E extends Event<?>> implements EventHandler<E> {

	private final EventHandler<E> first;

	private final EventHandler<E> second;

	/**
	 * Creates a new instance that wraps two event listeners.
	 *
	 * @param first  The first listener of this multicaster
	 * @param second The second listener of this multicaster
	 */
	public EventMulticaster(EventHandler<E> first, EventHandler<E> second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * A static method to concatenate two event listeners by means of event
	 * multicaster instances, thus building a tree of multicaster instances and
	 * listeners.
	 *
	 * @param first  The first listener to concatenate (may be NULL)
	 * @param second The second listener to concatenate (may be NULL)
	 * @return The resulting event listener which may be a multicaster instance
	 */
	public static <E extends Event<?>> EventHandler<E> add(
		EventHandler<E> first, EventHandler<E> second) {
		if (first == null) {
			return second;
		}

		if (second == null) {
			return first;
		}

		return new EventMulticaster<E>(first, second);
	}

	/**
	 * A static method that removes an event listener from a tree of
	 * multicaster
	 * instances. If the given listener isn't part of the tree the call will
	 * have no effect.
	 *
	 * @param listener The listener (tree) to remove the other listener from
	 * @param toRemove The listener to remove (NULL will be ignored)
	 * @return The resulting event listener which may be a multicaster instance
	 */
	public static <E extends Event<?>> EventHandler<E> remove(
		EventHandler<E> listener, EventHandler<E> toRemove) {
		if (listener == toRemove) {
			return null;
		} else if (listener instanceof EventMulticaster<?>) {
			return ((EventMulticaster<E>) listener).remove(toRemove);
		} else {
			return listener;
		}
	}

	/**
	 * Dispatches the given even to all event handlers in the tree of event
	 * multicasters that starts at this instance.
	 *
	 * @see EventHandler#handleEvent(Event)
	 */
	@Override
	public void handleEvent(E event) {
		first.handleEvent(event);
		second.handleEvent(event);
	}

	/**
	 * Recursively removes a certain event listener from this multicaster
	 * (sub)tree and returns the resulting event listener (which may be a
	 * multicaster).
	 *
	 * @param toRemove The listener to remove
	 * @return The resulting event listener which may be a multicaster instance
	 */
	protected EventHandler<E> remove(EventHandler<E> toRemove) {
		if (first == toRemove) {
			return second;
		}

		if (second == toRemove) {
			return first;
		}

		EventHandler<E> removeFirst = remove(first, toRemove);
		EventHandler<E> removeSecond = remove(second, toRemove);

		if (removeFirst != first || removeSecond != second) {
			return add(removeFirst, removeSecond);
		}

		return this;
	}
}
