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
package lucee.commons.io.res.type.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.type.datasource.core.Core;
import lucee.commons.io.res.type.datasource.core.MSSQL;
import lucee.commons.io.res.type.datasource.core.MySQL;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPro;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

/**
 * Resource Provider for ram resource
 */
public final class DatasourceResourceProvider implements ResourceProviderPro {

	public static final int DBTYPE_ANSI92 = 0;
	public static final int DBTYPE_MSSQL = 1;
	public static final int DBTYPE_MYSQL = 2;

	private static final int MAXAGE = 5000;

	// private static final int CONNECTION_ID = 0;

	private String scheme = "ds";

	boolean caseSensitive = true;
	// private Resources resources;
	private long lockTimeout = 1000;
	private ResourceLockImpl lock = new ResourceLockImpl(lockTimeout, caseSensitive);
	private DatasourceManagerImpl _manager;
	private String defaultPrefix = "rdr";
	// private DataSourceManager manager;
	// private Core core;
	private Map cores = new WeakHashMap();
	private Map<String, SoftReference<Attr>> attrCache = new ConcurrentHashMap<String, SoftReference<Attr>>();
	private Map<String, SoftReference<Attr>> attrsCache = new ConcurrentHashMap<String, SoftReference<Attr>>();
	private Map arguments;

	/**
	 * initialize ram resource
	 * 
	 * @param scheme
	 * @param arguments
	 * @return RamResource
	 */
	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		if (!StringUtil.isEmpty(scheme)) this.scheme = scheme;

		if (arguments != null) {
			this.arguments = arguments;
			// case-sensitive
			Object oCaseSensitive = arguments.get("case-sensitive");
			if (oCaseSensitive != null) {
				caseSensitive = Caster.toBooleanValue(oCaseSensitive, true);
			}

			// prefix
			Object oPrefix = arguments.get("prefix");
			if (oPrefix != null) {
				defaultPrefix = Caster.toString(oPrefix, defaultPrefix);
			}

			// lock-timeout
			Object oTimeout = arguments.get("lock-timeout");
			if (oTimeout != null) {
				lockTimeout = Caster.toLongValue(oTimeout, lockTimeout);
			}
		}
		lock.setLockTimeout(lockTimeout);
		lock.setCaseSensitive(caseSensitive);
		return this;
	}

	@Override
	public Resource getResource(String path) {
		StringBuilder sb = new StringBuilder();
		return new DatasourceResource(this, parse(sb, path), sb.toString());
	}

	public ConnectionData parse(StringBuilder subPath, String path) {
		path = ResourceUtil.removeScheme(scheme, path);

		ConnectionData data = new ConnectionData();
		int atIndex = path.indexOf('@');
		int slashIndex = path.indexOf('/');
		if (slashIndex == -1) {
			slashIndex = path.length();
			path += "/";
		}
		int index;

		// username/password
		if (atIndex != -1) {
			index = path.indexOf(':');
			if (index != -1 && index < atIndex) {
				data.setUsername(path.substring(0, index));
				data.setPassword(path.substring(index + 1, atIndex));
			}
			else data.setUsername(path.substring(0, atIndex));
		}
		// host port
		if (slashIndex > atIndex + 1) {
			data.setDatasourceName(path.substring(atIndex + 1, slashIndex));
		}
		if (slashIndex > atIndex + 1) {
			index = path.indexOf(':', atIndex + 1);
			if (index != -1 && index > atIndex && index < slashIndex) {
				data.setDatasourceName(path.substring(atIndex + 1, index));
				data.setPrefix(path.substring(index + 1, slashIndex));
			}
			else {
				data.setDatasourceName(path.substring(atIndex + 1, slashIndex));
				data.setPrefix(defaultPrefix);
			}
		}
		subPath.append(path.substring(slashIndex));
		return data;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public void setResources(Resources resources) {
		// this.resources=resources;
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isModeSupported() {
		return true;
	}

	private DatasourceManagerImpl getManager() {
		if (_manager == null) {
			Config config = ThreadLocalPageContext.getConfig();
			_manager = new DatasourceManagerImpl((ConfigImpl) config);
		}
		return _manager;
	}

	private Core getCore(ConnectionData data) throws PageException {
		Core core = (Core) cores.get(data.datasourceName);
		if (core == null) {
			DatasourceConnection dc = getManager().getConnection(ThreadLocalPageContext.get(), data.getDatasourceName(), data.getUsername(), data.getPassword());
			try {

				dc.setAutoCommit(false);
				dc.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

				if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = new MSSQL(dc, data.getPrefix());
				else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = new MSSQL(dc, data.getPrefix());
				else if ("net.sourceforge.jtds.jdbc.Driver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = new MSSQL(dc, data.getPrefix());
				else if ("org.gjt.mm.mysql.Driver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = new MySQL(dc, data.getPrefix());
				else throw new ApplicationException("There is no DatasourceResource driver for this database [" + data.getPrefix() + "]");

				cores.put(data.datasourceName, core);
			}
			catch (SQLException e) {
				throw new DatabaseException(e, dc);
			}
			finally {
				release(dc);
				// manager.releaseConnection(CONNECTION_ID,dc);
			}
		}
		return core;
	}

	private DatasourceConnection getDatasourceConnection(ConnectionData data, boolean autoCommit) throws PageException {
		DatasourceConnection dc = getManager().getConnection(ThreadLocalPageContext.get(), data.getDatasourceName(), data.getUsername(), data.getPassword());

		try {
			dc.getConnection().setAutoCommit(autoCommit);
			dc.getConnection().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}
		catch (SQLException e) {
			throw new DatabaseException(e, dc);
		}

		return dc;
	}

	private DatasourceConnection getDatasourceConnection(ConnectionData data) throws PageException {
		return getDatasourceConnection(data, false);
	}

	public Attr getAttr(ConnectionData data, int fullPathHash, String path, String name) {
		Attr attr = getFromCache(data, path, name);
		if (attr != null) return attr;
		try {
			return _getAttr(data, fullPathHash, path, name);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	private Attr _getAttr(ConnectionData data, int fullPathHash, String path, String name) throws PageException {
		if (!StringUtil.isEmpty(data.getDatasourceName())) {
			DatasourceConnection dc = null;
			try {
				dc = getDatasourceConnection(data);
				Attr attr = getCore(data).getAttr(dc, data.getPrefix(), fullPathHash, path, name);
				if (attr != null) return putToCache(data, path, name, attr);
			}
			catch (SQLException e) {
				throw new DatabaseException(e, dc);
			}
			finally {
				getManager().releaseConnection(ThreadLocalPageContext.get(), dc);
			}
		}
		return putToCache(data, path, name, Attr.notExists(name, path));
	}

	public Attr[] getAttrs(ConnectionData data, int pathHash, String path) throws PageException {
		if (StringUtil.isEmpty(data.getDatasourceName())) return null;

		// Attr[] attrs = getFromCache(data, path);
		// if(attrs!=null) return attrs;

		DatasourceConnection dc = null;
		try {
			dc = getDatasourceConnection(data);
			List list = getCore(data).getAttrs(dc, data.getPrefix(), pathHash, path);

			if (list != null) {
				Iterator it = list.iterator();
				Attr[] rtn = new Attr[list.size()];
				int index = 0;
				while (it.hasNext()) {
					rtn[index] = (Attr) it.next();
					putToCache(data, rtn[index].getParent(), rtn[index].getName(), rtn[index]);
					index++;
				}
				// putToCache(data, path, rtn);
				return rtn;
			}
		}
		catch (SQLException e) {
			throw new DatabaseException(e, dc);
		}
		finally {
			release(dc);
			// manager.releaseConnection(CONNECTION_ID,dc);
		}
		return null;
	}

	public void create(ConnectionData data, int fullPathHash, int pathHash, String path, String name, int type) throws IOException {
		if (StringUtil.isEmpty(data.getDatasourceName())) throw new IOException("Missing datasource definition");

		removeFromCache(data, path, name);

		DatasourceConnection dc = null;
		try {
			dc = getDatasourceConnection(data);
			getCore(data).create(dc, data.getPrefix(), fullPathHash, pathHash, path, name, type);
		}
		catch (SQLException e) {
			throw new IOException(e.getMessage());
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
		finally {
			release(dc);
		}
	}

	public void delete(ConnectionData data, int fullPathHash, String path, String name) throws IOException {

		Attr attr = getAttr(data, fullPathHash, path, name);
		if (attr == null) throw new IOException("Can't delete resource [" + path + name + "], resource does not exist");

		DatasourceConnection dc = null;
		try {
			dc = getDatasourceConnection(data);
			getCore(data).delete(dc, data.getPrefix(), attr);
		}
		catch (SQLException e) {
			throw new IOException(e.getMessage());
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
		finally {
			removeFromCache(data, path, name);
			release(dc);
			// manager.releaseConnection(CONNECTION_ID,dc);
		}
	}

	public InputStream getInputStream(ConnectionData data, int fullPathHash, String path, String name) throws IOException {
		Attr attr = getAttr(data, fullPathHash, path, name);
		if (attr == null) throw new IOException("File [" + path + name + "] does not exist");
		DatasourceConnection dc = null;
		try {
			dc = getDatasourceConnection(data);
			return getCore(data).getInputStream(dc, data.getPrefix(), attr);
		}
		catch (SQLException e) {
			throw new IOException(e.getMessage());
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
		finally {
			release(dc);
			// manager.releaseConnection(CONNECTION_ID,dc);
		}
	}

	public synchronized OutputStream getOutputStream(ConnectionData data, int fullPathHash, int pathHash, String path, String name, boolean append) throws IOException {

		Attr attr = getAttr(data, fullPathHash, path, name);
		if (attr.getId() == 0) {
			create(data, fullPathHash, pathHash, path, name, Attr.TYPE_FILE);
			attr = getAttr(data, fullPathHash, path, name);
		}

		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream();
		pis.connect(pos);
		DatasourceConnection dc = null;
		// Connection c=null;
		try {
			dc = getDatasourceConnection(data);
			// Connection c = dc.getConnection();

			DataWriter writer = new DataWriter(getCore(data), dc, data.getPrefix(), attr, pis, this, append);
			writer.start();

			return new DatasourceResourceOutputStream(writer, pos);
			// core.getOutputStream(dc, name, attr, pis);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
		finally {
			removeFromCache(data, path, name);
			// manager.releaseConnection(CONNECTION_ID,dc);
		}
	}

	public boolean setLastModified(ConnectionData data, int fullPathHash, String path, String name, long time) {
		try {
			Attr attr = getAttr(data, fullPathHash, path, name);
			DatasourceConnection dc = getDatasourceConnection(data);
			try {
				getCore(data).setLastModified(dc, data.getPrefix(), attr, time);
			}
			/*
			 * catch (SQLException e) { return false; }
			 */
			finally {
				removeFromCache(data, path, name);
				release(dc);
				// manager.releaseConnection(CONNECTION_ID,dc);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		return true;
	}

	public boolean setMode(ConnectionData data, int fullPathHash, String path, String name, int mode) {
		try {
			Attr attr = getAttr(data, fullPathHash, path, name);
			DatasourceConnection dc = getDatasourceConnection(data);
			try {
				getCore(data).setMode(dc, data.getPrefix(), attr, mode);
			}
			/*
			 * catch (SQLException e) { return false; }
			 */
			finally {
				removeFromCache(data, path, name);
				release(dc);
				// manager.releaseConnection(CONNECTION_ID,dc);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		return true;
	}

	public boolean concatSupported(ConnectionData data) {
		try {
			return getCore(data).concatSupported();
		}
		catch (PageException e) {
			return false;
		}
	}

	private Attr removeFromCache(ConnectionData data, String path, String name) {
		attrsCache.remove(data.key() + path);
		SoftReference<Attr> rtn = attrCache.remove(data.key() + path + name);
		return rtn == null ? null : rtn.get();
	}

	private Attr getFromCache(ConnectionData data, String path, String name) {
		String key = data.key() + path + name;
		SoftReference<Attr> tmp = attrCache.get(key);
		Attr attr = tmp == null ? null : tmp.get();
		if (attr != null && attr.timestamp() + MAXAGE < System.currentTimeMillis()) {
			attrCache.remove(key);
			return null;
		}
		return attr;
	}

	private Attr putToCache(ConnectionData data, String path, String name, Attr attr) {
		attrCache.put(data.key() + path + name, new SoftReference<Attr>(attr));
		return attr;
	}

	/*
	 * private Attr[] getFromCache(ConnectionData data, String path) { String key=data.key()+path;
	 * Attr[] attrs= (Attr[]) attrsCache.get(key);
	 * 
	 * / *if(attr!=null && attr.timestamp()+MAXAGE<System.currentTimeMillis()) { attrCache.remove(key);
	 * return null; }* / return attrs; }
	 * 
	 * private Attr[] putToCache(ConnectionData data, String path, Attr[] attrs) {
	 * attrsCache.put(data.key()+path, attrs); return attrs; }
	 */

	public class ConnectionData {
		private String username;
		private String password;
		private String datasourceName;
		private String prefix;

		/**
		 * @return the prefix
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * @param prefix the prefix to set
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @return the datasourceName
		 */
		public String getDatasourceName() {
			return datasourceName;
		}

		/**
		 * @param datasourceName the datasourceName to set
		 */
		public void setDatasourceName(String datasourceName) {
			this.datasourceName = datasourceName;
		}

		public String key() {
			if (StringUtil.isEmpty(username)) return datasourceName;
			return username + ":" + password + "@" + datasourceName;
		}

	}

	/**
	 * release datasource connection
	 * 
	 * @param dc
	 * @param autoCommit
	 */
	void release(DatasourceConnection dc) {
		if (dc != null) {

			try {
				dc.getConnection().commit();
				dc.getConnection().setAutoCommit(true);
				dc.getConnection().setTransactionIsolation(((DatasourceConnectionPro) dc).getDefaultTransactionIsolation());
			}
			catch (SQLException e) {}

			getManager().releaseConnection(ThreadLocalPageContext.get(), dc);
		}
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return '/';
	}

}