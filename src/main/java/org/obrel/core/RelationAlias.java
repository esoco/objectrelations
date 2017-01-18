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
 * A relation wrapper implementation that wraps another relation with a
 * different relation type with the same generic types. That allows to write to
 * the wrapper like to the original relation.
 */
public class RelationAlias<T> extends RelationWrapper<T>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rAliasType       The relation type of this alias relation
	 * @param rAliasedRelation The relation to be aliased
	 */
	RelationAlias(RelationType<T> rAliasType, Relation<T> rAliasedRelation)
	{
		super(rAliasType, rAliasedRelation);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Updates the target object on the aliased relation if the type of this
	 * alias is not final.
	 *
	 * @see Relation#updateTarget(Object)
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		@SuppressWarnings("unchecked")
		Relation<T> rWrapped = (Relation<T>) getWrappedRelation();

		// check state of target type too to prevent illegal modifications
		rWrapped.getType().checkUpdateAllowed();
		rWrapped.getType().prepareRelationUpdate(rWrapped, rNewTarget);
		rWrapped.updateTarget(rNewTarget);
	}
}
