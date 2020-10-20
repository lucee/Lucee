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
package lucee.runtime.type.scope.storage;

import java.sql.SQLException;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.debug.DebuggerUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.storage.db.SQLExecutionFactory;
import lucee.runtime.type.scope.storage.db.SQLExecutor;
import lucee.runtime.type.util.KeyConstants;

/**
 * client scope that store it's data in a datasource
 */
public abstract class StorageScopeDatasource extends StorageScopeImpl {

	private static final long serialVersionUID = 239179599401918216L;

	public static final String PREFIX = "cf";

	private String datasourceName;

	private String appName;

	private String cfid;

	/**
	 * Constructor of the class
	 * 
	 * @param pc
	 * @param name
	 * @param sct
	 * @param b
	 */
	protected StorageScopeDatasource(PageContext pc, String datasourceName, String strType, int type, Struct sct) {
		super(sct, doNowIfNull(pc, Caster.toDate(sct.get(TIMECREATED, null), false, pc.getTimeZone(), null)),
				doNowIfNull(pc, Caster.toDate(sct.get(LASTVISIT, null), false, pc.getTimeZone(), null)), -1,
				type == SCOPE_CLIENT ? Caster.toIntValue(sct.get(HITCOUNT, "1"), 1) : 0, strType, type);

		this.datasourceName = datasourceName;
		appName = pc.getApplicationContext().getName();
		cfid = pc.getCFID();
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	protected StorageScopeDatasource(StorageScopeDatasource other, boolean deepCopy) {
		super(other, deepCopy);
		this.datasourceName = other.datasourceName;
	}

	private static DateTime doNowIfNull(PageContext pc, DateTime dt) {
		if (dt == null) return new DateTimeImpl(pc.getConfig());
		return dt;
	}

	protected static Struct _loadData(PageContext pc, String datasourceName, String strType, int type, Log log, boolean mxStyle) throws PageException {
		ConfigPro config = (ConfigPro) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = config.getDatasourceConnectionPool();
		DatasourceConnection dc = pool.getDatasourceConnection(config, pc.getDataSource(datasourceName), null, null);
		SQLExecutor executor = SQLExecutionFactory.getInstance(dc);

		Query query;

		try {
			if (!dc.getDatasource().isStorage()) throw new ApplicationException("storage usage for this datasource is disabled, you can enable this in the Lucee administrator.");
			query = executor.select(config, pc.getCFID(), pc.getApplicationContext().getName(), dc, type, log, true);
		}
		catch (SQLException se) {
			throw Caster.toPageException(se);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}

		if (query != null && config.debug()) {
			boolean debugUsage = DebuggerUtil.debugQueryUsage(pc, query);
			pc.getDebugger().addQuery(debugUsage ? query : null, datasourceName, "", query.getSql(), query.getRecordcount(), ((PageContextImpl) pc).getCurrentPageSource(null),
					query.getExecutionTime());
		}
		boolean _isNew = query.getRecordcount() == 0;

		if (_isNew) {
			ScopeContext.debug(log,
					"create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID() + " in datasource [" + datasourceName + "]");
			return null;
		}
		String str = Caster.toString(query.get(KeyConstants._data));
		if (str != null && str.startsWith("struct:")) str = str.substring(7);
		if (mxStyle) return null;

		try {
			Struct s = (Struct) pc.evaluate(str);
			ScopeContext.debug(log, "load existing data from [" + datasourceName + "." + PREFIX + "_" + strType + "_data] to create " + strType + " scope for "
					+ pc.getApplicationContext().getName() + "/" + pc.getCFID());
			return s;
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
			return null;
		}
	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchAfterRequest(pc);

		store(pc);
	}

	@Override
	public void store(PageContext pc) {
		DatasourceConnection dc = null;
		ConfigPro ci = (ConfigPro) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		Log log = ci.getLog("scope");
		try {
			pc = ThreadLocalPageContext.get(pc);// FUTURE change method interface
			DataSource ds;
			if (pc != null) ds = pc.getDataSource(datasourceName);
			else ds = ci.getDataSource(datasourceName);
			dc = pool.getDatasourceConnection(null, ds, null, null);
			SQLExecutor executor = SQLExecutionFactory.getInstance(dc);
			executor.update(ci, cfid, appName, dc, getType(), sct, getTimeSpan(), log);
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public void unstore(PageContext pc) {
		ConfigPro ci = (ConfigPro) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnection dc = null;

		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		Log log = ci.getLog("scope");
		try {
			pc = ThreadLocalPageContext.get(pc);// FUTURE change method interface
			DataSource ds;
			if (pc != null) ds = pc.getDataSource(datasourceName);
			else ds = ci.getDataSource(datasourceName);
			dc = pool.getDatasourceConnection(null, ds, null, null);
			SQLExecutor executor = SQLExecutionFactory.getInstance(dc);
			executor.delete(ci, cfid, appName, dc, getType(), log);
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchBeforeRequest(pc);
	}

	@Override
	public String getStorageType() {
		return "Datasource";
	}

	/**
	 * @return the datasourceName
	 */
	public String getDatasourceName() {
		return datasourceName;
	}
}