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

		StringBuilder sb = new StringBuilder();
		int sqlLen = strSQL.length();
		char c, quoteType = 0, p = 0;
		boolean inQuotes = false;
		int index = 0;
		for (int i = 0; i < sqlLen; i++) {
			c = strSQL.charAt(i);

			if (c == '"' || c == '\'') {
				if (inQuotes) {
					if (c == quoteType) {
						if ('\\' != p) {
							inQuotes = false;
						}
					}
				}
				else {
					quoteType = c;
					inQuotes = true;
				}
			}
			else if (!inQuotes && c == '?') {
				if ((index + 1) > items.length) throw new RuntimeException("there are more question marks in the SQL than params defined");
				if (items[index].isNulls()) sb.append("null");
				else sb.append(SQLCaster.toString(items[index]));
				index++;
			}

			else {
				sb.append(c);
			}
			p = c;

		}
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