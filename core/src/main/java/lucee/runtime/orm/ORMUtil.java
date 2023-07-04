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
package lucee.runtime.orm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.component.Property;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Constants;
import lucee.runtime.db.DataSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class ORMUtil {

	public static ORMSession getSession(PageContext pc) throws PageException {
		return getSession(pc, true);
	}

	public static ORMSession getSession(PageContext pc, boolean create) throws PageException {
		return ((PageContextImpl) pc).getORMSession(create);
	}

	public static ORMEngine getEngine(PageContext pc) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();
		return config.getORMEngine(pc);
	}

	/**
	 * 
	 * @param pc
	 * @param force if set to false the engine is on loaded when the configuration has changed
	 * @throws PageException
	 */
	public static void resetEngine(PageContext pc, boolean force) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();
		config.resetORMEngine(pc, force);
	}

	public static void printError(Exception t, ORMEngine engine) {
		printError(t, engine, t.getMessage());
	}

	public static void printError(String msg, ORMEngine engine) {
		printError(null, engine, msg);
	}

	public static void printError(Exception t) {
		printError(t, null, t.getMessage());
	}

	public static void printError(String msg) {
		printError(null, null, msg);
	}

	private static void printError(Exception t, ORMEngine engine, String msg) {
		if (engine != null) LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_ERROR, ORMUtil.class.getName(), "{" + engine.getLabel().toUpperCase() + "} - " + msg);
		else LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_ERROR, ORMUtil.class.getName(), msg);

		if (t == null) t = new Exception();
		LogUtil.log(ThreadLocalPageContext.get(), ORMUtil.class.getName(), t);
	}

	public static boolean equals(Object left, Object right) {
		HashSet<Object> done = new HashSet<Object>();
		return _equals(done, left, right);
	}

	private static boolean _equals(HashSet<Object> done, Object left, Object right) {

		if (left == right) return true;
		if (left == null || right == null) return false;

		// components
		if (left instanceof Component && right instanceof Component) {
			return _equals(done, (Component) left, (Component) right);
		}

		// arrays
		if (Decision.isArray(left) && Decision.isArray(right)) {
			return _equals(done, Caster.toArray(left, null), Caster.toArray(right, null));
		}

		// struct
		if (Decision.isStruct(left) && Decision.isStruct(right)) {
			return _equals(done, Caster.toStruct(left, null), Caster.toStruct(right, null));
		}

		try {
			return OpUtil.equals(ThreadLocalPageContext.get(), left, right, false);
		}
		catch (PageException e) {
			return false;
		}
	}

	private static boolean _equals(HashSet<Object> done, Collection left, Collection right) {
		if (done.contains(left)) return done.contains(right);
		done.add(left);
		done.add(right);

		if (left.size() != right.size()) return false;
		// Key[] keys = left.keys();
		Iterator<Entry<Key, Object>> it = left.entryIterator();
		Entry<Key, Object> e;
		Object l, r;
		while (it.hasNext()) {
			e = it.next();
			l = e.getValue();
			r = right.get(e.getKey(), null);
			if (r == null || !_equals(done, l, r)) return false;
		}
		return true;
	}

	private static boolean _equals(HashSet<Object> done, Component left, Component right) {
		if (done.contains(left)) return done.contains(right);
		done.add(left);
		done.add(right);

		if (left == null || right == null) return false;
		if (!left.getPageSource().equals(right.getPageSource())) return false;
		Property[] props = getProperties(left);
		Object l, r;
		props = getIds(props);
		for (int i = 0; i < props.length; i++) {
			l = left.getComponentScope().get(KeyImpl.init(props[i].getName()), null);
			r = right.getComponentScope().get(KeyImpl.init(props[i].getName()), null);
			if (!_equals(done, l, r)) return false;
		}
		return true;
	}

	public static Property[] getIds(Property[] props) {
		ArrayList<Property> ids = new ArrayList<Property>();
		for (int y = 0; y < props.length; y++) {
			String fieldType = Caster.toString(props[y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null);
			if ("id".equalsIgnoreCase(fieldType) || ListUtil.listFindNoCaseIgnoreEmpty(fieldType, "id", ',') != -1) ids.add(props[y]);
		}

		// no id field defined
		if (ids.size() == 0) {
			String fieldType;
			for (int y = 0; y < props.length; y++) {
				fieldType = Caster.toString(props[y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null);
				if (StringUtil.isEmpty(fieldType, true) && props[y].getName().equalsIgnoreCase("id")) {
					ids.add(props[y]);
					props[y].getDynamicAttributes().setEL(KeyConstants._fieldtype, "id");
				}
			}
		}

		// still no id field defined
		if (ids.size() == 0 && props.length > 0) {
			String owner = props[0].getOwnerName();
			if (!StringUtil.isEmpty(owner)) owner = ListUtil.last(owner, '.').trim();

			String fieldType;
			if (!StringUtil.isEmpty(owner)) {
				String id = owner + "id";
				for (int y = 0; y < props.length; y++) {
					fieldType = Caster.toString(props[y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null);
					if (StringUtil.isEmpty(fieldType, true) && props[y].getName().equalsIgnoreCase(id)) {
						ids.add(props[y]);
						props[y].getDynamicAttributes().setEL(KeyConstants._fieldtype, "id");
					}
				}
			}
		}
		return ids.toArray(new Property[ids.size()]);
	}

	public static Object getPropertyValue(Component cfc, String name, Object defaultValue) {
		Property[] props = getProperties(cfc);

		for (int i = 0; i < props.length; i++) {
			if (!props[i].getName().equalsIgnoreCase(name)) continue;
			return cfc.getComponentScope().get(KeyImpl.init(name), null);
		}
		return defaultValue;
	}
	/*
	 * jira2049 public static Object getPropertyValue(ORMSession session,Component cfc, String name,
	 * Object defaultValue) { Property[] props=getProperties(cfc); Object raw=null; SessionImpl
	 * sess=null; if(session!=null){ raw=session.getRawSession(); if(raw instanceof SessionImpl)
	 * sess=(SessionImpl) raw; } Object val; for(int i=0;i<props.length;i++){
	 * if(!props[i].getName().equalsIgnoreCase(name)) continue; val =
	 * cfc.getComponentScope().get(KeyImpl.getInstance(name),null); if(sess!=null && !(val instanceof
	 * PersistentCollection)){ if(val instanceof List) return new PersistentList(sess,(List)val); if(val
	 * instanceof Map && !(val instanceof Component)) return new PersistentMap(sess,(Map)val); if(val
	 * instanceof Set) return new PersistentSet(sess,(Set)val); if(val instanceof Array) return new
	 * PersistentList(sess,Caster.toList(val,null));
	 * 
	 * } return val; } return defaultValue; }
	 */

	private static Property[] getProperties(Component cfc) {
		return cfc.getProperties(true, true, false, false);
	}

	public static boolean isRelated(Property prop) {
		String fieldType = Caster.toString(prop.getDynamicAttributes().get(KeyConstants._fieldtype, "column"), "column");
		if (StringUtil.isEmpty(fieldType, true)) return false;
		fieldType = fieldType.toLowerCase().trim();

		if ("one-to-one".equals(fieldType)) return true;
		if ("many-to-one".equals(fieldType)) return true;
		if ("one-to-many".equals(fieldType)) return true;
		if ("many-to-many".equals(fieldType)) return true;
		return false;
	}

	public static Struct convertToSimpleMap(String paramsStr) {
		paramsStr = paramsStr.trim();
		if (!StringUtil.startsWith(paramsStr, '{') || !StringUtil.endsWith(paramsStr, '}')) return null;

		paramsStr = paramsStr.substring(1, paramsStr.length() - 1);
		String items[] = ListUtil.listToStringArray(paramsStr, ',');

		Struct params = new StructImpl();
		String arr$[] = items;
		int index;
		for (int i = 0; i < arr$.length; i++) {
			String pair = arr$[i];
			index = pair.indexOf('=');
			if (index == -1) return null;

			params.setEL(KeyImpl.init(deleteQuotes(pair.substring(0, index).trim()).trim()), deleteQuotes(pair.substring(index + 1).trim()));
		}

		return params;
	}

	private static String deleteQuotes(String str) {
		if (StringUtil.isEmpty(str, true)) return "";
		char first = str.charAt(0);
		if ((first == '\'' || first == '"') && StringUtil.endsWith(str, first)) return str.substring(1, str.length() - 1);
		return str;
	}

	public static DataSource getDefaultDataSource(PageContext pc) throws PageException {
		pc = ThreadLocalPageContext.get(pc);
		Object o = pc.getApplicationContext().getORMDataSource();

		if (StringUtil.isEmpty(o)) {
			boolean isCFML = pc.getRequestDialect() == CFMLEngine.DIALECT_CFML;
			throw ORMExceptionUtil.createException((ORMSession) null/* no session here, otherwise we get an infinite loop */, null,
					"missing datasource definition in " + (isCFML ? Constants.CFML_APPLICATION_EVENT_HANDLER : Constants.LUCEE_APPLICATION_EVENT_HANDLER) + "/"
							+ (isCFML ? Constants.CFML_APPLICATION_TAG_NAME : Constants.LUCEE_APPLICATION_TAG_NAME),
					null);
		}
		return o instanceof DataSource ? (DataSource) o : pc.getDataSource(Caster.toString(o));
	}

	public static DataSource getDefaultDataSource(PageContext pc, DataSource defaultValue) {
		pc = ThreadLocalPageContext.get(pc);
		Object o = pc.getApplicationContext().getORMDataSource();
		if (StringUtil.isEmpty(o)) return defaultValue;
		try {
			return o instanceof DataSource ? (DataSource) o : pc.getDataSource(Caster.toString(o));
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public static DataSource getDataSource(PageContext pc, String dsn, DataSource defaultValue) {
		if (StringUtil.isEmpty(dsn, true)) return ORMUtil.getDefaultDataSource(pc, defaultValue);
		return ((PageContextImpl) pc).getDataSource(dsn.trim(), defaultValue);
	}

	public static DataSource getDataSource(PageContext pc, String dsn) throws PageException {
		if (StringUtil.isEmpty(dsn, true)) return ORMUtil.getDefaultDataSource(pc);
		return ((PageContextImpl) pc).getDataSource(dsn.trim());
	}

	/**
	 * if the given component has defined a datasource in the meta data, lucee is returning this
	 * datasource, otherwise the default orm datasource is returned
	 * 
	 * @param pc
	 * @param cfc
	 * @return
	 * @throws PageException
	 */
	public static DataSource getDataSource(PageContext pc, Component cfc, DataSource defaultValue) {
		pc = ThreadLocalPageContext.get(pc);

		// datasource defined with cfc
		try {
			Struct meta = cfc.getMetaData(pc);
			String datasourceName = Caster.toString(meta.get(KeyConstants._datasource, null), null);
			if (!StringUtil.isEmpty(datasourceName, true)) {
				DataSource ds = pc.getDataSource(datasourceName, null);
				if (ds != null) return ds;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return getDefaultDataSource(pc, defaultValue);
	}

	/**
	 * if the given component has defined a datasource in the meta data, lucee is returning this
	 * datasource, otherwise the default orm datasource is returned
	 * 
	 * @param pc
	 * @param cfc
	 * @return
	 * @throws PageException
	 */
	public static DataSource getDataSource(PageContext pc, Component cfc) throws PageException {
		pc = ThreadLocalPageContext.get(pc);

		// datasource defined with cfc
		Struct meta = cfc.getMetaData(pc);
		String datasourceName = Caster.toString(meta.get(KeyConstants._datasource, null), null);
		if (!StringUtil.isEmpty(datasourceName, true)) {
			return pc.getDataSource(datasourceName);
		}

		return getDefaultDataSource(pc);
	}

	public static String getDataSourceName(PageContext pc, Component cfc) throws PageException {
		pc = ThreadLocalPageContext.get(pc);

		// datasource defined with cfc
		Struct meta = cfc.getMetaData(pc);
		String datasourceName = Caster.toString(meta.get(KeyConstants._datasource, null), null);
		if (!StringUtil.isEmpty(datasourceName, true)) {
			return datasourceName.trim();
		}
		return getDefaultDataSource(pc).getName();
	}

	public static String getDataSourceName(PageContext pc, Component cfc, String defaultValue) {
		pc = ThreadLocalPageContext.get(pc);

		// datasource defined with cfc
		Struct meta = null;
		try {
			meta = cfc.getMetaData(pc);
			String datasourceName = Caster.toString(meta.get(KeyConstants._datasource, null), null);
			if (!StringUtil.isEmpty(datasourceName, true)) {
				return datasourceName.trim();
			}
		}
		catch (PageException e) {
		}

		DataSource ds = getDefaultDataSource(pc, null);
		if (ds != null) return ds.getName();
		return defaultValue;
	}
}