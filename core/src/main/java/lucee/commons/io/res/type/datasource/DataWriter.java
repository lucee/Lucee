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
package lucee.commons.io.res.type.datasource;

import java.io.InputStream;
import java.sql.SQLException;

import lucee.commons.io.res.type.datasource.core.Core;
import lucee.runtime.db.DatasourceConnection;

public class DataWriter extends Thread {

	private Core core;
	private DatasourceConnection dc;
	private String prefix;
	private Attr attr;
	private InputStream is;
	private SQLException e;
	private boolean append;

	private DatasourceResourceProvider drp;

	public DataWriter(Core core, DatasourceConnection dc, String prefix, Attr attr, InputStream is, DatasourceResourceProvider drp, boolean append) {
		this.core = core;
		this.dc = dc;
		this.prefix = prefix;
		this.attr = attr;
		this.is = is;
		this.drp = drp;
		this.append = append;
	}

	@Override
	public void run() {
		try {
			core.write(dc, prefix, attr, is, append);
			drp.release(dc);
			// manager.releaseConnection(connId,dc);
		}
		catch (SQLException e) {
			this.e = e;
		}
	}

	public SQLException getException() {
		return e;
	}
}