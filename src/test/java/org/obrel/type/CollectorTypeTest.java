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
package org.obrel.type;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.datatype.Pair;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertEquals;

import static org.obrel.type.StandardTypes.MAXIMUM;


/********************************************************************
 * Test of {@link CollectorType}.
 *
 * @author eso
 */
public class CollectorTypeTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of standard collector.
	 */
	@Test
	public void testCollector()
	{
		CollectorType<String> aCollectorType =
			new CollectorType<String>("test.COLLECTOR_TYPE",
									  String.class,
									  (r, v) -> v.toString(),
									  false);

		assertEquals(Arrays.asList("1", "2", "3", "1"),
					 doCollect(aCollectorType,
							   -1,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.NAME, "2"),
							   t(StandardTypes.NAME, "3"),
							   t(StandardTypes.NAME, "1")));

		assertEquals(Arrays.asList("1", "2", "3", "1"),
					 doCollect(aCollectorType,
							   -1,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.DESCRIPTION, "2"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.NAME, "1")));

		assertEquals(Arrays.asList("2", "3", "1"),
					 doCollect(aCollectorType,
							   3,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.DESCRIPTION, "2"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.NAME, "1")));
	}

	/***************************************
	 * Test of distinct collector.
	 */
	@Test
	public void testDistinctCollector()
	{
		CollectorType<String> aCollectorType =
			new CollectorType<String>("test._DISTINCT_COLLECTOR_TYPE",
									  String.class,
									  (r, v) -> v.toString(),
									  true);

		assertEquals(CollectionUtil.orderedSetOf("1", "2", "3"),
					 doCollect(aCollectorType,
							   -1,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.NAME, "2"),
							   t(StandardTypes.NAME, "3"),
							   t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("3", "2", "1"),
					 doCollect(aCollectorType,
							   -1,
							   t(StandardTypes.NAME, "3"),
							   t(StandardTypes.NAME, "2"),
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("1", "2", "3"),
					 doCollect(aCollectorType,
							   -1,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.DESCRIPTION, "2"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("3", "1"),
					 doCollect(aCollectorType,
							   2,
							   t(StandardTypes.NAME, "1"),
							   t(StandardTypes.DESCRIPTION, "2"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.INFO, "3"),
							   t(StandardTypes.NAME, "1")));
	}

	/***************************************
	 * Collects values into a collector type by setting them on a related
	 * object.
	 *
	 * @param  rCollectorType The collector relation type
	 * @param  nMaxSize       The size limit for the collection
	 * @param  rRelations     The values to collect
	 *
	 * @return The collected values
	 */
	@SafeVarargs
	final Collection<String> doCollect(
		CollectorType<String>				  rCollectorType,
		int									  nMaxSize,
		Pair<RelationType<String>, String>... rRelations)
	{
		Relatable o = new RelatedObject();

		o.init(rCollectorType);

		if (nMaxSize >= 0)
		{
			o.getRelation(rCollectorType).set(MAXIMUM, nMaxSize);
		}

		for (Pair<RelationType<String>, String> rData : rRelations)
		{
			o.set(rData.first(), rData.second());
		}

		return o.get(rCollectorType);
	}
}
