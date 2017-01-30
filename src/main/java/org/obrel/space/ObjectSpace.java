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

import java.util.NoSuchElementException;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;


/********************************************************************
 * An interface that defines hierarchical spaces of objects which can be
 * accessed with hierarchical URLs.
 *
 * @author eso
 */
public interface ObjectSpace<T> extends Relatable
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Deletes a value that is referenced by a certain URL. See the method
	 * {@link ObjectRelations#urlDelete(Relatable, String)} for details about
	 * this operation.
	 *
	 * @param  sUrl The URL to lookup the object under
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public void delete(String sUrl);

	/***************************************
	 * Gets a value object that is referenced by a certain URL. See the method
	 * {@link ObjectRelations#urlGet(Relatable, String)} for details about this
	 * operation.
	 *
	 * @param  sUrl The URL to lookup the object under
	 *
	 * @return The value referenced by the given URL
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public T get(String sUrl);

	/***************************************
	 * Stores or updates a value object at a certain URL. See the method {@link
	 * ObjectRelations#urlPut(Relatable, String, Object)} for details about this
	 * operation.
	 *
	 * @param  sUrl   The URL to lookup the object under
	 * @param  rValue The new or updated value
	 *
	 * @throws NoSuchElementException   If the URL could not be resolved
	 * @throws IllegalArgumentException If the URL doesn't resolve to a valid
	 *                                  relation type
	 */
	public void put(String sUrl, T rValue);
}
