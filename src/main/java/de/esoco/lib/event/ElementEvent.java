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
 * An event object that contains information related to the modification of an
 * element of a source object. The source object will typically be some kind of
 * container. The type of the event is identified by the {@link EventType}
 * enumeration. This makes it easier to add additional element event types in
 * future versions without breaking existing code. The following event types are
 * currently defined:
 *
 * <ul>
 *   <li>ADD: an element will be added to the event source</li>
 *   <li>REMOVE: an element will be removed from the event source</li>
 *   <li>REMOVE_ALL: all elements will be removed from the event source (in this
 *     case the element and update value fields will be NULL)</li>
 *   <li>UPDATE: an element of the event source will be updated</li>
 * </ul>
 *
 * <p>Subclasses and event sources do not need to support all these event types.
 * They should document under what circumstances events of a certain type will
 * be sent.</p>
 *
 * <p>Element events are intended to be sent <b>before</b> a modification
 * actually takes place to allow any listeners to prevent a change if necessary.
 * Especially subclasses should therefore contain all additional information
 * that is necessary to evaluate both the actual state of the element affected
 * by the event as well as the intended modification. For this purpose this base
 * class already contains the information about the update value of an UPDATE
 * event.</p>
 *
 * <p>This class is intended to be subclassed by event classes that are specific
 * to a certain purpose. To make it adaptable to different applications it
 * provides multiple generic parameters that can be used to define types of the
 * following elements:</p>
 *
 * <ol>
 *   <li>S: The source of the object (typically the affected container)</li>
 *   <li>E: The type of the affected element (typically the element type of the
 *     container)</li>
 *   <li>U: The value type in the case of an update event; this may either be
 *     the same type as the element type or, if the update modifies only a part
 *     of an element, the type of that part.</li>
 * </ol>
 *
 * <p>If possible a subclass should replace some or all of these parameters with
 * concrete types and not expose all generic parameters in the API that is used
 * by application developers.</p>
 *
 * @author eso
 */
public class ElementEvent<S, E, U> extends GenericEvent<S>
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the possible relation event types
	 */
	public enum EventType { ADD, REMOVE, REMOVE_ALL, UPDATE }

	//~ Instance fields --------------------------------------------------------

	private EventType rType;
	private E		  rElement;
	private U		  rUpdateValue;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rType        The event type
	 * @param rSource      The event source
	 * @param rElement     The element that is affected by this event
	 * @param rUpdateValue The update value in the case of update events (NULL
	 *                     for none)
	 */
	public ElementEvent(EventType rType, S rSource, E rElement, U rUpdateValue)
	{
		super(rSource);
		this.rType		  = rType;
		this.rElement     = rElement;
		this.rUpdateValue = rUpdateValue;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the element that is affected by this event. What exactly this is
	 * depends on the actual event subclass. In some cases it may be the
	 * affected element itself, in others it may be a reference that describes
	 * how to access the element.
	 *
	 * @return The element
	 */
	public final E getElement()
	{
		return rElement;
	}

	/***************************************
	 * Returns the type of this event.
	 *
	 * @return The event type
	 */
	public final EventType getType()
	{
		return rType;
	}

	/***************************************
	 * Returns the new element value in the case of an update event.
	 *
	 * @return The element update value
	 */
	public final U getUpdateValue()
	{
		return rUpdateValue;
	}

	/***************************************
	 * @see GenericEvent#toString()
	 */
	@Override
	protected String paramString()
	{
		return String.format("%s,%s,%s,%s",
							 super.paramString(),
							 rType,
							 rElement,
							 rUpdateValue);
	}
}
