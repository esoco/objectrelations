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
package de.esoco.lib.expression;

import junit.framework.TestCase;

import de.esoco.lib.expression.function.TokenStringFormat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;


/********************************************************************
 * TokenStringFormat Test
 *
 * @author eso
 */
public class TokenStringFormatTest extends TestCase
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of TokenStringFormat instances.
	 */
	public void testTokenStringFormat()
	{
		TokenStringFormat<Object> tsf;
		String					  s = "1234567890";
		String					  f = "yy-MM-dd HH:mm.ss";
		Date					  d = new Date();
		Integer					  n = new Integer(42);

		tsf =
			new TokenStringFormat<Object>("{#}: {length():F%03d}, " +
										  "{substring(0,5):&{substring(2,4):F%-4s]}}");
		assertEquals("1234567890: 010, 34  ]", tsf.evaluate(s));

		Object[] arr = new Object[] { "a", "b", "c" };

		tsf = new TokenStringFormat<Object>("{#:F%2s}");
		assertEquals(" a b c", tsf.evaluate(arr));

		tsf = new TokenStringFormat<Object>("{#:D" + f + "}");
		assertEquals(new SimpleDateFormat(f).format(d), tsf.evaluate(d));

		f   = "000.00";
		tsf = new TokenStringFormat<Object>("{#:N" + f + "}");

		assertEquals(new DecimalFormat(f).format(n), tsf.evaluate(n));

		f   = "1#Test 1|2#Test 2|42#Test 42";
		tsf = new TokenStringFormat<Object>("{#:C" + f + "}");

		assertEquals("Test 42", tsf.evaluate(n));
	}
}
