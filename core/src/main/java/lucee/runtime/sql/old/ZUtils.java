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

package lucee.runtime.sql.old;

import java.util.Hashtable;

public final class ZUtils {
	public static void init() {
		if (fcts_ != null) return;
		fcts_ = new Hashtable();
		// addCustomFunction("upper",1);
		addCustomFunction("lower", 1);
		addCustomFunction("lcase", 1);
		addCustomFunction("ucase", 1);
		addCustomFunction("abs", 1);
		addCustomFunction("acos", 1);
		addCustomFunction("asin", 1);
		addCustomFunction("atan", 1);
		addCustomFunction("atan2", 2);
		addCustomFunction("bitand", 2);
		addCustomFunction("bitor", 2);
		addCustomFunction("ceiling", 1);
		addCustomFunction("cos", 1);
		addCustomFunction("cot", 1);
		addCustomFunction("degrees", 1);
		addCustomFunction("exp", 1);
		addCustomFunction("floor", 1);
		addCustomFunction("log", 1);
		addCustomFunction("log10", 1);
		addCustomFunction("mod", 2);
		addCustomFunction("pi", 0);
		addCustomFunction("power", 2);
		addCustomFunction("radians", 1);
		addCustomFunction("rand", 0);
		addCustomFunction("round", 2);
		addCustomFunction("roundmagic", 1);
		addCustomFunction("sign", 1);
		addCustomFunction("sin", 1);
		addCustomFunction("sqrt", 1);
		addCustomFunction("tan", 1);
		addCustomFunction("truncate", 2);
		addCustomFunction("ascii", 1);
		addCustomFunction("bit_length", 1);
		addCustomFunction("char", 1);
		addCustomFunction("char_length", 1);
		addCustomFunction("concat", 2);
		addCustomFunction("difference", 2);
		addCustomFunction("hextoraw", 1);
		addCustomFunction("insert", 4);
		addCustomFunction("lcase", 1);
		addCustomFunction("left", 2);
		addCustomFunction("length", 1);
		addCustomFunction("locate", 3);
		addCustomFunction("ltrim", 1);
		addCustomFunction("octet_length", 1);
		addCustomFunction("rawtohex", 1);
		addCustomFunction("repeat", 2);
		addCustomFunction("replace", 3);
		addCustomFunction("right", 2);
		addCustomFunction("rtrim", 1);
		addCustomFunction("soundex", 1);
		addCustomFunction("space", 1);
		addCustomFunction("substr", 3);
		addCustomFunction("substring", 3);
		addCustomFunction("ucase", 1);
		addCustomFunction("lower", 1);
		addCustomFunction("upper", 1);
		addCustomFunction("curdate", 0);
		addCustomFunction("curtime", 0);
		addCustomFunction("datediff", 3);
		addCustomFunction("dayname", 1);
		addCustomFunction("dayofmonth", 1);
		addCustomFunction("dayofweek", 1);
		addCustomFunction("dayofyear", 1);
		addCustomFunction("hour", 1);
		addCustomFunction("minute", 1);
		addCustomFunction("month", 1);
		addCustomFunction("monthname", 1);
		addCustomFunction("now", 0);
		addCustomFunction("quarter", 1);
		addCustomFunction("second", 1);
		addCustomFunction("week", 1);
		addCustomFunction("year", 1);
		addCustomFunction("current_date", 1);
		addCustomFunction("current_time", 1);
		addCustomFunction("current_timestamp", 1);
		addCustomFunction("database", 0);
		addCustomFunction("user", 0);
		addCustomFunction("current_user", 0);
		addCustomFunction("identity", 0);
		addCustomFunction("ifnull", 2);
		addCustomFunction("casewhen", 3);
		addCustomFunction("convert", 2);
		// addCustomFunction("cast",1);
		addCustomFunction("coalesce", 1000);
		addCustomFunction("nullif", 2);
		addCustomFunction("extract", 1);
		addCustomFunction("position", 1);
		addCustomFunction("trim", 1);
		// LOCATE(search,s,[start])
		// SUBSTR(s,start[,len])
		// SUBSTRING(s,start[,len])
		// COALESCE(expr1,expr2,expr3,...)[1]

	}

	public ZUtils() {
	}

	public static void addCustomFunction(String s, int i) {
		if (fcts_ == null) fcts_ = new Hashtable();
		if (i <= 0) i = 1;
		fcts_.put(s.toUpperCase(), Integer.valueOf(i));
	}

	public static int isCustomFunction(String s) {
		init();
		Integer integer;
		if (s == null || s.length() < 1 || fcts_ == null || (integer = (Integer) fcts_.get(s.toUpperCase())) == null) return -1;

		return integer.intValue();
	}

	public static boolean isAggregate(String s) {
		s = s.toUpperCase().trim();
		return s.equals("SUM") || s.equals("AVG") || s.equals("MAX") || s.equals("MIN") || s.equals("COUNT") || fcts_ != null && fcts_.get(s) != null;
	}

	public static String getAggregateCall(String s) {
		int i = s.indexOf('(');
		if (i <= 0) return null;
		String s1 = s.substring(0, i);
		if (isAggregate(s1)) return s1.trim();
		return null;
	}

	private static Hashtable fcts_ = null;

}