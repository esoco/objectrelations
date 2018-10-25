//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.InvertibleFunction;


/********************************************************************
 * A relation wrapper implementation that wraps another relation with a
 * different relation type with the same generic types. That allows to write to
 * the wrapper like to the original relation.
 */
public class RelationAlias<T, A>
	extends RelationWrapper<T, A, InvertibleFunction<A, T>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent          The parent of this alias
	 * @param rAliasType       The relation type of this alias relation
	 * @param rAliasedRelation The relation to be aliased
	 * @param fAliasConversion A conversion function that produces the target
	 *                         value of the alias and can be inverted for the
	 *                         setting of new targets
	 */
	RelationAlias(Relatable				   rParent,
				  RelationType<T>		   rAliasType,
				  Relation<A>			   rAliasedRelation,
				  InvertibleFunction<A, T> fAliasConversion)
	{
		super(rParent, rAliasType, rAliasedRelation, fAliasConversion);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Copies this alias to another object. The copy will still refer to the
	 * original wrapped relation.
	 *
	 * @see RelationWrapper#copyTo(Relatable)
	 */
	@Override
	Relation<T> copyTo(Relatable rTarget)
	{
		return getWrappedRelation().aliasAs(
			getType(),
			rTarget,
			getConversion());
	}

	/***************************************
	 * Updates the target object on the aliased relation .
	 *
	 * @see Relation#updateTarget(Object)
	 */
	@Override
	void setTarget(T rNewTarget)
	{
		Relation<A> rWrapped = getWrappedRelation();
		A		    rTarget  = getConversion().invert(rNewTarget);

		// check state of target type too to prevent illegal modifications
		rWrapped.getType().checkUpdateAllowed();
		rWrapped.getType().prepareRelationUpdate(rWrapped, rTarget);
		rWrapped.updateTarget(rTarget);
	}
}
