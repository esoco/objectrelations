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
package org.obrel.type;

import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.ReflectionFuntions;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A relation type subclass for {@link Map} targets that are automatically
 * initialized to an empty map.
 *
 * @author eso
 */
public class MapType<K, V> extends RelationType<Map<K, V>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance for the use with static initialization. Instances
	 * created with this constructor MUST be declared as static constants in a
	 * class that is initialized with {@link RelationTypes#init(Class...)}.
	 *
	 * @param ordered   TRUE for an ordered map
	 * @param modifiers The optional relation type modifiers
	 */
	public MapType(boolean ordered, RelationTypeModifier... modifiers) {
		this(null, null, null, ordered, modifiers);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param name      The relation type name
	 * @param keyType   The datatype of the map keys
	 * @param valueType The datatype of the map values
	 * @param ordered   TRUE for an ordered map
	 * @param modifiers The optional relation type modifiers
	 */
	public MapType(String name, Class<K> keyType, Class<V> valueType,
		boolean ordered, RelationTypeModifier... modifiers) {
		super(name, Map.class, initialValueFunction(ordered), modifiers);

		set(MetaTypes.KEY_DATATYPE, keyType);
		set(MetaTypes.VALUE_DATATYPE, valueType);

		if (ordered) {
			set(MetaTypes.ORDERED);
		}
	}

	/**
	 * Creates the initial value function that produces either an ordered or an
	 * unordered map.
	 *
	 * @param ordered TRUE for an ordered map
	 * @return The initial value function
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Function<Object, Map<K, V>> initialValueFunction(
		boolean ordered) {
		Class<?> mapClass = ordered ? LinkedHashMap.class : HashMap.class;

		return ReflectionFuntions.newInstanceOf((Class<Map<K, V>>) mapClass);
	}
}
