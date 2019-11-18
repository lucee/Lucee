package lucee.runtime.type.scope.storage;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.debug.DebuggerUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.storage.db.SQLExecutionFactory;
import lucee.runtime.type.scope.storage.db.SQLExecutor;
import lucee.runtime.type.util.KeyConstants;

public class IKHandlerDatasource implements IKHandler {

	public static final String PREFIX = "cf";

	@Override
	public IKStorageValue loadData(PageContext pc, String appName, String name, String strType, int type, Log log) throws PageException {
		ConfigImpl config = (ConfigImpl) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = config.getDatasourceConnectionPool();
		DatasourceConnection dc = pool.getDatasourceConnection(config, pc.getDataSource(name), null, null);
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
			pc.getDebugger().addQuery(debugUsage ? query : null, name, "", query.getSql(), query.getRecordcount(), ((PageContextImpl) pc).getCurrentPageSource(null),
					query.getExecutionTime());
		}
		boolean _isNew = query.getRecordcount() == 0;

		if (_isNew) {
			ScopeContext.debug(log, "create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID() + " in datasource [" + name + "]");
			return null;
		}
		String str = Caster.toString(query.getAt(KeyConstants._data, 1));

		// old style
		boolean b;
		if ((b = str.startsWith("struct:")) || (str.startsWith("{") && str.endsWith("}"))) {
			if (b) str = str.substring(7);
			try {
				return toIKStorageValue((Struct) pc.evaluate(str));
			}
			catch (Exception e) {}
			return null;
		}

		try {
			IKStorageValue data = (IKStorageValue) JavaConverter.deserialize(str);
			ScopeContext.debug(log, "load existing data from [" + name + "." + PREFIX + "_" + strType + "_data] to create " + strType + " scope for "
					+ pc.getApplicationContext().getName() + "/" + pc.getCFID());
			return data;
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
			return null;
			// throw Caster.toPageException(e);
		}
	}

	public static IKStorageValue toIKStorageValue(Struct sct) throws PageException {
		// last modified
		long lastModified = 0;
		Object o = sct.get(KeyConstants._lastvisit, null);
		if (o instanceof Date) lastModified = ((Date) o).getTime();
		else {
			o = sct.get(KeyConstants._timecreated, null);
			if (o instanceof Date) lastModified = ((Date) o).getTime();
		}
		if (lastModified == 0) lastModified = System.currentTimeMillis();

		Map<Collection.Key, IKStorageScopeItem> map = new HashMap<Collection.Key, IKStorageScopeItem>();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey(), new IKStorageScopeItem(e.getValue(), lastModified));
		}
		return new IKStorageValue(map);
	}

	@Override
	public void store(IKStorageScopeSupport storageScope, PageContext pc, String appName, final String name, String cfid, Map<Key, IKStorageScopeItem> data, Log log) {
		DatasourceConnection dc = null;
		ConfigImpl ci = (ConfigImpl) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		try {
			pc = ThreadLocalPageContext.get(pc);
			DataSource ds;
			if (pc != null) ds = pc.getDataSource(name);
			else ds = ci.getDataSource(name);
			dc = pool.getDatasourceConnection(null, ds, null, null);
			SQLExecutor executor = SQLExecutionFactory.getInstance(dc);
			IKStorageValue existingVal = loadData(pc, appName, name, storageScope.getTypeAsString(), storageScope.getType(), log);
			IKStorageValue sv = new IKStorageValue(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope.lastModified()));
			executor.update(ci, cfid, appName, dc, storageScope.getType(), sv, storageScope.getTimeSpan(), log);
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public void unstore(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, String cfid, Log log) {
		ConfigImpl ci = (ConfigImpl) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnection dc = null;

		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		try {
			pc = ThreadLocalPageContext.get(pc);// FUTURE change method interface
			DataSource ds;
			if (pc != null) ds = pc.getDataSource(name);
			else ds = ci.getDataSource(name);
			dc = pool.getDatasourceConnection(null, ds, null, null);
			SQLExecutor executor = SQLExecutionFactory.getInstance(dc);
			executor.delete(ci, cfid, appName, dc, storageScope.getType(), log);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ScopeContext.error(log, t);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public String getType() {
		return "Datasource";
	}

}
