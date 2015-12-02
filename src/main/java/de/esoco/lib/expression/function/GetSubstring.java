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

/********************************************************************
 * A function implementation that returns a substring of an input string.
 *
 * @author eso
 */
public class GetSubstring extends AbstractFunction<String, String>
{
	//~ Instance fields --------------------------------------------------------

	private final int nBeginIndex;
	private final int nEndIndex;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param nBeginIndex The index of the first char of the substring
	 * @param nEndIndex   The index of the char after the last char of the
	 *                    substring or -1 for the end of the string
	 */
	public GetSubstring(int nBeginIndex, int nEndIndex)
	{
		super(GetSubstring.class.getSimpleName());

		this.nBeginIndex = nBeginIndex;
		this.nEndIndex   = nEndIndex;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see AbstractFunction#evaluate(Object)
	 */
	@Override
	public String evaluate(String sValue)
	{
		return nEndIndex != -1 ? sValue.substring(nBeginIndex, nEndIndex)
							   : sValue.substring(nBeginIndex);
	}

	/***************************************
	 * Returns the index of the first character of substrings.
	 *
	 * @return The first char index
	 */
	public final int getBeginIndex()
	{
		return nBeginIndex;
	}

	/***************************************
	 * Returns the index of the first character after the last character of
	 * substrings. Will be -1 if the end ist the end of the string.
	 *
	 * @return The last char index
	 */
	public final int getEndIndex()
	{
		return nEndIndex;
	}
}
