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

import java.math.BigDecimal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obrel.core.RelatedObject;


/********************************************************************
 * A generic JSON object that provides access methods for JSON datatypes.
 *
 * @author eso
 */
public class JsonObject extends RelatedObject
	implements JsonSerializable<JsonObject>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates an empty object.
	 */
	public JsonObject()
	{
	}

	/***************************************
	 * Creates a new instance with pre-set properties. It is expected (but not
	 * checked) that the given properties only contains valid JSON datatypes.
	 * Otherwise subsequent property queries will probably fail.
	 *
	 * @param rProperties The key-value pairs of the object properties
	 */
	@SafeVarargs
	public JsonObject(Pair<String, Object>... rProperties)
	{
		this(CollectionUtil.orderedMapOf(rProperties));
	}

	/***************************************
	 * Creates a new instance with pre-set properties. It is expected (but not
	 * checked) that the given properties map only contains valid JSON
	 * datatypes. Otherwise subsequent property queries will probably fail.
	 *
	 * @param rProperties A map containing the properties of this instance
	 */
	public JsonObject(Map<String, Object> rProperties)
	{
		setProperties(rProperties);
	}

	/***************************************
	 * Creates a new instance with a certain property.
	 *
	 * @param sName  The property name
	 * @param rValue The property value
	 */
	public JsonObject(String sName, Object rValue)
	{
		set(sName, rValue);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new generic JSON object from a JSON string.
	 *
	 * @param  sJson The JSON object declaration
	 *
	 * @return The new object
	 */
	public static JsonObject valueOf(String sJson)
	{
		return new JsonObject().fromJson(sJson);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder rBuilder)
	{
		rBuilder.appendObject(getProperties());
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object rOther)
	{
		if (rOther == this)
		{
			return true;
		}

		if (rOther == null || rOther.getClass() != getClass())
		{
			return false;
		}

		return relationsEqual((JsonObject) rOther);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public JsonObject fromJson(String sJson)
	{
		setProperties(new JsonParser().parseObjectMap(sJson));

		return this;
	}

	/***************************************
	 * Returns an {@link Option} containing a property value cast to a certain
	 * datatype. If the property value cannot be cast to the given datatype
	 * {@link Option#none()} will be returned.
	 *
	 * @param  sName     The property name
	 * @param  rDatatype The datatype to cast to
	 *
	 * @return The {@link BigDecimal} value
	 *
	 * @throws ClassCastException If the property is not of the given datatype
	 */
	public <T> Option<T> get(String sName, Class<? extends T> rDatatype)
	{
		return getProperty(sName).map(
			v -> Try.now(() -> rDatatype.cast(v)).orUse(null));
	}

	/***************************************
	 * Returns an {@link Option} containing an array property as a list of
	 * values. If the property doesn't exist or isn't an array an empty option
	 * will be returned.
	 *
	 * @param  sName The property name
	 *
	 * @return The JSON object option
	 */
	@SuppressWarnings("unchecked")
	public Option<List<Object>> getArray(String sName)
	{
		return getProperty(sName).map(
			v -> Try.now(() -> (List<Object>) v).orUse(null));
	}

	/***************************************
	 * Returns a property value as an integer or a default value if the property
	 * is not set or NULL. If the property value is a {@link Number} it's int
	 * value will be returned. If it is a string it will be tried to parse it
	 * into an int which will throw an exception on parsing errors. Else the
	 * default value will be returned. If the number exceeds the value range of
	 * a truncation or exceptions may occur.
	 *
	 * @param  sName    The property name
	 * @param  nDefault The default value if the property is NULL
	 *
	 * @return The integer value
	 */
	public int getInt(String sName, int nDefault)
	{
		Object rRawProperty = getProperty(sName);
		Number rValue	    =
			rRawProperty instanceof Number ? (Number) rRawProperty : null;

		return rValue != null
			   ? rValue.intValue()
			   : rRawProperty instanceof String
			   ? Integer.parseInt((String) rRawProperty) : nDefault;
	}

	/***************************************
	 * Returns a property value as a long or a default value if the property is
	 * not set or NULL. If the property value is a {@link Number} it's long
	 * value will be returned. If it is a string it will be tried to parse it
	 * into a long which will throw an exception on parsing errors. Else the
	 * default value will be returned. If the number exceeds the value range of
	 * a truncation or exceptions may occur.
	 *
	 * @param  sName    The property name
	 * @param  nDefault The default value if the property is NULL
	 *
	 * @return The long value
	 */
	public long getLong(String sName, long nDefault)
	{
		Object rRawProperty = getProperty(sName);
		Number rValue	    =
			rRawProperty instanceof Number ? (Number) rRawProperty : null;

		return rValue != null
			   ? rValue.longValue()
			   : rRawProperty instanceof String
			   ? Long.parseLong((String) rRawProperty) : nDefault;
	}

	/***************************************
	 * Returns an {@link Option} containing the value of a {@link Number}
	 * property. If the property doesn't exist or has a different datatype an
	 * empty option will be returned.
	 *
	 * @param  sName The property name
	 *
	 * @return The number option
	 */
	public Option<Number> getNumber(String sName)
	{
		return get(sName, Number.class);
	}

	/***************************************
	 * Returns an {@link Option} containing the value of a {@link JsonObject}
	 * property. If the property doesn't exist or has a different datatype an
	 * empty option will be returned.
	 *
	 * @param  sName The property name
	 *
	 * @return The JSON object option
	 */
	public Option<JsonObject> getObject(String sName)
	{
		return get(sName, JsonObject.class);
	}

	/***************************************
	 * Returns a reference to the properties map of this instance.
	 *
	 * @return The JSON properties
	 */
	public final Map<String, Object> getProperties()
	{
		return get(Json.JSON_PROPERTIES);
	}

	/***************************************
	 * Returns an option of a certain property value without cast or conversion.
	 *
	 * @param  sName The property name
	 *
	 * @return The property option
	 */
	public Option<Object> getProperty(String sName)
	{
		return Option.of(
			hasRelation(Json.JSON_PROPERTIES) ? getProperties().get(sName)
											  : null);
	}

	/***************************************
	 * Returns the names of the properties that are set in this instance.
	 *
	 * @return A set of property names
	 */
	public final Set<String> getPropertyNames()
	{
		return getProperties().keySet();
	}

	/***************************************
	 * Returns the values of the properties that are set in this instance.
	 *
	 * @return A set of property values
	 */
	public final Collection<Object> getPropertyValues()
	{
		return getProperties().values();
	}

	/***************************************
	 * Returns an {@link Option} containing the value of a string property. If
	 * the property doesn't exist or has a different datatype an empty option
	 * will be returned.
	 *
	 * @param  sName The property name
	 *
	 * @return The string option
	 */
	public Option<String> getString(String sName)
	{
		return get(sName, String.class);
	}

	/***************************************
	 * Checks whether a boolean property with a certain name has been set to
	 * TRUE on this instance.
	 *
	 * @param  sName The property name
	 *
	 * @return TRUE if the property exists, is not null, has a boolean datatype,
	 *         and is TRUE
	 */
	public boolean hasFlag(String sName)
	{
		Option<?> oValue = getProperty(sName);

		return oValue.is(Boolean.class) &&
			   oValue.map(Boolean.class::cast).orFail();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return 11 + relationsHashCode();
	}

	/***************************************
	 * Checks whether a certain property has been set in this instance.
	 *
	 * @param  sName The property name
	 *
	 * @return If the property has been set to any value (including NULL)
	 */
	public boolean hasProperty(String sName)
	{
		return hasRelation(Json.JSON_PROPERTIES) &&
			   getProperties().containsKey(sName);
	}

	/***************************************
	 * Checks whether a certain property has been set to a non-NULL value in
	 * this instance.
	 *
	 * @param  sName The property name
	 *
	 * @return If the property has been set to a non-NULL value
	 */
	public boolean hasPropertyValue(String sName)
	{
		return getProperty(sName) != null;
	}

	/***************************************
	 * Checks whether this object has any properties set.
	 *
	 * @return TRUE if no properties are set
	 */
	public boolean isEmpty()
	{
		return getProperties().isEmpty();
	}

	/***************************************
	 * Removes a property from this object. If the property doesn't exists this
	 * call has no effect.
	 *
	 * @param sName The property name
	 */
	public void remove(String sName)
	{
		getProperties().remove(sName);
	}

	/***************************************
	 * Sets a property on this object.
	 *
	 * @param sName  The property name
	 * @param rValue The property value
	 */
	public void set(String sName, Object rValue)
	{
		getProperties().put(sName, rValue);
	}

	/***************************************
	 * Sets multiple properties of this object.
	 *
	 * @param rProperties The new properties
	 */
	public void setProperties(Map<String, Object> rProperties)
	{
		if (!rProperties.isEmpty())
		{
			get(Json.JSON_PROPERTIES).putAll(rProperties);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return toJson();
	}

	/***************************************
	 * Sets a property and returns this object for fluent invocations.
	 *
	 * @param  sName  The property name
	 * @param  rValue The property value
	 *
	 * @return This object
	 */
	public JsonObject with(String sName, Object rValue)
	{
		getProperties().put(sName, rValue);

		return this;
	}
}
