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
 * An event listener interface that will be notified of the de-/selection of
 * objects of a certain type.
 *
 * @author eso
 */
public interface SelectionListener<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * This method will be invoked when the selection of the watched object
	 * changes.
	 *
	 * @param rNewSelection The new selection or NULL for no selection
	 */
	public void selectionChanged(T rNewSelection);
}
