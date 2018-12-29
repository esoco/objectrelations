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


/********************************************************************
 * An extension of {@link ArrayList} that also implements {@link
 * JsonSerializable} so that it can be used directly in JSON expressions. The
 * implementation does not validate the datatype of added elements. It is the
 * responsibility of the application to ensure that only valid JSON values are
 * contained.
 *
 * @author eso
 */
public class JsonArray extends ArrayList<Object>
	implements JsonSerializable<JsonArray>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public JsonArray()
	{
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @see ArrayList#ArrayList(int)
	 */
	public JsonArray(int nInitialCapacity)
	{
		super(nInitialCapacity);
	}

	/***************************************
	 * Creates a new instance.
	 *
	 * @see ArrayList#ArrayList(Collection)
	 */
	public JsonArray(Collection<?> rInitialElements)
	{
		super(rInitialElements);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method to create a new array from initial elements. The returned
	 * instance is mutable.
	 *
	 * @param  rElements The initial elements of the array
	 *
	 * @return The new JSON array
	 */
	public static JsonArray of(Object... rElements)
	{
		return new JsonArray(Arrays.asList(rElements));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void appendTo(JsonBuilder rBuilder)
	{
		rBuilder.appendArray(this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public JsonArray fromJson(String sJson)
	{
		new JsonParser().parseArray(sJson, this);

		return this;
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
