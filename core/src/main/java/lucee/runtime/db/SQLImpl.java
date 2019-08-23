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
package lucee.runtime.db;

import java.io.Serializable;

/**
 * represents a SQL Statement with his defined arguments for a prepared statement
 */
public final class SQLImpl implements SQL, Serializable {

	private String strSQL;
	private SQLItem[] items;
	private int position = 0;

	/**
	 * Constructor only with SQL String
	 * 
	 * @param strSQL SQL String
	 */
	public SQLImpl(String strSQL) {
		this.strSQL = strSQL;
		this.items = new SQLItem[0];
	}

	/**
	 * Constructor with SQL String and SQL Items
	 * 
	 * @param strSQL SQL String
	 * @param items SQL Items
	 */
	public SQLImpl(String strSQL, SQLItem[] items) {
		this.strSQL = strSQL;
		this.items = items == null ? new SQLItem[0] : items;
	}

	public void addItems(SQLItem item) {
		SQLItem[] tmp = new SQLItem[items.length + 1];
		for (int i = 0; i < items.length; i++) {
			tmp[i] = items[i];
		}
		tmp[items.length] = item;
		items = tmp;
	}

	@Override
	public SQLItem[] getItems() {
		return items;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public String getSQLString() {
		return strSQL;
	}

	@Override
	public void setSQLString(String strSQL) {
		this.strSQL = strSQL;
	}

	/**
	 * populates the SQL string with values from parameters
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		if (items.length == 0) return strSQL;

		StringBuilder sb = new StringBuilder(256);
		int pos, last = 0;
		for (int i = 0; i < items.length; i++) {

			pos = strSQL.indexOf('?', last);
			if (pos == -1) {
				sb.append(strSQL.substring(last));
				break;
			}

			if (pos < (strSQL.length() - 1) && strSQL.charAt(pos + 1) == '?') {
				// the '?' is escaped
				sb.append(strSQL.substring(last, pos + 1));
				last = pos + 2; // skip 2 chars to account for the escape char
				i--; // keep i unchanged for the next iteration
			}
			else {
				sb.append(strSQL.substring(last, pos));
				if (items[i].isNulls()) sb.append("null");
				else sb.append(SQLCaster.toString(items[i]));
				last = pos + 1;
			}
		}

		sb.append(strSQL.substring(last));
		return sb.toString();
	}

	@Override
	public String toHashString() {
		if (items.length == 0) return strSQL;
		StringBuilder sb = new StringBuilder(strSQL);
		for (int i = 0; i < items.length; i++) {
			sb.append(';').append(items[i].toString());
		}
		return sb.toString();
	}

	public static SQL duplicate(SQL sql) {
		if (!(sql instanceof SQLImpl)) return sql;
		return ((SQLImpl) sql).duplicate();
	}

	public SQL duplicate() {
		SQLImpl rtn = new SQLImpl(strSQL);
		rtn.position = position;
		rtn.items = new SQLItem[items.length];
		for (int i = 0; i < items.length; i++) {
			rtn.items[i] = SQLItemImpl.duplicate(items[i]);
		}

		return rtn;
	}

}