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
package de.esoco.lib.expression.function;

import de.esoco.lib.expression.Function;

import java.util.Calendar;
import java.util.Date;

/**
 * Contains {@link Calendar}-specific functions and predicates.
 *
 * @author eso
 */
public class CalendarFunctions {

	private static final int[] ORDERED_CALENDAR_FIELDS =
		new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE,
			Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH, Calendar.MONTH,
			Calendar.YEAR, Calendar.ERA };

	private static final Function<Calendar, Calendar> CLEAR_TIME =
		new AbstractFunction<Calendar, Calendar>("clearTime") {
			@Override
			public Calendar evaluate(Calendar calendar) {
				return clearTime(calendar);
			}
		};

	private static final Function<Calendar, Date> GET_TIME =
		new AbstractFunction<Calendar, Date>("getTime") {
			@Override
			public Date evaluate(Calendar calendar) {
				return calendar.getTime();
			}
		};

	/**
	 * Private, only static use.
	 */
	private CalendarFunctions() {
	}

	/**
	 * Returns a constant function that clears the time part of a calendar
	 * instance. The input calendar will be modified by the invocation of this
	 * method. It will also be returned to allow method concatenation.
	 *
	 * @return The constant function
	 */
	public static Function<Calendar, Calendar> clearTime() {
		return CLEAR_TIME;
	}

	/**
	 * Clears all time fields of a calendar by setting them to zero.
	 *
	 * @param calendar The calendar to clear the time fields of
	 * @return The calendar for concatenation
	 */
	public static Calendar clearTime(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar;
	}

	/**
	 * Returns a constant function that invokes {@link Calendar#getTime()}.
	 *
	 * @return The constant function
	 */
	public static Function<Calendar, Date> getTime() {
		return GET_TIME;
	}

	/**
	 * Checks whether a certain {@link Calendar} field represents a time field.
	 *
	 * @param calendarField The calendar field to check
	 * @return TRUE for a time field, FALSE for a date field
	 */
	public static boolean isTimeField(int calendarField) {
		return calendarField == Calendar.MILLISECOND ||
			calendarField == Calendar.SECOND ||
			calendarField == Calendar.MINUTE ||
			calendarField == Calendar.HOUR ||
			calendarField == Calendar.HOUR_OF_DAY ||
			calendarField == Calendar.AM_PM;
	}

	/**
	 * Checks whether the minimum value of a certain {@link Calendar} field is
	 * zero or one.
	 *
	 * @param calendarField The calendar field to check
	 * @return TRUE for a zero-based field, FALSE for fields that start with 1
	 */
	public static boolean isZeroBased(int calendarField) {
		return calendarField != Calendar.DAY_OF_WEEK &&
			calendarField != Calendar.DAY_OF_MONTH &&
			calendarField != Calendar.WEEK_OF_MONTH &&
			calendarField != Calendar.WEEK_OF_YEAR;
	}

	/**
	 * Resets all fields of a calendar that are below a certain field. The
	 * order
	 * of fields are determined ascending from {@link Calendar#MILLISECOND}
	 * which is the lowest field to {@link Calendar#ERA} as the highest.
	 *
	 * @param calendarField The first calendar field not to be cleared
	 * @param calendar      The calendar to clear the fields of
	 * @param maximum       TRUE to reset fields to their maximum value, FALSE
	 *                      for the minimum
	 * @return The calendar for concatenation
	 */
	public static Calendar resetBelow(int calendarField, Calendar calendar,
		boolean maximum) {
		int fieldIndex = 0;

		while (fieldIndex < ORDERED_CALENDAR_FIELDS.length &&
			ORDERED_CALENDAR_FIELDS[fieldIndex] != calendarField) {
			fieldIndex++;
		}

		if (fieldIndex < ORDERED_CALENDAR_FIELDS.length) {
			// iterate in reverse order to apply the ACTUAL min/max correctly
			for (int i = fieldIndex - 1; i >= 0; i--) {
				int field = ORDERED_CALENDAR_FIELDS[i];

				calendar.set(field, maximum ?
				                    calendar.getActualMaximum(field) :
				                    calendar.getActualMinimum(field));
			}
		}

		return calendar;
	}

	/**
	 * Returns a date value for the current day with the time values set to
	 * midnight, i.e. zero (by means of {@link #clearTime()}).
	 *
	 * @return The date of today midnight
	 */
	public static Date today() {
		Calendar today = Calendar.getInstance();

		today.setTimeInMillis(System.currentTimeMillis());

		return clearTime(today).getTime();
	}
}
