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
package org.obrel.space;

import org.obrel.core.ObjectRelations;
import org.obrel.core.RelatedObject;
import org.obrel.space.ObjectSpaceResolver.PutResolver;
import org.obrel.type.StandardTypes;

import static org.obrel.space.ObjectSpaceResolver.URL_DELETE;
import static org.obrel.space.ObjectSpaceResolver.URL_GET;

/**
 * An {@link ObjectSpace} implementation that is based on {@link RelatedObject}.
 * It maps access URLs to the hierarchy of it's relations.
 *
 * @author eso
 */
public class RelationSpace<T> extends RelatedObject implements ObjectSpace<T> {

	private final boolean modificationAllowed;

	/**
	 * Creates a new instance that doesn't allow modifications through the
	 * methods {@link #put(String, Object)} or {@link #delete(String)}.
	 */
	public RelationSpace() {
		this(false);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param modificationAllowed TRUE if modifications through the methods
	 *                            {@link #put(String, Object)} or {@link
	 *                            #delete(String)} are allowed
	 */
	public RelationSpace(boolean modificationAllowed) {
		this.modificationAllowed = modificationAllowed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String url) {
		if (modificationAllowed) {
			ObjectRelations.urlResolve(this, url, false, URL_DELETE);
		} else {
			throw new UnsupportedOperationException(
				"Modification not " + "allowed");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T get(String url) {
		return (T) ObjectRelations.urlResolve(this, url, false, URL_GET);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String url, T value) {
		if (modificationAllowed) {
			ObjectRelations.urlResolve(this, url, false,
				new PutResolver<T>(value));
		} else {
			throw new UnsupportedOperationException(
				"Modification not " + "allowed");
		}
	}

	/**
	 * Overridden to return the value of the {@link StandardTypes#NAME}
	 * relation.
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return get(StandardTypes.NAME);
	}
}
