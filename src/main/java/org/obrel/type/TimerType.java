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

/**
 * A relation type that returns the milliseconds since the creation of a
 * relation with this type.
 *
 * @author eso
 */
public class TimerType extends RelationType<Long> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 *
	 * @param name      The relation type name
	 * @param modifiers The optional modifiers
	 */
	@SuppressWarnings("boxing")
	public TimerType(String name, RelationTypeModifier... modifiers) {
		super(name, Long.class, o -> System.currentTimeMillis(), modifiers);
	}

	/**
	 * Creates a new partially initialized timer type for use in conjunction
	 * with {@link RelationTypes#init(Class...)}.
	 *
	 * @param modifiers The optional modifiers
	 * @return The new timer type
	 */
	public static TimerType newTimer(RelationTypeModifier... modifiers) {
		return new TimerType(null, modifiers);
	}

	/**
	 * @see RelationType#addRelation(Relatable, Relation)
	 */
	@Override
	@SuppressWarnings({ "boxing", "serial" })
	protected Relation<Long> addRelation(Relatable parent,
		Relation<Long> relation) {
		return new DirectRelation<Long>(this, relation.getTarget()) {
			@Override
			public Long getTarget() {
				return System.currentTimeMillis() - super.getTarget();
			}
		};
	}
}
