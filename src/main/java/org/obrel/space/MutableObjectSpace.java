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

import org.obrel.core.ObjectRelations;
import org.obrel.space.ObjectSpaceResolver.PutResolver;

import static org.obrel.space.ObjectSpaceResolver.URL_DELETE;


/********************************************************************
 * An extension of {@link SimpleObjectSpace} that adds implementations of the
 * methods {@link #put(String, Object)} and {@link #delete(String)}.
 *
 * @author eso
 */
public class MutableObjectSpace<T> extends SimpleObjectSpace<T>
	implements ObjectSpace<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String sUrl)
	{
		ObjectRelations.urlDo(this, sUrl, false, URL_DELETE);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void put(String sUrl, T rValue)
	{
		ObjectRelations.urlDo(this, sUrl, false, new PutResolver<T>(rValue));
	}
}
