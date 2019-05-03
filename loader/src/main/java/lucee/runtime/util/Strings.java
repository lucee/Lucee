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
package lucee.runtime.util;

public interface Strings {

	/**
	 * performs a replace operation on a string
	 * 
	 * @param input - the string input to work on
	 * @param find - the substring to find
	 * @param repl - the substring to replace the matches with
	 * @param firstOnly - if true then only the first occurrence of {@code find} will be replaced
	 * @param ignoreCase - if true then matches will not be case sensitive
	 * @return
	 */
	public String replace(String input, String find, String repl, boolean firstOnly, boolean ignoreCase);

	public String toVariableName(String str, boolean addIdentityNumber, boolean allowDot);

	/**
	 * return first element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @param ignoreEmpty
	 * @return returns the first element of the list
	 */
	public String first(String list, String delimiter, boolean ignoreEmpty);

	/**
	 * return last element of the list
	 * 
	 * @param list
	 * @param delimiter
	 * @param ignoreEmpty
	 * @return returns the last Element of a list
	 */
	public String last(String list, String delimiter, boolean ignoreEmpty);

	/**
	 * removes quotes(",') that wraps the string
	 * 
	 * @param string
	 * @return
	 */
	public String removeQuotes(String string, boolean trim);

	public long create64BitHash(CharSequence cs);

	public boolean isEmpty(String str);

	public boolean isEmpty(String str, boolean trim);

	public String emptyIfNull(String str);

	public boolean startsWithIgnoreCase(String haystack, String needle);

	public boolean endsWithIgnoreCase(String haystack, String needle);

	public String ucFirst(String str);
}