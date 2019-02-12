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
package de.esoco.lib.json;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of {@link JsonParser}.
 *
 * @author eso
 */
@RelationTypeNamespace("")
public class JsonParserTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final RelationType<List<Long>> TEST_LIST =
		RelationTypes.newListType();

	static
	{
		RelationTypes.init(JsonParserTest.class);
	}

	//~ Instance fields --------------------------------------------------------

	private JsonParser aParser = new JsonParser();

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test parsing with limited depth.
	 */
	@Test
	public void testLimitedDepth()
	{
		JsonObject jo =
			new JsonParser(1).parseObject(
				"{\"REF\": {\"NAME\": \"REF\"," +
				" \"INFO\": \"JSON_OBJECT\"}," +
				" \"NAME\": \"TEST\"," +
				" \"INFO\": \"JSON\"," +
				" \"PORT\": 12345}");

		// assert that REF is an unparsed string
		assertEquals(
			"{\"NAME\": \"REF\", \"INFO\": \"JSON_OBJECT\"}",
			jo.getString("REF").orFail());
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
		aCompare.set(TEST_LIST, Arrays.asList(1L, 2L, 3L));

		new JsonParser().parseRelatable(
			"{\"NAME\": \"TEST\"," +
			" \"INFO\": \"JSON\"," +
			" \"PORT\": 12345," +
			" \"TEST_LIST\": [1,2,3]}",
			aTest);
		assertTrue(aTest.relationsEqual(aCompare));
		assertEquals(Long.class, aTest.get(TEST_LIST).get(0).getClass());

		aCompare.set(StandardTypes.PARENT, aParent);

		new JsonParser().parseRelatable(
			"{\"PARENT\": {\"NAME\": \"PARENT\"," +
			" \"INFO\": \"JSON_OBJECT\"}," +
			" \"NAME\": \"TEST\"," +
			" \"INFO\": \"JSON\"," +
			" \"PORT\": 12345}",
			aTest);
		assertTrue(
			((RelatedObject) aTest.get(StandardTypes.PARENT)).relationsEqual(
				aParent));
	}

	/***************************************
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testParseValue()
	{
		assertEquals("TEST 123", aParser.parse("\"TEST 123\""));
		assertEquals("", aParser.parse("\"\""));

		assertEquals("null", aParser.parse("\"null\""));
		assertEquals(null, aParser.parse("null"));

		assertEquals("true", aParser.parse("\"true\""));
		assertEquals(Boolean.TRUE, aParser.parse("true"));
		assertEquals("false", aParser.parse("\"false\""));
		assertEquals(Boolean.FALSE, aParser.parse("false"));

		assertEquals(new Integer(12345678), aParser.parse("12345678"));
		assertEquals(
			Integer.MIN_VALUE,
			aParser.parse(Integer.toString(Integer.MIN_VALUE)));
		assertEquals(
			Integer.MAX_VALUE,
			aParser.parse(Integer.toString(Integer.MAX_VALUE)));

		assertEquals(
			new Long(1234567890123456L),
			aParser.parse("1234567890123456"));
		assertEquals(
			Long.MIN_VALUE,
			aParser.parse(Long.toString(Long.MIN_VALUE)));
		assertEquals(
			Long.MAX_VALUE,
			aParser.parse(Long.toString(Long.MAX_VALUE)));

		assertEquals(
			new Integer(Short.MIN_VALUE),
			aParser.parse(Short.toString(Short.MIN_VALUE)));
		assertEquals(
			new Integer(Short.MAX_VALUE),
			aParser.parse(Short.toString(Short.MAX_VALUE)));

		assertEquals(
			new BigInteger("1234567890123456789012345678901234567890"),
			aParser.parse("1234567890123456789012345678901234567890"));

		assertEquals(new BigDecimal("0.01"), aParser.parse("0.01"));
		assertEquals(new BigDecimal("42.0"), aParser.parse("42.0"));
		assertEquals(new BigDecimal("-42.0"), aParser.parse("-42.0"));
		assertEquals(new BigDecimal("-42.0e42"), aParser.parse("-42.0E42"));

		assertEquals(new JsonObject(), aParser.parse("{}"));
		assertEquals(
			new JsonObject(t("TESTKEY", "TESTVALUE")),
			aParser.parse("{\"TESTKEY\": \"TESTVALUE\"}"));
		assertEquals(
			new JsonObject(
				t("TESTKEY1", "TESTVALUE1"),
				t("TESTKEY2", "TESTVALUE2"),
				t("NULLVALUE", null)),
			aParser.parse(
				"{\"TESTKEY1\": \"TESTVALUE1\"," +
				" \"TESTKEY2\": \"TESTVALUE2\"," +
				"\"NULLVALUE\": null}"));

		assertEquals(new ArrayList<Object>(), aParser.parse("[]"));
		assertEquals(
			Arrays.asList("TEST1", "TEST2"),
			aParser.parse("[\"TEST1\", \"TEST2\"]"));
	}
}
