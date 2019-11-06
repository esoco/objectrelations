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

import org.junit.Before;
import org.junit.Test;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PORT;


/********************************************************************
 * Test of basic object space functionality.
 *
 * @author eso
 */
public class ObjectSpaceTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<ObjectSpace<Object>> SUBSPACE1 =
		RelationTypes.newType();
	private static final RelationType<ObjectSpace<Object>> SUBSPACE2 =
		RelationTypes.newType();

	static
	{
		RelationTypes.init(ObjectSpaceTest.class, StandardTypes.class);
	}

	//~ Instance fields --------------------------------------------------------

	private ObjectSpace<Object> aTestSpace;
	private ObjectSpace<Object> aSubSpace1;
	private ObjectSpace<Object> aSubSpace2;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Creates the test object space.
	 */
	@Before
	@SuppressWarnings("boxing")
	public void setup()
	{
		aTestSpace = new RelationSpace<>(true);
		aSubSpace1 = new RelationSpace<>(true);
		aSubSpace2 = new RelationSpace<>(true);

		aTestSpace.set(SUBSPACE1, aSubSpace1);
		aTestSpace.set(SUBSPACE2, aSubSpace2);

		aTestSpace.put("name", "Test");
		aTestSpace.put("port", 1234);
		aTestSpace.put("subspace1/name", "Sub1");
		aTestSpace.put("subspace1/port", 1111);
		aTestSpace.put("subspace2/name", "Sub2");
		aTestSpace.put("subspace2/port", 2222);
	}

	/***************************************
	 * Test of {@link ObjectSpace#delete(String)}
	 */
	@Test
	public void testDelete()
	{
		aTestSpace.delete("subspace1/name");
		aTestSpace.delete("subspace1/port");
		assertFalse(aSubSpace1.hasRelation(NAME));
		assertFalse(aSubSpace1.hasRelation(PORT));

		aTestSpace.delete("subspace1");
		assertFalse(aTestSpace.hasRelation(SUBSPACE1));
		assertTrue(aTestSpace.hasRelation(SUBSPACE2));

		try
		{
			aTestSpace.get("subspace1/name");
			fail();
		}
		catch (Exception e)
		{
			// expected
		}
	}

	/***************************************
	 * Test of {@link ObjectSpace#get(String)}
	 */
	@Test
	public void testGet()
	{
		assertEquals("Test", aTestSpace.get("name"));
		assertEquals(Integer.valueOf(1234), aTestSpace.get("port"));
		assertEquals("Sub1", aTestSpace.get("subspace1/name"));
		assertEquals(Integer.valueOf(1111), aTestSpace.get("subspace1/port"));
		assertEquals("Sub2", aTestSpace.get("subspace2/name"));
		assertEquals(Integer.valueOf(2222), aTestSpace.get("subspace2/port"));
	}
}
