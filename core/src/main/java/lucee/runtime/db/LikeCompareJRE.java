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
			if (escape.length() > 1) throw new DatabaseException("Invalid escape character [" + escape + "] has been specified in a LIKE conditional.  Escape char must be a single character.", null, sql, null);
		}

		StringBuilder sb = new StringBuilder(wildcard.length());
		int len = wildcard.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = wildcard.charAt(i);
			if (c == esc) {
				// If we aren't at the end of the string grab the next char
				// An escape char at the end of the string gets used as a literal
				if (i + 1 < len) {
					c = wildcard.charAt(++i);
				}
				escapeForRegex( sb, c );
			}
			else {
				if (c == '%') sb.append(".*");
				else if (c == '_') sb.append('.');
				else if (c == '[') {
					sb.append(c);
					// If we just opened unescaped brackets, check for a ^
					// All other ^ chars are treated like normal
					if (i + 1 < len && wildcard.charAt(i+1) == '^' ) {
						i++;
						sb.append('^');
					}
				} else if ( c == ']') sb.append(c);
				else escapeForRegex( sb, c );
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

	/**
	 * Consolidate the logic for escaping regex metachars
	 * @param sb The string builder to append to
	 * @param c The char to append
	 */
	private static void escapeForRegex( StringBuilder sb, char c ) {
		// If we have a regex metachar, escape it
		if (specials.indexOf(c) != -1) {
			sb.append('\\').append(c);
		} else {
			sb.append(c);	
		}
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