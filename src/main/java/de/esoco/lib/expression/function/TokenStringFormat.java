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
package de.esoco.lib.expression.function;

import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A transformation function that creates formatted strings from arbitrary input
 * objects. The format of the result is defined by a token string that contains
 * tokens wrapped in curly braces that will be replaced by formatted values. The
 * format of a token is {&lt;value&gt;[:&lt;format&gt;]} where &lt;value&gt; is
 * a description of the data value to be derived from the transformation input
 * object and to be inserted into the result string. The optional &lt;format&gt;
 * element defines how the value will appear in the result string. If omitted,
 * the value object will be inserted as the result of it's toString() method.
 *
 * <p>If the resulting value of a token is a collection or an array it's
 * elements will be processed. If the format string is preceded with a non-zero
 * integer value, only as many elements as defined by the value from the
 * beginning of the collection will be processed. If a format has been defined
 * it will be applied to each element. The resulting element strings will be
 * concatenated. If instead of a format a recursive token string is used the
 * collection or array will be passed to it unchanged.</p>
 *
 * <p>This class is intended for subclassing but it can also be used directly.
 * It defines some basic tokens that all support the formatting of values as
 * described below. The following tokens can be used in a standard token
 * string:</p>
 *
 * <ul>
 *   <li>#: The input value itself</li>
 *   <li>&lt;propertyMethod&gt;([arguments]): Allows to query a property of the
 *     input object. The method with the name &lt;propertyMethod&gt; will be
 *     invoked on the input object and the result will be used as the token
 *     value. If one or more arguments are defined the argument values are
 *     parsed with the method {@link TextUtil#parseObject(String)} and a method
 *     with such parameters will be looked up and invoked. For parameters that
 *     have a primitive type representation (like int for Integer) a method with
 *     a parameter of the primitive type will be looked up.</li>
 *   <li>now: a java.util.Date instance representing the current time and
 *     date</li>
 * </ul>
 *
 * <p>In this base implementation the &lt;format&gt; element (if provided) can
 * be of several types. The type to be used to parse the format string is
 * defined by the first character in the format string. The following format
 * type prefix characters are supported:</p>
 *
 * <ul>
 *   <li>&amp;: another token string pattern that will be applied
 *     recursively</li>
 *   <li>F: a format string that can be processed by an instance of the class
 *     {@link java.util.Formatter}, invoked through String.format().</li>
 *   <li>C: {@link java.text.ChoiceFormat}</li>
 *   <li>D: {@link java.text.SimpleDateFormat}</li>
 *   <li>N: {@link java.text.DecimalFormat}</li>
 * </ul>
 *
 * <p>Example: "Caused by {getCause():&amp;{getStackTrace():F\t| %s\n}}".
 * Assuming that the input value is an object that has a causing exception, this
 * format string will query the causing exception with the property method
 * getCause() and invoke the method getStackTrace() on the result. Each element
 * in the returned array of stacktrace elements will be formatted with the
 * Formatter format string {@code '\t| %s\n'}, resulting in each element being
 * printed on a single line.</p>
 *
 * <p>As said above this class is intended for subclassing. A subclass can
 * define it's own instances of either one of the inner classes Token or
 * PropertyToken or of an own subclass and return the matching Token instance
 * for a certain token string element from the method {@link #getToken(String)}.
 * More sophisticated implementations may also choose to override the complete
 * parsing in the {@link #parseToken(String, String)} method. If a subclass
 * token should provide it's own format it must override the Token method
 * parseFormat().</p>
 *
 * @author eso
 */
public class TokenStringFormat<T> extends AbstractFunction<T, String> {

	static final Pattern PROPERTY_METHOD_PATTERN =
		Pattern.compile("(\\w+?)\\((.*)\\)");

	static final Pattern FORMAT_PATTERN =
		Pattern.compile("(?s)(\\d*)([&FDNC])(.+)");

	private static final Map<String, Token<Object>> tokenRegistry =
		new HashMap<String, Token<Object>>();

	static {
		registerToken("now", new Token<Object>("now") {
			@Override
			protected Object extractValue(Object ignore) {
				return new Date();
			}
		});
		registerToken("#", new Token<Object>("#") {
			@Override
			protected Object extractValue(Object input) {
				return input;
			}
		});
	}

	private final List<Token<? super T>> tokens =
		new ArrayList<Token<? super T>>();

	private String targetPattern;

	/**
	 * Creates a new instance for a certain token string.
	 *
	 * @param tokenString The token string
	 * @throws IllegalArgumentException If the string contains invalid tokens
	 */
	public TokenStringFormat(String tokenString) {
		// use empty string as toString() is overridden
		super("");
		parseTokenString(tokenString);
	}

	/**
	 * Convenience method to format a value according to a certain token
	 * string.
	 *
	 * @param tokenString The token string containing the format description
	 * @param value       The value to format
	 * @return The formatted string
	 * @throws IllegalArgumentException If parsing or formatting fails
	 */
	public static String format(String tokenString, Object value) {
		return new TokenStringFormat<Object>(tokenString).evaluate(value);
	}

	/**
	 * Registers a new global tokenString that will be applied by all format
	 * instances.
	 *
	 * @param tokenString The tokenString string to register the tokenString
	 *                    for
	 * @param token       The tokenString to apply
	 */
	public static void registerToken(String tokenString, Token<Object> token) {
		tokenRegistry.put(tokenString, token);
	}

	/**
	 * Transforms the input value into a string by applying this instance's
	 * token string pattern to it.
	 *
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public String evaluate(T input) {
		StringBuilder sb = new StringBuilder(targetPattern);

		for (Token<? super T> token : tokens) {
			String tokenString = token.toString();
			String elem = token.transform(input);
			int start = sb.indexOf(tokenString);
			int end = start + tokenString.length();

			sb.replace(start, end, elem != null ? elem : "");
		}

		return sb.toString();
	}

	/**
	 * Returns a string describing this instance.
	 *
	 * @return A string description of this instance
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + targetPattern + "](" +
			INPUT_PLACEHOLDER + ")";
	}

	/**
	 * Returns a registered token instance for a certain string representation
	 * of the token. This method must be overridden by subclasses that define
	 * own tokens.
	 *
	 * @param token The token's string representation (without curly braces)
	 * @return The corresponding token or NULL if none has been registered
	 */
	protected Token<? super T> getToken(String token) {
		return tokenRegistry.get(token);
	}

	/**
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> other) {
		@SuppressWarnings("rawtypes")
		TokenStringFormat otherFunction = (TokenStringFormat) other;

		return Objects.equals(targetPattern, otherFunction.targetPattern) &&
			Objects.equals(tokens, otherFunction.tokens);
	}

	/**
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode() {
		return 31 * targetPattern.hashCode() + tokens.hashCode();
	}

	/**
	 * Parses a single token that has been read from the token string.
	 * Subclasses may override this to implement their own parse algorithm.
	 *
	 * @param tokenString The token's string
	 * @param tokenFormat The token's format
	 * @return A new token instance
	 * @throws IllegalArgumentException If the arguments cannot be parsed
	 */
	protected Token<? super T> parseToken(String tokenString,
		String tokenFormat) {
		Matcher methodMatcher = PROPERTY_METHOD_PATTERN.matcher(tokenString);

		Token<? super T> token;

		if (methodMatcher.matches()) {
			String method = methodMatcher.group(1);
			String arguments = methodMatcher.group(2);
			Object[] args = null;

			if (!arguments.isEmpty()) {
				String[] argStrings = arguments.split(",");

				args = new Object[argStrings.length];

				for (int i = 0; i < args.length; i++) {
					args[i] = TextUtil.parseObject(argStrings[i]);
				}
			}

			token = new PropertyToken(this, method, null, args);
		} else {
			token = getToken(tokenString);
		}

		if (token == null) {
			throw new IllegalArgumentException("Invalid token: " + tokenString);
		}

		if (tokenFormat != null) {
			if (!tokenFormat.isEmpty()) {
				token = token.copy();
				token.setFormat(tokenFormat);
			} else {
				throw new IllegalArgumentException("Empty format string");
			}
		}

		return token;
	}

	/**
	 * Parse's the token string and generates the internal data structures that
	 * are needed for the transformation.
	 *
	 * @param tokenString The token string to parse
	 * @throws IllegalArgumentException If the token string cannot be parsed
	 */
	private void parseTokenString(String tokenString) {
		StringBuilder sb = new StringBuilder();
		int length = tokenString.length();
		int textStart = 0;
		int start = 0;
		int end = 0;

		while ((start = tokenString.indexOf('{', start)) >= 0) {
			String parsedToken;
			String tokenFormat = null;
			int subCount = 0;
			int formatPos = -1;
			char c;

			end = ++start;

			while (end < length &&
				((c = tokenString.charAt(end)) != '}' || subCount > 0)) {
				// search terminating '}' but skip sub-pairs of braces '{...}'
				switch (c) {
					case '{':
						subCount++;
						break;

					case '}':
						subCount--;
						break;

					case ':':

						// remember first format separator
						if (formatPos == -1) {
							formatPos = end;
						}

						break;
				}

				end++;
			}

			if (end == length || formatPos == end) {
				throw new IllegalArgumentException(
					"Incomplete token: " + tokenString);
			}

			if (formatPos > 0) {
				parsedToken = tokenString.substring(start, formatPos);
				tokenFormat = tokenString.substring(formatPos + 1, end);
			} else {
				parsedToken = tokenString.substring(start, end);
			}

			Token<? super T> token = parseToken(parsedToken, tokenFormat);

			tokens.add(token);

			sb.append(tokenString, textStart, start - 1);
			sb.append(token.toString());

			textStart = start = end + 1;
		}

		sb.append(tokenString.substring(textStart));
		targetPattern = sb.toString();
	}

	/**
	 * Represents a single token in the token string and the associated
	 * formatting code.
	 */
	public static abstract class Token<T> implements Cloneable {

		private final TokenStringFormat<?> parent;

		private final String token;

		private Object formatObject = null;

		private int arrayElementCount = -1;

		/**
		 * Creates a new global token that has no parent.
		 *
		 * @param token The token string
		 */
		protected Token(String token) {
			this(token, null);
		}

		/**
		 * Creates a new global token without a parent and with a certain
		 * format. The format string argument will be parsed with the method
		 * {@link #parseFormat(String)}.
		 *
		 * @param token        The string description of the token
		 * @param formatString The format string for this token
		 */
		protected Token(String token, String formatString) {
			this(null, token, formatString);
		}

		/**
		 * Creates a new token with a certain parent token string format and
		 * format string. The format string argument will be parsed with the
		 * method {@link #parseFormat(String)}. Tokens of subclassed formats
		 * should always be created with this constructor to allow the correct
		 * creation of nested formats.
		 *
		 * @param parent       The parent format of this token
		 * @param token        The string description of the token
		 * @param formatString The format string for this token
		 */
		protected Token(TokenStringFormat<?> parent, String token,
			String formatString) {
			this.parent = parent;
			this.token = token;

			if (formatString != null) {
				this.formatObject = parseFormat(formatString);
			}
		}

		/**
		 * Returns the format object that will be used to format input values.
		 *
		 * @return The format object
		 */
		public final Object getFormatObject() {
			return formatObject;
		}

		/**
		 * Returns the token string of this token.
		 *
		 * @return The token string
		 */
		public final String getTokenString() {
			return token;
		}

		/**
		 * Returns the token's string representation. This is the token's
		 * string
		 * description inside curly braces ('{token}').
		 *
		 * @return The string representation of the token
		 */
		@Override
		public String toString() {
			return "{" + token + "}";
		}

		/**
		 * Transforms the input value into a string that can be inserted into
		 * the resulting string of a string token transformation. Returns the
		 * result of the {@link #applyFormat(Object)} method that has been
		 * invoked on the return value of the {@link #extractValue(Object)}
		 * method. If the input value is a collection or an array the format
		 * will be applied to each element in the array and the resulting
		 * strings will be concatenated.
		 *
		 * @param input The input value of the token string transformation
		 * @return The result of transforming this token into a string derived
		 * from the input value
		 */
		public final String transform(T input) {
			Object value = extractValue(input);
			String result;

			if (!(formatObject instanceof TokenStringFormat<?>) &&
				(value.getClass().isArray() ||
					value instanceof Collection<?>)) {
				if (value instanceof Collection<?>) {
					value = ((Collection<?>) value).toArray();
				}

				StringBuilder builder = new StringBuilder();
				int count = Array.getLength(value);

				if (arrayElementCount > 0) {
					count = Math.min(count, arrayElementCount);
				}

				for (int i = 0; i < count; i++) {
					builder.append(applyFormat(Array.get(value, i)));

					if (formatObject == null) {
						builder.append(',');
					}
				}

				if (formatObject == null && count > 0) {
					builder.setLength(builder.length() - 1);
				}

				result = builder.toString();
			} else {
				result = applyFormat(value);
			}

			return result;
		}

		/**
		 * Subclasses can override this method to apply a specific format to
		 * the
		 * value that has been returned by {@link #extractValue(Object)}. The
		 * default implementation handles the following types of format
		 * objects:
		 *
		 * <ul>
		 *   <li>String: returns String.format((String) Format, value)</li>
		 *   <li>TokenStringFormat: returns Format.transform(value)</li>
		 *   <li>java.text.Format: returns Format.format(value)</li>
		 *   <li>Other types or NULL: returns value.toString()</li>
		 * </ul>
		 *
		 * @param value The value that has been extracted from the input
		 * @return A string containing the formatted string representing the
		 * input value
		 */
		@SuppressWarnings("unchecked")
		protected String applyFormat(Object value) {
			if (formatObject instanceof String) {
				return String.format((String) formatObject, value);
			} else if (formatObject instanceof TokenStringFormat<?>) {
				return ((TokenStringFormat<Object>) formatObject).evaluate(
					value);
			} else if (formatObject instanceof Format) {
				return ((Format) formatObject).format(value);
			} else if (value != null) {
				return value.toString();
			} else {
				return "null";
			}
		}

		/**
		 * Extracts the value from the input object that corresponds to this
		 * token and returns a string representation of that value.
		 *
		 * @param input The input object to retrieve the value from
		 * @return A string derived from the input value
		 */
		protected abstract Object extractValue(T input);

		/**
		 * Can be overridden by subclasses that support different formats to
		 * implement the parsing of format strings. The default implementation
		 * provides support for several format types as described in the main
		 * class documentation.
		 *
		 * @param formatString The format string (must not be NULL)
		 * @return The object representing the parsed format
		 * @throws IllegalArgumentException If the argument doesn't contain a
		 *                                  recognized format or parsing it
		 *                                  fails
		 */
		protected Object parseFormat(String formatString) {
			Matcher formatMatcher = FORMAT_PATTERN.matcher(formatString);
			Object format = null;

			if (formatMatcher.matches()) {
				String count = formatMatcher.group(1);
				String type = formatMatcher.group(2);

				if (count.length() > 0) {
					arrayElementCount = Integer.parseInt(count);
				}

				formatString = formatMatcher.group(3);

				switch (type.charAt(0)) {
					case '&':
						if (parent != null) {
							format = ReflectUtil.newInstance(parent.getClass(),
								new Object[] { formatString }, null);
						} else {
							format = new TokenStringFormat<T>(formatString);
						}

						break;

					case 'F':
						format = formatString;
						break;

					case 'D':
						format = new SimpleDateFormat(formatString);
						break;

					case 'N':
						format = new DecimalFormat(formatString);
						break;

					case 'C':
						format = new ChoiceFormat(formatString);
						break;
				}
			}

			if (format == null) {
				throw new IllegalArgumentException(
					"Invalid format string: " + formatString);
			}

			return format;
		}

		/**
		 * Sets the format object that will be used to format input values. Can
		 * be used by subclasses to set a specific format instead of parsing it
		 * from a string.
		 *
		 * @param formatObject The new format object
		 */
		protected final void setFormatObject(Object formatObject) {
			this.formatObject = formatObject;
		}

		/**
		 * Convenience method that creates a copy of this token by invoking
		 * clone(). Subclasses that need to clone internal fields must override
		 * clone() to do so.
		 *
		 * @return The new token instance
		 */
		@SuppressWarnings("unchecked")
		final Token<T> copy() {
			try {
				return (Token<T>) clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}

		/**
		 * Sets the format string that will be used to format input values.
		 * Invokes {@link #parseFormat(String)} to parse the format string.
		 *
		 * @param formatStringString The format string to parse
		 */
		final void setFormat(String formatStringString) {
			formatObject = parseFormat(formatStringString);
		}
	}

	/**
	 * Token subclass that can retrieve properties from the input objects.
	 */
	public static class PropertyToken extends Token<Object> {

		private Method method = null;

		private Object[] args = null;

		/**
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param propertyMethod The name of the property access method
		 *                             (must be
		 *                       public)
		 */
		public PropertyToken(String propertyMethod) {
			super(propertyMethod);
		}

		/**
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param propertyMethod The name of the property access method
		 *                             (must be
		 *                       public)
		 * @param formatString   The format of the property (NULL for default)
		 * @param args           The optional arguments of the method call
		 */
		public PropertyToken(String propertyMethod, String formatString,
			Object... args) {
			this(null, propertyMethod, formatString, args);
		}

		/**
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param parent         The parent format of this token
		 * @param propertyMethod The name of the property access method
		 *                             (must be
		 *                       public)
		 * @param formatString   The format of the property (NULL for default)
		 * @param args           The optional arguments of the method call
		 */
		public PropertyToken(TokenStringFormat<?> parent,
			String propertyMethod,
			String formatString, Object... args) {
			super(parent, propertyMethod, formatString);

			this.args = args;
		}

		/**
		 * Formats a value of the input value according to this format elements
		 * definition. The default implementation invokes a method on the input
		 * value object with the name that has been defined in the constructor.
		 * Subclasses may return NULL to signal that the result shall be
		 * ignored.
		 *
		 * @param input The input value to format
		 * @return The formatted result
		 */
		@Override
		public Object extractValue(Object input) {
			if (method == null) {
				String methodName = getTokenString();
				Class<?>[] argTypes = ReflectUtil.getArgumentTypes(args, true);

				try {
					// remove the curly braces
					method = input.getClass().getMethod(methodName, argTypes);
				} catch (Exception e) {
					String err =
						"Method <" + method + "(" + Arrays.asList(argTypes) +
							")> not found in " + input.getClass();

					throw new IllegalArgumentException(err, e);
				}
			}

			return ReflectUtil.invoke(input, method, args);
		}
	}
}
