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
package de.esoco.lib.text;

import de.esoco.lib.datatype.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/********************************************************************
 * Utility class containing static text processing methods.
 *
 * @author  eso
 * @version 1.0
 */
public final class TextUtil extends TextConvert
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Pattern NON_ASCII_CHARS =
		Pattern.compile("[\\x80-\\xFFFF]");

	private static final Map<String, String> ASCII_REPLACE_MAP =
		new HashMap<String, String>();

	static
	{
		ASCII_REPLACE_MAP.put("[\u00C4\u00C6]", "Ae");
		ASCII_REPLACE_MAP.put("\u00C5", "Aa");
		ASCII_REPLACE_MAP.put("[\u00D6\u00D8]", "Oe");
		ASCII_REPLACE_MAP.put("\u00DC", "Ue");
		ASCII_REPLACE_MAP.put("[\u00E4\u00E6]", "ae");
		ASCII_REPLACE_MAP.put("\u00E5", "aa");
		ASCII_REPLACE_MAP.put("[\u00F6\u00F8]", "oe");
		ASCII_REPLACE_MAP.put("\u00FC", "ue");
		ASCII_REPLACE_MAP.put("\u00DF", "ss");
		ASCII_REPLACE_MAP.put("\u00C7", "C");
		ASCII_REPLACE_MAP.put("[\u00C0\u00C1\u00C2\u00C3]", "A");
		ASCII_REPLACE_MAP.put("[\u00C8\u00C9\u00CA\u00CB]", "E");
		ASCII_REPLACE_MAP.put("[\u00CC\u00CD\u00CE\u00CF]", "I");
		ASCII_REPLACE_MAP.put("[\u00D2\u00D3\u00D4\u00D5]", "O");
		ASCII_REPLACE_MAP.put("[\u00D9\u00DA\u00DB]", "U");
		ASCII_REPLACE_MAP.put("[\u00E0\u00E1\u00E2\u00E3]", "a");
		ASCII_REPLACE_MAP.put("\u00E7", "c");
		ASCII_REPLACE_MAP.put("[\u00E8\u00E9\u00EA\u00EB]", "e");
		ASCII_REPLACE_MAP.put("[\u00EC\u00ED\u00EE\u00EF]", "i");
		ASCII_REPLACE_MAP.put("[\u00F2\u00F3\u00F4\u00F5]", "o");
		ASCII_REPLACE_MAP.put("[\u00F9\u00FA\u00FB]", "u");
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Checks whether a certain string contains at least one lower case
	 * character as defined by {@link Character#isLowerCase(char)}.
	 *
	 * @param  sText The text to check
	 *
	 * @return TRUE if the text contains lower case characters
	 */
	public static boolean containsLowerCase(CharSequence sText)
	{
		int l = sText.length();

		for (int i = 0; i < l; i++)
		{
			if (Character.isLowerCase(sText.charAt(i)))
			{
				return true;
			}
		}

		return false;
	}

	/***************************************
	 * Checks whether a certain string contains at least one upper case
	 * character as defined by {@link Character#isUpperCase(char)}.
	 *
	 * @param  sText The text to check
	 *
	 * @return TRUE if the text contains upper case characters
	 */
	public static boolean containsUpperCase(CharSequence sText)
	{
		int l = sText.length();

		for (int i = 0; i < l; i++)
		{
			if (Character.isUpperCase(sText.charAt(i)))
			{
				return true;
			}
		}

		return false;
	}

	/***************************************
	 * Counts the occurrences of a certain regular expression in a string.
	 *
	 * @param  sText  The text to search in
	 * @param  sRegEx The regular expression pattern to count
	 *
	 * @return The number of occurrences
	 */
	public static int count(String sText, String sRegEx)
	{
		return count(sText, Pattern.compile(sRegEx));
	}

	/***************************************
	 * Counts the occurrences of a certain regular expression in a string.
	 *
	 * @param  sText  The text to search in
	 * @param  rRegEx The compiled regular expression pattern to count
	 *
	 * @return The number of occurrences
	 */
	public static int count(String sText, Pattern rRegEx)
	{
		Matcher aMatcher = rRegEx.matcher(sText);
		int     nCount   = 0;

		while (aMatcher.find())
		{
			nCount++;
		}

		return nCount;
	}

	/***************************************
	 * Extracts the first mnemonic character that is identified by an '&amp;'
	 * (ampersand) prefix from a text string and returns a pair containing the
	 * mnemonic character and the resulting string. All other mnemonic prefixes
	 * will be stripped from the string. Ampersand characters at the end of the
	 * string or which are followed by a whitespace character will be ignored
	 * and not removed. A double ampersand will be replaced by a single one.
	 *
	 * @param  sText The string to extract the mnemonic character from
	 *
	 * @return A pair containing the mnemonic character as the first element
	 *         (NULL for none) and the resulting string stripped from all
	 *         mnemonics as the second element
	 */
	@SuppressWarnings("boxing")
	public static Pair<Character, String> extractMnemonic(String sText)
	{
		StringBuilder sb	    = new StringBuilder(sText);
		int			  pos	    = 0;
		Character     aMnemonic = null;

		while (true)
		{
			pos = sb.indexOf("&", pos);

			if (pos != -1 && pos < (sb.length() - 1))
			{
				char c = sb.charAt(pos + 1);

				if (!Character.isWhitespace(c))
				{
					sb.deleteCharAt(pos);

					if (aMnemonic == null && c != '&')
					{
						aMnemonic = c;
					}
				}
			}
			else
			{
				break;
			}
		}

		return new Pair<Character, String>(aMnemonic, sb.toString());
	}

	/***************************************
	 * Checks whether the given character is a control character in the full
	 * Unicode character code range.
	 *
	 * @param  c The character value to check
	 *
	 * @return TRUE if it is a control character
	 */
	public static boolean isControlCharacter(char c)
	{
		return (c >= '\u0000' && c <= '\u001F') ||
			   (c >= '\u007F' && c <= '\u009F') ||
			   (c >= '\u2000' && c <= '\u20FF');
	}

	/***************************************
	 * Checks whether a certain string contains only lower case characters as
	 * defined by {@link Character#isLowerCase(char)}.
	 *
	 * @param  sText The text to check
	 *
	 * @return TRUE if the text contains only lower case characters
	 */
	public static boolean isLowerCase(CharSequence sText)
	{
		int l = sText.length();

		for (int i = 0; i < l; i++)
		{
			if (!Character.isLowerCase(sText.charAt(i)))
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * Checks whether a certain string contains only upper case character as
	 * defined by {@link Character#isUpperCase(char)}.
	 *
	 * @param  sText The text to check
	 *
	 * @return TRUE if the text contains only upper case characters
	 */
	public static boolean isUpperCase(CharSequence sText)
	{
		int l = sText.length();

		for (int i = 0; i < l; i++)
		{
			if (!Character.isUpperCase(sText.charAt(i)))
			{
				return false;
			}
		}

		return true;
	}

	/***************************************
	 * Returns the index of the next valid (non-NULL) group from the argument
	 * matcher or -1 if no such group exists. The matcher must have been matched
	 * against the input sequence before invoking this method. <b>Attention:</b>
	 * remember that group indices start with 1, 0 refers to the complete match
	 * expression.
	 *
	 * @param  rMatcher The matcher to return the group from
	 * @param  nStart   The index of the group to start with
	 *
	 * @return The index of the next valid group or -1 if no more exists
	 */
	public static int nextGroup(Matcher rMatcher, int nStart)
	{
		int nGroups = rMatcher.groupCount();

		while (nStart <= nGroups && rMatcher.group(nStart) == null)
		{
			nStart++;
		}

		return nStart <= nGroups ? nStart : -1;
	}

	/***************************************
	 * Parses a Java object from a string. Leading and trailing whitespace will
	 * be ignored. The type of the returned object depends on the string's
	 * contents - if no parseable data can be found the original string will be
	 * returned. The following list shows the datatypes that are currently
	 * supported with the corresponding regular expression pattern shown in
	 * parentheses:
	 *
	 * <ul>
	 *   <li>String: the default; quotation marks (' or &quot;) will be stripped
	 *     from the beginning and the end</li>
	 *   <li>Integer: values containing only digits with an optional leading
	 *     minus sign (-?[\d]+)</li>
	 *   <li>boolean: value contains string 'true' or 'false' (case
	 *     ignored)</li>
	 *   <li>null: value contains string 'null'</li>
	 * </ul>
	 *
	 * @param  sValue The string value that the object shall be parsed from
	 *
	 * @return The parsed object
	 */
	public static Object parseObject(String sValue)
	{
		sValue = sValue.trim();

		int nLength = sValue.length();

		if (Pattern.matches("\".*\"|'.*'", sValue))
		{
			// string enclosed in quotes
			return sValue.substring(1, nLength - 1);
		}
		else if (Pattern.matches("-?[\\d]+", sValue))
		{
			// positive or negative (-?) integer
			return Integer.valueOf(sValue);
		}
		else if (Pattern.matches("(?i:true|false)", sValue))
		{
			return Boolean.valueOf(sValue);
		}
		else if (sValue.equals("null"))
		{
			return null;
		}
		else
		{
			return sValue;
		}
	}

	/***************************************
	 * Converts a simple search pattern (e.g. Window file name patterns like "*
	 * .txt") into their regular expression equivalent (".*\.txt").
	 *
	 * <ul>
	 *   <li>. into \.</li>
	 *   <li>* into .*</li>
	 *   <li>? into .?</li>
	 * </ul>
	 *
	 * @param  sPattern The original simple pattern to convert
	 *
	 * @return The converted pattern.
	 */
	public static String simplePatternToRegEx(String sPattern)
	{
		sPattern = sPattern.replace(".", "\\.");
		sPattern = sPattern.replace("*", ".*");
		sPattern = sPattern.replace("?", ".?");

		return sPattern;
	}

	/***************************************
	 * Converts a string that may contain non-ASCII characters to the
	 * corresponding ASCII representation.
	 *
	 * @param  sString The string to convert
	 *
	 * @return The non-ASCII representation of the input string
	 */
	public static String toAscii(String sString)
	{
		if (sString != null)
		{
			if (NON_ASCII_CHARS.matcher(sString.toLowerCase()).find())
			{
				for (Entry<String, String> rReplace :
					 ASCII_REPLACE_MAP.entrySet())
				{
					sString =
						sString.replaceAll(rReplace.getKey(),
										   rReplace.getValue());
				}
			}
		}

		return sString;
	}
}
