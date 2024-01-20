//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import static de.esoco.lib.datatype.Pair.t;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.obrel.type.StandardTypes.CHILDREN;
import static org.obrel.type.StandardTypes.INFO;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PORT;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.text.TextConvert.IdentifierStyle;

/**
 * Test method
 *
 * @author eso
 */
public class JsonBuilderTest {
	private static final RelationType<String> TEST_RELATION =
		RelationTypes.newType();

	static {
		RelationTypes.init(JsonBuilderTest.class);
	}

	/**
	 * Test method
	 */
	@Test
	public void testAppendArray() {
	}

	/**
	 * Test method
	 */
	@Test
	public void testAppendName() {
		assertEquals("\"Testname\"",
			new JsonBuilder().append("Testname").toString());
	}

	/**
	 * Test method
	 */
	@Test
	public void testAppendObjectFromMap() {
		@SuppressWarnings("boxing")
		Map<String, Object> data =
			CollectionUtil.orderedMapOf(t("name", "Name"), t("value", 42),
				t("flag", true), t("nothing", null),
				t("array", Arrays.asList("a1", "a2", "a3")),
				t("map", CollectionUtil.orderedMapOf(t("m1", "v1"))));

		JsonBuilder jsonBuilder = new JsonBuilder().compact();

		assertEquals("{\"name\":\"Name\",\"value\":42,\"flag\":true," +
			"\"nothing\":null,\"array\":[\"a1\",\"a2\",\"a3\"]," +
			"\"map\":{\"m1\":\"v1\"}}", jsonBuilder.append(data).toString());
	}

	/**
	 * Test method
	 */
	@Test
	public void testAppendRelatable() {
		JsonBuilder json = new JsonBuilder().compact();

		json.appendRelatable(createTestRelatable(), null, true);

		assertEquals("{\"NAME\":\"TEST\",\"INFO\":\"JSON\",\"PORT\":12345," +
				"\"MODIFIED\":true," +
				"\"PARENT\":{\"NAME\":\"PARENT\",\"INFO\":\"JSON_OBJECT\"}," +
				"\"CHILDREN\":[{\"NAME\":\"CHILD1\"},{\"NAME\":\"CHILD2\"}]}",
			json.toString());
	}

	/**
	 * Test method
	 */
	@Test
	public void testAppendRelatableWithSerializedTypes() {
		JsonBuilder json = new JsonBuilder().compact();

		Relatable testObj = createTestRelatable();

		testObj.set(Json.JSON_PROPERTY_NAMING, IdentifierStyle.UPPERCASE);
		testObj.set(Json.JSON_SERIALIZED_TYPES,
			Arrays.asList(NAME, INFO, PORT, CHILDREN));

		json.appendRelatable(testObj, null, true);

		assertEquals("{\"NAME\":\"TEST\",\"INFO\":\"JSON\",\"PORT\":12345," +
				"\"CHILDREN\":[{\"NAME\":\"CHILD1\"},{\"NAME\":\"CHILD2\"}]}",
			json.toString());
	}

	/**
	 * Test of
	 * {@link JsonBuilder#append(org.obrel.core.Relation, boolean, boolean)}
	 */
	@Test
	public void testAppendRelation() {
		RelatedObject r = new RelatedObject();

		r.set(TEST_RELATION, "Test");

		JsonBuilder jb = new JsonBuilder();

		assertTrue(
			jb.append(r.getRelation(TEST_RELATION), IdentifierStyle.UPPERCASE,
				false));
		assertEquals(
			String.format("\"%s\": \"Test\"", TEST_RELATION.getSimpleName()),
			jb.toString());

		jb = new JsonBuilder().withNamespaces();

		assertTrue(
			jb.append(r.getRelation(TEST_RELATION), IdentifierStyle.UPPERCASE,
				false));
		assertEquals(String.format("\"%s\": \"Test\"",
				TEST_RELATION.getName()),
			jb.toString());

		jb = new JsonBuilder();
		assertTrue(
			jb.append(r.getRelation(TEST_RELATION), IdentifierStyle.CAMELCASE,
				false));
		assertEquals("\"TestRelation\": \"Test\"", jb.toString());

		jb = new JsonBuilder();
		assertTrue(jb.append(r.getRelation(TEST_RELATION),
			IdentifierStyle.LOWER_CAMELCASE, false));
		assertEquals("\"testRelation\": \"Test\"", jb.toString());

		r.set(TEST_RELATION, null);
		jb = new JsonBuilder();
		assertTrue(
			jb.append(r.getRelation(TEST_RELATION), IdentifierStyle.UPPERCASE,
				true));
		assertEquals(
			String.format("\"%s\": null", TEST_RELATION.getSimpleName()),
			jb.toString());

		jb = new JsonBuilder();
		assertFalse(
			jb.append(r.getRelation(TEST_RELATION), IdentifierStyle.UPPERCASE,
				false));
		assertEquals("", jb.toString());
	}

	/**
	 * Test method
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testAppendValue() {
		assertEquals("null", new JsonBuilder().append(null).toString());
		assertEquals("true",
			new JsonBuilder().append(Boolean.TRUE).toString());
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
		assertEquals("123456789012345678901234567890", new JsonBuilder()
			.append(new BigInteger("123456789012345678901234567890"))
			.toString());
		assertEquals("0.123", new JsonBuilder().append(0.123).toString());
		assertEquals("1.23", new JsonBuilder().append(1.23).toString());
		assertEquals("12300.0", new JsonBuilder().append(1.23e4).toString());
		assertEquals("1.23", new JsonBuilder().append(1.23d).toString());
		assertEquals("12300.0", new JsonBuilder().append(1.23e4d).toString());
		assertEquals("1.23E+4",
			new JsonBuilder().append(new BigDecimal("1.23e4")).toString());

		testAppendDate();

		assertEquals("\"NAME\"",
			new JsonBuilder().append(StandardTypes.NAME).toString());

		assertEquals("\"test string\"",
			new JsonBuilder().append("test string").toString());
		assertEquals("\"\\ttest string\\n\\r\\tmultiline \u011F\"",
			new JsonBuilder()
				.append("\ttest string\n\r\tmultiline \u011f")
				.toString());
	}

	/**
	 * Creates a {@link Relatable} object with test data.
	 */
	protected Relatable createTestRelatable() {
		RelatedObject testObj = new RelatedObject();
		RelatedObject parent = new RelatedObject();
		RelatedObject child1 = new RelatedObject();
		RelatedObject child2 = new RelatedObject();

		parent.set(StandardTypes.NAME, "PARENT");
		parent.set(StandardTypes.INFO, "JSON_OBJECT");

		child1.set(StandardTypes.NAME, "CHILD1");
		child2.set(StandardTypes.NAME, "CHILD2");

		testObj.set(StandardTypes.NAME, "TEST");
		testObj.set(StandardTypes.INFO, "JSON");
		testObj.set(StandardTypes.PORT, 12345);
		testObj.set(MetaTypes.MODIFIED);
		testObj.set(StandardTypes.PARENT, parent);
		testObj.set(StandardTypes.CHILDREN, Arrays.asList(child1, child2));

		return testObj;
	}

	/**
	 * Tests appending a date value.
	 */
	private void testAppendDate() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, 6);
		cal.set(Calendar.DAY_OF_MONTH, 10);
		cal.set(Calendar.HOUR, 12);
		cal.set(Calendar.MINUTE, 6);
		cal.set(Calendar.SECOND, 42);
		cal.set(Calendar.MILLISECOND, 123);

		assertEquals(String.format("\"%s\"",
				Json.JSON_DATE_FORMAT.format(cal.getTime())),
			new JsonBuilder().append(cal.getTime()).toString());
	}
}
