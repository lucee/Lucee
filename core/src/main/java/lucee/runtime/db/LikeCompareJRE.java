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
package lucee.runtime.db;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * Wildcard Filter
 */
class LikeCompareJRE {

	private static final String specials = "{}[]().?+\\^$";
	private static Map patterns = new WeakHashMap();

	private static Pattern createPattern(SQL sql, String wildcard, String escape) throws PageException {
		Pattern pattern = (Pattern) patterns.get(wildcard + escape);
		if (pattern != null) return pattern;
		char esc = 0;
		if (!StringUtil.isEmpty(escape)) {
			esc = escape.charAt(0);
			if (escape.length() > 1) throw new DatabaseException("Invalid escape character [" + escape + "] has been specified in a LIKE conditional", null, sql, null);
		}

		StringBuilder sb = new StringBuilder(wildcard.length());
		int len = wildcard.length();
		// boolean isEscape=false;
		char c;
		for (int i = 0; i < len; i++) {
			c = wildcard.charAt(i);
			if (c == esc) {
				if (i + 1 == len)
					throw new DatabaseException("Invalid Escape Sequence. Valid sequence pairs for this escape character are: [" + esc + "%] or [" + esc + "_]", null, sql, null);
				c = wildcard.charAt(++i);
				if (c == '%') sb.append(c);
				else if (c == '_') sb.append(c);
				else throw new DatabaseException(
						"Invalid Escape Sequence [" + esc + "" + c + "]. Valid sequence pairs for this escape character are: [" + esc + "%] or [" + esc + "_]", null, sql, null);
			}
			else {
				if (c == '%') sb.append(".*");
				else if (c == '_') sb.append('.');
				else if (specials.indexOf(c) != -1) sb.append('\\').append(c);
				else sb.append(c);
			}

		}
		try {
			patterns.put(wildcard + escape, pattern = Pattern.compile(sb.toString(), Pattern.DOTALL));
		}
		catch (PatternSyntaxException e) {
			throw Caster.toPageException(e);
		}
		return pattern;
	}

	public static boolean like(SQL sql, String haystack, String needle) throws PageException {
		return like(sql, haystack, needle, null);
	}

	public static boolean like(SQL sql, String haystack, String needle, String escape) throws PageException {
		haystack = StringUtil.toLowerCase(haystack);
		Pattern p = createPattern(sql, StringUtil.toLowerCase(needle), escape == null ? null : StringUtil.toLowerCase(escape));
		return p.matcher(haystack).matches();
	}

}