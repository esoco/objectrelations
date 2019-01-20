//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/********************************************************************
 * Test of {@link Promise}.
 *
 * @author eso
 */
public class PromiseTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Promise#and(Monad, java.util.function.BiFunction)}.
	 */
	@Test
	public void testAnd()
	{
		LocalDate today = LocalDate.now();

		Promise<LocalDate> aLocalDatePromise =
			Promise.of(() -> today.getYear())
				   .and(
	   				Promise.of(() -> today.getMonth()),
	   				(y, m) -> Pair.of(y, m))
				   .and(
	   				Promise.of(() -> today.getDayOfMonth()),
	   				(ym, d) ->
	   					LocalDate.of(ym.first(), ym.second(), d));

		aLocalDatePromise.then(d -> assertEquals(today, d));
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Promise#flatMap(java.util.function.Function)}.
	 */
	@Test
	public void testFlatMap()
	{
		Promise.of("42")
			   .flatMap(s -> Promise.of(() -> Integer.parseInt(s)))
			   .then(i -> assertEquals(Integer.valueOf(42), i));
	}

	/***************************************
	 * Test of {@link Promise#map(Function)}.
	 */
	@Test
	public void testMap()
	{
		Promise.of(() -> "42")
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
	   			Arrays.asList(
	   				Promise.of(() -> 1),
	   				Promise.of(() -> 2),
	   				Promise.of(() -> 3))
	   			.stream())
			   .then(
	   			stream ->
	   				assertEquals(
	   					Arrays.asList(1, 2, 3),
	   					stream.collect(Collectors.toList())));
	}

	/***************************************
	 * Test of {@link Promise#await()}.
	 */
	@Test
	public void testOrUse()
	{
		Promise<String> p = Promise.of(() -> 42).map(i -> Integer.toString(i));

		assertEquals("42", p.orUse(null));
		assertTrue(p.isResolved());

		p = Promise.of(() -> 42).flatMap(i -> Promise.of(Integer.toString(i)));

		assertEquals("42", p.orUse(null));
		assertTrue(p.isResolved());

		p = Promise.of(() -> 42).map(i -> Integer.toString(i));
		assertEquals("42", p.orUse(null));
		assertTrue(p.isResolved());

		p = Promise.of(CompletableFuture.supplyAsync(() -> 42))
				   .map(i -> Integer.toString(i));
		assertEquals("42", p.orUse(null));
		assertTrue(p.isResolved());
	}

	/***************************************
	 * Test of {@link Promise#then(Consumer)}.
	 */
	@Test
	public void testThen()
	{
		Promise.of(() -> "TEST").then(s -> assertEquals("TEST", s));
		Promise.of(() -> null).then(s -> assertNull(s));
	}
}
