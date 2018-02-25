//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/********************************************************************
 * Test of {@link Json}
 *
 * @author eso
 */
public class JsonUtilTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Json#escape(String)}
	 */
	@Test
	public void testEscape()
	{
		assertEquals("\\\"", Json.escape("\""));
		assertEquals("\\\\", Json.escape("\\"));
		assertEquals("\\/", Json.escape("/"));
		assertEquals("\\b", Json.escape("\b"));
		assertEquals("\\f", Json.escape("\f"));
		assertEquals("\\n", Json.escape("\n"));
		assertEquals("\\r", Json.escape("\r"));
		assertEquals("\\t", Json.escape("\t"));
		assertEquals("\\u0000-\\u001F", Json.escape("\u0000-\u001f"));
		assertEquals("\\u007F-\\u009F", Json.escape("\u007f-\u009f"));
		assertEquals("\\u2000-\\u20FF", Json.escape("\u2000-\u20ff"));
		assertEquals("\u2100,\u21FF", Json.escape("\u2100,\u21ff"));
		assertEquals("\uFFFF", Json.escape("\uffff"));
		assertEquals("\\\"\\\\\\/\\b\\f\\n\\r\\t\\\"",
					 Json.escape("\"\\/\b\f\n\r\t\""));
		assertEquals(" \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\\" ",
					 Json.escape(" \" \\ / \b \f \n \r \t \" "));
	}

	/***************************************
	 * Test of {@link Json#restore(String)}
	 */
	@Test
	public void testRestore()
	{
		assertEquals("\"", Json.restore("\\\""));
		assertEquals("\\", Json.restore("\\\\"));
		assertEquals("/", Json.restore("\\/"));
		assertEquals("\b", Json.restore("\\b"));
		assertEquals("\f", Json.restore("\\f"));
		assertEquals("\n", Json.restore("\\n"));
		assertEquals("\r", Json.restore("\\r"));
		assertEquals("\t", Json.restore("\\t"));
		assertEquals("\u0000-\u001F", Json.restore("\\u0000-\\u001f"));
		assertEquals("\u007F-\u009F", Json.restore("\\u007f-\\u009f"));
		assertEquals("\u2000-\u20FF", Json.restore("\\u2000-\\u20ff"));
		assertEquals("\u2100,\u21FF", Json.restore("\u2100,\u21ff"));
		assertEquals("\uFFFF", Json.restore("\uffff"));
		assertEquals("\"\\/\b\f\n\r\t\"",
					 Json.restore("\\\"\\\\\\/\\b\\f\\n\\r\\t\\\""));
		assertEquals(" \" \\ / \b \f \n \r \t \" ",
					 Json.restore(" \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\\" "));
	}
}
