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
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;

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

import static de.esoco.lib.json.Json.JSON_DATE_FORMAT;
import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.ERROR_HANDLING;

/**
 * A parser for the JSON data that can also be used to parse the relations of
 * {@link Relatable} objects.
 *
 * @author eso
 */
public class JsonParser {

	private int depth;

	/**
	 * Creates a new instance that parses the full hierarchy of a JSON string
	 * (limited to {@link Short#MAX_VALUE}).
	 */
	public JsonParser() {
		this(Short.MAX_VALUE);
	}

	/**
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
	 * interest and should be parsed. By parsing such a record with
	 * {@link #parseObject(String)} or {@link #parseObjectMap(String)} and a
	 * depth of 1 these properties can then be queried from the returned object
	 * as strings and parsed into a typed object.</p>
	 *
	 * @param depth The maximum depth of properties to parse
	 */
	public JsonParser(int depth) {
		this.depth = depth;
	}

	/**
	 * Returns a function that parses a JSON string into objects. The
	 * parsing is
	 * done by {@link #parse(String)}.
	 *
	 * @return A function that parses JSON string into objects
	 */
	public static Function<String, Object> parseJson() {
		return json -> new JsonParser().parse(json);
	}

	/**
	 * Returns a binary function that parses a JSON string into an Object
	 * with a
	 * certain datatype by invoking {@link #parse(String, Class)}.
	 *
	 * @param datatype The preset datatype for unary function invocation
	 * @return The parsing function
	 */
	public static <T> Function<String, T> parseJson(Class<T> datatype) {
		return json -> new JsonParser().parse(json, datatype);
	}

	/**
	 * Returns a function that parses a JSON array into a list with a specific
	 * element type.
	 *
	 * @param elementType The datatype of the list elements
	 * @return The parsing function
	 */
	public static <T> Function<String, List<T>> parseJsonArray(
		Class<T> elementType) {
		return json -> new JsonParser().parseArray(json, elementType);
	}

	/**
	 * Returns a function that parses a JSON object and returns a map
	 * containing
	 * the parsed values. The returned map preserves the order of the parsed
	 * values in the input string.
	 *
	 * @return The parsing function
	 */
	public static Function<String, Map<String, Object>> parseJsonMap() {
		return json -> new JsonParser().parseObjectMap(json);
	}

	/**
	 * Parses a JSON string value according to it's JSON datatype. For more
	 * enhanced datatype parsing see {@link #parse(String, Class)}.
	 *
	 * @param json The JSON value string
	 * @return The parsed value (NULL if input is NULL or empty)
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public Object parse(String json) {
		Object value;

		if (json == null || json.isEmpty()) {
			value = null;
		} else if (depth <= 0) {
			value = json;
		} else if (json.charAt(0) == JsonStructure.STRING.getOpenChar()) {
			value = Json.restore(json.substring(1, json.length() - 1));
		} else if (json.charAt(0) == JsonStructure.OBJECT.getOpenChar()) {
			value = parseObject(json);
		} else if (json.charAt(0) == JsonStructure.ARRAY.getOpenChar()) {
			value = parseArray(json, new ArrayList<>());
		} else if (json.equals("null")) {
			value = null;
		} else if (json.equals("true") || json.equals("false")) {
			value = Boolean.valueOf(json);
		} else {
			value = parseNumber(json);
		}

		return value;
	}

	/**
	 * Parses a JSON string value into a certain datatype.
	 *
	 * @param jsonValue The JSON value string
	 * @param datatype  The target datatype
	 * @return The parsed value
	 * @throws RuntimeException If the input string is not valid JSON or
	 * doesn't
	 *                          match the given datatype
	 */
	@SuppressWarnings("unchecked")
	public <T> T parse(String jsonValue, Class<? extends T> datatype) {
		Object value;

		if ("null".equals(jsonValue)) {
			value = null;
		} else if (JsonSerializable.class.isAssignableFrom(datatype)) {
			value = ReflectUtil.newInstance(datatype);

			((JsonSerializable<?>) value).fromJson(jsonValue);
		} else if (datatype == Boolean.class || datatype == boolean.class) {
			value = Boolean.valueOf(jsonValue);
		} else if (datatype.isPrimitive()) {
			// all non-boolean primitives must be numbers as character values
			// are not supported in JSON
			value = parseNumber(jsonValue,
				(Class<? extends Number>) ReflectUtil.getWrapperType(datatype));
		} else if (Number.class.isAssignableFrom(datatype)) {
			value = parseNumber(jsonValue, (Class<? extends Number>) datatype);
		} else if (datatype.isArray()) {
			value = parseIntoArray(jsonValue, datatype);
		} else if (Collection.class.isAssignableFrom(datatype)) {
			Collection<Object> collection =
				Set.class.isAssignableFrom(datatype) ?
				new HashSet<>() :
				new ArrayList<>();

			value = parseArray(jsonValue, collection);
		} else if (Map.class.isAssignableFrom(datatype)) {
			value = parseObject(jsonValue);
		} else if (Date.class.isAssignableFrom(datatype)) {
			value = parseDate(jsonValue);
		} else if (Relatable.class.isAssignableFrom(datatype)) {
			value = parseRelatable(jsonValue, datatype);
		} else {
			jsonValue = getContent(jsonValue, JsonStructure.STRING);
			jsonValue = Json.restore(jsonValue);
			value = Conversions.parseValue(jsonValue, datatype);
		}

		return (T) value;
	}

	/**
	 * Parses the values from a JSON array into a list.
	 *
	 * @param jsonArray The JSON array string
	 * @return A new list containing the parsed array values
	 */
	public List<Object> parseArray(String jsonArray) {
		return parseArray(jsonArray, new ArrayList<>());
	}

	/**
	 * Parses a JSON array into an existing collection. The collection elements
	 * will be parsed by {@link #parse(String)}. For better control of the
	 * element datatype the method
	 * {@link #parseArray(String, Collection, Class)} can be used.
	 *
	 * @param jsonArray        The JSON array string
	 * @param targetCollection The target collection
	 * @return The input collection containing the parsed array values
	 */
	public <C extends Collection<Object>> C parseArray(String jsonArray,
		C targetCollection) {
		parseStructure(jsonArray, JsonStructure.ARRAY,
			arrayElement -> targetCollection.add(parse(arrayElement)));

		return targetCollection;
	}

	/**
	 * Parses the values from a JSON array into a collection with a specific
	 * datatype.
	 *
	 * @param jsonArray   The JSON array string
	 * @param elementType The data type of the collection elements
	 * @return The input collection containing the parsed array values
	 */
	public <T> List<T> parseArray(String jsonArray, Class<T> elementType) {
		return parseArray(jsonArray, new ArrayList<>(), elementType);
	}

	/**
	 * Parses a JSON array into an existing collection of a certain datatype.
	 * The collection elements will be parsed by {@link #parse(String, Class)}.
	 *
	 * @param jsonArray        The JSON array string
	 * @param targetCollection The target collection
	 * @param elementType      The data type of the collection elements
	 * @return The input collection containing the parsed array values
	 */
	public <T, C extends Collection<T>> C parseArray(String jsonArray,
		C targetCollection, Class<T> elementType) {
		parseStructure(jsonArray, JsonStructure.ARRAY,
			arrayElement -> targetCollection.add(
				parse(arrayElement, elementType)));

		return targetCollection;
	}

	/**
	 * Parses a numeric value from a JSON string.
	 *
	 * @param jsonNumber The JSON value to parse
	 * @return The corresponding {@link Number} subclass for the input value
	 */
	@SuppressWarnings("boxing")
	public Number parseNumber(String jsonNumber) {
		Number number;

		if (jsonNumber.indexOf('.') > 0) {
			number = new BigDecimal(jsonNumber);
		} else {
			BigInteger bigInt = new BigInteger(jsonNumber);
			int bitLength = bigInt.bitLength();

			if (bitLength <= 32) {
				number = bigInt.intValue();
			} else if (bitLength <= 64) {
				number = bigInt.longValue();
			} else {
				number = bigInt;
			}
		}

		return number;
	}

	/**
	 * Parses a JSON number string into a Java {@link Number} subclass
	 * instance.
	 *
	 * @param jsonNumber The JSON number value
	 * @param datatype   The target datatype
	 * @return The resulting value or NULL if no mapping exists
	 */
	public Number parseNumber(String jsonNumber,
		Class<? extends Number> datatype) {
		Number value = null;

		if (datatype == Integer.class) {
			value = Integer.valueOf(jsonNumber);
		} else if (datatype == Long.class) {
			value = Long.valueOf(jsonNumber);
		} else if (datatype == Short.class) {
			value = Short.valueOf(jsonNumber);
		} else if (datatype == Byte.class) {
			value = Byte.valueOf(jsonNumber);
		} else if (datatype == BigInteger.class) {
			value = new BigInteger(jsonNumber);
		} else if (datatype == BigDecimal.class) {
			value = new BigDecimal(jsonNumber);
		} else if (datatype == Float.class) {
			value = Float.valueOf(jsonNumber);
		} else if (datatype == Double.class) {
			value = Double.valueOf(jsonNumber);
		}

		return value;
	}

	/**
	 * Parses a JSON object structure into a {@link JsonObject}. This is the
	 * same as invoking {@link JsonObject#fromJson(String)} on the input
	 * string.
	 *
	 * @param jsonObject The JSON object string
	 * @return A new map containing the parsed object attributes
	 */
	public JsonObject parseObject(String jsonObject) {
		return new JsonObject(parseObjectMap(jsonObject));
	}

	/**
	 * Parses a JSON object structure into a map. The map will preserve the
	 * order in which the object attributes in the JSON string.
	 *
	 * @param jsonObject The JSON object string
	 * @return A new map containing the parsed object attributes
	 */
	public Map<String, Object> parseObjectMap(String jsonObject) {
		Map<String, Object> map = new LinkedHashMap<>();

		parseStructure(jsonObject, JsonStructure.OBJECT,
			mapping -> parseMapping(mapping, map));

		return map;
	}

	/**
	 * Parses a JSON object from a string into the relations of a relatable
	 * target object.
	 *
	 * @param jsonObject json The string containing a JSON object
	 * @param target     The relatable target object to set the parsed
	 *                      relations
	 *                   on
	 * @return The input relatable, containing the parsed relations
	 * @see #parseRelation(String, Relatable)
	 */
	public <R extends Relatable> R parseRelatable(String jsonObject,
		R target) {
		parseStructure(jsonObject, JsonStructure.OBJECT,
			objectElement -> parseRelation(objectElement, target));

		return target;
	}

	/**
	 * Parses a relation from a JSON string into {@link Relatable} object. The
	 * JSON input string must be in a compatible format as generated by the
	 * method {@link JsonBuilder#appendRelations(Relatable, Collection)} or
	 * else
	 * the parsing may cause errors. Furthermore all relation types referenced
	 * in the JSON must have their full namespace and must have been created as
	 * instances or else their lookup will fail.
	 *
	 * @param json   The JSON input string
	 * @param target The related object to set the relation in
	 */
	@SuppressWarnings("unchecked")
	public void parseRelation(String json, Relatable target) {
		int colon = json.indexOf(':');
		String typeName = json.substring(1, colon - 1).trim();
		String jsonValue = json.substring(colon + 1).trim();
		RelationType<?> relationType = null;

		Collection<RelationType<?>> jsonTypes =
			target.get(Json.JSON_SERIALIZED_TYPES);

		if (jsonTypes != null) {
			typeName = TextConvert.uppercaseIdentifier(typeName);

			for (RelationType<?> type : jsonTypes) {
				if (type.getSimpleName().equalsIgnoreCase(typeName)) {
					relationType = type;

					break;
				}
			}
		} else {
			relationType = RelationType.valueOf(typeName);
		}

		if (relationType != null) {
			Class<?> valueType = relationType.getTargetType();
			Object value;

			if (List.class.isAssignableFrom(valueType)) {
				Class<?> elementType = relationType.get(ELEMENT_DATATYPE);

				if (elementType != null) {
					value = parseArray(jsonValue, elementType);
				} else {
					value = parseArray(jsonValue);
				}
			} else {
				value = parse(jsonValue, valueType);
			}

			target.set((RelationType<Object>) relationType, value);
		} else {
			ErrorHandling errorHandling = target.get(ERROR_HANDLING);

			if (errorHandling == ErrorHandling.THROW) {
				throw new IllegalArgumentException(
					"Unknown RelationType: " + typeName);
			} else if (errorHandling == ErrorHandling.LOG) {
				System.out.printf("Warning: unknown RelationType %s\n",
					typeName);
			}
		}
	}

	/**
	 * Extracts the content from a JSON structure (object, array, or string) .
	 * If the structure doesn't match the expected format an exception will be
	 * thrown.
	 *
	 * @param jsonStructure The string containing the JSON structure
	 * @param structure     structureDelimiters A string that is exactly 2
	 *                      characters long and contains the delimiters of the
	 *                      JSON structure (brackets, braces, quotation marks)
	 * @return The extracted structure content
	 * @throws IllegalArgumentException If the content doesn't represent the
	 *                                  expected structure
	 */
	private String getContent(String jsonStructure, JsonStructure structure) {
		jsonStructure = jsonStructure.trim();

		if (jsonStructure.charAt(0) != structure.getOpenChar() ||
			jsonStructure.charAt(jsonStructure.length() - 1) !=
				structure.getCloseChar()) {
			throw new IllegalArgumentException(
				"Not a JSON " + structure.name().toLowerCase() + ": " +
					jsonStructure);
		}

		return jsonStructure.substring(1, jsonStructure.length() - 1).trim();
	}

	/**
	 * Parses a JSON date value that must be formatted in the standard JSON
	 * date
	 * format defined by {@link Json#JSON_DATE_FORMAT}.
	 *
	 * @param jsonDate The JSON date value
	 * @return A {@link Date} instance
	 * @throws IllegalArgumentException If the given JSON string cannot be
	 *                                  parsed
	 */
	private Date parseDate(String jsonDate) {
		jsonDate = getContent(jsonDate, JsonStructure.STRING);

		try {
			return JSON_DATE_FORMAT.parse(jsonDate);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid JSON date", e);
		}
	}

	/**
	 * Parses a JSON array into a Java array. The target datatype can also
	 * be an
	 * array of primitive values.
	 *
	 * @param jsonArray The JSON array string
	 * @param arrayType The target datatype
	 * @return A new array of the given target type
	 */
	private Object parseIntoArray(String jsonArray, Class<?> arrayType) {
		Class<?> componentType = arrayType.getComponentType();

		List<?> arrayValues = parseArray(jsonArray, componentType);

		int count = arrayValues.size();
		Object value = Array.newInstance(componentType, count);

		for (int i = 0; i < count; i++) {
			Array.set(value, i, arrayValues.get(i));
		}

		return value;
	}

	/**
	 * Parses a JSON key-value mapping into a map.
	 *
	 * @param mapping The raw mapping string
	 * @param map     The target map
	 */
	private void parseMapping(String mapping, Map<String, Object> map) {
		int pos = mapping.indexOf(':');
		String key = mapping.substring(1, pos - 1).trim();
		String jsonValue = mapping.substring(pos + 1).trim();

		map.put(key, parse(jsonValue));
	}

	/**
	 * Handles the parsing into {@link Relatable} instances.
	 *
	 * @param jsonValue The JSON value to parse
	 * @param datatype  The target datatype (must be a subclass of
	 *                  {@link Relatable})
	 * @return The parsed object
	 */
	private <T> Object parseRelatable(String jsonValue,
		Class<? extends T> datatype) {
		Object value;

		if (RelationType.class.isAssignableFrom(datatype)) {
			value = RelationType.valueOf(jsonValue);
		} else {
			Relatable relatable;

			if (datatype == Relatable.class) {
				relatable = new RelatedObject();
			} else {
				relatable = (Relatable) ReflectUtil.newInstance(datatype);
			}

			value = parseRelatable(jsonValue, relatable);
		}

		return value;
	}

	/**
	 * Parses a JSON structure and executes an action for each element.
	 *
	 * @param jsonData       The JSON string to parse
	 * @param structure      The type of the JSON structure
	 * @param processElement The action to execute for each structure element
	 */
	private void parseStructure(String jsonData, JsonStructure structure,
		Action<String> processElement) {
		String json = getContent(jsonData, structure);
		int max = json.length() - 1;
		int elementStart = 0;
		int elementEnd = 0;

		depth--;

		while (elementEnd <= max) {
			JsonStructure skippedStructure = null;
			int structureLevel = 0;
			boolean inString = false;
			char currentChar;

			do {
				currentChar = json.charAt(elementEnd++);

				// toggle string but consider escaped string delimiters
				if (currentChar == JsonStructure.STRING.getOpenChar() &&
					(!inString || json.charAt(elementEnd - 2) != '\\')) {
					inString = !inString;
				}

				if (!inString) {
					if (currentChar == JsonStructure.ARRAY.getOpenChar()) {
						if (skippedStructure == null) {
							skippedStructure = JsonStructure.ARRAY;
						}

						if (skippedStructure == JsonStructure.ARRAY) {
							structureLevel++;
						}
					} else if (currentChar ==
						JsonStructure.OBJECT.getOpenChar()) {
						if (skippedStructure == null) {
							skippedStructure = JsonStructure.OBJECT;
						}

						if (skippedStructure == JsonStructure.OBJECT) {
							structureLevel++;
						}
					} else if (skippedStructure != null &&
						currentChar == skippedStructure.getCloseChar()) {
						structureLevel--;

						if (structureLevel == 0) {
							skippedStructure = null;
						}
					}
				}
			} // loop until mapping separator (,) or string end is found
			while (
				(inString || skippedStructure != null || currentChar != ',') &&
					elementEnd <= max);

			if (skippedStructure != null || inString) {
				if (inString) {
					skippedStructure = JsonStructure.STRING;
				}

				throw new IllegalArgumentException(
					String.format("Unclosed JSON " + "%s in %s",
						skippedStructure.name().toLowerCase(), json));
			}

			// exclude separator except for last structure element
			if (elementEnd <= max) {
				elementEnd--;
			}

			String element = json.substring(elementStart, elementEnd).trim();

			processElement.execute(element);

			elementStart = ++elementEnd;
		}
		depth++;
	}
}
