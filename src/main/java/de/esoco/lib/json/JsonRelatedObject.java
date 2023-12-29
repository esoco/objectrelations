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

/**
 * A {@link Relatable} implementation that has direct support for serialization
 * to and from JSON.
 *
 * @author eso
 */
public class JsonRelatedObject<J extends JsonRelatedObject<J>>
	extends RelatedObject implements JsonSerializable<J> {

	/**
	 * Creates a new instance that will convert all relation types to JSON that
	 * have been declared in a subclass of this class.
	 */
	public JsonRelatedObject() {
		set(Json.JSON_SERIALIZED_TYPES, ObjectRelations
			.getRelatable(getClass())
			.get(MetaTypes.DECLARED_RELATION_TYPES));
	}

	/**
	 * Creates a new instance that only serializes certain relation types.
	 *
	 * @param jsonTypes The relation types to serialize into JSON format
	 */
	public JsonRelatedObject(RelationType<?>... jsonTypes) {
		set(Json.JSON_SERIALIZED_TYPES,
			CollectionUtil.orderedSetOf(jsonTypes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder jsonBuilder) {
		jsonBuilder.appendRelatable(this, null, true);
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

		return relationsEqual((JsonRelatedObject<?>) other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public J fromJson(String json) {
		new JsonParser().parseRelatable(json, this);

		return (J) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 17 + relationsHashCode();
	}
}
