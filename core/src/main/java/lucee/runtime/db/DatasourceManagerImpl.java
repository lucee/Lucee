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

import lucee.commons.lang.Pair;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.orm.ORMConnection;
import lucee.runtime.orm.ORMDatasourceConnection;
import lucee.runtime.orm.ORMSession;

/**
 * this class handle multible db connection, transaction and logging
 */
public final class DatasourceManagerImpl implements DataSourceManager {

	public static final String QOQ_DATASOURCE_NAME = "_queryofquerydb";

	private ConfigImpl config;

	boolean autoCommit = true;
	private int isolation = Connection.TRANSACTION_NONE;
	private Map<DataSource, DatasourceConnection> transConns = new HashMap<DataSource, DatasourceConnection>();

	private boolean inside;

	/**
	 * constructor of the class
	 * 
	 * @param pc
	 */
	public DatasourceManagerImpl(ConfigImpl c) {
		this.config = c;
	}

	@Override
	public DatasourceConnection getConnection(PageContext pc, String _datasource, String user, String pass) throws PageException {
		return getConnection(pc, pc.getDataSource(_datasource), user, pass);
	}

	@Override
	public DatasourceConnection getConnection(PageContext pc, DataSource ds, String user, String pass) throws PageException {
		if (autoCommit && !((DataSourcePro) ds).isRequestExclusive()) {
			return config.getDatasourceConnectionPool().getDatasourceConnection(ThreadLocalPageContext.getConfig(pc), ds, user, pass);
		}

		pc = ThreadLocalPageContext.get(pc);
		// DatasourceConnection newDC = _getConnection(pc,ds,user,pass);
		DatasourceConnectionPro existingDC = null;
		try {
			existingDC = (DatasourceConnectionPro) transConns.get(ds);

			// first time that datasource is used within this transaction
			if (existingDC == null) {
				DatasourceConnection newDC = config.getDatasourceConnectionPool().getDatasourceConnection(config, ds, user, pass);
				if (!autoCommit) {
					newDC.setAutoCommit(false);
					if (isolation != Connection.TRANSACTION_NONE) newDC.setTransactionIsolation(isolation);
				}
				transConns.put(ds, newDC);
				return newDC;
			}

			// we have already the same datasource but with different credentials
			if (!DatasourceConnectionImpl.equals(existingDC, ds, user, pass)) {
				if (QOQ_DATASOURCE_NAME.equalsIgnoreCase(ds.getName())) {
					if (autoCommit) {
						if (!existingDC.getAutoCommit()) {
							existingDC.setAutoCommit(true);
							existingDC.getConnection().setTransactionIsolation(existingDC.getDefaultTransactionIsolation());
						}
					}
					else {
						if (existingDC.getAutoCommit()) {
							existingDC.setAutoCommit(false);
							if (isolation != Connection.TRANSACTION_NONE) existingDC.setTransactionIsolation(isolation);
						}
					}
					return existingDC;
				}
				throw new DatabaseException("can't use different connections to the same datasource inside a single transaction.", null, null, existingDC);
			}

			if (autoCommit) {
				if (!existingDC.getAutoCommit()) {
					existingDC.setAutoCommit(true);
					existingDC.getConnection().setTransactionIsolation(existingDC.getDefaultTransactionIsolation());
				}
			}
			else {
				if (existingDC.getAutoCommit()) {
					existingDC.setAutoCommit(false);
					if (isolation != Connection.TRANSACTION_NONE) existingDC.setTransactionIsolation(isolation);
				}
			}

			return existingDC;
		}
		catch (SQLException e) {
			throw new DatabaseException(e, null, existingDC);
			// ExceptionHandler.printStackTrace(e);
		}
		// return newDC;
	}

	public void add(PageContext pc, ORMSession session) throws PageException {
		if (autoCommit || inside) return;

		inside = true;
		try {
			DataSource[] sources = session.getDataSources();
			for (int i = 0; i < sources.length; i++) {
				_add(pc, session, sources[i]);
			}
		}
		finally {
			inside = false;
		}
	}

	private void _add(PageContext pc, ORMSession session, DataSource ds) throws PageException {
		// ORMDatasourceConnection newDC = new ORMDatasourceConnection(pc,session,ds);
		DatasourceConnectionPro existingDC = null;
		try {
			existingDC = (DatasourceConnectionPro) transConns.get(ds);
			//
			if (existingDC == null) {
				if (isolation == Connection.TRANSACTION_NONE) isolation = Connection.TRANSACTION_SERIALIZABLE;

				ORMDatasourceConnection newDC = new ORMDatasourceConnection(pc, session, ds, isolation);

				transConns.put(ds, newDC);
				return;
			}

			if (!DatasourceConnectionImpl.equals(existingDC, ds, null, null)) {
				// releaseConnection(pc,newDC);
				throw new DatabaseException("can't use different connections to the same datasource inside a single transaction", null, null, existingDC);
			}

			if (existingDC.isAutoCommit()) {
				existingDC.setAutoCommit(false);
			}
			return;
		}
		catch (SQLException e) {
			// ExceptionHandler.printStackTrace(e);
			throw new DatabaseException(e, null, existingDC);
		}
	}

	@Override
	public void releaseConnection(PageContext pc, DatasourceConnection dc) {
		releaseConnection(pc, dc, false);
	}

	private void releaseConnection(PageContext pc, DatasourceConnection dc, boolean ignoreRequestExclusive) {
		if (autoCommit && (ignoreRequestExclusive || !((DataSourcePro) dc.getDatasource()).isRequestExclusive())) {
			config.getDatasourceConnectionPool().releaseDatasourceConnection(dc, pc != null && ((PageContextImpl) pc).getTimeoutStackTrace() != null);
		}
	}

	@Override
	public void begin() {
		this.autoCommit = false;
		this.isolation = Connection.TRANSACTION_NONE;
	}

	@Override
	public void begin(String isolation) {
		this.autoCommit = false;

		if (isolation.equalsIgnoreCase("read_uncommitted")) this.isolation = Connection.TRANSACTION_READ_UNCOMMITTED;
		else if (isolation.equalsIgnoreCase("read_committed")) this.isolation = Connection.TRANSACTION_READ_COMMITTED;
		else if (isolation.equalsIgnoreCase("repeatable_read")) this.isolation = Connection.TRANSACTION_REPEATABLE_READ;
		else if (isolation.equalsIgnoreCase("serializable")) this.isolation = Connection.TRANSACTION_SERIALIZABLE;
		else this.isolation = Connection.TRANSACTION_NONE;

	}

	@Override
	public void begin(int isolation) {
		// print.out("begin:"+autoCommit);
		this.autoCommit = false;
		this.isolation = isolation;
	}

	@Override
	public void rollback() throws DatabaseException {
		if (autoCommit || transConns.size() == 0) return;

		Iterator<DatasourceConnection> it = this.transConns.values().iterator();
		DatasourceConnection dc = null;
		Pair<DatasourceConnection, Exception> pair = null;

		while (it.hasNext()) {
			dc = it.next();
			try {
				dc.getConnection().rollback();
			}
			catch (Exception e) {
				// we only keep the first exception
				if (pair == null) {
					pair = new Pair<DatasourceConnection, Exception>(dc, e);
				}
			}
		}

		if (pair != null) {
			if (pair.getValue() instanceof SQLException) {
				throw new DatabaseException((SQLException) pair.getValue(), pair.getName());
			}
			throw new PageRuntimeException(pair.getValue());
		}
	}

	@Override
	public void savepoint() throws DatabaseException {
		if (autoCommit || transConns.size() == 0) return;

		Iterator<DatasourceConnection> it = this.transConns.values().iterator();
		DatasourceConnection dc = null;
		Pair<DatasourceConnection, Exception> pair = null;

		while (it.hasNext()) {
			dc = it.next();
			try {
				dc.getConnection().setSavepoint();
			}
			catch (Exception e) {
				// we only keep the first exception
				if (pair == null) {
					pair = new Pair<DatasourceConnection, Exception>(dc, e);
				}
			}
		}

		if (pair != null) {
			if (pair.getValue() instanceof SQLException) {
				throw new DatabaseException((SQLException) pair.getValue(), pair.getName());
			}
			throw new PageRuntimeException(pair.getValue());
		}
	}

	@Override
	public void commit() throws DatabaseException {
		if (autoCommit || transConns.size() == 0) return;

		Iterator<DatasourceConnection> it = this.transConns.values().iterator();
		DatasourceConnection dc = null;
		Pair<DatasourceConnection, Exception> pair = null;

		while (it.hasNext()) {
			dc = it.next();
			try {
				dc.getConnection().commit();
			}
			catch (Exception e) {
				// we only keep the first exception
				if (pair == null) {
					pair = new Pair<DatasourceConnection, Exception>(dc, e);
				}
			}
		}

		if (pair != null) {
			if (pair.getValue() instanceof SQLException) {
				throw new DatabaseException((SQLException) pair.getValue(), pair.getName());
			}
			throw new PageRuntimeException(pair.getValue());
		}
	}

	@Override
	public boolean isAutoCommit() {
		return autoCommit;
	}

	@Override
	public void remove(DataSource datasource) {
		config.getDatasourceConnectionPool().remove(datasource);
	}

	@Override
	public void remove(String datasource) {
		throw new PageRuntimeException(new DeprecatedException("method no longer supported!"));
		// config.getDatasourceConnectionPool().remove(datasource);
	}

	@Override
	public void end() { // FUTURE add DatabaseException
		end(false);
	}

	public void end(boolean onlyORM) {
		autoCommit = true;
		Pair<DatasourceConnection, Exception> pair = null;
		if (transConns.size() > 0) {
			Map<DataSource, DatasourceConnection> tmp = null;
			if (onlyORM) tmp = new HashMap<DataSource, DatasourceConnection>();
			Iterator<Entry<DataSource, DatasourceConnection>> it = this.transConns.entrySet().iterator();
			DatasourceConnection dc;
			Entry<DataSource, DatasourceConnection> entry;
			while (it.hasNext()) {
				entry = it.next();
				dc = entry.getValue();
				try {
					if (onlyORM && !(dc.getConnection() instanceof ORMConnection)) {
						tmp.put(entry.getKey(), entry.getValue());
						continue;
					}
					dc.getConnection().setAutoCommit(true);
					dc.getConnection().setTransactionIsolation(((DatasourceConnectionPro) dc).getDefaultTransactionIsolation());

				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
					continue;
				}
				releaseConnection(null, dc, true);
			}
			transConns.clear();
			if (onlyORM) transConns = tmp;
		}
		this.isolation = Connection.TRANSACTION_NONE;

		if (pair != null) {
			if (pair.getValue() instanceof SQLException) {
				throw new PageRuntimeException(new DatabaseException((SQLException) pair.getValue(), pair.getName()));
			}
			throw new PageRuntimeException(pair.getValue());
		}
	}

	@Override
	public void release() {
		end(false);
	}

	public void releaseORM() {
		end(true);
	}

}