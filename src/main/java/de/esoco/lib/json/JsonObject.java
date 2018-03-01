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
import de.esoco.lib.datatype.Pair;

import java.math.BigDecimal;

import java.util.Collection;
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
	 * @param rObjectProperties The JSON object properties of this instance
	 */
	@SafeVarargs
	public JsonObject(Pair<String, Object>... rObjectProperties)
	{
		this(CollectionUtil.orderedMapOf(rObjectProperties));
	}

	/***************************************
	 * Creates a new instance with pre-set properties. It is expected (but not
	 * checked) that the given properties map only contains valid JSON
	 * datatypes. Otherwise subsequent property queries will probably fail.
	 *
	 * @param rObjectProperties The JSON object properties of this instance
	 */
	public JsonObject(Map<String, Object> rObjectProperties)
	{
		if (!rObjectProperties.isEmpty())
		{
			get(Json.JSON_OBJECT_DATA).putAll(rObjectProperties);
		}
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new instance from a JSON string.
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
		Map<String, Object> rProperties =
			new JsonParser().parseObjectMap(sJson);

		if (!rProperties.isEmpty())
		{
			set(Json.JSON_OBJECT_DATA, rProperties);
		}

		return this;
	}

	/***************************************
	 * Returns a property value as a certain datatype or a default value if the
	 * property is not set or NULL. If the property value cannot be cast to the
	 * generic datatype a {@link ClassCastException} will be thrown.
	 *
	 * @param  sName    The property name
	 * @param  rDefault The default value if the property is NULL
	 *
	 * @return The {@link BigDecimal} value
	 *
	 * @throws ClassCastException If the property is not a {@link BigDecimal}
	 */
	public <T> T get(String sName, T rDefault)
	{
		@SuppressWarnings("unchecked")
		T rValue = (T) getRawProperty(sName);

		return rValue != null ? rValue : rDefault;
	}

	/***************************************
	 * Returns a property value as an integer or a default value if the property
	 * is not set or NULL. If the property value cannot be cast to {@link
	 * Number} an exception will be thrown. If the value range of the number
	 * exceed that of an integer truncation may occur.
	 *
	 * @param  sName    The property name
	 * @param  nDefault The default value if the property is NULL
	 *
	 * @return The integer value
	 *
	 * @throws ClassCastException If the property is not a number
	 */
	public int getInt(String sName, int nDefault)
	{
		Number rValue = (Number) getRawProperty(sName);

		return rValue != null ? rValue.intValue() : nDefault;
	}

	/***************************************
	 * Returns a property value as a long or a default value if the property is
	 * not set or NULL. If the property value cannot be cast to {@link Number}
	 * an exception will be thrown. If the value range of the number exceed that
	 * of a long truncation may occur.
	 *
	 * @param  sName    The property name
	 * @param  nDefault The default value if the property is NULL
	 *
	 * @return The long value
	 *
	 * @throws ClassCastException If the property is not a number
	 */
	public long getLong(String sName, long nDefault)
	{
		Number rValue = (Number) getRawProperty(sName);

		return rValue != null ? rValue.longValue() : nDefault;
	}

	/***************************************
	 * Returns a reference to the properties map of this instance.
	 *
	 * @return The JSON properties
	 */
	public final Map<String, Object> getProperties()
	{
		return get(Json.JSON_OBJECT_DATA);
	}

	/***************************************
	 * Returns the names of the properties in this instance.
	 *
	 * @return A set of property names
	 */
	public final Set<String> getPropertyNames()
	{
		return getProperties().keySet();
	}

	/***************************************
	 * Returns the values of the properties in this instance.
	 *
	 * @return A set of property values
	 */
	public final Collection<Object> getPropertyValues()
	{
		return getProperties().values();
	}

	/***************************************
	 * Returns a raw property value without cast or conversion.
	 *
	 * @param  sName The property name
	 *
	 * @return The property value
	 */
	public Object getRawProperty(String sName)
	{
		return hasRelation(Json.JSON_OBJECT_DATA) ? getProperties().get(sName)
												  : null;
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
		Object rValue = getRawProperty(sName);

		return rValue != null && rValue instanceof Boolean &&
			   ((Boolean) rValue).booleanValue() == true;
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
		return hasRelation(Json.JSON_OBJECT_DATA) &&
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
		return getRawProperty(sName) != null;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return toJson();
	}
}
