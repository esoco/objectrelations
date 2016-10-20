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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/********************************************************************
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
public class TokenStringFormat<T> extends AbstractFunction<T, String>
{
	//~ Static fields/initializers ---------------------------------------------

	private static Map<String, Token<Object>> aTokenRegistry =
		new HashMap<String, Token<Object>>();

	static
	{
		registerToken("now",
					  new Token<Object>("now")
		  			{
		  				@Override
		  				protected Object extractValue(Object ignore)
		  				{
		  					return new Date();
		  				}
		  			});
		registerToken("#",
			new Token<Object>("#")
			{
				@Override
				protected Object extractValue(Object rInput)
				{
					return rInput;
				}
			});
	}

	static final Pattern PROPERTY_METHOD_PATTERN =
		Pattern.compile("(\\w+?)\\((.*)\\)");
	static final Pattern FORMAT_PATTERN			 =
		Pattern.compile("(?s)(\\d*)([&FDNC])(.+)");

	//~ Instance fields --------------------------------------------------------

	private String				   sTargetPattern;
	private List<Token<? super T>> aTokens = new ArrayList<Token<? super T>>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain token string.
	 *
	 * @param  sTokenString The token string
	 *
	 * @throws IllegalArgumentException If the string contains invalid tokens
	 */
	public TokenStringFormat(String sTokenString)
	{
		// use empty string as toString() is overridden
		super("");
		parseTokenString(sTokenString);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Convenience method to format a value according to a certain token string.
	 *
	 * @param  sTokenString The token string containing the format description
	 * @param  rValue       The value to format
	 *
	 * @return The formatted string
	 *
	 * @throws IllegalArgumentException If parsing or formatting fails
	 */
	public static String format(String sTokenString, Object rValue)
	{
		return new TokenStringFormat<Object>(sTokenString).evaluate(rValue);
	}

	/***************************************
	 * Registers a new global token that will be applied by all format
	 * instances.
	 *
	 * @param sToken The token string to register the token for
	 * @param rToken The token to apply
	 */
	public static void registerToken(String sToken, Token<Object> rToken)
	{
		aTokenRegistry.put(sToken, rToken);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Transforms the input value into a string by applying this instance's
	 * token string pattern to it.
	 *
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public String evaluate(T rInput)
	{
		StringBuilder sb = new StringBuilder(sTargetPattern);

		for (Token<? super T> rToken : aTokens)
		{
			String sToken = rToken.toString();
			String sElem  = rToken.transform(rInput);
			int    nStart = sb.indexOf(sToken);
			int    nEnd   = nStart + sToken.length();

			sb.replace(nStart, nEnd, sElem != null ? sElem : "");
		}

		return sb.toString();
	}

	/***************************************
	 * Returns a string describing this instance.
	 *
	 * @return A string description of this instance
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + sTargetPattern + "](" +
			   INPUT_PLACEHOLDER + ")";
	}

	/***************************************
	 * Returns a registered token instance for a certain string representation
	 * of the token. This method must be overridden by subclasses that define
	 * own tokens.
	 *
	 * @param  sToken The token's string representation (without curly braces)
	 *
	 * @return The corresponding token or NULL if none has been registered
	 */
	protected Token<? super T> getToken(String sToken)
	{
		return aTokenRegistry.get(sToken);
	}

	/***************************************
	 * @see AbstractFunction#paramsEqual(AbstractFunction)
	 */
	@Override
	protected boolean paramsEqual(AbstractFunction<?, ?> rOther)
	{
		TokenStringFormat<?> rOtherFunction = (TokenStringFormat<?>) rOther;

		return sTargetPattern.equals(rOtherFunction.sTargetPattern) &&
			   aTokens.equals(rOtherFunction.aTokens);
	}

	/***************************************
	 * @see AbstractFunction#paramsHashCode()
	 */
	@Override
	protected int paramsHashCode()
	{
		return 31 * sTargetPattern.hashCode() + aTokens.hashCode();
	}

	/***************************************
	 * Parses a single token that has been read from the token string.
	 * Subclasses may override this to implement their own parse algorithm.
	 *
	 * @param  sToken       The token's string
	 * @param  sTokenFormat The token's format
	 *
	 * @return A new token instance
	 *
	 * @throws IllegalArgumentException If the arguments cannot be parsed
	 */
	protected Token<? super T> parseToken(String sToken, String sTokenFormat)
	{
		Matcher aMethodMatcher = PROPERTY_METHOD_PATTERN.matcher(sToken);

		Token<? super T> rToken = null;

		if (aMethodMatcher.matches())
		{
			String   sMethod    = aMethodMatcher.group(1);
			String   sArguments = aMethodMatcher.group(2);
			Object[] aArgs	    = null;

			if (sArguments.length() > 0)
			{
				String[] aArgStrings = sArguments.split(",");

				aArgs = new Object[aArgStrings.length];

				for (int i = 0; i < aArgs.length; i++)
				{
					aArgs[i] = TextUtil.parseObject(aArgStrings[i]);
				}
			}

			rToken = new PropertyToken(this, sMethod, null, aArgs);
		}
		else
		{
			rToken = getToken(sToken);
		}

		if (rToken == null)
		{
			throw new IllegalArgumentException("Invalid token: " + sToken);
		}

		if (sTokenFormat != null)
		{
			if (sTokenFormat.length() > 0)
			{
				rToken = rToken.copy();
				rToken.setFormat(sTokenFormat);
			}
			else
			{
				throw new IllegalArgumentException("Empty format string");
			}
		}

		return rToken;
	}

	/***************************************
	 * Parse's the token string and generates the internal data structures that
	 * are needed for the transformation.
	 *
	 * @param  sTokenString The token string to parse
	 *
	 * @throws IllegalArgumentException If the token string cannot be parsed
	 */
	private void parseTokenString(String sTokenString)
	{
		StringBuilder sb		 = new StringBuilder();
		int			  nLength    = sTokenString.length();
		int			  nTextStart = 0;
		int			  nStart     = 0;
		int			  nEnd		 = 0;

		while ((nStart = sTokenString.indexOf('{', nStart)) >= 0)
		{
			String sToken;
			String sTokenFormat = null;
			int    nSubCount    = 0;
			int    nFormatPos   = -1;
			char   c;

			nEnd = ++nStart;

			while (nEnd < nLength &&
				   ((c = sTokenString.charAt(nEnd)) != '}' || nSubCount > 0))
			{
				// search terminating '}' but skip sub-pairs of braces '{...}'
				switch (c)
				{
					case '{':
						nSubCount++;
						break;

					case '}':
						nSubCount--;
						break;

					case ':':

						// remember first format separator
						if (nFormatPos == -1)
						{
							nFormatPos = nEnd;
						}

						break;
				}

				nEnd++;
			}

			if (nEnd == nLength || nFormatPos == nEnd)
			{
				throw new IllegalArgumentException("Incomplete token: " +
												   sTokenString);
			}

			if (nFormatPos > 0)
			{
				sToken		 = sTokenString.substring(nStart, nFormatPos);
				sTokenFormat = sTokenString.substring(nFormatPos + 1, nEnd);
			}
			else
			{
				sToken = sTokenString.substring(nStart, nEnd);
			}

			Token<? super T> rToken = parseToken(sToken, sTokenFormat);

			aTokens.add(rToken);

			sb.append(sTokenString.substring(nTextStart, nStart - 1));
			sb.append(rToken.toString());

			nTextStart = nStart = nEnd + 1;
		}

		sb.append(sTokenString.substring(nTextStart));
		sTargetPattern = sb.toString();
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * Represents a single token in the token string and the associated
	 * formatting code.
	 */
	public static abstract class Token<T> implements Cloneable
	{
		//~ Instance fields ----------------------------------------------------

		private TokenStringFormat<?> rParent;

		private String sToken;

		private Object rFormatObject	  = null;
		private int    nArrayElementCount = -1;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new global token that has no parent.
		 *
		 * @param sToken The token string
		 */
		protected Token(String sToken)
		{
			this(sToken, null);
		}

		/***************************************
		 * Creates a new global token without a parent and with a certain
		 * format. The format string argument will be parsed with the method
		 * {@link #parseFormat(String)}.
		 *
		 * @param sToken  The string description of the token
		 * @param sFormat The format string for this token
		 */
		protected Token(String sToken, String sFormat)
		{
			this(null, sToken, sFormat);
		}

		/***************************************
		 * Creates a new token with a certain parent token string format and
		 * format string. The format string argument will be parsed with the
		 * method {@link #parseFormat(String)}. Tokens of subclassed formats
		 * should always be created with this constructor to allow the correct
		 * creation of nested formats.
		 *
		 * @param rParent The parent format of this token
		 * @param sToken  The string description of the token
		 * @param sFormat The format string for this token
		 */
		protected Token(TokenStringFormat<?> rParent,
						String				 sToken,
						String				 sFormat)
		{
			this.rParent = rParent;
			this.sToken  = sToken;

			if (sFormat != null)
			{
				this.rFormatObject = parseFormat(sFormat);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the format object that will be used to format input values.
		 *
		 * @return The format object
		 */
		public final Object getFormatObject()
		{
			return rFormatObject;
		}

		/***************************************
		 * Returns the token string of this token.
		 *
		 * @return The token string
		 */
		public final String getTokenString()
		{
			return sToken;
		}

		/***************************************
		 * Returns the token's string representation. This is the token's string
		 * description inside curly braces ('{token}').
		 *
		 * @return The string representation of the token
		 */
		@Override
		public String toString()
		{
			return "{" + sToken + "}";
		}

		/***************************************
		 * Transforms the input value into a string that can be inserted into
		 * the resulting string of a string token transformation. Returns the
		 * result of the {@link #applyFormat(Object)} method that has been
		 * invoked on the return value of the {@link #extractValue(Object)}
		 * method. If the input value is a collection or an array the format
		 * will be applied to each element in the array and the resulting
		 * strings will be concatenated.
		 *
		 * @param  rInput The input value of the token string transformation
		 *
		 * @return The result of transforming this token into a string derived
		 *         from the input value
		 */
		public final String transform(T rInput)
		{
			Object rValue  = extractValue(rInput);
			String sResult;

			if (!(rFormatObject instanceof TokenStringFormat<?>) &&
				(rValue.getClass().isArray() ||
				 rValue instanceof Collection<?>))
			{
				if (rValue instanceof Collection<?>)
				{
					rValue = ((Collection<?>) rValue).toArray();
				}

				StringBuilder aBuilder = new StringBuilder();
				int			  nCount   = Array.getLength(rValue);

				if (nArrayElementCount > 0)
				{
					nCount = Math.min(nCount, nArrayElementCount);
				}

				for (int i = 0; i < nCount; i++)
				{
					aBuilder.append(applyFormat(Array.get(rValue, i)));

					if (rFormatObject == null)
					{
						aBuilder.append(',');
					}
				}

				if (rFormatObject == null && nCount > 0)
				{
					aBuilder.setLength(aBuilder.length() - 1);
				}

				sResult = aBuilder.toString();
			}
			else
			{
				sResult = applyFormat(rValue);
			}

			return sResult;
		}

		/***************************************
		 * Extracts the value from the input object that corresponds to this
		 * token and returns a string representation of that value.
		 *
		 * @param  rInput The input object to retrieve the value from
		 *
		 * @return A string derived from the input value
		 */
		protected abstract Object extractValue(T rInput);

		/***************************************
		 * Subclasses can override this method to apply a specific format to the
		 * value that has been returned by {@link #extractValue(Object)}. The
		 * default implementation handles the following types of format objects:
		 *
		 * <ul>
		 *   <li>String: returns String.format((String) Format, rValue)</li>
		 *   <li>TokenStringFormat: returns Format.transform(rValue)</li>
		 *   <li>java.text.Format: returns Format.format(rValue)</li>
		 *   <li>Other types or NULL: returns rValue.toString()</li>
		 * </ul>
		 *
		 * @param  rValue The value that has been extracted from the input
		 *
		 * @return A string containing the formatted string representing the
		 *         input value
		 */
		@SuppressWarnings("unchecked")
		protected String applyFormat(Object rValue)
		{
			if (rFormatObject instanceof String)
			{
				return String.format((String) rFormatObject, rValue);
			}
			else if (rFormatObject instanceof TokenStringFormat<?>)
			{
				return ((TokenStringFormat<Object>) rFormatObject).evaluate(rValue);
			}
			else if (rFormatObject instanceof Format)
			{
				return ((Format) rFormatObject).format(rValue);
			}
			else if (rValue != null)
			{
				return rValue.toString();
			}
			else
			{
				return "null";
			}
		}

		/***************************************
		 * Can be overridden by subclasses that support different formats to
		 * implement the parsing of format strings. The default implementation
		 * provides support for several format types as described in the main
		 * class documentation.
		 *
		 * @param  sFormat The format string (must not be NULL)
		 *
		 * @return The object representing the parsed format
		 *
		 * @throws IllegalArgumentException If the argument doesn't contain a
		 *                                  recognized format or parsing it
		 *                                  fails
		 */
		protected Object parseFormat(String sFormat)
		{
			Matcher aFormatMatcher = FORMAT_PATTERN.matcher(sFormat);
			Object  rFormat		   = null;

			if (aFormatMatcher.matches())
			{
				String sCount = aFormatMatcher.group(1);
				String sType  = aFormatMatcher.group(2);

				if (sCount.length() > 0)
				{
					nArrayElementCount = Integer.parseInt(sCount);
				}

				sFormat = aFormatMatcher.group(3);

				switch (sType.charAt(0))
				{
					case '&':
						if (rParent != null)
						{
							rFormat =
								ReflectUtil.newInstance(rParent.getClass(),
														new Object[]
														{
															sFormat
														},
														null);
						}
						else
						{
							rFormat = new TokenStringFormat<T>(sFormat);
						}

						break;

					case 'F':
						rFormat = sFormat;
						break;

					case 'D':
						rFormat = new SimpleDateFormat(sFormat);
						break;

					case 'N':
						rFormat = new DecimalFormat(sFormat);
						break;

					case 'C':
						rFormat = new ChoiceFormat(sFormat);
						break;
				}
			}

			if (rFormat == null)
			{
				throw new IllegalArgumentException("Invalid format string: " +
												   sFormat);
			}

			return rFormat;
		}

		/***************************************
		 * Sets the format object that will be used to format input values. Can
		 * be used by subclasses to set a specific format instead of parsing it
		 * from a string.
		 *
		 * @param rFormatObject The new format object
		 */
		protected final void setFormatObject(Object rFormatObject)
		{
			this.rFormatObject = rFormatObject;
		}

		/***************************************
		 * Convenience method that creates a copy of this token by invoking
		 * clone(). Subclasses that need to clone internal fields must override
		 * clone() to do so.
		 *
		 * @return The new token instance
		 */
		@SuppressWarnings("unchecked")
		final Token<T> copy()
		{
			try
			{
				return (Token<T>) clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new AssertionError();
			}
		}

		/***************************************
		 * Sets the format string that will be used to format input values.
		 * Invokes {@link #parseFormat(String)} to parse the format string.
		 *
		 * @param sFormatString The format string to parse
		 */
		final void setFormat(String sFormatString)
		{
			rFormatObject = parseFormat(sFormatString);
		}
	}

	/********************************************************************
	 * Token subclass that can retrieve properties from the input objects.
	 */
	public static class PropertyToken extends Token<Object>
	{
		//~ Instance fields ----------------------------------------------------

		private Method   rMethod = null;
		private Object[] rArgs   = null;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param sPropertyMethod The name of the property access method (must
		 *                        be public)
		 */
		public PropertyToken(String sPropertyMethod)
		{
			super(sPropertyMethod);
		}

		/***************************************
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param sPropertyMethod The name of the property access method (must
		 *                        be public)
		 * @param sFormat         The format of the property (NULL for default)
		 * @param rArgs           The optional arguments of the method call
		 */
		public PropertyToken(String    sPropertyMethod,
							 String    sFormat,
							 Object... rArgs)
		{
			this(null, sPropertyMethod, sFormat, rArgs);
		}

		/***************************************
		 * Constructor for a token that will retrieve a certain property from
		 * the input value by invoking a no-argument public access method on it
		 * through reflection. The property method argument must contain the
		 * full name of the method (e.g. 'getValue'). It will also be used as
		 * the token string of this token.
		 *
		 * @param rParent         The parent format of this token
		 * @param sPropertyMethod The name of the property access method (must
		 *                        be public)
		 * @param sFormat         The format of the property (NULL for default)
		 * @param rArgs           The optional arguments of the method call
		 */
		public PropertyToken(TokenStringFormat<?> rParent,
							 String				  sPropertyMethod,
							 String				  sFormat,
							 Object... 			  rArgs)
		{
			super(rParent, sPropertyMethod, sFormat);

			this.rArgs = rArgs;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Formats a value of the input value according to this format elements
		 * definition. The default implementation invokes a method on the input
		 * value object with the name that has been defined in the constructor.
		 * Subclasses may return NULL to signal that the result shall be
		 * ignored.
		 *
		 * @param  rInput The input value to format
		 *
		 * @return The formatted result
		 */
		@Override
		public Object extractValue(Object rInput)
		{
			if (rMethod == null)
			{
				String     sMethod   = getTokenString();
				Class<?>[] aArgTypes =
					ReflectUtil.getArgumentTypes(rArgs, true);

				try
				{
					// remove the curly braces
					rMethod = rInput.getClass().getMethod(sMethod, aArgTypes);
				}
				catch (Exception e)
				{
					String sErr =
						"Method <" + sMethod + "(" + Arrays.asList(aArgTypes) +
						")> not found in " + rInput.getClass();

					throw new IllegalArgumentException(sErr, e);
				}
			}

			return ReflectUtil.invoke(rInput, rMethod, rArgs);
		}
	}
}
