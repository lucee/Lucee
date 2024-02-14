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
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.db.DBUtil;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigPro;
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

	private ConfigPro config;
	boolean autoCommit = true;
	private int isolation = Connection.TRANSACTION_NONE;
	private Map<DataSource, DatasourceConnectionPro> transConnsReg = new HashMap<DataSource, DatasourceConnectionPro>();
	private Map<DataSource, ORMDatasourceConnection> transConnsORM = new HashMap<DataSource, ORMDatasourceConnection>();
	private boolean inside;

	private Map<String, Savepoint> savepoints = new ConcurrentHashMap<>();

	public DatasourceManagerImpl(ConfigPro c) {
		this.config = c;
	}

	public long getOpenConnections(PageContext pc, DataSource ds, String user, String pass) throws PageException {
		return config.getDatasourceConnectionPool(ds, user, pass).getBorrowedCount();
	}

	@Override
	public DatasourceConnection getConnection(PageContext pc, String _datasource, String user, String pass) throws PageException {
		return getConnection(pc, pc.getDataSource(_datasource), user, pass);
	}

	@Override
	public DatasourceConnection getConnection(PageContext pc, DataSource ds, String user, String pass) throws PageException {
		if (autoCommit && !((DataSourcePro) ds).isRequestExclusive()) {
			return config.getDatasourceConnectionPool(ds, user, pass).borrowObject();
		}
		pc = ThreadLocalPageContext.get(pc);
		// DatasourceConnection newDC = _getConnection(pc,ds,user,pass);
		DatasourceConnectionPro existingDC = null;
		try {
			existingDC = transConnsReg.get(ds);

			// first time that datasource is used within this transaction
			if (existingDC == null) {
				synchronized (SystemUtil.createToken("DatasourceManagerImpl", ds.id())) {
					existingDC = transConnsReg.get(ds);
					if (existingDC == null) {
						DatasourceConnectionPro newDC = (DatasourceConnectionPro) config.getDatasourceConnectionPool().getDatasourceConnection(config, ds, user, pass);
						if (!autoCommit) {
							newDC.setAutoCommit(false);
							if (isolation != Connection.TRANSACTION_NONE) DBUtil.setTransactionIsolationEL(newDC.getConnection(), isolation);
						}
						newDC.setManaged(true);
						transConnsReg.put(ds, newDC);
						return newDC;
					}
				}
			}

			// we have already the same datasource but with different credentials
			if (!DatasourceConnectionImpl.equals(existingDC, ds, user, pass)) {
				if (QOQ_DATASOURCE_NAME.equalsIgnoreCase(ds.getName())) {
					if (autoCommit) {
						if (!existingDC.getAutoCommit()) {
							existingDC.setAutoCommit(true);
							DBUtil.setTransactionIsolationEL(existingDC.getConnection(), existingDC.getDefaultTransactionIsolation());
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
					DBUtil.setTransactionIsolationEL(existingDC.getConnection(), existingDC.getDefaultTransactionIsolation());
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
		}
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
		DatasourceConnectionPro existingDC = null;
		try {
			existingDC = transConnsORM.get(ds);
			if (existingDC == null) {
				if (isolation == Connection.TRANSACTION_NONE) isolation = Connection.TRANSACTION_SERIALIZABLE;
				ORMDatasourceConnection newDC = new ORMDatasourceConnection(pc, session, ds, isolation);
				transConnsORM.put(ds, newDC);
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
			throw new DatabaseException(e, null, existingDC);
		}
	}

	@Override
	public void releaseConnection(PageContext pc, DatasourceConnection dc) {
		releaseConnection(pc, dc, false);
	}

	private void releaseConnection(PageContext pc, DatasourceConnection dc, boolean ignoreRequestExclusive) {
		if (!((DatasourceConnectionPro) dc).isManaged() && autoCommit && (ignoreRequestExclusive || !((DataSourcePro) dc.getDatasource()).isRequestExclusive())) {

			if (pc != null && ((PageContextImpl) pc).getTimeoutStackTrace() != null) {
				IOUtil.closeEL(dc);
			}
			else {
				((DatasourceConnectionPro) dc).release();
			}
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

		this.autoCommit = false;
		this.isolation = isolation;
	}

	@Override
	public void rollback() throws DatabaseException {
		rollback(null);
	}

	// FUTURE
	public void rollback(String savePointName) throws DatabaseException {
		if (autoCommit || _size() == 0) return;
		DatasourceConnection dc = null;
		Pair<DatasourceConnection, Exception> pair = null;
		boolean hasSavePointMatch = false;

		// ORM
		{
			Iterator<ORMDatasourceConnection> it = this.transConnsORM.values().iterator();
			while (it.hasNext()) {
				dc = it.next();
				try {
					if (savePointName == null) dc.getConnection().rollback();
					else {
						Savepoint sp = savepoints.get(toKey(dc.getDatasource(), savePointName));
						if (sp != null) {
							dc.getConnection().rollback(sp);
							hasSavePointMatch = true;
						}
					}
				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
				}
			}
		}
		// Reg
		{
			Iterator<DatasourceConnectionPro> it = this.transConnsReg.values().iterator();
			while (it.hasNext()) {
				dc = it.next();
				try {
					if (savePointName == null) dc.getConnection().rollback();
					else {
						Savepoint sp = savepoints.get(toKey(dc.getDatasource(), savePointName));
						if (sp != null) {
							dc.getConnection().rollback(sp);
							hasSavePointMatch = true;
						}
					}
				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
				}
			}
		}
		throwException(pair);
		if (savePointName != null && !hasSavePointMatch) throw new DatabaseException("There are no savepoint with name [" + savePointName + "] set", null, null, null);
	}

	@Override
	public void savepoint() throws DatabaseException {
		savepoint(null);
	}

	// FUTURE
	public void savepoint(String savePointName) throws DatabaseException {
		if (autoCommit || _size() == 0) return;

		DatasourceConnection dc;
		Pair<DatasourceConnection, Exception> pair = null;
		// ORM
		{
			Iterator<ORMDatasourceConnection> it = this.transConnsORM.values().iterator();
			while (it.hasNext()) {
				dc = it.next();
				try {
					if (savePointName == null) dc.getConnection().setSavepoint();
					else dc.getConnection().setSavepoint(savePointName);
				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
				}
			}
		}
		// Reg
		{
			Iterator<DatasourceConnectionPro> it = this.transConnsReg.values().iterator();
			while (it.hasNext()) {
				dc = it.next();
				try {

					if (savePointName == null) dc.getConnection().setSavepoint();
					else savepoints.put(toKey(dc.getDatasource(), savePointName), dc.getConnection().setSavepoint(savePointName));
				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
				}
			}
		}
		throwException(pair);
	}

	private String toKey(DataSource ds, String savePointName) {
		return HashUtil.create64BitHashAsString(savePointName + ":" + ds.id());
	}

	@Override
	public void commit() throws DatabaseException {
		if (autoCommit || _size() == 0) return;

		Pair<DatasourceConnection, Exception> pair = null;
		DatasourceConnection dc;
		// ORM
		{
			Iterator<ORMDatasourceConnection> it = this.transConnsORM.values().iterator();
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
		}
		// Reg
		{
			Iterator<DatasourceConnectionPro> it = this.transConnsReg.values().iterator();
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
		}
		throwException(pair);
	}

	@Override
	public boolean isAutoCommit() {
		return autoCommit;
	}

	@Override
	public void remove(DataSource datasource) {
		config.removeDatasourceConnectionPool(datasource);
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

		savepoints.clear();

		// ORM
		if (transConnsORM.size() > 0) {
			Map<DataSource, DatasourceConnection> tmp = null;
			Iterator<Entry<DataSource, ORMDatasourceConnection>> it = this.transConnsORM.entrySet().iterator();
			DatasourceConnection dc;
			Entry<DataSource, ORMDatasourceConnection> entry;
			while (it.hasNext()) {
				entry = it.next();
				dc = entry.getValue();
				try {
					dc.setAutoCommit(true);
					DBUtil.setTransactionIsolationEL(dc.getConnection(), ((DatasourceConnectionPro) dc).getDefaultTransactionIsolation());

				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
					continue;
				}
			}
			transConnsORM.clear();
		}

		// Reg
		if (transConnsReg.size() > 0) {
			Map<DataSource, DatasourceConnectionPro> tmp = null;
			if (onlyORM) tmp = new HashMap<DataSource, DatasourceConnectionPro>();
			Iterator<Entry<DataSource, DatasourceConnectionPro>> it = this.transConnsReg.entrySet().iterator();
			DatasourceConnectionPro dc;
			Entry<DataSource, DatasourceConnectionPro> entry;
			while (it.hasNext()) {
				entry = it.next();
				dc = entry.getValue();
				try {
					if (onlyORM && !(dc.getConnection() instanceof ORMConnection)) {
						tmp.put(entry.getKey(), entry.getValue());
						continue;
					}

					if (dc.isManaged()) {
						dc.setManaged(false);
						dc.setAutoCommit(true);
						DBUtil.setTransactionIsolationEL(dc.getConnection(), dc.getDefaultTransactionIsolation());
						releaseConnection(null, dc, true);
					}
				}
				catch (Exception e) {
					// we only keep the first exception
					if (pair == null) {
						pair = new Pair<DatasourceConnection, Exception>(dc, e);
					}
					continue;
				}
			}
			transConnsReg.clear();
			if (onlyORM) transConnsReg = tmp;
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

	private void throwException(Pair<DatasourceConnection, Exception> pair) throws DatabaseException {
		if (pair != null) {
			if (pair.getValue() instanceof SQLException) {
				throw new DatabaseException((SQLException) pair.getValue(), pair.getName());
			}
			throw new PageRuntimeException(pair.getValue());
		}
	}

	private int _size() {
		return transConnsORM.size() + transConnsReg.size();
	}
}