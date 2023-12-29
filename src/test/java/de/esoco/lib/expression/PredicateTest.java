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

import static de.esoco.lib.expression.Predicates.equalTo;
import static de.esoco.lib.expression.Predicates.greaterOrEqual;
import static de.esoco.lib.expression.Predicates.greaterThan;
import static de.esoco.lib.expression.Predicates.ifField;
import static de.esoco.lib.expression.Predicates.lessOrEqual;
import static de.esoco.lib.expression.Predicates.lessThan;
import static de.esoco.lib.expression.Predicates.matching;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test of predicates
 *
 * @author eso
 */
public class PredicateTest {

	/**
	 * Test of comparison predicates.
	 */
	@Test
	void testComparison() {
		Predicate<Integer> less = lessThan(Integer.valueOf(5));
		Predicate<Integer> lessOrEqual = lessOrEqual(Integer.valueOf(5));
		Predicate<Integer> greater = greaterThan(Integer.valueOf(5));
		Predicate<Integer> greaterOrEqual = greaterOrEqual(Integer.valueOf(5));
		Predicate<Integer> equals = equalTo(Integer.valueOf(5));

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

	/**
	 * Test logical and.
	 */
	@Test
	void testLogicalAnd() {
		Predicate<String> andTest = matching("[ABC].+");

		andTest = andTest.and(matching(".+[XYZ]"));

		assertTrue(andTest.evaluate("AZ"));
		assertTrue(andTest.evaluate("B12345Y"));
		assertFalse(andTest.evaluate("A"));
		assertFalse(andTest.evaluate("Z"));
	}

	/**
	 * Test logical or.
	 */
	@Test
	void testLogicalOr() {
		Predicate<String> orTest = matching("[ABC]");

		orTest = orTest.or(matching("[^ABC].+"));

		assertTrue(orTest.evaluate("Test"));
		assertTrue(orTest.evaluate("A"));
		assertFalse(orTest.evaluate("D"));
		assertFalse(orTest.evaluate("Ast"));
	}

	/**
	 * Test reflection predicates.
	 */
	// @Test
	void testReflection() {
		assertTrue(ifField("nReflectionTestField", equalTo(42)).evaluate(this));
		assertFalse(
			ifField("nReflectionTestField", lessThan(20)).evaluate(this));
	}
}
