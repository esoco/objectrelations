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

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.collection.CollectionUtil.orderedMapOf;
import static de.esoco.lib.datatype.Pair.t;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
		"	\"NAME\": \"JsonTestObject\",\n" +
		"	\"CLASS_PACKAGE\": \"de.esoco.lib.json\",\n" +
		"	\"OBJECT_ID\": 1,\n" +
		"	\"FLOAT\": 42.001,\n" +
		"	\"DECIMAL\": 3.14159,\n" +
		"	\"FLAG\": true,\n" +
		"	\"ARRAY\": [1, 2, 3, 4, 5],\n" +
		"	\"LIST\": [1, 2, 3, 4],\n" +
		"	\"MAP\": {\n" +
		"		\"k1\": \"v1\",\n" +
		"		\"k2\": \"v2\",\n" +
		"		\"k3\": \"v3\"\n" +
		"	},\n" +
		"	\"CHILD\": null\n" +
		"}";

	private static final Float	    TEST_FLOAT   = 42.001F;
	private static final BigDecimal TEST_DECIMAL = new BigDecimal("3.14159");
	private static final Integer[]  TEST_ARRAY   =
		new Integer[] { 1, 2, 3, 4, 5 };

	private static final List<Integer> TEST_LIST = Arrays.asList(1, 2, 3, 4);

	private static final Map<String, String> TEST_MAP =
		orderedMapOf(t("k1", "v1"), t("k2", "v2"), t("k3", "v3"));

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * JSON parsing test.
	 */
	@Test
	public void testBuildJson()
	{
		JsonTestObject aTestObject = new JsonTestObject();

		aTestObject.setTestValues();

		assertEquals(TEST_OBJECT_JSON, aTestObject.toJson());

		aTestObject.set(JsonTestObject.CHILD, new JsonChildObject(42, "CHILD"));
		System.out.println(aTestObject.toJson());
	}

	/***************************************
	 * JSON building test.
	 */
	@Test
	public void testParseJson()
	{
		JsonTestObject aTestObject = new JsonTestObject();

		aTestObject.fromJson(TEST_OBJECT_JSON);

		assertEquals(JsonTestObject.class.getSimpleName(),
					 aTestObject.get(JsonTestObject.NAME));
		assertEquals(JsonTestObject.class.getPackage().getName(),
					 aTestObject.get(JsonTestObject.CLASS_PACKAGE));
		assertEquals(Integer.valueOf(1),
					 aTestObject.get(JsonTestObject.OBJECT_ID));
		assertEquals(true, aTestObject.get(JsonTestObject.FLAG));
		assertEquals(TEST_FLOAT, aTestObject.get(JsonTestObject.FLOAT));
		assertEquals(TEST_DECIMAL, aTestObject.get(JsonTestObject.DECIMAL));
		assertArrayEquals(TEST_ARRAY, aTestObject.get(JsonTestObject.ARRAY));
		assertEquals(TEST_LIST, aTestObject.get(JsonTestObject.LIST));
		assertEquals(TEST_MAP, aTestObject.get(JsonTestObject.MAP));
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A simple subordinate JSON test object.
	 *
	 * @author eso
	 */
	public static class JsonChildObject
		extends JsonRelatedObject<JsonChildObject>
	{
		//~ Static fields/initializers -----------------------------------------

		static final RelationType<Integer> ID   = newType();
		static final RelationType<String>  NAME = StandardTypes.NAME;

		static
		{
			RelationTypes.init(JsonChildObject.class);
		}

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		public JsonChildObject()
		{
		}

		/***************************************
		 * Sets test values.
		 *
		 * @param nId
		 * @param sName
		 */
		@SuppressWarnings("boxing")
		public JsonChildObject(int nId, String sName)
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
	public static class JsonTestObject extends JsonRelatedObject<JsonTestObject>
	{
		//~ Static fields/initializers -----------------------------------------

		static final RelationType<String> NAME = StandardTypes.NAME;

		static final RelationType<String>     CLASS_PACKAGE = newType();
		static final RelationType<Integer>    OBJECT_ID     = newType();
		static final RelationType<Float>	  FLOAT		    = newType();
		static final RelationType<BigDecimal> DECIMAL	    = newType();
		static final RelationType<Boolean>    FLAG		    = newType();

		static final RelationType<Integer[]>		   ARRAY = newType();
		static final RelationType<List<Integer>>	   LIST  = newType();
		static final RelationType<Map<String, String>> MAP   = newType();
		static final RelationType<JsonChildObject>     CHILD = newType();

		static
		{
			RelationTypes.init(JsonTestObject.class);
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
			set(ARRAY, TEST_ARRAY);
			set(LIST, TEST_LIST);
			set(MAP, TEST_MAP);
			set(CHILD, null);
		}
	}
}
