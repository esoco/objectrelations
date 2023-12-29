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

import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextConvert.IdentifierStyle;
import de.esoco.lib.text.TextUtil;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newType;

/**
 * Contains declarations and functions for the handling of JSON data.
 *
 * @author eso
 */
public class Json {

	/**
	 * Enumeration of the available JSON structures.
	 */
	public enum JsonStructure {
		OBJECT('{', '}'), ARRAY('[', ']'), STRING('"', '"');

		private final char openChar;

		private final char closeChar;

		/**
		 * Creates a new instance.
		 *
		 * @param open  The structure opening character
		 * @param close The structure closing character
		 */
		JsonStructure(char open, char close) {
			this.openChar = open;
			this.closeChar = close;
		}

		public char getCloseChar() {
			return closeChar;
		}

		public char getOpenChar() {
			return openChar;
		}
	}

	/**
	 * A Java {@link DateFormat} instance for the formatting of JSON date value
	 * in ISO 8601 format.
	 */
	public static final DateFormat JSON_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	/**
	 * The style for the naming of properties when building or parsing JSON.
	 */
	public static final RelationType<IdentifierStyle> JSON_PROPERTY_NAMING =
		newType();

	/**
	 * The relation types of a {@link Relatable} object that should be
	 * considered for JSON serialization.
	 */
	public static final RelationType<Collection<RelationType<?>>>
		JSON_SERIALIZED_TYPES = newType();

	/**
	 * A map containing the properties of a JSON object structure.
	 * Auto-initialized to an empty ordered map.
	 */
	public static final RelationType<Map<String, Object>> JSON_PROPERTIES =
		newMapType(true);

	static {
		RelationTypes.init(Json.class);
	}

	/**
	 * Private, only static use.
	 */
	private Json() {
	}

	/**
	 * Creates a JSON compatible value by escaping the control characters in
	 * it.
	 *
	 * @param original The original string to escape
	 * @return The escaped string
	 */
	public static String escape(String original) {
		StringBuilder result = new StringBuilder();
		final int length = original.length();

		for (int pos = 0; pos < length; pos++) {
			char c = original.charAt(pos);

			switch (c) {
				case '"':
					result.append("\\\"");
					break;

				case '\\':
					result.append("\\\\");
					break;

				case '/':
					result.append("\\/");
					break;

				case '\b':
					result.append("\\b");
					break;

				case '\f':
					result.append("\\f");
					break;

				case '\n':
					result.append("\\n");
					break;

				case '\r':
					result.append("\\r");
					break;

				case '\t':
					result.append("\\t");
					break;

				default:
					if (TextUtil.isControlCharacter(c)) {
						String hexValue = Integer.toHexString(c).toUpperCase();

						result.append("\\u");
						result.append(TextConvert.padLeft(hexValue, 4, '0'));
					} else {
						result.append(c);
					}
			}
		}

		return result.toString();
	}

	/**
	 * Parses a JSON string with a new {@link JsonParser} instance by invoking
	 * {@link JsonParser#parse(String)}.
	 *
	 * @param json The input string
	 * @return The parsed result
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static Object parse(String json) {
		return new JsonParser().parse(json);
	}

	/**
	 * Parses a JSON string of a certain datatype with a new {@link JsonParser}
	 * instance by invoking {@link JsonParser#parse(String, Class)}.
	 *
	 * @param json     The input string
	 * @param datatype The target datatype
	 * @return The parsed result
	 * @throws RuntimeException If the input string is not valid JSON or
	 * doesn't
	 *                          match the given datatype
	 */
	public static <T> T parse(String json, Class<T> datatype) {
		return new JsonParser().parse(json, datatype);
	}

	/**
	 * Parses a JSON array into a list.
	 *
	 * @param jsonArray The JSON array to parse
	 * @return The resulting list of parsed elements
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static List<Object> parseArray(String jsonArray) {
		return new JsonParser().parseArray(jsonArray);
	}

	/**
	 * Parses a JSON array into a list with limited depth. Limiting the depth
	 * will leave deeper levels as raw JSON strings, allowing to parse them
	 * into
	 * specific datatypes after inspection. For example, using a depth of 1
	 * will
	 * return a list of raw JSON strings (unless the JSON array is not
	 * empty, in
	 * which case the list will be empty too).
	 *
	 * @param jsonArray The JSON array to parse
	 * @param depth     The depth up to which parse values into objects
	 * @return The resulting list of parsed elements
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static List<Object> parseArray(String jsonArray, int depth) {
		return new JsonParser(depth).parseArray(jsonArray);
	}

	/**
	 * Parses a JSON array into a list with the given element datatype.
	 *
	 * @param jsonArray   The JSON array to parse
	 * @param elementType The datatype to parse the array elements with
	 * @return The resulting list of parsed elements
	 * @throws RuntimeException If the input string is not valid JSON or cannot
	 *                          be parsed into a list of elements of the given
	 *                          type
	 */
	public static <T> List<T> parseArray(String jsonArray,
		Class<T> elementType) {
		return new JsonParser().parseArray(jsonArray, elementType);
	}

	/**
	 * Parses a JSON string into a {@link JsonObject}with a new instance of
	 * {@link JsonParser} by invoking {@link JsonParser#parseObject(String)}.
	 *
	 * @param json The input string
	 * @return The parsed object
	 * @throws RuntimeException If the input string is not a valid JSON object
	 */
	public static JsonObject parseObject(String json) {
		return new JsonParser().parseObject(json);
	}

	/**
	 * Parses a JSON string up to a certain depth into a {@link JsonObject}.
	 * Creates a new instance of {@link JsonParser} with the given depth and
	 * then invokes {@link JsonParser#parseObject(String)}. Limiting the depth
	 * will leave deeper levels as raw JSON strings, allowing to parse them
	 * into
	 * specific datatypes after inspection.
	 *
	 * @param json  The input string
	 * @param depth The depth up to which parse the JSON hierarchy
	 * @return The parsed result
	 * @throws RuntimeException If the input string is not a valid JSON object
	 */
	public static JsonObject parseObject(String json, int depth) {
		return new JsonParser(depth).parseObject(json);
	}

	/**
	 * Restores a string from an escaped JSON string.
	 *
	 * @param escaped The escaped string to restore
	 * @return The restored string
	 */
	public static String restore(String escaped) {
		StringBuilder result = new StringBuilder();
		final int max = escaped.length() - 1;
		int i = 0;

		while (i <= max) {
			char c = escaped.charAt(i++);

			if (c == '\\' && i <= max) {
				c = escaped.charAt(i++);

				switch (c) {
					case '"':
						result.append('"');
						break;

					case '\\':
						result.append('\\');
						break;

					case '/':
						result.append('/');
						break;

					case 'b':
						result.append('\b');
						break;

					case 'f':
						result.append('\f');
						break;

					case 'n':
						result.append('\n');
						break;

					case 'r':
						result.append('\r');
						break;

					case 't':
						result.append('\t');
						break;

					case 'u':
						try {
							String hex = escaped.substring(i, i + 4);
							char hexChar = (char) Integer.parseInt(hex, 16);

							if (TextUtil.isControlCharacter(hexChar)) {
								result.append(hexChar);
								i += 4;
							} else {
								result.append("\\u");
							}
						} catch (Exception e) {
							// append original chars
							result.append("\\u");
						}

						break;

					default:
						result.append('\\');
						result.append(c);
				}
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	/**
	 * Converts a value into a compact JSON string without linefeeds and
	 * unnecessary whitespace. To get a more readable formatted string use
	 * {@link #toJson(Object)} instead.
	 *
	 * @param value The value to convert
	 * @return The JSON string
	 * @see JsonBuilder#toJson(Object)
	 */
	public static String toCompactJson(Object value) {
		return new JsonBuilder().noLinefeeds().noWhitespace().toJson(value);
	}

	/**
	 * Converts a value into a JSON string with the default settings of the
	 * {@link JsonBuilder} class. The returned string contains linefeeds and
	 * whitespace for better readability. To get a more compact formatted
	 * string
	 * use {@link #toCompactJson(Object)} instead.
	 *
	 * @param value The value to convert
	 * @return The JSON string
	 * @see JsonBuilder#toJson(Object)
	 */
	public static String toJson(Object value) {
		return new JsonBuilder().toJson(value);
	}
}
