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
		return invoke(pc, object, null, Locale.US, ThreadLocalPageContext.getTimeZone(pc));
	}

	/**
	 * @param pc
	 * @param object
	 * @param mask Characters that show how CFML displays a date:
	 * @return Formated Time Object as String
	 * @throws ExpressionException
	 */
	public static String call(PageContext pc, Object object, String mask) throws ExpressionException {
		return invoke(pc, object, mask, Locale.US, ThreadLocalPageContext.getTimeZone(pc));
	}

	public static String call(PageContext pc, Object object, String mask, TimeZone tz) throws ExpressionException {
		return invoke(pc, object, mask, Locale.US, tz == null ? ThreadLocalPageContext.getTimeZone(pc) : tz);
	}

	public static String invoke(PageContext pc, Object object, String mask, Locale locale, TimeZone tz) throws ExpressionException {
		return invoke(object, mask, locale, tz);// FUTURE remove this method
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
		if ("isoms".equalsIgnoreCase(mask) || "isoMillis".equalsIgnoreCase(mask) || "javascript".equalsIgnoreCase(mask)) {
			DateTimeFormatter formatter = FormatUtil.getDateTimeFormatter(locale, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			return FormatUtil.format(formatter, datetime, tz);
		}

		DateTimeFormatter formatter = FormatUtil.getDateTimeFormatter(locale, convertMask(mask));

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

		while ((mask.indexOf("ttt")) != -1) {
			mask = StringUtil.replace(mask, "ttt", "tt", false, true);
		}

		char[] carr = mask.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < carr.length; i++) {

			switch (carr[i]) {
			case 'm':
				if (!inside) {
					sb.append('M');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'S':
				if (!inside) {
					sb.append('s');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 't':
				if (!inside) {
					if (i + 1 < carr.length && (carr[i + 1] == 't' || carr[i + 1] == 'T')) {
						sb.append('a');
						i++;
					}
					else sb.append(">>>a<<<");
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'T':
				if (!inside) {
					if (i + 1 < carr.length && (carr[i + 1] == 't' || carr[i + 1] == 'T')) {
						sb.append('a');
						i++;
					}
					else sb.append(">>>a<<<");
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'n':
				if (!inside) {
					sb.append('m');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'N':
				if (!inside) {
					sb.append('m');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'l':
				if (!inside) {
					sb.append('S');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'L':
				if (!inside) {
					sb.append('S');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'Y':
				if (!inside) {
					sb.append('y');
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'g':
				if (!inside) {
					sb.append('G');
				}
				else {
					sb.append(carr[i]);
				}
				break;

			case 'f':
				if (!inside) {
					sb.append("'f'");
				}
				else {
					sb.append(carr[i]);
				}
				break;
			case 'e':
				if (!inside) {
					sb.append("'e'");
				}
				else {
					sb.append(carr[i]);
				}
				break;

			case 'G':
			case 'y':
			case 'M':
			case 'W':
			case 'w':
			case 'F':
			case 'E':
			case 'a':
			case 'H':
			case 'h':
			case 'K':
			case 'k':
			case 'x':
			case 'X':
			case 'Z':
			case 'z':
			case 's':
				// case '.':
				sb.append(carr[i]);
				break;

			case 'D':
			case 'd':
				int len = sb.length();
				// 2 before are D or d
				if (len > 1 && (sb.charAt(len - 1) == 'd' || sb.charAt(len - 1) == 'D') && (sb.charAt(len - 2) == 'd' || sb.charAt(len - 2) == 'D')) {
					sb.deleteCharAt(len - 1);
					sb.deleteCharAt(len - 2);
					sb.append(ONE).append(ONE).append(ONE);
					break;
				}
				// 2 before are D or d
				else if (len > 0 && sb.charAt(len - 1) == ONE) {
					sb.append(ONE);
					break;
				}

				sb.append(carr[i]);
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
			/*
			 * case '\'': if(carr.length-1>i) { if(carr[i+1]=='\'') { i++; sb.append("''"); break; } }
			 * sb.append("''"); break;
			 */
			default:
				char c = carr[i];
				if (!inside && ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) sb.append('\'').append(c).append('\'');
				else sb.append(c);
			}
		}
		String str = StringUtil.replace(sb.toString(), "''", "", false);
		str = StringUtil.replace(str, ZEROZERO, "''", false);
		str = str.replace(ONE, 'E');
		str = y2yyyy(str);
		while ((str.indexOf("aa")) != -1) {
			str = StringUtil.replace(str, "aa", "a", false, true);
		}

		return str;
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
