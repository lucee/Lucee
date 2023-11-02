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
package lucee.commons.io.res.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lucee.commons.lang.StringUtil;

/**
 * a WildcardPattern that accepts a comma- (or semi-colon-) separated value of patterns, e.g.
 * "*.gif, *.jpg, *.jpeg, *.png" and an optional isExclude boolean value which negates the results
 * of the default implementation
 * 
 * also, lines 31 - 35 allow to set isExclude to true by passing a pattern whose first character is
 * an exclamation point '!'
 * 
 * @author Igal
 */
public class WildcardPattern {

	private final String pattern;
	private final boolean isInclude;

	private final List<ParsedPattern> patterns;

	/**
	 * 
	 * @param pattern - the wildcard pattern, or a comma/semi-colon separated value of wildcard patterns
	 * @param isCaseSensitive - if true, does a case-sensitive matching
	 * @param isExclude - if true, the filter becomes an Exclude filter so that only items that do not
	 *            match the pattern are accepted
	 */
	public WildcardPattern(String pattern, boolean isCaseSensitive, boolean isExclude, String delimiters) {

		if (pattern.charAt(0) == '!') { // set isExclude to true if the first char of pattern is an exclamation point '!'

			pattern = pattern.substring(1);
			isExclude = true;
		}

		this.pattern = pattern;
		this.isInclude = !isExclude;

		StringTokenizer tokenizer = new StringTokenizer(pattern, !StringUtil.isEmpty(delimiters, true) ? delimiters : "|");

		patterns = new ArrayList<ParsedPattern>();

		while (tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken().trim();

			if (!token.isEmpty()) patterns.add(new ParsedPattern(token, isCaseSensitive));
		}
	}

	/** calls this( pattern, isCaseSensitive, false, delimiters ); */
	public WildcardPattern(String pattern, boolean isCaseSensitive, String delimiters) {

		this(pattern, isCaseSensitive, false, delimiters);
	}

	public boolean isMatch(String input) {

		for (ParsedPattern pp: this.patterns) {

			if (pp.isMatch(input)) return isInclude;
		}

		return !isInclude;
	}

	@Override
	public String toString() {

		return "WildcardPattern: " + pattern;
	}

	public static class ParsedPattern {

		public final static String MATCH_ANY = "*";
		public final static String MATCH_ONE = "?";

		private String[] parts;
		private final boolean isCaseSensitive;

		public ParsedPattern(String pattern, boolean isCaseSensitive) {

			this.isCaseSensitive = isCaseSensitive;

			if (!isCaseSensitive) pattern = pattern.toLowerCase();

			List<String> lsp = new ArrayList<String>();

			int len = pattern.length();
			int subStart = 0;

			for (int i = subStart; i < len; i++) {

				char c = pattern.charAt(i);

				if (c == '*' || c == '?') {

					if (i > subStart) lsp.add(pattern.substring(subStart, i));

					lsp.add(c == '*' ? MATCH_ANY : MATCH_ONE);
					subStart = i + 1;
				}
			}

			if (len > subStart) lsp.add(pattern.substring(subStart));

			this.parts = lsp.toArray(new String[lsp.size()]);
		}

		/** calls this( pattern, false, false ); */
		public ParsedPattern(String pattern) {

			this(pattern, false);
		}

		/** tests if the input string matches the pattern */
		public boolean isMatch(String input) {

			if (!isCaseSensitive) input = input.toLowerCase();

			if (parts.length == 1) return (parts[0] == MATCH_ANY || parts[0].equals(input));

			if (parts.length == 2) {

				if (parts[0] == MATCH_ANY) return input.endsWith(parts[1]);

				if (parts[parts.length - 1] == MATCH_ANY) return input.startsWith(parts[0]);
			}

			int pos = 0;
			int len = input.length();

			boolean doMatchAny = false;

			for (String part: parts) {

				if (part == MATCH_ANY) {

					doMatchAny = true;
					continue;
				}

				if (part == MATCH_ONE) {

					doMatchAny = false;
					pos++;
					continue;
				}

				int ix = input.indexOf(part, pos);

				if (ix == -1) return false;

				if (!doMatchAny && ix != pos) return false;

				pos = ix + part.length();
				doMatchAny = false;
			}

			if ((parts[parts.length - 1] != MATCH_ANY) && (len != pos)) // if pattern doesn't end with * then we shouldn't have any more characters in input
				return false;

			return true;
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();

			for (String s: parts)
				sb.append(s);

			return sb.toString();
		}
	}
}