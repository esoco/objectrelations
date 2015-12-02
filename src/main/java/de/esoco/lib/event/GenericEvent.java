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
 * A generic event class that implements the Event interface. The generic type
 * parameter defines the type of the event source that can be queried with the
 * {@link #getSource()} method.
 *
 * @author eso
 */
public class GenericEvent<T> implements Event<T>
{
	//~ Instance fields --------------------------------------------------------

	private T rSource;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance from a certain event source.
	 *
	 * @param rSource The event source
	 */
	public GenericEvent(T rSource)
	{
		this.rSource = rSource;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the source.
	 *
	 * @return The source
	 */
	@Override
	public final T getSource()
	{
		return rSource;
	}

	/***************************************
	 * Returns a string representation of this event. Subclasses with additional
	 * parameters should override the method {@link #paramString()} to return a
	 * string that describes these parameters.
	 *
	 * @return A string describing this event
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() +
			   String.format("[%s]", paramString());
	}

	/***************************************
	 * Returns a string description of this event's parameters. Subclasses that
	 * have additional parameters should override this method and add a
	 * comma-separated list of their parameters to the value returned by this
	 * implementation.
	 *
	 * @return A comma-separated string list of this event's parameters
	 */
	protected String paramString()
	{
		return rSource.toString();
	}

	/***************************************
	 * Sets the source. Protected because this method is only meant to allow
	 * subclasses to change the event source in special cases.
	 *
	 * @param rSource The new source object
	 */
	protected final void setSource(T rSource)
	{
		this.rSource = rSource;
	}
}
