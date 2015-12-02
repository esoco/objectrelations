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
package org.obrel.type;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;


/********************************************************************
 * A base class for event listener relation types. The generic parameters define
 * the type of the event listener interface and the event objects that are
 * handled by the subclass.
 *
 * <p>The actual event handling can be performed in two different ways. For the
 * common and more simple case of event listener interfaces that contain just a
 * single event handling method a new subclass can be created for the event type
 * (e.g. by creating an (anonymous) inner class). This subclass must then
 * override the method {@link #notifyListener(Object, Object)} which will be
 * invoked by the method {@link #notifyListeners(Object, Object)}. The latter
 * method must be invoked by application code to notify all listeners of the
 * first argument object.</p>
 *
 * <p>For the more complex case where an event listener interface consists of
 * multiple methods an application can use the alternate notification method
 * {@link #notifyListeners(Object, Object, NotificationHandler)} instead. It's
 * first argument is an instance of the interface {@link NotificationHandler}
 * which will be invoked for each listeners. The application code that performs
 * the listener notification then needs to provide an implementation of this
 * interface for each listener method that will be invoked.</p>
 *
 * @author eso
 */
@RelationTypeNamespace(RelationType.DEFAULT_NAMESPACE)
public class ListenerType<L, E> extends RelationType<List<L>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/** A listener type for the action listener interface */
	public static final ListenerType<ActionListener, ActionEvent> ACTION_LISTENERS =
		new ListenerType<ActionListener, ActionEvent>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void notifyListener(
				ActionListener rListener,
				ActionEvent    rEvent)
			{
				rListener.actionPerformed(rEvent);
			}
		};

	static
	{
		RelationTypes.init(ListenerType.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rModifiers The relation type modifiers for the new instance
	 */
	public ListenerType(RelationTypeModifier... rModifiers)
	{
		super(rModifiers);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden to always return a new list.
	 *
	 * @see RelationType#initialValue(Relatable)
	 */
	@Override
	public List<L> initialValue(Relatable rParent)
	{
		return new ArrayList<L>();
	}

	/***************************************
	 * Notifies all event listeners that are registered with this relation type
	 * in the given source object of an event. The notification will be
	 * performed by invoking the method {@link #notifyListener(Object, Object)}
	 * which must have been overridden by a superclass (or else an exception
	 * will be thrown).
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
				notifyListener(rListener, rEvent);
			}
		}
	}

	/***************************************
	 * Notifies all event listeners that are registered with this relation type
	 * in the given source object by using the given notification handler.
	 *
	 * @param rSource  The event source to notify the listeners of
	 * @param rEvent   The event object to send to the listeners
	 * @param rHandler The event handler to be used to notify the listeners
	 */
	public final void notifyListeners(Object					rSource,
									  E							rEvent,
									  NotificationHandler<L, E> rHandler)
	{
		Relatable rRelatable = ObjectRelations.getRelatable(rSource);

		if (rRelatable.hasRelation(this))
		{
			for (L rListener : rRelatable.get(this))
			{
				rHandler.notifyListener(rListener, rEvent);
			}
		}
	}

	/***************************************
	 * This method must be overridden by subclasses to notify a single event
	 * listener. It will be invoked internally by this base class from the
	 * method {@link #notifyListeners(Object, Object)}. Alternatively the method
	 * {@link #notifyListeners(Object, Object, NotificationHandler)} can be used
	 * to notify listeners with different event handling methods by using an
	 * implementation of the interface {@link NotificationHandler}.
	 *
	 * <p>This default implementation throws an exception to signal that the
	 * event handling has not been implemented.</p>
	 *
	 * @param rListener The event listener to invoke a method of
	 * @param rEventObj The event object to invoke the listener method with
	 */
	protected void notifyListener(L rListener, E rEventObj)
	{
		throw new UnsupportedOperationException("notifyListener() not implemented");
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * This interface is used by instances of {@link ListenerType} to perform
	 * the listener notification. Implementations must invoke the notification
	 * method of the original event listener type L in their implementation of
	 * the {@link #notify()} method.
	 */
	public static interface NotificationHandler<L, E>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * This method must be implemented to invoke an event notification
		 * method of the the original event listener type L.
		 *
		 * @param rListener The event listener to invoke a method of
		 * @param rEventObj The event object to invoke the listener method with
		 */
		public void notifyListener(L rListener, E rEventObj);
	}
}
