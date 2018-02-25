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

/********************************************************************
 * An interface for objects that can be serialized to and from the JSON data
 * format. It is recommended that implementations provide a no-argument
 * constructor to allow automatic de-serialization by {@link JsonParser}.
 *
 * @author eso
 */
public interface JsonSerializable<T extends JsonSerializable<T>>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Appends this instance to a JSON builder. This method needs to be
	 * implemented to allow the serialization of hierarchical structures. To
	 * simply serialize a (root-level) object use the default method {@link
	 * #toJson()} instead.
	 *
	 * @param rBuilder The {@link JsonBuilder} to append this instance to
	 */
	public void appendTo(JsonBuilder rBuilder);

	/***************************************
	 * Parses the contents of this instance from a JSON string.
	 *
	 * @param  sJson The JSON string to parse
	 *
	 * @return This instance so that it can be directly used after
	 *         de-serialization
	 */
	public T fromJson(String sJson);

	/***************************************
	 * Converts this instance into a JSON representation that can be
	 * de-serialized by invoking {@link #fromJson(String)}. This default
	 * implementation creates a multi-line JSON string where hierarchy levels
	 * are indented by tabulator characters. To use a custom format the method
	 * {@link #appendTo(JsonBuilder)} should be invoked with a correspondingly
	 * initialized {@link JsonBuilder}.
	 *
	 * @return The resulting JSON string
	 */
	default public String toJson()
	{
		return new JsonBuilder().indent("\t").append(this).toString();
	}
}
