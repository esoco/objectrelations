//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.expression.Predicates;

import java.util.Collection;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.CollectionPredicates.elementOf;


/********************************************************************
 * This class defines several static methods that return standard relation
 * filters. The methods are intended to be used with the filtered methods of
 * {@link Relatable} and are therefore named with appropriate semantics. Using
 * static imports the following example code would resolve all relations with
 * the relation types {@link StandardTypes#NAME} and {@link StandardTypes#INFO}:
 * {@code rObject.getAll(withTypes(NAME,INFO));}
 *
 * @author eso
 */
public class RelationFilters
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A standard relation filter constant that selects all relations; it is
	 * simply a different name for {@link Predicates#alwaysTrue()}.
	 */
	public static final Predicate<Relation<?>> ALL_RELATIONS =
		Predicates.alwaysTrue();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private RelationFilters()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a filter for a relations with types that match a certain
	 * predicate.
	 *
	 * @param  rTypePredicate The predicate to evaluate relation types with
	 *
	 * @return A new relation filter predicate
	 */
	public static Predicate<Relation<?>> ifType(
		Predicate<RelationType<?>> rTypePredicate)
	{
		return r -> rTypePredicate.test(r.getType());
	}

	/***************************************
	 * Returns a relation filter predicate that tests for the presence of a
	 * certain relation type.
	 *
	 * @param  rType The relation type to filter
	 *
	 * @return A new relation filter predicate
	 */
	public static Predicate<Relation<?>> ifType(RelationType<?> rType)
	{
		return ifType(t -> t == rType);
	}

	/***************************************
	 * Returns a relation filter predicate that tests for the absesence of a
	 * certain relation type.
	 *
	 * @param  rType The relation type to filter
	 *
	 * @return A new relation filter predicate
	 */
	public static Predicate<Relation<?>> ifTypeNot(RelationType<?> rType)
	{
		return ifType(t -> t != rType);
	}

	/***************************************
	 * Returns a new relation filter instance that filters relations based on
	 * their annotations. Annotations are meta-relations that are set on the
	 * relations themselves, not on their parent object.
	 *
	 * @param  rAnnotationFilter The predicate to evaluate relation annotations
	 *                           with
	 *
	 * @return A new relation filter predicate
	 */
	public static Predicate<Relation<?>> withAnnotation(
		Predicate<Relation<?>> rAnnotationFilter)
	{
		return r -> r.hasRelations(rAnnotationFilter);
	}

	/***************************************
	 * Returns a new relation filter instance that evaluates a predicate on the
	 * relation target values. The result of the predicate evaluation will also
	 * be the result of the returned predicate.
	 *
	 * @param  rTargetPredicate The predicate to evaluate relation targets with
	 *
	 * @return A new relation filter predicate
	 */
	public static Predicate<Relation<?>> withTarget(
		Predicate<Object> rTargetPredicate)
	{
		return r -> rTargetPredicate.evaluate(r.getTarget());
	}

	/***************************************
	 * Returns a filter for a set of relation types.
	 *
	 * @param  rTypes The set of relation types
	 *
	 * @return A new relation filter instance
	 */
	public static Predicate<Relation<?>> withTypeIn(
		Collection<RelationType<?>> rTypes)
	{
		return ifType(elementOf(rTypes));
	}
}
