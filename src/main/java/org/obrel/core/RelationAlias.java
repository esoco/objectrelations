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

/**
 * A relation wrapper implementation that wraps another relation with a
 * different relation type with the same generic types. That allows to write to
 * the wrapper like to the original relation.
 */
public class RelationAlias<T, A>
	extends RelationWrapper<T, A, InvertibleFunction<A, T>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 *
	 * @param parent          The parent of this alias
	 * @param aliasType       The relation type of this alias relation
	 * @param aliasedRelation The relation to be aliased
	 * @param aliasConversion A conversion function that produces the target
	 *                        value of the alias and can be inverted for the
	 *                        setting of new targets
	 */
	RelationAlias(Relatable parent, RelationType<T> aliasType,
		Relation<A> aliasedRelation,
		InvertibleFunction<A, T> aliasConversion) {
		super(parent, aliasType, aliasedRelation, aliasConversion);
	}

	/**
	 * Copies this alias to another object. The copy will still refer to the
	 * original wrapped relation.
	 *
	 * @see RelationWrapper#copyTo(Relatable)
	 */
	@Override
	Relation<T> copyTo(Relatable target) {
		return getWrappedRelation().aliasAs(getType(), target,
			getConversion());
	}

	/**
	 * Updates the target object on the aliased relation .
	 *
	 * @see Relation#updateTarget(Object)
	 */
	@Override
	void setTarget(T newTarget) {
		Relation<A> wrapped = getWrappedRelation();
		A target = getConversion().invert(newTarget);

		// check state of target type too to prevent illegal modifications
		wrapped.getType().checkUpdateAllowed();
		wrapped.getType().prepareRelationUpdate(wrapped, target);
		wrapped.updateTarget(target);
	}
}
