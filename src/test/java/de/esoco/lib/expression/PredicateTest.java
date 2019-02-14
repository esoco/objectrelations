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
package de.esoco.lib.expression;

import junit.framework.TestCase;

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.greaterOrEqual;
import static de.esoco.lib.expression.Predicates.greaterThan;
import static de.esoco.lib.expression.Predicates.ifField;
import static de.esoco.lib.expression.Predicates.lessOrEqual;
import static de.esoco.lib.expression.Predicates.lessThan;
import static de.esoco.lib.expression.Predicates.matching;


/********************************************************************
 * Test of predicates
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class PredicateTest extends TestCase
{
	//~ Instance fields --------------------------------------------------------

	@SuppressWarnings("unused")
	private int nReflectionTestField = 42;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of comparison predicates.
	 */
	public void testComparison()
	{
		Predicate<Integer> less			  = lessThan(new Integer(5));
		Predicate<Integer> lessOrEqual    = lessOrEqual(new Integer(5));
		Predicate<Integer> greater		  = greaterThan(new Integer(5));
		Predicate<Integer> greaterOrEqual = greaterOrEqual(new Integer(5));
		Predicate<Integer> equals		  = equalTo(new Integer(5));

		assertTrue(less.evaluate(4));
		assertTrue(lessOrEqual.evaluate(4));
		assertFalse(less.evaluate(5));
		assertTrue(lessOrEqual.evaluate(5));
		assertFalse(less.evaluate(6));
		assertFalse(lessOrEqual.evaluate(6));

		assertTrue(greater.evaluate(6));
		assertTrue(greaterOrEqual.evaluate(6));
		assertFalse(greater.evaluate(5));
		assertTrue(greaterOrEqual.evaluate(5));
		assertFalse(greater.evaluate(4));
		assertFalse(greaterOrEqual.evaluate(4));

		assertTrue(equals.evaluate(5));
		assertFalse(equals.evaluate(4));
		assertFalse(equals.evaluate(6));
	}

	/***************************************
	 * Test logical and.
	 */
	public void testLogicalAnd()
	{
		Predicate<String> aAndTest = matching("[ABC].+");

		aAndTest = aAndTest.and(matching(".+[XYZ]"));

		assertTrue(aAndTest.evaluate("AZ"));
		assertTrue(aAndTest.evaluate("B12345Y"));
		assertFalse(aAndTest.evaluate("A"));
		assertFalse(aAndTest.evaluate("Z"));
	}

	/***************************************
	 * Test logical or.
	 */
	public void testLogicalOr()
	{
		Predicate<String> aOrTest = matching("[ABC]");

		aOrTest = aOrTest.or(matching("[^ABC].+"));

		assertTrue(aOrTest.evaluate("Test"));
		assertTrue(aOrTest.evaluate("A"));
		assertFalse(aOrTest.evaluate("D"));
		assertFalse(aOrTest.evaluate("Ast"));
	}

	/***************************************
	 * Test reflection predicates.
	 */
	public void testReflection()
	{
		assertTrue(ifField("nReflectionTestField", equalTo(42)).evaluate(this));
		assertFalse(
			ifField("nReflectionTestField", lessThan(20)).evaluate(this));
	}
}
