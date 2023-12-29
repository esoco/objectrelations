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
package org.obrel.space;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

/**
 * Test of basic object space functionality.
 *
 * @author eso
 */
public class ObjectSpaceTest {
	private static final RelationType<ObjectSpace<Object>> SUBSPACE1 =
		RelationTypes.newType();

	private static final RelationType<ObjectSpace<Object>> SUBSPACE2 =
		RelationTypes.newType();

	static {
		RelationTypes.init(ObjectSpaceTest.class, StandardTypes.class);
	}

	private ObjectSpace<Object> testSpace;

	private ObjectSpace<Object> aSubSpace1;

	private ObjectSpace<Object> aSubSpace2;

	/**
	 * Creates the test object space.
	 */
	@BeforeEach
	public void setup() {
		testSpace = new RelationSpace<>(true);
		aSubSpace1 = new RelationSpace<>(true);
		aSubSpace2 = new RelationSpace<>(true);

		testSpace.set(SUBSPACE1, aSubSpace1);
		testSpace.set(SUBSPACE2, aSubSpace2);

		testSpace.put("name", "Test");
		testSpace.put("port", 1234);
		testSpace.put("subspace1/name", "Sub1");
		testSpace.put("subspace1/port", 1111);
		testSpace.put("subspace2/name", "Sub2");
		testSpace.put("subspace2/port", 2222);
	}

	/**
	 * Test of {@link ObjectSpace#delete(String)}
	 */
	@Test
	public void testDelete() {
		testSpace.delete("subspace1/name");
		testSpace.delete("subspace1/port");
		assertFalse(aSubSpace1.hasRelation(NAME));
		assertFalse(aSubSpace1.hasRelation(PORT));

		testSpace.delete("subspace1");
		assertFalse(testSpace.hasRelation(SUBSPACE1));
		assertTrue(testSpace.hasRelation(SUBSPACE2));

		try {
			testSpace.get("subspace1/name");
			fail();
		} catch (Exception e) {
			// expected
		}
	}

	/**
	 * Test of {@link ObjectSpace#get(String)}
	 */
	@Test
	public void testGet() {
		assertEquals("Test", testSpace.get("name"));
		assertEquals(Integer.valueOf(1234), testSpace.get("port"));
		assertEquals("Sub1", testSpace.get("subspace1/name"));
		assertEquals(Integer.valueOf(1111), testSpace.get("subspace1/port"));
		assertEquals("Sub2", testSpace.get("subspace2/name"));
		assertEquals(Integer.valueOf(2222), testSpace.get("subspace2/port"));
	}
}
