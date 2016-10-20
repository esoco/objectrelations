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


/********************************************************************
 * A datatype that represent time and date periods and implements the
 * corresponding date calculations.
 */
public class Period implements Serializable
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the available period units. The constant {@link #NONE} can
	 * not be used to created new period instances, instead the period constant
	 * {@link Period#NONE} must be used.
	 */
	public enum Unit
	{
		MILLISECOND(Calendar.MILLISECOND, 1), SECOND(Calendar.SECOND, 1000L),
		MINUTE(Calendar.MINUTE, 60L * 1000),
		HOUR(Calendar.HOUR_OF_DAY, 60L * 60 * 1000),
		DAY(Calendar.DAY_OF_MONTH, 24L * 60 * 60 * 1000),
		WEEK(Calendar.WEEK_OF_YEAR, 7L * 24 * 60 * 60 * 1000),
		MONTH(Calendar.MONTH, 30L * 24 * 60 * 60 * 1000),
		YEAR(Calendar.YEAR, 365 * 24 * 60 * 60 * 1000),
		NONE(-1, -1)
		{
			@Override
			public Date calculateDate(Date rDate, int nFieldAdd, int nDayAdd)
			{
				return null;
			}
		};

		//~ Static fields/initializers -----------------------------------------

		/** A set containing typical time units */
		public static Set<Unit> TIME_UNITS =
			EnumSet.of(MILLISECOND, SECOND, MINUTE, HOUR);

		/** A set containing typical date units */
		public static Set<Unit> DATE_UNITS = EnumSet.of(DAY, WEEK, MONTH, YEAR);

		//~ Instance fields ----------------------------------------------------

		private final int nCalendarField;

		private long nMilliseconds;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance with the values that are needed to calculate
		 * next or previous dates for this period.
		 *
		 * @param nCalendarField The calendar field to modify for calculations
		 * @param nMilliseconds  The number of milliseconds in one count of this
		 *                       unit
		 */
		private Unit(int nCalendarField, long nMilliseconds)
		{
			this.nCalendarField = nCalendarField;
			this.nMilliseconds  = nMilliseconds;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Calculates a new date for certain period parameters.
		 *
		 * @param  rDate     The date to base the calculation on
		 * @param  nFieldAdd The value to add to the calendar field
		 * @param  nDayAdd   The value to add to the day field of the calendar
		 *
		 * @return The resulting date
		 */
		public Date calculateDate(Date rDate, int nFieldAdd, int nDayAdd)
		{
			Calendar rCalendar = Calendar.getInstance();

			rCalendar.setTime(rDate);
			rCalendar.add(nCalendarField, nFieldAdd);

			if (nDayAdd != 0 && DATE_UNITS.contains(this))
			{
				rCalendar.add(Calendar.DAY_OF_MONTH, nDayAdd);
			}

			return rCalendar.getTime();
		}

		/***************************************
		 * Returns the calendar field that corresponds with this unit.
		 *
		 * @return The calendar field
		 */
		public int getCalendarField()
		{
			return nCalendarField;
		}

		/***************************************
		 * Returns the milliseconds that one count of this unit has. For date
		 * units with varying size like months or years the returned value will
		 * be an average or approximation (e.g. 30 or 365 days). To perform
		 * exact calculations with these units a {@link Calendar} instance must
		 * be used.
		 *
		 * @return The milliseconds of one unit
		 */
		public long getMilliseconds()
		{
			return nMilliseconds;
		}
	}

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A constant for no period. This is a singleton instance that is restored
	 * on deserialization. Therefore it is safe to use it in instance
	 * comparisons.
	 */
	public static final Period NONE = new Period();

	/** Constant for a one-hour period. */
	public static final Period HOURLY = new Period(1, Unit.HOUR);

	/** Constant for a one-day period. */
	public static final Period DAYLY = new Period(1, Unit.DAY);

	/** Constant for a one-week period. */
	public static final Period WEEKLY = new Period(1, Unit.WEEK);

	/** Constant for a one-month period. */
	public static final Period MONTHLY = new Period(1, Unit.MONTH);

	/** Constant for a three-month period. */
	public static final Period QUARTERLY = new Period(3, Unit.MONTH);

	/** Constant for a six-month period. */
	public static final Period HALF_YEARLY = new Period(6, Unit.MONTH);

	/** Constant for a one-year period. */
	public static final Period YEARLY = new Period(1, Unit.YEAR);

	static
	{
		Conversions.registerStringConversion(Period.class,
			new StringConversion<Period>(Period.class)
			{
				@Override
				public Period invert(String sValue)
				{
					return valueOf(sValue);
				}
			});
	}

	//~ Instance fields --------------------------------------------------------

	private int  nCount;
	private Unit eUnit;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param  nCount The size of this period in counts of the given unit
	 * @param  eUnit  The period unit
	 *
	 * @throws IllegalArgumentException If the count is &lt;= 0 or the unit is
	 *                                  {@link Unit#NONE} (which is reserved for
	 *                                  internal use)
	 */
	public Period(int nCount, Unit eUnit)
	{
		if (nCount <= 0 || eUnit == Unit.NONE)
		{
			throw new IllegalArgumentException("Invalid period parameters: " +
											   nCount + "," + eUnit);
		}

		this.nCount = nCount;
		this.eUnit  = eUnit;
	}

	/***************************************
	 * Internal constructor for the singleton instance {@link #NONE}.
	 */
	private Period()
	{
		nCount = 0;
		eUnit  = Unit.NONE;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Parses an instance from a string.
	 *
	 * @param  sPeriod The string to parse
	 *
	 * @return A new period instance (or the singleton instance {@link #NONE})
	 *
	 * @throws IllegalArgumentException If the input string has the wrong format
	 *                                  or contains an invalid unit
	 * @throws NumberFormatException    If the count part of the input string is
	 *                                  no valid integer
	 */
	public static Period valueOf(String sPeriod)
	{
		Period rResult;

		if (Unit.NONE.name().equals(sPeriod))
		{
			rResult = NONE;
		}
		else
		{
			String[] rParts = sPeriod.split("\\.");

			if (rParts.length != 2)
			{
				throw new IllegalArgumentException("Invalid period string: " +
												   sPeriod);
			}

			try
			{
				int  nCount = Integer.parseInt(rParts[0]);
				Unit eUnit  = Unit.valueOf(rParts[1]);

				rResult = eUnit != Unit.NONE ? new Period(nCount, eUnit) : NONE;
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Unparseable period string: " +
												   sPeriod,
												   e);
			}
		}

		return rResult;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Calculates the last date of this period relative to a certain start date.
	 * This is currently only supported for {@link Unit#DATE_UNITS} where the
	 * calendar day will be adjusted to the corresponding period end.
	 *
	 * @param  rStartDate The start date to calculate the period end date for
	 *
	 * @return The resulting date
	 */
	public Date endDate(Date rStartDate)
	{
		return eUnit.calculateDate(rStartDate, nCount, -1);
	}

	/***************************************
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object rObject)
	{
		if (this == rObject)
		{
			return true;
		}

		if (rObject == null || getClass() != rObject.getClass())
		{
			return false;
		}

		Period rOther = (Period) rObject;

		return eUnit == rOther.eUnit && nCount == rOther.nCount;
	}

	/***************************************
	 * Returns the period count.
	 *
	 * @return The period count
	 */
	public final int getCount()
	{
		return nCount;
	}

	/***************************************
	 * Returns the period unit.
	 *
	 * @return The period value
	 */
	public final Unit getUnit()
	{
		return eUnit;
	}

	/***************************************
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return 31 * (eUnit == null ? 0 : eUnit.hashCode()) + nCount;
	}

	/***************************************
	 * Calculates the next date according to this period.
	 *
	 * @param  rDate The date to calculate the next period date of
	 *
	 * @return The resulting date
	 */
	public Date nextDate(Date rDate)
	{
		return eUnit.calculateDate(rDate, nCount, 0);
	}

	/***************************************
	 * Calculates the previous date according to this period.
	 *
	 * @param  rDate The date to calculate the previous period date of
	 *
	 * @return The resulting date
	 */
	public Date previousDate(Date rDate)
	{
		return eUnit.calculateDate(rDate, -nCount, 0);
	}

	/***************************************
	 * Calculates the last date of this period relative to a certain start date.
	 * This is currently only supported for {@link Unit#DATE_UNITS} where the
	 * calendar day will be adjusted to the corresponding period start.
	 *
	 * @param  rEndDate The start date to calculate the period end date for
	 *
	 * @return The resulting date
	 */
	public Date startDate(Date rEndDate)
	{
		return eUnit.calculateDate(rEndDate, -nCount, 1);
	}

	/***************************************
	 * Returns a string representation of this instance that can be parsed by
	 * the {@link #valueOf(String)} method.
	 *
	 * @return A parseable string
	 */
	@Override
	public String toString()
	{
		return eUnit != Unit.NONE ? nCount + "." + eUnit : Unit.NONE.name();
	}

	/***************************************
	 * Allows subclasses to set the period count.
	 *
	 * @param rCount The period count
	 */
	protected void setCount(int rCount)
	{
		nCount = rCount;
	}

	/***************************************
	 * Allows subclasses to set the period unit.
	 *
	 * @param rUnit The period unit
	 */
	protected void setUnit(Unit rUnit)
	{
		eUnit = rUnit;
	}

	/***************************************
	 * Replaces an object with unit {@link Unit#NONE} with the singleton
	 * instance {@link #NONE}.
	 *
	 * @return The deserialized instance or the singleton instance {@link #NONE}
	 *
	 * @throws ObjectStreamException Not used
	 */
	private Object readResolve() throws ObjectStreamException
	{
		return eUnit == Unit.NONE ? NONE : this;
	}
}
