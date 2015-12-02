//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package org.obrel.filter;

import java.util.Comparator;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;


/********************************************************************
 * A {@link Comparator} implementation that compares relation targets. The
 * relation targets must not be NULL or else a {@link NullPointerException} will
 * be thrown.
 *
 * @author eso
 */
public class RelationComparator<T extends Relatable> implements Comparator<T>
{
	//~ Instance fields --------------------------------------------------------

	private final RelationType<Comparable<Object>>[] rRelationTypes;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that compares a certain relation of relatable
	 * objects.
	 *
	 * @param rCompareRelationTypes The relation type to compare the target
	 *                              values of
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public RelationComparator(
		RelationType<? extends Comparable<?>>... rCompareRelationTypes)
	{
		// cast is safe because both target values will be of the same type
		rRelationTypes =
			(RelationType<Comparable<Object>>[]) rCompareRelationTypes;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see Comparator#compare(Object, Object)
	 */
	@Override
	public int compare(T rFirst, T rSecond)
	{
		int nComparison = 0;

		for (RelationType<Comparable<Object>> rType : rRelationTypes)
		{
			nComparison = rFirst.get(rType).compareTo(rSecond.get(rType));

			if (nComparison != 0)
			{
				break;
			}
		}

		return nComparison;
	}
}
