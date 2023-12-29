//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.lib.expression.function.GetSubstring;
import de.esoco.lib.expression.monad.Try;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextUtil;

import java.net.IDN;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains factory methods for string-specific functions and predicates.
 *
 * @author eso
 */
public class StringFunctions {
	private static final Function<String, String> TO_LOWER_CASE =
		String::toLowerCase;

	private static final Function<String, String> TO_UPPER_CASE =
		String::toUpperCase;

	private static final InvertibleFunction<String, byte[]> TO_BYTE_ARRAY =
		InvertibleFunction.of(s -> s != null ? s.getBytes() : null,
			b -> b != null ? new String(b) : null);

	private static final Predicate<CharSequence> IS_EMPTY =
		s -> s == null || s.length() == 0;

	private static final Predicate<CharSequence> NOT_EMPTY =
		s -> s != null && s.length() > 0;

	private static final Predicate<String> IS_VALID_IDN_UNICODE =
		s -> Try.now(() -> IDN.toASCII(s) != null).orUse(false);

	private static final Function<String, String> IDN_UNICODE_TO_ASCII =
		s -> IDN.toASCII(s);

	private static final Function<String, String> IDN_ASCII_TO_UNICODE =
		s -> IDN.toUnicode(s);

	/**
	 * Private, only static use.
	 */
	private StringFunctions() {
	}

	/**
	 * Returns a new function that capitalizes a string with a certain
	 * separator. See {@link TextConvert#capitalize(String, String)} for
	 * details.
	 *
	 * @param separator The separator string
	 * @return A new function instance
	 */
	public static BinaryFunction<String, String, String> capitalize(
		String separator) {
		return new AbstractBinaryFunction<String, String, String>(separator,
			"Capitalize") {
			@Override
			public String evaluate(String text, String separator) {
				return TextConvert.capitalize(text, separator);
			}
		};
	}

	/**
	 * Returns a function constant that converts a string to a capitalized
	 * identifier. See {@link TextConvert#capitalizedIdentifier(String)} for
	 * details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> capitalizedIdentifier() {
		return TextUtil::capitalizedIdentifier;
	}

	/**
	 * Returns a new binary function instance that searches input values for a
	 * certain regular expression.
	 *
	 * @param regex The regular expression to search for
	 * @return A new binary function instance
	 */
	public static BinaryFunction<String, String, String> find(String regex) {
		return new AbstractBinaryFunction<String, String, String>(regex,
			"Find") {
			@Override
			public String evaluate(String input, String regex) {
				Matcher matcher = Pattern.compile(regex).matcher(input);

				return matcher.find() ? matcher.group() : null;
			}
		};
	}

	/**
	 * Returns a new binary function instance that formats input values as
	 * defined by {@link String#format(String, Object...)}.
	 *
	 * @param pattern The format string
	 * @return A new binary function instance
	 */
	public static <T> BinaryFunction<T, String, String> format(String pattern) {
		return new AbstractBinaryFunction<T, String, String>(pattern,
			"StringFormat") {
			@Override
			public String evaluate(T value, String pattern) {
				return String.format(pattern, value);
			}
		};
	}

	/**
	 * Returns a function constant that converts an IDN ASCII domain name
	 * string
	 * into a Unicode string. See {@link IDN} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> idnAsciiToUnicode() {
		return IDN_ASCII_TO_UNICODE;
	}

	/**
	 * Returns a function constant that converts an IDN Unicode domain name
	 * string into an ASCII string. See {@link IDN} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> idnUnicodeToAscii() {
		return IDN_UNICODE_TO_ASCII;
	}

	/**
	 * Returns a predicate constant that checks whether a character sequence is
	 * empty. The checked value is considered empty if it is NULL or has a
	 * length of zero.
	 *
	 * @return A predicate constant that tests for empty strings
	 */
	public static Predicate<CharSequence> isEmpty() {
		return IS_EMPTY;
	}

	/**
	 * Checks whether a string variable is empty (NULL or has a length of
	 * zero).
	 *
	 * @param string The string to test
	 * @return TRUE if the string variable is empty
	 */
	public static boolean isEmpty(CharSequence string) {
		return IS_EMPTY.test(string);
	}

	/**
	 * Returns a predicate constant that checks whether a string contains a
	 * valid internationalized domain name in Unicode format. This is achieved
	 * by invoking {@link IDN#toASCII(String)} on the input string and checking
	 * for exceptions.
	 *
	 * @return A predicate constant that tests for empty strings
	 */
	public static Predicate<String> isValidIdnUnicode() {
		return IS_VALID_IDN_UNICODE;
	}

	/**
	 * Returns a predicate constant that checks whether a character sequence is
	 * not empty. The checked value is considered not empty if it is not NULL
	 * and has a length greater than zero.
	 *
	 * @return A predicate constant that tests for not empty strings
	 */
	public static Predicate<CharSequence> notEmpty() {
		return NOT_EMPTY;
	}

	/**
	 * Returns a new binary function instance that splits a string around a
	 * certain regular expression pattern into an array of strings. See the
	 * documentation of {@link String#split(String)} for details.
	 *
	 * @param pattern The regular expression pattern
	 * @return A new binary function instance
	 */
	public static BinaryFunction<String, String, String[]> split(
		String pattern) {
		return new AbstractBinaryFunction<String, String, String[]>(pattern,
			"StringSplit") {
			@Override
			public String[] evaluate(String value, String pattern) {
				return value.split(pattern);
			}
		};
	}

	/**
	 * Returns a new instance of {@link GetSubstring} that has a substring
	 * of an
	 * input string as it's result. See {@link String#substring(int, int)} for
	 * details.
	 *
	 * @param beginIndex The index of the first char of the substring
	 * @param endIndex   The index of the char after the last char of the
	 *                   substring or -1 for the end of the input string
	 * @return A new function instance
	 */
	public static GetSubstring substring(int beginIndex, final int endIndex) {
		return new GetSubstring(beginIndex, endIndex);
	}

	/**
	 * Returns an invertible function constant that converts a string to a byte
	 * array and vice versa. Applies the method {@link String#getBytes()} or
	 * the
	 * constructor {@link String#String(byte[])} for the inversion. NULL values
	 * are ignored and will also yield NULL.
	 *
	 * @return A function constant
	 */
	public static InvertibleFunction<String, byte[]> toByteArray() {
		return TO_BYTE_ARRAY;
	}

	/**
	 * Returns a new binary function instance that converts a string into a
	 * modifiable collection of strings by splitting it around a certain
	 * regular
	 * expression pattern. See the documentation of
	 * {@link String#split(String)}
	 * for details.
	 *
	 * @param collectionClass The type of collection to create
	 * @param splitPattern    The regular expression split pattern
	 * @param trim            TRUE if each list element should be trimmed with
	 *                        {@link String#trim()}
	 * @return A new binary function instance
	 */
	public static <C extends Collection<String>> BinaryFunction<String, String
		, C> toCollection(
		final Class<C> collectionClass, String splitPattern,
		final boolean trim) {
		return new AbstractBinaryFunction<String, String, C>(splitPattern,
			"StringSplit") {
			@Override
			public C evaluate(String value, String pattern) {
				C result = ReflectUtil.newInstance(collectionClass);

				if (value != null && value.length() > 0) {
					String[] split = value.split(pattern);

					for (String element : split) {
						result.add(trim ? element.trim() : element);
					}
				}

				return result;
			}
		};
	}

	/**
	 * Returns a new binary function instance that converts a string into a
	 * modifiable list of strings by splitting it around a certain regular
	 * expression pattern. See the documentation of
	 * {@link String#split(String)}
	 * for details.
	 *
	 * @param splitPattern The regular expression split pattern
	 * @param trim         TRUE if each list element should be trimmed
	 * @return A new binary function instance
	 */
	@SuppressWarnings({ "unchecked" })
	public static <C extends Collection<String>> BinaryFunction<String, String
		, List<String>> toList(
		String splitPattern, boolean trim) {

		// temporary type needed to prevent javac error
		Class<?> tmp = ArrayList.class;
		Class<C> listClass = (Class<C>) tmp;

		return (BinaryFunction<String, String, List<String>>) toCollection(
			listClass, splitPattern, trim);
	}

	/**
	 * Invokes the function returned by {@link #toList(String, boolean)} on an
	 * input value and returns the result.
	 *
	 * @param input        The input string
	 * @param splitPattern The regular expression split pattern
	 * @param trim         TRUE if each list element should be trimmed
	 * @return The list containing the split entries
	 */
	public static List<String> toList(String input, String splitPattern,
		boolean trim) {
		return toList(splitPattern, trim).evaluate(input);
	}

	/**
	 * Returns a function constant that converts a string to lower case. See
	 * the
	 * method {@link String#toLowerCase()} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> toLowerCase() {
		return TO_LOWER_CASE;
	}

	/**
	 * Returns a new binary function instance that converts a string into a
	 * modifiable list of strings by splitting it around a certain regular
	 * expression pattern. See the documentation of
	 * {@link String#split(String)}
	 * for details.
	 *
	 * @param splitPattern The regular expression split pattern
	 * @param trim         TRUE if each list element should be trimmed
	 * @param ordered      TRUE if the resulting set shall keep the original
	 *                     order of the elements in an input string
	 * @return A new binary function instance
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Collection<String>> BinaryFunction<String, String
		, Set<String>> toSet(
		String splitPattern, boolean trim, boolean ordered) {
		Class<C> setClass =
			(Class<C>) (ordered ? LinkedHashSet.class : HashSet.class);

		return (BinaryFunction<String, String, Set<String>>) toCollection(
			setClass, splitPattern, trim);
	}

	/**
	 * Invokes the function returned by {@link #toList(String, boolean)} on an
	 * input value and returns the result.
	 *
	 * @param input        The input string
	 * @param splitPattern The regular expression split pattern
	 * @param trim         TRUE if each list element should be trimmed
	 * @param ordered      TRUE if the resulting set shall keep the original
	 *                     order of the elements in an input string
	 * @return The list containing the split entries
	 */
	public static Set<String> toSet(String input, String splitPattern,
		boolean trim, boolean ordered) {
		return toSet(splitPattern, trim, ordered).evaluate(input);
	}

	/**
	 * Returns a function constant that converts a string to upper case. See
	 * the
	 * method {@link String#toUpperCase()} for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> toUpperCase() {
		return TO_UPPER_CASE;
	}

	/**
	 * Returns a function constant that converts a string to an uppercase
	 * identifier. See method {@link TextConvert#uppercaseIdentifier(String)}
	 * for details.
	 *
	 * @return A function constant
	 */
	public static Function<String, String> uppercaseIdentifier() {
		return TextConvert::uppercaseIdentifier;
	}
}
