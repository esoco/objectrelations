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

import de.esoco.lib.event.ElementEvent.EventType;
import de.esoco.lib.expression.Predicates;

import org.junit.Test;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.core.RelationTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.obrel.type.StandardTypes.INFO;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * Test of {@link CounterType}.
 *
 * @author eso
 */
public class CounterTypeTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<Integer> INT_COUNTER =
		CounterType.newIntCounter(Predicates.alwaysTrue());

	private static final RelationType<Integer> FINAL_INT_COUNTER  =
		CounterType.newIntCounter(Predicates.alwaysTrue(),
								  RelationTypeModifier.FINAL);
	@SuppressWarnings("boxing")
	private static final RelationType<Integer> SHORT_NAME_COUNTER =
		CounterType.newIntCounter(e ->
								  e.getType() != EventType.REMOVE &&
								  e.getElement().getType() == NAME);

	static
	{
		RelationTypes.init(CounterTypeTest.class);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of an final integer counter.
	 */
	@Test
	public void testFinalIntCounter()
	{
		Relatable o = new RelatedObject();

		o.set(FINAL_INT_COUNTER, 5);
		assertEquals(5, o.get(FINAL_INT_COUNTER).intValue());
		o.set(NAME, "Test1");
		assertEquals(6, o.get(FINAL_INT_COUNTER).intValue());

		try
		{
			o.set(FINAL_INT_COUNTER, 0);
			fail();
		}
		catch (Exception e)
		{
			// expected
		}
	}

	/***************************************
	 * Test of an (non-final) integer counter.
	 */
	@Test
	public void testIntCounter()
	{
		Relatable o = new RelatedObject();

		assertEquals(0, o.get(INT_COUNTER).intValue());
		o.set(NAME, "Test1");
		o.getRelation(NAME).set(INT_COUNTER, 100);
		NAME.set(INT_COUNTER, 200);
		assertEquals(1, o.get(INT_COUNTER).intValue());
		o.set(NAME, "Test2");
		assertEquals(2, o.get(INT_COUNTER).intValue());
		assertEquals(101, o.getRelation(NAME).get(INT_COUNTER).intValue());
		assertEquals(201, NAME.get(INT_COUNTER).intValue());
		o.set(INFO, "Test3");
		assertEquals(3, o.get(INT_COUNTER).intValue());
		assertEquals(101, o.getRelation(NAME).get(INT_COUNTER).intValue());
		assertEquals(201, NAME.get(INT_COUNTER).intValue());
		o.deleteRelation(NAME);
		assertEquals(4, o.get(INT_COUNTER).intValue());
		o.set(INT_COUNTER, 0);
		assertEquals(0, o.get(INT_COUNTER).intValue());
		o.set(NAME, "Test4");
		assertEquals(1, o.get(INT_COUNTER).intValue());
	}

	/***************************************
	 * Test of a short counter of name relation accesses.
	 */
	@Test
	public void testShortNameCounter()
	{
		Relatable o = new RelatedObject();

		o.init(SHORT_NAME_COUNTER);
		assertEquals(0, o.get(SHORT_NAME_COUNTER).intValue());
		o.set(INFO, "Test1");
		assertEquals(0, o.get(SHORT_NAME_COUNTER).intValue());
		o.set(NAME, "Test2");
		assertEquals(1, o.get(SHORT_NAME_COUNTER).intValue());
	}
}
