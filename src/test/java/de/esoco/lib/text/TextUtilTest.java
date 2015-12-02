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
package de.esoco.lib.text;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/********************************************************************
 * Test of TextUtil class.
 *
 * @author eso
 */
public class TextUtilTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of method {@link TextUtil#uppercaseIdentifier(String)}.
	 */
	@Test
	public final void testCapitalizedIdentifier()
	{
		assertEquals("Foobar", TextUtil.capitalizedIdentifier("Foobar"));
		assertEquals("FooBar", TextUtil.capitalizedIdentifier("FooBar"));
		assertEquals("Foobar", TextUtil.capitalizedIdentifier("FOOBAR"));
		assertEquals("Foobar", TextUtil.capitalizedIdentifier("foobar"));
		assertEquals("FooBar", TextUtil.capitalizedIdentifier("FOO_BAR"));
		assertEquals("FooBar", TextUtil.capitalizedIdentifier("FOO BAR"));
		assertEquals("FooBar", TextUtil.capitalizedIdentifier("foo_bar"));
	}

	/***************************************
	 * Test of method {@link TextUtil#count(String, Pattern)}.
	 */
	@Test
	public final void testCount()
	{
		String s = "[] test [abc][xyz] [1]---[2]";

		assertEquals(5, TextUtil.count(s, Pattern.compile("\\[.*?\\]")));
	}

	/***************************************
	 * Test of method {@link TextUtil#parseObject(String)}.
	 */
	@Test
	public final void testParseObject()
	{
		assertEquals(null, TextUtil.parseObject("null"));

		// tests of valid string values
		assertEquals("TEST1", TextUtil.parseObject("'TEST1'"));
		assertEquals("TEST2", TextUtil.parseObject("\"TEST2\""));
		assertEquals("TEST3", TextUtil.parseObject("TEST3"));

		assertEquals("", TextUtil.parseObject("''"));
		assertEquals("'", TextUtil.parseObject("'''"));
		assertEquals("\"\"", TextUtil.parseObject("\"\"\"\""));
		assertEquals("\"'\"'", TextUtil.parseObject("\"\"'\"'\""));

		// tests of valid integer values
		assertEquals(new Integer(0), TextUtil.parseObject("0"));
		assertEquals(new Integer(0), TextUtil.parseObject("-0"));
		assertEquals(new Integer(0), TextUtil.parseObject("000000000"));

		assertEquals(new Integer(12345), TextUtil.parseObject("12345"));
		assertEquals(new Integer(-654321), TextUtil.parseObject("-654321"));
		assertEquals(new Integer(Integer.MAX_VALUE),
					 TextUtil.parseObject("" + Integer.MAX_VALUE));
		assertEquals(new Integer(Integer.MIN_VALUE),
					 TextUtil.parseObject("" + Integer.MIN_VALUE));
	}

	/***************************************
	 * Test of method {@link TextUtil#toAscii(String)}.
	 */
	@Test
	public final void testToAscii()
	{
		assertEquals("aeoeueAeOeUess",
					 TextUtil.toAscii("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF"));

		assertEquals("aaaaAAAA",
					 TextUtil.toAscii("\u00E0\u00E1\u00E2\u00E3\u00C0\u00C1\u00C2\u00C3"));
	}

	/***************************************
	 * Test of method {@link TextUtil#uppercaseIdentifier(String)}.
	 */
	@Test
	public final void testUppercaseIdentifier()
	{
		assertEquals("FOOBAR", TextUtil.uppercaseIdentifier("foobar"));
		assertEquals("FOOBAR", TextUtil.uppercaseIdentifier("Foobar"));

		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("FooBar"));
		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("FOOBar"));
		assertEquals("F_OOBAR", TextUtil.uppercaseIdentifier("FOobar"));
		assertEquals("F_OO_BAR", TextUtil.uppercaseIdentifier("FOoBar"));
		assertEquals("F_OO_BAR", TextUtil.uppercaseIdentifier("fOoBar"));

		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("FooBAR"));
		assertEquals("FOO_B_AR", TextUtil.uppercaseIdentifier("FooBAr"));
		assertEquals("FOOB_AR", TextUtil.uppercaseIdentifier("FoobAr"));
		assertEquals("FOOB_AR", TextUtil.uppercaseIdentifier("FoobAR"));
		assertEquals("FOOBA_R", TextUtil.uppercaseIdentifier("foobaR"));
		assertEquals("FOOBA_R", TextUtil.uppercaseIdentifier("FoobaR"));

		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("foo_bar"));
		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("foo bar"));
		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("Foo_Bar"));
		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("Foo Bar"));
		assertEquals("_FOOBAR", TextUtil.uppercaseIdentifier("_Foobar"));

		assertEquals("FOOBAR1", TextUtil.uppercaseIdentifier("Foobar1"));
		assertEquals("FOOBAR_1", TextUtil.uppercaseIdentifier("Foobar_1"));
		assertEquals("FOO1BAR", TextUtil.uppercaseIdentifier("Foo1bar"));
		assertEquals("FOO1_BAR", TextUtil.uppercaseIdentifier("Foo1BAR"));
		assertEquals("FOO1_BA", TextUtil.uppercaseIdentifier("Foo1BA"));
		assertEquals("FOO1_B", TextUtil.uppercaseIdentifier("Foo1B"));
		assertEquals("FOO1B", TextUtil.uppercaseIdentifier("Foo1b"));
		assertEquals("FOO1BA", TextUtil.uppercaseIdentifier("Foo1ba"));
		assertEquals("FOO1_BAR", TextUtil.uppercaseIdentifier("Foo1Bar"));
		assertEquals("FOO1_BA", TextUtil.uppercaseIdentifier("Foo1Ba"));
		assertEquals("FOO1_B", TextUtil.uppercaseIdentifier("Foo1B"));
		assertEquals("FOO_1_BAR", TextUtil.uppercaseIdentifier("Foo_1Bar"));
		assertEquals("FOO123_BAR", TextUtil.uppercaseIdentifier("Foo123Bar"));
		assertEquals("FOO_123_BAR", TextUtil.uppercaseIdentifier("Foo_123Bar"));

		assertEquals("FOOBAR", TextUtil.uppercaseIdentifier("FOOBAR"));
		assertEquals("FOO_BAR", TextUtil.uppercaseIdentifier("FOO_BAR"));
		assertEquals("FOO_BAR_", TextUtil.uppercaseIdentifier("FOO_BAR_"));
		assertEquals("_FOO_BAR", TextUtil.uppercaseIdentifier("_FOO_BAR"));
		assertEquals("_FOO_BAR_", TextUtil.uppercaseIdentifier("_FOO_BAR_"));

		assertEquals("FOOBAR1", TextUtil.uppercaseIdentifier("FOOBAR1"));
		assertEquals("FOOBAR12", TextUtil.uppercaseIdentifier("FOOBAR12"));
		assertEquals("FOOBAR123", TextUtil.uppercaseIdentifier("FOOBAR123"));
		assertEquals("FOOBAR_1", TextUtil.uppercaseIdentifier("FOOBAR_1"));
		assertEquals("FOOBAR_12", TextUtil.uppercaseIdentifier("FOOBAR_12"));
		assertEquals("FOOBAR_123", TextUtil.uppercaseIdentifier("FOOBAR_123"));

		assertEquals("FOO1BAR", TextUtil.uppercaseIdentifier("FOO1BAR"));
		assertEquals("FOO1B", TextUtil.uppercaseIdentifier("FOO1B"));
		assertEquals("FOO1BA", TextUtil.uppercaseIdentifier("FOO1BA"));
		assertEquals("FOO12BAR", TextUtil.uppercaseIdentifier("FOO12BAR"));
		assertEquals("FOO123BAR", TextUtil.uppercaseIdentifier("FOO123BAR"));

		assertEquals("FOO_1BAR", TextUtil.uppercaseIdentifier("FOO_1BAR"));
		assertEquals("FOO1_BAR", TextUtil.uppercaseIdentifier("FOO1_BAR"));
		assertEquals("FOO_1_BAR", TextUtil.uppercaseIdentifier("FOO_1_BAR"));
	}
}
