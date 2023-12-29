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

import de.esoco.lib.datatype.Pair;

/**
 * A type-safe pair of a relation type and an associated value.
 *
 * @author eso
 */
public class RelationData<T> extends Pair<RelationType<T>, T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 *
	 * @param type  The relation type
	 * @param value The relation value
	 */
	public RelationData(RelationType<T> type, T value) {
		super(type, value);
	}

	/**
	 * Factory method to create a new instance. Intended to be used with static
	 * imports.
	 *
	 * @param type  The relation type
	 * @param value The relation value
	 * @return A new relation data instance
	 */
	public static <T> RelationData<T> r(RelationType<T> type, T value) {
		return new RelationData<T>(type, value);
	}

	/**
	 * Sets the relation defined by this instance on the given object.
	 *
	 * @param object The target relatable
	 */
	public void applyTo(Relatable object) {
		object.set(first(), second());
	}
}
