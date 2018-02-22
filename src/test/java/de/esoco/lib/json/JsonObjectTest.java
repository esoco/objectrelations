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

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Test of {@link JsonObject}.
 *
 * @author eso
 */
public class JsonObjectTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * JSON parsing test.
	 */
	@Test
	public void testBuildJson()
	{
		JsonTestObject aTestObject = new JsonTestObject();

		System.out.printf("JSON: %s\n", aTestObject.toJson());
	}

	/***************************************
	 * JSON building test.
	 */
	@Test
	public void testParseJson()
	{
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A test object that contains some JSON relation type attributes.
	 *
	 * @author eso
	 */
	static class JsonTestObject extends JsonObject<JsonTestObject>
	{
		//~ Static fields/initializers -----------------------------------------

		static final RelationType<String> NAME = StandardTypes.NAME;

		static final RelationType<String>			   PACKAGE = newType();
		static final RelationType<Integer>			   ID	   = newType();
		static final RelationType<Float>			   FLOAT   = newType();
		static final RelationType<BigDecimal>		   DECIMAL = newType();
		static final RelationType<Boolean>			   FLAG    = newType();
		static final RelationType<Integer[]>		   ARRAY   = newType();
		static final RelationType<List<Integer>>	   LIST    = newType();
		static final RelationType<Map<String, String>> MAP     = newType();

		static
		{
			RelationTypes.init(JsonTestObject.class);
		}

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		@SuppressWarnings("boxing")
		public JsonTestObject()
		{
			set(NAME, getClass().getSimpleName());
			set(PACKAGE, getClass().getPackage().getName());
			set(ID, 1);
			set(FLOAT, 42.001F);
			set(DECIMAL, new BigDecimal("3.14159"));
			set(FLAG);
			set(ARRAY, new Integer[] { 1, 2, 3, 4, 5 });
			set(LIST, Arrays.asList(1, 2, 3, 4));
			set(MAP, orderedMapOf(t("k1", "v1"), t("k2", "v2"), t("k3", "v3")));
		}
	}
}
