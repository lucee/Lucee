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
package com.allaire.cfx;

import java.util.Enumeration;
import java.util.Hashtable;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.cfx.QueryWrap;

public final class DebugResponse implements Response {

	private final StringBuffer write = new StringBuffer();
	private final StringBuffer writeDebug = new StringBuffer();
	private final Hashtable variables = new Hashtable();
	private final Hashtable queries = new Hashtable();

	@Override
	public Query addQuery(final String name, final String[] columns) {
		final QueryWrap query = new QueryWrap(CFMLEngineFactory.getInstance().getCreationUtil().createQuery(columns, 0, name), name.toLowerCase());
		queries.put(name.toLowerCase(), query);
		return query;
	}

	@Override
	public void setVariable(final String key, final String value) {
		variables.put(key.toLowerCase(), value);
	}

	@Override
	public void write(final String str) {
		write.append(str);
	}

	@Override
	public void writeDebug(final String str) {
		writeDebug.append(str);
	}

	/**
	 * print out the response
	 */
	public void printResults() {
		System.out.println("[ --- Lucee Debug Response --- ]");
		System.out.println();

		System.out.println("----------------------------");
		System.out.println("|          Output          |");
		System.out.println("----------------------------");
		System.out.println(write);
		System.out.println();

		System.out.println("----------------------------");
		System.out.println("|       Debug Output       |");
		System.out.println("----------------------------");
		System.out.println(writeDebug);
		System.out.println();

		System.out.println("----------------------------");
		System.out.println("|        Variables         |");
		System.out.println("----------------------------");

		Enumeration e = variables.keys();
		while (e.hasMoreElements()) {
			final Object key = e.nextElement();
			System.out.println("[Variable:" + key + "]");
			System.out.println(escapeString(variables.get(key).toString()));
		}
		System.out.println();

		e = queries.keys();
		while (e.hasMoreElements()) {
			final Query query = (Query) queries.get(e.nextElement());
			printQuery(query);
			System.out.println();
		}

	}

	/**
	 * print out a query
	 * 
	 * @param query query to print
	 */
	public void printQuery(final Query query) {
		if (query != null) {
			final String[] cols = query.getColumns();
			final int rows = query.getRowCount();
			System.out.println("[Query:" + query.getName() + "]");
			for (int i = 0; i < cols.length; i++) {
				if (i > 0) System.out.print(", ");
				System.out.print(cols[i]);
			}
			System.out.println();

			for (int row = 1; row <= rows; row++) {
				for (int col = 1; col <= cols.length; col++) {
					if (col > 1) System.out.print(", ");
					System.out.print(escapeString(query.getData(row, col)));
				}
				System.out.println();
			}
		}
	}

	private String escapeString(final String string) {
		final int len = string.length();
		final StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			final char c = string.charAt(i);
			if (c == '\n') sb.append("\\n");
			else if (c == '\t') sb.append("\\t");
			else if (c == '\\') sb.append("\\\\");
			else if (c == '\b') sb.append("\\b");
			else if (c == '\r') sb.append("\\r");
			else if (c == '\"') sb.append("\\\"");
			else sb.append(c);
		}

		return "\"" + sb.toString() + "\"";
	}

}