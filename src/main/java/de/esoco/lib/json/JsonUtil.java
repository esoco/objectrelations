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

import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import java.text.DateFormat;
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
	 * Appends a string value to a JSON string builder. All reserved JSON
	 * control characters in the value will be escaped and it will be enclosed
	 * in double quotes.
	 *
	 * @param rJsonData The JSON string builder
	 * @param sValue    sName The string value
	 */
	public static void appendStringValue(StringBuilder rJsonData, String sValue)
	{
		rJsonData.append('\"');
		JsonUtil.escapeJsonValue(rJsonData, sValue);
		rJsonData.append('\"');
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
			else if (Collection.class.isAssignableFrom(rDatatype))
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
				appendStringValue(rJsonData,
								  JSON_DATE_FORMAT.format((Date) rValue));
			}
			else
			{
				appendStringValue(rJsonData, rValue.toString());
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
}