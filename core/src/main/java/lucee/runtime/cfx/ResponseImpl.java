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
package lucee.runtime.cfx;

import java.io.IOException;

import com.allaire.cfx.Query;
import com.allaire.cfx.Response;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

/**
 * 
 */
public final class ResponseImpl implements Response {

	private PageContext pc;
	private boolean debug;

	/**
	 * @param pc
	 * @param debug
	 */
	public ResponseImpl(PageContext pc, boolean debug) {
		this.pc = pc;
		this.debug = debug;
	}

	@Override
	public Query addQuery(String name, String[] column) {
		lucee.runtime.type.Query query = new lucee.runtime.type.QueryImpl(column, 0, name);

		try {
			pc.setVariable(name, query);
		}
		catch (PageException e) {
		}
		return new QueryWrap(query);
	}

	@Override
	public void setVariable(String key, String value) {
		try {
			pc.setVariable(key, value);
		}
		catch (PageException e) {
		}
	}

	@Override
	public void write(String str) {
		try {
			pc.write(str);
		}
		catch (IOException e) {

		}
	}

	@Override
	public void writeDebug(String str) {
		if (debug) write(str);
	}

}