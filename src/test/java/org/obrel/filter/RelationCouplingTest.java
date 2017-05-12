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
package org.obrel.filter;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.obrel.filter.RelationCoupling.COUPLINGS;
import static org.obrel.filter.RelationCoupling.couple;
import static org.obrel.filter.RelationCoupling.getAll;
import static org.obrel.filter.RelationCoupling.removeAll;
import static org.obrel.filter.RelationCoupling.setAll;
import static org.obrel.type.StandardTypes.COUNT;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * Test of {@link RelationCoupling}
 *
 * @author eso
 */
public class RelationCouplingTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final List<RelationType<?>> ALL_TEST_TYPES =
		Arrays.asList(NAME, COUNT);

	//~ Instance fields --------------------------------------------------------

	Relatable	   o	   = new RelatedObject();
	private String sTarget = null;
	private int    nCount  = 0;

	private RelationCoupling<String> c1 =
		couple(o, NAME, v -> sTarget = v, () -> sTarget);

	@SuppressWarnings({ "boxing", "unused" })
	private RelationCoupling<Integer> c2 =
		couple(o, COUNT, n -> nCount = n, () -> nCount);

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link RelationCoupling#couple(Relatable,
	 * org.obrel.core.RelationType, java.util.function.Consumer,
	 * java.util.function.Supplier)}
	 */
	@Test
	public void testCouple()
	{
		assertTrue(o.hasRelation(NAME));
		assertTrue(o.getRelation(NAME).hasAnnotation(COUPLINGS));
		assertEquals(null, o.get(NAME));
		assertEquals(null, sTarget);
		assertEquals(1, o.getRelation(NAME).get(COUPLINGS).size());
	}

	/***************************************
	 * Test of {@link RelationCoupling#get()}
	 */
	@Test
	public void testGet()
	{
		sTarget = "TEST_GET";
		c1.get();
		assertEquals("TEST_GET", o.get(NAME));
	}

	/***************************************
	 * Test of {@link RelationCoupling#getAll(Relatable, java.util.Collection)}
	 */
	@Test
	public void testGetAll()
	{
		sTarget = "TEST_GET_ALL";
		nCount  = 43;

		getAll(o, ALL_TEST_TYPES);
		assertEquals("TEST_GET_ALL", o.get(NAME));
		assertEquals(43, o.get(COUNT).intValue());
	}

	/***************************************
	 * Test of {@link RelationCoupling#remove()}
	 */
	@Test
	public void testRemove()
	{
		o.set(NAME, "TEST_REMOVE");
		c1.remove();
		sTarget = "REMOVED";
		c1.get();
		assertEquals("TEST_REMOVE", o.get(NAME));
		c1.set();
		assertEquals("REMOVED", sTarget);
	}

	/***************************************
	 * Test of {@link RelationCoupling#removeAll(Relatable,
	 * java.util.Collection)}
	 */
	@Test
	public void testRemoveAll()
	{
		o.set(NAME, "TEST_REMOVE_ALL");
		o.set(COUNT, 123);
		removeAll(o, ALL_TEST_TYPES);
		sTarget = "REMOVED_ALL";
		nCount  = 321;

		getAll(o, ALL_TEST_TYPES);
		assertEquals("TEST_REMOVE_ALL", o.get(NAME));
		assertEquals(123, o.get(COUNT).intValue());
		setAll(o, ALL_TEST_TYPES);
		assertEquals("REMOVED_ALL", sTarget);
		assertEquals(321, nCount);
	}

	/***************************************
	 * Test of {@link RelationCoupling#set()}
	 */
	@Test
	public void testSet()
	{
		o.set(NAME, "TEST_SET");
		assertEquals(null, sTarget);
		c1.set();
		assertEquals("TEST_SET", sTarget);
	}

	/***************************************
	 * Test of {@link RelationCoupling#setAll(Relatable, java.util.Collection)}
	 */
	@Test
	public void testSetAll()
	{
		o.set(NAME, "TEST_SET_ALL");
		o.set(COUNT, 42);
		setAll(o, ALL_TEST_TYPES);
		assertEquals("TEST_SET_ALL", sTarget);
		assertEquals(42, nCount);
	}
}
