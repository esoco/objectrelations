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
	 * de-serialized by invoking {@link #fromJson(String)}.
	 *
	 * @return The resulting JSON string
	 */
	public String toJson();
}
