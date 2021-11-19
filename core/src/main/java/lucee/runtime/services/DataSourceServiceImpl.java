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
package lucee.runtime.services;

import java.io.IOException;
import java.sql.SQLException;

import coldfusion.server.DataSourceService;
import coldfusion.server.ServiceException;
import coldfusion.sql.DataSource;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.functions.list.ListFirst;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class DataSourceServiceImpl extends ServiceSupport implements DataSourceService {

	private Number maxQueryCount = new Double(500);

	@Override
	public Struct getDefaults() {
		Struct sct = new StructImpl();
		sct.setEL("alter", Boolean.TRUE);
		sct.setEL("blob_buffer", new Double(64000));
		sct.setEL("buffer", new Double(64000));
		sct.setEL("create", Boolean.TRUE);
		sct.setEL("delete", Boolean.TRUE);
		sct.setEL("disable", Boolean.FALSE);
		sct.setEL("disable_blob", Boolean.TRUE);
		sct.setEL("disable_clob", Boolean.TRUE);
		sct.setEL("drop", Boolean.TRUE);
		sct.setEL("grant", Boolean.TRUE);
		sct.setEL("insert", Boolean.TRUE);
		sct.setEL("pooling", Boolean.TRUE);
		sct.setEL("revoke", Boolean.TRUE);
		sct.setEL("select", Boolean.TRUE);
		sct.setEL("storedproc", Boolean.TRUE);
		sct.setEL("update", Boolean.TRUE);
		sct.setEL("", Boolean.TRUE);
		sct.setEL("", Boolean.TRUE);
		sct.setEL("", Boolean.TRUE);
		sct.setEL("interval", new Double(420));
		sct.setEL("login_timeout", new Double(30));
		sct.setEL("timeout", new Double(1200));

		return sct;
	}

	@Override
	public Number getMaxQueryCount() {
		return maxQueryCount;
	}

	@Override
	public void setMaxQueryCount(Number maxQueryCount) {
		this.maxQueryCount = maxQueryCount;
	}

	@Override
	public String encryptPassword(String pass) {
		throw new PageRuntimeException(new ServiceException("method [encryptPassword] is not supported for datasource service"));
		// return pass;
	}

	@Override
	public String getDbdir() {
		Resource db = config().getConfigDir().getRealResource("db");
		if (!db.exists()) db.createNewFile();
		return db.getPath();
	}

	@Override
	public Object getCachedQuery(String key) {
		throw new PageRuntimeException(new ServiceException("method [getQueryCache] is not supported for datasource service"));
	}

	@Override
	public void setCachedQuery(String arg0, Object arg1) {
		throw new PageRuntimeException(new ServiceException("method [setQueryCache] is not supported for datasource service"));
	}

	@Override
	public void purgeQueryCache() throws IOException {
		PageContext pc = pc();
		if (pc != null) try {
			pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clean(pc);
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
		// if(pc!=null)pc.getQueryCache().clearUnused(pc);

	}

	@Override
	public boolean disableConnection(String name) {
		return false;
	}

	@Override
	public boolean isJadoZoomLoaded() {
		return false;
	}

	@Override
	public Struct getDrivers() throws ServiceException, SecurityException {
		checkReadAccess();
		Struct rtn = new StructImpl();
		Struct driver;

		try {
			Resource luceeContext = ResourceUtil.toResourceExisting(pc(), "/lucee/admin/dbdriver/");
			Resource[] children = luceeContext.listResources(new ExtensionResourceFilter(Constants.getComponentExtensions()));

			String name;
			for (int i = 0; i < children.length; i++) {
				driver = new StructImpl();
				name = ListFirst.call(pc(), children[i].getName(), ".", false, 1);
				driver.setEL(KeyConstants._name, name);
				driver.setEL("handler", children[i].getName());
				rtn.setEL(name, driver);
			}
		}
		catch (ExpressionException e) {
			throw new ServiceException(e.getMessage());
		}
		return rtn;
	}

	@Override
	public Struct getDatasources() throws SecurityException {// MUST muss struct of struct zurueckgeben!!!
		checkReadAccess();
		lucee.runtime.db.DataSource[] sources = config().getDataSources();
		Struct rtn = new StructImpl();
		for (int i = 0; i < sources.length; i++) {
			rtn.setEL(sources[i].getName(), new DataSourceImpl(sources[i]));
		}
		return rtn;
	}

	@Override
	public Array getNames() throws SecurityException {
		checkReadAccess();
		lucee.runtime.db.DataSource[] sources = config().getDataSources();
		Array names = new ArrayImpl();
		for (int i = 0; i < sources.length; i++) {
			names.appendEL(sources[i].getName());
		}
		return names;
	}

	@Override
	public void removeDatasource(String name) throws SQLException, SecurityException {
		checkWriteAccess();
		try {
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(config(), null);
			admin.removeDataSource(name);
		}
		catch (Exception e) {
			// ignoriert wenn die db nicht existiert
		}
	}

	@Override
	public boolean verifyDatasource(String name) throws SQLException, SecurityException {
		checkReadAccess();
		lucee.runtime.db.DataSource d = _getDatasource(name);
		PageContext pc = pc();
		DataSourceManager manager = pc.getDataSourceManager();
		try {
			manager.releaseConnection(pc, manager.getConnection(pc, name, d.getUsername(), d.getPassword()));
			return true;
		}
		catch (PageException e) {
			return false;
		}
	}

	@Override
	public DataSource getDatasource(String name) throws SQLException, SecurityException {
		return new DataSourceImpl(_getDatasource(name));
	}

	private lucee.runtime.db.DataSource _getDatasource(String name) throws SQLException, SecurityException {
		checkReadAccess();
		name = name.trim();
		lucee.runtime.db.DataSource[] sources = config().getDataSources();
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].getName().equalsIgnoreCase(name)) return sources[i];
		}
		throw new SQLException("no datasource with name [" + name + "] found");
	}
}