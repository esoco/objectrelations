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

import de.esoco.lib.expression.Function;


/********************************************************************
 * A relation wrapper implementation that provides a readonly view of another
 * relation with a different relation type and datatype.
 */
public class RelationView<T, V> extends RelationWrapper<T, V, Function<V, T>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rViewType       The relation type of this view relation
	 * @param rViewedRelation The relation to be viewed
	 * @param fViewConversion A conversion function that produces the target
	 *                        value of the view
	 */
	RelationView(RelationType<T> rViewType,
				 Relation<V>	 rViewedRelation,
				 Function<V, T>  fViewConversion)
	{
		super(rViewType, rViewedRelation, fViewConversion);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a copy of this view. The copy will still refer to the original
	 * wrapped relation.
	 *
	 * @see RelationWrapper#copy()
	 */
	@Override
	Relation<T> copy()
	{
		return new RelationView<>(getType(),
								  getWrappedRelation(),
								  getConversion());
	}
}
