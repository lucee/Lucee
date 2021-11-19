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
package lucee.commons.lang;

import java.util.ArrayList;

/**
 * a Simple single direction string list
 */
public final class StringList {

	private final Entry root = new Entry(null, Entry.NUL);
	private Entry curr;
	private int count = 0;

	/**
	 * constructor of the class
	 */
	public StringList() {
		curr = root;
	}

	/**
	 * constructor of the class
	 * 
	 * @param str String Element
	 */
	public StringList(String str) {
		root.next = new Entry(str, Entry.NUL);
		curr = root.next;
		count = 1;
	}

	/**
	 * constructor of the class, initalize with 2 values
	 * 
	 * @param str1
	 * @param str2
	 */
	public StringList(String str1, String str2) {
		this(str1);
		add(str2);
	}

	/**
	 * @return returns if List has a next Element
	 */
	public boolean hasNext() {
		return curr.next != null;
	}

	/**
	 * @return returns if List has a next Element
	 */
	public boolean hasNextNext() {
		return curr.next != null && curr.next.next != null;
	}

	/**
	 * @return returns next element in the list
	 */
	public String next() {
		curr = curr.next;
		return curr.data;
	}

	public char delimiter() {
		return curr.delimiter;
	}

	/**
	 * @return returns current element in the list
	 */
	public String current() {
		return curr.data;
	}

	/**
	 * reset the String List
	 * 
	 * @return
	 */
	public StringList reset() {
		curr = root;
		return this;
	}

	/**
	 * @return returns the size of the list
	 */
	public int size() {
		return count;
	}

	/**
	 * adds an element to the list
	 * 
	 * @param str String Element to add
	 */
	public void add(String str) {
		curr.next = new Entry(str, Entry.NUL);
		curr = curr.next;
		count++;
	}

	public void add(String str, char delimiter) {
		curr.next = new Entry(str, delimiter);
		curr = curr.next;
		count++;
	}

	private class Entry {
		private static final char NUL = (char) 0;
		private Entry next;
		private String data;
		private char delimiter;

		private Entry(String data, char delimiter) {
			this.data = data;
			this.delimiter = delimiter;
		}
	}

	public String[] toArray() {
		ArrayList<String> list = new ArrayList<String>();
		while (hasNext()) {
			list.add(next());
		}
		return list.toArray(new String[list.size()]);
	}
}