package lucee.runtime.type.scope.storage;

import java.sql.SQLException;

import lucee.commons.collection.MapPro;
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
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.storage.db.SQLExecutionFactory;
import lucee.runtime.type.scope.storage.db.SQLExecutor;
import lucee.runtime.type.util.KeyConstants;

public class IKHandlerDatasource implements IKHandler {
	
	public static final String PREFIX = "cf";
	
	@Override
	public IKStorageValue loadData(PageContext pc, String appName, String name, String strType,
			int type, Log log) throws PageException {
		ConfigImpl config = (ConfigImpl)ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = config.getDatasourceConnectionPool();
		DatasourceConnection dc=pool.getDatasourceConnection(config,pc.getDataSource(name),null,null);
		SQLExecutor executor=SQLExecutionFactory.getInstance(dc);
		Query query;
		
		try {
			if(!dc.getDatasource().isStorage()) 
				throw new ApplicationException("storage usage for this datasource is disabled, you can enable this in the Lucee administrator.");
			query = executor.select(config,pc.getCFID(),pc.getApplicationContext().getName(), dc, type,log, true);
		} 
		catch (SQLException se) {
			throw Caster.toPageException(se);
		}
	    finally {
	    	if(dc!=null) pool.releaseDatasourceConnection(dc);
	    }
	    
	    if(query!=null && config.debug()) {
	    	boolean debugUsage=DebuggerUtil.debugQueryUsage(pc,query);
	    	pc.getDebugger().addQuery(debugUsage?query:null,name,"",query.getSql(),query.getRecordcount(),((PageContextImpl)pc).getCurrentPageSource(null),query.getExecutionTime());
	    }
	    boolean _isNew = query.getRecordcount()==0;
	    
	    if(_isNew) {
	    	ScopeContext.info(log,"create new "+strType+" scope for "+pc.getApplicationContext().getName()+"/"+pc.getCFID()+" in datasource ["+name+"]");
			return null;
	    }
	    String str=Caster.toString(query.getAt(KeyConstants._data,1));
	    
	    if(str.startsWith("struct:")) return null;
	    try{
		    IKStorageValue data=(IKStorageValue) JavaConverter.deserialize(str);
		    ScopeContext.info(log,"load existing data from ["+name+"."+PREFIX+"_"+strType+"_data] to create "+strType+" scope for "+pc.getApplicationContext().getName()+"/"+pc.getCFID());
		    return data;
	    }
	    catch(Exception e) {
	    	ScopeContext.error(log, e);
	    	return null;
	    	//throw Caster.toPageException(e);
	    }
	}

	@Override
	public void store(IKStorageScopeSupport storageScope, PageContext pc, String appName, final String name, String cfid,
			MapPro<Key, IKStorageScopeItem> data, Log log) {
		DatasourceConnection dc = null;
		ConfigImpl ci = (ConfigImpl)ThreadLocalPageContext.getConfig(pc);
		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		try {
			pc = ThreadLocalPageContext.get(pc);
			DataSource ds;
			if(pc!=null) ds=pc.getDataSource(name);
			else ds=ci.getDataSource(name);
			dc=pool.getDatasourceConnection(null,ds,null,null);
			SQLExecutor executor=SQLExecutionFactory.getInstance(dc);
			IKStorageValue existingVal = loadData(pc, appName,name, storageScope.getTypeAsString(), storageScope.getType(), log);
			IKStorageValue sv = new IKStorageValue(IKStorageScopeSupport.prepareToStore(data,existingVal,storageScope.lastModified()));
			executor.update(ci, cfid,appName, dc, storageScope.getType(), sv, storageScope.getTimeSpan(),log);
		} 
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ScopeContext.error(log, t);
		}
		finally {
			if(dc!=null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public void unstore(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, String cfid, Log log) {
		ConfigImpl ci=(ConfigImpl) ThreadLocalPageContext.getConfig(pc);
		DatasourceConnection dc = null;
		
		
		DatasourceConnectionPool pool = ci.getDatasourceConnectionPool();
		try {
			pc = ThreadLocalPageContext.get(pc);// FUTURE change method interface
			DataSource ds;
			if(pc!=null) ds=pc.getDataSource(name);
			else ds=ci.getDataSource(name);
			dc=pool.getDatasourceConnection(null,ds,null,null);
			SQLExecutor executor=SQLExecutionFactory.getInstance(dc);
			executor.delete(ci, cfid,appName, dc, storageScope.getType(),log);
		} 
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ScopeContext.error(log, t);
		}
		finally {
			if(dc!=null) pool.releaseDatasourceConnection(dc);
		}
	}

	@Override
	public String getType() {
		return "Datasource";
	}

}
