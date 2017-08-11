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
package de.esoco.lib.json;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test method
 *
 * @author eso
 */
public class JsonBuilderTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<String> TEST_RELATION =
		RelationTypes.newType();

	static
	{
		RelationTypes.init(JsonBuilderTest.class);
	}

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
		assertEquals("\"Testname\"",
					 new JsonBuilder().append("Testname").toString());
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendObjectFromMap()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testAppendObjectFromRelatable()
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

		aJson.appendObject(aTest, null, true);
	}

	/***************************************
	 * Test of {@link JsonBuilder#append(org.obrel.core.Relation, boolean,
	 * boolean)}
	 */
	@Test
	public void testAppendRelation()
	{
		RelatedObject r = new RelatedObject();

		r.set(TEST_RELATION, "Test");

		JsonBuilder jb = new JsonBuilder();

		assertTrue(jb.append(r.getRelation(TEST_RELATION), true, true));
		assertEquals(String.format("\"%s\": \"Test\"", TEST_RELATION.getName()),
					 jb.toString());

		jb = new JsonBuilder();
		assertTrue(jb.append(r.getRelation(TEST_RELATION), false, true));
		assertEquals(String.format("\"%s\": \"Test\"",
								   TEST_RELATION.getSimpleName()),
					 jb.toString());

		r.set(TEST_RELATION, null);
		jb = new JsonBuilder();
		assertTrue(jb.append(r.getRelation(TEST_RELATION), false, true));
		assertEquals(String.format("\"%s\": null",
								   TEST_RELATION.getSimpleName()),
					 jb.toString());

		jb = new JsonBuilder();
		assertFalse(jb.append(r.getRelation(TEST_RELATION), false, false));
		assertEquals(String.format("", TEST_RELATION.getSimpleName()),
					 jb.toString());
	}

	/***************************************
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testAppendValue()
	{
		assertEquals("null", new JsonBuilder().append(null).toString());
		assertEquals("true", new JsonBuilder().append(Boolean.TRUE).toString());
		assertEquals("false",
					 new JsonBuilder().append(Boolean.FALSE).toString());

		assertEquals("42", new JsonBuilder().append(42).toString());
		assertEquals(Integer.toString(Integer.MIN_VALUE),
					 new JsonBuilder().append(Integer.MIN_VALUE).toString());
		assertEquals(Integer.toString(Integer.MAX_VALUE),
					 new JsonBuilder().append(Integer.MAX_VALUE).toString());
		assertEquals(Long.toString(Long.MIN_VALUE),
					 new JsonBuilder().append(Long.MIN_VALUE).toString());
		assertEquals(Long.toString(Long.MAX_VALUE),
					 new JsonBuilder().append(Long.MAX_VALUE).toString());
		assertEquals("123456789012345678901234567890",
					 new JsonBuilder().append(new BigInteger("123456789012345678901234567890"))
					 .toString());
		assertEquals("0.123", new JsonBuilder().append(0.123).toString());
		assertEquals("1.23", new JsonBuilder().append(1.23).toString());
		assertEquals("12300.0", new JsonBuilder().append(1.23e4).toString());
		assertEquals("1.23", new JsonBuilder().append(1.23d).toString());
		assertEquals("12300.0", new JsonBuilder().append(1.23e4d).toString());
		assertEquals("1.23E+4",
					 new JsonBuilder().append(new BigDecimal("1.23e4"))
					 .toString());

		testAppendDate();

		assertEquals("\"NAME\"",
					 new JsonBuilder().append(StandardTypes.NAME).toString());

		assertEquals("\"test string\"",
					 new JsonBuilder().append("test string").toString());
		assertEquals("\"\\ttest string\\n\\r\\tmultiline \u011F\"",
					 new JsonBuilder().append("\ttest string\n\r\tmultiline \u011f")
					 .toString());
	}

	/***************************************
	 * Tests appending a date value.
	 */
	private void testAppendDate()
	{
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, 6);
		cal.set(Calendar.DAY_OF_MONTH, 10);
		cal.set(Calendar.HOUR, 12);
		cal.set(Calendar.MINUTE, 6);
		cal.set(Calendar.SECOND, 42);
		cal.set(Calendar.MILLISECOND, 123);

		assertEquals(String.format("\"%s\"",
								   JsonUtil.JSON_DATE_FORMAT.format(cal.getTime())),
					 new JsonBuilder().append(cal.getTime()).toString());
	}
}
