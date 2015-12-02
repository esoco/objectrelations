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
package de.esoco.lib.expression;

/********************************************************************
 * An interface for objects that implement access to certain elements of other
 * objects. The descriptor of the accessed elements can be queried with the
 * {@link #getElementDescriptor()} method.
 *
 * @author eso
 */
public interface ElementAccess<E>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the element descriptor of this instance.
	 *
	 * @return The element descriptor
	 */
	public E getElementDescriptor();
}
