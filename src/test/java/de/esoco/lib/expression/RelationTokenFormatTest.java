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

import de.esoco.lib.expression.function.RelationTokenFormat;
import org.junit.jupiter.api.Test;
import org.obrel.core.Relatable;
import org.obrel.core.RelatedObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.obrel.type.StandardTypes.CHILDREN;
import static org.obrel.type.StandardTypes.DATE;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.PORT;

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
		Date date = new Date();
		Integer testInt = Integer.valueOf(42);
		Relatable relatable = newRelatedObject(date, testInt);
		RelationTokenFormat tokenFormat;
		String formatString;

		relatable.set(CHILDREN, Arrays.asList(newRelatedObject(date, 1),
			newRelatedObject(date, 2)));

		tokenFormat = new RelationTokenFormat(
			"{NAME}: {NAME:&{length():F%03d}}," + " " +
				"{NAME:&{substring(0,5):&{substring(2,4):F%-4s]}}}");
		assertEquals("1234567890: 010, 34  ]",
			tokenFormat.evaluate(relatable));

		formatString = "yy-MM-dd HH:mm.ss";
		tokenFormat = new RelationTokenFormat("{DATE:D" + formatString + "}");

		assertEquals(new SimpleDateFormat(formatString).format(date),
			tokenFormat.evaluate(relatable));

		formatString = "000.00";
		tokenFormat = new RelationTokenFormat("{PORT:N" + formatString + "}");

		assertEquals(new DecimalFormat(formatString).format(testInt),
			tokenFormat.evaluate(relatable));

		tokenFormat = new RelationTokenFormat(
			"{CHILDREN:&{size()}}-" + "{CHILDREN:&{get(0):&{PORT}}}-" +
				"{CHILDREN:&{get(1):&{PORT}}}");
		assertEquals("2-1-2", tokenFormat.evaluate(relatable));
	}

	/**
	 * Creates a new test object.
	 *
	 * @param testDate The date
	 * @param testInt  The int
	 * @return The new object
	 */
	private Relatable newRelatedObject(Date testDate, Integer testInt) {
		Relatable relatable = new RelatedObject();

		relatable.set(NAME, "1234567890");
		relatable.set(DATE, testDate);
		relatable.set(PORT, testInt);

		return relatable;
	}
}
