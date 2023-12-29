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

import static de.esoco.lib.datatype.Pair.t;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.obrel.type.StandardTypes.MAXIMUM;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.datatype.Pair;

/**
 * Test of {@link CollectorType}.
 *
 * @author eso
 */
public class CollectorTypeTest {
	/**
	 * Test of standard collector.
	 */
	@Test
	public void testCollector() {
		CollectorType<String> collectorType =
			new CollectorType<String>("test.COLLECTOR_TYPE", String.class,
				(r, v) -> v.toString(), false);

		assertEquals(Arrays.asList("1", "2", "3", "1"),
			doCollect(collectorType, -1, t(StandardTypes.NAME, "1"),
				t(StandardTypes.NAME, "2"), t(StandardTypes.NAME, "3"),
				t(StandardTypes.NAME, "1")));

		assertEquals(Arrays.asList("1", "2", "3", "1"),
			doCollect(collectorType, -1, t(StandardTypes.NAME, "1"),
				t(StandardTypes.DESCRIPTION, "2"), t(StandardTypes.INFO, "3"),
				t(StandardTypes.NAME, "1")));

		assertEquals(Arrays.asList("2", "3", "1"),
			doCollect(collectorType, 3, t(StandardTypes.NAME, "1"),
				t(StandardTypes.DESCRIPTION, "2"), t(StandardTypes.INFO, "3"),
				t(StandardTypes.NAME, "1")));
	}

	/**
	 * Test of distinct collector.
	 */
	@Test
	public void testDistinctCollector() {
		CollectorType<String> collectorType =
			new CollectorType<String>("test._DISTINCT_COLLECTOR_TYPE",
				String.class, (r, v) -> v.toString(), true);

		assertEquals(CollectionUtil.orderedSetOf("1", "2", "3"),
			doCollect(collectorType, -1, t(StandardTypes.NAME, "1"),
				t(StandardTypes.NAME, "2"), t(StandardTypes.NAME, "3"),
				t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("3", "2", "1"),
			doCollect(collectorType, -1, t(StandardTypes.NAME, "3"),
				t(StandardTypes.NAME, "2"), t(StandardTypes.NAME, "1"),
				t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("1", "2", "3"),
			doCollect(collectorType, -1, t(StandardTypes.NAME, "1"),
				t(StandardTypes.DESCRIPTION, "2"), t(StandardTypes.INFO, "3"),
				t(StandardTypes.INFO, "3"), t(StandardTypes.NAME, "1")));

		assertEquals(CollectionUtil.orderedSetOf("3", "1"),
			doCollect(collectorType, 2, t(StandardTypes.NAME, "1"),
				t(StandardTypes.DESCRIPTION, "2"), t(StandardTypes.INFO, "3"),
				t(StandardTypes.INFO, "3"), t(StandardTypes.NAME, "1")));
	}

	/**
	 * Collects values into a collector type by setting them on a related
	 * object.
	 *
	 * @param collectorType The collector relation type
	 * @param maxSize       The size limit for the collection
	 * @param relations     The values to collect
	 * @return The collected values
	 */
	@SafeVarargs
	final Collection<String> doCollect(CollectorType<String> collectorType,
		int maxSize, Pair<RelationType<String>, String>... relations) {
		Relatable o = new RelatedObject();

		o.init(collectorType);

		if (maxSize >= 0) {
			o.getRelation(collectorType).set(MAXIMUM, maxSize);
		}

		for (Pair<RelationType<String>, String> data : relations) {
			o.set(data.first(), data.second());
		}

		return o.get(collectorType);
	}
}
