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
import lucee.runtime.type.Collection;

public interface ListUtil {

	/**
	 * casts a list to Array object, the list can be have quoted (",') arguments and delimiter in this
	 * arguments are ignored. quotes are not removed example:
	 * listWithQuotesToArray("aab,a'a,b',a\"a,b\"",",","\"'") will be translated to
	 * ["aab","a'a,b'","a\"a,b\""]
	 * 
	 * @param list list to cast
	 * @param delimiter delimiter of the list
	 * @param quotes quotes of the list
	 * @return Array Object
	 */
	public Array listWithQuotesToArray(String list, String delimiter, String quotes);

	/**
	 * casts a list to Array object
	 * 
	 * @param list list to cast
	 * @param delimiter delimiter of the list
	 * @return Array Object
	 */
	public Array toArray(String list, String delimiter);

	public Array toArray(String list, String delimiter, boolean includeEmptyFields, boolean multiCharDelim);

	/**
	 * casts a list to Array object remove Empty Elements
	 * 
	 * @param list list to cast
	 * @param delimiter delimiter of the list
	 * @return Array Object
	 */
	public Array toArrayRemoveEmpty(String list, String delimiter);

	public List<String> toListRemoveEmpty(String list, char delimiter);

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list
	 * 
	 * @param list list to cast
	 * @param delimiter delimiter of the list
	 * @return Array Object
	 */
	public Array toArrayTrim(String list, String delimiter);

	/**
	 * casts a list to Array object, remove all empty items at start and end of the list and store count
	 * to info
	 * 
	 * @param list list to cast
	 * @param pos position
	 * @param value value
	 * @param delimiter delimiter of the list
	 * @param ignoreEmpty ignore empty
	 * @return Array Object
	 * @throws PageException Page Exception
	 */
	public String insertAt(String list, int pos, String value, String delimiter, boolean ignoreEmpty) throws PageException;

	/**
	 * finds a value inside a list, do not ignore case
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list (0-n) or -1
	 */
	public int findNoCase(String list, String value, String delimiter);

	/**
	 * finds a value inside a list, do not ignore case
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @param trim trim the list or not
	 * @return position in list (0-n) or -1
	 */
	public int findNoCase(String list, String value, String delimiter, boolean trim);

	public int findForSwitch(String list, String value, String delimiter);

	/**
	 * finds a value inside a list, ignore case, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public int findNoCaseIgnoreEmpty(String list, String value, String delimiter);

	/**
	 * finds a value inside a list, ignore case, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public int findNoCaseIgnoreEmpty(String list, String value, char delimiter);

	/**
	 * finds a value inside a list, case sensitive
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @return position in list or 0
	 */
	public int find(String list, String value);

	/**
	 * finds a value inside a list, do not case sensitive
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public int find(String list, String value, String delimiter);

	/**
	 * finds a value inside a list, case sensitive, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public int findIgnoreEmpty(String list, String value, String delimiter);

	/**
	 * finds a value inside a list, case sensitive, ignore empty items
	 * 
	 * @param list list to search
	 * @param value value to find
	 * @param delimiter delimiter of the list
	 * @return position in list or 0
	 */
	public int findIgnoreEmpty(String list, String value, char delimiter);

	/**
	 * returns if a value of the list contains given value, ignore case
	 * 
	 * @param list list to search in
	 * @param value value to search
	 * @param delimiter delimiter of the list
	 * @param includeEmptyFields include empty fields
	 * @param multiCharacterDelimiter multi character delimiter
	 * @return position in list or 0
	 */
	public int containsNoCase(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter);

	/**
	 * returns if a value of the list contains given value, case sensitive
	 * 
	 * @param list list to search in
	 * @param value value to search
	 * @param delimiter delimiter of the list
	 * @param includeEmptyFields include empty fields
	 * @param multiCharacterDelimiter multi character delimiter
	 
	 * @return position in list or 0
	 */
	public int contains(String list, String value, String delimiter, boolean includeEmptyFields, boolean multiCharacterDelimiter);

	/**
	 * convert a string array to string list, removes empty values at begin and end of the list
	 * 
	 * @param array array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string array
	 */
	public String toListTrim(String[] array, String delimiter);

	/**
	 * convert a string array to string list
	 * 
	 * @param array array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string array
	 */
	public String toList(String[] array, String delimiter);

	public String toList(Collection.Key[] array, String delimiter);

	/**
	 * convert Array Object to string list
	 * 
	 * @param array Array to convert
	 * @param delimiter delimiter for the new list
	 * @return list generated from string Array
	 * @throws PageException Page Exception
	 */
	public String toList(Array array, String delimiter) throws PageException;

	public String toList(java.util.List<?> list, String delimiter) throws PageException;

	/**
	 * input is already a String List, so no casting necessary
	 * 
	 * @param list List
	 * @param delimiter delimiter of the list
	 * @return Returns a list.
	 */
	public String toListEL(java.util.List<String> list, String delimiter);

	/**
	 * trims a string array, removes all empty array positions at the start and the end of the array
	 * 
	 * @param array array to remove elements
	 * @return cleared array
	 */
	public String[] trim(String[] array);

	/**
	 * trims a string list, remove all empty delimiter at start and the end
	 * 
	 * @param list list to trim
	 * @param delimiter delimiter of the list
	 * @param multiCharacterDelimiter is a delimeter with multiple character handled as ne character or
	 *            as many
	 * @return trimmed list
	 */
	public String trim(String list, String delimiter, boolean multiCharacterDelimiter);

	/**
	 * sorts a string list
	 * 
	 * @param list list to sort
	 * @param sortType sort type (numeric,text,textnocase)
	 * @param sortOrder sort order (asc,desc)
	 * @param delimiter list delimiter
	 * @return sorted list
	 * @throws PageException Page Exception
	 */
	public String sortIgnoreEmpty(String list, String sortType, String sortOrder, String delimiter) throws PageException;

	/**
	 * sorts a string list
	 * 
	 * @param list list to sort
	 * @param sortType sort type (numeric,text,textnocase)
	 * @param sortOrder sort order (asc,desc)
	 * @param delimiter list delimiter
	 * @return sorted list
	 * @throws PageException Page Exception
	 */
	public String sort(String list, String sortType, String sortOrder, String delimiter) throws PageException;

	/**
	 * cast an Object Array to a String Array
	 * 
	 * @param array Array to be casted
	 * @return String Array
	 * @throws PageException Page Exception
	 */
	public String[] toStringArray(Array array) throws PageException;

	public String[] toStringArray(Set<String> set);

	public String[] toStringArray(List<String> list);

	/**
	 * cast an Object Array to a String Array
	 * 
	 * @param array Array
	 * @param defaultValue default Value
	 * @return String Array
	 */
	public String[] toStringArray(Array array, String defaultValue);

	/**
	 * cast an Object Array to a String Array and trim all values
	 * 
	 * @param array Array
	 * @return String Array
	 * @throws PageException Page Exception
	 */
	public String[] toStringArrayTrim(Array array) throws PageException;

	/**
	 * return last element of the list
	 * 
	 * @param list List
	 * @param delimiter delimiter of the list
	 * @param ignoreEmpty ignore empty
	 * @return returns the last Element of a list
	 */
	public String last(String list, String delimiter, boolean ignoreEmpty);

	/**
	 * returns count of items in the list
	 * 
	 * @param list List
	 * @param delimiter delimiter of the list
	 * @param ignoreEmpty ignore empty
	 * @return list len
	 */
	public int len(String list, String delimiter, boolean ignoreEmpty);

	/**
	 * gets a value from list
	 * 
	 * @param list list to cast
	 * @param delimiter delimiter of the list
	 * @param position position
	 * @param ignoreEmpty ignore empty
	 * @param defaultValue default Value
	 * @return Array Object
	 */
	public String getAt(String list, String delimiter, int position, boolean ignoreEmpty, String defaultValue);

	public String[] toStringArray(String list, String delimiter);

	/**
	 * trim every single item of the Array
	 * 
	 * @param arr Array
	 * @return Returns a trimmed list.
	 */
	public String[] trimItems(String[] arr);

	/**
	 * trim every single item of the Array
	 * 
	 * @param arr Array
	 * @return Returns a trimmed list.
	 * @throws PageException Page Exception
	 */
	public Array trimItems(Array arr) throws PageException;

	public Set<String> toSet(String list, String delimiter, boolean trim);

	public Set<String> toSet(String[] arr);

	public String first(String list, String delimiters, boolean ignoreEmpty, int count);

	public String first(String list, String delimiters, boolean ignoreEmpty);

	public String rest(String list, String delimiters, boolean ignoreEmpty, int offset);

	public String rest(String list, String delimiters, boolean ignoreEmpty);

	/**
	 * returns the 0-based delimiter position for the specified item
	 * 
	 * @param list List
	 * @param itemPos Item Position
	 * @param delims delimiters of the list
	 * @param ignoreEmpty Ignore Empty
	 * @return Returns the delimiter position.
	 */
	public int getDelimIndex(String list, int itemPos, char[] delims, boolean ignoreEmpty);

	public List<String> toList(Set<String> set);

	public List<String> toList(String[] arr);
}