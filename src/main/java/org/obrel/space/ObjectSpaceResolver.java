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
package org.obrel.space;

import de.esoco.lib.expression.BinaryFunction;
import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

/**
 * A binary function extension that resolves URLs in object spaces. Invoked by
 * {@link ObjectRelations#urlResolve(Relatable, String, boolean,
 * ObjectSpaceResolver)}.
 *
 * @author eso
 */
public interface ObjectSpaceResolver
	extends BinaryFunction<Relatable, RelationType<?>, Object> {

	/**
	 * Standard delete resolver.
	 */
	DeleteResolver URL_DELETE = (r, t) -> {
		r.deleteRelation(t);

		return null;
	};

	/**
	 * Standard get resolver.
	 */
	GetResolver URL_GET = (r, t) -> r.get(t);

	/**
	 * Resolves a URL in a certain {@link ObjectSpace}. Will be invoked by
	 * {@link ObjectRelations#urlResolve(Relatable, String, boolean,
	 * ObjectSpaceResolver)} for the remaining URL if the URL traversal
	 * encounters an object space element.
	 *
	 * @param space       The object space to resolve the URL in
	 * @param relativeUrl The space-relative URL to resolve
	 * @return The resolved object (NULL for none)
	 */
	Object resolve(ObjectSpace<?> space, String relativeUrl);

	/**
	 * An object space resolver implementation that invokes
	 * {@link ObjectSpace#delete(String)}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	interface DeleteResolver extends ObjectSpaceResolver {

		/**
		 * {@inheritDoc}
		 */
		@Override
		default Object resolve(ObjectSpace<?> space, String relativeUrl) {
			space.delete(relativeUrl);

			return null;
		}
	}

	/**
	 * An object space resolver implementation that invokes
	 * {@link ObjectSpace#get(String)}.
	 *
	 * @author eso
	 */
	@FunctionalInterface
	interface GetResolver extends ObjectSpaceResolver {

		/**
		 * {@inheritDoc}
		 */
		@Override
		default Object resolve(ObjectSpace<?> space, String relativeUrl) {
			return space.get(relativeUrl);
		}
	}

	/**
	 * An object space resolver implementation that invokes
	 * {@link ObjectSpace#delete(String)}.
	 *
	 * @author eso
	 */
	class PutResolver<T> implements ObjectSpaceResolver {

		private final T value;

		/**
		 * Creates a new instance.
		 *
		 * @param value The value to put
		 */
		public PutResolver(T value) {
			this.value = value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate(Relatable relatable, RelationType<?> type) {
			Object value = getValue();

			if (!type.getTargetType().isAssignableFrom(value.getClass())) {
				String message =
					String.format("Invalid value for type '%s': %s", type,
						value);

				throw new IllegalArgumentException(message);
			}

			relatable.set((RelationType<Object>) type, value);

			return null;
		}

		/**
		 * Returns the value of this instance.
		 *
		 * @return The put value
		 */
		public T getValue() {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object resolve(ObjectSpace<?> space, String relativeUrl) {
			((ObjectSpace<Object>) space).put(relativeUrl, value);

			return null;
		}
	}
}
