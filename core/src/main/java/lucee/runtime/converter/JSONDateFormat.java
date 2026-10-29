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
package lucee.runtime.converter;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;

public final class JSONDateFormat {

	public static final String PATTERN_CF = "MMMM, dd yyyy HH:mm:ss Z";
	public static final String PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ"; // preferred pattern for json

	/**
	 * value of the setting "dateFormat" for the traditional Lucee format (see PATTERN_CF)
	 */
	public static final String FORMAT_CF = "cf";

	private static Locale locale = Locale.ENGLISH;

	/**
	 * formats the date with the given pattern. The formatter is cached by FormatUtil per locale and
	 * pattern.
	 */
	public static String format(Date date, TimeZone tz, String pattern) {
		tz = ThreadLocalPageContext.getTimeZone(tz);
		return FormatUtil.format(FormatUtil.getDateTimeFormatter(locale, pattern).formatter, date, tz);
	}

	/**
	 * translates the value of the setting "dateFormat" (this.serialization.dateFormat in Application.cfc
	 * or serialization.dateFormat in the configuration) to a pattern.
	 * 
	 * @param dateFormat "cf" (or empty), "iso"/"iso8601", "isoms"/"isoMillis" or a custom date/time mask
	 * @return the pattern
	 * @throws ApplicationException if the value is not a valid mask
	 */
	public static String toPattern(String dateFormat) throws ApplicationException {
		if (StringUtil.isEmpty(dateFormat, true)) return PATTERN_CF;
		dateFormat = dateFormat.trim();
		if (FORMAT_CF.equalsIgnoreCase(dateFormat)) return PATTERN_CF;
		try {
			// FormatUtil handles the keywords (iso, iso8601, isoms, ...) and custom masks
			FormatUtil.getDateTimeFormatter(locale, dateFormat);
		}
		catch (IllegalArgumentException e) {
			throw new ApplicationException("the serialization setting dateFormat has the invalid value [" + dateFormat
					+ "], valid values are [cf], [iso], [isoms] or a custom date/time mask like [yyyy-MM-dd HH:mm:ss]: " + e.getMessage());
		}
		return dateFormat;
	}
}
