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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/********************************************************************
 * Extracts a string from a char sequence based on a regular expression. The
 * evaluation applies a regular expression pattern to the input char sequence
 * and invokes the method {@link Matcher#find()}. If the pattern is found it
 * returns either the content of the first regular expression group or, if the
 * pattern contains no groups, the full sequence matched by the pattern. If the
 * pattern doesn't match NULL will be returned.
 *
 * @author eso
 */
public class ExtractString
	extends AbstractBinaryFunction<String, Pattern, String>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain regular expression pattern string.
	 *
	 * @see ExtractString#ExtractString(Pattern)
	 */
	public ExtractString(String sRegEx)
	{
		this(Pattern.compile(sRegEx));
	}

	/***************************************
	 * Creates a new instance for a certain regular expression pattern.
	 *
	 * @param  rPattern The regular expression pattern
	 *
	 * @throws NullPointerException If the pattern is NULL
	 */
	public ExtractString(Pattern rPattern)
	{
		super(rPattern, ExtractString.class.getSimpleName());

		if (rPattern == null)
		{
			throw new NullPointerException("Pattern must not be NULL");
		}
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Matches the regular expression pattern against the input sequence and
	 * invokes the method {@link Matcher#find()}. If the pattern is found this
	 * method returns either the content of the first regular expression group
	 * or, if the pattern contains no groups, the full sequence matched by the
	 * pattern. If the pattern doesn't match NULL will be returned.
	 *
	 * @param  sInput   The input value to be evaluated
	 * @param  rPattern The regular expression pattern
	 *
	 * @return The matching char sequence or NULL if not found
	 */
	@Override
	public String evaluate(String sInput, Pattern rPattern)
	{
		Matcher m = rPattern.matcher(sInput);

		if (m.find())
		{
			if (m.groupCount() >= 1)
			{
				return m.group(1);
			}
			else
			{
				return m.group();
			}
		}
		else
		{
			return null;
		}
	}
}
