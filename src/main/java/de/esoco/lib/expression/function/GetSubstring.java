//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'ObjectRelations' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.function;

/**
 * A function implementation that returns a substring of an input string.
 *
 * @author eso
 */
public class GetSubstring extends AbstractFunction<String, String> {

	private final int beginIndex;

	private final int endIndex;

	/**
	 * Creates a new instance.
	 *
	 * @param beginIndex The index of the first char of the substring
	 * @param endIndex   The index of the char after the last char of the
	 *                   substring or -1 for the end of the string
	 */
	public GetSubstring(int beginIndex, int endIndex) {
		super(GetSubstring.class.getSimpleName());

		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	/**
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public String evaluate(String value) {
		return endIndex != -1 ?
		       value.substring(beginIndex, endIndex) :
		       value.substring(beginIndex);
	}

	/**
	 * Returns the index of the first character of substrings.
	 *
	 * @return The first char index
	 */
	public final int getBeginIndex() {
		return beginIndex;
	}

	/**
	 * Returns the index of the first character after the last character of
	 * substrings. Will be -1 if the end ist the end of the string.
	 *
	 * @return The last char index
	 */
	public final int getEndIndex() {
		return endIndex;
	}
}
