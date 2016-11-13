//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.function.AbstractAction;
import de.esoco.lib.json.JsonUtil.JsonStructure;
import de.esoco.lib.reflect.ReflectUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

import static de.esoco.lib.json.JsonUtil.JSON_DATE_FORMAT;


/********************************************************************
 * A parser for the creation of {@link Relatable} objects from JSON data.
 *
 * @author eso
 */
public class JsonParser
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private JsonParser()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Parses a JSON array into a collection.
	 *
	 * @param  sJsonArray  The JSON array string
	 * @param  rCollection The target collection
	 *
	 * @return The input collection containing the parsed array values
	 */
	public static <C extends Collection<Object>> C parseCollection(
		String  sJsonArray,
		final C rCollection)
	{
		parseStructure(sJsonArray,
					   JsonStructure.ARRAY,
			new AbstractAction<String>("ParseJsonArrayElement")
			{
				@Override
				public void execute(String sArrayElement)
				{
					rCollection.add(parseValue(sArrayElement));
				}
			});

		return rCollection;
	}

	/***************************************
	 * Parses a numeric value from a JSON string.
	 *
	 * @param  sJsonValue The JSON value to parse
	 *
	 * @return The corresponding {@link Number} subclass for the input value
	 */
	@SuppressWarnings("boxing")
	public static Number parseJsonNumber(String sJsonValue)
	{
		Number aNumber;

		if (sJsonValue.indexOf('.') > 0)
		{
			aNumber = new BigDecimal(sJsonValue);
		}
		else
		{
			BigInteger aBigInt    = new BigInteger(sJsonValue);
			int		   nBitLength = aBigInt.bitLength();

			if (nBitLength <= 32)
			{
				aNumber = aBigInt.intValue();
			}
			else if (nBitLength <= 64)
			{
				aNumber = aBigInt.longValue();
			}
			else
			{
				aNumber = aBigInt;
			}
		}

		return aNumber;
	}

	/***************************************
	 * Parses a JSON object into a map.
	 *
	 * @param  sJsonObject The JSON object string
	 * @param  rMap        The target map
	 *
	 * @return The input map containing the parsed object attributes
	 */
	public static Map<String, Object> parseMap(
		String					  sJsonObject,
		final Map<String, Object> rMap)
	{
		parseStructure(sJsonObject,
					   JsonStructure.OBJECT,
			new AbstractAction<String>("ParseJsonObjectElement")
			{
				@Override
				public void execute(String sMapping)
				{
					int    nPos		  = sMapping.indexOf(':');
					String sKey		  = sMapping.substring(1, nPos - 1).trim();
					String sJsonValue = sMapping.substring(nPos + 1).trim();

					rMap.put(sKey, parseValue(sJsonValue));
				}
			});

		return rMap;
	}

	/***************************************
	 * Parses a JSON number string into a Java {@link Number} subclass instance.
	 *
	 * @param  sJsonNumber The JSON number value
	 * @param  rDatatype   The target datatype
	 *
	 * @return The resulting value or NULL if no mapping exists
	 */
	public static Number parseNumber(
		String					sJsonNumber,
		Class<? extends Number> rDatatype)
	{
		Number rValue = null;

		if (rDatatype == Integer.class)
		{
			rValue = Integer.valueOf(sJsonNumber);
		}
		else if (rDatatype == Long.class)
		{
			rValue = Long.valueOf(sJsonNumber);
		}
		else if (rDatatype == Short.class)
		{
			rValue = Short.valueOf(sJsonNumber);
		}
		else if (rDatatype == Byte.class)
		{
			rValue = Byte.valueOf(sJsonNumber);
		}
		else if (rDatatype == BigInteger.class)
		{
			rValue = new BigInteger(sJsonNumber);
		}
		else if (rDatatype == BigDecimal.class)
		{
			rValue = new BigDecimal(sJsonNumber);
		}
		else if (rDatatype == Float.class)
		{
			rValue = Float.valueOf(sJsonNumber);
		}
		else if (rDatatype == Double.class)
		{
			rValue = Double.valueOf(sJsonNumber);
		}

		return rValue;
	}

	/***************************************
	 * Parses a JSON object from a string into the relations of a relatable
	 * target object.
	 *
	 * @param  sJsonObject sJson The string containing a JSON object
	 * @param  rTarget     The relatable target object to set the parsed
	 *                     relations on
	 *
	 * @return The input relatable, containing the parsed relations
	 *
	 * @see    #parseRelation(String, Relatable)
	 */
	public static <R extends Relatable> R parseRelatable(
		String  sJsonObject,
		final R rTarget)
	{
		parseStructure(sJsonObject,
					   JsonStructure.OBJECT,
			new AbstractAction<String>("ParseJsonArrayElement")
			{
				@Override
				public void execute(String sObjectElement)
				{
					parseRelation(sObjectElement, rTarget);
				}
			});

		return rTarget;
	}

	/***************************************
	 * Parses a relation from a JSON string into {@link Relatable} object. The
	 * JSON input string must be in a compatible format as generated by the
	 * method {@link #appendRelation(StringBuilder, Relation, boolean, boolean)}
	 * or else the parsing may cause errors. Furthermore all relation types
	 * referenced in the JSON must have their full namespace and must have been
	 * created as instances or else their lookup will fail.
	 *
	 * @param sJson   The JSON input string
	 * @param rTarget The related object to set the relation in
	 */
	@SuppressWarnings("unchecked")
	public static void parseRelation(String sJson, Relatable rTarget)
	{
		int    nColon     = sJson.indexOf(':');
		String sTypeName  = sJson.substring(1, nColon - 1).trim();
		String sJsonValue = sJson.substring(nColon + 1).trim();

		RelationType<?> rRelationType = RelationType.valueOf(sTypeName);

		Object rValue = parseValue(sJsonValue, rRelationType.getTargetType());

		rTarget.set((RelationType<Object>) rRelationType, rValue);
	}

	/***************************************
	 * Parses a JSON string value according to it's JSON datatype. For more
	 * enhanced datatype parsing see {@link #parseValue(String, Class)}.
	 *
	 * @param  sJsonValue The JSON value string
	 *
	 * @return The parsed value
	 */
	public static Object parseValue(String sJsonValue)
	{
		Object aValue;

		if (sJsonValue.charAt(0) == JsonStructure.STRING.cOpen)
		{
			aValue = sJsonValue.substring(1, sJsonValue.length() - 1);
		}
		else if (sJsonValue.charAt(0) == JsonStructure.OBJECT.cOpen)
		{
			aValue = parseMap(sJsonValue, new LinkedHashMap<String, Object>());
		}
		else if (sJsonValue.charAt(0) == JsonStructure.ARRAY.cOpen)
		{
			aValue = parseCollection(sJsonValue, new ArrayList<>());
		}
		else if (sJsonValue.equals("null"))
		{
			aValue = null;
		}
		else if (sJsonValue.equals("true") || sJsonValue.equals("false"))
		{
			aValue = Boolean.valueOf(sJsonValue);
		}
		else
		{
			aValue = parseJsonNumber(sJsonValue);
		}

		return aValue;
	}

	/***************************************
	 * Parses a JSON string value into a certain datatype. The value must be in
	 * a format as generated by {@link JsonBuilder#appendValue(Object)}.
	 *
	 * @param  sJsonValue The JSON value string
	 * @param  rDatatype  The target datatype
	 *
	 * @return The parsed value
	 */
	@SuppressWarnings("unchecked")
	public static Object parseValue(String sJsonValue, Class<?> rDatatype)
	{
		Object rValue = null;

		if (rDatatype == Boolean.class)
		{
			rValue = Boolean.valueOf(sJsonValue);
		}
		else if (Number.class.isAssignableFrom(rDatatype))
		{
			rValue =
				parseNumber(sJsonValue, (Class<? extends Number>) rDatatype);
		}
		else if (Collection.class.isAssignableFrom(rDatatype))
		{
			Collection<Object> aCollection =
				Set.class.isAssignableFrom(rDatatype) ? new HashSet<>()
													  : new ArrayList<>();

			rValue = parseCollection(sJsonValue, aCollection);
		}
		else if (Map.class.isAssignableFrom(rDatatype))
		{
			rValue = parseMap(sJsonValue, new LinkedHashMap<String, Object>());
		}
		else if (Date.class.isAssignableFrom(rDatatype))
		{
			sJsonValue = getContent(sJsonValue, JsonStructure.STRING);

			try
			{
				rValue = JSON_DATE_FORMAT.parse(sJsonValue);
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e);
			}
		}
		else if (Relatable.class.isAssignableFrom(rDatatype))
		{
			Relatable aRelatable;

			if (rDatatype == Relatable.class)
			{
				aRelatable = new RelatedObject();
			}
			else
			{
				aRelatable = (Relatable) ReflectUtil.newInstance(rDatatype);
			}

			rValue = parseRelatable(sJsonValue, aRelatable);
		}
		else
		{
			sJsonValue = getContent(sJsonValue, JsonStructure.STRING);
			rValue     = Conversions.parseValue(sJsonValue, rDatatype);
		}

		return rValue;
	}

	/***************************************
	 * Extracts the content from a JSON structure (object, array, or string). If
	 * the structure doesn't match the expected format an exception will be
	 * thrown.
	 *
	 * @param  sJsonStructure The string containing the JSON structure
	 * @param  eStructure     sStructureDelimiters A string that is exactly 2
	 *                        characters long and contains the delimiters of the
	 *                        JSON structure (brackets, braces, quotation marks)
	 *
	 * @return The extracted structure content
	 *
	 * @throws IllegalArgumentException If the content doesn't represent the
	 *                                  expected structure
	 */
	private static String getContent(
		String		  sJsonStructure,
		JsonStructure eStructure)
	{
		if (sJsonStructure.charAt(0) != eStructure.cOpen ||
			sJsonStructure.charAt(sJsonStructure.length() - 1) !=
			eStructure.cClose)
		{
			throw new IllegalArgumentException("Not a JSON " +
											   eStructure.name().toLowerCase());
		}

		return sJsonStructure.substring(1, sJsonStructure.length() - 1).trim();
	}

	/***************************************
	 * Parses a JSON structure and executes an action for each element.
	 *
	 * @param sJsonData       The JSON string to parse
	 * @param eStructure      The type of the JSON structure
	 * @param fProcessElement The action to execute for each structure element
	 */
	private static void parseStructure(String		  sJsonData,
									   JsonStructure  eStructure,
									   Action<String> fProcessElement)
	{
		String sJson		 = getContent(sJsonData, eStructure);
		int    nMax			 = sJson.length() - 1;
		int    nElementStart = 0;
		int    nElementEnd   = 0;

		while (nElementEnd < nMax)
		{
			boolean bInString =
				(sJson.charAt(nElementEnd) == JsonStructure.STRING.cOpen);
//			boolean bInStructure =
//				(sJson.charAt(nElementEnd) == JsonStructure.ARRAY.cOpen ||
//				 sJson.charAt(nElementEnd) == JsonStructure.OBJECT.cOpen);

			do
			{
				nElementEnd++;

				// toggle in string and out of string by considering escaped
				// quotation marks in strings
				if (sJson.charAt(nElementEnd) == '"' &&
					(!bInString || sJson.charAt(nElementEnd - 1) != '\\'))
				{
					bInString = !bInString;
				}
			} // loop until mapping separator (,) or string end is found
			while ((bInString || sJson.charAt(nElementEnd) != ',') &&
				   nElementEnd < nMax);

			// include last character (because there is no separator at the end)
			if (nElementEnd == nMax)
			{
				nElementEnd++;
			}

			fProcessElement.execute(sJson.substring(nElementStart,
													nElementEnd).trim());

			nElementStart = ++nElementEnd;
		}
	}
}