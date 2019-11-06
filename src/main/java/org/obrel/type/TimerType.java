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

import org.obrel.core.DirectRelation;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;


/********************************************************************
 * A relation type that returns the milliseconds since the creation of a
 * relation with this type.
 *
 * @author eso
 */
public class TimerType extends RelationType<Long>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sName      The relation type name
	 * @param rModifiers The optional modifiers
	 */
	@SuppressWarnings("boxing")
	public TimerType(String sName, RelationTypeModifier... rModifiers)
	{
		super(sName, Long.class, o -> System.currentTimeMillis(), rModifiers);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new partially initialized timer type for use in conjunction
	 * with {@link RelationTypes#init(Class...)}.
	 *
	 * @param  rModifiers The optional modifiers
	 *
	 * @return The new timer type
	 */
	public static TimerType newTimer(RelationTypeModifier... rModifiers)
	{
		return new TimerType(null, rModifiers);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	@SuppressWarnings({ "boxing", "serial" })
	protected Relation<Long> addRelation(
		Relatable	   rParent,
		Relation<Long> rRelation)
	{
		return new DirectRelation<Long>(this, rRelation.getTarget())
		{
			@Override
			public Long getTarget()
			{
				return System.currentTimeMillis() - super.getTarget();
			}
		};
	}
}
