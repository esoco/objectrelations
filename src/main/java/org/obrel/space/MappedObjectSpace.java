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
import de.esoco.lib.expression.InvertibleFunction;

import org.obrel.core.RelatedObject;


/********************************************************************
 * An {@link ObjectSpace} implementation that maps values from another object
 * space. The conversion between the generic type of this space and that of the
 * wrapped object space is performed by a value mapping function that must be
 * provided to the constructor. If the mapping function also implements the
 * {@link InvertibleFunction} interface the mapping will be bi-directional and
 * allows write access through the method {@link #put(String, Object)} too (if
 * the wrapped space allows modifications). Otherwise only read access through
 * {@link #get(String)} will be possible (and {@link #delete(String)}, if
 * supported by the wrapped space).
 *
 * @author eso
 */
public class MappedObjectSpace<T, O> extends RelatedObject
	implements ObjectSpace<T>
{
	//~ Instance fields --------------------------------------------------------

	private ObjectSpace<O>			 rWrappedSpace;
	private Function<O, T>			 fValueMapper;
	private InvertibleFunction<O, T> fInvertibleMapper = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param rWrappedSpace The target object space to map values from and to
	 * @param fValueMapper  The value mapping function
	 */
	public MappedObjectSpace(
		ObjectSpace<O> rWrappedSpace,
		Function<O, T> fValueMapper)
	{
		this.rWrappedSpace = rWrappedSpace;
		this.fValueMapper  = fValueMapper;

		if (fValueMapper instanceof InvertibleFunction)
		{
			fInvertibleMapper = (InvertibleFunction<O, T>) fValueMapper;
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String sUrl)
	{
		rWrappedSpace.delete(sUrl);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T get(String sUrl)
	{
		return fValueMapper.evaluate(rWrappedSpace.get(sUrl));
	}

	/***************************************
	 * Returns the value mapping function of this space.
	 *
	 * @return The value mapping function
	 */
	public final Function<O, T> getValueMapper()
	{
		return fValueMapper;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, T rValue)
	{
		if (fInvertibleMapper != null)
		{
			rWrappedSpace.put(sUrl, fInvertibleMapper.invert(rValue));
		}
		else
		{
			throw new UnsupportedOperationException("Value mapping not invertible");
		}
	}
}
