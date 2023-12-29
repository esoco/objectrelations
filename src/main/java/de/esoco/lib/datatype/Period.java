//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.lib.datatype;

import de.esoco.lib.expression.Conversions;
import de.esoco.lib.expression.Conversions.StringConversion;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * A datatype that represent time and date periods and implements the
 * corresponding date calculations.
 */
public class Period implements Serializable {

	/**
	 * Enumeration of the available period units. The constant {@link #NONE}
	 * can
	 * not be used to created new period instances, instead the period constant
	 * {@link Period#NONE} must be used.
	 */
	public enum Unit {
		MILLISECOND(Calendar.MILLISECOND, 1), SECOND(Calendar.SECOND, 1000L),
		MINUTE(Calendar.MINUTE, 60L * 1000),
		HOUR(Calendar.HOUR_OF_DAY, 60L * 60 * 1000),
		DAY(Calendar.DAY_OF_MONTH, 24L * 60 * 60 * 1000),
		WEEK(Calendar.WEEK_OF_YEAR, 7L * 24 * 60 * 60 * 1000),
		MONTH(Calendar.MONTH, 30L * 24 * 60 * 60 * 1000),
		YEAR(Calendar.YEAR, 365L * 24 * 60 * 60 * 1000), NONE(-1, -1) {
			@Override
			public Date calculateDate(Date date, int fieldAdd, int dayAdd) {
				return null;
			}
		};

		/**
		 * A set containing typical time units
		 */
		public static Set<Unit> TIME_UNITS =
			EnumSet.of(MILLISECOND, SECOND, MINUTE, HOUR);

		/**
		 * A set containing typical date units
		 */
		public static Set<Unit> DATE_UNITS = EnumSet.of(DAY, WEEK, MONTH,
			YEAR);

		private final int calendarField;

		private final long milliseconds;

		/**
		 * Creates a new instance with the values that are needed to calculate
		 * next or previous dates for this period.
		 *
		 * @param calendarField The calendar field to modify for calculations
		 * @param milliseconds  The number of milliseconds in one count of this
		 *                      unit
		 */
		Unit(int calendarField, long milliseconds) {
			this.calendarField = calendarField;
			this.milliseconds = milliseconds;
		}

		/**
		 * Calculates a new date for certain period parameters.
		 *
		 * @param date     The date to base the calculation on
		 * @param fieldAdd The value to add to the calendar field
		 * @param dayAdd   The value to add to the day field of the calendar
		 * @return The resulting date
		 */
		public Date calculateDate(Date date, int fieldAdd, int dayAdd) {
			Calendar calendar = Calendar.getInstance();

			calendar.setTime(date);
			calendar.add(calendarField, fieldAdd);

			if (dayAdd != 0 && DATE_UNITS.contains(this)) {
				calendar.add(Calendar.DAY_OF_MONTH, dayAdd);
			}

			return calendar.getTime();
		}

		/**
		 * Returns the calendar field that corresponds with this unit.
		 *
		 * @return The calendar field
		 */
		public int getCalendarField() {
			return calendarField;
		}

		/**
		 * Returns the milliseconds that one count of this unit has. For date
		 * units with varying size like months or years the returned value will
		 * be an average or approximation (e.g. 30 or 365 days). To perform
		 * exact calculations with these units a {@link Calendar} instance must
		 * be used.
		 *
		 * @return The milliseconds of one unit
		 */
		public long getMilliseconds() {
			return milliseconds;
		}
	}

	/**
	 * A constant for no period. This is a singleton instance that is restored
	 * on deserialization. Therefore it is safe to use it in instance
	 * comparisons.
	 */
	public static final Period NONE = new Period();

	/**
	 * Constant for a one-hour period.
	 */
	public static final Period HOURLY = new Period(1, Unit.HOUR);

	/**
	 * Constant for a one-day period.
	 */
	public static final Period DAYLY = new Period(1, Unit.DAY);

	/**
	 * Constant for a one-week period.
	 */
	public static final Period WEEKLY = new Period(1, Unit.WEEK);

	/**
	 * Constant for a one-month period.
	 */
	public static final Period MONTHLY = new Period(1, Unit.MONTH);

	/**
	 * Constant for a three-month period.
	 */
	public static final Period QUARTERLY = new Period(3, Unit.MONTH);

	/**
	 * Constant for a six-month period.
	 */
	public static final Period HALF_YEARLY = new Period(6, Unit.MONTH);

	/**
	 * Constant for a one-year period.
	 */
	public static final Period YEARLY = new Period(1, Unit.YEAR);

	private static final long serialVersionUID = 1L;

	static {
		Conversions.registerStringConversion(Period.class,
			new StringConversion<Period>(Period.class) {
				@Override
				public Period invert(String value) {
					return valueOf(value);
				}
			});
	}

	private final int count;

	private final Unit unit;

	/**
	 * Creates a new instance.
	 *
	 * @param count The size of this period in counts of the given unit
	 * @param unit  The period unit
	 * @throws IllegalArgumentException If the count is &lt;= 0 or the unit is
	 *                                  {@link Unit#NONE} (which is reserved
	 *                                  for
	 *                                  internal use)
	 */
	public Period(int count, Unit unit) {
		if (count <= 0 || unit == Unit.NONE) {
			throw new IllegalArgumentException(
				"Invalid period parameters: " + count + "," + unit);
		}

		this.count = count;
		this.unit = unit;
	}

	/**
	 * Internal constructor for the singleton instance {@link #NONE}.
	 */
	private Period() {
		count = 0;
		unit = Unit.NONE;
	}

	/**
	 * Parses an instance from a string.
	 *
	 * @param period The string to parse
	 * @return A new period instance (or the singleton instance {@link #NONE})
	 * @throws IllegalArgumentException If the input string has the wrong
	 * format
	 *                                  or contains an invalid unit
	 * @throws NumberFormatException    If the count part of the input
	 * string is
	 *                                  no valid integer
	 */
	public static Period valueOf(String period) {
		Period result;

		if (Unit.NONE.name().equals(period)) {
			result = NONE;
		} else {
			String[] parts = period.split("\\.");

			if (parts.length != 2) {
				throw new IllegalArgumentException(
					"Invalid period string: " + period);
			}

			try {
				int count = Integer.parseInt(parts[0]);
				Unit unit = Unit.valueOf(parts[1]);

				result = unit != Unit.NONE ? new Period(count, unit) : NONE;
			} catch (Exception e) {
				throw new IllegalArgumentException(
					"Unparseable period string: " + period, e);
			}
		}

		return result;
	}

	/**
	 * Calculates the last date of this period relative to a certain start
	 * date.
	 * This is currently only supported for {@link Unit#DATE_UNITS} where the
	 * calendar day will be adjusted to the corresponding period end.
	 *
	 * @param startDate The start date to calculate the period end date for
	 * @return The resulting date
	 */
	public Date endDate(Date startDate) {
		return unit.calculateDate(startDate, count, -1);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		Period other = (Period) object;

		return unit == other.unit && count == other.count;
	}

	/**
	 * Returns the period count.
	 *
	 * @return The period count
	 */
	public final int getCount() {
		return count;
	}

	/**
	 * Returns the period unit.
	 *
	 * @return The period value
	 */
	public final Unit getUnit() {
		return unit;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * (unit == null ? 0 : unit.hashCode()) + count;
	}

	/**
	 * Calculates the next date according to this period.
	 *
	 * @param date The date to calculate the next period date of
	 * @return The resulting date
	 */
	public Date nextDate(Date date) {
		return unit.calculateDate(date, count, 0);
	}

	/**
	 * Calculates the previous date according to this period.
	 *
	 * @param date The date to calculate the previous period date of
	 * @return The resulting date
	 */
	public Date previousDate(Date date) {
		return unit.calculateDate(date, -count, 0);
	}

	/**
	 * Calculates the last date of this period relative to a certain start
	 * date.
	 * This is currently only supported for {@link Unit#DATE_UNITS} where the
	 * calendar day will be adjusted to the corresponding period start.
	 *
	 * @param endDate The start date to calculate the period end date for
	 * @return The resulting date
	 */
	public Date startDate(Date endDate) {
		return unit.calculateDate(endDate, -count, 1);
	}

	/**
	 * Returns a string representation of this instance that can be parsed by
	 * the {@link #valueOf(String)} method.
	 *
	 * @return A parseable string
	 */
	@Override
	public String toString() {
		return unit != Unit.NONE ? count + "." + unit : Unit.NONE.name();
	}

	/**
	 * Allows subclasses to set the period count.
	 *
	 * @param count The period count
	 */
	protected void setCount(int count) {
		count = count;
	}

	/**
	 * Allows subclasses to set the period unit.
	 *
	 * @param unit The period unit
	 */
	protected void setUnit(Unit unit) {
		unit = unit;
	}

	/**
	 * Replaces an object with unit {@link Unit#NONE} with the singleton
	 * instance {@link #NONE}.
	 *
	 * @return The deserialized instance or the singleton instance
	 * {@link #NONE}
	 * @throws ObjectStreamException Not used
	 */
	private Object readResolve() throws ObjectStreamException {
		return unit == Unit.NONE ? NONE : this;
	}
}
