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
package lucee.runtime.type.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lucee.commons.lang.StringList;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

/**
 * List is not a type, only some static method to manipulate String lists
 */
public final class ListUtil {

	/**
	 * casts a list to Array object, the list can be have quoted (",') arguments and delimter in this
	 * arguments are ignored. quotes are not removed example:
	 * listWithQuotesToArray("aab,a'a,b',a\"a,b\"",",","\"'") will be translated to
	 * ["aab","a'a,b'","a\"a,b\""]
	 *
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @param quotes quotes of the list
	 * @return Array Object
	 */
	public static Array listWithQuotesToArray(String list, String delimiter, String quotes) {
		if (list.length() == 0) return new ArrayImpl();

		int len = list.length();
		int last = 0;
		char[] del = delimiter.toCharArray();
		char[] quo = quotes.toCharArray();
		char c;
		char inside = 0;

		ArrayImpl array = new ArrayImpl();
		// try{
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < quo.length; y++) {
				if (c == quo[y]) {
					if (c == inside) inside = 0;
					else if (inside == 0) inside = c;
					continue;
				}
			}

			for (int y = 0; y < del.length; y++) {
				if (inside == 0 && c == del[y]) {
					array.appendEL(list.substring(last, i));
					last = i + 1;
					break;
				}
			}
		}
		if (last <= len) array.appendEL(list.substring(last));
		// }catch(PageException e){}
		return array;
	}

	/**
	 * casts a list to Array object
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static Array listToArray(String list, String delimiter) {
		if (delimiter.length() == 1) return listToArray(list, delimiter.charAt(0));
		if (list.length() == 0) return new ArrayImpl();
		if (delimiter.length() == 0) {
			int len = list.length();
			ArrayImpl array = new ArrayImpl();
			array.appendEL("");// ACF compatibility
			for (int i = 0; i < len; i++) {
				array.appendEL(list.charAt(i));
			}
			array.appendEL("");// ACF compatibility
			return array;
		}

		int len = list.length();
		int last = 0;
		char[] del = delimiter.toCharArray();
		char c;

		ArrayImpl array = new ArrayImpl();
		// try{
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					array.appendEL(list.substring(last, i));
					last = i + 1;
					break;
				}
			}
		}
		if (last <= len) array.appendEL(list.substring(last));
		// }catch(ExpressionException e){}
		return array;
	}

	public static Array listToArray(String list, String delimiter, boolean includeEmptyFields, boolean multiCharDelim) {
		if (includeEmptyFields) return listToArray(list, delimiter, multiCharDelim);
		return listToArrayRemoveEmpty(list, delimiter, multiCharDelim);

	}

	private static Array listToArray(String list, String delimiter, boolean multiCharDelim) {
		if (!multiCharDelim || delimiter.length() == 0) return listToArray(list, delimiter);
		if (delimiter.length() == 1) return listToArray(list, delimiter.charAt(0));
		int len = list.length();
		if (len == 0) return new ArrayImpl();

		Array array = new ArrayImpl();
		int from = 0;
		int index;
		int dl = delimiter.length();
		while ((index = list.indexOf(delimiter, from)) != -1) {
			array.appendEL(list.substring(from, index));
			from = index + dl;
		}
		array.appendEL(list.substring(from, len));

		return array;
	}

	/**
	 * casts a list to Array object
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static Array listToArray(String list, char delimiter) {
		if (list.length() == 0) return new ArrayImpl();
		int len = list.length();
		int last = 0;

		Array array = new ArrayImpl();
		try {
			for (int i = 0; i < len; i++) {
				if (list.charAt(i) == delimiter) {
					array.append(list.substring(last, i));
					last = i + 1;
				}
			}
			if (last <= len) array.append(list.substring(last));
		}
		catch (PageException e) {
		}
		return array;
	}

	/**
	 * casts a list to Array object remove Empty Elements
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	private static Array listToArrayRemoveEmpty(String list, String delimiter, boolean multiCharDelim) {
		if (!multiCharDelim || delimiter.length() == 0) return listToArrayRemoveEmpty(list, delimiter);

		if (delimiter.length() == 1) return listToArrayRemoveEmpty(list, delimiter.charAt(0));

		int len = list.length();
		if (len == 0) return new ArrayImpl();

		Array array = new ArrayImpl();
		int from = 0;
		int index;
		int dl = delimiter.length();
		while ((index = list.indexOf(delimiter, from)) != -1) {
			if (from < index) array.appendEL(list.substring(from, index));
			from = index + dl;
		}
		if (from < len) array.appendEL(list.substring(from, len));
		return array;

	}

	public static Array listToArrayRemoveEmpty(String list, String delimiter) {
		if (delimiter.length() == 1) return listToArrayRemoveEmpty(list, delimiter.charAt(0));
		int len = list.length();
		ArrayImpl array = new ArrayImpl();
		if (len == 0) return array;
		if (delimiter.length() == 0) {
			for (int i = 0; i < len; i++) {
				array.appendEL(String.valueOf(list.charAt(i)));
			}
			return array;
		}
		int last = 0;

		char[] del = delimiter.toCharArray();
		char c;
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (last < i) array.appendEL(list.substring(last, i));
					last = i + 1;
					break;
				}
			}
		}
		if (last < len) array.appendEL(list.substring(last));
		return array;
	}

	public static String listRemoveEmpty(String list, String delimiter) {
		if (delimiter.length() == 1) return listRemoveEmpty(list, delimiter.charAt(0));
		if (delimiter.length() == 0) return list;
		int len = list.length();

		if (len == 0) return "";
		StringBuilder sb = new StringBuilder();

		int last = 0;

		char[] del = delimiter.toCharArray();
		char c;
		boolean first = true;
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (last < i) {
						if (first) first = false;
						else sb.append(delimiter);
						sb.append(list.substring(last, i));
					}
					last = i + 1;
					break;
				}
			}
		}
		if (last < len) {
			if (!first) sb.append(delimiter);
			sb.append(list.substring(last));
		}
		return sb.toString();
	}

	/**
	 * casts a list to Array object remove Empty Elements
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static Array listToArrayRemoveEmpty(String list, char delimiter) {
		int len = list.length();
		ArrayImpl array = new ArrayImpl();
		if (len == 0) return array;
		int last = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) array.appendEL(list.substring(last, i));
				last = i + 1;
			}
		}
		if (last < len) array.appendEL(list.substring(last));

		return array;
	}

	public static String listRemoveEmpty(String list, char delimiter) {
		int len = list.length();
		if (len == 0) return "";

		StringBuilder sb = new StringBuilder();
		int last = 0;
		boolean first = true;
		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) {
					if (first) first = false;
					else sb.append(delimiter);
					sb.append(list.substring(last, i));
				}
				last = i + 1;
			}
		}
		if (last < len) {
			if (!first) sb.append(delimiter);
			sb.append(list.substring(last));
		}
		return sb.toString();
	}

	public static List<String> toListRemoveEmpty(String list, char delimiter) {
		int len = list.length();
		List<String> array = new ArrayList<String>();
		if (len == 0) return array;
		int last = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) array.add(list.substring(last, i));
				last = i + 1;
			}
		}
		if (last < len) array.add(list.substring(last));

		return array;
	}

	/**
	 * casts a list to Array object remove Empty Elements
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static StringList listToStringListRemoveEmpty(String list, char delimiter) {
		int len = list.length();
		StringList rtn = new StringList();
		if (len == 0) return rtn.reset();
		int last = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) rtn.add(list.substring(last, i));
				last = i + 1;
			}
		}
		if (last < len) rtn.add(list.substring(last));

		return rtn.reset();
	}

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static Array listToArrayTrim(String list, String delimiter) {
		if (delimiter.length() == 1) return listToArrayTrim(list, delimiter.charAt(0));
		if (list.length() == 0) return new ArrayImpl();
		char[] del = delimiter.toCharArray();
		char c;

		// remove at start
		outer: while (list.length() > 0) {
			c = list.charAt(0);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					list = list.substring(1);
					continue outer;
				}
			}
			break;
		}

		int len;
		outer: while (list.length() > 0) {
			c = list.charAt(list.length() - 1);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					len = list.length();
					list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
					continue outer;
				}
			}
			break;
		}
		return listToArray(list, delimiter);
	}

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list and store count
	 * to info
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @param info
	 * @return Array Object
	 */
	public static Array listToArrayTrim(String list, String delimiter, int[] info) {
		if (delimiter.length() == 1) return listToArrayTrim(list, delimiter.charAt(0), info);
		if (list.length() == 0) return new ArrayImpl();
		char[] del = delimiter.toCharArray();
		char c;

		// remove at start
		outer: while (list.length() > 0) {
			c = list.charAt(0);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					info[0]++;
					list = list.substring(1);
					continue outer;
				}
			}
			break;
		}

		int len;
		outer: while (list.length() > 0) {
			c = list.charAt(list.length() - 1);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					info[1]++;
					len = list.length();
					list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
					continue outer;
				}
			}
			break;
		}
		return listToArray(list, delimiter);
	}

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list and store count
	 * to info
	 * 
	 * @param list list to cast
	 * @param pos
	 * @param delimiter delimter of the list
	 * @return Array Object
	 * @throws ExpressionException
	 */
	public static String listInsertAt(String list, int pos, String value, String delimiter, boolean ignoreEmpty) throws ExpressionException {
		if (pos < 1) throw new ExpressionException("invalid string list index [" + (pos) + "]");

		char[] del = delimiter.toCharArray();
		char c;
		StringBuilder result = new StringBuilder();
		String end = "";
		int len;

		// remove at start
		if (ignoreEmpty) {
			outer: while (list.length() > 0) {
				c = list.charAt(0);
				for (int i = 0; i < del.length; i++) {
					if (c == del[i]) {
						list = list.substring(1);
						result.append(c);
						continue outer;
					}
				}
				break;
			}
		}

		// remove at end
		if (ignoreEmpty) {
			outer: while (list.length() > 0) {
				c = list.charAt(list.length() - 1);
				for (int i = 0; i < del.length; i++) {
					if (c == del[i]) {
						len = list.length();
						list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
						end = c + end;
						continue outer;
					}

				}
				break;
			}
		}

		len = list.length();
		int last = 0;

		int count = 0;
		outer: for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {

					if (!ignoreEmpty || last < i) {
						if (pos == ++count) {
							result.append(value);
							result.append(del[0]);
						}
					}
					result.append(list.substring(last, i));
					result.append(c);
					last = i + 1;
					continue outer;
				}
			}
		}
		count++;
		if (last <= len) {
			if (pos == count) {
				result.append(value);
				result.append(del[0]);
			}

			result.append(list.substring(last));
		}
		if (pos > count) {
			throw new ExpressionException("invalid string list index [" + (pos) + "], indexes go from 1 to " + (count));

		}

		return result + end;
	}

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @return Array Object
	 */
	public static Array listToArrayTrim(String list, char delimiter) {
		if (list.length() == 0) return new ArrayImpl();
		// remove at start
		while (list.indexOf(delimiter) == 0) {
			list = list.substring(1);
		}
		int len = list.length();
		if (len == 0) return new ArrayImpl();
		while (list.lastIndexOf(delimiter) == len - 1) {
			list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
			len = list.length();
		}
		return listToArray(list, delimiter);
	}

	/**
	 * @param list
	 * @param delimiter
	 * @return trimmed list
	 */
	public static StringList toListTrim(String list, char delimiter) {
		if (list.length() == 0) return new StringList();
		// remove at start
		while (list.indexOf(delimiter) == 0) {
			list = list.substring(1);
		}
		int len = list.length();
		if (len == 0) return new StringList();
		while (list.lastIndexOf(delimiter) == len - 1) {
			list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
			len = list.length();
		}

		return toList(list, delimiter);
	}

	/**
	 * @param list
	 * @param delimiter
	 * @return list
	 */
	public static StringList toList(String list, char delimiter) {
		if (list.length() == 0) return new StringList();
		int len = list.length();
		int last = 0;

		StringList rtn = new StringList();

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				rtn.add(list.substring(last, i));
				last = i + 1;
			}
		}
		if (last <= len) rtn.add(list.substring(last));
		rtn.reset();
		return rtn;
	}

	public static StringList toWordList(String list) {
		if (list.length() == 0) return new StringList();
		int len = list.length();
		int last = 0;
		char c, l = 0;
		StringList rtn = new StringList();

		for (int i = 0; i < len; i++) {
			if (StringUtil.isWhiteSpace(c = list.charAt(i))) {
				rtn.add(list.substring(last, i), l);
				l = c;
				last = i + 1;
			}
		}
		if (last <= len) rtn.add(list.substring(last), l);
		rtn.reset();
		return rtn;
	}

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @param info
	 * @return Array Object
	 */
	public static Array listToArrayTrim(String list, char delimiter, int[] info) {
		if (list.length() == 0) return new ArrayImpl();
		// remove at start
		while (list.indexOf(delimiter) == 0) {
			info[0]++;
			list = list.substring(1);
		}
		int len = list.length();
		if (len == 0) return new ArrayImpl();
		while (list.lastIndexOf(delimiter) == len - 1) {
			info[1]++;
			list = list.substring(0, len - 1 < 0 ? 0 : len - 1);
			len = list.length();
		}
		return listToArray(list, delimiter);
	}

	/*
	 * * finds a value inside a list, ignore case
	 * 
	 * @param list list to search
	 * 
	 * @param value value to find
	 * 
	 * @return position in list (0-n) or -1
	 *
	 * private static int listFindNoCase(String list, String value) { return listFindNoCase(list, value,
	 * ",", true); }
	 */

	/**
	 * finds a value inside a list, do not ignore case
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list (0-n) or -1
	 */
	public static int listFindNoCase(String list, String value, String delimiter) {
		return listFindNoCase(list, value, delimiter, true);
	}

	/**
	 * finds a value inside a list, do not ignore case
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @param trim trim the list or not
	 * @return position in list (0-n) or -1
	 */
	public static int listFindNoCase(String list, String value, String delimiter, boolean trim) {
		Array arr = trim ? listToArrayTrim(list, delimiter) : listToArray(list, delimiter);
		int len = arr.size();
		for (int i = 1; i <= len; i++) {
			if (((String) arr.get(i, "")).equalsIgnoreCase(value)) return i - 1;
		}
		return -1;
	}

	public static int listFindForSwitch(String list, String value, String delimiter) {
		if (list.indexOf(delimiter) == -1 && list.equalsIgnoreCase(value)) return 1;

		Array arr = listToArray(list, delimiter);
		int len = arr.size();
		for (int i = 1; i <= len; i++) {
			if (((String) arr.get(i, "")).equalsIgnoreCase(value)) return i;
		}
		return -1;
	}

	/**
	 * finds a value inside a list, ignore case, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listFindNoCaseIgnoreEmpty(String list, String value, String delimiter) {
		if (delimiter.length() == 1) return listFindNoCaseIgnoreEmpty(list, value, delimiter.charAt(0));
		if (list == null) return -1;
		int len = list.length();
		if (len == 0) return -1;
		int last = 0;
		int count = 0;
		char[] del = delimiter.toCharArray();
		char c;

		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (last < i) {
						if (list.substring(last, i).equalsIgnoreCase(value)) return count;
						count++;
					}
					last = i + 1;
					break;
				}
			}
		}
		if (last < len) {
			if (list.substring(last).equalsIgnoreCase(value)) return count;
		}
		return -1;
	}

	/**
	 * finds a value inside a list, ignore case, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listFindNoCaseIgnoreEmpty(String list, String value, char delimiter) {
		if (list == null) return -1;
		int len = list.length();
		if (len == 0) return -1;
		int last = 0;
		int count = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) {
					if (list.substring(last, i).equalsIgnoreCase(value)) return count;
					count++;
				}
				last = i + 1;
			}
		}
		if (last < len) {
			if (list.substring(last).equalsIgnoreCase(value)) return count;
		}
		return -1;
	}

	/**
	 * finds a value inside a list, case sensitive
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @return position in list or 0
	 */
	public static int listFind(String list, String value) {
		return listFind(list, value, ",");
	}

	/**
	 * finds a value inside a list, do not case sensitive
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listFind(String list, String value, String delimiter) {
		Array arr = listToArrayTrim(list, delimiter);
		int len = arr.size();
		for (int i = 1; i <= len; i++) {
			if (arr.get(i, "").equals(value)) return i - 1;
		}

		return -1;
	}

	/**
	 * finds a value inside a list, case sensitive, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listFindIgnoreEmpty(String list, String value, String delimiter) {
		if (delimiter.length() == 1) return listFindIgnoreEmpty(list, value, delimiter.charAt(0));
		if (list == null) return -1;
		int len = list.length();
		if (len == 0) return -1;
		int last = 0;
		int count = 0;
		char[] del = delimiter.toCharArray();
		char c;

		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (last < i) {
						if (list.substring(last, i).equals(value)) return count;
						count++;
					}
					last = i + 1;
					break;
				}
			}
		}
		if (last < len) {
			if (list.substring(last).equals(value)) return count;
		}
		return -1;
	}

	/**
	 * finds a value inside a list, case sensitive, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listFindIgnoreEmpty(String list, String value, char delimiter) {
		if (list == null) return -1;
		int len = list.length();
		if (len == 0) return -1;
		int last = 0;
		int count = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (last < i) {
					if (list.substring(last, i).equals(value)) return count;
					count++;
				}
				last = i + 1;
			}
		}
		if (last < len) {
			if (list.substring(last).equals(value)) return count;
		}
		return -1;
	}

	/**
	 * returns if a value of the list contains given value, ignore case
	 * 
	 * @param list list to search in
	 * @param value value to serach
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listContainsNoCase(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		if (StringUtil.isEmpty(value)) return -1;

		Array arr = listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter);
		int len = arr.size();

		for (int i = 1; i <= len; i++) {
			if (StringUtil.indexOfIgnoreCase(arr.get(i, "").toString(), value) != -1) return i - 1;
		}
		return -1;
	}

	/*
	 * * returns if a value of the list contains given value, ignore case, ignore empty values
	 * 
	 * @param list list to search in
	 * 
	 * @param value value to serach
	 * 
	 * @param delimiter delimiter of the list
	 * 
	 * @return position in list or 0
	 * 
	 * public static int listContainsIgnoreEmptyNoCase(String list, String value, String delimiter) {
	 * if(StringUtil.isEmpty(value)) return -1; Array arr=listToArrayRemoveEmpty(list,delimiter); int
	 * count=0; int len=arr.size();
	 * 
	 * for(int i=1;i<=len;i++) { String item=arr.get(i,"").toString();
	 * if(StringUtil.indexOfIgnoreCase(item, value)!=-1) return count; count++; } return -1; }
	 */

	/**
	 * returns if a value of the list contains given value, case sensitive
	 * 
	 * @param list list to search in
	 * @param value value to serach
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public static int listContains(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		if (StringUtil.isEmpty(value)) return -1;

		Array arr = listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter);
		int len = arr.size();
		for (int i = 1; i <= len; i++) {
			if (arr.get(i, "").toString().indexOf(value) != -1) return i - 1;
		}
		return -1;
	}

	/*
	 * * returns if a value of the list contains given value, case sensitive, ignore empty positions
	 * 
	 * @param list list to search in
	 * 
	 * @param value value to serach
	 * 
	 * @param delimiter delimiter of the list
	 * 
	 * @return position in list or 0
	 * 
	 * private static int listContainsIgnoreEmpty(String list, String value, String delimiter, boolean
	 * multiCharacterDelimiter) { if(StringUtil.isEmpty(value)) return -1; Array
	 * arr=listToArrayRemoveEmpty(list,delimiter); int count=0; int len=arr.size();
	 * 
	 * String item; for(int i=1;i<=len;i++) { item=arr.get(i,"").toString(); if(item.indexOf(value)!=-1)
	 * return count; count++; } return -1; }
	 */

	/**
	 * convert a string array to string list, removes empty values at begin and end of the list
	 * 
	 * @param array array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string array
	 */
	public static String arrayToListTrim(String[] array, String delimiter) {
		return trim(arrayToList(array, delimiter), delimiter, false);
	}

	/**
	 * convert a string array to string list
	 * 
	 * @param array array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string array
	 */
	public static String arrayToList(String[] array, String delimiter) {
		if (ArrayUtil.isEmpty(array)) return "";
		StringBuilder sb = new StringBuilder(array[0]);

		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			for (int i = 1; i < array.length; i++) {
				sb.append(c);
				sb.append(array[i]);
			}
		}
		else {
			for (int i = 1; i < array.length; i++) {
				sb.append(delimiter);
				sb.append(array[i]);
			}
		}

		return sb.toString();
	}

	public static String arrayToList(Collection.Key[] array, String delimiter) {
		if (array.length == 0) return "";
		StringBuilder sb = new StringBuilder(array[0].getString());

		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			for (int i = 1; i < array.length; i++) {
				sb.append(c);
				sb.append(array[i].getString());
			}
		}
		else {
			for (int i = 1; i < array.length; i++) {
				sb.append(delimiter);
				sb.append(array[i].getString());
			}
		}

		return sb.toString();
	}

	/**
	 * convert Array Object to string list
	 * 
	 * @param array array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string array
	 * @throws PageException
	 */
	public static String arrayToList(Array array, String delimiter) throws PageException {
		if (array.size() == 0) return "";
		StringBuilder sb = new StringBuilder(Caster.toString(array.get(1, "")));
		int len = array.size();

		for (int i = 2; i <= len; i++) {
			sb.append(delimiter);
			sb.append(Caster.toString(array.get(i, "")));
		}
		return sb.toString();
	}

	public static String toList(java.util.Collection<String> list, String delimiter) {
		if (list.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = list.iterator();

		if (it.hasNext()) sb.append((it.next()));

		while (it.hasNext()) {
			sb.append(delimiter);
			sb.append((it.next()));
		}
		return sb.toString();
	}

	public static String listToList(java.util.List<?> list, String delimiter) throws PageException {
		if (list.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = list.iterator();

		if (it.hasNext()) sb.append(Caster.toString(it.next()));

		while (it.hasNext()) {
			sb.append(delimiter);
			sb.append(Caster.toString(it.next()));
		}
		return sb.toString();
	}

	public static String listToListEL(java.util.List<String> list, String delimiter) {
		if (list.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = list.iterator();

		if (it.hasNext()) sb.append(it.next());

		while (it.hasNext()) {
			sb.append(delimiter);
			sb.append(it.next());
		}
		return sb.toString();
	}

	/**
	 * trims a string array, removes all empty array positions at the start and the end of the array
	 * 
	 * @param array array to remove elements
	 * @return cleared array
	 */
	public static String[] trim(String[] array) {
		int from = 0;
		int to = 0;

		// test start
		for (int i = 0; i < array.length; i++) {
			from = i;
			if (array[i].length() != 0) break;
		}

		// test end
		for (int i = array.length - 1; i >= 0; i--) {
			to = i;
			if (array[i].length() != 0) break;
		}

		int newLen = to - from + 1;

		if (newLen < array.length) {
			String[] rtn = new String[newLen];
			System.arraycopy(array, from, rtn, 0, newLen);
			return rtn;
		}
		return array;
	}

	/**
	 * trims a string list, remove all empty delimiter at start and the end
	 * 
	 * @param list list to trim
	 * @param delimiter delimiter of the list
	 * @return trimed list
	 */
	public static String trim(String list, String delimiter) {
		return trim(list, delimiter, new int[2], false);
	}

	public static String trim(String list, String delimiter, boolean multiCharacterDelimiter) {
		return trim(list, delimiter, new int[2], multiCharacterDelimiter);
	}

	/**
	 * trims a string list, remove all empty delimiter at start and the end
	 * 
	 * @param list list to trim
	 * @param delimiter delimiter of the list
	 * @param removeInfo int array contain count of removed values (removeInfo[0]=at the
	 *            begin;removeInfo[1]=at the end)
	 * @return trimed list
	 */
	public static String trim(String list, String delimiter, int[] removeInfo, boolean multiCharacterDelimiter) {
		if (list.length() == 0) return "";

		if (multiCharacterDelimiter && delimiter.length() > 1) {
			int from = 0;

			// remove at start
			while (list.length() >= from + delimiter.length()) {
				if (list.indexOf(delimiter, from) == from) {
					from += delimiter.length();
					removeInfo[0]++;
					continue;
				}
				break;
			}

			if (from > 0) list = list.substring(from);

			// remove at end
			while (list.length() >= delimiter.length()) {
				if (list.lastIndexOf(delimiter) == list.length() - delimiter.length()) {
					removeInfo[1]++;
					list = list.substring(0, list.length() - delimiter.length());
					continue;
				}
				break;
			}
			return list;
		}

		if (list.length() == 0) return "";
		int from = 0;
		int to = list.length();
		// int len=delimiter.length();
		char[] del = delimiter.toCharArray();
		char c;

		// remove at start
		outer: while (list.length() > from) {
			c = list.charAt(from);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					from++;
					removeInfo[0]++;
					// list=list.substring(from);
					continue outer;
				}
			}
			break;
		}

		// int len;
		outer: while (to > from) {
			c = list.charAt(to - 1);
			for (int i = 0; i < del.length; i++) {
				if (c == del[i]) {
					to--;
					removeInfo[1]++;
					continue outer;
				}
			}
			break;
		}
		int newLen = to - from;

		if (newLen < list.length()) {
			return list.substring(from, to);
		}
		return list;

	}

	/**
	 * sorts a string list
	 * 
	 * @param list list to sort
	 * @param sortType sort type (numeric,text,textnocase)
	 * @param sortOrder sort order (asc,desc)
	 * @param delimiter list delimiter
	 * @return sorted list
	 * @throws PageException
	 */
	public static String sortIgnoreEmpty(String list, String sortType, String sortOrder, String delimiter) throws PageException {
		return _sort(toStringArray(listToArrayRemoveEmpty(list, delimiter)), sortType, sortOrder, delimiter);
	}

	/**
	 * sorts a string list
	 * 
	 * @param list list to sort
	 * @param sortType sort type (numeric,text,textnocase)
	 * @param sortOrder sort order (asc,desc)
	 * @param delimiter list delimiter
	 * @return sorted list
	 * @throws PageException
	 */
	public static String sort(String list, String sortType, String sortOrder, String delimiter) throws PageException {
		return _sort(toStringArray(listToArray(list, delimiter)), sortType, sortOrder, delimiter);
	}

	private static String _sort(Object[] arr, String sortType, String sortOrder, String delimiter) throws PageException {

		Arrays.sort(arr, ArrayUtil.toComparator(null, sortType, sortOrder, false));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i != 0) sb.append(delimiter);
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * cast an Object Array to a String Array
	 * 
	 * @param array
	 * @return String Array
	 */
	public static String[] toStringArrayEL(Array array) {
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, null), null);
		}

		return arr;
	}

	/**
	 * cast an Object Array to a String Array
	 * 
	 * @param array
	 * @return String Array
	 * @throws PageException
	 */
	public static String[] toStringArray(Array array) throws PageException {
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, null));
		}
		return arr;
	}

	public static String[] toStringArray(java.util.Collection<String> coll) {
		return coll.toArray(new String[coll.size()]);
	}

	public static String[] toStringArray(List<String> list) {
		return list.toArray(new String[list.size()]);
	}

	/**
	 * cast an Object Array to a String Array
	 * 
	 * @param array
	 * @param defaultValue
	 * @return String Array
	 */
	public static String[] toStringArray(Array array, String defaultValue) {
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, defaultValue), defaultValue);
		}

		return arr;
	}

	/**
	 * cast an Object Array to a String Array and trim all values
	 * 
	 * @param array
	 * @return String Array
	 * @throws PageException
	 */
	public static String[] toStringArrayTrim(Array array) throws PageException {
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, "")).trim();
		}

		return arr;
	}

	/**
	 * return first element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @return returns the first element of the list
	 * @deprecated use instead first(String list, String delimiter, boolean ignoreEmpty)
	 */
	@Deprecated
	public static String first(String list, String delimiter) {
		return first(list, delimiter, true, 1);
	}

	/**
	 * return last element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @return returns the last Element of a list
	 * @deprecated use instead last(String list, String delimiter, boolean ignoreEmpty)
	 */
	@Deprecated
	public static String last(String list, String delimiter) {
		return last(list, delimiter, true);
	}

	/**
	 * return last element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @param ignoreEmpty
	 * @return returns the last Element of a list
	 */
	public static String last(String list, String delimiter, boolean ignoreEmpty) {

		if (StringUtil.isEmpty(list)) return "";
		int len = list.length();

		char[] del;
		if (StringUtil.isEmpty(delimiter)) {
			del = new char[] { ',' };
		}
		else del = delimiter.toCharArray();

		int index;
		int x;
		while (true) {
			index = -1;

			for (int i = 0; i < del.length; i++) {
				x = list.lastIndexOf(del[i]);
				if (x > index) index = x;
			}

			if (index == -1) {
				return list;
			}

			else if (index + 1 == len) {
				if (!ignoreEmpty) return "";
				list = list.substring(0, len - 1);
				len--;
			}
			else {
				return list.substring(index + 1);
			}
		}
	}

	/**
	 * return last element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @return returns the last Element of a list
	 */

	public static String last(String list, char delimiter) {

		int len = list.length();
		if (len == 0) return "";
		int index = 0;

		while (true) {
			index = list.lastIndexOf(delimiter);
			if (index == -1) {
				return list;
			}
			else if (index + 1 == len) {
				list = list.substring(0, len - 1);
				len--;
			}
			else {
				return list.substring(index + 1);
			}
		}
	}

	/**
	 * returns count of items in the list
	 * 
	 * @param list
	 * @param delimiter
	 * @return list len
	 */
	public static int len(String list, char delimiter, boolean ignoreEmpty) {
		int len = StringUtil.length(list);
		if (len == 0) return 0;

		int count = 0;
		int last = 0;

		for (int i = 0; i < len; i++) {
			if (list.charAt(i) == delimiter) {
				if (!ignoreEmpty || last < i) count++;
				last = i + 1;
			}
		}
		if (!ignoreEmpty || last < len) count++;
		return count;
	}

	/**
	 * returns count of items in the list
	 * 
	 * @param list
	 * @param delimiter
	 * @return list len
	 */
	public static int len(String list, String delimiter, boolean ignoreEmpty) {
		if (delimiter.length() == 1) return len(list, delimiter.charAt(0), ignoreEmpty);
		char[] del = delimiter.toCharArray();
		int len = StringUtil.length(list);
		if (len == 0) return 0;

		int count = 0;
		int last = 0;
		char c;

		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (!ignoreEmpty || last < i) count++;
					last = i + 1;
					break;
				}
			}
		}
		if (!ignoreEmpty || last < len) count++;
		return count;
	}

	/*
	 * * cast an int into a char
	 * 
	 * @param i int to cast
	 * 
	 * @return int as char / private char c(int i) { return (char)i; }
	 */

	/**
	 * gets a value from list
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @param position
	 * @return Array Object
	 */
	public static String getAt(String list, String delimiter, int position, boolean ignoreEmpty, String defaultValue) {
		if (delimiter.length() == 1) return getAt(list, delimiter.charAt(0), position, ignoreEmpty, defaultValue);
		int len = list.length();

		if (len == 0) return defaultValue;
		int last = -1;
		int count = -1;

		char[] del = delimiter.toCharArray();
		char c;
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					if (ignoreEmpty && (last + 1) == i) {
						last = i;
						break;
					}

					count++;
					if (count == position) {
						return list.substring(last + 1, i);
					}
					last = i;
					break;
				}
			}
		}

		if (position == count + 1) {
			if (!ignoreEmpty || last + 1 < len) return list.substring(last + 1);
		}
		return defaultValue;
	}

	/**
	 * get an element at a specified position in list
	 * 
	 * @param list list to cast
	 * @param delimiter delimter of the list
	 * @param position
	 * @return Array Object
	 */
	public static String getAt(String list, char delimiter, int position, boolean ignoreEmpty, String defaultValue) {
		int len = list.length();
		if (len == 0) return defaultValue;
		int last = -1;
		int count = -1;

		for (int i = 0; i < len; i++) {
			// char == delimiter
			if (list.charAt(i) == delimiter) {
				if (ignoreEmpty && (last + 1) == i) {
					last = i;
					continue;
				}

				count++;
				if (count == position) {
					return list.substring(last + 1, i);
				}
				last = i;
			}
		}

		if (position == count + 1) {
			if (!ignoreEmpty || last + 1 < len) return list.substring(last + 1);
		}
		return defaultValue;
	}

	public static String[] listToStringArray(String list, char delimiter) {
		Array array = ListUtil.listToArrayRemoveEmpty(list, delimiter);
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, ""), "");
		}
		return arr;
	}

	public static String[] listToStringArray(String list, String delimiter) {
		Array array = ListUtil.listToArrayRemoveEmpty(list, delimiter);
		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Caster.toString(array.get(i + 1, ""), "");
		}
		return arr;
	}

	/**
	 * trim every single item of the array
	 * 
	 * @param arr
	 * @return
	 */
	public static String[] trimItems(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/**
	 * trim every single item of the array
	 * 
	 * @param arr
	 * @return
	 * @throws PageException
	 */
	public static Array trimItems(Array arr) throws PageException {
		Key[] keys = CollectionUtil.keys(arr);

		for (int i = 0; i < keys.length; i++) {
			arr.setEL(keys[i], Caster.toString(arr.get(keys[i], null)).trim());
		}
		return arr;
	}

	public static Set<String> listToSet(String list, String delimiter, boolean trim) {
		if (list == null || list.length() == 0) return new HashSet<String>();
		int len = list.length();
		int last = 0;
		char[] del = delimiter.toCharArray();
		char c;

		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			for (int y = 0; y < del.length; y++) {
				if (c == del[y]) {
					set.add(trim ? list.substring(last, i).trim() : list.substring(last, i));
					last = i + 1;
					break;
				}
			}
		}
		if (last <= len) set.add(list.substring(last));
		return set;
	}

	public static Set<String> listToSet(String list, char delimiter, boolean trim) {
		if (list.length() == 0) return new HashSet<String>();
		int len = list.length();
		int last = 0;
		char c;

		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			if (c == delimiter) {
				set.add(trim ? list.substring(last, i).trim() : list.substring(last, i));
				last = i + 1;
			}
		}
		if (last <= len) set.add(list.substring(last));
		return set;
	}

	public static Set<String> toSet(String[] arr) {
		Set<String> set = new HashSet<String>();

		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
		}
		return set;
	}

	public static List<String> listToList(String list, char delimiter, boolean trim) {
		if (list.length() == 0) return new ArrayList<String>();
		int len = list.length();
		int last = 0;
		char c;

		ArrayList<String> rtn = new ArrayList<String>();
		for (int i = 0; i < len; i++) {
			c = list.charAt(i);
			if (c == delimiter) {
				rtn.add(trim ? list.substring(last, i).trim() : list.substring(last, i));
				last = i + 1;
			}
		}
		if (last <= len) rtn.add(list.substring(last));
		return rtn;
	}

	public static String first(String list, String delimiters, boolean ignoreEmpty, int count) {
		if (count < 1) return "";
		char[] delims = StringUtil.isEmpty(delimiters) ? new char[] { ',' } : delimiters.toCharArray();
		StringBuilder sbList = new StringBuilder(list);
		int ix = getDelimIndex(sbList, count, delims, ignoreEmpty);
		if (ix == -1) return list;
		return sbList.substring(0, ix);
	}

	public static String last(String list, String delimiters, boolean ignoreEmpty, int count) {
		if (count < 1) return "";
		char[] delims = StringUtil.isEmpty(delimiters) ? new char[] { ',' } : delimiters.toCharArray();
		StringBuilder sbList = rev(list);// new StringBuilder(list);
		int ix = getDelimIndex(sbList, count, delims, ignoreEmpty);
		if (ix == -1) return list;
		return rev(sbList.substring(0, ix)).toString();
	}

	private static StringBuilder rev(CharSequence list) {
		if (StringUtil.isEmpty(list)) return new StringBuilder();
		StringBuilder sb = new StringBuilder();
		for (int i = list.length() - 1; i >= 0; i--) {
			sb.append(list.charAt(i));
		}
		return sb;
	}

	public static String first(String list, String delimiters, boolean ignoreEmpty) {
		return first(list, delimiters, ignoreEmpty, 1);
	}

	public static String rest(String list, String delimiters, boolean ignoreEmpty, int offset) {
		if (offset < 1) return list;

		char[] delims = StringUtil.isEmpty(delimiters) ? new char[] { ',' } : delimiters.toCharArray();
		StringBuilder sbList = new StringBuilder(list);
		int ix = getDelimIndex(sbList, offset, delims, ignoreEmpty);

		if (ix == -1 || ix >= sbList.length() - 1) return "";

		return sbList.substring(ix + 1);
	}

	public static String rest(String list, String delimiters, boolean ignoreEmpty) {
		return rest(list, delimiters, ignoreEmpty, 1);
	}

	/**
	 * returns the 0-based delimiter position for the specified item
	 *
	 * @param list
	 * @param itemPos
	 * @param ignoreEmpty
	 * @return
	 */
	public static int getDelimIndex(StringBuilder sb, int itemPos, char[] delims, boolean ignoreEmpty) {

		if (StringUtil.isEmpty(sb)) return -1;

		int curr = -1, listIndex = 0;
		int len = sb.length();
		for (int i = 0; i < len; i++) {
			if (contains(delims, sb.charAt(i))) {
				curr = i;
				if (ignoreEmpty) {
					if (i == 0 || (i + 1 < len && contains(delims, sb.charAt(i + 1)))) {
						sb.delete(curr, curr + 1);
						len--;
						i--;
						curr--;
						continue;
					}
				}
				if (++listIndex == itemPos) break;
			}
		}
		if (listIndex < itemPos) return len;
		return curr;
	}

	private static boolean contains(char[] carr, char c) {
		if (carr.length == 1) return carr[0] == c;

		for (char ca: carr) {
			if (ca == c) return true;
		}
		return false;
	}

	public static List<String> toList(Set<String> set) {
		Iterator<String> it = set.iterator();
		List<String> list = new ArrayList<String>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	public static List<String> arrayToList(String[] arr) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < arr.length; i++) {
			list.add(arr[i]);
		}
		return list;
	}

	public static List<String> toList(Array arr) throws PageException {
		Iterator<Object> it = arr.valueIterator();
		List<String> list = new ArrayList<String>();
		while (it.hasNext()) {
			list.add(Caster.toString(it.next()));
		}
		return list;
	}

	public static Iterator<String> toIterator(Enumeration<String> input) {
		List<String> output = new ArrayList<String>();
		while (input.hasMoreElements()) {
			output.add(input.nextElement());
		}
		return output.iterator();
	}

}