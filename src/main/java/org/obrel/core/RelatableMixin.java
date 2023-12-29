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

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;

import java.util.List;

/**
 * A {@link Relatable} sub-interface that can be used to provide relation
 * support for existing classes that cannot inherit from {@link RelatedObject}.
 * All methods have default implementations, so it is sufficient to simply
 * implement this interface to get relation support.
 *
 * <p>The only method added by this mixin is {@link #getRelationContainer()}. By
 * default it returns the result of {@link ObjectRelations#getRelatable(Object)}
 * but implementations may override this to return an explicit {@link Relatable}
 * instance for better lookup performance (e.g. an {@link RelatedObject} stored
 * in a field).</p>
 *
 * @author eso
 */
public interface RelatableMixin extends Relatable {

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void deleteRelation(Relation<?> relation) {
		getRelationContainer().deleteRelation(relation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <T> T get(RelationType<T> type) {
		return getRelationContainer().get(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <T> Relation<T> getRelation(RelationType<T> type) {
		return getRelationContainer().getRelation(type);
	}

	/**
	 * Returns the relation container of this instance. By default it returns
	 * {@link ObjectRelations#getRelationContainer(Object, boolean)} for this
	 * instance but implementations may override this to return a different
	 * {@link Relatable} (typically some {@link RelatedObject} instance).
	 *
	 * @return The relation container
	 */
	default Relatable getRelationContainer() {
		return ObjectRelations.getRelatable(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default List<Relation<?>> getRelations(
		Predicate<? super Relation<?>> filter) {
		return getRelationContainer().getRelations(filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <T> Relation<T> set(RelationType<T> type, T target) {
		return getRelationContainer().set(type, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <T, I> Relation<T> set(RelationType<T> type,
		Function<I, T> targetResolver, I intermediateTarget) {
		return getRelationContainer().set(type, targetResolver,
			intermediateTarget);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <T, D> TransformedRelation<T, D> transform(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		return getRelationContainer().transform(type, transformation);
	}
}
