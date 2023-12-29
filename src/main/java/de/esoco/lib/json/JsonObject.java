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
package de.esoco.lib.json;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.datatype.Pair;
import de.esoco.lib.expression.monad.Option;
import de.esoco.lib.expression.monad.Try;
import org.obrel.core.RelatedObject;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A generic JSON object that provides access methods for JSON datatypes.
 *
 * @author eso
 */
public class JsonObject extends RelatedObject
	implements JsonSerializable<JsonObject> {

	/**
	 * Creates an empty object.
	 */
	public JsonObject() {
	}

	/**
	 * Creates a new instance with pre-set properties. It is expected (but not
	 * checked) that the given properties only contains valid JSON datatypes.
	 * Otherwise subsequent property queries will probably fail.
	 *
	 * @param properties The key-value pairs of the object properties
	 */
	@SafeVarargs
	public JsonObject(Pair<String, Object>... properties) {
		this(CollectionUtil.orderedMapOf(properties));
	}

	/**
	 * Creates a new instance with pre-set properties. It is expected (but not
	 * checked) that the given properties map only contains valid JSON
	 * datatypes. Otherwise subsequent property queries will probably fail.
	 *
	 * @param properties A map containing the properties of this instance
	 */
	public JsonObject(Map<String, Object> properties) {
		setProperties(properties);
	}

	/**
	 * Creates a new instance with a certain property.
	 *
	 * @param name  The property name
	 * @param value The property value
	 */
	public JsonObject(String name, Object value) {
		set(name, value);
	}

	/**
	 * Creates a new generic JSON object from a JSON string.
	 *
	 * @param json The JSON object declaration
	 * @return The new object
	 */
	public static JsonObject valueOf(String json) {
		return new JsonObject().fromJson(json);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder builder) {
		builder.appendObject(getProperties());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other == null || other.getClass() != getClass()) {
			return false;
		}

		return relationsEqual((JsonObject) other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonObject fromJson(String json) {
		setProperties(new JsonParser().parseObjectMap(json));

		return this;
	}

	/**
	 * Returns an {@link Option} containing a property value cast to a certain
	 * datatype. If the property value cannot be cast to the given datatype
	 * {@link Option#none()} will be returned.
	 *
	 * @param name     The property name
	 * @param datatype The datatype to cast to
	 * @return The {@link BigDecimal} value
	 * @throws ClassCastException If the property is not of the given datatype
	 */
	public <T> Option<T> get(String name, Class<? extends T> datatype) {
		return getProperty(name).map(
			v -> Try.now(() -> datatype.cast(v)).orUse(null));
	}

	/**
	 * Returns an {@link Option} containing an array property as a list of
	 * values. If the property doesn't exist or isn't an array an empty option
	 * will be returned.
	 *
	 * @param name The property name
	 * @return The JSON object option
	 */
	@SuppressWarnings("unchecked")
	public Option<List<Object>> getArray(String name) {
		return getProperty(name).map(
			v -> Try.now(() -> (List<Object>) v).orUse(null));
	}

	/**
	 * Returns a property value as an integer or a default value if the
	 * property
	 * is not set or NULL. If the property value is a {@link Number} it's int
	 * value will be returned. If it is a string it will be tried to parse it
	 * into an int which will throw an exception on parsing errors. Else the
	 * default value will be returned. If the number exceeds the value range of
	 * a truncation or exceptions may occur.
	 *
	 * @param name         The property name
	 * @param defaultValue The default value if the property is NULL
	 * @return The integer value
	 */
	public int getInt(String name, int defaultValue) {
		Object rawProperty = getProperty(name);
		Number value =
			rawProperty instanceof Number ? (Number) rawProperty : null;

		return value != null ?
		       value.intValue() :
		       rawProperty instanceof String ?
		       Integer.parseInt((String) rawProperty) :
		       defaultValue;
	}

	/**
	 * Returns a property value as a long or a default value if the property is
	 * not set or NULL. If the property value is a {@link Number} it's long
	 * value will be returned. If it is a string it will be tried to parse it
	 * into a long which will throw an exception on parsing errors. Else the
	 * default value will be returned. If the number exceeds the value range of
	 * a truncation or exceptions may occur.
	 *
	 * @param name         The property name
	 * @param defaultValue The default value if the property is NULL
	 * @return The long value
	 */
	public long getLong(String name, long defaultValue) {
		Object rawProperty = getProperty(name);
		Number value =
			rawProperty instanceof Number ? (Number) rawProperty : null;

		return value != null ?
		       value.longValue() :
		       rawProperty instanceof String ?
		       Long.parseLong((String) rawProperty) :
		       defaultValue;
	}

	/**
	 * Returns an {@link Option} containing the value of a {@link Number}
	 * property. If the property doesn't exist or has a different datatype an
	 * empty option will be returned.
	 *
	 * @param name The property name
	 * @return The number option
	 */
	public Option<Number> getNumber(String name) {
		return get(name, Number.class);
	}

	/**
	 * Returns an {@link Option} containing the value of a {@link JsonObject}
	 * property. If the property doesn't exist or has a different datatype an
	 * empty option will be returned.
	 *
	 * @param name The property name
	 * @return The JSON object option
	 */
	public Option<JsonObject> getObject(String name) {
		return get(name, JsonObject.class);
	}

	/**
	 * Returns a reference to the properties map of this instance.
	 *
	 * @return The JSON properties
	 */
	public final Map<String, Object> getProperties() {
		return get(Json.JSON_PROPERTIES);
	}

	/**
	 * Returns an option of a certain property value without cast or
	 * conversion.
	 *
	 * @param name The property name
	 * @return The property option
	 */
	public Option<Object> getProperty(String name) {
		return Option.of(hasRelation(Json.JSON_PROPERTIES) ?
		                 getProperties().get(name) :
		                 null);
	}

	/**
	 * Returns the names of the properties that are set in this instance.
	 *
	 * @return A set of property names
	 */
	public final Set<String> getPropertyNames() {
		return getProperties().keySet();
	}

	/**
	 * Returns the values of the properties that are set in this instance.
	 *
	 * @return A set of property values
	 */
	public final Collection<Object> getPropertyValues() {
		return getProperties().values();
	}

	/**
	 * Returns an {@link Option} containing the value of a string property. If
	 * the property doesn't exist or has a different datatype an empty option
	 * will be returned.
	 *
	 * @param name The property name
	 * @return The string option
	 */
	public Option<String> getString(String name) {
		return get(name, String.class);
	}

	/**
	 * Checks whether a boolean property with a certain name has been set to
	 * TRUE on this instance.
	 *
	 * @param name The property name
	 * @return TRUE if the property exists, is not null, has a boolean datatype
	 * and is TRUE
	 */
	public boolean hasFlag(String name) {
		Option<?> value = getProperty(name);

		return value.is(Boolean.class) &&
			value.map(Boolean.class::cast).orFail();
	}

	/**
	 * Checks whether a certain property has been set in this instance.
	 *
	 * @param name The property name
	 * @return If the property has been set to any value (including NULL)
	 */
	public boolean hasProperty(String name) {
		return hasRelation(Json.JSON_PROPERTIES) &&
			getProperties().containsKey(name);
	}

	/**
	 * Checks whether a certain property has been set to a non-NULL value in
	 * this instance.
	 *
	 * @param name The property name
	 * @return If the property has been set to a non-NULL value
	 */
	public boolean hasPropertyValue(String name) {
		return getProperty(name) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 11 + relationsHashCode();
	}

	/**
	 * Checks whether this object has any properties set.
	 *
	 * @return TRUE if no properties are set
	 */
	public boolean isEmpty() {
		return getProperties().isEmpty();
	}

	/**
	 * Removes a property from this object. If the property doesn't exists this
	 * call has no effect.
	 *
	 * @param name The property name
	 */
	public void remove(String name) {
		getProperties().remove(name);
	}

	/**
	 * Sets a property on this object.
	 *
	 * @param name  The property name
	 * @param value The property value
	 */
	public void set(String name, Object value) {
		getProperties().put(name, value);
	}

	/**
	 * Sets multiple properties of this object.
	 *
	 * @param properties The new properties
	 */
	public void setProperties(Map<String, Object> properties) {
		if (!properties.isEmpty()) {
			get(Json.JSON_PROPERTIES).putAll(properties);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return toJson();
	}

	/**
	 * Sets a property and returns this object for fluent invocations.
	 *
	 * @param name  The property name
	 * @param value The property value
	 * @return This object
	 */
	public JsonObject with(String name, Object value) {
		getProperties().put(name, value);

		return this;
	}
}
