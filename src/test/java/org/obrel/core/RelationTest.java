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
package org.obrel.core;

import de.esoco.lib.event.EventHandler;
import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.InvertibleFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import org.obrel.core.Annotations.NoRelationNameCheck;
import org.obrel.core.Annotations.RelationTypeNamespace;
import org.obrel.filter.RelationFilters;
import org.obrel.type.ListenerTypes;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.Functions.invert;
import static de.esoco.lib.expression.StringFunctions.toByteArray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.obrel.core.ObjectRelations.urlDelete;
import static org.obrel.core.ObjectRelations.urlGet;
import static org.obrel.core.ObjectRelations.urlPut;
import static org.obrel.core.RelationTypeModifier.FINAL;
import static org.obrel.core.RelationTypeModifier.PRIVATE;
import static org.obrel.core.RelationTypes.newFlagType;
import static org.obrel.core.RelationTypes.newListType;
import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.core.RelationTypes.newSetType;
import static org.obrel.core.RelationTypes.newType;
import static org.obrel.filter.RelationFilters.ALL_RELATIONS;
import static org.obrel.type.StandardTypes.DESCRIPTION;
import static org.obrel.type.StandardTypes.INFO;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.ORDINAL;


/********************************************************************
 * Basic object relation tests.
 *
 * @author eso
 */
@RelationTypeNamespace("org.obrel.test")
@SuppressWarnings("boxing")
public class RelationTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String TEST_VALUE = "TestValue";

	static final RelationType<Object>    TEST_ID  = newType();
	static final RelationType<Relatable> TEST_REF = newType();

	static final RelationType<Boolean> TEST_FLAG		 = newType(FINAL);
	static final RelationType<Boolean> FINAL_TEST_FLAG   = newFlagType(FINAL);
	static final RelationType<Boolean> PRIVATE_TEST_FLAG = newFlagType(PRIVATE);

	static final RelationType<List<String>> ELEMENTS		   = newListType();
	static final RelationType<String[]>     TEST_ARRAY		   = newType();
	static final RelationType<List<?>[]>    TEST_GENERIC_ARRAY = newType();

	static final RelationType<Set<String>> TEST_SET = newSetType(false);

	static final RelationType<Map<String, Integer>> TEST_MAP =
		newMapType(false);

	static final RelationType<Set<String>> TEST_ORDERED_SET = newSetType(true);

	static final RelationType<Map<String, Integer>> TEST_ORDERED_MAP =
		newMapType(true);

	static final RelationType<List<Map<String, Integer>>> TEST_MAP_LIST =
		newListType();

	static final RelationType<String> TEST_FLAGGED =
		newType(MetaTypes.MANDATORY);

	static final RelationType<Integer> TEST_ANNOTATED =
		RelationTypes.<Integer>newType().annotate(MetaTypes.ORDERED);

	// test if name check annotation works
	@NoRelationNameCheck
	static final RelationType<String> TEST_NAME = StandardTypes.NAME;

	static
	{
		RelationTypes.init(RelationTest.class);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Performs a load test of relations.
	 */
	@Test
	public void loadTest()
	{
		int				  nTestCount = 100000;
		RelatedObject     aTestObj   = new RelatedObject();
		RelationType<?>[] aTypes     = new RelationType<?>[nTestCount];

		for (int i = 0; i < nTestCount; i++)
		{
			RelationType<String> aType =
				new RelationType<>("TEST_TYPE_" + i, String.class);

			aTypes[i] = aType;
			aTestObj.set(aType, TEST_VALUE + i);
		}

		int nHalf = nTestCount / 2;

		for (int i = 0; i < nHalf; i++)
		{
			int nAddIndex = nHalf + i;
			int nSubIndex = nHalf - i;

			assertEquals(
				TEST_VALUE + nAddIndex,
				aTestObj.get(aTypes[nAddIndex]));
			assertEquals(
				TEST_VALUE + nSubIndex,
				aTestObj.get(aTypes[nSubIndex]));
		}

		assertEquals(nTestCount, aTestObj.getRelationCount(ALL_RELATIONS));

		for (int i = 0; i < nHalf; i++)
		{
			int nAddIndex = nHalf + i;
			int nSubIndex = nHalf - i;

			aTestObj.deleteRelation(aTypes[nAddIndex]);
			aTestObj.deleteRelation(aTypes[nSubIndex]);
		}

		assertEquals(1, aTestObj.getRelationCount(ALL_RELATIONS));
	}

	/***************************************
	 * Tests aliased relations. See {@link RelationAlias} and {@link
	 * RelationView}.
	 */
	@Test
	public void testAliasedConvertedRelations()
	{
		RelatedObject     o = new RelatedObject();
		Relation<Integer> r = o.set(ORDINAL, 1234);

		InvertibleFunction<Integer, String> fConvertInt =
			Conversions.getStringConversion(Integer.class);

		r.aliasAs(NAME, o, fConvertInt);
		r.viewAs(INFO, o, fConvertInt);

		assertEquals("1234", o.get(NAME));
		assertEquals("1234", o.get(INFO));

		o.set(NAME, "42");
		assertEquals(Integer.valueOf(42), o.get(ORDINAL));
	}

	/***************************************
	 * Tests aliased relations. See {@link RelationAlias} and {@link
	 * RelationView}.
	 */
	@Test
	public void testAliasedRelations()
	{
		RelatedObject    o = new RelatedObject();
		Relation<String> r = o.set(NAME, "AliasTest");

		r.aliasAs(DESCRIPTION, o);
		r.viewAs(TEST_ID, o);

		assertEquals("AliasTest", o.get(NAME));
		assertEquals("AliasTest", o.get(DESCRIPTION));
		assertEquals("AliasTest", o.get(TEST_ID));

		try
		{
			o.set(TEST_ID, "XXX");
			fail();
		}
		catch (UnsupportedOperationException e)
		{
			// expected
			assertEquals("AliasTest", o.get(NAME));
			assertEquals("AliasTest", o.get(TEST_ID));
		}

		o.set(DESCRIPTION, "Changed");
		assertEquals("Changed", o.get(NAME));
		assertEquals("Changed", o.get(DESCRIPTION));
		assertEquals("Changed", o.get(TEST_ID));

		// alias annotation inheritance
		r.annotate(INFO, "Meta");
		assertFalse(o.getRelation(DESCRIPTION).hasAnnotation(INFO));
		assertFalse(o.getRelation(TEST_ID).hasAnnotation(INFO));

		// deletion of aliases
		o.deleteRelation(NAME);
		assertFalse(o.hasRelation(NAME));
		assertTrue(o.hasRelation(DESCRIPTION));
		assertTrue(o.hasRelation(TEST_ID));
	}

	/***************************************
	 * Test of annotations (meta-relations).
	 */
	@Test
	public void testAnnotations()
	{
		RelatedObject o = new RelatedObject();

		o.set(NAME, "Test").annotate(TEST_ID, "ID1");

		assertTrue(o.getRelation(NAME).hasAnnotation(TEST_ID));
		assertEquals("ID1", o.getRelation(NAME).getAnnotation(TEST_ID));
	}

	/***************************************
	 * Test relation access.
	 */
	@Test
	public void testBasicRelations()
	{
		RelatedObject o = new RelatedObject();

		o.get(ELEMENTS).add("Elem1");
		o.get(ELEMENTS).add("Elem2");
		o.set(NAME, "Test1");
		o.set(NAME, "Test2");

		assertEquals("Test2", o.get(NAME));
		assertEquals(2, o.getRelationCount(ALL_RELATIONS));
		assertFalse(o.hasFlag(TEST_FLAG));
	}

	/***************************************
	 * Test of {@link ObjectRelations#copyRelations(Relatable, Relatable,
	 * boolean)}.
	 */
	@Test
	public void testCopyRelations()
	{
		RelatedObject s = new RelatedObject();
		RelatedObject t = new RelatedObject();

		s.set(TEST_ID, 1);
		s.set(NAME, "TEST1");
		s.set(DESCRIPTION, "DESC1");
		t.set(TEST_ID, 2);
		t.set(StandardTypes.NAME, "TEST2");

		ObjectRelations.copyRelations(s, t, false);

		assertEquals(3, t.getRelationCount(null));
		assertEquals(2, t.get(TEST_ID));
		assertEquals("TEST2", t.get(StandardTypes.NAME));
		assertEquals("DESC1", t.get(StandardTypes.DESCRIPTION));

		ObjectRelations.copyRelations(s, t, true);

		assertEquals(3, t.getRelationCount(null));
		assertEquals(1, t.get(TEST_ID));
		assertEquals("TEST1", t.get(StandardTypes.NAME));
		assertEquals("DESC1", t.get(StandardTypes.DESCRIPTION));
	}

	/***************************************
	 * Tests the methods {@link RelatedObject#relationsEqual(RelatedObject)},
	 * {@link RelatedObject#relationsHashCode()}, {@link Relation#hashCode()},
	 * and {@link Relation#equals(Object)}.
	 */
	@Test
	public void testEqualsAndHashCode()
	{
		RelatedObject o1 = new RelatedObject();
		RelatedObject o2 = new RelatedObject();

		o1.set(NAME, "test");
		o1.set(DESCRIPTION, "desc");
		o2.set(DESCRIPTION, "desc");
		o2.set(NAME, "test");

		assertTrue(o1.relationsEqual(o2));
		assertTrue(o1.getRelation(NAME).equals(o2.getRelation(NAME)));
		assertFalse(o1.getRelation(NAME).equals(o2.getRelation(DESCRIPTION)));

		assertEquals(o1.relationsHashCode(), o2.relationsHashCode());
		assertEquals(
			o1.getRelation(NAME).hashCode(),
			o2.getRelation(NAME).hashCode());
	}

	/***************************************
	 * Test final relation type.
	 */
	@Test
	public void testFinalType()
	{
		RelatedObject o = new RelatedObject();

		o.set(FINAL_TEST_FLAG);

		try
		{
			o.set(FINAL_TEST_FLAG, false);
			fail();
		}
		catch (Exception e)
		{
			assertTrue(o.get(FINAL_TEST_FLAG));
		}

		try
		{
			o.deleteRelation(FINAL_TEST_FLAG);
			fail();
		}
		catch (Exception e)
		{
			assertTrue(o.get(FINAL_TEST_FLAG));
		}
	}

	/***************************************
	 * Test immutable flag type {@link MetaTypes#IMMUTABLE}.
	 */
	@Test
	public void testImmutable()
	{
		RelatedObject o = new RelatedObject();

		o.set(NAME, "immutable");
		o.get(ELEMENTS).add("E1");
		o.set(MetaTypes.IMMUTABLE);

		try
		{
			o.set(NAME, "changed");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("immutable", o.get(NAME));
		}

		try
		{
			o.set(DESCRIPTION, "test");
			fail();
		}
		catch (Exception e)
		{
			assertFalse(o.hasRelation(DESCRIPTION));
		}

		try
		{
			o.getRelation(NAME).annotate(TEST_FLAG);
			fail();
		}
		catch (Exception e)
		{
			assertFalse(o.getRelation(NAME).hasFlagAnnotation(TEST_FLAG));
		}

		try
		{
			o.deleteRelation(NAME);
			fail();
		}
		catch (Exception e)
		{
			assertTrue(o.hasRelation(NAME));
		}

		try
		{
			o.get(ELEMENTS).add("E2");
			fail();
		}
		catch (Exception e)
		{
			assertTrue(
				o.get(ELEMENTS).size() == 1 &&
				o.get(ELEMENTS).get(0).equals("E1"));
		}

		try
		{
			o.get(ListenerTypes.RELATION_LISTENERS)
			 .add(
 				new EventHandler<RelationEvent<?>>()
 				{
 					@Override
 					public void handleEvent(RelationEvent<?> rEvent)
 					{
 						// should never be invoked
 						fail();
 					}
 				});
			fail();
		}
		catch (Exception e)
		{
			// this should happen
		}
	}

	/***************************************
	 * Test immutable flag type {@link MetaTypes#IMMUTABLE} on single relation.
	 */
	@Test
	public void testImmutableRelation()
	{
		RelatedObject o = new RelatedObject();

		o.set(NAME, "test").annotate(MetaTypes.IMMUTABLE);

		try
		{
			o.set(NAME, "changed");
			fail();
		}
		catch (Exception e)
		{
			assertEquals("test", o.get(NAME));
		}
	}

	/***************************************
	 * Test of {@link RelatedObject#set(RelationType,
	 * de.esoco.lib.expression.Function, Object)}.
	 */
	@Test
	public void testIntermediateRelation()
	{
		RelatedObject o = new RelatedObject();

		Relation<String> r =
			o.set(NAME, invert(toByteArray()), "TEST".getBytes());

		Object it = r.get(IntermediateRelation.INTERMEDIATE_TARGET);

		assertTrue(it instanceof byte[]);
		assertEquals("TEST", o.get(NAME));

		o = new RelatedObject();
		o.set(NAME, "TEST");

		try
		{
			r = o.set(NAME, invert(toByteArray()), "TEST".getBytes());
			assertTrue(false);
		}
		catch (IllegalStateException e)
		{
			// expected
		}
	}

	/***************************************
	 * Test setting list relations with vararg method.
	 */
	@Test
	public void testListRelations()
	{
		RelatedObject o = new RelatedObject();

		o.set(ELEMENTS, Arrays.asList("Elem1", "Elem2"));

		assertEquals(2, o.get(ELEMENTS).size());
		assertEquals("Elem1", o.get(ELEMENTS).get(0));
		assertEquals("Elem2", o.get(ELEMENTS).get(1));
		assertEquals(1, o.getRelationCount(ALL_RELATIONS));
	}

	/***************************************
	 * Test private relation type.
	 */
	@Test
	public void testPrivateType()
	{
		RelatedObject o = new RelatedObject();

		o.set(PRIVATE_TEST_FLAG);

		assertEquals(0, o.getRelationCount(RelationFilters.ALL_RELATIONS));
		assertEquals(0, o.getRelations(RelationFilters.ALL_RELATIONS).size());
		assertEquals(0, o.getAll(RelationFilters.ALL_RELATIONS).size());
		assertTrue(o.get(PRIVATE_TEST_FLAG));

		o.set(PRIVATE_TEST_FLAG, false);
		assertTrue(o.hasRelation(PRIVATE_TEST_FLAG));
		assertFalse(o.get(PRIVATE_TEST_FLAG));

		o.deleteRelation(PRIVATE_TEST_FLAG);
		assertEquals(0, o.getRelationCount(RelationFilters.ALL_RELATIONS));
		assertEquals(0, o.getRelations(RelationFilters.ALL_RELATIONS).size());
		assertEquals(0, o.getAll(RelationFilters.ALL_RELATIONS).size());
		assertFalse(o.hasRelation(PRIVATE_TEST_FLAG));
	}

	/***************************************
	 * Tests the correct initialization of the relation types.
	 */
	@Test
	public void testRelationTypeInit()
	{
		RelatedObject o = new RelatedObject();

		List<String>		 l  = o.get(ELEMENTS);
		Set<String>			 s  = o.get(TEST_SET);
		Set<String>			 os = o.get(TEST_ORDERED_SET);
		Map<String, Integer> m  = o.get(TEST_MAP);
		Map<String, Integer> om = o.get(TEST_ORDERED_MAP);

		assertTrue(ELEMENTS.get(MetaTypes.ELEMENT_DATATYPE) == String.class);
		assertTrue(TEST_SET.get(MetaTypes.ELEMENT_DATATYPE) == String.class);
		assertTrue(TEST_MAP_LIST.get(MetaTypes.ELEMENT_DATATYPE) == Map.class);

		assertTrue(TEST_MAP.get(MetaTypes.KEY_DATATYPE) == String.class);
		assertTrue(TEST_MAP.get(MetaTypes.VALUE_DATATYPE) == Integer.class);

		assertTrue(!TEST_SET.hasFlag(MetaTypes.ORDERED));
		assertTrue(!TEST_MAP.hasFlag(MetaTypes.ORDERED));
		assertTrue(TEST_ORDERED_SET.hasFlag(MetaTypes.ORDERED));
		assertTrue(TEST_ORDERED_MAP.hasFlag(MetaTypes.ORDERED));

		assertTrue(l.getClass() == ArrayList.class);
		assertTrue(s.getClass() == HashSet.class);
		assertTrue(os.getClass() == LinkedHashSet.class);
		assertTrue(m.getClass() == HashMap.class);
		assertTrue(om.getClass() == LinkedHashMap.class);

		assertTrue(TEST_ARRAY.getTargetType() == String[].class);
		assertTrue(TEST_GENERIC_ARRAY.getTargetType() == List[].class);

		assertEquals("org.obrel.test", TEST_ID.getNamespace());

		assertTrue(TEST_FLAGGED.hasFlag(MetaTypes.MANDATORY));
		assertTrue(TEST_ANNOTATED.hasFlag(MetaTypes.ORDERED));

		assertTrue(
			StandardTypes.NAME.get(MetaTypes.DECLARING_CLASS) ==
			StandardTypes.class);

		List<RelationType<?>> rStandardTypes =
			ObjectRelations.getRelatable(StandardTypes.class)
						   .get(MetaTypes.DECLARED_RELATION_TYPES);

		assertTrue(rStandardTypes.contains(StandardTypes.NAME));

		try
		{
			rStandardTypes.clear();
			fail();
		}
		catch (Exception e)
		{
			// this is expected
		}
	}

	/***************************************
	 * Test of {@link ObjectRelations#swapRelations(RelatedObject,
	 * RelatedObject)}.
	 */
	@Test
	public void testSwapRelations()
	{
		RelatedObject o1 = new RelatedObject();
		RelatedObject o2 = new RelatedObject();

		o1.set(TEST_ID, 1);
		o1.set(StandardTypes.NAME, "TEST1");
		o1.set(StandardTypes.DESCRIPTION, "DESC1");
		o2.set(TEST_ID, 2);
		o2.set(StandardTypes.NAME, "TEST2");

		ObjectRelations.swapRelations(o1, o2);

		assertEquals(2, o1.getRelationCount(null));
		assertEquals(2, o1.get(TEST_ID));
		assertEquals("TEST2", o1.get(StandardTypes.NAME));

		assertEquals(3, o2.getRelationCount(null));
		assertEquals(1, o2.get(TEST_ID));
		assertEquals("TEST1", o2.get(StandardTypes.NAME));
		assertEquals("DESC1", o2.get(StandardTypes.DESCRIPTION));
	}

	/***************************************
	 * Test of {@link ObjectRelations#syncRelations(RelatedObject,
	 * RelatedObject)}.
	 */
	@Test
	public void testSyncRelations()
	{
		RelatedObject o1 = new RelatedObject();
		RelatedObject o2 = new RelatedObject();

		o1.set(TEST_ID, 1);
		o2.set(TEST_ID, 2);
		o2.set(StandardTypes.NAME, "TEST2");

		ObjectRelations.syncRelations(o2, o1);

		assertEquals(1, o2.getRelationCount(null));
		assertEquals(1, o2.get(TEST_ID));

		o1.set(StandardTypes.NAME, "TEST1");
		assertEquals("TEST1", o2.get(StandardTypes.NAME));
	}

	/***************************************
	 * Test of {@link RelatedObject#transform(RelationType, InvertibleFunction)}.
	 */
	@Test
	public void testTransformedRelation()
	{
		RelatedObject o = new RelatedObject();

		TransformedRelation<String, byte[]> tr =
			o.transform(NAME, toByteArray());

		assertTrue(o.set(NAME, "TEST") instanceof TransformedRelation);
		assertEquals(toByteArray(), tr.getTransformation());
		assertEquals("TEST", o.get(NAME));

		o = new RelatedObject();
		o.set(NAME, "TEST");
		tr = o.transform(NAME, toByteArray());

		assertEquals(toByteArray(), tr.getTransformation());
		assertEquals("TEST", o.get(NAME));
	}

	/***************************************
	 * Test of {@link ObjectRelations#urlGet(Relatable, String)} and {@link
	 * ObjectRelations#urlResolve(Relatable, String, boolean,
	 * org.obrel.space.ObjectSpaceResolver)}.
	 */
	@Test
	public void testUrlGet()
	{
		RelatedObject o1 = new RelatedObject();
		RelatedObject o2 = new RelatedObject();
		RelatedObject o3 = new RelatedObject();

		o1.set(TEST_REF, o2);
		o2.set(TEST_REF, o3);
		o1.set(NAME, "TEST1");
		o2.set(NAME, "TEST2");
		o3.set(NAME, "TEST3");
		o3.set(TEST_FLAG);

		assertEquals("TEST1", urlGet(o1, "name"));
		assertEquals("TEST1", urlGet(o1, "Name"));
		assertEquals("TEST1", urlGet(o1, "NAME"));
		assertEquals("TEST1", urlGet(o1, "/name"));
		assertEquals("TEST1", urlGet(o1, "name/"));
		assertEquals("TEST1", urlGet(o1, "/name/"));
		assertEquals("TEST1", urlGet(o1, "//name//"));
		assertEquals("TEST2", urlGet(o1, "test-ref/name"));
		assertEquals("TEST2", urlGet(o1, "//test-ref//name//"));
		assertEquals("TEST3", urlGet(o1, "test-ref/test-ref/name"));
		assertEquals("TEST3", urlGet(o1, "test-ref//test-ref///name"));
		assertEquals(true, urlGet(o1, "test-ref/test-ref/test-flag"));
		assertEquals(
			true,
			urlGet(o1, "test-ref/test-ref/org.obrel.test.test-flag"));

		try
		{
			urlGet(o1, "test-flag");
			fail();
		}
		catch (NoSuchElementException e)
		{
			// expected
		}

		try
		{
			urlGet(o1, "test-ref/test-flag");
			fail();
		}
		catch (NoSuchElementException e)
		{
			// expected
		}

		try
		{
			urlGet(o1, "test-ref/test-ref/test-ref");
			fail();
		}
		catch (NoSuchElementException e)
		{
			// expected
		}
	}

	/***************************************
	 * Test of {@link ObjectRelations#urlPut(Relatable, String, Object)}.
	 */
	@Test
	public void testUrlPut()
	{
		RelatedObject o1 = new RelatedObject();
		RelatedObject o2 = new RelatedObject();
		RelatedObject o3 = new RelatedObject();

		o1.set(TEST_REF, o2);
		o2.set(TEST_REF, o3);
		o1.set(NAME, "TEST1");

		urlPut(o1, "name", "TEST1-PUT");
		assertEquals("TEST1-PUT", o1.get(NAME));

		urlPut(o1, "test-ref/name", "TEST2-PUT");
		assertEquals("TEST2-PUT", o2.get(NAME));

		urlPut(o1, "test-ref/test-ref/name", "TEST3-PUT");
		assertEquals("TEST3-PUT", o3.get(NAME));

		urlPut(o1, "test-ref/test-ref/org.obrel.test.test-flag", Boolean.TRUE);
		assertEquals(Boolean.TRUE, o3.get(TEST_FLAG));

		try
		{
			urlPut(o1, "name", new Integer(1));
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}

		urlDelete(o1, "test-ref/name");
		assertTrue(o1.hasRelation(NAME));
		assertFalse(o2.hasRelation(NAME));
		urlDelete(o1, "/name");
		assertFalse(o1.hasRelation(NAME));
	}
}
