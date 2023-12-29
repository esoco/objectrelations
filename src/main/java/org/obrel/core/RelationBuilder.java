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
package org.obrel.core;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;

import org.obrel.type.MetaTypes;

/**
 * An interface for {@link Relatable} implementations that provides default
 * methods for classes that need builder pattern relation setting methods. The
 * {@link #with(RelationType, Object) with(...)} methods invoke the method
 * {@link Relatable#set(RelationType, Object)} but return the instance of the
 * interface implementation (i.e. the target object of the {@code with()} call).
 * This allows to concatenate multiple such calls to build a relatable object
 * that contains different relation with a single invocation. The generic type
 * of an implementation must be the implementation itself, similar to
 * self-referencing enums.
 *
 * @author eso
 */
public interface RelationBuilder<R extends RelationBuilder<R>>
	extends FluentRelatable<R> {
	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * A builder pattern method to annotate a relation. Queries the relation
	 * with the first type (which must exist) and annotates it with the second
	 * type and the given value.
	 *
	 * @param type           The type of the relation to annotate
	 * @param annotationType The relation type of the annotation
	 * @param value          The value to annotate the relation with
	 * @return This instance for concatenation
	 */
	default <T> R annotate(RelationType<?> type,
		RelationType<T> annotationType,
		T value) {
		return _with(() -> {
			Relation<?> relation = getRelation(type);

			if (relation == null) {
				throw new IllegalArgumentException(
					"No relation with type " + type);
			}

			relation.annotate(annotationType, value);
		});
	}

	/**
	 * Seals this instance by setting the flag {@link MetaTypes#IMMUTABLE} so
	 * that the relations cannot be modified anymore.
	 */
	default void seal() {
		set(MetaTypes.IMMUTABLE);
	}

	/**
	 * Invokes {@link Relatable#set(RelationData...)} and returns this
	 * instance.
	 *
	 * @param relations The relations to set
	 * @return This instance for concatenation
	 */
	default R with(RelationData<?>... relations) {
		return _with(() -> set(relations));
	}

	/**
	 * Builder method for transformed relations.
	 *
	 * @see #transform(RelationType, InvertibleFunction)
	 */
	default <T, D> R with(RelationType<T> type,
		InvertibleFunction<T, D> transformation) {
		return _with(() -> transform(type, transformation));
	}

	/**
	 * Builder method for intermediate relations.
	 *
	 * @see #set(RelationType, Function, Object)
	 */
	default <T, I> R with(RelationType<T> type, Function<I, T> targetResolver,
		I intermediateTarget) {
		return _with(() -> set(type, targetResolver, intermediateTarget));
	}
}
