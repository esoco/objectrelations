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

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Function;
import de.esoco.lib.expression.InvertibleFunction;
import de.esoco.lib.expression.Predicate;
import de.esoco.lib.json.Json.JsonStructure;
import de.esoco.lib.text.TextConvert;
import de.esoco.lib.text.TextConvert.IdentifierStyle;
import org.obrel.core.Relatable;
import org.obrel.core.Relation;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypeModifier;
import org.obrel.type.ListenerTypes;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.lib.json.Json.JSON_DATE_FORMAT;
import static org.obrel.type.MetaTypes.IMMUTABLE;

/**
 * A builder for JSON strings that can append arbitrary data objects to a JSON
 * string. This includes support for {@link Relatable} and {@link Relation}
 * objects. If such an object is appended either all or the given relations of
 * that object will be appended to the JSON string. This includes the recursive
 * evaluation for other relatable objects that are referenced from relations.
 *
 * @author eso
 * @see JsonParser
 * @see Json
 */
public class JsonBuilder {

	private static final ConvertJson CONVERT_JSON = new ConvertJson();

	@SuppressWarnings("boxing")
	private static final Predicate<Relation<?>> IS_NOT_TRANSIENT =
		r -> !r.getType().hasModifier(RelationTypeModifier.TRANSIENT);

	private static final Collection<RelationType<?>>
		DEFAULT_EXCLUDED_RELATION_TYPES =
		CollectionUtil.setOf(ListenerTypes.RELATION_LISTENERS,
			ListenerTypes.RELATION_TYPE_LISTENERS,
			ListenerTypes.RELATION_UPDATE_LISTENERS, IMMUTABLE);

	private final StringBuilder json = new StringBuilder();

	private final Collection<RelationType<?>> excludedRelationTypes =
		new HashSet<>(DEFAULT_EXCLUDED_RELATION_TYPES);

	private String indent = "";

	private String currentIndent = indent;

	private boolean whitespace = true;

	private boolean multiLine = true;

	private boolean recursiveRelations = false;

	private boolean namespaces = false;

	/**
	 * Creates a new instance that creates JSON without indentations.
	 */
	public JsonBuilder() {
	}

	/**
	 * Returns a function that builds a JSON string from arbitrary input
	 * objects.
	 *
	 * @param indent The indentation of generated JSON objects (empty string
	 *                 for
	 *               none)
	 * @return A function that converts objects into JSON
	 */
	public static <T> Function<T, String> buildJson(String indent) {
		return value -> new JsonBuilder()
			.indent(indent)
			.append(value)
			.toString();
	}

	/**
	 * Returns an invertible function that converts objects into JSON strings
	 * and in inverted form parses JSON strings into objects.
	 *
	 * @return An instance of {@link ConvertJson}
	 */
	public static InvertibleFunction<Object, String> convertJson() {
		return CONVERT_JSON;
	}

	/**
	 * Appends a value to a JSON string builder and converts it according to
	 * it's datatype.
	 *
	 * @param value The value to append (can be NULL)
	 * @return This instance for concatenation
	 */
	public JsonBuilder append(Object value) {
		if (value == null) {
			json.append("null");
		} else if (value instanceof JsonSerializable) {
			((JsonSerializable<?>) value).appendTo(this);
		} else if (value instanceof Boolean || value instanceof Number) {
			json.append(value);
		} else if (value instanceof Date) {
			appendString(JSON_DATE_FORMAT.format((Date) value));
		} else if (value.getClass().isArray()) {
			if (value.getClass().getComponentType().isPrimitive()) {
				int count = Array.getLength(value);
				Object[] wrappedValues = new Object[count];

				for (int i = 0; i < count; i++) {
					wrappedValues[i] = Array.get(value, i);
				}

				value = wrappedValues;
			}

			appendArray(Arrays.asList((Object[]) value));
		} else if (value instanceof Iterable) {
			appendArray((Iterable<?>) value);
		} else if (value instanceof Map) {
			appendObject((Map<?, ?>) value);
		} else if (value instanceof RelationType) {
			appendString(Json.escape(value.toString()));
		} else if (recursiveRelations && value instanceof Relatable) {
			appendRelatable((Relatable) value, null, recursiveRelations);
		} else {
			String text;

			try {
				text = Conversions.asString(value);
			} catch (Exception e) {
				// if conversion not possible use toString()
				text = value.toString();
			}

			appendString(Json.escape(text));
		}

		return this;
	}

	/**
	 * Appends a relation of a {@link Relatable} object to a JSON string
	 * builder.
	 *
	 * @param relation         The relation to append
	 * @param namingStyle      The style for converting relation type names to
	 *                         JSON properties
	 * @param appendNullValues TRUE if NULL values should be appended, FALSE if
	 *                         they should be omitted
	 * @return TRUE if a relation has been appended (can only be FALSE if
	 * appendNullValues is FALSE)
	 */
	public boolean append(Relation<?> relation, IdentifierStyle namingStyle,
		boolean appendNullValues) {
		Object value = relation.getTarget();
		boolean hasValue = (value != null || appendNullValues);

		if (hasValue) {
			RelationType<?> relationType = relation.getType();
			String name = relationType.getSimpleName();

			if (namingStyle != IdentifierStyle.UPPERCASE) {
				name = TextConvert.convertTo(namingStyle, name);
			}

			if (namespaces) {
				String namespace = relationType.getNamespace();

				if (!namespace.isEmpty()) {
					name = namespace + '.' + name;
				}
			}

			appendName(name);
			append(value);
		}

		return hasValue;
	}

	/**
	 * Appends values from an iterable object as a JSON array.
	 *
	 * @param elements The iterable object to append (may be empty or NULL)
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendArray(Iterable<?> elements) {
		json.append(JsonStructure.ARRAY.getOpenChar());

		if (elements != null) {
			Iterator<?> iterator = elements.iterator();
			boolean hasNext = iterator.hasNext();

			while (hasNext) {
				append(iterator.next());
				hasNext = iterator.hasNext();

				if (hasNext) {
					json.append(',');

					if (whitespace) {
						json.append(' ');
					}
				}
			}
		}

		json.append(JsonStructure.ARRAY.getCloseChar());

		return this;
	}

	/**
	 * Appends a JSON attribute name to a string builder. Attribute names will
	 * not be escaped because it is assumed that they do not contain JSON
	 * control characters.
	 *
	 * @param name The attribute name
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendName(String name) {
		appendString(name);
		json.append(':');

		if (whitespace) {
			json.append(' ');
		}

		return this;
	}

	/**
	 * Appends a mapping of key/value pairs to this instance as a JSON object.
	 *
	 * @param map The key/value mapping to append (may be empty or NULL)
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendObject(Map<?, ?> map) {
		beginObject();

		if (map != null && !map.isEmpty()) {
			int count = map.size();

			for (Entry<?, ?> entry : map.entrySet()) {
				appendName(entry.getKey().toString());
				append(entry.getValue());

				if (--count > 0) {
					json.append(',');
					newLine();
				}
			}
		}

		endObject();

		return this;
	}

	/**
	 * Appends the relations of a {@link Relatable} object to this JSON string
	 * as a JSON object structure. See
	 * {@link #appendRelations(Relatable, Collection)} for details about how
	 * the
	 * relations are appended.
	 *
	 * <p>The boolean parameter defines whether this method will be applied
	 * recursively to relatable objects in relation or if only their string
	 * representation will be appended. Using recursion should be used with
	 * caution as circular references can cause stack overflows.</p>
	 *
	 * @param object        The object to append the relations of
	 * @param relationTypes The types of the relation to be converted to JSON
	 *                      (NULL for all)
	 * @param recursive     TRUE to recursively append all relatables stored in
	 *                      the relations of the object
	 * @return This instance for concatenation
	 * @see #appendRelations(Relatable, Collection)
	 */
	public JsonBuilder appendRelatable(Relatable object,
		Collection<RelationType<?>> relationTypes, boolean recursive) {
		recursiveRelations = recursive;

		beginObject();
		appendRelations(object, relationTypes);
		endObject();

		return this;
	}

	/**
	 * Appends the relations of a {@link Relatable} object to this JSON string.
	 * If enabled references to other related objects are converted
	 * recursively.
	 * If no explicit relation types are provided all relations of the object
	 * will be converted to JSON. In that case it is necessary that there
	 * are no
	 * cycles in the relations, i.e. objects referring each other either
	 * directly (like in parent-child relationships) or indirectly. It is
	 * possible to exclude certain relation types from the processing by
	 * indicating them through {@link #exclude(RelationType)}.
	 *
	 * <p>Alternatively the object can specify the relation types to serialize
	 * with the annotation {@link Json#JSON_SERIALIZED_TYPES}. If that is
	 * present only the relation types stored therein will be serialized.</p>
	 *
	 * <p>Furthermore all relation values in the source object must be
	 * compatible with JSON. That means they must either have a datatype that
	 * can be converted directly or valid a string representation. The latter
	 * can be achieved by registering a global string conversion through
	 * {@link Conversions#registerStringConversion(Class, InvertibleFunction)}.
	 * If these requirements are not met the resulting JSON string will
	 * probably
	 * not be parseable by the class {@link JsonParser}.</p>
	 *
	 * @param object        The object to append the relations of
	 * @param relationTypes The types of the relation to be converted to JSON
	 *                      (NULL for all)
	 * @return This instance for concatenation
	 */
	@SuppressWarnings("boxing")
	public JsonBuilder appendRelations(Relatable object,
		Collection<RelationType<?>> relationTypes) {
		IdentifierStyle namingStyle = object.get(Json.JSON_PROPERTY_NAMING);

		if (relationTypes == null &&
			object.hasRelation(Json.JSON_SERIALIZED_TYPES)) {
			relationTypes = object.get(Json.JSON_SERIALIZED_TYPES);

			if (namingStyle == null) {
				namingStyle = IdentifierStyle.LOWER_CAMELCASE;
			}
		} else if (namingStyle == null) {
			namingStyle = IdentifierStyle.UPPERCASE;
		}

		Predicate<Relation<?>> matchesType;

		if (relationTypes != null) {
			Collection<RelationType<?>> types = relationTypes;

			matchesType = r -> types.contains(r.getType());
		} else {
			matchesType = r -> !excludedRelationTypes.contains(r.getType());
		}

		appendRelations(object.getRelations(IS_NOT_TRANSIENT.and(matchesType)),
			namingStyle, true);

		return this;
	}

	/**
	 * Appends a string value by wrapping it in the JSON string delimiters. The
	 * string will not be escaped. That need to be done separately by invoking
	 * {@link Json#escape(String)} if necessary.
	 *
	 * @param stringValue text The text string to append
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendString(String stringValue) {
		json.append(JsonStructure.STRING.getOpenChar());
		json.append(stringValue);
		json.append(JsonStructure.STRING.getCloseChar());

		return this;
	}

	/**
	 * Appends an arbitrary text. The caller is responsible that the resulting
	 * string is valid according to the JSON specification.
	 *
	 * @param text The text string to append
	 * @return This instance for concatenation
	 */
	public JsonBuilder appendText(String text) {
		json.append(text);

		return this;
	}

	/**
	 * Starts the output of a new JSON object by inserting the corresponding
	 * delimiter.
	 *
	 * @return This instance for concatenation
	 * @see #endObject()
	 */
	public JsonBuilder beginObject() {
		json.append(JsonStructure.OBJECT.getOpenChar());
		currentIndent += indent;
		newLine();

		return this;
	}

	/**
	 * Disables the addition of whitespace and linefeeds to create compact
	 * output.
	 *
	 * @return This instance for fluent invocation
	 * @see #noWhitespace()
	 * @see #noLinefeeds()
	 */
	public JsonBuilder compact() {
		return noWhitespace().noLinefeeds();
	}

	/**
	 * Ends the output of the current JSON object by inserting the
	 * corresponding
	 * delimiter.
	 *
	 * @return This instance for concatenation
	 * @see #beginObject()
	 */
	public JsonBuilder endObject() {
		currentIndent = currentIndent.substring(0,
			currentIndent.length() - indent.length());
		newLine();
		json.append(JsonStructure.OBJECT.getCloseChar());

		return this;
	}

	/**
	 * Excludes a certain relation type from the relation-based conversions in
	 * this class. A relation type added through this method will be ignored by
	 * {@link JsonBuilder#appendRelations(Relatable, Collection)} if this
	 * method
	 * is invoked without an explicit list of relation types. This class
	 * already
	 * contains a default list of relation types that either have no meaningful
	 * JSON representation or would prevent the JSON generation. Appending a
	 * single single relation explicitly is not affected by this setting.
	 *
	 * @param excludedType The relation type to be excluded from the JSON
	 *                     generation
	 * @return This instance for concatenation
	 */
	public JsonBuilder exclude(RelationType<?> excludedType) {
		excludedRelationTypes.add(excludedType);

		return this;
	}

	/**
	 * Sets the indent of this builder.
	 *
	 * @param indent The indentation string to be prefixed per level
	 * @return This instance for concatenation
	 */
	public JsonBuilder indent(String indent) {
		this.indent = indent;

		return this;
	}

	/**
	 * Returns the current length of the JSON string.
	 *
	 * @return The JSON string length
	 */
	public int length() {
		return json.length();
	}

	/**
	 * Disables the adding of linefeeds to the generated JSON.
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder noLinefeeds() {
		this.multiLine = false;

		return this;
	}

	/**
	 * Disables the adding of whitespace to the generated JSON.
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder noWhitespace() {
		this.whitespace = false;

		return this;
	}

	/**
	 * Converts a value into a JSON string.
	 *
	 * @param value The value to convert
	 * @return The JSON string
	 */
	public String toJson(Object value) {
		JsonBuilder jsonBuilder = new JsonBuilder();

		if (value instanceof JsonSerializable) {
			((JsonSerializable<?>) value).appendTo(jsonBuilder);
		} else if (value instanceof Relatable) {
			jsonBuilder.appendRelatable((Relatable) value, null, false);
		} else {
			jsonBuilder.append(value);
		}

		return jsonBuilder.toString();
	}

	/**
	 * Returns the current JSON string representation of this instance.
	 *
	 * @return The JSON string
	 */
	@Override
	public String toString() {
		return json.toString();
	}

	/**
	 * Enables the addition of namespaces to the names of properties created
	 * from relation types. The namespace will always be lower case names
	 * separated by dots, independent of the {@link IdentifierStyle} used for
	 * the actual property name.
	 *
	 * <p>By default this option is disabled.</p>
	 *
	 * @return This instance for fluent invocation
	 */
	public JsonBuilder withNamespaces() {
		this.namespaces = true;

		return this;
	}

	/**
	 * Appends a collection of relations to this JSON string.
	 *
	 * @param relations        The relations to append
	 * @param namingStyle      The style for converting relation type names to
	 *                         JSON properties
	 * @param appendNullValues TRUE if NULL values should be appended, FALSE if
	 *                         they should be omitted
	 * @return This instance for concatenation
	 */
	private JsonBuilder appendRelations(Collection<Relation<?>> relations,
		IdentifierStyle namingStyle, boolean appendNullValues) {
		int count = relations.size();

		for (Relation<?> relation : relations) {
			append(relation, namingStyle, appendNullValues);

			if (--count > 0) {
				json.append(',');
				newLine();
			}
		}

		return this;
	}

	/**
	 * Appends a line break to the current JSON string to start a new line.
	 *
	 * @return This instance for concatenation
	 */
	private JsonBuilder newLine() {
		if (multiLine) {
			json.append('\n');
			json.append(currentIndent);
		}

		return this;
	}

	/**
	 * An invertible function to convert objects to ({@link #evaluate(Object)})
	 * and from ({@link #invert(String)} JSON. Can be subclassed to extend the
	 * base functionality.
	 *
	 * @author eso
	 */
	public static class ConvertJson
		implements InvertibleFunction<Object, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String evaluate(Object value) {
			return new JsonBuilder().append(value).toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object invert(String json) {
			return new JsonParser().parse(json);
		}
	}
}
