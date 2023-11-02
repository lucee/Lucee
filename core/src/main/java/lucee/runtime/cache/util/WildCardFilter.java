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
package lucee.runtime.cache.util;

import java.util.regex.Pattern;

import org.apache.oro.text.regex.MalformedPatternException;

import lucee.commons.io.cache.CacheKeyFilter;

/**
 * Wildcard Filter
 */
public class WildCardFilter implements CacheKeyFilter {

	private static final String specials = "{}[]().+\\^$";

	private final Pattern pattern;
	private final String wildcard;

	private boolean ignoreCase;

	/**
	 * @param wildcard
	 * @throws MalformedPatternException
	 */
	public WildCardFilter(String wildcard, boolean ignoreCase) {
		this.wildcard = wildcard;
		this.ignoreCase = ignoreCase;
		StringBuilder sb = new StringBuilder(wildcard.length());
		int len = wildcard.length();

		for (int i = 0; i < len; i++) {
			char c = wildcard.charAt(i);
			if (c == '*') sb.append(".*");
			else if (c == '?') sb.append('.');
			else if (specials.indexOf(c) != -1) sb.append('\\').append(c);
			else sb.append(c);
		}
		pattern = Pattern.compile(ignoreCase ? sb.toString().toLowerCase() : sb.toString());
		// pattern=new Perl5Compiler().compile(ignoreCase?sb.toString().toLowerCase():sb.toString());
	}

	@Override
	public boolean accept(String key) {
		return pattern.matcher(ignoreCase ? key.toLowerCase() : key).matches();
	}

	@Override
	public String toString() {
		return "Wildcardfilter:" + wildcard;
	}

	@Override
	public String toPattern() {
		return wildcard;
	}
}