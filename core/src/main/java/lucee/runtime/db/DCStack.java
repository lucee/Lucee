/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.db;

import java.sql.Connection;
import java.sql.SQLException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

class DCStack {

	private static final int DEFAULT_TIMEOUT;
	private Item item;
	private DataSource datasource;
	private String user;
	private String pass;
	private final RefInteger counter;

	static {
		DEFAULT_TIMEOUT = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.datasource.timeout.validation", null), 5);
	}

	DCStack(DataSource datasource, String user, String pass) {
		this.datasource = datasource;
		this.user = user;
		this.pass = pass;
		this.counter = new RefIntegerImpl(0);
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public String getUsername() {
		return user;
	}

	public String getPassword() {
		return pass;
	}

	public void add(DatasourceConnection dc) {
		// make sure the connection is not already in stack, this can happen when the conn is released twice
		Item test = item;
		while (test != null) {
			if (test.dc == dc) {
				LogUtil.log(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, DCStack.class.getName(), "a datasource connection was released twice!");
				return;
			}
			test = test.prev;
		}

		item = new Item(item, dc);
	}

	public DatasourceConnection get() {
		if (item == null) return null;
		DatasourceConnection rtn = item.dc;
		item = item.prev;
		try {

			if (!rtn.getConnection().isClosed()) {
				return rtn;
			}
			return get();
		}
		catch (SQLException e) {}
		return null;
	}

	public boolean isEmpty() {
		return item == null;
	}

	public int size() {
		int count = 0;
		Item i = item;
		while (i != null) {
			count++;
			i = i.prev;
		}
		return count;
	}

	public int openConnectionsIn() {
		int count = 0;
		Item i = item;
		while (i != null) {
			try {
				if (!i.dc.getConnection().isClosed()) count++;
			}
			catch (Exception e) {}
			i = i.prev;
		}
		return count;
	}

	public int openConnectionsOut() {
		return counter.toInt();
	}

	public int openConnections() {
		return openConnectionsIn() + openConnectionsOut();
	}

	class Item {
		private DatasourceConnection dc;
		private Item prev;
		private int count = 1;

		public Item(Item item, DatasourceConnection dc) {
			this.prev = item;
			this.dc = dc;
			if (prev != null) count = prev.count + 1;
		}

		@Override
		public String toString() {
			return "(" + prev + ")<-" + count;
		}
	}

	public void clear(boolean force, boolean validate) {
		synchronized (this) {
			clear(item, null, force, validate);
		}
	}

	/**
	 * 
	 * @param current
	 * @param next
	 * @param timeout timeout in seconds used to validate existing connections
	 * @throws SQLException
	 */
	private void clear(Item current, Item next, boolean force, boolean validate) {
		if (current == null) return;

		// timeout or closed
		if (force || current.dc.isTimeout() || current.dc.isLifecycleTimeout() || isClosedEL(current.dc.getConnection())
				|| (validate && Boolean.FALSE.equals(isValidEL(current.dc.getConnection())))) {

			// when timeout was reached but it is still open, close it
			if (!isClosedEL(current.dc.getConnection())) {
				try {
					current.dc.close();
				}
				catch (Exception e) {}
			}

			// remove this connection from chain
			if (next == null) item = current.prev;
			else {
				next.prev = current.prev;
			}

			clear(current.prev, next, force, validate);
		}
		else {
			// make sure that auto commit is true
			try {
				if (!current.dc.getAutoCommit()) current.dc.setAutoCommit(true);
			}
			catch (SQLException e) {}
			clear(current.prev, current, force, validate);
		}

		counter.setValue(0);
	}

	private boolean isClosedEL(Connection conn) {
		try {
			return conn.isClosed();
		}
		catch (Exception se) {
			datasource.getLog().error("Connection  Pool", se);
			// in case of an exception we see this conn as useless and close the connection
			try {
				conn.close();
			}
			catch (SQLException e) {
				datasource.getLog().error("Connection  Pool", e);
			}

			return true;
		}
	}

	private Boolean isValidEL(Connection conn) {
		try {
			// value is in ms but method expect s
			int ms = datasource.getNetworkTimeout();
			int s = DEFAULT_TIMEOUT;
			if (ms > 0) s = (int) Math.ceil(ms / 1000);

			return conn.isValid(s) ? Boolean.TRUE : Boolean.FALSE;
		}
		catch (Exception e) {
			return null;
		}
	}

	public RefInteger getCounter() {
		return counter;
	}
}