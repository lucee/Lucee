/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.op;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.Strings;

public class StringsImpl implements Strings {

	private static Strings singelton;

	public static Strings getInstance() {
		if (singelton == null) singelton = new StringsImpl();
		return singelton;
	}

	@Override
	public String replace(String input, String find, String repl, boolean firstOnly, boolean ignoreCase) {
		return StringUtil.replace(input, find, repl, firstOnly, ignoreCase);
	}

	@Override
	public String toVariableName(String str, boolean addIdentityNumber, boolean allowDot) {
		return StringUtil.toVariableName(str, addIdentityNumber, allowDot);
	}

	@Override
	public String first(String list, String delimiter, boolean ignoreEmpty) {
		return ListUtil.first(list, delimiter, ignoreEmpty);
	}

	@Override
	public String last(String list, String delimiter, boolean ignoreEmpty) {
		return ListUtil.last(list, delimiter, ignoreEmpty);
	}

	@Override
	public String removeQuotes(String str, boolean trim) {
		return StringUtil.removeQuotes(str, trim);
	}

	@Override
	public long create64BitHash(CharSequence cs) {
		return HashUtil.create64BitHash(cs);
	}

	@Override
	public boolean isEmpty(String str) {
		return StringUtil.isEmpty(str);
	}

	@Override
	public boolean isEmpty(String str, boolean trim) {
		return StringUtil.isEmpty(str, trim);
	}

	@Override
	public String emptyIfNull(String str) {
		return StringUtil.emptyIfNull(str);
	}

	@Override
	public boolean startsWithIgnoreCase(String haystack, String needle) {
		return StringUtil.startsWithIgnoreCase(haystack, needle);
	}

	@Override
	public boolean endsWithIgnoreCase(String haystack, String needle) {
		return StringUtil.endsWithIgnoreCase(haystack, needle);
	}

	@Override
	public String ucFirst(String str) {
		return StringUtil.ucFirst(str);
	}
}