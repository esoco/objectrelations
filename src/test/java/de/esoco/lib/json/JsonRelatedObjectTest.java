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

import de.esoco.lib.text.TextConvert.IdentifierStyle;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Test of {@link JsonRelatedObject}.
 *
 * @author eso
 */
@SuppressWarnings("boxing")
public class JsonRelatedObjectTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String TEST_OBJECT_JSON =
		"{\n" +
		"	\"NAME\": \"TestObject\",\n" +
		"	\"CLASS_PACKAGE\": \"de.esoco.lib.json\",\n" +
		"	\"OBJECT_ID\": 1,\n" +
		"	\"FLOAT\": 42.001,\n" +
		"	\"DECIMAL\": 3.14159,\n" +
		"	\"FLAG\": true,\n" +
		"	\"BYTE_ARRAY\": [49, 50, 51, 52, 53],\n" +
		"	\"INTEGER_ARRAY\": [1, 2, 3, 4, 5],\n" +
		"	\"LIST\": [1, 2, 3, 4],\n" +
		"	\"JSON_OBJECT\": {\n" +
		"		\"k1\": \"v1\",\n" +
		"		\"k2\": \"v2\",\n" +
		"		\"k3\": \"v3\"\n" +
		"	}%s\n" +
		"}";

	private static final String TEST_OBJECT_NULL_CHILD = ",\n	\"CHILD\": null";
	private static final String TEST_OBJECT_ONE_CHILD  =
		",\n" +
		"	\"CHILD\": {\n" +
		"		\"ID\": 42,\n" +
		"		\"NAME\": \"CHILD\"\n" +
		"	}";

	private static final Float	    TEST_FLOAT		   = 42.001F;
	private static final BigDecimal TEST_DECIMAL	   =
		new BigDecimal("3.14159");
	private static final byte[]     TEST_BYTE_ARRAY    =
		new byte[] { 0x31, 0x32, 0x33, 0x34, 0x35 };
	private static final Integer[]  TEST_INTEGER_ARRAY =
		new Integer[] { 1, 2, 3, 4, 5 };

	private static final List<Integer> TEST_LIST = Arrays.asList(1, 2, 3, 4);

	private static final JsonObject TEST_OBJECT =
		new JsonObject(t("k1", "v1"), t("k2", "v2"), t("k3", "v3"));

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * JSON parsing test.
	 */
	@Test
	public void testBuildJson()
	{
		TestObject aTestObject = new TestObject();

		aTestObject.setTestValues();
		aTestObject.set(Json.JSON_PROPERTY_NAMING, IdentifierStyle.UPPERCASE);

		assertEquals(String.format(TEST_OBJECT_JSON, TEST_OBJECT_NULL_CHILD),
					 aTestObject.toJson());

		ChildObject aChild = new ChildObject(42, "CHILD");

		aChild.set(Json.JSON_PROPERTY_NAMING, IdentifierStyle.UPPERCASE);
		aTestObject.set(TestObject.CHILD, aChild);

		assertEquals(String.format(TEST_OBJECT_JSON, TEST_OBJECT_ONE_CHILD),
					 aTestObject.toJson());
	}

	/***************************************
	 * JSON building test.
	 */
	@Test
	public void testParseJson()
	{
		TestObject aTestObject = new TestObject();

		aTestObject.fromJson(String.format(TEST_OBJECT_JSON,
										   TEST_OBJECT_NULL_CHILD));

		assertJsonRelations(aTestObject);
		assertNull(aTestObject.get(TestObject.CHILD));

		aTestObject.fromJson(String.format(TEST_OBJECT_JSON,
										   TEST_OBJECT_ONE_CHILD));

		assertJsonRelations(aTestObject);
		assertEquals(new ChildObject(42, "CHILD"),
					 aTestObject.get(TestObject.CHILD));
	}

	/***************************************
	 * Assert correct relation values.
	 *
	 * @param rTestObject The test object
	 */
	private void assertJsonRelations(TestObject rTestObject)
	{
		assertEquals(TestObject.class.getSimpleName(),
					 rTestObject.get(TestObject.NAME));
		assertEquals(TestObject.class.getPackage().getName(),
					 rTestObject.get(TestObject.CLASS_PACKAGE));
		assertEquals(Integer.valueOf(1), rTestObject.get(TestObject.OBJECT_ID));
		assertEquals(true, rTestObject.get(TestObject.FLAG));
		assertEquals(TEST_FLOAT, rTestObject.get(TestObject.FLOAT));
		assertEquals(TEST_DECIMAL, rTestObject.get(TestObject.DECIMAL));
		assertArrayEquals(TEST_INTEGER_ARRAY,
						  rTestObject.get(TestObject.INTEGER_ARRAY));
		assertEquals(TEST_LIST, rTestObject.get(TestObject.LIST));
		assertEquals(TEST_OBJECT, rTestObject.get(TestObject.JSON_OBJECT));
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A simple subordinate JSON test object.
	 *
	 * @author eso
	 */
	public static class ChildObject extends JsonRelatedObject<ChildObject>
	{
		//~ Static fields/initializers -----------------------------------------

		static final RelationType<Integer> ID   = newType();
		static final RelationType<String>  NAME = StandardTypes.NAME;

		static
		{
			RelationTypes.init(ChildObject.class);
		}

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public ChildObject()
		{
		}

		/***************************************
		 * Sets test values.
		 *
		 * @param nId
		 * @param sName
		 */
		@SuppressWarnings("boxing")
		public ChildObject(int nId, String sName)
		{
			set(ID, nId);
			set(NAME, sName);
		}
	}

	/********************************************************************
	 * A test object that contains some JSON relation type attributes.
	 *
	 * @author eso
	 */
	public static class TestObject extends JsonRelatedObject<TestObject>
	{
		//~ Static fields/initializers -----------------------------------------

		static final RelationType<String> NAME = StandardTypes.NAME;

		static final RelationType<String>		 CLASS_PACKAGE = newType();
		static final RelationType<Integer>		 OBJECT_ID     = newType();
		static final RelationType<Float>		 FLOAT		   = newType();
		static final RelationType<BigDecimal>    DECIMAL	   = newType();
		static final RelationType<Boolean>		 FLAG		   = newType();
		static final RelationType<byte[]>		 BYTE_ARRAY    = newType();
		static final RelationType<Integer[]>     INTEGER_ARRAY = newType();
		static final RelationType<List<Integer>> LIST		   = newType();
		static final RelationType<JsonObject>    JSON_OBJECT   = newType();
		static final RelationType<ChildObject>   CHILD		   = newType();

		static
		{
			RelationTypes.init(TestObject.class);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Sets test values
		 */
		void setTestValues()
		{
			set(NAME, getClass().getSimpleName());
			set(CLASS_PACKAGE, getClass().getPackage().getName());
			set(OBJECT_ID, 1);
			set(FLOAT, TEST_FLOAT);
			set(DECIMAL, TEST_DECIMAL);
			set(FLAG);
			set(BYTE_ARRAY, TEST_BYTE_ARRAY);
			set(INTEGER_ARRAY, TEST_INTEGER_ARRAY);
			set(LIST, TEST_LIST);
			set(JSON_OBJECT, TEST_OBJECT);
			set(CHILD, null);
		}
	}
}
