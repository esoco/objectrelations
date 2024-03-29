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
package de.esoco.lib.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ReflectUtil}.
 *
 * @author eso
 */
public class ReflectUtilTest {
	/**
	 * Test {@link ReflectUtil#collectConstants(Class, Class, String, boolean,
	 * boolean, boolean)}
	 */
	@Test
	public void testCollectConstants() {
		ConstantTestBaseClass bc = new ConstantTestBaseClass();

		assertEquals(2, bc.constants.size());
		assertTrue(bc.constants.contains(ConstantTestBaseClass.TC1));
		assertTrue(bc.constants.contains(ConstantTestBaseClass.TC2));
		assertFalse(bc.constants.contains(ConstantTestSubclass.TC3));

		ConstantTestSubclass sc = new ConstantTestSubclass();

		assertEquals(2, sc.constants.size());
		assertFalse(sc.constants.contains(ConstantTestBaseClass.TC1));
		assertTrue(sc.constants.contains(ConstantTestSubclass.TC3));

		assertEquals(4, sc.allConstants.size());
		assertTrue(sc.allConstants.contains(ConstantTestBaseClass.TC1));
		assertTrue(sc.constants.contains(ConstantTestSubclass.TC3));
	}

	// ~ Inner Classes
	// ----------------------------------------------------------

	/**
	 * Constant test base class.
	 */
	static class ConstantTestBaseClass {
		// ~ Static fields/initializers
		// -----------------------------------------

		/**
		 * Test constant 1
		 */
		public static final String TC1 = "TC1";

		/**
		 * Test constant 1
		 */
		public static final String TC2 = "TC2";

		// ~ Instance fields
		// ----------------------------------------------------

		List<String> constants;

		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * Creates a new instance.
		 */
		public ConstantTestBaseClass() {
			constants = ReflectUtil.collectConstants(getClass(), String.class,
				"toString", false, true, false);
		}
	}

	/**
	 * Constant test subclass.
	 */
	static class ConstantTestSubclass extends ConstantTestBaseClass {
		// ~ Static fields/initializers
		// -----------------------------------------

		/**
		 * Test constant 1
		 */
		public static final String TC3 = "TC3";

		/**
		 * Test constant 1
		 */
		public static final String TC4 = "TC4";

		// ~ Instance fields
		// ----------------------------------------------------

		List<String> constants;

		List<String> allConstants;

		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * Creates a new instance.
		 */
		public ConstantTestSubclass() {
			constants = ReflectUtil.collectConstants(getClass(), String.class,
				"toString", false, false, false);
			allConstants =
				ReflectUtil.collectConstants(getClass(), String.class,
					"toString", false, true, false);
		}
	}
}
