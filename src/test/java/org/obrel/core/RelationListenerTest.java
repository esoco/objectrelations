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
package org.obrel.core;

import de.esoco.lib.event.EventHandler;

import org.junit.Test;

import org.obrel.type.StandardTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import static org.obrel.type.StandardTypes.RELATION_LISTENERS;


/********************************************************************
 * Test of relation listener functionality.
 *
 * @author eso
 */
public class RelationListenerTest implements EventHandler<RelationEvent<?>>
{
	//~ Instance fields --------------------------------------------------------

	private Object rRelationTarget;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see EventHandler#handleEvent(Event)
	 */
	@Override
	public void handleEvent(RelationEvent<?> rEvent)
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

	/***************************************
	 * Test relation listener on an object.
	 */
	@Test
	public void testObjectRelationListener()
	{
		RelatedObject aTest1 = new RelatedObject();
		RelatedObject aTest2 = new RelatedObject();

		aTest1.get(RELATION_LISTENERS).add(this);

		aTest1.set(StandardTypes.NAME, "TEST1");
		aTest2.set(StandardTypes.NAME, "TEST2");

		assertEquals("TEST1", rRelationTarget);

		aTest1.set(StandardTypes.NAME, "TEST1A");
		assertEquals("TEST1A", rRelationTarget);

		aTest1.deleteRelation(StandardTypes.NAME);
		assertNull(rRelationTarget);

		aTest1.get(RELATION_LISTENERS).remove(this);
	}

	/***************************************
	 * Test relation listener on a relation type.
	 */
	@Test
	public void testTypeRelationListener()
	{
		RelatedObject aTest1 = new RelatedObject();
		RelatedObject aTest2 = new RelatedObject();

		StandardTypes.NAME.get(RELATION_LISTENERS).add(this);

		aTest1.set(StandardTypes.NAME, "TEST1");
		assertEquals("TEST1", rRelationTarget);

		aTest2.set(StandardTypes.NAME, "TEST2");
		assertEquals("TEST2", rRelationTarget);

		aTest1.set(StandardTypes.NAME, "TEST1A");
		assertEquals("TEST1A", rRelationTarget);

		aTest2.set(StandardTypes.NAME, "TEST2A");
		assertEquals("TEST2A", rRelationTarget);

		aTest1.deleteRelation(StandardTypes.NAME);
		assertNull(rRelationTarget);

		rRelationTarget = "";
		aTest2.deleteRelation(StandardTypes.NAME);
		assertNull(rRelationTarget);

		StandardTypes.NAME.get(RELATION_LISTENERS).remove(this);
	}
}
