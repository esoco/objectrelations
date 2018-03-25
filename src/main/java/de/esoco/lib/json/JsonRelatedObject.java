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

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;


/********************************************************************
 * A {@link Relatable} implementation that has direct support for serialization
 * to and from JSON.
 *
 * @author eso
 */
public class JsonRelatedObject<J extends JsonRelatedObject<J>>
	extends RelatedObject implements JsonSerializable<J>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance that will convert all relation types to JSON that
	 * have been declared in a subclass of this class.
	 */
	public JsonRelatedObject()
	{
		set(Json.JSON_SERIALIZED_TYPES,
			ObjectRelations.getRelatable(getClass())
			.get(MetaTypes.DECLARED_RELATION_TYPES));
	}

	/***************************************
	 * Creates a new instance that only serializes certain relation types.
	 *
	 * @param rJsonTypes The relation types to serialize into JSON format
	 */
	public JsonRelatedObject(RelationType<?>... rJsonTypes)
	{
		set(Json.JSON_SERIALIZED_TYPES,
			CollectionUtil.orderedSetOf(rJsonTypes));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder rJsonBuilder)
	{
		rJsonBuilder.appendRelatable(this, null, true);
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

		return relationsEqual((JsonRelatedObject<?>) rOther);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public J fromJson(String sJson)
	{
		new JsonParser().parseRelatable(sJson, this);

		return (J) this;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return 17 + relationsHashCode();
	}
}
