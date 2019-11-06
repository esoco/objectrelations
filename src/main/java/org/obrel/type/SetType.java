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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;


/********************************************************************
 * A relation type subclass for {@link Set} targets that are automatically
 * initialized to an empty set.
 *
 * @author eso
 */
public class SetType<T> extends RelationType<Set<T>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for the use with static initialization. Instances
	 * created with this constructor MUST be declared as static constants in a
	 * class that is initialized with {@link RelationTypes#init(Class...)}.
	 *
	 * @param bOrdered   TRUE for an ordered map
	 * @param rModifiers The optional relation type modifiers
	 */
	public SetType(boolean bOrdered, RelationTypeModifier... rModifiers)
	{
		this(null, null, bOrdered, rModifiers);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName        The relation type name (NULL for automatic
	 *                     initialization of static relation types)
	 * @param rElementType The datatype of the set elements
	 * @param bOrdered     TRUE for an ordered map
	 * @param rModifiers   The optional relation type modifiers
	 */
	public SetType(String				   sName,
				   Class<T>				   rElementType,
				   boolean				   bOrdered,
				   RelationTypeModifier... rModifiers)
	{
		super(sName, Set.class, initialValueFunction(bOrdered), rModifiers);

		set(MetaTypes.ELEMENT_DATATYPE, rElementType);

		if (bOrdered)
		{
			set(MetaTypes.ORDERED);
		}
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates the initial value function that produces either an ordered or an
	 * unordered set.
	 *
	 * @param  bOrdered TRUE for an ordered set
	 *
	 * @return The initial value function
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<Object, Set<T>> initialValueFunction(
		boolean bOrdered)
	{
		Class<?> rSetClass = bOrdered ? LinkedHashSet.class : HashSet.class;

		return ReflectionFuntions.newInstanceOf((Class<Set<T>>) rSetClass);
	}
}
