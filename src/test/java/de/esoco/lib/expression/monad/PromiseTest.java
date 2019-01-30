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
import de.esoco.lib.expression.monad.Promise.State;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
	 *
	 * @throws Throwable
	 */
	@Test
	public void testAnd() throws Throwable
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

		aLocalDatePromise.then(d -> assertEquals(today, d)).orFail();
	}

	/***************************************
	 * Test of {@link Promise#flatMap(java.util.function.Function)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testFlatMap() throws Throwable
	{
		Promise.of("42")
			   .flatMap(s -> Promise.of(() -> Integer.parseInt(s)))
			   .then(i -> assertEquals(Integer.valueOf(42), i))
			   .orFail();
	}

	/***************************************
	 * Test of {@link Promise#map(Function)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testMap() throws Throwable
	{
		Promise.of(() -> "42")
			   .map(Integer::parseInt)
			   .then(i -> assertEquals(Integer.valueOf(42), i))
			   .orFail();
	}

	/***************************************
	 * Test of {@link Promise#ofAll(java.util.stream.Stream)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testOfAll() throws Throwable
	{
		List<Promise<Integer>> values =
			new ArrayList<>(
				Arrays.asList(
					Promise.of(() -> 1),
					Promise.of(() -> 2),
					Promise.of(() -> 3)));

		Promise<Collection<Integer>> p = Promise.ofAll(values);

		assertEquals(3, p.orFail().size());
		assertTrue(p.isResolved());

		Exception eError = new Exception("TEST");

		values.add(Promise.failure(eError));
		Promise.ofAll(values)
			   .then(s -> fail())
			   .onError(e -> assertEquals(eError, e));
	}

	/***************************************
	 * Test of {@link Promise#ofAny(java.util.stream.Stream)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testOfAny() throws Throwable
	{
		List<Promise<String>> values =
			Arrays.asList(
				Promise.of("1"),
				Promise.of(() -> "2"),
				Promise.of(() -> "3"));

		Promise<String> p = Promise.ofAny(values);

		assertTrue(p.isResolved());
		assertEquals("1", p.orFail());

		values =
			Arrays.asList(
				Promise.failure(new Exception("TEST")),
				Promise.of(() -> "2"),
				Promise.of(() -> "3"));

		p = Promise.ofAny(values);

		assertEquals(State.FAILED, p.getState());
	}

	/***************************************
	 * Test of {@link Promise#orFail()}.
	 */
	@Test
	public void testOrElse()
	{
		Promise<String> p	    = Promise.failure(new Exception());
		Throwable[]     aResult = new Throwable[1];

		p.then(s -> fail()).orElse(e -> aResult[0] = e);

		assertEquals("ERROR", p.orUse("ERROR"));
		assertNotNull(aResult[0]);
	}

	/***************************************
	 * Test of {@link Promise#orFail()}.
	 */
	@Test
	public void testOrFail()
	{
		Promise<String> p = Promise.failure(new Exception());

		try
		{
			p.orFail();
			fail();
		}
		catch (Throwable e)
		{
			// expected
		}
	}

	/***************************************
	 * Test of {@link Promise#orThrow(java.util.function.Function)}.
	 */
	@Test
	public void testOrThrow()
	{
		Exception	    eError = new Exception();
		Promise<String> p	   = Promise.failure(new Exception());

		try
		{
			p.orThrow(e -> eError);
			fail();
		}
		catch (Throwable e)
		{
			assertEquals(eError, e);
		}
	}

	/***************************************
	 * Test of {@link Promise#orUse(Object)}.
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
