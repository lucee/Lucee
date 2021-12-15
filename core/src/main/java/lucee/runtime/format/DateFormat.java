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
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

public final class DateFormat extends BaseFormat implements Format {

	/**
	 * constructor of the class
	 * 
	 * @param locale
	 */
	public DateFormat(Locale locale) {
		super(locale);
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
		return format(date.getTime(), mask, tz);
	}

	public String format(long time, String mask, TimeZone tz) {
		TimeZone def = null;
		try {
			tz = ThreadLocalPageContext.getTimeZone(tz);
			Calendar calendar = JREDateTimeUtil.getThreadCalendar(getLocale(), tz);
			calendar.setTimeInMillis(time);

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
				case 'z': {
					int count = 1;
					while (mask.length() > pos + 1 && mask.charAt(pos + 1) == 'z') {
						pos++;
						count++;
					}
					formated.append(z(time, tz, count));
				}
					break;
				// RFC 822 TimeZone
				case 'Z': {
					while (mask.length() > pos + 1 && mask.charAt(pos + 1) == 'Z') {
						pos++;
					}
					formated.append(Z(time, tz));
				}
					break;
				// ISO 8601 TimeZone
				case 'X': {
					int count = 1;
					while (mask.length() > pos + 1 && mask.charAt(pos + 1) == 'X') {
						pos++;
						count++;
					}
					if (def == null) def = TimeZone.getDefault();
					TimeZone.setDefault(TimeZone.getTimeZone("CET"));
					formated.append(X(time, tz, count));
				}
					break;

				// g: Era designator
				// gg: Era designator

				case 'g':
				case 'G':
					String era = toEra(calendar.get(Calendar.ERA), "");
					while (mask.length() > pos + 1 && Character.toLowerCase(mask.charAt(pos + 1)) == 'g') {
						pos++;
					}
					formated.append(era);
					break;

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

				// w: Week of the year; no leading zero for single-digit week
				// ww: Week of the month; leading zero for single-digit week
				// W: Week of the month; no leading zero for single-digit week
				// WW: Month. Week of the month; leading zero for single-digit week

				case 'w':
				case 'W':
					int week = 0;
					if (c == 'W' || next == 'W') week = calendar.get(Calendar.WEEK_OF_MONTH);
					if (c == 'w' || next == 'w') week = calendar.get(Calendar.WEEK_OF_YEAR);

					char next_1 = (len > pos + 1) ? mask.charAt(pos + 1) : (char) 0;
					if (next == 'w' || next == 'W' && next_1 == 'w' || next_1 == 'W') {
						formated.append(week < 10 ? "0" + week : "" + week);
						pos++;
					}
					else {
						formated.append(week);
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
						else if ((next__2 == 'y' || next__2 == 'Y')) {
							formated.append(year4);
							pos += 2;
						}
						else {
							formated.append(year2 < 10 ? "0" + year2 : "" + year2);
							pos++;
						}
					}
					else {
						formated.append(year4);
					}
					break;

				// Otherwise
				default:
					formated.append(c);
				}
			}
			return formated.toString();
		}
		finally {
			if (def != null) TimeZone.setDefault(def);
		}
	}

	private String toEra(int era, String defaultValue) {
		if (GregorianCalendar.AD == era) return "AD";
		if (GregorianCalendar.BC == era) return "BC";
		return defaultValue;
	}

	public static Object X(long time, TimeZone tz, int count) {
		if (tz.equals(TimeZoneConstants.UTC)) return "Z";
		String res = Z(time, tz);
		if (count == 1) return res.substring(0, 3);
		if (count == 2) return res;

		// String h=(res.charAt(1)=='0')? h=res.substring(2, 3):res.substring(1, 3);
		return res.substring(0, 1) + res.substring(1, 3) + ":" + res.substring(3);
	}

	public static String z(long time, TimeZone tz, int count) {
		Calendar c = Calendar.getInstance(tz, Locale.US);
		c.setTimeInMillis(time);

		boolean daylight = c.get(Calendar.DST_OFFSET) != 0;
		int style = (count < 4 ? TimeZone.SHORT : TimeZone.LONG);

		return tz.getDisplayName(daylight, style, Locale.US);
	}

	public static String Z(long time, TimeZone tz) {
		StringBuilder sb = new StringBuilder();
		Calendar c = Calendar.getInstance(tz, Locale.US);
		c.setTimeInMillis(time);
		int value = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000;
		int width = 4;
		if (value >= 0) sb.append('+');
		else width++;

		int num = (value / 60) * 100 + (value % 60);
		sprintf0d(sb, num, width);
		return sb.toString();
	}

	/**
	 * Mimics sprintf(buf, "%0*d", decaimal, width).
	 */
	private static final StringBuilder sprintf0d(StringBuilder sb, int value, int width) {
		long d = value;
		if (d < 0) {
			sb.append('-');
			d = -d;
			--width;
		}
		int n = 10;
		for (int i = 2; i < width; i++) {
			n *= 10;
		}
		for (int i = 1; i < width && d < n; i++) {
			sb.append('0');
			n /= 10;
		}
		sb.append(d);
		return sb;
	}

	private String getAsString(Calendar c, int style, TimeZone tz) {
		java.text.DateFormat df = java.text.DateFormat.getDateInstance(style, getLocale());
		df.setTimeZone(tz);
		return df.format(c.getTime());
	}
}