/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.format;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

public final class DateTimeFormat extends BaseFormat implements Format {

	// private final Calendar calendar;

	/**
	 * constructor of the class
	 * 
	 * @param locale
	 */
	public DateTimeFormat(Locale locale) {
		super(locale);
		// calendar=JREDateTimeUtil.newInstance(locale);
	}

	/**
	 * formats a date to a cfml date format (short)
	 * 
	 * @param date
	 * @return formated date
	 */
	@Override
	public String format(Date date) {
		return format(date, "medium");
	}

	/**
	 * formats a date to a cfml date format
	 * 
	 * @param date
	 * @param mask
	 * @return formated date as string
	 */
	@Override
	public String format(Date date, String mask) {
		return format(date, mask, null);
	}

	public String format(Date date, String mask, TimeZone tz) {
		Calendar calendar = JREDateTimeUtil.getThreadCalendar(getLocale(), tz);
		calendar.setTimeInMillis(date.getTime());

		String lcMask = StringUtil.toLowerCase(mask);
		if (lcMask.equals("short")) return getAsString(calendar, java.text.DateFormat.SHORT, tz);
		else if (lcMask.equals("medium")) return getAsString(calendar, java.text.DateFormat.MEDIUM, tz);
		else if (lcMask.equals("long")) return getAsString(calendar, java.text.DateFormat.LONG, tz);
		else if (lcMask.equals("full")) return getAsString(calendar, java.text.DateFormat.FULL, tz);

		int len = mask.length();
		int pos = 0;
		if (len == 0) return "";

		StringBuilder formated = new StringBuilder();

		for (; pos < len; pos++) {
			char c = mask.charAt(pos);
			char next = (len > pos + 1) ? mask.charAt(pos + 1) : (char) 0;
			switch (c) {

			// d: Day of month. Digits; no leading zero for single-digit days
			// dd: Day of month. Digits; leading zero for single-digit days
			// ddd: Day of week, abbreviation
			// dddd: Day of week. Full name
			case 'd':
			case 'D':
				char next2 = (len > pos + 2) ? mask.charAt(pos + 2) : (char) 0;
				char next3 = (len > pos + 3) ? mask.charAt(pos + 3) : (char) 0;

				int day = calendar.get(Calendar.DATE);
				if (next == 'd' || next == 'D') {
					if (next2 == 'd' || next2 == 'D') {
						if (next3 == 'd' || next3 == 'D') {
							formated.append(getDayOfWeekAsString(calendar.get(Calendar.DAY_OF_WEEK)));
							pos += 3;
						}
						else {
							formated.append(getDayOfWeekShortAsString(calendar.get(Calendar.DAY_OF_WEEK)));
							pos += 2;
						}
					}
					else {
						formated.append(day < 10 ? "0" + day : "" + day);
						pos++;
					}
				}
				else {
					formated.append(day);
				}
				break;

			// m: Month. Digits; no leading zero for single-digit months
			// mm: Month. Digits; leading zero for single-digit months
			// mmm: Month. abbreviation (if appropriate)
			// mmmm: Month. Full name
			case 'm':
			case 'M':
				char next_2 = (len > pos + 2) ? mask.charAt(pos + 2) : (char) 0;
				char next_3 = (len > pos + 3) ? mask.charAt(pos + 3) : (char) 0;

				int month = calendar.get(Calendar.MONTH) + 1;
				if (next == 'm' || next == 'M') {
					if (next_2 == 'm' || next_2 == 'M') {
						if (next_3 == 'm' || next_3 == 'M') {
							formated.append(getMonthAsString(month));
							pos += 3;
						}
						else {
							formated.append(getMonthShortAsString(month));
							pos += 2;
						}
					}
					else {
						formated.append(month < 10 ? "0" + month : "" + month);
						pos++;
					}
				}
				else {
					formated.append(month);
				}
				break;

			// y: Year. Last two digits; no leading zero for years less than 10
			// yy: Year. Last two digits; leading zero for years less than 10
			// yyyy: Year. Four digits
			case 'y':
			case 'Y':
				char next__2 = (len > pos + 2) ? mask.charAt(pos + 2) : (char) 0;
				char next__3 = (len > pos + 3) ? mask.charAt(pos + 3) : (char) 0;

				int year4 = calendar.get(Calendar.YEAR);
				int year2 = year4 % 100;
				if (next == 'y' || next == 'Y') {
					if ((next__2 == 'y' || next__2 == 'Y') && (next__3 == 'y' || next__3 == 'Y')) {
						formated.append(year4);
						pos += 3;
					}
					else {
						formated.append(year2 < 10 ? "0" + year2 : "" + year2);
						pos++;
					}
				}
				else {
					formated.append(year2);
				}
				break;

			// h: Hours; no leading zero for single-digit hours (12-hour clock)
			// hh: Hours; leading zero for single-digit hours. (12-hour clock)
			case 'h':
				int hour1 = calendar.get(Calendar.HOUR_OF_DAY);
				if (hour1 == 0) hour1 = 12;
				if (hour1 > 12) hour1 = hour1 - 12;
				if (next == 'h') {
					formated.append(hour1 < 10 ? "0" + hour1 : "" + hour1);
					pos++;
				}
				else {
					formated.append(hour1);
				}
				break;

			// H: Hours; no leading zero for single-digit hours (24-hour clock)
			// HH: Hours; leading zero for single-digit hours (24-hour clock)
			case 'H':
				int hour2 = calendar.get(Calendar.HOUR_OF_DAY);
				if (next == 'H') {
					formated.append(hour2 < 10 ? "0" + hour2 : "" + hour2);
					pos++;
				}
				else {
					formated.append(hour2);
				}
				break;

			// n: Minutes; no leading zero for single-digit minutes
			// nn: Minutes; leading zero for single-digit minutes
			case 'N':
			case 'n':
				int minute = calendar.get(Calendar.MINUTE);
				if (next == 'N' || next == 'n') {
					formated.append(minute < 10 ? "0" + minute : "" + minute);
					pos++;
				}
				else {
					formated.append(minute);
				}
				break;

			// s: Seconds; no leading zero for single-digit seconds
			// ss: Seconds; leading zero for single-digit seconds
			case 's':
			case 'S':
				int second = calendar.get(Calendar.SECOND);
				if (next == 'S' || next == 's') {
					formated.append(second < 10 ? "0" + second : "" + second);
					pos++;
				}
				else {
					formated.append(second);
				}
				break;

			// l: Milliseconds
			case 'l':
			case 'L':
				char nextnext = (len > pos + 2) ? mask.charAt(pos + 2) : (char) 0;

				String millis = Caster.toString(calendar.get(Calendar.MILLISECOND));
				if (next == 'L' || next == 'l') {
					if (millis.length() == 1) millis = "0" + millis;
					pos++;
				}
				if (nextnext == 'L' || nextnext == 'l') {
					if (millis.length() == 2) millis = "0" + millis;
					pos++;
				}
				formated.append(millis);

				break;

			// t: One-character time marker string, such as A or P.
			// tt: Multiple-character time marker string, such as AM or PM
			case 't':
			case 'T':
				boolean isAm = calendar.get(Calendar.HOUR_OF_DAY) < 12;
				if (next == 'T' || next == 't') {
					formated.append(isAm ? "AM" : "PM");
					pos++;
				}
				else {
					formated.append(isAm ? "A" : "P");
				}
				break;
			case 'z':
			case 'Z':
				// count next z and jump to last z (max 6)
				int start = pos;
				while ((pos + 1) < len && Character.toLowerCase(mask.charAt(pos + 1)) == 'z') {
					pos++;
					if (pos - start > 4) break;
				}
				if (pos - start > 2) formated.append(tz.getDisplayName(getLocale()));
				else formated.append(tz.getID());

				break;

			// Otherwise
			default:
				formated.append(c);
			}
		}
		return formated.toString();
	}

	private String getAsString(Calendar c, int style, TimeZone tz) {
		java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(style, style, getLocale());
		df.setTimeZone(tz);
		return df.format(c.getTime());
	}
}