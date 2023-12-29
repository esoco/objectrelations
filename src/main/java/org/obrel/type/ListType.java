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

import java.util.ArrayList;
import java.util.List;

/**
 * A relation type subclass for {@link List} targets that are automatically
 * initialized to an empty list.
 *
 * @author eso
 */
public class ListType<T> extends RelationType<List<T>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance for the use with static initialization. Instances
	 * created with this constructor MUST be declared as static constants in a
	 * class that is initialized with {@link RelationTypes#init(Class...)}.
	 *
	 * @param modifiers The optional relation type modifiers
	 */
	public ListType(RelationTypeModifier... modifiers) {
		this(null, null, modifiers);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param name        The relation type name (NULL for automatic
	 *                    initialization of static relation types)
	 * @param elementType The datatype of the list elements (can also be NULL
	 *                    for static initialization)
	 * @param modifiers   The optional relation type modifiers
	 */
	public ListType(String name, Class<T> elementType,
		RelationTypeModifier... modifiers) {
		super(name, List.class, initialValueFunction(), modifiers);

		annotate(MetaTypes.ELEMENT_DATATYPE, elementType);
	}

	/**
	 * Creates the initial value function that produces the list instance.
	 *
	 * @return The initial value function
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<Object, List<T>> initialValueFunction() {
		Class<List<T>> listClass = (Class<List<T>>) (Class<?>) ArrayList.class;

		return ReflectionFuntions.newInstanceOf(listClass);
	}
}
