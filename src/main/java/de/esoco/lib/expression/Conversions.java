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
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

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

import static org.obrel.type.MetaTypes.ELEMENT_DATATYPE;
import static org.obrel.type.MetaTypes.KEY_DATATYPE;
import static org.obrel.type.MetaTypes.ORDERED;
import static org.obrel.type.MetaTypes.VALUE_DATATYPE;

/**
 * Contains factory methods for functions that perform datatype conversions.
 * Most of these functions are invertible functions that can be used to convert
 * values in both directions.
 *
 * @author eso
 */
public class Conversions {
	private static Map<Class<?>, InvertibleFunction<?, String>>
		stringConversions;

	/**
	 * Private, only static use.
	 */
	private Conversions() {
	}

	/**
	 * Returns the string representation of a certain value as defined by the
	 * string conversion for the value's datatype. The string conversion for a
	 * certain type can be queried with {@link #getStringConversion(Class)}. If
	 * no conversion has been registered an exception will be thrown. If the
	 * value is NULL the returned string will be 'null'.
	 *
	 * <p>
	 * Collections and maps will also be converted by invoking this method
	 * recursively on the elements or keys and values. They will be separated
	 * with the default strings in
	 * {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR} and
	 * {@link TextConvert#DEFAULT_KEY_VALUE_SEPARATOR}.
	 * </p>
	 *
	 * @param o The value object to convert into a string
	 * @return A string representation of the given value
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for the value's class
	 */
	public static String asString(Object o) {
		String value;

		if (o == null) {
			value = "null";
		} else if (o instanceof Collection<?>) {
			value = asString((Collection<?>) o,
				TextConvert.DEFAULT_COLLECTION_SEPARATOR);
		} else if (o instanceof Map<?, ?>) {
			value = asString((Map<?, ?>) o,
				TextConvert.DEFAULT_COLLECTION_SEPARATOR,
				TextConvert.DEFAULT_KEY_VALUE_SEPARATOR);
		} else {
			@SuppressWarnings("unchecked")
			InvertibleFunction<Object, String> conversion =
				(InvertibleFunction<Object, String>) getStringConversion(
					o.getClass());

			if (conversion == null) {
				throw new IllegalArgumentException(
					"No string conversion registered for " + o.getClass());
			}

			value = conversion.evaluate(o);
		}

		return value;
	}

	/**
	 * Returns the string representation of a collection by converting all
	 * collection elements with the method {@link #asString(Object)}. The
	 * resulting strings will be concatenated with the given separator string.
	 *
	 * <p>
	 * It is the responsibility of the invoking code to ensure that the
	 * separator string doesn't occur in the converted collection elements. To
	 * protect against programming errors this is safeguarded by an
	 * assertion at
	 * development time.
	 * </p>
	 *
	 * @param collection The collection to convert into a string
	 * @param separator  The separator string between the collection elements
	 * @return A string representation of the given collection
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for one of the
	 *                                  collection elements
	 */
	public static String asString(Collection<?> collection, String separator) {
		StringBuilder result = new StringBuilder();

		if (collection.size() > 0) {
			for (Object element : collection) {
				result.append(
					TextConvert.unicodeEncode(asString(element), separator));
				result.append(separator);
			}

			result.setLength(result.length() - separator.length());
		}

		return result.toString();
	}

	/**
	 * Returns the string representation of a map. This is done by converting
	 * all map keys and values with the method {@link #asString(Object)} and
	 * concatenating the results with the given entry separator. These
	 * converted
	 * map entries will then be concatenated with the separator string to
	 * create
	 * the result.
	 *
	 * <p>
	 * It is the responsibility of the invoking code to ensure that neither the
	 * entry separator nor the separator string occur in the converted keys and
	 * values.To protect against programming errors this is safeguarded by an
	 * assertion at development time.
	 * </p>
	 *
	 * @param map               The map to convert into a string
	 * @param entrySeparator    The separator string between the map entries
	 * @param keyValueSeparator The separator string between the key and value
	 *                          of a map entry
	 * @return A string representation of the given map
	 * @throws IllegalArgumentException If no string conversion function has
	 *                                  been registered for one of the
	 *                                  values in
	 *                                  the map
	 */
	public static String asString(Map<?, ?> map, String entrySeparator,
		String keyValueSeparator) {
		StringBuilder result = new StringBuilder();

		if (map.size() > 0) {
			for (Entry<?, ?> entry : map.entrySet()) {
				String key = asString(entry.getKey());
				String value = asString(entry.getValue());

				assert key.indexOf(entrySeparator) < 0 &&
					key.indexOf(keyValueSeparator) < 0;

				result.append(key);
				result.append(keyValueSeparator);
				result.append(TextConvert.unicodeEncode(value,
					entrySeparator));
				result.append(entrySeparator);
			}

			result.setLength(result.length() - entrySeparator.length());
		}

		return result.toString();
	}

	/**
	 * Returns an invertible function that converts an enum value of a certain
	 * enum type into a string and vice versa.
	 *
	 * @param enumClass The class of the enum the returned function will
	 *                  convert
	 * @return An enum conversion function for the given enum type
	 */
	public static <E extends Enum<E>> InvertibleFunction<E, String> enumToString(
		final Class<E> enumClass) {
		return new StringConversion<E>(enumClass) {
			@Override
			public E invert(String enumName) {
				return Enum.valueOf(enumClass, enumName);
			}
		};
	}

	/**
	 * Returns a standard string conversion function for a certain datatype
	 * class. If no string conversion has been registered for the exact class
	 * through {@link #registerStringConversion(Class, InvertibleFunction)}
	 * this
	 * implementation will search recursively for a conversion of one of it's
	 * superclasses.
	 *
	 * @param datatype The datatype to return the conversion function for
	 * @return The invertible string conversion function or NULL if none has
	 * been registered for the given datatype or one of it's superclasses
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T, E extends Enum<E>> InvertibleFunction<T, String> getStringConversion(
		Class<T> datatype) {
		Map<Class<?>, InvertibleFunction<?, String>> conversions =
			getStringConversionMap();

		InvertibleFunction<T, String> conversion =
			(InvertibleFunction<T, String>) conversions.get(datatype);

		if (conversion == null) {
			if (datatype.isEnum()) {
				conversion = (InvertibleFunction<T, String>) enumToString(
					(Class<E>) datatype);

				conversions.put(datatype, conversion);
			} else {
				Class<? super T> superclass = datatype;

				do {
					superclass = superclass.getSuperclass();

					if (superclass != Object.class) {
						conversion =
							(InvertibleFunction<T, String>) conversions.get(
								superclass);
					} else {
						// if no conversion found for class hierarchy try a
						// default string conversion
						conversion = new StringConversion<T>(datatype);
						conversions.put(datatype, conversion);
					}
				} while (conversion == null);
			}
		}

		return conversion;
	}

	/**
	 * Internal method to access the map of standard string conversion
	 * functions
	 * and to initialize it if necessary.
	 *
	 * @return The map of string conversion functions
	 */
	private static Map<Class<?>, InvertibleFunction<?, String>> getStringConversionMap() {
		if (stringConversions == null) {
			stringConversions =
				new HashMap<Class<?>, InvertibleFunction<?, String>>();

			stringConversions.put(String.class, Functions.identity());

			stringConversions.put(Boolean.class,
				new StringConversion<Boolean>(Boolean.class) {
					@Override
					public Boolean invert(String value) {
						return Boolean.valueOf(value);
					}
				});
			stringConversions.put(Integer.class,
				new StringConversion<Integer>(Integer.class) {
					@Override
					public Integer invert(String value) {
						return Integer.valueOf(value);
					}
				});
			stringConversions.put(Long.class,
				new StringConversion<Long>(Long.class) {
					@Override
					public Long invert(String value) {
						return Long.valueOf(value);
					}
				});
			stringConversions.put(Short.class,
				new StringConversion<Short>(Short.class) {
					@Override
					public Short invert(String value) {
						return Short.valueOf(value);
					}
				});
			stringConversions.put(Float.class,
				new StringConversion<Float>(Float.class) {
					@Override
					public Float invert(String value) {
						return Float.valueOf(value);
					}
				});
			stringConversions.put(Double.class,
				new StringConversion<Double>(Double.class) {
					@Override
					public Double invert(String value) {
						return Double.valueOf(value);
					}
				});
			stringConversions.put(RelationType.class,
				new StringConversion<RelationType<?>>(RelationType.class) {
					@Override
					public RelationType<?> invert(String name) {
						return RelationType.valueOf(name);
					}
				});
			stringConversions.put(BigDecimal.class,
				new StringConversion<>(BigDecimal.class));
			stringConversions.put(BigInteger.class,
				new StringConversion<>(BigInteger.class));

			stringConversions.put(Date.class, new DateToStringConversion());
			stringConversions.put(Class.class, new ClassToStringConversion());
		}

		return stringConversions;
	}

	/**
	 * Parses a collection from a string where the collection elements are
	 * separated by {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR}.
	 *
	 * @see #parseCollection(String, Class, Class, String, boolean)
	 */
	public static <E, C extends Collection<E>> C parseCollection(
		String elements, Class<C> collectionType, Class<E> elementType,
		boolean ordered) {
		return parseCollection(elements, collectionType, elementType,
			TextConvert.DEFAULT_COLLECTION_SEPARATOR, ordered);
	}

	/**
	 * Parses a collection from a string where the collection elements are
	 * separated by {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR}.
	 *
	 * @param elements       A string containing the elements to be parsed;
	 *                          NULL
	 *                       values are allowed and will result in an empty
	 *                       collection
	 * @param collectionType The base type of the collection
	 * @param elementType    The type of the collection elements
	 * @param separator      The separator between the collection elements
	 * @param ordered        TRUE for an ordered collection
	 * @return A new collection of an appropriate type containing the parsed
	 * elements
	 */
	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> C parseCollection(
		String elements, Class<C> collectionType, Class<E> elementType,
		String separator, boolean ordered) {
		Class<? extends C> collectionClass = null;

		if (ordered) {
			Class<?> setClass = LinkedHashSet.class;

			collectionClass = (Class<? extends C>) setClass;
		} else {
			collectionClass =
				ReflectUtil.getImplementationClass(collectionType);
		}

		C collection = ReflectUtil.newInstance(collectionClass);

		if (elements != null) {
			StringTokenizer tokenizer =
				new StringTokenizer(elements, separator);

			while (tokenizer.hasMoreElements()) {
				String element =
					TextConvert.unicodeDecode(tokenizer.nextToken(),
						separator);

				collection.add(parseValue(element, elementType));
			}
		}

		return collection;
	}

	/**
	 * Parses a map from a string with the default map entry separator
	 * {@link TextConvert#DEFAULT_COLLECTION_SEPARATOR} and the default key and
	 * value separator {@link TextConvert#DEFAULT_KEY_VALUE_SEPARATOR}.
	 *
	 * @see #parseMap(String, Class, Class, Class, String, String, boolean)
	 */
	public static <K, V, M extends Map<K, V>> Map<K, V> parseMap(
		String mapEntries, Class<M> mapType, Class<K> keyType,
		Class<V> valueType, boolean ordered) {
		return parseMap(mapEntries, mapType, keyType, valueType,
			TextConvert.DEFAULT_COLLECTION_SEPARATOR,
			TextConvert.DEFAULT_KEY_VALUE_SEPARATOR, ordered);
	}

	/**
	 * Parses a map from a string.
	 *
	 * @param mapEntries        A string containing the map entries to be
	 *                          parsed; NULL values are allowed and will result
	 *                          in an empty map
	 * @param mapType           The base type of the map
	 * @param keyType           The type of the map keys
	 * @param valueType         The type of the map values
	 * @param entrySeparator    The separator string between the map entries
	 * @param keyValueSeparator The separator string between the key and value
	 *                          of a map entry
	 * @param ordered           TRUE for an ordered map
	 * @return A new map of an appropriate type containing the parsed key-value
	 * pairs
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, M extends Map<K, V>> Map<K, V> parseMap(
		String mapEntries, Class<M> mapType, Class<K> keyType,
		Class<V> valueType, String entrySeparator, String keyValueSeparator,
		boolean ordered) {
		Class<? extends M> mapClass;

		if (ordered) {
			// intermediate type necessary to prevent javac error
			Class<?> tmp = LinkedHashMap.class;
			mapClass = (Class<? extends M>) tmp;
		} else {
			mapClass = ReflectUtil.getImplementationClass(mapType);
		}

		M map = ReflectUtil.newInstance(mapClass);

		if (mapEntries != null) {
			StringTokenizer elements =
				new StringTokenizer(mapEntries, entrySeparator);

			while (elements.hasMoreElements()) {
				String entry = elements.nextToken();
				int keyEnd = entry.indexOf(keyValueSeparator);
				String key = entry.substring(0, keyEnd);
				String value =
					entry.substring(keyEnd + keyValueSeparator.length());

				value = TextConvert.unicodeDecode(value, entrySeparator);

				map.put(parseValue(key, keyType), parseValue(value,
					valueType));
			}
		}

		return map;
	}

	/**
	 * Creates an object value by parsing it's string representation as defined
	 * by the method {@link #asString(Object)}.
	 *
	 * @param value    The string value to parse
	 * @param datatype The datatype of the returned object
	 * @return The corresponding value object of the given datatype or NULL if
	 * the original object was NULL too
	 * @throws NullPointerException     If one of the parameters is NULL
	 * @throws FunctionException        If the string conversion function fails
	 * @throws IllegalArgumentException If no string conversion exists for the
	 *                                  given datatype
	 */
	public static <T> T parseValue(String value, Class<T> datatype) {
		if (datatype == null) {
			throw new NullPointerException("Datatype must not be NULL");
		}

		if (value == null || value.equals("null")) {
			return null;
		}

		InvertibleFunction<T, String> conversion =
			getStringConversion(datatype);

		if (conversion == null) {
			throw new IllegalArgumentException(
				"No string conversion registered for " + datatype);
		}

		return conversion.invert(value);
	}

	/**
	 * Parses a string value for assignment to a certain relation type. Other
	 * than {@link #parseValue(String, Class)} this method will also parse
	 * collections and maps. For this it relies on the meta information in the
	 * relation type's meta information like {@link MetaTypes#ELEMENT_DATATYPE}
	 * to determine the target datatypes. If the relation type has the flag
	 * {@link MetaTypes#ORDERED} set an ordered collection will be created.
	 *
	 * @param value      The string value to parse
	 * @param targetType The relation type that defines the target datatype of
	 *                   the conversion
	 * @return The parsed value with a datatype suitable for assigning to the
	 * relation type
	 * @throws IllegalArgumentException If parsing the string fails
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseValue(String value, RelationType<T> targetType) {
		Class<T> datatype = (Class<T>) targetType.getTargetType();
		T result;

		boolean ordered = targetType.hasFlag(ORDERED);

		if (Collection.class.isAssignableFrom(datatype)) {
			result =
				(T) parseCollection(value,
					(Class<Collection<Object>>) datatype,
					(Class<Object>) targetType.get(ELEMENT_DATATYPE), ordered);
		} else if (Map.class.isAssignableFrom(datatype)) {
			result = (T) parseMap(value, (Class<Map<Object, Object>>) datatype,
				(Class<Object>) targetType.get(KEY_DATATYPE),
				(Class<Object>) targetType.get(VALUE_DATATYPE), ordered);
		} else {
			if (datatype.isEnum() &&
				HasOrder.class.isAssignableFrom(datatype)) {
				value = value.substring(value.indexOf('-') + 1);
			}

			result = parseValue(value, datatype);
		}

		return result;
	}

	/**
	 * Registers a string conversion function for a certain datatype in the
	 * global registry. This will replace any previously registered function
	 * for
	 * the given datatype. Functions registered through this method can be
	 * accessed globally through the method
	 * {@link #getStringConversion(Class)}.
	 *
	 * <p>
	 * It is possible to register functions for a base type of certain
	 * datatypes
	 * because the lookup in {@link #getStringConversion(Class)} works
	 * recursively. In such a case the application must ensure that the
	 * registered function can also restore the correct sub-type upon inversion
	 * of the function.
	 * </p>
	 *
	 * @param datatype   The datatype to register the string conversion for
	 * @param conversion The string conversion function
	 */
	public static <T> void registerStringConversion(Class<? super T> datatype,
		InvertibleFunction<T, String> conversion) {
		getStringConversionMap().put(datatype, conversion);
	}

	// ~ Inner Classes
	// ----------------------------------------------------------

	/**
	 * An invertible function that converts a date value into a string and vice
	 * versa.
	 *
	 * @author eso
	 */
	public static class DateToStringConversion
		implements InvertibleFunction<Date, String> {
		// ~ Methods
		// ------------------------------------------------------------

		/**
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(Date date) {
			return Long.toString(date.getTime());
		}

		/**
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		public Date invert(String value) {
			return new Date(Long.parseLong(value));
		}
	}

	/**
	 * A base class for conversions that convert values into strings and vice
	 * versa. The methods have default implementations that convert values by
	 * invoking their {@link Object#toString()} method and try to restore them
	 * by invoking a constructor with a single argument of type {@link String}.
	 * If that is not sufficient subclasses can override these methods to
	 * invoke
	 * more specific methods for certain datatypes.
	 *
	 * @author eso
	 */
	public static class StringConversion<T>
		implements InvertibleFunction<T, String> {
		// ~ Static fields/initializers
		// -----------------------------------------

		private static final Class<?>[] STRING_ARG =
			new Class<?>[] { String.class };

		// ~ Instance fields
		// ----------------------------------------------------

		private final Class<? super T> datatype;

		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * Creates a new instance.
		 *
		 * @param datatype The datatype to convert to and from
		 */
		public StringConversion(Class<? super T> datatype) {
			this.datatype = datatype;
		}

		// ~ Methods
		// ------------------------------------------------------------

		/**
		 * Implemented to return the result of {@code value.toString()}. Only
		 * subclasses for which this is not appropriate need to override this
		 * method.
		 *
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(T value) {
			return value.toString();
		}

		/**
		 * Default implementation that tries to invoke a constructor of the
		 * datatype with a single argument of the type {@link String}.
		 *
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T invert(String value) {
			return (T) ReflectUtil.newInstance(datatype,
				new Object[] { value },
				STRING_ARG);
		}
	}

	/**
	 * Internal class that implements a string conversion for classes. This
	 * must
	 * use the raw type or else the registration in the string conversion map
	 * will not be possible.
	 *
	 * @author eso
	 */
	@SuppressWarnings("rawtypes")
	private static class ClassToStringConversion
		extends StringConversion<Class> {
		// ~ Constructors
		// -------------------------------------------------------

		/**
		 * Creates a new instance.
		 */
		ClassToStringConversion() {
			super(Class.class);
		}

		/**
		 * Implemented to return the result of {@link Class#getName()}.
		 *
		 * @see Function#evaluate(Object)
		 */
		@Override
		public String evaluate(Class type) {
			return type.getName();
		}

		/**
		 * Invokes {@link Class#forName(String)}.
		 *
		 * @see InvertibleFunction#invert(Object)
		 */
		@Override
		public Class<?> invert(String value) {
			try {
				return Class.forName(value);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
