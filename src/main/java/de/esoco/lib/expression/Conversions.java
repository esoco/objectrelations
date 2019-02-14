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

import de.esoco.lib.property.HasOrder;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.KEY_DATATYPE;
import static org.obrel.type.MetaTypes.ORDERED;
import static org.obrel.type.MetaTypes.VALUE_DATATYPE;


/********************************************************************
 * Contains factory methods for functions that perform datatype conversions.
 * Most of these functions are invertible functions that can be used to convert
 * values in both directions.
 *
 * @author eso
 */
public class Conversions
{
	//~ Static fields/initializers ---------------------------------------------

	private static Map<Class<?>, InvertibleFunction<?, String>> aStringConversions;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private Conversions()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the string representation of a certain value as defined by the
	 * string conversion for the value's datatype. The string conversion for a
	 * certain type can be queried with {@link #getStringConversion(Class)}. If
	 * no conversion has been registered an exception will be thrown. If the
	 * value is NULL the returned string will be 'null'.
	 *
	 * <p>Collections and maps will also be converted by invoking this method
	 * recursively on the elements or keys and values. They will be separated
	 * with the default strings in {@link
	 * TextConvert#DEFAULT_COLLECTION_SEPARATOR} and {@link
	 * TextConvert#DEFAULT_KEY_VALUE_SEPARATOR}.</p>
	 *
	 * @param  rValue The value to convert into a string
	 *
	 * @return A string representation of the given value
	 *
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for the value's class
	 */
	public static String asString(Object rValue)
	{
		String sValue;

		if (rValue == null)
		{
			sValue = "null";
		}
		else if (rValue instanceof Collection<?>)
		{
			sValue =
				asString(
					(Collection<?>) rValue,
					TextConvert.DEFAULT_COLLECTION_SEPARATOR);
		}
		else if (rValue instanceof Map<?, ?>)
		{
			sValue =
				asString(
					(Map<?, ?>) rValue,
					TextConvert.DEFAULT_COLLECTION_SEPARATOR,
					TextConvert.DEFAULT_KEY_VALUE_SEPARATOR);
		}
		else
		{
			@SuppressWarnings("unchecked")
			InvertibleFunction<Object, String> rConversion =
				(InvertibleFunction<Object, String>)
				getStringConversion(rValue.getClass());

			if (rConversion == null)
			{
				throw new IllegalArgumentException(
					"No string conversion registered for " +
					rValue.getClass());
			}

			sValue = rConversion.evaluate(rValue);
		}

		return sValue;
	}

	/***************************************
	 * Returns the string representation of a collection by converting all
	 * collection elements with the method {@link #asString(Object)}. The
	 * resulting strings will be concatenated with the given separator string.
	 *
	 * <p>It is the responsibility of the invoking code to ensure that the
	 * separator string doesn't occur in the converted collection elements. To
	 * protect against programming errors this is safeguarded by an assertion at
	 * development time.</p>
	 *
	 * @param  rCollection The collection to convert into a string
	 * @param  sSeparator  The separator string between the collection elements
	 *
	 * @return A string representation of the given collection
	 *
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for one of the
	 *                                  collection elements
	 */
	public static String asString(Collection<?> rCollection, String sSeparator)
	{
		StringBuilder aResult = new StringBuilder();

		if (rCollection.size() > 0)
		{
			for (Object rElement : rCollection)
			{
				String sElement = asString(rElement);

				aResult.append(TextConvert.unicodeEncode(sElement, sSeparator));
				aResult.append(sSeparator);
			}

			aResult.setLength(aResult.length() - sSeparator.length());
		}

		return aResult.toString();
	}

	/***************************************
	 * Returns the string representation of a map. This is done by converting
	 * all map keys and values with the method {@link #asString(Object)} and
	 * concatenating the results with the given entry separator. These converted
	 * map entries will then be concatenated with the separator string to create
	 * the result.
	 *
	 * <p>It is the responsibility of the invoking code to ensure that neither
	 * the entry separator nor the separator string occur in the converted keys
	 * and values.To protect against programming errors this is safeguarded by
	 * an assertion at development time.</p>
	 *
	 * @param  rMap               The map to convert into a string
	 * @param  sEntrySeparator    The separator string between the map entries
	 * @param  sKeyValueSeparator The separator string between the key and value
	 *                            of a map entry
	 *
	 * @return A string representation of the given map
	 *
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for one of the values in
	 *                                  the map
	 */
	public static String asString(Map<?, ?> rMap,
								  String    sEntrySeparator,
								  String    sKeyValueSeparator)
	{
		StringBuilder aResult = new StringBuilder();

		if (rMap.size() > 0)
		{
			for (Entry<?, ?> rEntry : rMap.entrySet())
			{
				String sKey   = asString(rEntry.getKey());
				String sValue = asString(rEntry.getValue());

				assert sKey.indexOf(sEntrySeparator) < 0 &&
					   sKey.indexOf(sKeyValueSeparator) < 0;

				aResult.append(sKey);
				aResult.append(sKeyValueSeparator);
				aResult.append(
					TextConvert.unicodeEncode(sValue, sEntrySeparator));
				aResult.append(sEntrySeparator);
			}

			aResult.setLength(aResult.length() - sEntrySeparator.length());
		}

		return aResult.toString();
	}

	/***************************************
	 * Returns an invertible function that converts an enum value of a certain
	 * enum type into a string and vice versa.
	 *
	 * @param  rEnumClass The class of the enum the returned function will
	 *                    convert
	 *
	 * @return An enum conversion function for the given enum type
	 */
	public static <E extends Enum<E>> InvertibleFunction<E, String>
	enumToString(final Class<E> rEnumClass)
	{
		return new StringConversion<E>(rEnumClass)
		{
			@Override
			public E invert(String sEnumName)
			{
				return Enum.valueOf(rEnumClass, sEnumName);
			}
		};
	}

	/***************************************
	 * Returns a standard string conversion function for a certain datatype
	 * class. If no string conversion has been registered for the exact class
	 * through {@link #registerStringConversion(Class, InvertibleFunction)} this
	 * implementation will search recursively for a conversion of one of it's
	 * superclasses.
	 *
	 * @param  rDatatype The datatype to return the conversion function for
	 *
	 * @return The invertible string conversion function or NULL if none has
	 *         been registered for the given datatype or one of it's
	 *         superclasses
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T, E extends Enum<E>> InvertibleFunction<T, String>
	getStringConversion(Class<T> rDatatype)
	{
		Map<Class<?>, InvertibleFunction<?, String>> rConversions =
			getStringConversionMap();

		InvertibleFunction<T, String> rConversion =
			(InvertibleFunction<T, String>) rConversions.get(rDatatype);

		if (rConversion == null)
		{
			if (rDatatype.isEnum())
			{
				rConversion =
					(InvertibleFunction<T, String>)
					enumToString((Class<E>) rDatatype);

				rConversions.put(rDatatype, rConversion);
			}
			else
			{
				Class<? super T> rSuperclass = rDatatype;

				do
				{
					rSuperclass = rSuperclass.getSuperclass();

					if (rSuperclass != Object.class)
					{
						rConversion =
							(InvertibleFunction<T, String>) rConversions.get(
								rSuperclass);
					}
					else
					{
						// if no conversion found for class hierarchy try a
						// default string conversion
						rConversion = new StringConversion<T>(rDatatype);
						rConversions.put(rDatatype, rConversion);
					}
				}
				while (rConversion == null);
			}
		}

		return rConversion;
	}

	/***************************************
	 * Parses a collection from a string where the collection elements are
	 * separated by {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR}.
	 *
	 * @see #parseCollection(String, Class, Class, String, boolean)
	 */
	public static <E, C extends Collection<E>> C parseCollection(
		String   sElements,
		Class<C> rCollectionType,
		Class<E> rElementType,
		boolean  bOrdered)
	{
		return parseCollection(
			sElements,
			rCollectionType,
			rElementType,
			TextConvert.DEFAULT_COLLECTION_SEPARATOR,
			bOrdered);
	}

	/***************************************
	 * Parses a collection from a string where the collection elements are
	 * separated by {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR}.
	 *
	 * @param  sElements       A string containing the elements to be parsed;
	 *                         NULL values are allowed and will result in an
	 *                         empty collection
	 * @param  rCollectionType The base type of the collection
	 * @param  rElementType    The type of the collection elements
	 * @param  sSeparator      The separator between the collection elements
	 * @param  bOrdered        TRUE for an ordered collection
	 *
	 * @return A new collection of an appropriate type containing the parsed
	 *         elements
	 */
	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> C parseCollection(
		String   sElements,
		Class<C> rCollectionType,
		Class<E> rElementType,
		String   sSeparator,
		boolean  bOrdered)
	{
		Class<? extends C> rCollectionClass = null;

		if (bOrdered)
		{
			Class<?> rSetClass = LinkedHashSet.class;

			rCollectionClass = (Class<? extends C>) rSetClass;
		}
		else
		{
			rCollectionClass =
				ReflectUtil.getImplementationClass(rCollectionType);
		}

		C aCollection = ReflectUtil.newInstance(rCollectionClass);

		if (sElements != null)
		{
			StringTokenizer aElements =
				new StringTokenizer(sElements, sSeparator);

			while (aElements.hasMoreElements())
			{
				String sElement =
					TextConvert.unicodeDecode(
						aElements.nextToken(),
						sSeparator);

				aCollection.add(parseValue(sElement, rElementType));
			}
		}

		return aCollection;
	}

	/***************************************
	 * Parses a map from a string with the default map entry separator {@link
	 * TextConvert#DEFAULT_COLLECTION_SEPARATOR} and the default key and value
	 * separator {@link TextConvert#DEFAULT_KEY_VALUE_SEPARATOR}.
	 *
	 * @see #parseMap(String, Class, Class, Class, String, String, boolean)
	 */
	public static <K, V, M extends Map<K, V>> Map<K, V> parseMap(
		String   sMapEntries,
		Class<M> rMapType,
		Class<K> rKeyType,
		Class<V> rValueType,
		boolean  bOrdered)
	{
		return parseMap(
			sMapEntries,
			rMapType,
			rKeyType,
			rValueType,
			TextConvert.DEFAULT_COLLECTION_SEPARATOR,
			TextConvert.DEFAULT_KEY_VALUE_SEPARATOR,
			bOrdered);
	}

	/***************************************
	 * Parses a map from a string.
	 *
	 * @param  sMapEntries        A string containing the map entries to be
	 *                            parsed; NULL values are allowed and will
	 *                            result in an empty map
	 * @param  rMapType           The base type of the map
	 * @param  rKeyType           The type of the map keys
	 * @param  rValueType         The type of the map values
	 * @param  sEntrySeparator    The separator string between the map entries
	 * @param  sKeyValueSeparator The separator string between the key and value
	 *                            of a map entry
	 * @param  bOrdered           TRUE for an ordered map
	 *
	 * @return A new map of an appropriate type containing the parsed key-value
	 *         pairs
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, M extends Map<K, V>> Map<K, V> parseMap(
		String   sMapEntries,
		Class<M> rMapType,
		Class<K> rKeyType,
		Class<V> rValueType,
		String   sEntrySeparator,
		String   sKeyValueSeparator,
		boolean  bOrdered)
	{
		Class<? extends M> rMapClass;

		if (bOrdered)
		{
			// double cast necessary for JDK javac
			rMapClass = (Class<? extends M>) (Class<?>) LinkedHashMap.class;
		}
		else
		{
			rMapClass = ReflectUtil.getImplementationClass(rMapType);
		}

		M aMap = ReflectUtil.newInstance(rMapClass);

		if (sMapEntries != null)
		{
			StringTokenizer aElements =
				new StringTokenizer(sMapEntries, sEntrySeparator);

			while (aElements.hasMoreElements())
			{
				String sEntry  = aElements.nextToken();
				int    nKeyEnd = sEntry.indexOf(sKeyValueSeparator);
				String sKey    = sEntry.substring(0, nKeyEnd);
				String sValue  =
					sEntry.substring(nKeyEnd + sKeyValueSeparator.length());

				sValue = TextConvert.unicodeDecode(sValue, sEntrySeparator);

				aMap.put(
					parseValue(sKey, rKeyType),
					parseValue(sValue, rValueType));
			}
		}

		return aMap;
	}

	/***************************************
	 * Creates an object value by parsing it's string representation as defined
	 * by the method {@link #asString(Object)}.
	 *
	 * @param  sValue    The string value to parse
	 * @param  rDatatype The datatype of the returned object
	 *
	 * @return The corresponding value object of the given datatype or NULL if
	 *         the original object was NULL too
	 *
	 * @throws NullPointerException     If one of the parameters is NULL
	 * @throws FunctionException        If the string conversion function fails
	 * @throws IllegalArgumentException If no string conversion exists for the
	 *                                  given datatype
	 */
	public static <T> T parseValue(String sValue, Class<T> rDatatype)
	{
		if (rDatatype == null)
		{
			throw new NullPointerException("Datatype must not be NULL");
		}

		if (sValue == null || sValue.equals("null"))
		{
			return null;
		}

		InvertibleFunction<T, String> rConversion =
			getStringConversion(rDatatype);

		if (rConversion == null)
		{
			throw new IllegalArgumentException(
				"No string conversion registered for " +
				rDatatype);
		}

		return rConversion.invert(sValue);
	}

	/***************************************
	 * Parses a string value for assignment to a certain relation type. Other
	 * than {@link #parseValue(String, Class)} this method will also parse
	 * collections and maps. For this it relies on the meta information in the
	 * relation type's meta information like {@link MetaTypes#ELEMENT_DATATYPE}
	 * to determine the target datatypes. If the relation type has the flag
	 * {@link MetaTypes#ORDERED} set an ordered collection will be created.
	 *
	 * @param  sValue      The string value to parse
	 * @param  rTargetType The relation type that defines the target datatype of
	 *                     the conversion
	 *
	 * @return The parsed value with a datatype suitable for assigning to the
	 *         relation type
	 *
	 * @throws IllegalArgumentException If parsing the string fails
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseValue(String sValue, RelationType<T> rTargetType)
	{
		Class<T> rDatatype = (Class<T>) rTargetType.getTargetType();
		T		 aResult;

		boolean bOrdered = rTargetType.hasFlag(ORDERED);

		if (Collection.class.isAssignableFrom(rDatatype))
		{
			aResult =
				(T) parseCollection(
					sValue,
					(Class<Collection<Object>>) rDatatype,
					(Class<Object>) rTargetType.get(ELEMENT_DATATYPE),
					bOrdered);
		}
		else if (Map.class.isAssignableFrom(rDatatype))
		{
			aResult =
				(T) parseMap(
					sValue,
					(Class<Map<Object, Object>>) rDatatype,
					(Class<Object>) rTargetType.get(KEY_DATATYPE),
					(Class<Object>) rTargetType.get(VALUE_DATATYPE),
					bOrdered);
		}
		else
		{
			if (rDatatype.isEnum() &&
				HasOrder.class.isAssignableFrom(rDatatype))
			{
				sValue = sValue.substring(sValue.indexOf('-') + 1);
			}

			aResult = parseValue(sValue, rDatatype);
		}

		return aResult;
	}

	/***************************************
	 * Registers a string conversion function for a certain datatype in the
	 * global registry. This will replace any previously registered function for
	 * the given datatype. Functions registered through this method can be
	 * accessed globally through the method {@link #getStringConversion(Class)}.
	 *
	 * <p>It is possible to register functions for a base type of certain
	 * datatypes because the lookup in {@link #getStringConversion(Class)} works
	 * recursively. In such a case the application must ensure that the
	 * registered function can also restore the correct sub-type upon inversion
	 * of the function.</p>
	 *
	 * @param rDatatype   The datatype to register the string conversion for
	 * @param rConversion The string conversion function
	 */
	public static <T> void registerStringConversion(
		Class<? super T>			  rDatatype,
		InvertibleFunction<T, String> rConversion)
	{
		getStringConversionMap().put(rDatatype, rConversion);
	}

	/***************************************
	 * Internal method to access the map of standard string conversion functions
	 * and to initialize it if necessary.
	 *
	 * @return The map of string conversion functions
	 */
	private static Map<Class<?>, InvertibleFunction<?, String>>
	getStringConversionMap()
	{
		if (aStringConversions == null)
		{
			aStringConversions =
				new HashMap<Class<?>, InvertibleFunction<?, String>>();

			aStringConversions.put(String.class, Functions.<String>identity());

			aStringConversions.put(
				Boolean.class,
				new StringConversion<Boolean>(Boolean.class)
				{
					@Override
					public Boolean invert(String sValue)
					{
						return Boolean.valueOf(sValue);
					}
				});
			aStringConversions.put(
				Integer.class,
				new StringConversion<Integer>(Integer.class)
				{
					@Override
					public Integer invert(String sValue)
					{
						return Integer.valueOf(sValue);
					}
				});
			aStringConversions.put(
				Long.class,
				new StringConversion<Long>(Long.class)
				{
					@Override
					public Long invert(String sValue)
					{
						return Long.valueOf(sValue);
					}
				});
			aStringConversions.put(
				Short.class,
				new StringConversion<Short>(Short.class)
				{
					@Override
					public Short invert(String sValue)
					{
						return Short.valueOf(sValue);
					}
				});
			aStringConversions.put(
				Float.class,
				new StringConversion<Float>(Float.class)
				{
					@Override
					public Float invert(String sValue)
					{
						return Float.valueOf(sValue);
					}
				});
			aStringConversions.put(
				Double.class,
				new StringConversion<Double>(Double.class)
				{
					@Override
					public Double invert(String sValue)
					{
						return Double.valueOf(sValue);
					}
				});
			aStringConversions.put(
				RelationType.class,
				new StringConversion<RelationType<?>>(RelationType.class)
				{
					@Override
					public RelationType<?> invert(String sName)
					{
						return RelationType.valueOf(sName);
					}
				});
			aStringConversions.put(
				BigDecimal.class,
				new StringConversion<>(BigDecimal.class));
			aStringConversions.put(
				BigInteger.class,
				new StringConversion<>(BigInteger.class));

			aStringConversions.put(Date.class, new DateToStringConversion());
			aStringConversions.put(Class.class, new ClassToStringConversion());
		}

		return aStringConversions;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An invertible function that converts a date value into a string and vice
	 * versa.
	 *
	 * @author eso
	 */
	public static class DateToStringConversion
		implements InvertibleFunction<Date, String>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(Date rDate)
		{
			return Long.toString(rDate.getTime());
		}

		/***************************************
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		public Date invert(String sValue)
		{
			return new Date(Long.parseLong(sValue));
		}
	}

	/********************************************************************
	 * A base class for conversions that convert values into strings and vice
	 * versa. The methods have default implementations that convert values by
	 * invoking their {@link Object#toString()} method and try to restore them
	 * by invoking a constructor with a single argument of type {@link String}.
	 * If that is not sufficient subclasses can override these methods to invoke
	 * more specific methods for certain datatypes.
	 *
	 * @author eso
	 */
	public static class StringConversion<T>
		implements InvertibleFunction<T, String>
	{
		//~ Static fields/initializers -----------------------------------------

		private static final Class<?>[] STRING_ARG =
			new Class<?>[] { String.class };

		//~ Instance fields ----------------------------------------------------

		private final Class<? super T> rDatatype;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rDatatype The datatype to convert to and from
		 */
		public StringConversion(Class<? super T> rDatatype)
		{
			this.rDatatype = rDatatype;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Implemented to return the result of {@code rValue.toString()}. Only
		 * subclasses for which this is not appropriate need to override this
		 * method.
		 *
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(T rValue)
		{
			return rValue.toString();
		}

		/***************************************
		 * Default implementation that tries to invoke a constructor of the
		 * datatype with a single argument of the type {@link String}.
		 *
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T invert(String sValue)
		{
			return (T) ReflectUtil.newInstance(
				rDatatype,
				new Object[] { sValue },
				STRING_ARG);
		}
	}

	/********************************************************************
	 * Internal class that implements a string conversion for classes. This must
	 * use the raw type or else the registration in the string conversion map
	 * will not be possible.
	 *
	 * @author eso
	 */
	@SuppressWarnings("rawtypes")
	private static class ClassToStringConversion extends StringConversion<Class>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 */
		ClassToStringConversion()
		{
			super(Class.class);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Implemented to return the result of {@link Class#getName()}.
		 *
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(Class rClass)
		{
			return rClass.getName();
		}

		/***************************************
		 * Invokes {@link Class#forName(String)}.
		 *
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		public Class<?> invert(String sValue)
		{
			try
			{
				return Class.forName(sValue);
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalStateException(e);
			}
		}
	}
}
