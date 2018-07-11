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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Contains declarations and functions for the handling of JSON data.
 *
 * @author eso
 */
public class Json
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available JSON structures.
	 */
	public enum JsonStructure
	{
		OBJECT('{', '}'), ARRAY('[', ']'), STRING('"', '"');

		//~ Instance fields ----------------------------------------------------

		char cOpen;
		char cClose;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param cOpen  The structure opening character
		 * @param cClose The structure closing character
		 */
		private JsonStructure(char cOpen, char cClose)
		{
			this.cOpen  = cOpen;
			this.cClose = cClose;
		}
	}

	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A Java {@link DateFormat} instance for the formatting of JSON date value
	 * in ISO 8601 format.
	 */
	public static final DateFormat JSON_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	/** The style for the naming of properties when building or parsing JSON. */
	public static final RelationType<IdentifierStyle> JSON_PROPERTY_NAMING =
		newType();

	/**
	 * The relation types of a {@link Relatable} object that should be
	 * considered for JSON serialization.
	 */
	public static final RelationType<Collection<RelationType<?>>> JSON_SERIALIZED_TYPES =
		newType();

	/**
	 * A map containing the properties of a JSON object structure.
	 * Auto-initialized to an empty ordered map.
	 */
	public static final RelationType<Map<String, Object>> JSON_PROPERTIES =
		newMapType(true);

	static
	{
		RelationTypes.init(Json.class);
	}

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Json()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a JSON compatible value by escaping the control characters in it.
	 *
	 * @param  sOriginal The original string to escape
	 *
	 * @return The escaped string
	 */
	public static String escape(String sOriginal)
	{
		StringBuilder aResult = new StringBuilder();
		final int     nLength = sOriginal.length();

		for (int nChar = 0; nChar < nLength; nChar++)
		{
			char c = sOriginal.charAt(nChar);

			switch (c)
			{
				case '"':
					aResult.append("\\\"");
					break;

				case '\\':
					aResult.append("\\\\");
					break;

				case '/':
					aResult.append("\\/");
					break;

				case '\b':
					aResult.append("\\b");
					break;

				case '\f':
					aResult.append("\\f");
					break;

				case '\n':
					aResult.append("\\n");
					break;

				case '\r':
					aResult.append("\\r");
					break;

				case '\t':
					aResult.append("\\t");
					break;

				default:
					if (TextUtil.isControlCharacter(c))
					{
						String sHexValue = Integer.toHexString(c).toUpperCase();

						aResult.append("\\u");
						aResult.append(TextConvert.padLeft(sHexValue, 4, '0'));
					}
					else
					{
						aResult.append(c);
					}
			}
		}

		return aResult.toString();
	}

	/***************************************
	 * Parses a JSON string with a new {@link JsonParser} instance by invoking
	 * {@link JsonParser#parse(String)}.
	 *
	 * @param  sJson The input string
	 *
	 * @return The parsed result
	 *
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static Object parse(String sJson)
	{
		return new JsonParser().parse(sJson);
	}

	/***************************************
	 * Parses a JSON string of a certain datatype with a new {@link JsonParser}
	 * instance by invoking {@link JsonParser#parse(String, Class)}.
	 *
	 * @param  sJson     The input string
	 * @param  rDatatype The target datatype
	 *
	 * @return The parsed result
	 *
	 * @throws RuntimeException If the input string is not valid JSON or doesn't
	 *                          match the given datatype
	 */
	public static <T> T parse(String sJson, Class<T> rDatatype)
	{
		return new JsonParser().parse(sJson, rDatatype);
	}

	/***************************************
	 * Parses a JSON array into a list.
	 *
	 * @param  sJsonArray The JSON array to parse
	 *
	 * @return The resulting list of parsed elements
	 *
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static List<Object> parseArray(String sJsonArray)
	{
		return new JsonParser().parseArray(sJsonArray);
	}

	/***************************************
	 * Parses a JSON array into a list with limited depth. Limiting the depth
	 * will leave deeper levels as raw JSON strings, allowing to parse them into
	 * specific datatypes after inspection. For example, using a depth of 1 will
	 * return a list of raw JSON strings (unless the JSON array is not empty, in
	 * which case the list will be empty too).
	 *
	 * @param  sJsonArray The JSON array to parse
	 * @param  nDepth     The depth up to which parse values into objects
	 *
	 * @return The resulting list of parsed elements
	 *
	 * @throws RuntimeException If the input string is not valid JSON
	 */
	public static List<Object> parseArray(String sJsonArray, int nDepth)
	{
		return new JsonParser(nDepth).parseArray(sJsonArray);
	}

	/***************************************
	 * Parses a JSON array into a list with the given element datatype.
	 *
	 * @param  sJsonArray   The JSON array to parse
	 * @param  rElementType The datatype to parse the array elements with
	 *
	 * @return The resulting list of parsed elements
	 *
	 * @throws RuntimeException If the input string is not valid JSON or cannot
	 *                          be parsed into a list of elements of the given
	 *                          type
	 */
	public static <T> List<T> parseArray(
		String   sJsonArray,
		Class<T> rElementType)
	{
		return new JsonParser().parseArray(sJsonArray, rElementType);
	}

	/***************************************
	 * Parses a JSON string into a {@link JsonObject}with a new instance of
	 * {@link JsonParser} by invoking {@link JsonParser#parseObject(String)}.
	 *
	 * @param  sJson The input string
	 *
	 * @return The parsed object
	 *
	 * @throws RuntimeException If the input string is not a valid JSON object
	 */
	public static JsonObject parseObject(String sJson)
	{
		return new JsonParser().parseObject(sJson);
	}

	/***************************************
	 * Parses a JSON string up to a certain depth into a {@link JsonObject}.
	 * Creates a new instance of {@link JsonParser} with the given depth and
	 * then invokes {@link JsonParser#parseObject(String)}. Limiting the depth
	 * will leave deeper levels as raw JSON strings, allowing to parse them into
	 * specific datatypes after inspection.
	 *
	 * @param  sJson  The input string
	 * @param  nDepth The depth up to which parse the JSON hierarchy
	 *
	 * @return The parsed result
	 *
	 * @throws RuntimeException If the input string is not a valid JSON object
	 */
	public static JsonObject parseObject(String sJson, int nDepth)
	{
		return new JsonParser(nDepth).parseObject(sJson);
	}

	/***************************************
	 * Restores a string from an escaped JSON string.
	 *
	 * @param  sEscaped The escaped string to restore
	 *
	 * @return The restored string
	 */
	public static String restore(String sEscaped)
	{
		StringBuilder aResult = new StringBuilder();
		final int     nMax    = sEscaped.length() - 1;
		int			  i		  = 0;

		while (i <= nMax)
		{
			char c = sEscaped.charAt(i++);

			if (c == '\\' && i <= nMax)
			{
				c = sEscaped.charAt(i++);

				switch (c)
				{
					case '"':
						aResult.append('"');
						break;

					case '\\':
						aResult.append('\\');
						break;

					case '/':
						aResult.append('/');
						break;

					case 'b':
						aResult.append('\b');
						break;

					case 'f':
						aResult.append('\f');
						break;

					case 'n':
						aResult.append('\n');
						break;

					case 'r':
						aResult.append('\r');
						break;

					case 't':
						aResult.append('\t');
						break;

					case 'u':
						try
						{
							String sHex = sEscaped.substring(i, i + 4);
							char   cHex = (char) Integer.parseInt(sHex, 16);

							if (TextUtil.isControlCharacter(cHex))
							{
								aResult.append(cHex);
								i += 4;
							}
							else
							{
								aResult.append("\\u");
							}
						}
						catch (Exception e)
						{
							// append original chars
							aResult.append("\\u");
						}

						break;

					default:
						aResult.append('\\');
						aResult.append(c);
				}
			}
			else
			{
				aResult.append(c);
			}
		}

		return aResult.toString();
	}

	/***************************************
	 * Converts a value into a compact JSON string without linefeeds and
	 * unnecessary whitespace. To get a more readable formatted string use
	 * {@link #toJson(Object)} instead.
	 *
	 * @param  rValue The value to convert
	 *
	 * @return The JSON string
	 *
	 * @see    JsonBuilder#toJson(Object)
	 */
	public static String toCompactJson(Object rValue)
	{
		return new JsonBuilder().noLinefeeds().noWhitespace().toJson(rValue);
	}

	/***************************************
	 * Converts a value into a JSON string with the default settings of the
	 * {@link JsonBuilder} class. The returned string contains linefeeds and
	 * whitespace for better readability. To get a more compact formatted string
	 * use {@link #toCompactJson(Object)} instead.
	 *
	 * @param  rValue The value to convert
	 *
	 * @return The JSON string
	 *
	 * @see    JsonBuilder#toJson(Object)
	 */
	public static String toJson(Object rValue)
	{
		return new JsonBuilder().toJson(rValue);
	}
}
