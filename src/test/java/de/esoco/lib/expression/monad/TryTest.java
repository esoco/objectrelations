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
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/********************************************************************
 * Test of {@link Try}.
 *
 * @author eso
 */
public class TryTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Try#and(Monad, java.util.function.BiFunction)}.
	 */
	@Test
	public void testAnd()
	{
		LocalDate today = LocalDate.now();

		Try<LocalDate> aLocalDateTry =
			Try.of(() -> today.getYear())
			   .and(Try.of(() -> today.getMonth()), (y, m) -> Pair.of(y, m))
			   .and(
   				Try.of(() -> today.getDayOfMonth()),
   				(ym, d) -> LocalDate.of(ym.first(), ym.second(), d));

		aLocalDateTry.then(d -> assertEquals(today, d));
	}

	/***************************************
	 * Test of {@link Try#equals(Object)}.
	 */
	@Test
	public void testEquals()
	{
		assertEquals(Try.of(() -> "TEST"), (Try.of(() -> "TEST")));
		assertEquals(
			Try.of(() -> "42").map(Integer::parseInt),
			Try.of(() -> "42").map(Integer::parseInt));
		assertEquals(Try.success("42"), (Try.of(() -> "42")));

		assertNotEquals(Try.of(() -> "TEST1"), Try.of(() -> "TEST2"));
		assertNotEquals(
			Try.of(() -> new Exception()),
			Try.failure(new Exception()));
	}

	/***************************************
	 * Test of {@link Try#exists()}.
	 */
	@Test
	public void testExists()
	{
		assertTrue(Try.of(() -> "TEST").isSuccess());
		assertFalse(Try.failure(new Exception()).isSuccess());
		assertFalse(
			Try.of(() -> { throw new RuntimeException(); }).isSuccess());
	}

	/***************************************
	 * Test of {@link Try#failure(Throwable)}.
	 */
	@Test
	public void testFailure()
	{
		assertEquals("FAILED", Try.failure(new Exception()).orUse("FAILED"));

		String[] message = new String[1];

		Try.failure(new Exception("FAILED"))
		   .then(v -> fail())
		   .orElse(e -> message[0] = e.getMessage());
		assertEquals("FAILED", message[0]);

		try
		{
			Try.failure(new Exception()).orFail();
			fail();
		}
		catch (Throwable e)
		{
			// expected
		}

		try
		{
			Try.failure(new Exception()).orThrow(RuntimeException::new);
			fail();
		}
		catch (RuntimeException e)
		{
			// expected
		}
	}

	/***************************************
	 * Test of {@link Try#filter(java.util.function.Predicate)}.
	 */
	@Test
	public void testFilter()
	{
		assertTrue(Try.of(() -> 42).filter(i -> i == 42).isSuccess());
		assertFalse(Try.of(() -> 42).filter(i -> i < 42).isSuccess());
		assertFalse(
			Try.<Integer>failure(new Exception())
			.filter(i -> i >= Integer.MIN_VALUE)
			.isSuccess());
	}

	/***************************************
	 * Test of {@link Try#flatMap(java.util.function.Function)}.
	 */
	@Test
	public void testFlatMap()
	{
		Try<Integer> aTry =
			Try.of(() -> "42").flatMap(s -> Try.of(() -> Integer.parseInt(s)));

		assertTrue(aTry.isSuccess());
		aTry.then(i -> assertTrue(i == 42));
		assertFalse(
			Try.failure(new Exception())
			.flatMap(v -> Try.of(() -> v))
			.isSuccess());
	}

	/***************************************
	 * Test of {@link Try#equals(Object)}.
	 */
	@Test
	public void testHashCode()
	{
		assertTrue(
			Try.of(() -> "TEST").hashCode() == Try.of(() -> "TEST")
			.hashCode());
		assertTrue(
			Try.of(() -> "42").map(Integer::parseInt).hashCode() ==
			Try.of(() -> "42").map(Integer::parseInt).hashCode());

		assertFalse(
			Try.of(() -> "TEST1").hashCode() ==
			Try.of(() -> "TEST2").hashCode());
	}

	/***************************************
	 * Test of {@link Try#map(Function)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testMap() throws Throwable
	{
		assertFalse(
			Try.<String>failure(new Exception())
			.map(Integer::parseInt)
			.isSuccess());
		Try.of(() -> "42")
		   .map(Integer::parseInt)
		   .then(i -> assertTrue(i == 42))
		   .orFail();
	}

	/***************************************
	 * Test of {@link Try#ofAll(java.util.stream.Stream)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testOfAll() throws Throwable
	{
		Try.ofAll(
   			Arrays.asList(Try.of(() -> 1), Try.of(() -> 2), Try.of(() -> 3)))
		   .then(c -> assertEquals(Arrays.asList(1, 2, 3), c))
		   .orFail();
		Try.ofAll(
   			Arrays.asList(
   				Try.of(() -> 1),
   				Try.of(() -> 2),
   				Try.of(() -> 3),
   				Try.<Integer>failure(new Exception("FAILED"))))
		   .then(c -> fail())
		   .orElse(e -> assertEquals("FAILED", e.getMessage()));
	}

	/***************************************
	 * Test of {@link Try#ofSuccessful(java.util.stream.Stream)}.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testOfSuccessful() throws Throwable
	{
		Try.ofSuccessful(
   			Arrays.asList(Try.of(() -> 1), Try.of(() -> 2), Try.of(() -> 3))
   			.stream())
		   .then(
   			stream ->
   				assertEquals(
   					Arrays.asList(1, 2, 3),
   					stream.collect(Collectors.toList())))
		   .orFail();
		Try.ofSuccessful(
   			Arrays.asList(
   				Try.of(() -> 1),
   				Try.of(() -> 2),
   				Try.of(() -> 3),
   				Try.<Integer>failure(new Exception()))
   			.stream())
		   .then(
   			stream ->
   				assertEquals(
   					Arrays.asList(1, 2, 3),
   					stream.collect(Collectors.toList())))
		   .orFail();
	}

	/***************************************
	 * Test of {@link Try#success(Object)}.
	 */
	@Test
	public void testSuccess()
	{
		Try.success("TEST").then(s -> assertEquals("TEST", s));
		assertEquals("SUCCESS", Try.success("SUCCESS").orUse("FAILED"));

		try
		{
			assertEquals("SUCCESS", Try.success("SUCCESS").orFail());
		}
		catch (Throwable e)
		{
			fail();
		}
	}

	/***************************************
	 * Test of {@link Try#toString()}.
	 */
	@Test
	public void testToString()
	{
		assertEquals("Success[TEST]", Try.success("TEST").toString());
		assertEquals(
			"Failure[ERROR]",
			Try.failure(new Exception("ERROR")).toString());
	}
}
