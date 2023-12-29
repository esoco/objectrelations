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

import de.esoco.lib.property.Fluent;

/**
 * An interface that can be implemented by relatable objects that provide a
 * fluent interface. The default method {@link #with(RelationType, Object)} sets
 * a relation type and then returns the current instance so that the next method
 * can be invoked immediately (and with the corresponding syntax fluently).
 *
 * @author eso
 */
public interface FluentRelatable<T extends FluentRelatable<T>>
	extends Relatable, Fluent<T> {

	/**
	 * Sets a flag relation type and returns this instance.
	 *
	 * @param type The relation type
	 * @return This instance for fluent invocation
	 */
	default T with(RelationType<Boolean> type) {
		return _with(() -> set(type));
	}

	/**
	 * Sets an integer relation type from an int value and returns this
	 * instance.
	 *
	 * @see #with(RelationType, Object)
	 */
	default T with(RelationType<Integer> type, int value) {
		return _with(() -> set(type, Integer.valueOf(value)));
	}

	/**
	 * Sets a relation type and returns this instance for fluent invocation of
	 * additional methods. Although this method can be invoked directly it is
	 * recommended that implementing classes call this method from methods with
	 * names that provide a more concise fluent syntax.
	 *
	 * @param type  The relation type
	 * @param value The value
	 * @return This instance for fluent invocation
	 */
	default <V> T with(RelationType<V> type, V value) {
		return _with(() -> set(type, value));
	}
}
