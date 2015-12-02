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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of the object ID class.
 *
 * @author eso
 */
public class ObjectIdTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link ObjectId#equals(Object)}.
	 */
	@Test
	public void testEquals()
	{
		ObjectId<Object> aStringId  = ObjectId.stringId("42");
		ObjectId<Object> aIntegerId = ObjectId.intId(42);

		assertFalse(aStringId.equals(aIntegerId));
		assertFalse(aIntegerId.equals(aStringId));

		aStringId  = ObjectId.stringId("TestId");
		aIntegerId = ObjectId.intId(42);

		assertFalse(aStringId.equals(aIntegerId));
		assertFalse(aIntegerId.equals(aStringId));
	}

	/***************************************
	 * Test of {@link ObjectId#intId(int)}.
	 */
	@Test
	public void testGetIdInt()
	{
		int				 nId = 42;
		ObjectId<Object> aId = ObjectId.intId(nId);

		assertTrue(aId.internalValue() instanceof Integer);
		assertTrue(nId == ((Integer) aId.internalValue()).intValue());
	}

	/***************************************
	 * Test of {@link ObjectId#stringId(String)}.
	 */
	@Test
	public void testGetIdString()
	{
		String			 sId = "TestId";
		ObjectId<Object> aId = ObjectId.stringId(sId);

		assertTrue(sId == aId.internalValue());
	}

	/***************************************
	 * Test of {@link ObjectId#hashCode()}.
	 */
	@Test
	public void testHashCode()
	{
		ObjectId<Object> aStringId  = ObjectId.stringId("42");
		ObjectId<Object> aIntegerId = ObjectId.intId(42);

		assertTrue(aStringId.hashCode() == aIntegerId.hashCode());
	}

	/***************************************
	 * Test of {@link ObjectId#toString()}.
	 */
	@Test
	public void testToString()
	{
		ObjectId<Object> aStringId  = ObjectId.stringId("42");
		ObjectId<Object> aIntegerId = ObjectId.intId(42);
		ObjectId<Object> aLongId    = ObjectId.longId(42);

		assertEquals("42", aStringId.toString());
		assertEquals("42", aIntegerId.toString());
		assertEquals("42", aLongId.toString());
	}
}
