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
package de.esoco.lib.expression;

import static de.esoco.lib.datatype.Pair.t;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.obrel.type.StandardTypes;

import de.esoco.lib.collection.CollectionUtil;

/**
 * Test of {@link Conversions}.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class ConversionsTest {

	private static final String PI_STRING = "3.1415";

	private static final Collection<Integer> TEST_COLLECTION =
		Arrays.asList(1, 2, 3, 4, 5);

	private static final Map<Integer, String> TEST_MAP =
		CollectionUtil.mapOf(t(1, "A"), t(2, "B"), t(3, "C"));

	private static final Collection<String> ENCODE_COLLECTION =
		Arrays.asList("1,2", "2,3", "4,5");

	private static final Map<Integer, String> ENCODE_MAP =
		CollectionUtil.mapOf(t(1, "A={x,y}"), t(2, "B:(s,t,u)"),
			t(3, "C;[e-f, k+m, ::p]"));

	private static final String DEFAULT_COLLECTION_STRING = "1,2,3,4,5";

	private static final String MODIFIED_COLLECTION_STRING = "1;2;3;4;5";

	private static final String ENCODED_COLLECTION_STRING =
		"1\\u002C2,2\\u002C3,4\\u002C5";

	private static final String DEFAULT_MAP_STRING = "1=A,2=B,3=C";

	private static final String MODIFIED_MAP_STRING = "1:A;2:B;3:C";

	private static final String ENCODED_MAP_STRING =
		"1=A={x\\u002Cy},2=B:(s\\u002Ct\\u002Cu),3=C;[e-f\\u002C k+m\\u002C " +
			"::p]";

	/**
	 * Test
	 */
	@Test
	public void testAsString() {
		assertEquals("TEST", Conversions.asString("TEST"));
		assertEquals("true", Conversions.asString(Boolean.TRUE));
		assertEquals("42", Conversions.asString(Integer.valueOf(42)));
		assertEquals(Conversions.class.getName(),
			Conversions.asString(Conversions.class));
		assertEquals(PI_STRING,
			Conversions.asString(new BigDecimal(PI_STRING)));
	}

	/**
	 * Test
	 */
	@Test
	public void testAsStringCollection() {
		assertEquals(DEFAULT_COLLECTION_STRING,
			Conversions.asString(TEST_COLLECTION));
		assertEquals(MODIFIED_COLLECTION_STRING,
			Conversions.asString(TEST_COLLECTION, ";"));
		assertEquals(ENCODED_COLLECTION_STRING,
			Conversions.asString(ENCODE_COLLECTION));
	}

	/**
	 * Test
	 */
	@Test
	public void testAsStringMap() {
		assertEquals(DEFAULT_MAP_STRING, Conversions.asString(TEST_MAP));
		assertEquals(MODIFIED_MAP_STRING,
			Conversions.asString(TEST_MAP, ";", ":"));
		assertEquals(ENCODED_MAP_STRING, Conversions.asString(ENCODE_MAP));
	}

	/**
	 * Test
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testParseCollection() {
		List<Integer> parsedList =
			Conversions.parseCollection(DEFAULT_COLLECTION_STRING, List.class,
				Integer.class, false);

		assertEquals(TEST_COLLECTION, parsedList);

		parsedList =
			Conversions.parseCollection(MODIFIED_COLLECTION_STRING, List.class,
				Integer.class, ";", false);

		assertEquals(TEST_COLLECTION, parsedList);

		parsedList =
			Conversions.parseCollection(ENCODED_COLLECTION_STRING, List.class,
				String.class, false);

		assertEquals(ENCODE_COLLECTION, parsedList);
	}

	/**
	 * Test
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testParseMap() {
		Map<Integer, String> parsedMap =
			Conversions.parseMap(DEFAULT_MAP_STRING, Map.class, Integer.class,
				String.class, false);

		assertEquals(TEST_MAP, parsedMap);

		parsedMap =
			Conversions.parseMap(MODIFIED_MAP_STRING, Map.class, Integer.class,
				String.class, ";", ":", false);

		assertEquals(TEST_MAP, parsedMap);

		parsedMap =
			Conversions.parseMap(ENCODED_MAP_STRING, Map.class, Integer.class,
				String.class, false);

		assertEquals(ENCODE_MAP, parsedMap);
	}

	/**
	 * Test
	 */
	@Test
	public void testParseValue() {
		Date date = new Date();
		BigDecimal bigDecimal = new BigDecimal(PI_STRING);

		assertEquals("TEST", Conversions.parseValue("TEST", String.class));
		assertEquals(Boolean.TRUE,
			Conversions.parseValue("true", Boolean.class));
		assertEquals(Integer.valueOf(42),
			Conversions.parseValue("42", Integer.class));
		assertEquals(date,
			Conversions.parseValue(Conversions.asString(date), Date.class));
		assertEquals(bigDecimal,
			Conversions.parseValue(Conversions.asString(bigDecimal),
				BigDecimal.class));
		assertEquals(Conversions.class,
			Conversions.parseValue(Conversions.class.getName(), Class.class));
	}

	/**
	 * Test
	 */
	@Test
	public void testParseValueOfRelationType() {
		Date date = new Date();

		assertEquals("TEST",
			Conversions.parseValue("TEST", StandardTypes.NAME));
		assertEquals(Integer.valueOf(42),
			Conversions.parseValue("42", StandardTypes.MINIMUM));
		assertEquals(date, Conversions.parseValue(Conversions.asString(date),
			StandardTypes.DATE));
	}
}
