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
import java.util.StringTokenizer;
import java.util.Vector;

public final class ZTuple {

	public ZTuple() {
		attributes_ = new Vector();
		values_ = new Vector();
		searchTable_ = new Hashtable();
	}

	public ZTuple(String s) {
		this();
		for (StringTokenizer stringtokenizer = new StringTokenizer(s, ","); stringtokenizer.hasMoreTokens(); setAtt(stringtokenizer.nextToken().trim(), null)) {
		}
	}

	public void setRow(String s) {
		StringTokenizer stringtokenizer = new StringTokenizer(s, ",");
		for (int i = 0; stringtokenizer.hasMoreTokens(); i++) {
			String s1 = stringtokenizer.nextToken().trim();
			try {
				Double double1 = new Double(s1);
				setAtt(getAttName(i), double1);
			}
			catch (Exception exception) {
				setAtt(getAttName(i), s1);
			}
		}

	}

	public void setRow(Vector vector) {
		for (int i = 0; i < vector.size(); i++)
			setAtt(getAttName(i), vector.elementAt(i));

	}

	public void setAtt(String s, Object obj) {
		if (s != null) {
			boolean flag = searchTable_.containsKey(s);
			if (flag) {
				int i = ((Integer) searchTable_.get(s)).intValue();
				values_.setElementAt(obj, i);
			}
			else {
				int j = attributes_.size();
				attributes_.addElement(s);
				values_.addElement(obj);
				searchTable_.put(s, Integer.valueOf(j));
			}
		}
	}

	public String getAttName(int i) {
		try {
			return (String) attributes_.elementAt(i);
		}
		catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
			return null;
		}
	}

	public int getAttIndex(String s) {
		if (s == null) return -1;
		Integer integer = (Integer) searchTable_.get(s);
		if (integer != null) return integer.intValue();
		return -1;
	}

	public Object getAttValue(int i) {
		try {
			return values_.elementAt(i);
		}
		catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
			return null;
		}
	}

	public Object getAttValue(String s) {
		boolean flag = false;
		if (s != null) flag = searchTable_.containsKey(s);
		if (flag) {
			int i = ((Integer) searchTable_.get(s)).intValue();
			return values_.elementAt(i);
		}
		return null;

	}

	public boolean isAttribute(String s) {
		if (s != null) return searchTable_.containsKey(s);
		return false;
	}

	public int getNumAtt() {
		return values_.size();
	}

	@Override
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer();
		stringbuffer.append("[");
		if (attributes_.size() > 0) {
			Object obj = attributes_.elementAt(0);
			String s;
			if (obj == null) s = "(null)";
			else s = obj.toString();
			Object obj2 = values_.elementAt(0);
			String s2;
			if (obj2 == null) s2 = "(null)";
			else s2 = obj2.toString();
			stringbuffer.append(s + " = " + s2);
		}
		for (int i = 1; i < attributes_.size(); i++) {
			Object obj1 = attributes_.elementAt(i);
			String s1;
			if (obj1 == null) s1 = "(null)";
			else s1 = obj1.toString();
			Object obj3 = values_.elementAt(i);
			String s3;
			if (obj3 == null) s3 = "(null)";
			else s3 = obj3.toString();
			stringbuffer.append(", " + s1 + " = " + s3);
		}

		stringbuffer.append("]");
		return stringbuffer.toString();
	}

	private Vector attributes_;
	private Vector values_;
	private Hashtable searchTable_;
}