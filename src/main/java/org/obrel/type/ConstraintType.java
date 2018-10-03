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
package org.obrel.type;

import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Predicate;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationEvent;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;


/********************************************************************
 * A special property relation type that implements constraints on relations.
 *
 * @author eso
 */
public class ConstraintType<T> extends RelationType<T>
	implements EventHandler<RelationEvent<?>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private final Predicate<Object> rTargetPredicate;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName            The name of the constraint type
	 * @param rDatatype        The datatype of the constraint value
	 * @param rTargetPredicate The predicate to apply to relation targets
	 * @param rFlags           The optional relation type flags
	 */
	public ConstraintType(String				  sName,
						  Class<? super T>		  rDatatype,
						  Predicate<Object>		  rTargetPredicate,
						  RelationTypeModifier... rFlags)
	{
		super(sName, rDatatype, rFlags);
		this.rTargetPredicate = rTargetPredicate;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Evaluates the given event with the predicate that is stored under this
	 * type and throws an IllegalArgumentException if the evaluation yields
	 * FALSE.
	 *
	 * @param  rEvent The relation event
	 *
	 * @throws IllegalArgumentException If the predicate evaluation yields FALSE
	 */
	@Override
	@SuppressWarnings({ "boxing" })
	public void handleEvent(RelationEvent<?> rEvent)
	{
		if (!rTargetPredicate.evaluate(rEvent.getElement().getTarget()))
		{
			throw new IllegalArgumentException();
		}
	}

	/***************************************
	 * Overridden to add this instance as a relation listener to the target
	 * object of the new relation.
	 *
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	protected Relation<T> addRelation(Relatable rParent, Relation<T> rRelation)
	{
		super.addRelation(rParent, rRelation);

		rParent.get(ListenerTypes.RELATION_LISTENERS).add(this);

		return rRelation;
	}

	/***************************************
	 * Overridden to remove this instance as an relation listener from the
	 * target object of the deleted relation.
	 *
	 * @see RelationType#deleteRelation(Relatable, Relation)
	 */
	@Override
	protected void deleteRelation(Relatable rParent, Relation<?> rRelation)
	{
		rParent.get(ListenerTypes.RELATION_LISTENERS).remove(this);
		super.deleteRelation(rParent, rRelation);
	}
}
