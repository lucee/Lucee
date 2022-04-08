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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class DatasourceConnectionPool {

	private static final long WAIT = 1000L;
	private final Object waiter = new Object();

	private static final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<String, String>();

	private ConcurrentHashMap<String, DCStack> dcs = new ConcurrentHashMap<String, DCStack>();

	public int getOpenConnection(DataSource datasource, String user, String pass) throws PageException {
		if (StringUtil.isEmpty(user)) {
			user = datasource.getUsername();
			pass = datasource.getPassword();
		}
		if (pass == null) pass = "";

		// get stack
		DCStack stack = getDCStack(datasource, user, pass);
		RefInteger cnt = stack.getCounter();
		return cnt == null ? 0 : cnt.toInt();
	}

	// !!! do not change used in hibernate extension
	public DatasourceConnection getDatasourceConnection(Config config, DataSource datasource, String user, String pass) throws PageException {
		config = ThreadLocalPageContext.getConfig(config);

		if (StringUtil.isEmpty(user)) {
			user = datasource.getUsername();
			pass = datasource.getPassword();
		}
		if (pass == null) pass = "";

		// get stack
		DCStack stack = getDCStack(datasource, user, pass);
		int max = datasource.getConnectionLimit();

		// get an existing connection
		DatasourceConnection rtn;
		boolean wait = false;
		outer: while (true) {
			rtn = null;

			// wait until it is again my turn
			if (wait) {
				SystemUtil.wait(waiter, WAIT);
				wait = false;
			}

			synchronized (stack) {
				// do we have already to many open connections?
				if (max != -1) {
					RefInteger _counter = stack.getCounter();// _getCounter(stack,datasource,user,pass);
					if (max <= _counter.toInt()) {// go back ant wait
						wait = true;
						continue outer;
					}
				}

				// get an existing connection
				while (!stack.isEmpty()) {
					DatasourceConnection dc = stack.get();
					if (dc != null) {
						rtn = dc;
						break;
					}
				}

				_inc(stack, datasource, user, pass); // if new or fine we increase in any case
				// create a new instance
			}
			if (rtn == null) {
				try {
					rtn = loadDatasourceConnection(config, (DataSourcePro) datasource, user, pass);
				}
				catch (PageException pe) {
					synchronized (stack) {
						_dec(stack, datasource, user, pass);
					}
					throw pe;
				}
				if (rtn instanceof DatasourceConnectionPro) ((DatasourceConnectionPro) rtn).using();
				return rtn;
			}

			// we get us a fine connection (we do validation outside the
			// synchronized to safe shared time)
			if (isValid(rtn)) {
				if (rtn instanceof DatasourceConnectionPro) ((DatasourceConnectionPro) rtn).using();
				return rtn;
			}

			// we have an invalid connection (above check failed), so we have to
			// start over
			synchronized (stack) {
				_dec(stack, datasource, user, pass); // we already did increment
				// in case we are fine
				SystemUtil.notify(waiter);
			}
			try {
				IOUtil.close(rtn.getConnection());
			}
			catch (SQLException e) {
				throw Caster.toPageException(e);
			}
			rtn = null;
		}
	}

	private DatasourceConnectionImpl loadDatasourceConnection(Config config, DataSourcePro ds, String user, String pass) throws PageException {
		Connection conn = null;
		try {
			conn = ds.getConnection(config, user, pass);
		}
		catch (SQLException e) {
			throw new DatabaseException(e, null);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return new DatasourceConnectionImpl(conn, ds, user, pass);
	}

	public void releaseDatasourceConnection(DatasourceConnection dc, boolean closeIt) {
		if (dc == null) return;
		if (!closeIt && dc.getDatasource().getConnectionTimeout() == 0) closeIt = true; // smaller than 0 is infiniti
		if (closeIt) IOUtil.closeEL(dc.getConnection());

		DCStack stack = getDCStack(dc.getDatasource(), dc.getUsername(), dc.getPassword());
		synchronized (stack) {
			if (!closeIt) stack.add(dc);
			_dec(stack, dc.getDatasource(), dc.getUsername(), dc.getPassword());
			if (dc.getDatasource().getConnectionLimit() != -1) SystemUtil.notify(waiter);
		}
	}

	public void releaseDatasourceConnection(DatasourceConnection dc) {
		releaseDatasourceConnection(dc, false);
	}

	public void clear(boolean force, boolean validate) {
		// remove all timed out conns
		try {
			Object[] arr = dcs.entrySet().toArray();
			if (ArrayUtil.isEmpty(arr)) return;
			for (int i = 0; i < arr.length; i++) {
				DCStack conns = (DCStack) ((Map.Entry) arr[i]).getValue();
				if (conns != null) conns.clear(force, validate);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	/**
	 * 
	 * @param dataSourceName
	 * @param force
	 * @param validate only used when force is false
	 */
	public void clear(String dataSourceName, boolean force, boolean validate) {
		// remove all timed out conns
		try {
			Object[] arr = dcs.entrySet().toArray();
			if (ArrayUtil.isEmpty(arr)) return;
			Entry e;
			for (int i = 0; i < arr.length; i++) {
				e = ((Map.Entry) arr[i]);
				DCStack conns = (DCStack) e.getValue();

				DatasourceConnection dc = conns.get();
				if (dc != null) {
					String name = dc.getDatasource().getName();
					if (dataSourceName.equalsIgnoreCase(name)) {
						if (conns != null) conns.clear(force, validate);
					}
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public void remove(DataSource datasource) {
		if (datasource == null) return;

		// remove existing connections
		{
			Iterator<Entry<String, DCStack>> it = dcs.entrySet().iterator();
			Entry<String, DCStack> e;
			while (it.hasNext()) {
				e = it.next();
				if (datasource.equals(e.getValue().getDatasource())) {
					e.getValue().clear(true, false);
				}
			}
		}
	}

	public static boolean isValid(DatasourceConnection dc) {
		try {
			if (dc.getConnection().isClosed()) return false;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}

		if (!OSGiUtil.isValid(dc.getConnection())) return false;

		try {
			if (((DatasourceConnectionPro) dc).validate() || dc.isLifecycleTimeout() || dc.isTimeout()) {
				if (!DataSourceUtil.isValid(dc, 1000, true)) return false;
			}

		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(), "datasource", "connection", e, Log.LEVEL_INFO);
		} // not all driver support this, because of that we ignore an error
			// here, also protect from java 5

		return true;
	}

	public Struct meta() {
		Iterator<Entry<String, DCStack>> it = dcs.entrySet().iterator();
		Entry<String, DCStack> e;
		DCStack dcstack;
		DataSource ds;
		Struct sct;
		Struct arr = new StructImpl();
		while (it.hasNext()) {
			e = it.next();
			dcstack = e.getValue();
			ds = dcstack.getDatasource();
			sct = new StructImpl();
			try {
				sct.setEL(KeyConstants._name, ds.getName());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionLimit", ds.getConnectionLimit());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionTimeout", ds.getConnectionTimeout());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionString", ds.getConnectionStringTranslated());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("openConnections", openConnections(ds.getName()));
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL(KeyConstants._database, ds.getDatabase());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			if (sct.size() > 0) arr.setEL(ds.getName(), sct);
		}
		return arr;
	}

	private DCStack getDCStack(DataSource datasource, String user, String pass) {
		String id = createId(datasource, user, pass);
		synchronized (id) {
			DCStack stack = dcs.get(id);
			if (stack == null) {
				dcs.put(id, stack = new DCStack(datasource, user, pass));
			}
			return stack;
		}
	}

	// do not change interface, used by argus monitor
	public Map<String, Integer> openConnections() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Iterator<DCStack> it = dcs.values().iterator();

		// all connections in pool
		DCStack dcstack;
		while (it.hasNext()) {
			dcstack = it.next();
			Integer val = map.get(dcstack.getDatasource().getName());
			if (val == null) val = dcstack.openConnections();
			else val = val.intValue() + dcstack.openConnections();
			map.put(dcstack.getDatasource().getName(), val);
		}
		return map;
	}

	public int openConnections(String dataSourceName) {
		Integer res = openConnections().get(dataSourceName);
		if (res == null) return -1;
		return res.intValue();
	}

	private void _inc(DCStack stack, DataSource datasource, String username, String password) {
		RefInteger c = stack.getCounter();
		c.plus(1);
	}

	private void _dec(DCStack stack, DataSource datasource, String username, String password) {
		RefInteger c = stack.getCounter();
		c.minus(1);
	}

	public static String createId(DataSource datasource, String user, String pass) {
		String str = new StringBuilder().append(datasource.id()).append("::").append(user).append(":").append(pass).toString();
		String lock = tokens.putIfAbsent(str, str);
		if (lock == null) {
			lock = str;
		}
		return lock;
	}
}
