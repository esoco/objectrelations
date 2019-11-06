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
 * Test of {@link Option}.
 *
 * @author eso
 */
public class OptionTest
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test of {@link Option#and(Monad, java.util.function.BiFunction)}.
	 */
	@Test
	public void testAnd()
	{
		LocalDate today = LocalDate.now();

		Option<LocalDate> aLocalDateOption =
			Option.of(today.getYear())
				  .and(Option.of(today.getMonth()), (y, m) ->
	  					Pair.of(y, m))
				  .and(
	  				Option.of(today.getDayOfMonth()),
	  				(ym, d) -> LocalDate.of(ym.first(), ym.second(), d));

		aLocalDateOption.then(d -> assertEquals(today, d));
	}

	/***************************************
	 * Test of {@link Option#equals(Object)}.
	 */
	@Test
	public void testEquals()
	{
		assertEquals(Option.of("TEST"), (Option.of("TEST")));
		assertEquals(
			Option.of("42").map(Integer::parseInt),
			Option.of("42").map(Integer::parseInt));
		assertEquals(Option.none(), (Option.of(null)));

		assertNotEquals(Option.of("TEST1"), Option.of("TEST2"));
		assertNotEquals(Option.of("TEST"), Option.none());
		assertNotEquals(Option.none(), Option.of("TEST"));
	}

	/***************************************
	 * Test of {@link Option#exists()}.
	 */
	@Test
	public void testExists()
	{
		assertTrue(Option.of("TEST").exists());
		assertFalse(Option.of(null).exists());
		assertFalse(Option.none().exists());
	}

	/***************************************
	 * Test of {@link Option#filter(java.util.function.Predicate)}.
	 */
	@Test
	public void testFilter()
	{
		assertTrue(Option.of(42).filter(i -> i == 42).exists());
		assertFalse(Option.of(42).filter(i -> i < 42).exists());
		assertFalse(
			Option.<Integer>none()
			.filter(i -> i >= Integer.MIN_VALUE)
			.exists());
	}

	/***************************************
	 * Test of {@link Option#flatMap(java.util.function.Function)}.
	 */
	@Test
	public void testFlatMap()
	{
		Option<String> none = Option.none();

		assertFalse(none.flatMap(s -> Option.of(s.length())).exists());
		Option.of("42")
			  .flatMap(s -> Option.of(Integer.parseInt(s)))
			  .then(i -> assertTrue(i == 42));
	}

	/***************************************
	 * Test of {@link Option#equals(Object)}.
	 */
	@Test
	public void testHashCode()
	{
		assertTrue(
			Option.of("TEST").hashCode() == Option.of("TEST").hashCode());
		assertTrue(
			Option.of("42").map(Integer::parseInt).hashCode() ==
			Option.of("42").map(Integer::parseInt).hashCode());
		assertTrue(Option.none().hashCode() == Option.of(null).hashCode());

		assertFalse(
			Option.of("TEST1").hashCode() == Option.of("TEST2").hashCode());
		assertFalse(Option.of("TEST").hashCode() == Option.none()
			.hashCode());
		assertFalse(Option.none().hashCode() == Option.of("TEST").hashCode());
	}

	/***************************************
	 * Test of {@link Option#map(Function)}.
	 */
	@Test
	public void testMap()
	{
		assertFalse(Option.of((String) null).map(s -> s.length()).exists());
		Option.of("42").map(Integer::parseInt).then(i -> assertTrue(i == 42));
	}

	/***************************************
	 * Test of {@link Option#none()}.
	 */
	@Test
	public void testNone()
	{
		assertTrue(Option.none() == Option.of(null));
		assertTrue(Option.of(null) == Option.none());
		Option.none().then(v -> fail());
	}

	/***************************************
	 * Test of {@link Option#ofAll(java.util.Collection)}.
	 */
	@Test
	public void testOfAll()
	{
		boolean[] result = new boolean[1];

		Option.ofAll(Arrays.asList(Option.of(1), Option.of(2), Option.of(3)))
			  .then(c -> assertEquals(Arrays.asList(1, 2, 3), c))
			  .orFail();

		Option<Integer> noInt = Option.none();

		Option.ofAll(
	  			Arrays.asList(
	  				Option.of(1),
	  				Option.of(2),
	  				Option.of(3),
	  				noInt)).then(c -> fail()).orElse(e ->
	  				result[0] = true);
		assertTrue(result[0]);
	}

	/***************************************
	 * Test of {@link Option#ofExisting(Stream)}.
	 */
	@Test
	public void testOfExisting()
	{
		Option.ofExisting(
	  			Arrays.asList(Option.of(1), Option.of(2), Option.of(3))
	  			.stream())
			  .then(
	  			stream ->
	  				assertEquals(
	  					Arrays.asList(1, 2, 3),
	  					stream.collect(Collectors.toList())));

		Option.ofExisting(
	  			Arrays.asList(
	  				Option.of(1),
	  				Option.<Integer>none(),
	  				Option.of(2),
	  				Option.<Integer>none(),
	  				Option.of(3),
	  				Option.<Integer>none())
	  			.stream())
			  .then(
	  			stream ->
	  				assertEquals(
	  					Arrays.asList(1, 2, 3),
	  					stream.collect(Collectors.toList())));
	}

	/***************************************
	 * Test of {@link Option#orElse(Runnable)}, {@link Option#orUse(Object)},
	 * {@link Option#orFail()}.
	 */
	@Test
	public void testOr()
	{
		boolean[] result = new boolean[1];

		Option.none().then(v -> fail()).orElse(e -> result[0] = true);
		assertTrue(result[0]);
		assertEquals("DEFAULT", Option.none().orUse("DEFAULT"));

		try
		{
			Option.none().orThrow(e -> new Exception("THROW", e));
			fail();
		}
		catch (Throwable e)
		{
			assertEquals(Exception.class, e.getClass());
			assertEquals("THROW", e.getMessage());
		}

		try
		{
			Option.none().orFail();
			fail();
		}
		catch (Throwable e)
		{
			assertEquals(NullPointerException.class, e.getClass());
		}
	}

	/***************************************
	 * Test of {@link Option#toString()}.
	 */
	@Test
	public void testToString()
	{
		assertEquals("TEST", Option.of("TEST").toString());
		assertEquals("[none]", Option.none().toString());
	}
}
