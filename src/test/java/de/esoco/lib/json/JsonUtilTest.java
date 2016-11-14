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
 * Test of {@link JsonUtil}
 *
 * @author eso
 */
public class JsonUtilTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link JsonUtil#escape(String)}
	 */
	@Test
	public void testEscape()
	{
		assertEquals("\\\"", JsonUtil.escape("\""));
		assertEquals("\\\\", JsonUtil.escape("\\"));
		assertEquals("\\/", JsonUtil.escape("/"));
		assertEquals("\\b", JsonUtil.escape("\b"));
		assertEquals("\\f", JsonUtil.escape("\f"));
		assertEquals("\\n", JsonUtil.escape("\n"));
		assertEquals("\\r", JsonUtil.escape("\r"));
		assertEquals("\\t", JsonUtil.escape("\t"));
		assertEquals("\\u0000-\\u001F", JsonUtil.escape("\u0000-\u001f"));
		assertEquals("\\u007F-\\u009F", JsonUtil.escape("\u007f-\u009f"));
		assertEquals("\\u2000-\\u20FF", JsonUtil.escape("\u2000-\u20ff"));
		assertEquals("\u2100,\u21FF", JsonUtil.escape("\u2100,\u21ff"));
		assertEquals("\uFFFF", JsonUtil.escape("\uffff"));
		assertEquals("\\\"\\\\\\/\\b\\f\\n\\r\\t\\\"",
					 JsonUtil.escape("\"\\/\b\f\n\r\t\""));
		assertEquals(" \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\\" ",
					 JsonUtil.escape(" \" \\ / \b \f \n \r \t \" "));
	}

	/***************************************
	 * Test of {@link JsonUtil#restore(String)}
	 */
	@Test
	public void testRestore()
	{
		assertEquals("\"", JsonUtil.restore("\\\""));
		assertEquals("\\", JsonUtil.restore("\\\\"));
		assertEquals("/", JsonUtil.restore("\\/"));
		assertEquals("\b", JsonUtil.restore("\\b"));
		assertEquals("\f", JsonUtil.restore("\\f"));
		assertEquals("\n", JsonUtil.restore("\\n"));
		assertEquals("\r", JsonUtil.restore("\\r"));
		assertEquals("\t", JsonUtil.restore("\\t"));
		assertEquals("\u0000-\u001F", JsonUtil.restore("\\u0000-\\u001f"));
		assertEquals("\u007F-\u009F", JsonUtil.restore("\\u007f-\\u009f"));
		assertEquals("\u2000-\u20FF", JsonUtil.restore("\\u2000-\\u20ff"));
		assertEquals("\u2100,\u21FF", JsonUtil.restore("\u2100,\u21ff"));
		assertEquals("\uFFFF", JsonUtil.restore("\uffff"));
		assertEquals("\"\\/\b\f\n\r\t\"",
					 JsonUtil.restore("\\\"\\\\\\/\\b\\f\\n\\r\\t\\\""));
		assertEquals(" \" \\ / \b \f \n \r \t \" ",
					 JsonUtil.restore(" \\\" \\\\ \\/ \\b \\f \\n \\r \\t \\\" "));
	}
}
