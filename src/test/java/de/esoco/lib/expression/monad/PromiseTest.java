//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.expression.monad;

import de.esoco.lib.datatype.Pair;

import java.time.LocalDate;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;


/********************************************************************
 * Test of {@link Promise}.
 *
 * @author eso
 */
public class PromiseTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Promise#equals(Object)}.
	 */
	@Test
	public void testEqualsObject()
	{
		assertEquals(Promise.of("TEST"), Promise.of("TEST"));
		assertNotEquals(Promise.of("TEST1"), Promise.of("TEST2"));
	}

	/***************************************
	 * Test of {@link Promise#flatMap(java.util.function.Function)}.
	 */
	@Test
	public void testFlatMap()
	{
		Promise.of("42")
			   .flatMap(s -> Promise.of(Integer.parseInt(s)))
			   .then(i -> assertEquals(Integer.valueOf(42), i));
	}

	/***************************************
	 * Test of {@link Promise#join(Monad, java.util.function.BiFunction)}.
	 */
	@Test
	public void testJoin()
	{
		LocalDate today = LocalDate.now();

		Promise.of(today.getYear())
			   .join(Promise.of(today.getMonth()), (y, m) ->
	   				Pair.of(y, m))
			   .join(
	   			Promise.of(today.getDayOfMonth()),
	   			(ym, d) -> LocalDate.of(ym.first(), ym.second(), d))
			   .then(d -> assertEquals(today, d));
	}

	/***************************************
	 * Test of {@link Promise#map(java.util.function.Function)}.
	 */
	@Test
	public void testMap()
	{
		Promise.of("42")
			   .map(Integer::parseInt)
			   .then(i -> assertEquals(Integer.valueOf(42), i));
	}

	/***************************************
	 * Test of {@link Promise#ofExisting(Stream)}.
	 */
	@Test
	public void testOfAll()
	{
		Promise.ofAll(
	   			Arrays.asList(Promise.of(1), Promise.of(2), Promise.of(3))
	   			.stream())
			   .then(
	   			stream ->
	   				assertEquals(
	   					Arrays.asList(1, 2, 3),
	   					stream.collect(Collectors.toList())));
	}

	/***************************************
	 * Test of {@link Promise#then(java.util.function.Consumer)}.
	 */
	@Test
	public void testThen()
	{
		Promise.of("TEST").then(s -> assertEquals("TEST", s));
		Promise.of(r -> r.accept(null)).then(s -> assertNull(s));
	}
}
