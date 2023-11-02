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

import java.util.List;
import java.util.Set;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;

public class ListUtilImpl implements ListUtil {

	@Override
	public Array listWithQuotesToArray(String list, String delimiter, String quotes) {
		return lucee.runtime.type.util.ListUtil.listWithQuotesToArray(list, delimiter, quotes);
	}

	@Override
	public Array toArray(String list, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listToArray(list, delimiter);
	}

	@Override
	public Array toArray(String list, String delimiter, boolean includeEmptyFields, boolean multiCharDelim) {
		return lucee.runtime.type.util.ListUtil.listToArray(list, delimiter, includeEmptyFields, multiCharDelim);
	}

	@Override
	public Array toArrayRemoveEmpty(String list, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(list, delimiter);
	}

	@Override
	public List<String> toListRemoveEmpty(String list, char delimiter) {
		return lucee.runtime.type.util.ListUtil.toListRemoveEmpty(list, delimiter);
	}

	@Override
	public Array toArrayTrim(String list, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listToArrayTrim(list, delimiter);
	}

	@Override
	public String insertAt(String list, int pos, String value, String delimiter, boolean ignoreEmpty) throws PageException {
		return lucee.runtime.type.util.ListUtil.listInsertAt(list, pos, value, delimiter, ignoreEmpty);
	}

	@Override
	public int findNoCase(String list, String value, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindNoCase(list, value, delimiter);
	}

	@Override
	public int findNoCase(String list, String value, String delimiter, boolean trim) {
		return lucee.runtime.type.util.ListUtil.listFindNoCase(list, value, delimiter);
	}

	@Override
	public int findForSwitch(String list, String value, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindForSwitch(list, value, delimiter);
	}

	@Override
	public int findNoCaseIgnoreEmpty(String list, String value, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindNoCaseIgnoreEmpty(list, value, delimiter);
	}

	@Override
	public int findNoCaseIgnoreEmpty(String list, String value, char delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindNoCaseIgnoreEmpty(list, value, delimiter);
	}

	@Override
	public int find(String list, String value) {
		return lucee.runtime.type.util.ListUtil.listFind(list, value);
	}

	@Override
	public int find(String list, String value, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listFind(list, value, delimiter);
	}

	@Override
	public int findIgnoreEmpty(String list, String value, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindIgnoreEmpty(list, value, delimiter);
	}

	@Override
	public int findIgnoreEmpty(String list, String value, char delimiter) {
		return lucee.runtime.type.util.ListUtil.listFindIgnoreEmpty(list, value, delimiter);
	}

	@Override
	public int containsNoCase(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		return lucee.runtime.type.util.ListUtil.listContainsNoCase(list, value, delimiter, includeEmptyFields, multiCharacterDelimiter);
	}

	@Override
	public int contains(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter) {
		return lucee.runtime.type.util.ListUtil.listContains(list, value, delimiter, includeEmptyFields, multiCharacterDelimiter);
	}

	@Override
	public String toListTrim(String[] array, String delimiter) {
		return lucee.runtime.type.util.ListUtil.arrayToListTrim(array, delimiter);
	}

	@Override
	public String toList(String[] array, String delimiter) {
		// TODO Auto-generated method stub
		return lucee.runtime.type.util.ListUtil.arrayToList(array, delimiter);
	}

	@Override
	public String toList(Key[] array, String delimiter) {
		return lucee.runtime.type.util.ListUtil.arrayToList(array, delimiter);
	}

	@Override
	public String toList(Array array, String delimiter) throws PageException {
		return lucee.runtime.type.util.ListUtil.arrayToList(array, delimiter);
	}

	@Override
	public String toList(List<?> list, String delimiter) throws PageException {
		return lucee.runtime.type.util.ListUtil.listToList(list, delimiter);
	}

	@Override
	public String toListEL(List<String> list, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listToListEL(list, delimiter);
	}

	@Override
	public String[] trim(String[] array) {
		return lucee.runtime.type.util.ListUtil.trim(array);
	}

	@Override
	public String trim(String list, String delimiter, boolean multiCharacterDelimiter) {
		return lucee.runtime.type.util.ListUtil.trim(list, delimiter, multiCharacterDelimiter);
	}

	@Override
	public String sortIgnoreEmpty(String list, String sortType, String sortOrder, String delimiter) throws PageException {
		return lucee.runtime.type.util.ListUtil.sortIgnoreEmpty(list, sortType, sortOrder, delimiter);
	}

	@Override
	public String sort(String list, String sortType, String sortOrder, String delimiter) throws PageException {
		return lucee.runtime.type.util.ListUtil.sort(list, sortType, sortOrder, delimiter);
	}

	@Override
	public String[] toStringArray(Array array) throws PageException {
		return lucee.runtime.type.util.ListUtil.toStringArray(array);
	}

	@Override
	public String[] toStringArray(Set<String> set) {
		return lucee.runtime.type.util.ListUtil.toStringArray(set);
	}

	@Override
	public String[] toStringArray(List<String> list) {
		return lucee.runtime.type.util.ListUtil.toStringArray(list);
	}

	@Override
	public String[] toStringArray(Array array, String defaultValue) {
		return lucee.runtime.type.util.ListUtil.toStringArray(array, defaultValue);
	}

	@Override
	public String[] toStringArrayTrim(Array array) throws PageException {
		return lucee.runtime.type.util.ListUtil.toStringArrayTrim(array);
	}

	@Override
	public String last(String list, String delimiter, boolean ignoreEmpty) {
		return lucee.runtime.type.util.ListUtil.last(list, delimiter, ignoreEmpty);
	}

	@Override
	public int len(String list, String delimiter, boolean ignoreEmpty) {
		return lucee.runtime.type.util.ListUtil.len(list, delimiter, ignoreEmpty);
	}

	@Override
	public String getAt(String list, String delimiter, int position, boolean ignoreEmpty, String defaultValue) {
		return lucee.runtime.type.util.ListUtil.getAt(list, delimiter, position, ignoreEmpty, defaultValue);
	}

	@Override
	public String[] toStringArray(String list, String delimiter) {
		return lucee.runtime.type.util.ListUtil.listToStringArray(list, delimiter);
	}

	@Override
	public String[] trimItems(String[] arr) {
		return lucee.runtime.type.util.ListUtil.trimItems(arr);
	}

	@Override
	public Array trimItems(Array arr) throws PageException {
		return lucee.runtime.type.util.ListUtil.trimItems(arr);
	}

	@Override
	public Set<String> toSet(String list, String delimiter, boolean trim) {
		return lucee.runtime.type.util.ListUtil.listToSet(list, delimiter, trim);
	}

	@Override
	public Set<String> toSet(String[] arr) {
		return lucee.runtime.type.util.ListUtil.toSet(arr);
	}

	@Override
	public String first(String list, String delimiters, boolean ignoreEmpty, int count) {
		return lucee.runtime.type.util.ListUtil.first(list, delimiters, ignoreEmpty, count);
	}

	@Override
	public String first(String list, String delimiters, boolean ignoreEmpty) {
		return lucee.runtime.type.util.ListUtil.first(list, delimiters, ignoreEmpty);
	}

	@Override
	public String rest(String list, String delimiters, boolean ignoreEmpty, int offset) {
		return lucee.runtime.type.util.ListUtil.rest(list, delimiters, ignoreEmpty, offset);
	}

	@Override
	public String rest(String list, String delimiters, boolean ignoreEmpty) {
		return lucee.runtime.type.util.ListUtil.rest(list, delimiters, ignoreEmpty);
	}

	@Override
	public int getDelimIndex(String list, int itemPos, char[] delims, boolean ignoreEmpty) {
		return lucee.runtime.type.util.ListUtil.getDelimIndex(new StringBuilder(list), itemPos, delims, ignoreEmpty);
	}

	@Override
	public List<String> toList(Set<String> set) {
		return lucee.runtime.type.util.ListUtil.toList(set);
	}

	@Override
	public List<String> toList(String[] arr) {
		return lucee.runtime.type.util.ListUtil.arrayToList(arr);
	}

}