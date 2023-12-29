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
package de.esoco.lib.expression.function;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An wrapper for a relatable object and a certain relation type that implements
 * the {@link Consumer} and {@link Supplier} interfaces for functional access to
 * the relation.
 *
 * @author eso
 */
public class RelationAccessor<T> implements Consumer<T>, Supplier<T> {

	private final Relatable relatable;

	private final RelationType<T> type;

	/**
	 * Creates a new instance.
	 */
	public RelationAccessor(Relatable relatable, RelationType<T> type) {
		this.relatable = relatable;
		this.type = type;
	}

	/**
	 * Implemented to set a value in the target relation of the wrapped
	 * relatable.
	 *
	 * @param value The new relation value
	 */
	@Override
	public void accept(T value) {
		relatable.set(type, value);
	}

	/**
	 * Implemented to return the value of the target relation of the wrapped
	 * relatable.
	 *
	 * @return The relation value
	 */
	@Override
	public T get() {
		return relatable.get(type);
	}
}
