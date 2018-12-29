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
public interface JsonSerializable<J extends JsonSerializable<J>>
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
	 * Parses data from a JSON string and returns the resulting instance.
	 * Invocations should always work with the returned instance as immutable
	 * implementations will return a new instance.
	 *
	 * <p>All implementing types should provide a no-argument constructor that
	 * does as few initialization as possible. This applies especially to
	 * immutable types that will only be created to invoke this method, which
	 * will then create and fill a new instance.</p>
	 *
	 * @param  sJson The JSON string to parse
	 *
	 * @return This instance so that it can be directly used after
	 *         de-serialization
	 */
	public J fromJson(String sJson);

	/***************************************
	 * Converts this instance into a compact JSON representation that can be
	 * de-serialized by invoking {@link #fromJson(String)}.
	 *
	 * @return The resulting JSON string
	 */
	default public String toCompactJson()
	{
		return new JsonBuilder().compact().append(this).toString();
	}

	/***************************************
	 * Converts this instance into a JSON representation that can be
	 * de-serialized by invoking {@link #fromJson(String)}. This default
	 * implementation creates a human-readable, multi-line JSON string where
	 * hierarchy levels are indented by tabulator characters. The method {@link
	 * #toCompactJson()} creates a single-line JSON string that doesn't contain
	 * fill characters.
	 *
	 * <p>The default implementation creates a new {@link JsonBuilder} instance
	 * and invokes {@link JsonBuilder#append(Object)}. This will in turn invoke
	 * the abstract method {@link #appendTo(JsonBuilder)} which must be
	 * implemented to perform the actual JSON conversion.</p>
	 *
	 * @return The resulting JSON string
	 */
	default public String toJson()
	{
		return new JsonBuilder().indent("\t").append(this).toString();
	}
}
