//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.json;

import java.util.Arrays;

import org.junit.Test;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.type.StandardTypes;


/********************************************************************
 * Test method
 *
 * @author eso
 */
public class JsonBuilderTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendArray()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendName()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendObjectMap()
	{
	}

	/***************************************
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testAppendObjectRelatable()
	{
		JsonBuilder aJson = new JsonBuilder("\t");

		RelatedObject aTest   = new RelatedObject();
		RelatedObject aParent = new RelatedObject();
		RelatedObject aChild1 = new RelatedObject();
		RelatedObject aChild2 = new RelatedObject();

		aParent.set(StandardTypes.NAME, "PARENT");
		aParent.set(StandardTypes.INFO, "JSON_OBJECT");

		aChild1.set(StandardTypes.NAME, "CHILD1");
		aChild2.set(StandardTypes.NAME, "CHILD2");

		aTest.set(StandardTypes.NAME, "TEST");
		aTest.set(StandardTypes.INFO, "JSON");
		aTest.set(StandardTypes.PORT, 12345);
		aTest.set(StandardTypes.PARENT, aParent);
		aTest.set(StandardTypes.CHILDREN,
				  Arrays.<Relatable>asList(aChild1, aChild2));
		aJson.appendObject(aTest, null);
		System.out.printf("---- JSON ----\n%s", aJson);
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendRelatable()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendRelation()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendString()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendValue()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testBeginArray()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testBeginObject()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testBeginString()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testEndArray()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testEndObject()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testEndString()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testJsonBuilder()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testLength()
	{
	}
}
