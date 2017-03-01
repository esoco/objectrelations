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
package org.obrel.core;

/********************************************************************
 * A subclass of related object that is intended to hold relation-based
 * parameters. It implements the relation builder interface so that relations
 * can easily be added to an instance. It also provides the factory method
 * {@link #params(RelationData)} which returns a new instance initialized with
 * the given parameters.
 *
 * @author eso
 */
public class Params extends RelatedObject implements RelationBuilder<Params>
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method to create a new instance of this class that is initialized
	 * with the given relations.
	 *
	 * @param  rRelations The initial relations of this instance
	 *
	 * @return The new instance
	 */
	public static Params params(RelationData<?> rRelations)
	{
		return new Params().with(rRelations);
	}
}
