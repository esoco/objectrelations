//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import org.obrel.type.StandardTypes;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * Performs test of relation performance.
 *
 * @author eso
 */
public class PerformanceTest
{
	//~ Static fields/initializers ---------------------------------------------

	private static final int TEST_COUNT = 1000000;

	private static final RelationType<Integer> VALUE = newType();

	static
	{
		RelationTypes.init(PerformanceTest.class);
	}

	//~ Instance fields --------------------------------------------------------

	/** Test name. */
	@Rule
	public TestName		  aTestName = new TestName();
	private long		  nTime;

	private long nPrevTime;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Test the performance of simple attributes.
	 */
//	@Test
	public void childAttr()
	{
		TestObject to = new TestObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			to.addChild(new TestObject());
		}

		printProfiling("JAVA");

		RelatedObject ro = new RelatedObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			ro.get(StandardTypes.CHILDREN).add(new RelatedObject());
		}

		printProfiling("OBRL");
	}

	/***************************************
	 * Test the performance of simple attributes.
	 */
//	@Test
	public void intAttr()
	{
		TestObject to = new TestObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			to.setValue(i);
			to.getValue();
		}

		printProfiling("JAVA");

		RelatedObject ro = new RelatedObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			ro.set(VALUE, i);
			ro.get(VALUE);
		}

		printProfiling("OBRL");
	}

	/***************************************
	 * Starts the profiling of the current test methods.
	 */
	@Before
	public void startProfiling()
	{
		nTime = System.currentTimeMillis();
	}

	/***************************************
	 * Test the performance of simple attributes.
	 */
//	@Test
	public void stringAttr()
	{
		TestObject to = new TestObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			to.setName("Test" + i);
			to.getName();
		}

		printProfiling("JAVA");

		RelatedObject ro = new RelatedObject();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			ro.set(StandardTypes.NAME, "Test" + i);
			ro.get(StandardTypes.NAME);
		}

		printProfiling("OBRL");
	}

	/***************************************
	 * Prints profiling information for a certain test run.
	 *
	 * @param sName The name of the test run
	 */
	@SuppressWarnings("boxing")
	private void printProfiling(String sName)
	{
		nTime = System.currentTimeMillis() - nTime;

		System.out.printf("%s %s: %d.%03d",
						  aTestName.getMethodName(),
						  sName,
						  nTime / 1000,
						  nTime % 1000);

		if (nPrevTime != 0)
		{
			System.out.printf(" (%d %%)", nTime * 100 / nPrevTime);
		}

		System.out.printf("\n");

		nPrevTime = nTime;
		nTime     = System.currentTimeMillis();
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A standard java object for performance comparisons.
	 *
	 * @author eso
	 */
	static class TestObject
	{
		//~ Instance fields ----------------------------------------------------

		private int				 nValue;
		private String			 sName;
		private List<TestObject> aChildren = new ArrayList<>();

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Adds a child.
		 *
		 * @param rChild The child
		 */
		public final void addChild(TestObject rChild)
		{
			aChildren.add(rChild);
		}

		/***************************************
		 * Returns the children.
		 *
		 * @return The children
		 */
		public final List<TestObject> getChildren()
		{
			return aChildren;
		}

		/***************************************
		 * Returns the name.
		 *
		 * @return The name
		 */
		public final String getName()
		{
			return sName;
		}

		/***************************************
		 * Returns the value value.
		 *
		 * @return The value value
		 */
		public final int getValue()
		{
			return nValue;
		}

		/***************************************
		 * Removes a child.
		 *
		 * @param rChild The child
		 */
		public final void removeChild(TestObject rChild)
		{
			aChildren.remove(rChild);
		}

		/***************************************
		 * Sets the name.
		 *
		 * @param sName The name
		 */
		public final void setName(String sName)
		{
			this.sName = sName;
		}

		/***************************************
		 * Sets the value.
		 *
		 * @param nValue The value
		 */
		public final void setValue(int nValue)
		{
			this.nValue = nValue;
		}
	}
}
