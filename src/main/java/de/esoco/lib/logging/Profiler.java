//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
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
package de.esoco.lib.logging;

import de.esoco.lib.text.TextUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
 * A class that collects performance measurements that are added through {@link
 * #measure(String, long)}.
 *
 * @author eso
 */
public class Profiler
{
	//~ Static fields/initializers ---------------------------------------------

	/** A relation type that can be used to store a profiler reference. */
	public static final RelationType<Profiler> PROFILER = newType();

	static
	{
		RelationTypes.init(Profiler.class);
	}

	//~ Instance fields --------------------------------------------------------

	String sDescription;

	private final long nCreationTime = System.currentTimeMillis();
	private long	   nStartTime    = System.currentTimeMillis();

	private Map<String, Measurement> aMeasurements = new LinkedHashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public Profiler()
	{
		this(null);
	}

	/***************************************
	 * Creates a new instance with a default description.
	 *
	 * @param sDescription The default description of this instance
	 */
	public Profiler(String sDescription)
	{
		this.sDescription = sDescription;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the time in milliseconds when this instance has been created.
	 *
	 * @return The creation time in milliseconds
	 */
	public final long getCreationTime()
	{
		return nCreationTime;
	}

	/***************************************
	 * Returns a certain result.
	 *
	 * @param  sDescription The description of the result
	 *
	 * @return The result measurement or NULL for none
	 */
	public Measurement getResult(String sDescription)
	{
		return aMeasurements.get(sDescription);
	}

	/***************************************
	 * Returns a mapping from descriptions to resulting measurements.
	 *
	 * @return The results
	 */
	public Map<String, Measurement> getResults()
	{
		return aMeasurements;
	}

	/***************************************
	 * Measures the time that a certain execution has taken from the last
	 * measurement until now.
	 *
	 * @param  sDescription The description of the measured execution
	 *
	 * @return The current time in milliseconds (for concatenation of
	 *         measurements)
	 */
	public long measure(String sDescription)
	{
		return measure(sDescription, nStartTime);
	}

	/***************************************
	 * Measures the time from a certain starting time until now.
	 *
	 * @param  sDescription The description of the measured execution
	 * @param  nFromTime    The starting time of the measurement in milliseconds
	 *
	 * @return The current time in milliseconds (for concatenation of
	 *         measurements)
	 */
	public long measure(String sDescription, long nFromTime)
	{
		Measurement aMeasurement = aMeasurements.get(sDescription);
		long	    nNow		 = System.currentTimeMillis();
		long	    nDuration    = nNow - nFromTime;

		if (aMeasurement == null)
		{
			aMeasurements.put(sDescription, new Measurement(nDuration));
		}
		else
		{
			aMeasurement.add(nDuration);
		}

		nStartTime = nNow;

		return nNow;
	}

	/***************************************
	 * Prints all measurements to {@link System#out}.
	 *
	 * @param sIndent The indentation to print with
	 */
	public void printResults(String sIndent)
	{
		for (String sDescription : aMeasurements.keySet())
		{
			System.out.printf("%sTotal time for %s: %s\n",
							  sIndent,
							  sDescription,
							  aMeasurements.get(sDescription));
		}
	}

	/***************************************
	 * Prints the description of this instance, the total time since creation in
	 * seconds, and all measurements to {@link System#out}.
	 */
	@SuppressWarnings("boxing")
	public void printSummary()
	{
		printSummary(sDescription + ":", "", 1);
	}

	/***************************************
	 * Prints the given title, the total time since creation in seconds, and all
	 * measurements to {@link System#out}. If the number of elements processed
	 * is larger than 1 the time the processing of a single element took will be
	 * displayed too.
	 *
	 * @param sTitle       The title string
	 * @param sElementName The name of an element if nCount > 1
	 * @param nCount       The number of elements processed (must be >= 1)
	 */
	@SuppressWarnings("boxing")
	public void printSummary(String sTitle, String sElementName, long nCount)
	{
		long nTime = System.currentTimeMillis() - nCreationTime;

		String sHeader =
			String.format("====== %s %s ======",
						  sTitle,
						  TextUtil.formatDuration(nTime));

		System.out.println(sHeader);

		if (nCount > 1)
		{
			String sElementTime =
				String.format(" Time per %s: %s ",
							  sElementName,
							  TextUtil.formatDuration(nTime / nCount));

			System.out.println(TextUtil.padCenter(sElementTime,
												  sHeader.length(),
												  '-'));
		}

		printResults("");
		System.out.printf("%s\n", sHeader.replaceAll(".", "="));
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A data structure for profiling measurements.
	 *
	 * @author eso
	 */
	public static class Measurement
	{
		//~ Instance fields ----------------------------------------------------

		private long nTotalTime;
		private int  nCount;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nTime
		 */
		private Measurement(long nTime)
		{
			nTotalTime = nTime;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Returns the average measured time in milliseconds (i.e. {@link
		 * #getTotalTime()} / {@link #getCount()}).
		 *
		 * @return The average time
		 */
		public long getAverageTime()
		{
			return nCount > 0 ? nTotalTime / nCount : nTotalTime;
		}

		/***************************************
		 * Returns the number of single measurements taken.
		 *
		 * @return The number of measurements
		 */
		public final int getCount()
		{
			return nCount;
		}

		/***************************************
		 * Returns the total measured time in milliseconds.
		 *
		 * @return The total time
		 */
		public long getTotalTime()
		{
			return nTotalTime;
		}

		/***************************************
		 * Returns a string representation of the measured time.
		 *
		 * @return The string representation of this measurement
		 */
		@Override
		public String toString()
		{
			return TextUtil.formatDuration(nTotalTime) + "s";
		}

		/***************************************
		 * Adds time in milliseconds to this record.
		 *
		 * @param nTime The time to add
		 */
		private void add(long nTime)
		{
			nTotalTime += nTime;
			nCount++;
		}
	}
}
