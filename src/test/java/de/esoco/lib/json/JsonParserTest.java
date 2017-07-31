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

import de.esoco.lib.collection.CollectionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import org.obrel.core.RelatedObject;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of {@link JsonParser}.
 *
 * @author eso
 */
public class JsonParserTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseCollection()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseMap()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseNumber()
	{
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseRelatable()
	{
		RelatedObject aCompare = new RelatedObject();
		RelatedObject aTest    = new RelatedObject();
		RelatedObject aParent  = new RelatedObject();

		aParent.set(StandardTypes.NAME, "PARENT");
		aParent.set(StandardTypes.INFO, "JSON_OBJECT");

		aCompare.set(StandardTypes.NAME, "TEST");
		aCompare.set(StandardTypes.INFO, "JSON");
		aCompare.set(StandardTypes.PORT, 12345);

		new JsonParser().parseRelatable("{\"NAME\": \"TEST\"," +
										" \"INFO\": \"JSON\"," +
										" \"PORT\": 12345}",
										aTest);
		assertTrue(aTest.relationsEqual(aCompare));

		aCompare.set(StandardTypes.PARENT, aParent);

		new JsonParser().parseRelatable("{\"PARENT\": {\"NAME\": \"PARENT\"," +
										" \"INFO\": \"JSON_OBJECT\"}," +
										" \"NAME\": \"TEST\"," +
										" \"INFO\": \"JSON\"," +
										" \"PORT\": 12345}",
										aTest);
		assertTrue(((RelatedObject) aTest.get(StandardTypes.PARENT))
				   .relationsEqual(aParent));
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseRelation()
	{
	}

	/***************************************
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testParseValue()
	{
		assertEquals("TEST 123", new JsonParser().parseValue("\"TEST 123\""));
		assertEquals("", new JsonParser().parseValue("\"\""));

		assertEquals("null", new JsonParser().parseValue("\"null\""));
		assertEquals(null, new JsonParser().parseValue("null"));

		assertEquals("true", new JsonParser().parseValue("\"true\""));
		assertEquals(Boolean.TRUE, new JsonParser().parseValue("true"));
		assertEquals("false", new JsonParser().parseValue("\"false\""));
		assertEquals(Boolean.FALSE, new JsonParser().parseValue("false"));

		assertEquals(new Integer(12345678),
					 new JsonParser().parseValue("12345678"));
		assertEquals(Integer.MIN_VALUE,
					 new JsonParser().parseValue(Integer.toString(Integer.MIN_VALUE)));
		assertEquals(Integer.MAX_VALUE,
					 new JsonParser().parseValue(Integer.toString(Integer.MAX_VALUE)));

		assertEquals(new Long(1234567890123456L),
					 new JsonParser().parseValue("1234567890123456"));
		assertEquals(Long.MIN_VALUE,
					 new JsonParser().parseValue(Long.toString(Long.MIN_VALUE)));
		assertEquals(Long.MAX_VALUE,
					 new JsonParser().parseValue(Long.toString(Long.MAX_VALUE)));

		assertEquals(new Integer(Short.MIN_VALUE),
					 new JsonParser().parseValue(Short.toString(Short.MIN_VALUE)));
		assertEquals(new Integer(Short.MAX_VALUE),
					 new JsonParser().parseValue(Short.toString(Short.MAX_VALUE)));

		assertEquals(new BigInteger("1234567890123456789012345678901234567890"),
					 new JsonParser().parseValue("1234567890123456789012345678901234567890"));

		assertEquals(new BigDecimal("0.01"),
					 new JsonParser().parseValue("0.01"));
		assertEquals(new BigDecimal("42.0"),
					 new JsonParser().parseValue("42.0"));
		assertEquals(new BigDecimal("-42.0"),
					 new JsonParser().parseValue("-42.0"));
		assertEquals(new BigDecimal("-42.0e42"),
					 new JsonParser().parseValue("-42.0E42"));

		assertEquals(new HashMap<String, Object>(),
					 new JsonParser().parseValue("{}"));
		assertEquals(CollectionUtil.mapOf(t("TESTKEY", "TESTVALUE")),
					 new JsonParser().parseValue("{\"TESTKEY\": \"TESTVALUE\"}"));
		assertEquals(CollectionUtil.mapOf(t("TESTKEY1", "TESTVALUE1"),
										  t("TESTKEY2", "TESTVALUE2")),
					 new JsonParser().parseValue("{\"TESTKEY1\": \"TESTVALUE1\"," +
												 " \"TESTKEY2\": \"TESTVALUE2\"}"));

		assertEquals(new ArrayList<Object>(),
					 new JsonParser().parseValue("[]"));
		assertEquals(Arrays.asList("TEST1", "TEST2"),
					 new JsonParser().parseValue("[\"TEST1\", \"TEST2\"]"));
	}

	/***************************************
	 * Test method
	 */
	@Test
	public void testParseValueWithDatatype()
	{
	}
}
