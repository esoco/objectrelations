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

/********************************************************************
 * An interface for classes that can generate events of the generic type E.
 *
 * @author eso
 */
public interface EventSource<E extends Event<?>>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds a new element listener to this list. All registered listeners will
	 * be notified if the contents of this list will be notified.
	 *
	 * @param rListener The listener to add
	 */
	public void addListener(EventHandler<? super E> rListener);

	/***************************************
	 * Returns the class of the events of this event source. This method be
	 * implemented to provide type-safety when event sources are cast to event
	 * handlers and vice versa. The generic type has been relaxed to be a
	 * supertype of the actual event class to allow implementations to use event
	 * classes that also have generic parameters. The returned class should be
	 * the most specific class that describes the event classes used by all
	 * instances of a certain implementation.
	 *
	 * @return The event class
	 */
	public Class<? super E> getEventClass();

	/***************************************
	 * Adds a new element listener to this list. All registered listeners will
	 * be notified if the contents of this list will be notified.
	 *
	 * @param rListener The listener to add
	 */
	public void removeListener(EventHandler<? super E> rListener);
}
