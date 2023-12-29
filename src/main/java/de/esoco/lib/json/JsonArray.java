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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * An extension of {@link ArrayList} that also implements {@link
 * JsonSerializable} so that it can be used directly in JSON expressions. The
 * implementation does not validate the datatype of added elements. It is the
 * responsibility of the application to ensure that only valid JSON values are
 * contained.
 *
 * @author eso
 */
public class JsonArray extends ArrayList<Object>
	implements JsonSerializable<JsonArray> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public JsonArray() {
	}

	/**
	 * Creates a new instance.
	 *
	 * @see ArrayList#ArrayList(int)
	 */
	public JsonArray(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a new instance.
	 *
	 * @see ArrayList#ArrayList(Collection)
	 */
	public JsonArray(Collection<?> initialElements) {
		super(initialElements);
	}

	/**
	 * Factory method to create a new array from initial elements. The returned
	 * instance is mutable.
	 *
	 * @param elements The initial elements of the array
	 * @return The new JSON array
	 */
	public static JsonArray of(Object... elements) {
		return new JsonArray(Arrays.asList(elements));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder builder) {
		builder.appendArray(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonArray fromJson(String json) {
		new JsonParser().parseArray(json, this);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return toJson();
	}
}
