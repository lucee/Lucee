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
package lucee.runtime.functions.displayFormatting;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import lucee.commons.i18n.FormatUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

/**
 * Implements the CFML Function dateformat
 */
public final class DateTimeFormat extends BIF {

	private static final long serialVersionUID = 134840879454373440L;
	public static final String DEFAULT_MASK = "dd-MMM-yyyy HH:mm:ss";// this is already a SimpleDateFormat mask!
	private static final String[] AP = new String[] { "A", "P" };
	private static final char ZERO = (char) 0;
	private static final char ONE = (char) 1;
	private static final String ZEROZERO = new StringBuilder().append(ZERO).append(ZERO).toString();

	/**
	 * @param pc
	 * @param object
	 * @return Formated Time Object as String
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object) throws ExpressionException {
		return invoke(object, null, Locale.US, ThreadLocalPageContext.getTimeZone(pc));
	}

	/**
	 * @param pc
	 * @param object
	 * @param mask Characters that show how CFML displays a date:
	 * @return Formated Time Object as String
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object, String mask) throws ExpressionException {
		return invoke(object, mask, Locale.US, ThreadLocalPageContext.getTimeZone(pc));
	}

	public static String call(PageContext pc, Object object, String mask, TimeZone tz) throws ExpressionException {
		return invoke(object, mask, Locale.US, tz == null ? ThreadLocalPageContext.getTimeZone(pc) : tz);
	}

	public static String invoke(Object object, String mask, Locale locale, TimeZone tz) throws ExpressionException {
		DateTime datetime = Caster.toDate(object, true, tz, null);
		if (datetime == null) {
			if (object.toString().trim().length() == 0) return "";
			throw new ExpressionException("Can't convert value [" + object + "] to a datetime value");
		}
		return invoke(datetime, mask, locale, tz);
	}

	public static String invoke(DateTime datetime, String mask, Locale locale, TimeZone tz) {

		if (locale == null) locale = Locale.US;

		if ("epoch".equalsIgnoreCase(mask)) {
			String gettime = String.valueOf(datetime.getTime() / 1000);
			String epoch = gettime.toString();
			return epoch;
		}
		else if ("epochms".equalsIgnoreCase(mask)) {
			String gettime = String.valueOf(datetime.getTime());
			String epoch = gettime.toString();
			return epoch;
		}

		DateTimeFormatter formatter;
		if (mask != null && (

		mask.equalsIgnoreCase("short") ||

				mask.equalsIgnoreCase("medium") ||

				mask.equalsIgnoreCase("long") ||

				mask.equalsIgnoreCase("full") ||

				mask.equalsIgnoreCase("iso") ||

				mask.equalsIgnoreCase("iso8601") ||

				mask.equalsIgnoreCase("isoms") ||

				mask.equalsIgnoreCase("isomillis") ||

				mask.equalsIgnoreCase("javascript")

		)

		) {
			formatter = FormatUtil.getDateTimeFormatter(locale, mask);
		}

		else {
			formatter = FormatUtil.getDateTimeFormatter(locale, convertMask(mask));

			String result = FormatUtil.format(formatter, datetime, tz);
			if (!StringUtil.isEmpty(result)) {
				int start, end = 0;
				String content;
				while ((start = result.indexOf(">>>")) != -1) {
					end = result.indexOf("<<<", start + 3);
					if (end == -1) break;
					content = result.substring(start + 3, end);
					if (content.length() == 2) {
						content = content.substring(0, 1);
					}
					result = result.substring(0, start) + content + result.substring(end + 3);
				}
			}
			return result;
		}
		return FormatUtil.format(formatter, datetime, tz);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, args[0]);
		if (args.length == 2) return call(pc, args[0], Caster.toString(args[1]));
		return call(pc, args[0], Caster.toString(args[1]), Caster.toTimeZone(args[2]));
	}

	public static String convertMask(String mask) {

		if (mask == null) return DEFAULT_MASK;
		else if ("iso8601".equalsIgnoreCase(mask) || "iso".equalsIgnoreCase(mask)) return "yyyy-MM-dd'T'HH:mm:ssXXX";
		else if ("isoms".equalsIgnoreCase(mask) || "isoMillis".equalsIgnoreCase(mask) || "javascript".equalsIgnoreCase(mask)) return "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

		mask = StringUtil.replace(mask, "''", ZEROZERO, false);
		boolean inside = false;

		char[] carr = mask.toCharArray();
		StringBuilder sb = new StringBuilder();
		char c;
		boolean reevaluate = false;

		for (int i = 0; i < carr.length; i++) {
			c = carr[i];
			switch (c) {
			// max 1
			case 'a':
			case 'W':
			case 'F':
				if (!inside) {
					if (!hasAlready(sb, c, 1)) sb.append(c);
				}
				else {
					sb.append(c);
				}
				break;
			// max 2
			case 's':
			case 'H':
			case 'K':
			case 'k':
			case 'h':
			case 'w':
				if (!inside) {
					if (!hasAlready(sb, c, 2)) sb.append(c);
				}
				else {
					sb.append(c);
				}
				break;
			// max 3
			case 'x':
			case 'Z':
				if (!inside) {
					if (!hasAlready(sb, c, 3)) sb.append(c);
				}
				else {
					sb.append(c);
				}
				break;
			// max 4
			case 'G':
			case 'E':
			case 'M':
			case 'z':
				if (!inside) {
					if (!hasAlready(sb, c, 4)) sb.append(c);
					else if (c == 'D' || c == 'd') reevaluate = true;
				}
				else {
					sb.append(c);
				}
				break;
			// max 5
			case 'X':
				if (!inside) {
					if (!hasAlready(sb, c, 5)) sb.append(c);
				}
				else {
					sb.append(c);
				}
				break;
			// max 10
			case 'y':
				if (!inside) {
					if (!hasAlready(sb, c, 10)) sb.append(c);
				}
				else {
					sb.append(c);
				}
				break;
			// no indentical char
			case 'm':
				if (!inside) {
					if (!hasAlready(sb, 'M', 4)) sb.append('M');
				}
				else {
					sb.append(c);
				}
				break;

			case 'D':
				if (!inside) {
					if (hasAlready(sb, 'E', 4)) {
						;
					}
					else if (hasAlready(sb, 'E', 3)) {
						sb.append('E');
					}
					else if (hasAlready(sb, 'D', 2)) {
						sb.setCharAt(i - 2, 'E');
						sb.setCharAt(i - 1, 'E');
						sb.append('E');
					}
					else if (!hasAlready(sb, 'D', 2)) {
						sb.append('D');
					}
					else sb.append('D');
				}
				else {
					sb.append(c);
				}
				break;
			case 'd':
				if (!inside) {
					if (hasAlready(sb, 'E', 4)) {
						;
					}
					else if (hasAlready(sb, 'E', 3)) {
						sb.append('E');
					}
					else if (hasAlready(sb, 'd', 2)) {
						sb.setCharAt(i - 2, 'E');
						sb.setCharAt(i - 1, 'E');
						sb.append('E');
					}
					else if (!hasAlready(sb, 'd', 2)) {
						sb.append('d');
					}
					else sb.append('d');
				}
				else {
					sb.append(c);
				}
				break;

			case 'S':
				if (!inside) {
					if (!hasAlready(sb, 's', 2)) sb.append('s');
				}
				else {
					sb.append(c);
				}
				break;

			case 't':
			case 'T':
				if (!inside) {
					if (i + 1 < carr.length && (carr[i + 1] == 't' || carr[i + 1] == 'T')) {
						if (!hasAlready(sb, 'a', 1)) sb.append('a');
						i++;
					}
					else if (!hasAlready(sb, 'a', 1)) sb.append(">>>a<<<");
				}
				else {
					sb.append(c);
				}
				break;

			case 'n':
				if (!inside) {
					if (!hasAlready(sb, 'm', 2)) sb.append('m');
				}
				else {
					sb.append(c);
				}
				break;
			case 'N':
				if (!inside) {
					if (!hasAlready(sb, 'm', 2)) sb.append('m');
				}
				else {
					sb.append(c);
				}
				break;
			case 'l':
				if (!inside) {// lllllllll
					if (!hasAlready(sb, 'S', 9)) sb.append('S');
				}
				else {
					sb.append(c);
				}
				break;
			case 'L':
				if (!inside) {
					if (!hasAlready(sb, 'S', 9)) sb.append('S');
				}
				else {
					sb.append(c);
				}
				break;
			case 'Y':
				if (!inside) {
					if (!hasAlready(sb, 'y', 10)) sb.append('y');
				}
				else {
					sb.append(c);
				}
				break;

			case 'g':
				if (!inside) {
					if (!hasAlready(sb, 'G', 4)) sb.append('G');
				}
				else {
					sb.append(c);
				}
				break;
			// quote it
			case 'f':
			case 'e':
			case 'A':
				if (!inside) {
					sb.append("'" + c + "'");
				}
				else {
					sb.append(c);
				}
				break;

			case '\'':
				if (carr.length - 1 > i) {
					if (carr[i + 1] == '\'') {
						i++;
						sb.append("''");
						break;
					}
				}

				inside = !inside;
				sb.append(carr[i]);
				break;

			default:
				if (!inside && ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) sb.append('\'').append(c).append('\'');
				else sb.append(c);
			}
		}

		String str = sb.toString();
		str = StringUtil.replace(str, "''", "", false);
		str = StringUtil.replace(str, ZEROZERO, "''", false);
		str = str.replace(ONE, 'E');
		str = y2yyyy(str);
		return str;
	}

	private static boolean hasAlready(StringBuilder sb, char c, int count) {
		int l = sb.length();
		if (l < count) return false;
		while (sb.charAt(l - count) == c) {
			if (--count == 0) return true;
		}
		return false;
	}

	public static String y2yyyy(String str) {
		char[] carr = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		boolean inside = false;
		char c;
		for (int i = 0; i < carr.length; i++) {
			c = carr[i];
			if (c == '\'') inside = !inside;
			else if (!inside && c == 'y') {
				if ((i == 0 || carr[i - 1] != 'y') && (i == (carr.length - 1) || carr[i + 1] != 'y')) {
					sb.append("yyyy");
					continue;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
