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

import de.esoco.lib.expression.Action;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.json.Json.JsonStructure;
import de.esoco.lib.property.ErrorHandling;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;

import java.lang.reflect.Array;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

import static de.esoco.lib.json.Json.JSON_DATE_FORMAT;

import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.ERROR_HANDLING;


/********************************************************************
 * A parser for the JSON data that can also be used to parse the relations of
 * {@link Relatable} objects.
 *
 * @author eso
 */
public class JsonParser
{
	//~ Instance fields --------------------------------------------------------

	private int nDepth;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that parses the full hierarchy of a JSON string
	 * (limited to {@link Short#MAX_VALUE}).
	 */
	public JsonParser()
	{
		this(Short.MAX_VALUE);
	}

	/***************************************
	 * Creates a new instance that parses only a certain number of hierarchy
	 * levels. If the depth reaches zero all properties will be left as the
	 * original JSON strings. This can only be for parsing untyped data through
	 * methods like {@link #parse(String)} or for providing typed objects (like
	 * relatables) with the correct string datatypes at the levels where the
	 * parsing is terminated if the maximum depth is reached.
	 *
	 * <p>Limiting the depth allows to prevent unnecessary parsing if only
	 * sub-structures of JSON data need to be evaluated. For example, in JSON
	 * RPC requests and responses only the params and result properties are of
	 * interest and should be parsed. By parsing such a record with {@link
	 * #parseObject(String)} or {@link #parseObjectMap(String)} and a depth of 1
	 * these properties can then be queried from the returned object as strings
	 * and parsed into a typed object.</p>
	 *
	 * @param nDepth The maximum depth of properties to parse
	 */
	public JsonParser(int nDepth)
	{
		this.nDepth = nDepth;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a function that parses a JSON string into objects. The parsing is
	 * done by {@link #parse(String)}.
	 *
	 * @return A function that parses JSON string into objects
	 */
	public static Function<String, Object> parseJson()
	{
		return sJson -> new JsonParser().parse(sJson);
	}

	/***************************************
	 * Returns a binary function that parses a JSON string into an Object with a
	 * certain datatype by invoking {@link #parse(String, Class)}.
	 *
	 * @param  rDatatype The preset datatype for unary function invocation
	 *
	 * @return The parsing function
	 */
	public static <T> Function<String, T> parseJson(Class<T> rDatatype)
	{
		return sJson -> new JsonParser().parse(sJson, rDatatype);
	}

	/***************************************
	 * Returns a function that parses a JSON array into a list with a specific
	 * element type.
	 *
	 * @param  rElementType The datatype of the list elements
	 *
	 * @return The parsing function
	 */
	public static <T> Function<String, List<T>> parseJsonArray(
		Class<T> rElementType)
	{
		return sJson -> new JsonParser().parseArray(sJson, rElementType);
	}

	/***************************************
	 * Returns a function that parses a JSON object and returns a map containing
	 * the parsed values. The returned map preserves the order of the parsed
	 * values in the input string.
	 *
	 * @return The parsing function
	 */
	public static Function<String, Map<String, Object>> parseJsonMap()
	{
		return sJson -> new JsonParser().parseObjectMap(sJson);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Parses a JSON string value according to it's JSON datatype. For more
	 * enhanced datatype parsing see {@link #parse(String, Class)}.
	 *
	 * @param  sJson The JSON value string
	 *
	 * @return The parsed value (NULL if input is NULL or empty)
	 *
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public Object parse(String sJson)
	{
		Object aValue;

		if (sJson == null || sJson.isEmpty())
		{
			aValue = null;
		}
		else if (nDepth <= 0)
		{
			aValue = sJson;
		}
		else if (sJson.charAt(0) == JsonStructure.STRING.cOpen)
		{
			aValue = Json.restore(sJson.substring(1, sJson.length() - 1));
		}
		else if (sJson.charAt(0) == JsonStructure.OBJECT.cOpen)
		{
			aValue = parseObject(sJson);
		}
		else if (sJson.charAt(0) == JsonStructure.ARRAY.cOpen)
		{
			aValue = parseArray(sJson, new ArrayList<>());
		}
		else if (sJson.equals("null"))
		{
			aValue = null;
		}
		else if (sJson.equals("true") || sJson.equals("false"))
		{
			aValue = Boolean.valueOf(sJson);
		}
		else
		{
			aValue = parseNumber(sJson);
		}

		return aValue;
	}

	/***************************************
	 * Parses a JSON string value into a certain datatype.
	 *
	 * @param  sJsonValue The JSON value string
	 * @param  rDatatype  The target datatype
	 *
	 * @return The parsed value
	 *
	 * @throws RuntimeException If the input string is not valid JSON or doesn't
	 *                          match the given datatype
	 */
	@SuppressWarnings("unchecked")
	public <T> T parse(String sJsonValue, Class<? extends T> rDatatype)
	{
		Object rValue;

		if ("null".equals(sJsonValue))
		{
			rValue = null;
		}
		else if (JsonSerializable.class.isAssignableFrom(rDatatype))
		{
			rValue = ReflectUtil.newInstance(rDatatype);

			((JsonSerializable<?>) rValue).fromJson(sJsonValue);
		}
		else if (rDatatype == Boolean.class || rDatatype == boolean.class)
		{
			rValue = Boolean.valueOf(sJsonValue);
		}
		else if (rDatatype.isPrimitive())
		{
			// all non-boolean primitives must be numbers as character values
			// are not supported in JSON
			rValue =
				parseNumber(sJsonValue,
							(Class<? extends Number>) ReflectUtil
							.getWrapperType(rDatatype));
		}
		else if (Number.class.isAssignableFrom(rDatatype))
		{
			rValue =
				parseNumber(sJsonValue, (Class<? extends Number>) rDatatype);
		}
		else if (rDatatype.isArray())
		{
			rValue = parseIntoArray(sJsonValue, rDatatype);
		}
		else if (Collection.class.isAssignableFrom(rDatatype))
		{
			Collection<Object> aCollection =
				Set.class.isAssignableFrom(rDatatype) ? new HashSet<>()
													  : new ArrayList<>();

			rValue = parseArray(sJsonValue, aCollection);
		}
		else if (Map.class.isAssignableFrom(rDatatype))
		{
			rValue = parseObject(sJsonValue);
		}
		else if (Date.class.isAssignableFrom(rDatatype))
		{
			rValue = parseDate(sJsonValue);
		}
		else if (Relatable.class.isAssignableFrom(rDatatype))
		{
			rValue = parseRelatable(sJsonValue, rDatatype);
		}
		else
		{
			sJsonValue = getContent(sJsonValue, JsonStructure.STRING);
			sJsonValue = Json.restore(sJsonValue);
			rValue     = Conversions.parseValue(sJsonValue, rDatatype);
		}

		return (T) rValue;
	}

	/***************************************
	 * Parses the values from a JSON array into a list.
	 *
	 * @param  sJsonArray The JSON array string
	 *
	 * @return A new list containing the parsed array values
	 */
	public List<Object> parseArray(String sJsonArray)
	{
		return parseArray(sJsonArray, new ArrayList<>());
	}

	/***************************************
	 * Parses a JSON array into an existing collection. The collection elements
	 * will be parsed by {@link #parse(String)}. For better control of the
	 * element datatype the method {@link #parseArray(String, Collection,
	 * Class)} can be used.
	 *
	 * @param  sJsonArray        The JSON array string
	 * @param  rTargetCollection The target collection
	 *
	 * @return The input collection containing the parsed array values
	 */
	public <C extends Collection<Object>> C parseArray(
		String sJsonArray,
		C	   rTargetCollection)
	{
		parseStructure(sJsonArray,
					   JsonStructure.ARRAY,
					   sArrayElement ->
					   rTargetCollection.add(parse(sArrayElement)));

		return rTargetCollection;
	}

	/***************************************
	 * Parses the values from a JSON array into a collection with a specific
	 * datatype.
	 *
	 * @param  sJsonArray   The JSON array string
	 * @param  rElementType The data type of the collection elements
	 *
	 * @return The input collection containing the parsed array values
	 */
	public <T> List<T> parseArray(String sJsonArray, Class<T> rElementType)
	{
		return parseArray(sJsonArray, new ArrayList<>(), rElementType);
	}

	/***************************************
	 * Parses a JSON array into an existing collection of a certain datatype.
	 * The collection elements will be parsed by {@link #parse(String, Class)}.
	 *
	 * @param  sJsonArray        The JSON array string
	 * @param  rTargetCollection The target collection
	 * @param  rElementType      The data type of the collection elements
	 *
	 * @return The input collection containing the parsed array values
	 */
	public <T, C extends Collection<T>> C parseArray(String   sJsonArray,
													 C		  rTargetCollection,
													 Class<T> rElementType)
	{
		parseStructure(sJsonArray,
					   JsonStructure.ARRAY,
					   sArrayElement ->
					   rTargetCollection.add(parse(sArrayElement,
												   rElementType)));

		return rTargetCollection;
	}

	/***************************************
	 * Parses a numeric value from a JSON string.
	 *
	 * @param  sJsonNumber The JSON value to parse
	 *
	 * @return The corresponding {@link Number} subclass for the input value
	 */
	@SuppressWarnings("boxing")
	public Number parseNumber(String sJsonNumber)
	{
		Number aNumber;

		if (sJsonNumber.indexOf('.') > 0)
		{
			aNumber = new BigDecimal(sJsonNumber);
		}
		else
		{
			BigInteger aBigInt    = new BigInteger(sJsonNumber);
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
	 * Parses a JSON number string into a Java {@link Number} subclass instance.
	 *
	 * @param  sJsonNumber The JSON number value
	 * @param  rDatatype   The target datatype
	 *
	 * @return The resulting value or NULL if no mapping exists
	 */
	public Number parseNumber(
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
	 * Parses a JSON object structure into a {@link JsonObject}. This is the
	 * same as invoking {@link JsonObject#fromJson(String)} on the input string.
	 *
	 * @param  sJsonObject The JSON object string
	 *
	 * @return A new map containing the parsed object attributes
	 */
	public JsonObject parseObject(String sJsonObject)
	{
		return new JsonObject(parseObjectMap(sJsonObject));
	}

	/***************************************
	 * Parses a JSON object structure into a map. The map will preserve the
	 * order in which the object attributes in the JSON string.
	 *
	 * @param  sJsonObject The JSON object string
	 *
	 * @return A new map containing the parsed object attributes
	 */
	public Map<String, Object> parseObjectMap(String sJsonObject)
	{
		Map<String, Object> aMap = new LinkedHashMap<>();

		parseStructure(sJsonObject,
					   JsonStructure.OBJECT,
					   sMapping -> parseMapping(sMapping, aMap));

		return aMap;
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
	public <R extends Relatable> R parseRelatable(String sJsonObject, R rTarget)
	{
		parseStructure(sJsonObject,
					   JsonStructure.OBJECT,
					   sObjectElement -> parseRelation(sObjectElement, rTarget));

		return rTarget;
	}

	/***************************************
	 * Parses a relation from a JSON string into {@link Relatable} object. The
	 * JSON input string must be in a compatible format as generated by the
	 * method {@link JsonBuilder#appendRelations(Relatable, Collection)} or else
	 * the parsing may cause errors. Furthermore all relation types referenced
	 * in the JSON must have their full namespace and must have been created as
	 * instances or else their lookup will fail.
	 *
	 * @param sJson   The JSON input string
	 * @param rTarget The related object to set the relation in
	 */
	@SuppressWarnings("unchecked")
	public void parseRelation(String sJson, Relatable rTarget)
	{
		int			    nColon		  = sJson.indexOf(':');
		String		    sTypeName     = sJson.substring(1, nColon - 1).trim();
		String		    sJsonValue    = sJson.substring(nColon + 1).trim();
		RelationType<?> rRelationType = null;

		Collection<RelationType<?>> rJsonTypes =
			rTarget.get(Json.JSON_SERIALIZED_TYPES);

		if (rJsonTypes != null)
		{
			sTypeName = TextConvert.uppercaseIdentifier(sTypeName);

			for (RelationType<?> rType : rJsonTypes)
			{
				if (rType.getSimpleName().equalsIgnoreCase(sTypeName))
				{
					rRelationType = rType;

					break;
				}
			}
		}
		else
		{
			rRelationType = RelationType.valueOf(sTypeName);
		}

		if (rRelationType != null)
		{
			Class<?> rValueType = rRelationType.getTargetType();
			Object   rValue;

			if (List.class.isAssignableFrom(rValueType))
			{
				Class<?> rElementType = rRelationType.get(ELEMENT_DATATYPE);

				if (rElementType != null)
				{
					rValue = parseArray(sJsonValue, rElementType);
				}
				else
				{
					rValue = parseArray(sJsonValue);
				}
			}
			else
			{
				rValue = parse(sJsonValue, rValueType);
			}

			rTarget.set((RelationType<Object>) rRelationType, rValue);
		}
		else
		{
			ErrorHandling eErrorHandling = rTarget.get(ERROR_HANDLING);

			if (eErrorHandling == ErrorHandling.THROW)
			{
				throw new IllegalArgumentException("Unknown RelationType: " +
												   sTypeName);
			}
			else if (eErrorHandling == ErrorHandling.LOG)
			{
				System.out.printf("Warning: unknown RelationType %s\n",
								  sTypeName);
			}
		}
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
	private String getContent(String sJsonStructure, JsonStructure eStructure)
	{
		sJsonStructure = sJsonStructure.trim();

		if (sJsonStructure.charAt(0) != eStructure.cOpen ||
			sJsonStructure.charAt(sJsonStructure.length() - 1) !=
			eStructure.cClose)
		{
			throw new IllegalArgumentException("Not a JSON " +
											   eStructure.name().toLowerCase() +
											   ": " + sJsonStructure);
		}

		return sJsonStructure.substring(1, sJsonStructure.length() - 1).trim();
	}

	/***************************************
	 * Parses a JSON date value that must be formatted in the standard JSON date
	 * format defined by {@link Json#JSON_DATE_FORMAT}.
	 *
	 * @param  sJsonDate The JSON date value
	 *
	 * @return A {@link Date} instance
	 *
	 * @throws IllegalArgumentException If the given JSON string cannot be
	 *                                  parsed
	 */
	private Date parseDate(String sJsonDate)
	{
		sJsonDate = getContent(sJsonDate, JsonStructure.STRING);

		try
		{
			return JSON_DATE_FORMAT.parse(sJsonDate);
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException("Invalid JSON date", e);
		}
	}

	/***************************************
	 * Parses a JSON array into a Java array. The target datatype can also be an
	 * array of primitive values.
	 *
	 * @param  sJsonArray The JSON array string
	 * @param  rArrayType The target datatype
	 *
	 * @return A new array of the given target type
	 */
	private Object parseIntoArray(String sJsonArray, Class<?> rArrayType)
	{
		Class<?> rComponentType = rArrayType.getComponentType();

		List<?> rArrayValues = parseArray(sJsonArray, rComponentType);

		int    nCount = rArrayValues.size();
		Object rValue = Array.newInstance(rComponentType, nCount);

		for (int i = 0; i < nCount; i++)
		{
			Array.set(rValue, i, rArrayValues.get(i));
		}

		return rValue;
	}

	/***************************************
	 * Parses a JSON key-value mapping into a map.
	 *
	 * @param sMapping The raw mapping string
	 * @param rMap     The target map
	 */
	private void parseMapping(String sMapping, Map<String, Object> rMap)
	{
		int    nPos		  = sMapping.indexOf(':');
		String sKey		  = sMapping.substring(1, nPos - 1).trim();
		String sJsonValue = sMapping.substring(nPos + 1).trim();

		rMap.put(sKey, parse(sJsonValue));
	}

	/***************************************
	 * Handles the parsing into {@link Relatable} instances.
	 *
	 * @param  sJsonValue The JSON value to parse
	 * @param  rDatatype  The target datatype (must be a subclass of {@link
	 *                    Relatable})
	 *
	 * @return The parsed object
	 */
	private <T> Object parseRelatable(
		String			   sJsonValue,
		Class<? extends T> rDatatype)
	{
		Object rValue;

		if (RelationType.class.isAssignableFrom(rDatatype))
		{
			rValue = RelationType.valueOf(sJsonValue);
		}
		else
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

		return rValue;
	}

	/***************************************
	 * Parses a JSON structure and executes an action for each element.
	 *
	 * @param sJsonData       The JSON string to parse
	 * @param eStructure      The type of the JSON structure
	 * @param fProcessElement The action to execute for each structure element
	 */
	private void parseStructure(String		   sJsonData,
								JsonStructure  eStructure,
								Action<String> fProcessElement)
	{
		String sJson		 = getContent(sJsonData, eStructure);
		int    nMax			 = sJson.length() - 1;
		int    nElementStart = 0;
		int    nElementEnd   = 0;

		nDepth--;

		while (nElementEnd <= nMax)
		{
			JsonStructure eSkippedStructure = null;
			int			  nStructureLevel   = 0;
			boolean		  bInString		    = false;
			char		  cCurrentChar;

			do
			{
				cCurrentChar = sJson.charAt(nElementEnd++);

				// toggle string but consider escaped string delimiters
				if (cCurrentChar == JsonStructure.STRING.cOpen &&
					(!bInString || sJson.charAt(nElementEnd - 2) != '\\'))
				{
					bInString = !bInString;
				}

				if (!bInString)
				{
					if (cCurrentChar == JsonStructure.ARRAY.cOpen)
					{
						if (eSkippedStructure == null)
						{
							eSkippedStructure = JsonStructure.ARRAY;
						}

						if (eSkippedStructure == JsonStructure.ARRAY)
						{
							nStructureLevel++;
						}
					}
					else if (cCurrentChar == JsonStructure.OBJECT.cOpen)
					{
						if (eSkippedStructure == null)
						{
							eSkippedStructure = JsonStructure.OBJECT;
						}

						if (eSkippedStructure == JsonStructure.OBJECT)
						{
							nStructureLevel++;
						}
					}
					else if (eSkippedStructure != null &&
							 cCurrentChar == eSkippedStructure.cClose)
					{
						nStructureLevel--;

						if (nStructureLevel == 0)
						{
							eSkippedStructure = null;
						}
					}
				}
			} // loop until mapping separator (,) or string end is found
			while ((bInString || eSkippedStructure != null ||
					cCurrentChar != ',') &&
				   nElementEnd <= nMax);

			if (eSkippedStructure != null || bInString)
			{
				if (bInString)
				{
					eSkippedStructure = JsonStructure.STRING;
				}

				throw new IllegalArgumentException(String.format("Unclosed JSON " +
																 "%s in %s",
																 eSkippedStructure
																 .name()
																 .toLowerCase(),
																 sJson));
			}

			// exclude separator except for last structure element
			if (nElementEnd <= nMax)
			{
				nElementEnd--;
			}

			String sElement =
				sJson.substring(nElementStart, nElementEnd).trim();

			fProcessElement.execute(sElement);

			nElementStart = ++nElementEnd;
		}

		nDepth++;
	}
}
