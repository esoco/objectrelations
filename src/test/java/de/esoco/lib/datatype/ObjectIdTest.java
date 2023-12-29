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
package de.esoco.lib.datatype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test of the object ID class.
 *
 * @author eso
 */
public class ObjectIdTest {

	/**
	 * Test of {@link ObjectId#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		ObjectId<Object> stringId = ObjectId.stringId("42");
		ObjectId<Object> integerId = ObjectId.intId(42);

		assertNotEquals(stringId, integerId);
		assertNotEquals(integerId, stringId);

		stringId = ObjectId.stringId("TestId");
		integerId = ObjectId.intId(42);

		assertNotEquals(stringId, integerId);
		assertNotEquals(integerId, stringId);
	}

	/**
	 * Test of {@link ObjectId#intId(int)}.
	 */
	@Test
	public void testGetIdInt() {
		int id = 42;
		ObjectId<Object> objId = ObjectId.intId(id);

		assertInstanceOf(Integer.class, objId.internalValue());
		assertEquals(id, ((Integer) objId.internalValue()).intValue());
	}

	/**
	 * Test of {@link ObjectId#stringId(String)}.
	 */
	@Test
	public void testGetIdString() {
		String id = "TestId";
		ObjectId<Object> objId = ObjectId.stringId(id);

		assertSame(id, objId.internalValue());
	}

	/**
	 * Test of {@link ObjectId#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		ObjectId<Object> stringId = ObjectId.stringId("42");
		ObjectId<Object> integerId = ObjectId.intId(42);

		assertEquals(stringId.hashCode(), integerId.hashCode());
	}

	/**
	 * Test of {@link ObjectId#toString()}.
	 */
	@Test
	public void testToString() {
		ObjectId<Object> stringId = ObjectId.stringId("42");
		ObjectId<Object> integerId = ObjectId.intId(42);
		ObjectId<Object> longId = ObjectId.longId(42);

		assertEquals("42", stringId.toString());
		assertEquals("42", integerId.toString());
		assertEquals("42", longId.toString());
	}
}
