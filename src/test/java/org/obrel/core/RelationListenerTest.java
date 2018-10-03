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
package org.obrel.core;

import de.esoco.lib.event.EventDispatcher;
import de.esoco.lib.event.EventHandler;

import org.junit.Test;

import org.obrel.type.ListenerTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.obrel.type.StandardTypes.COUNT;
import static org.obrel.type.StandardTypes.NAME;
import static org.obrel.type.StandardTypes.ORDINAL;
import static org.obrel.type.StandardTypes.SIZE;


/********************************************************************
 * Test of relation listener functionality.
 *
 * @author eso
 */
public class RelationListenerTest
{
	//~ Instance fields --------------------------------------------------------

	private Object rRelationTarget;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test relation listener on a related object.
	 */
	@Test
	public void testObjectRelationListener()
	{
		RelatedObject aTest1 = new RelatedObject();
		RelatedObject aTest2 = new RelatedObject();

		TestListener<?> aListener = new TestListener<>();

		aTest1.get(ListenerTypes.RELATION_LISTENERS).add(aListener);

		aTest1.set(NAME, "TEST1");
		aTest2.set(NAME, "TEST2");

		assertEquals("TEST1", rRelationTarget);

		aTest1.set(NAME, "TEST1A");
		assertEquals("TEST1A", rRelationTarget);

		aTest1.deleteRelation(NAME);
		assertNull(rRelationTarget);

		aTest1.get(ListenerTypes.RELATION_LISTENERS).remove(aListener);
		assertTrue(
			aTest1.get(ListenerTypes.RELATION_LISTENERS).getEventHandlerCount() ==
			0);
	}

	/***************************************
	 * Test relation listener on a particular relation.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testRelationOnUpdateAndChangeListener()
	{
		RelatedObject     o = new RelatedObject();
		Relation<Integer> r = o.set(ORDINAL, 0);

		r.onUpdate(i -> o.set(COUNT, o.get(COUNT) + 1));
		r.onChange(i -> o.set(SIZE, o.get(SIZE) + 1));

		o.set(ORDINAL, 1);
		assertEquals(Integer.valueOf(1), o.get(COUNT));
		assertEquals(Integer.valueOf(1), o.get(SIZE));
		o.set(ORDINAL, 2);
		assertEquals(Integer.valueOf(2), o.get(COUNT));
		assertEquals(Integer.valueOf(2), o.get(SIZE));
		o.set(ORDINAL, 2);
		assertEquals(Integer.valueOf(3), o.get(COUNT));
		assertEquals(Integer.valueOf(2), o.get(SIZE));
		o.deleteRelation(ORDINAL);
		assertEquals(Integer.valueOf(3), o.get(COUNT));
		assertEquals(Integer.valueOf(2), o.get(SIZE));
	}

	/***************************************
	 * Test relation listener on a particular relation.
	 */
	@Test
	public void testRelationUpdateListener()
	{
		RelatedObject aTest1 = new RelatedObject();
		RelatedObject aTest2 = new RelatedObject();

		TestListener<String> aListener = new TestListener<String>();

		aTest1.set(NAME, null).addUpdateListener(aListener);
		aTest1.set(NAME, "TEST1");
		assertEquals("TEST1", rRelationTarget);
		aTest1.getRelation(NAME)
			  .get(ListenerTypes.RELATION_UPDATE_LISTENERS)
			  .remove(aListener);
		aTest1.set(NAME, "TEST1X");
		assertEquals("TEST1", rRelationTarget);

		aTest2.set(NAME, null).addUpdateListener(aListener);
		aTest2.set(NAME, "TEST2");
		assertEquals("TEST2", rRelationTarget);
	}

	/***************************************
	 * Test relation listener on a relation type.
	 */
	@Test
	public void testTypeRelationListener()
	{
		RelatedObject aTest1 = new RelatedObject();
		RelatedObject aTest2 = new RelatedObject();

		TestListener<String> aListener = new TestListener<>();

		NAME.addTypeListener(aListener);

		aTest1.set(NAME, "TEST1");
		assertEquals("TEST1", rRelationTarget);

		aTest2.set(NAME, "TEST2");
		assertEquals("TEST2", rRelationTarget);

		aTest1.set(NAME, "TEST1A");
		assertEquals("TEST1A", rRelationTarget);

		aTest2.set(NAME, "TEST2A");
		assertEquals("TEST2A", rRelationTarget);

		aTest1.deleteRelation(NAME);
		assertNull(rRelationTarget);

		rRelationTarget = "";
		aTest2.deleteRelation(NAME);
		assertNull(rRelationTarget);

		EventDispatcher<RelationEvent<?>> rTypeEventDispatcher =
			NAME.get(ListenerTypes.RELATION_TYPE_LISTENERS);

		rTypeEventDispatcher.remove(aListener);

		// disabled because of inconsistent behavior on OpenJDK
//		assertTrue(rTypeEventDispatcher.getEventHandlerCount() == 0);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A test event listener
	 */
	class TestListener<T> implements EventHandler<RelationEvent<T>>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see EventHandler#handleEvent(de.esoco.lib.event.Event)
		 */
		@Override
		public void handleEvent(RelationEvent<T> rEvent)
		{
			switch (rEvent.getType())
			{
				case ADD:
					rRelationTarget = rEvent.getElement().getTarget();
					break;

				case REMOVE:
					rRelationTarget = null;
					break;

				case UPDATE:
					rRelationTarget = rEvent.getUpdateValue();
					break;

				default:
					assertFalse("Unknown relation event: " + rEvent, true);
					break;
			}
		}
	}
}
