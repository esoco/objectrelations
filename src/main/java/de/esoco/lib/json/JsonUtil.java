//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//		 http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.json;

import de.esoco.lib.expression.Conversions;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;


/********************************************************************
 * Contains static helper functions for the handling of JSON data.
 *
 * @author eso
 */
public class JsonUtil
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * A Java {@link DateFormat} instance for the formatting of JSON date value
	 * in ISO 8601 format.
	 */
	public static final DateFormat JSON_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private JsonUtil()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Appends a collection of values to a string builder as an ordered JSON
	 * list.
	 *
	 * @param rJsonData   The JSON string builder
	 * @param rCollection The collection to append (can be empty but must not be
	 *                    NULL)
	 */
	public static void appendCollection(
		StringBuilder rJsonData,
		Collection<?> rCollection)
	{
		rJsonData.append("[");

		for (Object rElement : rCollection)
		{
			appendValue(rJsonData, rElement);
			rJsonData.append(", ");
		}

		if (!rCollection.isEmpty())
		{
			rJsonData.setLength(rJsonData.length() - 2);
		}

		rJsonData.append(']');
	}

	/***************************************
	 * Appends a mapping of key/value pairs to a string builder as a JSON
	 * object.
	 *
	 * @param rJsonData The JSON string builder
	 * @param rMap      rCollection The map to append (can be empty but must not
	 *                  be NULL)
	 */
	public static void appendMap(StringBuilder rJsonData, Map<?, ?> rMap)
	{
		if (!rMap.isEmpty())
		{
			rJsonData.append("{\n");

			for (Entry<?, ?> rEntry : rMap.entrySet())
			{
				appendName(rJsonData, rEntry.getKey().toString());
				appendValue(rJsonData, rEntry.getValue());
				rJsonData.append(",\n");
			}

			rJsonData.setLength(rJsonData.length() - 2);
			rJsonData.append("\n}");
		}
		else
		{
			rJsonData.append("{}");
		}
	}

	/***************************************
	 * Appends a JSON attribute name to a string builder.
	 *
	 * @param rJsonData The JSON string builder
	 * @param sName     The attribute name
	 */
	public static void appendName(StringBuilder rJsonData, String sName)
	{
		rJsonData.append('\"');
		rJsonData.append(sName);
		rJsonData.append("\": ");
	}

	/***************************************
	 * Appends a relation of a {@link Relatable} object to a JSON string
	 * builder.
	 *
	 * @param  rJsonData         The JSON string builder
	 * @param  rRelation         The relation to append
	 * @param  bWithNamespace    TRUE to include the relation type namespace,
	 *                           FALSE to only use it's simple name
	 * @param  bAppendNullValues TRUE if NULL values should be appended, FALSE
	 *                           if they should be omitted
	 *
	 * @return TRUE if a relation has been appended (can only be FALSE if
	 *         bAppendNullValues is FALSE)
	 */
	public static boolean appendRelation(StringBuilder rJsonData,
										 Relation<?>   rRelation,
										 boolean	   bWithNamespace,
										 boolean	   bAppendNullValues)
	{
		Object  rValue    = rRelation.getTarget();
		boolean bHasValue = (rValue != null || bAppendNullValues);

		if (bHasValue)
		{
			RelationType<?> rRelationType = rRelation.getType();

			String sName =
				bWithNamespace ? rRelationType.getName()
							   : rRelationType.getSimpleName();

			appendName(rJsonData, sName);
			appendValue(rJsonData, rValue);
		}

		return bHasValue;
	}

	/***************************************
	 * Appends a value to a JSON string builder and converts it according to
	 * it's datatype.
	 *
	 * @param rJsonData The JSON string builder
	 * @param rValue    The value to append (can be NULL)
	 */
	public static void appendValue(StringBuilder rJsonData, Object rValue)
	{
		if (rValue == null)
		{
			rJsonData.append("null");
		}
		else
		{
			Class<?> rDatatype = rValue.getClass();

			if (rDatatype == Boolean.class ||
				Number.class.isAssignableFrom(rDatatype))
			{
				rJsonData.append(rValue.toString());
			}

			if (Collection.class.isAssignableFrom(rDatatype))
			{
				appendCollection(rJsonData, (Collection<?>) rValue);
			}
			else if (Map.class.isAssignableFrom(rDatatype))
			{
				Map<?, ?> rMap = (Map<?, ?>) rValue;

				appendMap(rJsonData, rMap);
			}
			else if (Date.class.isAssignableFrom(rDatatype))
			{
				rJsonData.append('\"');
				rJsonData.append(JSON_DATE_FORMAT.format((Date) rValue));
				rJsonData.append('\"');
			}
			else
			{
				String sValue;

				try
				{
					sValue = Conversions.asString(rValue);
				}
				catch (Exception e)
				{
					// if conversion not possible use toString()
					sValue = rValue.toString();
				}

				rJsonData.append('\"');
				JsonUtil.escapeJsonValue(rJsonData, sValue);
				rJsonData.append('\"');
			}
		}
	}

	/***************************************
	 * Creates a JSON compatible value by escaping the control characters in it.
	 *
	 * @param aTarget        The string builder to write the escaped value to
	 * @param sOriginalValue The original value string to escape
	 */
	public static void escapeJsonValue(
		StringBuilder aTarget,
		CharSequence  sOriginalValue)
	{
		final int nLength = sOriginalValue.length();

		for (int nChar = 0; nChar < nLength; nChar++)
		{
			char c = sOriginalValue.charAt(nChar);

			switch (c)
			{
				case '"':
					aTarget.append("\\\"");
					break;

				case '\\':
					aTarget.append("\\\\");
					break;

				case '/':
					aTarget.append("\\/");
					break;

				case '\b':
					aTarget.append("\\b");
					break;

				case '\f':
					aTarget.append("\\f");
					break;

				case '\n':
					aTarget.append("\\n");
					break;

				case '\r':
					aTarget.append("\\r");
					break;

				case '\t':
					aTarget.append("\\t");
					break;

				default:
					if (TextUtil.isControlCharacter(c))
					{
						String sHexValue = Integer.toHexString(c).toUpperCase();

						aTarget.append("\\u");
						aTarget.append(TextConvert.padLeft(sHexValue, 4, '0'));
					}
					else
					{
						aTarget.append(c);
					}
			}
		}
	}

	/***************************************
	 * Extracts the content from a JSON structure (object, array, or string). If
	 * the structure doesn't match the expected format an exception will be
	 * thrown.
	 *
	 * @param  sJsonStructure        The string containing the JSON structure
	 * @param  cOpen                 The opening character of the structure
	 * @param  cClose                The closing character of the structure
	 * @param  sStructureDescription A description of the expected structure for
	 *                               the exception error message
	 *
	 * @return The extracted structure content
	 *
	 * @throws IllegalArgumentException If the content doesn't represent the
	 *                                  expected structure
	 */
	public static String getContent(String sJsonStructure,
									char   cOpen,
									char   cClose,
									String sStructureDescription)
	{
		if (sJsonStructure.charAt(0) != cOpen ||
			sJsonStructure.charAt(sJsonStructure.length() - 1) != cClose)
		{
			throw new IllegalArgumentException("Not a " +
											   sStructureDescription);
		}

		return sJsonStructure.substring(1, sJsonStructure.length() - 1);
	}

	/***************************************
	 * Parses a JSON array string into a new collection instance.
	 *
	 * @param  sJsonArray The JSON array string
	 * @param  rDatatype  The datatype of the target collection
	 *
	 * @return A new instance of the given collection types, containing the
	 *         parsed elements from the JSON array
	 */
	public static <C extends Collection<?>> C parseCollection(
		String   sJsonArray,
		Class<C> rDatatype)
	{
		String sArrayContent = getContent(sJsonArray, '[', ']', "JSON array");

		return null;
	}

	/***************************************
	 * Parses a JSON object string into a new {@link Map} instance.
	 *
	 * @param  sJsonObject The JSON object string
	 *
	 * @return A new ordered map instance, containing the parsed key-value pairs
	 *         from the JSON object string in the same order in which they occur
	 *         in the input string
	 */
	public static Map<?, ?> parseMap(String sJsonObject)
	{
		String sObjectContent =
			getContent(sJsonObject, '{', '}', "JSON object");

		return null;
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
		int    nSeparatorIndex = sJson.indexOf(':');
		String sTypeName	   = sJson.substring(1, nSeparatorIndex - 1).trim();
		String sJsonValue	   = sJson.substring(nSeparatorIndex + 1).trim();

		RelationType<?> rRelationType = RelationType.valueOf(sTypeName);

		Object rValue = parseValue(sJsonValue, rRelationType.getTargetType());

		rTarget.set((RelationType<Object>) rRelationType, rValue);
	}

	/***************************************
	 * Parses a JSON string value into a certain datatype. The value must be in
	 * a format as generated by {@link #appendValue(StringBuilder, Object)}.
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

		if (Collection.class.isAssignableFrom(rDatatype))
		{
			rValue =
				parseCollection(sJsonValue,
								(Class<? extends Collection<?>>) rDatatype);
		}
		else if (Map.class.isAssignableFrom(rDatatype))
		{
			rValue = parseMap(sJsonValue);
		}
		else if (Date.class.isAssignableFrom(rDatatype))
		{
			sJsonValue = getContent(sJsonValue, '"', '"', "JSON string");

			try
			{
				rValue = JSON_DATE_FORMAT.parse(sJsonValue);
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e);
			}
		}
		else
		{
			sJsonValue = getContent(sJsonValue, '"', '"', "JSON string");
			rValue     = Conversions.parseValue(sJsonValue, rDatatype);
		}

		return rValue;
	}
}
