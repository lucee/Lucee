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

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.cfx.QueryWrap;

/**
 * Implementation of the DebugQuery
 */
public final class DebugQuery extends QueryWrap {

	/**
	 * Constructor of the DebugQuery
	 * 
	 * @param name query name
	 * @param columns column names
	 * @param data query data
	 * @throws IllegalArgumentException thrown when arguments are invalid
	 */
	public DebugQuery(final String name, final String[] columns, final String[][] data) throws IllegalArgumentException {
		super(toQuery(name, columns, data), name);
	}

	/**
	 * Constructor of the DebugQuery
	 * 
	 * @param name query name
	 * @param columns column names
	 * @throws IllegalArgumentException thrown when arguments are invalid
	 */
	public DebugQuery(final String name, final String[] columns) throws IllegalArgumentException {
		super(toQuery(name, columns, 0), name);
	}

	private static lucee.runtime.type.Query toQuery(final String name, final String[] columns, final String[][] data) {

		final lucee.runtime.type.Query query = toQuery(name, columns, data.length);

		for (int row = 0; row < data.length; row++) {
			final int len = data[row].length > columns.length ? columns.length : data[row].length;
			for (int col = 0; col < len; col++)
				try {
					query.setAt(columns[col], row + 1, data[row][col]);
				}
				catch (final Exception e) {}
		}
		return query;
	}

	private static lucee.runtime.type.Query toQuery(final String name, final String[] columns, final int rows) {
		return CFMLEngineFactory.getInstance().getCreationUtil().createQuery(columns, rows, name);
	}
}