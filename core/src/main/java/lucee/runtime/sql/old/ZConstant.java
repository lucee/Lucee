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

//         ZExp

public final class ZConstant implements ZExp {

	public ZConstant(String s, int i) {
		// if(s.indexOf("12:00:00")!=-1)print.ds("init:"+s);
		type_ = -1;
		val_ = null;
		val_ = new String(s);
		type_ = i;
	}

	public String getValue() {
		return val_;
	}

	public int getType() {
		return type_;
	}

	@Override
	public String toString() {
		if (type_ == 3) return '\'' + val_ + '\'';
		return val_;
	}

	public static final int UNKNOWN = -1;
	public static final int COLUMNNAME = 0;
	public static final int NULL = 1;
	public static final int NUMBER = 2;
	public static final int STRING = 3;
	int type_;
	String val_;
}