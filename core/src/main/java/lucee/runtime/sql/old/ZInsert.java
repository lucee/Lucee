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

//Source File Name:   ZInsert.java

package lucee.runtime.sql.old;

import java.util.Vector;

//         ZExpression, ZQuery, ZStatement, ZExp

public final class ZInsert implements ZStatement {

	public ZInsert(String s) {
		columns_ = null;
		valueSpec_ = null;
		table_ = new String(s);
	}

	public String getTable() {
		return table_;
	}

	public Vector getColumns() {
		return columns_;
	}

	public void addColumns(Vector vector) {
		columns_ = vector;
	}

	public void addValueSpec(ZExp zexp) {
		valueSpec_ = zexp;
	}

	public Vector getValues() {
		if (!(valueSpec_ instanceof ZExpression)) return null;
		return ((ZExpression) valueSpec_).getOperands();
	}

	public ZQuery getQuery() {
		if (!(valueSpec_ instanceof ZQuery)) return null;
		return (ZQuery) valueSpec_;
	}

	@Override
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer("insert into " + table_);
		if (columns_ != null && columns_.size() > 0) {
			stringbuffer.append("(" + columns_.elementAt(0));
			for (int i = 1; i < columns_.size(); i++)
				stringbuffer.append("," + columns_.elementAt(i));

			stringbuffer.append(")");
		}
		String s = valueSpec_.toString();
		stringbuffer.append(" ");
		if (getValues() != null) stringbuffer.append("values ");
		if (s.startsWith("(")) stringbuffer.append(s);
		else stringbuffer.append(" (" + s + ")");
		return stringbuffer.toString();
	}

	String table_;
	Vector columns_;
	ZExp valueSpec_;
}