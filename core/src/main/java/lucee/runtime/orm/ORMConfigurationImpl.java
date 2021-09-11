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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ORMConfigurationImpl implements ORMConfiguration {

	public static final int DBCREATE_NONE = 0;
	public static final int DBCREATE_UPDATE = 1;
	public static final int DBCREATE_DROP_CREATE = 2;

	public static final Key AUTO_GEN_MAP = KeyImpl.getInstance("autogenmap");
	public static final Key CATALOG = KeyConstants._catalog;
	public static final Key IS_DEFAULT_CFC_LOCATION = KeyImpl.getInstance("isDefaultCfclocation");
	public static final Key DB_CREATE = KeyImpl.getInstance("dbCreate");
	public static final Key DIALECT = KeyConstants._dialect;
	public static final Key FLUSH_AT_REQUEST_END = KeyImpl.getInstance("flushAtRequestEnd");
	public static final Key LOG_SQL = KeyImpl.getInstance("logSql");
	public static final Key SAVE_MAPPING = KeyImpl.getInstance("savemapping");
	public static final Key SCHEMA = KeyConstants._schema;
	public static final Key SECONDARY_CACHE_ENABLED = KeyImpl.getInstance("secondarycacheenabled");
	public static final Key SQL_SCRIPT = KeyImpl.getInstance("sqlscript");
	public static final Key USE_DB_FOR_MAPPING = KeyImpl.getInstance("useDBForMapping");
	public static final Key CACHE_CONFIG = KeyImpl.getInstance("cacheconfig");
	public static final Key CACHE_PROVIDER = KeyImpl.getInstance("cacheProvider");
	public static final Key ORM_CONFIG = KeyImpl.getInstance("ormConfig");
	public static final Key EVENT_HANDLING = KeyImpl.getInstance("eventHandling");
	public static final Key EVENT_HANDLER = KeyImpl.getInstance("eventHandler");
	public static final Key AUTO_MANAGE_SESSION = KeyImpl.getInstance("autoManageSession");
	public static final Key NAMING_STRATEGY = KeyImpl.getInstance("namingstrategy");
	public static final Key CFC_LOCATION = KeyConstants._cfcLocation;

	private boolean autogenmap = true;
	private Resource[] cfcLocations;
	private Boolean eventHandling = null;
	private boolean flushAtRequestEnd = true;
	private boolean logSQL;
	private boolean saveMapping;
	private boolean secondaryCacheEnabled;
	private boolean useDBForMapping = true;
	private Resource cacheConfig;
	private String cacheProvider;
	private Resource ormConfig;
	private String eventHandler;
	private String namingStrategy;
	private boolean isDefaultCfcLocation = true;
	private boolean skipCFCWithError = true;
	private boolean autoManageSession = true;
	private ApplicationContext ac;

	private Map<String, String> dbCreateMap;
	private String dbCreateDefault = "";

	private Map<String, String> dialectMap;
	private String dialectDefault = "";

	private Map<String, String> schemaMap;
	private String schemaDefault = "";

	private Map<String, String> catalogMap;
	private String catalogDefault = "";

	private Map<String, String> sqlScriptMap;
	private String sqlScriptDefault = "";
	private Config config;

	private ORMConfigurationImpl() {
		autogenmap = true;
		flushAtRequestEnd = true;
		useDBForMapping = true;
	}

	public static ORMConfiguration load(Config config, ApplicationContext ac, Element el, Resource defaultCFCLocation, ORMConfiguration defaultConfig) {
		return _load(config, ac, new _GetElement(el), defaultCFCLocation, defaultConfig);
	}

	public static ORMConfiguration load(Config config, ApplicationContext ac, Struct settings, Resource defaultCFCLocation, ORMConfiguration defaultConfig) {
		return _load(config, ac, new _GetStruct(settings), defaultCFCLocation, defaultConfig);
	}

	private static ORMConfiguration _load(Config config, ApplicationContext ac, _Get settings, Resource defaultCFCLocation, ORMConfiguration _dc) {
		ORMConfigurationImpl dc = (ORMConfigurationImpl) _dc;
		if (dc == null) dc = new ORMConfigurationImpl();
		ORMConfigurationImpl c = dc.duplicate();
		c.config = config;
		c.cfcLocations = defaultCFCLocation == null ? new Resource[0] : new Resource[] { defaultCFCLocation };

		// autogenmap
		c.autogenmap = Caster.toBooleanValue(settings.get(AUTO_GEN_MAP, dc.autogenmap()), dc.autogenmap());

		// cfclocation
		Object obj = settings.get(KeyConstants._cfcLocation, null);

		if (obj != null) {
			java.util.List<Resource> list = AppListenerUtil.loadResources(config, ac, obj, true);

			if (list != null && list.size() > 0) {
				c.cfcLocations = list.toArray(new Resource[list.size()]);
				c.isDefaultCfcLocation = false;
			}
		}
		if (c.cfcLocations == null) c.cfcLocations = defaultCFCLocation == null ? new Resource[0] : new Resource[] { defaultCFCLocation };

		// catalog
		obj = settings.get(CATALOG, null);
		if (!StringUtil.isEmpty(obj)) {
			Coll coll = _load(obj);
			c.catalogDefault = StringUtil.emptyIfNull(coll.def);
			c.catalogMap = coll.map;
		}
		else {
			c.catalogDefault = StringUtil.emptyIfNull(dc.catalogDefault);
			c.catalogMap = dc.catalogMap;
		}

		// dbcreate
		obj = settings.get(DB_CREATE, null);
		if (!StringUtil.isEmpty(obj)) {
			Coll coll = _load(obj);
			c.dbCreateDefault = StringUtil.emptyIfNull(coll.def);
			c.dbCreateMap = coll.map;
		}
		else {
			c.dbCreateDefault = StringUtil.emptyIfNull(dc.dbCreateDefault);
			c.dbCreateMap = dc.dbCreateMap;
		}

		// dialect
		obj = settings.get(DIALECT, null);
		if (!StringUtil.isEmpty(obj)) {
			Coll coll = _load(obj);
			c.dialectDefault = StringUtil.emptyIfNull(coll.def);
			c.dialectMap = coll.map;
		}
		else {
			c.dialectDefault = StringUtil.emptyIfNull(dc.dialectDefault);
			c.dialectMap = dc.dialectMap;
		}

		// sqlscript
		obj = settings.get(SQL_SCRIPT, null);
		if (!StringUtil.isEmpty(obj)) {
			Coll coll = _load(obj);
			c.sqlScriptDefault = StringUtil.emptyIfNull(coll.def);
			c.sqlScriptMap = coll.map;
		}
		else {
			c.sqlScriptDefault = StringUtil.emptyIfNull(dc.sqlScriptDefault);
			c.sqlScriptMap = dc.sqlScriptMap;
		}

		// namingstrategy
		c.namingStrategy = Caster.toString(settings.get(NAMING_STRATEGY, dc.namingStrategy()), dc.namingStrategy());

		// eventHandler
		c.eventHandler = Caster.toString(settings.get(EVENT_HANDLER, dc.eventHandler()), dc.eventHandler());

		// eventHandling
		Boolean b = Caster.toBoolean(settings.get(EVENT_HANDLING, null), null);
		if (b == null) {
			if (dc.eventHandling()) b = Boolean.TRUE;
			else b = !StringUtil.isEmpty(c.eventHandler, true);
		}
		c.eventHandling = b;

		// flushatrequestend
		c.flushAtRequestEnd = Caster.toBooleanValue(settings.get(FLUSH_AT_REQUEST_END, dc.flushAtRequestEnd()), dc.flushAtRequestEnd());

		// logSQL
		c.logSQL = Caster.toBooleanValue(settings.get(LOG_SQL, dc.logSQL()), dc.logSQL());

		// autoManageSession
		c.autoManageSession = Caster.toBooleanValue(settings.get(AUTO_MANAGE_SESSION, dc.autoManageSession()), dc.autoManageSession());

		// skipCFCWithError
		c.skipCFCWithError = Caster.toBooleanValue(settings.get(KeyConstants._skipCFCWithError, dc.skipCFCWithError()), dc.skipCFCWithError());

		// savemapping
		c.saveMapping = Caster.toBooleanValue(settings.get(SAVE_MAPPING, dc.saveMapping()), dc.saveMapping());

		// schema
		// c.schema = StringUtil.trim(Caster.toString(settings.get(SCHEMA, dc.getSchema()), dc.getSchema()),
		// dc.getSchema());
		obj = settings.get(SCHEMA, null);
		if (obj != null) {
			Coll coll = _load(obj);
			c.schemaDefault = StringUtil.emptyIfNull(coll.def);
			c.schemaMap = coll.map;
		}
		else {
			c.schemaDefault = StringUtil.emptyIfNull(dc.schemaDefault);
			c.schemaMap = dc.schemaMap;
		}

		// secondarycacheenabled
		c.secondaryCacheEnabled = Caster.toBooleanValue(settings.get(SECONDARY_CACHE_ENABLED, dc.secondaryCacheEnabled()), dc.secondaryCacheEnabled());

		// useDBForMapping
		c.useDBForMapping = Caster.toBooleanValue(settings.get(USE_DB_FOR_MAPPING, dc.useDBForMapping()), dc.useDBForMapping());

		// cacheconfig
		obj = settings.get(CACHE_CONFIG, null);
		if (!StringUtil.isEmpty(obj)) {
			try {
				c.cacheConfig = toRes(config, obj, true);
			}
			catch (ExpressionException e) {
				// print.printST(e);
			}
		}

		// cacheprovider
		c.cacheProvider = StringUtil.trim(Caster.toString(settings.get(CACHE_PROVIDER, dc.getCacheProvider()), dc.getCacheProvider()), dc.getCacheProvider());

		// ormconfig
		obj = settings.get(ORM_CONFIG, null);
		if (!StringUtil.isEmpty(obj)) {
			try {
				c.ormConfig = toRes(config, obj, true);
			}
			catch (ExpressionException e) {
				// print.printST(e);
			}
		}
		c.ac = ac;

		return c;
	}

	private static Coll _load(Object obj) {
		final Coll coll = new Coll();
		if (obj != null) {
			// multi
			if (Decision.isStruct(obj)) {
				Struct sct = Caster.toStruct(obj, null);
				if (sct != null) {
					Iterator<Entry<Key, Object>> it = sct.entryIterator();
					coll.map = new HashMap<String, String>();
					Entry<Key, Object> e;
					String k;
					String v;
					while (it.hasNext()) {
						e = it.next();
						k = e.getKey().getLowerString().trim();
						v = Caster.toString(e.getValue(), "").trim();

						if ("__default__".equals(k) || "".equals(k)) coll.def = v;
						else coll.map.put(k, v);
					}
				}
			}
			else {
				coll.def = Caster.toString(obj, "").trim();
			}
		}
		return coll;
	}

	private static Resource toRes(Config config, Object obj, boolean existing) throws ExpressionException {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) return Caster.toResource(pc, obj, existing);
		return Caster.toResource(config, obj, existing);
	}

	private static Resource toResEL(Config config, Object obj, boolean existing) {
		PageContext pc = ThreadLocalPageContext.get();
		try {
			if (pc != null) return Caster.toResource(pc, obj, existing);
			return Caster.toResource(config, obj, existing);
		}
		catch (PageException pe) {
			return null;
		}
	}

	private ORMConfigurationImpl duplicate() {
		ORMConfigurationImpl other = new ORMConfigurationImpl();
		other.autogenmap = autogenmap;
		other.cfcLocations = cfcLocations;
		other.isDefaultCfcLocation = isDefaultCfcLocation;
		other.dbCreateMap = dbCreateMap;
		other.eventHandler = eventHandler;
		other.namingStrategy = namingStrategy;
		other.eventHandling = eventHandling;
		other.flushAtRequestEnd = flushAtRequestEnd;
		other.logSQL = logSQL;
		other.saveMapping = saveMapping;
		other.secondaryCacheEnabled = secondaryCacheEnabled;
		other.useDBForMapping = useDBForMapping;
		other.cacheConfig = cacheConfig;
		other.cacheProvider = cacheProvider;
		other.ormConfig = ormConfig;
		other.autoManageSession = autoManageSession;
		other.skipCFCWithError = skipCFCWithError;

		other.dbCreateDefault = dbCreateDefault;
		other.dbCreateMap = dbCreateMap;
		other.dialectDefault = dialectDefault;
		other.dialectMap = dialectMap;
		other.schemaDefault = schemaDefault;
		other.schemaMap = schemaMap;
		other.catalogDefault = catalogDefault;
		other.catalogMap = catalogMap;

		other.sqlScriptDefault = sqlScriptDefault;
		other.sqlScriptMap = sqlScriptMap;
		return other;
	}

	@Override
	public String hash() { // no longer used in Hibernate 3.5.5.72 and above
		ApplicationContext _ac = ac;
		if (_ac == null) _ac = ThreadLocalPageContext.get().getApplicationContext();
		Object ds = _ac.getORMDataSource();
		ORMConfiguration ormConf = _ac.getORMConfiguration();

		StringBuilder data = new StringBuilder().append(ormConf.autogenmap()).append(':').append(ormConf.getCatalog()).append(':').append(ormConf.isDefaultCfcLocation())
				.append(':').append(ormConf.eventHandling()).append(':').append(ormConf.namingStrategy()).append(':').append(ormConf.eventHandler()).append(':')
				.append(ormConf.flushAtRequestEnd()).append(':').append(ormConf.logSQL()).append(':').append(ormConf.autoManageSession()).append(':')
				.append(ormConf.skipCFCWithError()).append(':').append(ormConf.saveMapping()).append(':').append(ormConf.getSchema()).append(':')
				.append(ormConf.secondaryCacheEnabled()).append(':').append(ormConf.useDBForMapping()).append(':').append(ormConf.getCacheProvider()).append(':').append(ds)
				.append(':');

		append(data, ormConf.getCfcLocations());
		append(data, ormConf.getSqlScript());
		append(data, ormConf.getCacheConfig());
		append(data, ormConf.getOrmConfig());

		append(data, dbCreateDefault, dbCreateMap);
		append(data, catalogDefault, catalogMap);
		append(data, dialectDefault, dialectMap);
		append(data, schemaDefault, schemaMap);
		append(data, sqlScriptDefault, sqlScriptMap);

		return CFMLEngineFactory.getInstance().getSystemUtil().hash64b(data.toString());
	}

	private static void append(StringBuilder data, String def, Map<String, String> map) {
		data.append(':').append(def);
		if (map != null) {
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			Entry<String, String> e;
			while (it.hasNext()) {
				e = it.next();
				data.append(':').append(e.getKey()).append(':').append(e.getValue());
			}
		}
	}

	private void append(StringBuilder data, Resource[] reses) {
		if (reses == null) return;
		for (int i = 0; i < reses.length; i++) {
			append(data, reses[i]);
		}
	}

	private void append(StringBuilder data, Resource res) {
		if (res == null) return;
		if (res.isFile()) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			try {
				data.append(eng.getSystemUtil().hash64b(eng.getIOUtil().toString(res, null)));
				return;
			}
			catch (IOException e) {
			}
		}
		data.append(res.getAbsolutePath()).append(':');
	}

	/**
	 * @return the autogenmap
	 */
	@Override
	public boolean autogenmap() {
		return autogenmap;
	}

	/**
	 * @return the cfcLocation
	 */
	@Override
	public Resource[] getCfcLocations() {
		return cfcLocations;
	}

	@Override
	public boolean isDefaultCfcLocation() {
		return isDefaultCfcLocation;
	}

	@Override
	public int getDbCreate() {
		return dbCreateAsInt(dbCreateDefault);
	}

	public int getDbCreate(String datasourceName) { // FUTURE add to interface
		return dbCreateAsInt(_get(datasourceName, dbCreateDefault, dbCreateMap));
	}

	@Override
	public String getDialect() {
		return dialectDefault;
	}

	public String getDialect(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, dialectDefault, dialectMap);
	}

	@Override
	public String getSchema() {
		return schemaDefault;
	}

	public String getSchema(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, schemaDefault, schemaMap);
	}

	@Override
	public String getCatalog() {
		return catalogDefault;
	}

	public String getCatalog(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, catalogDefault, catalogMap);
	}

	@Override
	public Resource getSqlScript() {
		if (StringUtil.isEmpty(sqlScriptDefault)) return null;
		return toResEL(config, sqlScriptDefault, true);
	}

	public Resource getSqlScript(String datasourceName) { // FUTURE add to interface
		String res = _get(datasourceName, sqlScriptDefault, sqlScriptMap);
		if (StringUtil.isEmpty(res)) return null;
		return toResEL(config, res, true);
	}

	private static String _get(String datasourceName, String def, Map<String, String> map) {
		if (map != null && !StringUtil.isEmpty(datasourceName)) {
			datasourceName = datasourceName.toLowerCase().trim();
			String res = map.get(datasourceName);
			if (!StringUtil.isEmpty(res)) return res;
		}
		return def;
	}

	@Override
	public boolean eventHandling() {
		return eventHandling == null ? false : eventHandling.booleanValue();
	}

	@Override
	public String eventHandler() {
		return eventHandler;
	}

	@Override
	public String namingStrategy() {
		return namingStrategy;
	}

	@Override
	public boolean flushAtRequestEnd() {
		return flushAtRequestEnd;
	}

	@Override
	public boolean logSQL() {
		return logSQL;
	}

	@Override
	public boolean saveMapping() {
		return saveMapping;
	}

	@Override
	public boolean secondaryCacheEnabled() {
		return secondaryCacheEnabled;
	}

	@Override
	public boolean useDBForMapping() {
		return useDBForMapping;
	}

	@Override
	public Resource getCacheConfig() {
		return cacheConfig;
	}

	@Override
	public String getCacheProvider() {
		return cacheProvider;
	}

	@Override
	public Resource getOrmConfig() {
		return ormConfig;
	}

	@Override
	public boolean skipCFCWithError() {
		return skipCFCWithError;
	}

	@Override
	public boolean autoManageSession() {
		return autoManageSession;
	}

	@Override
	public Object toStruct() {

		Resource[] locs = getCfcLocations();
		Array arrLocs = new ArrayImpl();
		if (locs != null) for (int i = 0; i < locs.length; i++) {
			arrLocs.appendEL(getAbsolutePath(locs[i]));
		}
		Struct sct = new StructImpl();
		sct.setEL(AUTO_GEN_MAP, this.autogenmap());
		sct.setEL(CFC_LOCATION, arrLocs);
		sct.setEL(IS_DEFAULT_CFC_LOCATION, isDefaultCfcLocation());
		sct.setEL(EVENT_HANDLING, eventHandling());
		sct.setEL(EVENT_HANDLER, eventHandler());
		sct.setEL(NAMING_STRATEGY, namingStrategy());
		sct.setEL(FLUSH_AT_REQUEST_END, flushAtRequestEnd());
		sct.setEL(LOG_SQL, logSQL());
		sct.setEL(SAVE_MAPPING, saveMapping());
		sct.setEL(SECONDARY_CACHE_ENABLED, secondaryCacheEnabled());
		sct.setEL(USE_DB_FOR_MAPPING, useDBForMapping());
		sct.setEL(CACHE_CONFIG, getAbsolutePath(getCacheConfig()));
		sct.setEL(CACHE_PROVIDER, StringUtil.emptyIfNull(getCacheProvider()));
		sct.setEL(ORM_CONFIG, getAbsolutePath(getOrmConfig()));

		sct.setEL(CATALOG, externalize(catalogMap, catalogDefault));
		sct.setEL(SCHEMA, externalize(schemaMap, schemaDefault));
		sct.setEL(DB_CREATE, externalize(dbCreateMap, dbCreateDefault));
		sct.setEL(DIALECT, externalize(dialectMap, dialectDefault));
		sct.setEL(SQL_SCRIPT, externalize(sqlScriptMap, sqlScriptDefault));
		return sct;
	}

	private static String getAbsolutePath(Resource res) {
		if (res == null) return "";
		return res.getAbsolutePath();
	}

	public static int dbCreateAsInt(String dbCreate) {
		if (dbCreate == null) dbCreate = "";
		else dbCreate = dbCreate.trim().toLowerCase();

		if ("update".equals(dbCreate)) return DBCREATE_UPDATE;
		if ("dropcreate".equals(dbCreate)) return DBCREATE_DROP_CREATE;
		if ("drop-create".equals(dbCreate)) return DBCREATE_DROP_CREATE;
		return DBCREATE_NONE;
	}

	public static String dbCreateAsString(int dbCreate) {

		switch (dbCreate) {
		case DBCREATE_DROP_CREATE:
			return "dropcreate";
		case DBCREATE_UPDATE:
			return "update";
		}

		return "none";
	}

	private static Object externalize(Map<String, String> map, String def) {
		if (map == null || map.isEmpty()) return StringUtil.emptyIfNull(def);
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		Entry<String, String> e;
		Struct sct = new StructImpl();
		while (it.hasNext()) {
			e = it.next();
			if (!StringUtil.isEmpty(e.getValue())) sct.setEL(e.getKey(), e.getValue());
		}
		return sct;
	}
}

interface _Get {
	public Object get(Collection.Key name, Object defaultValue);
}

class _GetStruct implements _Get {

	private Struct sct;

	public _GetStruct(Struct sct) {
		this.sct = sct;
	}

	@Override
	public Object get(Collection.Key name, Object defaultValue) {
		return sct.get(name, defaultValue);
	}

	@Override
	public String toString() {
		return "_GetStruct:" + sct.toString();
	}
}

class _GetElement implements _Get {

	private Element el;

	public _GetElement(Element el) {
		this.el = el;
	}

	@Override
	public Object get(Collection.Key name, Object defaultValue) {
		String value = _get(name.getString());
		if (value == null) value = _get(StringUtil.camelToHypenNotation(name.getString()));
		if (value == null) value = _get(name.getLowerString());
		if (value == null) {
			NamedNodeMap map = el.getAttributes();
			int len = map.getLength();
			Attr attr;
			String n;
			for (int i = 0; i < len; i++) {
				attr = (Attr) map.item(i);
				n = attr.getName();
				n = StringUtil.replace(n, "-", "", false).toLowerCase();
				if (n.equalsIgnoreCase(name.getLowerString())) return attr.getValue();
			}

		}

		if (value == null) return defaultValue;
		return value;
	}

	private String _get(String name) {
		if (el.hasAttribute(name)) return el.getAttribute(name);
		return null;
	}
}

class Coll {
	Map<String, String> map;
	String def;
}
