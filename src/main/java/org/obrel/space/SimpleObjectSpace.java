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
package org.obrel.space;

import de.esoco.lib.expression.Function;

import org.obrel.core.ObjectRelations;
import org.obrel.core.RelatedObject;


/********************************************************************
 * A simple {@link ObjectSpace} implementation based on {@link RelatedObject}
 * that maps access URLs to the hierarchy of it's relations. The conversion
 * between relation target objects and the datatype of an object space is
 * performed by a value mapping function that must be handed to the constructor.
 *
 * @author eso
 */
public class SimpleObjectSpace<T> extends RelatedObject
	implements ObjectSpace<T>
{
	//~ Instance fields --------------------------------------------------------

	private Function<Object, T> fValueMapper;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param fValueMapper The value mapping function
	 */
	public SimpleObjectSpace(Function<Object, T> fValueMapper)
	{
		this.fValueMapper = fValueMapper;
	}

	/***************************************
	 * Subclass constructor without a mapping function. The subclass must
	 * override the {@link #get(String)} method because it uses the value
	 * function which will be NULL.
	 */
	protected SimpleObjectSpace()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T get(String sUrl)
	{
		return fValueMapper.evaluate(ObjectRelations.urlGet(this, sUrl));
	}

	/***************************************
	 * Returns the value mapping function of this space.
	 *
	 * @return The value mapping function
	 */
	public final Function<Object, T> getValueMapper()
	{
		return fValueMapper;
	}
}
