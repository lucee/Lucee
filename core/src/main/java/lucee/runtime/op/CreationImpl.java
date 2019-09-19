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
package lucee.runtime.op;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lucee.cli.servlet.ServletConfigImpl;
import lucee.cli.servlet.ServletContextImpl;
import lucee.commons.date.DateTimeUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.lang.types.RefDouble;
import lucee.commons.lang.types.RefDoubleImpl;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.commons.lang.types.RefIntegerSync;
import lucee.commons.lang.types.RefLong;
import lucee.commons.lang.types.RefLongImpl;
import lucee.commons.lang.types.RefString;
import lucee.commons.lang.types.RefStringImpl;
import lucee.commons.lock.KeyLock;
import lucee.commons.lock.KeyLockImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.component.PropertyImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateGUID;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.functions.struct.StructNew;
import lucee.runtime.functions.system.ContractPath;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTask;
import lucee.runtime.spooler.remote.RemoteClientTask;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.CastableStruct;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.ClusterEntry;
import lucee.runtime.type.scope.ClusterEntryImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.Creation;

/**
 * implemention of the ctration object
 */
public final class CreationImpl implements Creation, Serializable {

	private static CreationImpl singelton;

	private CreationImpl(CFMLEngine engine) {
		// !!! do not store engine Object, the engine is not serializable
	}

	/**
	 * @return singleton instance
	 */
	public static Creation getInstance(CFMLEngine engine) {
		if (singelton == null) singelton = new CreationImpl(engine);
		return singelton;
	}

	@Override
	public Array createArray() {
		return new ArrayImpl();
	}

	@Override
	public Array createArray(String list, String delimiter, boolean removeEmptyItem, boolean trim) {
		if (removeEmptyItem) return ListUtil.listToArrayRemoveEmpty(list, delimiter);
		if (trim) return ListUtil.listToArrayTrim(list, delimiter);
		return ListUtil.listToArray(list, delimiter);
	}

	@Override
	public Array createArray(int dimension) throws PageException {
		return ArrayUtil.getInstance(dimension);
	}

	@Override
	public Struct createStruct() {
		return new StructImpl();
	}

	@Override
	public Struct createStruct(int type) {
		return new StructImpl(type);
	}

	@Override
	public Struct createStruct(String type) throws ApplicationException {
		return new StructImpl(StructNew.toType(type));
	}

	@Override
	public Query createQuery(String[] columns, int rows, String name) {
		return new QueryImpl(columns, rows, name);
	}

	@Override
	public Query createQuery(Collection.Key[] columns, int rows, String name) throws DatabaseException {
		return new QueryImpl(columns, rows, name);
	}

	@Override
	public Query createQuery(String[] columns, String[] types, int rows, String name) throws DatabaseException {
		return new QueryImpl(columns, types, rows, name);
	}

	@Override
	public Query createQuery(Collection.Key[] columns, String[] types, int rows, String name) throws DatabaseException {
		return new QueryImpl(columns, types, rows, name);
	}

	@Override
	public Query createQuery(DatasourceConnection dc, SQL sql, int maxrow, int fetchsize, int timeout, String name) throws PageException {
		return new QueryImpl(ThreadLocalPageContext.get(), dc, sql, maxrow, fetchsize, TimeSpanImpl.fromMillis(timeout * 1000), name);
	}

	@Override
	public DateTime createDateTime(long time) {
		return new DateTimeImpl(time, false);
	}

	@Override
	public TimeSpan createTimeSpan(int day, int hour, int minute, int second) {
		return new TimeSpanImpl(day, hour, minute, second);
	}

	@Override
	public Date createDate(long time) {
		return new DateImpl(time);
	}

	@Override
	public Time createTime(long time) {
		return new TimeImpl(time, false);
	}

	@Override
	public DateTime createDateTime(int year, int month, int day, int hour, int minute, int second, int millis) throws ExpressionException {
		return DateTimeUtil.getInstance().toDateTime(ThreadLocalPageContext.getTimeZone(), year, month, day, hour, minute, second, millis);
	}

	@Override
	public Date createDate(int year, int month, int day) throws ExpressionException {
		return new DateImpl(DateTimeUtil.getInstance().toDateTime(null, year, month, day, 0, 0, 0, 0));
	}

	@Override
	public Time createTime(int hour, int minute, int second, int millis) {
		return new TimeImpl(DateTimeUtil.getInstance().toTime(null, 1899, 12, 30, hour, minute, second, millis, 0), false);
	}

	@Override
	public Key createKey(String key) {
		return KeyImpl.init(key);
	}

	@Override
	public SpoolerTask createRemoteClientTask(ExecutionPlan[] plans, RemoteClient remoteClient, Struct attrColl, String callerId, String type) {
		return new RemoteClientTask(plans, remoteClient, attrColl, callerId, type);
	}

	@Override
	public ClusterEntry createClusterEntry(Key key, Serializable value, int offset) {
		return new ClusterEntryImpl(key, value, offset);
	}

	@Override
	public Resource createResource(String path, boolean existing) throws PageException {
		if (existing) return ResourceUtil.toResourceExisting(ThreadLocalPageContext.get(), path);
		return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path);
	}

	@Override
	public HttpServletRequest createHttpServletRequest(File contextRoot, String serverName, String scriptName, String queryString, Cookie[] cookies, Map<String, Object> headers,
			Map<String, String> parameters, Map<String, Object> attributes, HttpSession session) {

		// header
		Pair<String, Object>[] _headers = new Pair[headers.size()];
		{
			int index = 0;
			Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
			Entry<String, Object> entry;
			while (it.hasNext()) {
				entry = it.next();
				_headers[index++] = new Pair<String, Object>(entry.getKey(), entry.getValue());
			}
		}
		// parameters
		Pair<String, Object>[] _parameters = new Pair[headers.size()];
		{
			int index = 0;
			Iterator<Entry<String, String>> it = parameters.entrySet().iterator();
			Entry<String, String> entry;
			while (it.hasNext()) {
				entry = it.next();
				_parameters[index++] = new Pair<String, Object>(entry.getKey(), entry.getValue());
			}
		}

		return new HttpServletRequestDummy(ResourceUtil.toResource(contextRoot), serverName, scriptName, queryString, cookies, _headers, _parameters,
				Caster.toStruct(attributes, null), session, null);
	}

	@Override
	public HttpServletResponse createHttpServletResponse(OutputStream io) {
		return new HttpServletResponseDummy(io); // do not change, flex extension is depending on this
	}

	// FUTURE add to interface
	public ServletConfig createServletConfig(File root, Map<String, Object> attributes, Map<String, String> params) {
		final String servletName = "";
		if (attributes == null) attributes = new HashMap<String, Object>();
		if (params == null) params = new HashMap<String, String>();
		if (root == null) root = new File("."); // working directory that the java command was called from

		final ServletContextImpl servletContext = new ServletContextImpl(root, attributes, params, 1, 0);
		final ServletConfigImpl servletConfig = new ServletConfigImpl(servletContext, servletName);
		return servletConfig;
	}

	@Override
	public PageContext createPageContext(HttpServletRequest req, HttpServletResponse rsp, OutputStream out) {
		Config config = ThreadLocalPageContext.getConfig();
		if (!(config instanceof ConfigWeb)) throw new RuntimeException("need a web context to create a PageContext");
		CFMLFactory factory = ((ConfigWeb) config).getFactory();

		return (PageContext) factory.getPageContext(factory.getServlet(), req, rsp, null, false, -1, false);
	}

	@Override
	public Component createComponentFromName(PageContext pc, String fullName) throws PageException {
		return pc.loadComponent(fullName);
	}

	@Override
	public Component createComponentFromPath(PageContext pc, String path) throws PageException {
		path = path.trim();
		String pathContracted = ContractPath.call(pc, path);

		if (Constants.isComponentExtension(ResourceUtil.getExtension(pathContracted, ""))) pathContracted = ResourceUtil.removeExtension(pathContracted, pathContracted);

		pathContracted = pathContracted.replace(File.pathSeparatorChar, '.').replace('/', '.').replace('\\', '.');

		while (pathContracted.toLowerCase().startsWith("."))
			pathContracted = pathContracted.substring(1);

		return createComponentFromName(pc, pathContracted);
	}

	@Override
	public RefBoolean createRefBoolean(boolean b) {
		return new RefBooleanImpl(b);
	}

	@Override
	public RefInteger createRefInteger(int i) {
		return new RefIntegerImpl(i);
	}

	// FUTURE add this and more to interface
	public RefInteger createRefInteger(int i, boolean _syncronized) {
		return _syncronized ? new RefIntegerSync(i) : new RefIntegerImpl(i);
	}

	@Override
	public RefLong createRefLong(long l) {
		return new RefLongImpl(l);
	}

	@Override
	public RefDouble createRefDouble(long d) {
		return new RefDoubleImpl(d);
	}

	@Override
	public RefString createRefString(String value) {
		return new RefStringImpl(value);
	}

	@Override
	public String createUUID() {
		return CreateUUID.invoke();
	}

	@Override
	public String createGUID() {
		return CreateGUID.invoke();
	}

	@Override
	public Property createProperty(String name, String type) {
		PropertyImpl pi = new PropertyImpl();
		pi.setName(name);
		pi.setType(type);
		return pi;
	}

	@Override
	public Mapping createMapping(Config config, String virtual, String strPhysical, String strArchive, short inspect, boolean physicalFirst, boolean hidden, boolean readonly,
			boolean topLevel, boolean appMapping, boolean ignoreVirtual, ApplicationListener appListener, int listenerMode, int listenerType) {
		return new MappingImpl(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, readonly, topLevel, appMapping, ignoreVirtual, appListener, listenerMode,
				listenerType);
	}

	@Override
	public Struct createCastableStruct(Object value) {
		return new CastableStruct(value);
	}

	@Override
	public Struct createCastableStruct(Object value, int type) {
		return new CastableStruct(value, type);
	}

	@Override
	public DateTime now() {
		return new DateTimeImpl();
	}

	@Override
	public <K> KeyLock<K> createKeyLock() {
		// TODO Auto-generated method stub
		return new KeyLockImpl<K>();
	}

}