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

import de.esoco.lib.expression.InvertibleFunction;

import org.obrel.core.ObjectRelations;


/********************************************************************
 * An extension of {@link SimpleObjectSpace} that adds implementations of the
 * methods {@link #put(String, Object)} and {@link #delete(String)}.
 *
 * @author eso
 */
public class MutableObjectSpace<T> extends SimpleObjectSpace<T>
	implements ObjectSpace<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with a certain value mapping function.
	 *
	 * @param fValueMapper The value mapping function
	 */
	public MutableObjectSpace(InvertibleFunction<Object, T> fValueMapper)
	{
		super(fValueMapper);
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
	public void put(String sUrl, T rValue)
	{
		Object rInvertedValue =
			((InvertibleFunction<Object, T>) getValueMapper()).invert(rValue);

		ObjectRelations.urlPut(this, sUrl, rInvertedValue);
	}
}
