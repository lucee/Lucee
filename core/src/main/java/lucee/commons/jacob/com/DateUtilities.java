/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 * Get more information about JACOB at http://sourceforge.net/projects/jacob-project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package lucee.commons.jacob.com;

import java.util.Calendar;
import java.util.Date;

/**
 * java / windows date conversion utilities
 *
 * @author joe
 *
 */
public class DateUtilities {

	/**
	 * converts a windows time to a Java Date Object
	 *
	 * @param comTime time in windows format
	 * @return Date object representing the windows time as specified in comTime
	 */
	static public Date convertWindowsTimeToDate(double comTime) {
		return new Date(convertWindowsTimeToMilliseconds(comTime));
	}

	/**
	 * Convert a COM time from functions Date(), Time(), Now() to a Java time (milliseconds). Visual
	 * Basic time values are based to 30.12.1899, Java time values are based to 1.1.1970 (= 0
	 * milliseconds). The difference is added to the Visual Basic value to get the corresponding Java
	 * value. The Visual Basic double value reads: &lt;day count delta since 30.12.1899&gt;. &lt;1 day
	 * percentage fraction &gt;, e.g. "38100.6453" means: 38100 days since 30.12.1899 plus (24 hours *
	 * 0.6453). Example usage: <code>Date javaDate = new Date(toMilliseconds (vbDate));</code>.
	 *
	 * @param comTime COM time.
	 * @return Java time.
	 */
	static public long convertWindowsTimeToMilliseconds(double comTime) {
		long result = 0;

		// code from jacobgen:
		comTime = comTime - 25569D;
		Calendar cal = Calendar.getInstance();
		result = Math.round(86400000L * comTime) - cal.get(Calendar.ZONE_OFFSET);
		cal.setTime(new Date(result));
		result -= cal.get(Calendar.DST_OFFSET);

		return result;
	}// convertWindowsTimeToMilliseconds()

	/**
	 * converts a java date to a windows time object (is this timezone safe?)
	 *
	 * @param javaDate the java date to be converted to windows time
	 * @return the double representing the date in a form windows understands
	 */
	static public double convertDateToWindowsTime(Date javaDate) {
		if (javaDate == null) {
			throw new IllegalArgumentException("cannot convert null to windows time");
		}
		return convertMillisecondsToWindowsTime(javaDate.getTime());
	}

	/**
	 * Convert a Java time to a COM time.
	 *
	 * @param milliseconds Java time.
	 * @return COM time.
	 */
	static public double convertMillisecondsToWindowsTime(long milliseconds) {
		double result = 0.0;

		// code from jacobgen:
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(milliseconds);
		milliseconds += (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)); // add GMT offset
		result = (milliseconds / 86400000D) + 25569D;

		return result;
	}// convertMillisecondsToWindowsTime()
}
