//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;


/********************************************************************
 * A {@link Relatable} implementation that has direct support for serialization
 * to and from JSON.
 *
 * @author eso
 */
public class JsonObject<T extends JsonObject<T>> extends RelatedObject
	implements JsonSerializable<T>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public JsonObject()
	{
	}

	/***************************************
	 * Creates a new instance that only serializes certain relation types.
	 *
	 * @param rJsonTypes The relation types to serialize into JSON format
	 */
	public JsonObject(RelationType<?>... rJsonTypes)
	{
		set(JsonBuilder.JSON_SERIALIZED_TYPES,
			CollectionUtil.setOf(rJsonTypes));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T fromJson(String sJson)
	{
		new JsonParser().parseRelatable(sJson, this);

		return (T) this;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toJson()
	{
		return new JsonBuilder().appendRelatable(this, null, true).toString();
	}
}
