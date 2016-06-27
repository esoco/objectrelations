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
package de.esoco.lib.expression;

import de.esoco.lib.expression.function.AbstractBinaryFunction;
import de.esoco.lib.expression.function.AbstractFunction;
import de.esoco.lib.expression.function.AbstractInvertibleFunction;
import de.esoco.lib.expression.function.ExceptionMappingBinaryFunction;
import de.esoco.lib.expression.function.GetSubstring;
import de.esoco.lib.expression.predicate.AbstractPredicate;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import java.net.IDN;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/********************************************************************
 * Contains factory methods for string-specific functions and predicates.
 *
 * @author eso
 */
public class StringFunctions
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Function<CharSequence, Integer> STRING_LENGTH =
		new AbstractFunction<CharSequence, Integer>("StringLength")
		{
			@Override
			@SuppressWarnings("boxing")
			public Integer evaluate(CharSequence sValue)
			{
				return sValue.length();
			}
		};

	private static final Function<String, String> TRIM =
		new AbstractFunction<String, String>("Trim")
		{
			@Override
			public String evaluate(String sValue)
			{
				return sValue.trim();
			}
		};

	private static final Function<String, String> TO_LOWER_CASE =
		new AbstractFunction<String, String>("ToLowerCase")
		{
			@Override
			public String evaluate(String sValue)
			{
				return sValue.toLowerCase();
			}
		};

	private static final Function<String, String> TO_UPPER_CASE =
		new AbstractFunction<String, String>("ToUpperCase")
		{
			@Override
			public String evaluate(String sValue)
			{
				return sValue.toUpperCase();
			}
		};

	private static final InvertibleFunction<String, byte[]> TO_BYTE_ARRAY =
		new AbstractInvertibleFunction<String, byte[]>("ToByteArray")
		{
			@Override
			public byte[] evaluate(String sValue)
			{
				return sValue != null ? sValue.getBytes() : null;
			}

			@Override
			public String invert(byte[] rValue)
			{
				return rValue != null ? new String(rValue) : null;
			}
		};

	private static final Function<String, String> CAPITALIZED_IDENTIFIER =
		new AbstractFunction<String, String>("CapitalizedIdentifier")
		{
			@Override
			public String evaluate(String sValue)
			{
				return TextUtil.capitalizedIdentifier(sValue);
			}
		};

	private static final Function<String, String> UPPERCASE_IDENTIFIER =
		new AbstractFunction<String, String>("UppercaseIdentifier")
		{
			@Override
			public String evaluate(String sValue)
			{
				return TextUtil.uppercaseIdentifier(sValue);
			}
		};

	private static final Predicate<CharSequence> IS_EMPTY =
		new AbstractPredicate<CharSequence>("StringIsEmpty")
		{
			@Override
			@SuppressWarnings("boxing")
			public Boolean evaluate(CharSequence rValue)
			{
				return rValue == null || rValue.length() == 0;
			}
		};

	private static final Predicate<CharSequence> IS_NOT_EMPTY =
		new AbstractPredicate<CharSequence>("StringIsNotEmpty")
		{
			@Override
			@SuppressWarnings("boxing")
			public Boolean evaluate(CharSequence rValue)
			{
				return rValue != null && rValue.length() > 0;
			}
		};

	private static final Predicate<String> IS_VALID_IDN_UNICODE =
		new AbstractPredicate<String>("IsValidIdnUnicode")
		{
			@Override
			@SuppressWarnings("boxing")
			public Boolean evaluate(String sIDN)
			{
				try
				{
					IDN.toASCII(sIDN);

					return true;
				}
				catch (Exception e)
				{
					return false;
				}
			}
		};

	private static final Function<String, String> IDN_UNICODE_TO_ASCII =
		new AbstractFunction<String, String>("IdnUnicodeToAscii")
		{
			@Override
			public String evaluate(String sValue)
			{
				return IDN.toASCII(sValue);
			}
		};

	private static final Function<String, String> IDN_ASCII_TO_UNICODE =
		new AbstractFunction<String, String>("IdnAsciiToUnicode")
		{
			@Override
			public String evaluate(String sValue)
			{
				return IDN.toUnicode(sValue);
			}
		};

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private StringFunctions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns a new function that capitalizes a string with a certain
	 * separator. See {@link TextConvert#capitalize(String, String)} for
	 * details.
	 *
	 * @param  sSeparator The separator string
	 *
	 * @return A new function instance
	 */
	public static BinaryFunction<String, String, String> capitalize(
		String sSeparator)
	{
		return new AbstractBinaryFunction<String, String, String>(sSeparator,
																  "Capitalize")
		{
			@Override
			public String evaluate(String sText, String sSeparator)
			{
				return TextConvert.capitalize(sText, sSeparator);
			}
		};
	}

	/***************************************
	 * Returns a function constant that converts a string to a capitalized
	 * identifier. See {@link TextConvert#capitalizedIdentifier(String)} for
	 * details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> capitalizedIdentifier()
	{
		return CAPITALIZED_IDENTIFIER;
	}

	/***************************************
	 * Returns a new binary function that converts a byte array into a string
	 * with a certain character set.
	 *
	 * @param  rCharset The character set to apply for the encoding
	 *
	 * @return A new binary function instance
	 */
	public static BinaryFunction<byte[], Charset, String> createString(
		Charset rCharset)
	{
		return new ExceptionMappingBinaryFunction<byte[], Charset, String>(rCharset,
																		   "createString")
		{
			@Override
			protected String evaluateWithException(
				byte[]  rBytes,
				Charset rCharset) throws Exception
			{
				return new String(rBytes, rCharset);
			}
		};
	}

	/***************************************
	 * Returns a new binary function instance that searches input values for a
	 * certain regular expression.
	 *
	 * @param  sRegex The regular expression to search for
	 *
	 * @return A new binary function instance
	 */
	public static BinaryFunction<String, String, String> find(String sRegex)
	{
		return new AbstractBinaryFunction<String, String, String>(sRegex,
																  "Find")
		{
			@Override
			public String evaluate(String sInput, String sRegex)
			{
				Matcher aMatcher = Pattern.compile(sRegex).matcher(sInput);

				return aMatcher.find() ? aMatcher.group() : null;
			}
		};
	}

	/***************************************
	 * Returns a new binary function instance that formats input values as
	 * defined by {@link String#format(String, Object...)}.
	 *
	 * @param  sPattern The format string
	 *
	 * @return A new binary function instance
	 */
	public static <T> BinaryFunction<T, String, String> format(String sPattern)
	{
		return new AbstractBinaryFunction<T, String, String>(sPattern,
															 "StringFormat")
		{
			@Override
			public String evaluate(T rValue, String sPattern)
			{
				return String.format(sPattern, rValue);
			}
		};
	}

	/***************************************
	 * Returns a function constant that converts an IDN ASCII domain name string
	 * into a Unicode string. See {@link IDN} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> idnAsciiToUnicode()
	{
		return IDN_ASCII_TO_UNICODE;
	}

	/***************************************
	 * Returns a function constant that converts an IDN Unicode domain name
	 * string into an ASCII string. See {@link IDN} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> idnUnicodeToAscii()
	{
		return IDN_UNICODE_TO_ASCII;
	}

	/***************************************
	 * Returns a predicate constant that checks whether a character sequence is
	 * empty. The checked value is considered empty if it is NULL or has a
	 * length of zero.
	 *
	 * @return A predicate constant that tests for empty strings
	 */
	public static Predicate<CharSequence> isEmpty()
	{
		return IS_EMPTY;
	}

	/***************************************
	 * Returns a predicate constant that checks whether a string contains a
	 * valid internationalized domain name in Unicode format. This is achieved
	 * by invoking {@link IDN#toASCII(String)} on the input string and checking
	 * for exceptions.
	 *
	 * @return A predicate constant that tests for empty strings
	 */
	public static Predicate<String> isValidIdnUnicode()
	{
		return IS_VALID_IDN_UNICODE;
	}

	/***************************************
	 * Returns a predicate constant that checks whether a character sequence is
	 * not empty. The checked value is considered not empty if it is not NULL
	 * and has a length greater than zero.
	 *
	 * @return A predicate constant that tests for not empty strings
	 */
	public static Predicate<CharSequence> notEmpty()
	{
		return IS_NOT_EMPTY;
	}

	/***************************************
	 * Returns a new binary function instance that splits a string around a
	 * certain regular expression pattern into an array of strings. See the
	 * documentation of {@link String#split(String)} for details.
	 *
	 * @param  sPattern The regular expression pattern
	 *
	 * @return A new binary function instance
	 */
	public static BinaryFunction<String, String, String[]> split(
		String sPattern)
	{
		return new AbstractBinaryFunction<String, String, String[]>(sPattern,
																	"StringSplit")
		{
			@Override
			public String[] evaluate(String sValue, String sPattern)
			{
				return sValue.split(sPattern);
			}
		};
	}

	/***************************************
	 * Returns a function constant that queries the length of strings or other
	 * character sequence.
	 *
	 * @return A function constant
	 */
	public static Function<CharSequence, Integer> stringLength()
	{
		return STRING_LENGTH;
	}

	/***************************************
	 * Returns a new instance of {@link GetSubstring} that has a substring of an
	 * input string as it's result. See {@link String#substring(int, int)} for
	 * details.
	 *
	 * @param  nBeginIndex The index of the first char of the substring
	 * @param  nEndIndex   The index of the char after the last char of the
	 *                     substring or -1 for the end of the input string
	 *
	 * @return A new function instance
	 */
	public static GetSubstring substring(int nBeginIndex, final int nEndIndex)
	{
		return new GetSubstring(nBeginIndex, nEndIndex);
	}

	/***************************************
	 * Returns an invertible function constant that converts a string to a byte
	 * array and vice versa. Applies the method {@link String#getBytes()} or the
	 * constructor {@link String#String(byte[])} for the inversion. NULL values
	 * are ignored and will also yield NULL.
	 *
	 * @return A function constant
	 */
	public static InvertibleFunction<String, byte[]> toByteArray()
	{
		return TO_BYTE_ARRAY;
	}

	/***************************************
	 * Returns a new binary function instance that converts a string into a
	 * modifiable collection of strings by splitting it around a certain regular
	 * expression pattern. See the documentation of {@link String#split(String)}
	 * for details.
	 *
	 * @param  rCollectionClass The type of collection to create
	 * @param  sSplitPattern    The regular expression split pattern
	 * @param  bTrim            TRUE if each list element should be trimmed with
	 *                          {@link String#trim()}
	 *
	 * @return A new binary function instance
	 */
	public static <C extends Collection<String>> BinaryFunction<String,
																String, C> toCollection(
		final Class<C> rCollectionClass,
		String		   sSplitPattern,
		final boolean  bTrim)
	{
		return new AbstractBinaryFunction<String, String, C>(sSplitPattern,
															 "StringSplit")
		{
			@Override
			public C evaluate(String sValue, String sPattern)
			{
				C aResult = ReflectUtil.newInstance(rCollectionClass);

				if (sValue != null && sValue.length() > 0)
				{
					String[] aSplit = sValue.split(sPattern);

					for (String sElement : aSplit)
					{
						aResult.add(bTrim ? sElement.trim() : sElement);
					}
				}

				return aResult;
			}
		};
	}

	/***************************************
	 * Returns a new binary function instance that converts a string into a
	 * modifiable list of strings by splitting it around a certain regular
	 * expression pattern. See the documentation of {@link String#split(String)}
	 * for details.
	 *
	 * @param  sSplitPattern The regular expression split pattern
	 * @param  bTrim         TRUE if each list element should be trimmed
	 *
	 * @return A new binary function instance
	 */
	@SuppressWarnings({ "unchecked" })
	public static <C extends Collection<String>> BinaryFunction<String,
																String,
																List<String>> toList(
		String  sSplitPattern,
		boolean bTrim)
	{
		Class<C> rListClass = (Class<C>) (Class<?>) ArrayList.class;

		return (BinaryFunction<String, String, List<String>>) toCollection(rListClass,
																		   sSplitPattern,
																		   bTrim);
	}

	/***************************************
	 * Invokes the function returned by {@link #toList(String, boolean)} on an
	 * input value and returns the result.
	 *
	 * @param  sInput        The input string
	 * @param  sSplitPattern The regular expression split pattern
	 * @param  bTrim         TRUE if each list element should be trimmed
	 *
	 * @return The list containing the split entries
	 */
	public static List<String> toList(String  sInput,
									  String  sSplitPattern,
									  boolean bTrim)
	{
		return toList(sSplitPattern, bTrim).evaluate(sInput);
	}

	/***************************************
	 * Returns a function constant that converts a string to lower case. See the
	 * method {@link String#toLowerCase()} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> toLowerCase()
	{
		return TO_LOWER_CASE;
	}

	/***************************************
	 * Returns a new binary function instance that converts a string into a
	 * modifiable list of strings by splitting it around a certain regular
	 * expression pattern. See the documentation of {@link String#split(String)}
	 * for details.
	 *
	 * @param  sSplitPattern The regular expression split pattern
	 * @param  bTrim         TRUE if each list element should be trimmed
	 * @param  bOrdered      TRUE if the resulting set shall keep the original
	 *                       order of the elements in an input string
	 *
	 * @return A new binary function instance
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Collection<String>> BinaryFunction<String,
																String,
																Set<String>> toSet(
		String  sSplitPattern,
		boolean bTrim,
		boolean bOrdered)
	{
		Class<C> rSetClass =
			(Class<C>) (bOrdered ? LinkedHashSet.class : HashSet.class);

		return (BinaryFunction<String, String, Set<String>>) toCollection(rSetClass,
																		  sSplitPattern,
																		  bTrim);
	}

	/***************************************
	 * Invokes the function returned by {@link #toList(String, boolean)} on an
	 * input value and returns the result.
	 *
	 * @param  sInput        The input string
	 * @param  sSplitPattern The regular expression split pattern
	 * @param  bTrim         TRUE if each list element should be trimmed
	 * @param  bOrdered      TRUE if the resulting set shall keep the original
	 *                       order of the elements in an input string
	 *
	 * @return The list containing the split entries
	 */
	public static Set<String> toSet(String  sInput,
									String  sSplitPattern,
									boolean bTrim,
									boolean bOrdered)
	{
		return toSet(sSplitPattern, bTrim, bOrdered).evaluate(sInput);
	}

	/***************************************
	 * Returns a function constant that converts a string to upper case. See the
	 * method {@link String#toUpperCase()} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> toUpperCase()
	{
		return TO_UPPER_CASE;
	}

	/***************************************
	 * Returns a function constant that removes leading and trailing whitespace
	 * from a string. See the method {@link String#trim()} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> trim()
	{
		return TRIM;
	}

	/***************************************
	 * Returns a function constant that converts a string to an uppercase
	 * identifier. See method {@link TextConvert#uppercaseIdentifier(String)}
	 * for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> uppercaseIdentifier()
	{
		return UPPERCASE_IDENTIFIER;
	}
}
