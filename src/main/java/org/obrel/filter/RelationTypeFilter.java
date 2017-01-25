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
package org.obrel.filter;

import de.esoco.lib.expression.BinaryFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.predicate.AbstractBinaryPredicate;

import org.obrel.core.Relation;
import org.obrel.core.RelationType;


/********************************************************************
 * A predicate implementation that filters relations based on the evaluation of
 * a predicate on their type.
 *
 * @author eso
 */
public class RelationTypeFilter
	extends AbstractBinaryPredicate<Relation<?>, Predicate<RelationType<?>>>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see AbstractBinaryPredicate#AbstractBinaryPredicate(Object, String,
	 *      boolean)
	 */
	public RelationTypeFilter(Predicate<RelationType<?>> rTypePredicate)
	{
		super(rTypePredicate, "RelationType %s");
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see BinaryFunction#evaluate(Object, Object)
	 */
	@Override
	public Boolean evaluate(
		Relation<?>				   rRelation,
		Predicate<RelationType<?>> rTypePredicate)
	{
		return rTypePredicate.evaluate(rRelation.getType());
	}
}
