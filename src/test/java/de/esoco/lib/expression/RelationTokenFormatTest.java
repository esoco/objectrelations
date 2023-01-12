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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.obrel.type.StandardTypes.CHILDREN;
import static org.obrel.type.StandardTypes.DATE;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PORT;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;

import de.esoco.lib.expression.function.RelationTokenFormat;

/**
 * TokenStringFormat Test
 *
 * @author eso
 */
public class RelationTokenFormatTest {
	/**
	 * Test of TokenStringFormat instances.
	 */
	@Test
	void testTokenStringFormat() {
		Date aDate = new Date();
		Integer aInt = Integer.valueOf(42);
		Relatable aRelatable = newRelatedObject(aDate, aInt);
		RelationTokenFormat aFormat;
		String sFormat;

		aRelatable.set(CHILDREN,
				Arrays.asList(newRelatedObject(aDate, 1),
						newRelatedObject(aDate, 2)));

		aFormat = new RelationTokenFormat("{NAME}: {NAME:&{length():F%03d}}, " +
				"{NAME:&{substring(0,5):&{substring(2,4):F%-4s]}}}");
		assertEquals("1234567890: 010, 34  ]", aFormat.evaluate(aRelatable));

		sFormat = "yy-MM-dd HH:mm.ss";
		aFormat = new RelationTokenFormat("{DATE:D" + sFormat + "}");

		assertEquals(new SimpleDateFormat(sFormat).format(aDate),
				aFormat.evaluate(aRelatable));

		sFormat = "000.00";
		aFormat = new RelationTokenFormat("{PORT:N" + sFormat + "}");

		assertEquals(new DecimalFormat(sFormat).format(aInt),
				aFormat.evaluate(aRelatable));

		aFormat = new RelationTokenFormat("{CHILDREN:&{size()}}-" +
				"{CHILDREN:&{get(0):&{PORT}}}-" +
				"{CHILDREN:&{get(1):&{PORT}}}");
		assertEquals("2-1-2", aFormat.evaluate(aRelatable));
	}

	/**
	 * Creates a new test object.
	 *
	 * @param aDate The date
	 * @param aInt  The int
	 *
	 * @return The new object
	 */
	private Relatable newRelatedObject(Date aDate, Integer aInt) {
		Relatable aRelatable = new RelatedObject();

		aRelatable.set(NAME, "1234567890");
		aRelatable.set(DATE, aDate);
		aRelatable.set(PORT, aInt);

		return aRelatable;
	}
}
