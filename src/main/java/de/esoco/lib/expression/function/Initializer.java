//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.function;

/********************************************************************
 * An interface for the initialization of objects.
 *
 * @author eso
 */
public interface Initializer<T>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Initializes the given object.
	 *
	 * @param  rInitObject The object to initialize
	 *
	 * @throws Exception If the initialization fails
	 */
	public void init(T rInitObject) throws Exception;
}
