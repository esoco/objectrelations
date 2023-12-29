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

import org.junit.jupiter.api.Test;
import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.esoco.lib.datatype.Pair.t;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of {@link JsonParser}.
 *
 * @author eso
 */
@RelationTypeNamespace("")
public class JsonParserTest {
	private static final RelationType<List<Long>> TEST_LIST =
		RelationTypes.newListType();

	static {
		RelationTypes.init(JsonParserTest.class);
	}

	private final JsonParser parser = new JsonParser();

	/**
	 * Test parsing with limited depth.
	 */
	@Test
	public void testLimitedDepth() {
		JsonObject jo = new JsonParser(1).parseObject(
			"{\"REF\": {\"NAME\": \"REF\"," + " \"INFO\": \"JSON_OBJECT\"}," +
				" \"NAME\": \"TEST\"," + " \"INFO\": \"JSON\"," +
				" \"PORT\": 12345}");

		// assert that REF is an unparsed string
		assertEquals("{\"NAME\": \"REF\", \"INFO\": \"JSON_OBJECT\"}",
			jo.getString("REF").orFail());
	}

	/**
	 * Test method
	 */
	@Test
	public void testParseRelatable() {
		RelatedObject compare = new RelatedObject();
		RelatedObject test = new RelatedObject();
		RelatedObject parent = new RelatedObject();

		parent.set(StandardTypes.NAME, "PARENT");
		parent.set(StandardTypes.INFO, "JSON_OBJECT");

		compare.set(StandardTypes.NAME, "TEST");
		compare.set(StandardTypes.INFO, "JSON");
		compare.set(StandardTypes.PORT, 12345);
		compare.set(TEST_LIST, Arrays.asList(1L, 2L, 3L));

		new JsonParser().parseRelatable(
			"{\"NAME\": \"TEST\"," + " \"INFO\": \"JSON\"," +
				" \"PORT\": 12345," + " \"TEST_LIST\": [1,2,3]}", test);
		assertTrue(test.relationsEqual(compare));
		assertEquals(Long.class, test.get(TEST_LIST).get(0).getClass());

		compare.set(StandardTypes.PARENT, parent);

		new JsonParser().parseRelatable("{\"PARENT\": {\"NAME\": \"PARENT\"," +
			" \"INFO\": \"JSON_OBJECT\"}," + " \"NAME\": \"TEST\"," +
			" \"INFO\": \"JSON\"," + " \"PORT\": 12345}", test);
		assertTrue(
			((RelatedObject) test.get(StandardTypes.PARENT)).relationsEqual(
				parent));
	}

	/**
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testParseValue() {
		assertEquals("TEST 123", parser.parse("\"TEST 123\""));
		assertEquals("", parser.parse("\"\""));

		assertEquals("null", parser.parse("\"null\""));
		assertNull(parser.parse("null"));

		assertEquals("true", parser.parse("\"true\""));
		assertEquals(Boolean.TRUE, parser.parse("true"));
		assertEquals("false", parser.parse("\"false\""));
		assertEquals(Boolean.FALSE, parser.parse("false"));

		assertEquals(Integer.valueOf(12345678), parser.parse("12345678"));
		assertEquals(Integer.MIN_VALUE,
			parser.parse(Integer.toString(Integer.MIN_VALUE)));
		assertEquals(Integer.MAX_VALUE,
			parser.parse(Integer.toString(Integer.MAX_VALUE)));

		assertEquals(Long.valueOf(1234567890123456L),
			parser.parse("1234567890123456"));
		assertEquals(Long.MIN_VALUE,
			parser.parse(Long.toString(Long.MIN_VALUE)));
		assertEquals(Long.MAX_VALUE,
			parser.parse(Long.toString(Long.MAX_VALUE)));

		assertEquals(Integer.valueOf(Short.MIN_VALUE),
			parser.parse(Short.toString(Short.MIN_VALUE)));
		assertEquals(Integer.valueOf(Short.MAX_VALUE),
			parser.parse(Short.toString(Short.MAX_VALUE)));

		assertEquals(new BigInteger(
			"1234567890123456789012345678901234567890"),
			parser.parse("1234567890123456789012345678901234567890"));

		assertEquals(new BigDecimal("0.01"), parser.parse("0.01"));
		assertEquals(new BigDecimal("42.0"), parser.parse("42.0"));
		assertEquals(new BigDecimal("-42.0"), parser.parse("-42.0"));
		assertEquals(new BigDecimal("-42.0e42"), parser.parse("-42.0E42"));

		assertEquals(new JsonObject(), parser.parse("{}"));
		assertEquals(new JsonObject(t("TESTKEY", "TESTVALUE")),
			parser.parse("{\"TESTKEY\": \"TESTVALUE\"}"));
		assertEquals(new JsonObject(t("TESTKEY1", "TESTVALUE1"),
			t("TESTKEY2", "TESTVALUE2"), t("NULLVALUE", null)), parser.parse(
			"{\"TESTKEY1\": \"TESTVALUE1\"," +
				" \"TESTKEY2\": \"TESTVALUE2\"," + "\"NULLVALUE\": null}"));

		assertEquals(new ArrayList<Object>(), parser.parse("[]"));
		assertEquals(Arrays.asList("TEST1", "TEST2"),
			parser.parse("[\"TEST1\", \"TEST2\"]"));
	}
}
