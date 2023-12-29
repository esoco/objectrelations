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
package de.esoco.lib.logging;

import de.esoco.lib.expression.monad.Option;
import de.esoco.lib.text.TextUtil;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.obrel.core.RelationTypes.newOptionType;

/**
 * A class that collects performance measurements that are added through
 * {@link #measure(String, long)}.
 *
 * @author eso
 */
public class Profiler {
	/**
	 * A relation that stores an optional profiler reference.
	 */
	public static final RelationType<Option<Profiler>> PROFILER =
		newOptionType();

	static {
		RelationTypes.init(Profiler.class);
	}

	private final long creationTime = System.currentTimeMillis();

	private final Map<String, Measurement> measurements =
		new LinkedHashMap<>();

	String description;

	private long startTime = System.currentTimeMillis();

	/**
	 * Creates a new instance.
	 */
	public Profiler() {
		this(null);
	}

	/**
	 * Creates a new instance with a default description.
	 *
	 * @param description The default description of this instance
	 */
	public Profiler(String description) {
		this.description = description;
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Returns the time in milliseconds when this instance has been created.
	 *
	 * @return The creation time in milliseconds
	 */
	public final long getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns a certain result.
	 *
	 * @param description The description of the result
	 * @return The result measurement or NULL for none
	 */
	public Measurement getResult(String description) {
		return measurements.get(description);
	}

	/**
	 * Returns a mapping from descriptions to resulting measurements.
	 *
	 * @return The results
	 */
	public Map<String, Measurement> getResults() {
		return measurements;
	}

	/**
	 * Measures the time that a certain execution has taken from the last
	 * measurement until now.
	 *
	 * @param description The description of the measured execution
	 * @return The current time in milliseconds (for concatenation of
	 * measurements)
	 */
	public long measure(String description) {
		return measure(description, startTime);
	}

	/**
	 * Measures the time from a certain starting time until now.
	 *
	 * @param description The description of the measured execution
	 * @param fromTime    The starting time of the measurement in milliseconds
	 * @return The current time in milliseconds (for concatenation of
	 * measurements)
	 */
	public long measure(String description, long fromTime) {
		Measurement measurement = measurements.get(description);
		long now = System.currentTimeMillis();
		long duration = now - fromTime;

		if (measurement == null) {
			measurements.put(description, new Measurement(duration));
		} else {
			measurement.add(duration);
		}

		startTime = now;

		return now;
	}

	/**
	 * Prints all measurements to {@link System#out}.
	 *
	 * @param indent The indentation to print with
	 */
	public void printResults(String indent) {
		for (String description : measurements.keySet()) {
			System.out.printf("%sTotal time for %s: %s\n", indent, description,
				measurements.get(description));
		}
	}

	/**
	 * Prints the description of this instance, the total time since
	 * creation in
	 * seconds, and all measurements to {@link System#out}.
	 */
	@SuppressWarnings("boxing")
	public void printSummary() {
		printSummary(description + ":", "", 1);
	}

	/**
	 * Prints the given title, the total time since creation in seconds, and
	 * all
	 * measurements to {@link System#out}. If the number of elements processed
	 * is larger than 1 the time the processing of a single element took
	 * will be
	 * displayed too.
	 *
	 * @param title       The title string
	 * @param elementName The name of an element if count &gt; 1
	 * @param count       The number of elements processed (must be &gt;= 1)
	 */
	@SuppressWarnings("boxing")
	public void printSummary(String title, String elementName, long count) {
		long time = System.currentTimeMillis() - creationTime;

		String header = String.format("====== %s %s ======", title,
			TextUtil.formatDuration(time));

		System.out.println(header);

		if (count > 1) {
			String elementTime = String.format(" Time per %s: %s ",
				elementName,
				TextUtil.formatDuration(time / count));

			System.out.println(
				TextUtil.padCenter(elementTime, header.length(), '-'));
		}

		printResults("");
		System.out.printf("%s\n", header.replaceAll(".", "="));
	}

	/**
	 * A data structure for profiling measurements.
	 *
	 * @author eso
	 */
	public static class Measurement {
		private long totalTime;

		private int count;

		/**
		 * Creates a new instance.
		 */
		private Measurement(long time) {
			totalTime = time;
		}

		/**
		 * Returns the average measured time in milliseconds (i.e.
		 * {@link #getTotalTime()} / {@link #getCount()}).
		 *
		 * @return The average time
		 */
		public long getAverageTime() {
			return count > 0 ? totalTime / count : totalTime;
		}

		/**
		 * Returns the number of single measurements taken.
		 *
		 * @return The number of measurements
		 */
		public final int getCount() {
			return count;
		}

		/**
		 * Returns the total measured time in milliseconds.
		 *
		 * @return The total time
		 */
		public long getTotalTime() {
			return totalTime;
		}

		/**
		 * Returns a string representation of the measured time.
		 *
		 * @return The string representation of this measurement
		 */
		@Override
		public String toString() {
			return TextUtil.formatDuration(totalTime) + "s";
		}

		/**
		 * Adds time in milliseconds to this record.
		 *
		 * @param time The time to add
		 */
		private void add(long time) {
			totalTime += time;
			count++;
		}
	}
}
