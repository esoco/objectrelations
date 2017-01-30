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

import org.obrel.core.ObjectRelations;
import org.obrel.core.RelatedObject;


/********************************************************************
 * A simple {@link ObjectSpace} implementation that is derived from {@link
 * RelatedObject} and maps the access URLs the hierarchy of it's relations.
 *
 * @author eso
 */
public class RelatableObjectSpace<T, D> extends RelatedObject
	implements ObjectSpace<T>
{
	//~ Instance fields --------------------------------------------------------

	private Function<D, T> fValueMapper;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param fValueMapper The value mapping function
	 */
	public RelatableObjectSpace(Function<D, T> fValueMapper)
	{
		this.fValueMapper = fValueMapper;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String sUrl)
	{
		ObjectRelations.urlDelete(this, sUrl);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T get(String sUrl)
	{
		return fValueMapper.evaluate(getValue(sUrl));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, T rValue)
	{
		if (fValueMapper instanceof InvertibleFunction)
		{
			((InvertibleFunction<D, T>) fValueMapper).invert(rValue);
		}

		ObjectRelations.urlPut(this, sUrl, rValue);
	}

	/***************************************
	 * Returns the value at a certain URL. This default implementation tries to
	 * cast the value to the data type of this space which may result in a
	 * {@link ClassCastException} if the value does not match the type.
	 * Subclasses should override this method to perform more extensive type
	 * checking.
	 *
	 * @param  sUrl The URL to get the value of
	 *
	 * @return The value at given URL
	 */
	@SuppressWarnings("unchecked")
	protected D getValue(String sUrl)
	{
		return (D) ObjectRelations.urlGet(this, sUrl);
	}
}
