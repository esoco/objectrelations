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

import java.math.BigDecimal;

import java.util.Arrays;

import org.junit.Test;

import static de.esoco.lib.collection.CollectionUtil.orderedMapOf;
import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of {@link JsonObject}.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class JsonObjectTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String SERIALIZED_JSON =
		"{\n" +
		"	\"testNull\": null,\n" +
		"	\"testString\": \"TEST\",\n" +
		"	\"testFlag\": true,\n" +
		"	\"testInt\": 42,\n" +
		"	\"testDecimal\": 3.14159,\n" +
		"	\"testCollection\": [\"test1\", \"test2\", \"test3\"],\n" +
		"	\"testChild\": {\n" +
		"		\"childName\": \"CHILD\",\n" +
		"		\"childId\": 1\n" +
		"	}\n" +
		"}";

	//~ Instance fields --------------------------------------------------------

	private JsonObject aChildObject =
		new JsonObject(orderedMapOf(t("childName", "CHILD"), t("childId", 1)));
	private JsonObject aJsonObject  =
		new JsonObject(
			orderedMapOf(
				t("testNull", null),
				t("testString", "TEST"),
				t("testFlag", true),
				t("testInt", 42),
				t("testDecimal", new BigDecimal("3.14159")),
				t("testCollection", Arrays.asList("test1", "test2", "test3")),
				t("testChild", aChildObject)));

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link JsonObject#toJson()} (including {@link
	 * JsonObject#appendTo(JsonBuilder)}).
	 */
	@Test
	public void testFromJson()
	{
		assertJsonProperties(new JsonObject().fromJson(SERIALIZED_JSON));
	}

	/***************************************
	 * Test of {@link JsonObject#get(String, Object)}.
	 */
	@Test
	public void testGetProperty()
	{
		assertJsonProperties(aJsonObject);
		aJsonObject.set("testLong", 555L);
		assertEquals(
			Long.valueOf(555),
			aJsonObject.getNumber("testLong").orFail());
	}

	/***************************************
	 * Test of {@link JsonObject#set(String, Object)}.
	 */
	@Test
	public void testSetProperty()
	{
		JsonObject aTestObject = new JsonObject();

		aTestObject.set("testName", "TEST");
		aTestObject.set("testObject", new JsonObject());

		assertEquals("TEST", aTestObject.getString("testName").orFail());
		assertEquals(
			new JsonObject(),
			aTestObject.getObject("testObject").orFail());
	}

	/***************************************
	 * Test of {@link JsonObject#toJson()} (including {@link
	 * JsonObject#appendTo(JsonBuilder)}).
	 */
	@Test
	public void testToJson()
	{
		String sJson = aJsonObject.toJson();

		assertEquals(SERIALIZED_JSON, sJson);
	}

	/***************************************
	 * Asserts that the given object contains all test properties.
	 *
	 * @param rJsonObject The JSON object
	 */
	private void assertJsonProperties(JsonObject rJsonObject)
	{
		assertEquals(Option.none(), rJsonObject.getProperty("testNull"));
		assertTrue(rJsonObject.hasFlag("testFlag"));
		assertTrue(rJsonObject.hasProperty("testNull"));
		assertEquals(
			"Not",
			rJsonObject.getString("testNotExisting").orUse("Not"));
		assertEquals("TEST", rJsonObject.getString("testString").orFail());
		assertEquals(
			Integer.valueOf(42),
			rJsonObject.getNumber("testInt").orFail());
		assertEquals(
			new BigDecimal("3.14159"),
			rJsonObject.getNumber("testDecimal").orFail());
		assertEquals(
			Arrays.asList("test1", "test2", "test3"),
			rJsonObject.getArray("testCollection").orFail());
		assertEquals(aChildObject, rJsonObject.getObject("testChild").orFail());
	}
}
