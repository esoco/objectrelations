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
import de.esoco.lib.text.TextUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Map;

import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;


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

	/**
	 * The relation types of a {@link Relatable} object that should be
	 * considered for JSON serialization.
	 */
	public static final RelationType<Collection<RelationType<?>>> JSON_SERIALIZED_TYPES =
		RelationTypes.newType();

	/**
	 * A map containing the data of a JSON object structure. Auto-initialized to
	 * an empty ordered map.
	 */
	public static final RelationType<Map<String, Object>> JSON_OBJECT_DATA =
		RelationTypes.newMapType(true);

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
}
