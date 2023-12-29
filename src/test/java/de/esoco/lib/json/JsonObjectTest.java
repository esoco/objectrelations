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

import de.esoco.lib.expression.monad.Option;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static de.esoco.lib.collection.CollectionUtil.orderedMapOf;
import static de.esoco.lib.datatype.Pair.t;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of {@link JsonObject}.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class JsonObjectTest {
	private static final String SERIALIZED_JSON =
		"{\n" + "	\"testNull\": null,\n" +
			"	\"testString\": \"TEST\",\n" + "	\"testFlag\": true,\n" +
			"	\"testInt\": 42,\n" + "	\"testDecimal\": 3.14159,\n" +
			"	\"testCollection\": [\"test1\", \"test2\", \"test3\"],\n" +
			"	\"testChild\": {\n" + "		\"childName\": \"CHILD\",\n" +
			"		\"childId\": 1\n" + "	}\n" + "}";

	private final JsonObject childObject =
		new JsonObject(orderedMapOf(t("childName", "CHILD"), t("childId", 1)));

	private final JsonObject jsonObject = new JsonObject(
		orderedMapOf(t("testNull", null), t("testString", "TEST"),
			t("testFlag", true), t("testInt", 42),
			t("testDecimal", new BigDecimal("3.14159")),
			t("testCollection", Arrays.asList("test1", "test2", "test3")),
			t("testChild", childObject)));

	/**
	 * Test of {@link JsonObject#toJson()} (including
	 * {@link JsonObject#appendTo(JsonBuilder)}).
	 */
	@Test
	public void testFromJson() {
		assertJsonProperties(new JsonObject().fromJson(SERIALIZED_JSON));
	}

	/**
	 * Test of {@link JsonObject#get(String, Object)}.
	 */
	@Test
	public void testGetProperty() {
		assertJsonProperties(jsonObject);
		jsonObject.set("testLong", 555L);
		assertEquals(Long.valueOf(555),
			jsonObject.getNumber("testLong").orFail());
	}

	/**
	 * Test of {@link JsonObject#set(String, Object)}.
	 */
	@Test
	public void testSetProperty() {
		JsonObject testObject = new JsonObject();

		testObject.set("testName", "TEST");
		testObject.set("testObject", new JsonObject());

		assertEquals("TEST", testObject.getString("testName").orFail());
		assertEquals(new JsonObject(),
			testObject.getObject("testObject").orFail());
	}

	/**
	 * Test of {@link JsonObject#toJson()} (including
	 * {@link JsonObject#appendTo(JsonBuilder)}).
	 */
	@Test
	public void testToJson() {
		String json = jsonObject.toJson();

		assertEquals(SERIALIZED_JSON, json);
	}

	/**
	 * Asserts that the given object contains all test properties.
	 *
	 * @param jsonObject The JSON object
	 */
	private void assertJsonProperties(JsonObject jsonObject) {
		assertEquals(Option.none(), jsonObject.getProperty("testNull"));
		assertTrue(jsonObject.hasFlag("testFlag"));
		assertTrue(jsonObject.hasProperty("testNull"));
		assertEquals("Not",
			jsonObject.getString("testNotExisting").orUse("Not"));
		assertEquals("TEST", jsonObject.getString("testString").orFail());
		assertEquals(Integer.valueOf(42),
			jsonObject.getNumber("testInt").orFail());
		assertEquals(new BigDecimal("3.14159"),
			jsonObject.getNumber("testDecimal").orFail());
		assertEquals(Arrays.asList("test1", "test2", "test3"),
			jsonObject.getArray("testCollection").orFail());
		assertEquals(childObject, jsonObject.getObject("testChild").orFail());
	}
}
