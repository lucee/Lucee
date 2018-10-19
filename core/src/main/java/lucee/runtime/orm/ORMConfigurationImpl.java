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

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class ORMConfigurationImpl implements ORMConfiguration {
    public static final int DBCREATE_NONE = 0;
    public static final int DBCREATE_UPDATE = 1;
    public static final int DBCREATE_DROP_CREATE = 2;

    public static final Key AUTO_GEN_MAP = KeyImpl.init("autogenmap");
    public static final Key CATALOG = KeyImpl.init("catalog");
    public static final Key IS_DEFAULT_CFC_LOCATION = KeyImpl.init("isDefaultCfclocation");
    public static final Key DB_CREATE = KeyImpl.init("dbCreate");
    public static final Key DIALECT = KeyImpl.init("dialect");
    public static final Key FLUSH_AT_REQUEST_END = KeyImpl.init("flushAtRequestEnd");
    public static final Key LOG_SQL = KeyImpl.init("logSql");
    public static final Key SAVE_MAPPING = KeyImpl.init("savemapping");
    public static final Key SCHEMA = KeyImpl.init("schema");
    public static final Key SECONDARY_CACHE_ENABLED = KeyImpl.init("secondarycacheenabled");
    public static final Key SQL_SCRIPT = KeyImpl.init("sqlscript");
    public static final Key USE_DB_FOR_MAPPING = KeyImpl.init("useDBForMapping");
    public static final Key CACHE_CONFIG = KeyImpl.init("cacheconfig");
    public static final Key CACHE_PROVIDER = KeyImpl.init("cacheProvider");
    public static final Key ORM_CONFIG = KeyImpl.init("ormConfig");
    public static final Key EVENT_HANDLING = KeyImpl.init("eventHandling");
    public static final Key EVENT_HANDLER = KeyImpl.init("eventHandler");
    public static final Key AUTO_MANAGE_SESSION = KeyImpl.init("autoManageSession");
    public static final Key NAMING_STRATEGY = KeyImpl.init("namingstrategy");
    public static final Key CFC_LOCATION = KeyImpl.init("cfcLocation");

    private boolean autogenmap = true;
    private String catalog;
    private Resource[] cfcLocations;
    private int dbCreate = DBCREATE_NONE;
    private String dialect;
    private Boolean eventHandling = null;
    private boolean flushAtRequestEnd = true;
    private boolean logSQL;
    private boolean saveMapping;
    private String schema;
    private boolean secondaryCacheEnabled;
    private Resource sqlScript;
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

    private ORMConfigurationImpl() {
	autogenmap = true;
	dbCreate = DBCREATE_NONE;
	flushAtRequestEnd = true;
	useDBForMapping = true;
    }

    public static ORMConfiguration load(Config config, ApplicationContext ac, Element el, Resource defaultCFCLocation, ORMConfiguration defaultConfig) {
	return _load(config, ac, new _GetElement(el), defaultCFCLocation, defaultConfig);
    }

    public static ORMConfiguration load(Config config, ApplicationContext ac, Struct settings, Resource defaultCFCLocation, ORMConfiguration defaultConfig) {
	return _load(config, ac, new _GetStruct(settings), defaultCFCLocation, defaultConfig);
    }

    private static ORMConfiguration _load(Config config, ApplicationContext ac, _Get settings, Resource defaultCFCLocation, ORMConfiguration dc) {

	if (dc == null) dc = new ORMConfigurationImpl();
	ORMConfigurationImpl c = ((ORMConfigurationImpl) dc).duplicate();
	c.cfcLocations = defaultCFCLocation == null ? new Resource[0] : new Resource[] { defaultCFCLocation };

	// autogenmap
	c.autogenmap = Caster.toBooleanValue(settings.get(AUTO_GEN_MAP, dc.autogenmap()), dc.autogenmap());

	// catalog
	c.catalog = StringUtil.trim(Caster.toString(settings.get(CATALOG, dc.getCatalog()), dc.getCatalog()), dc.getCatalog());

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

	// dbcreate
	obj = settings.get(DB_CREATE, null);
	if (obj != null) {
	    String str = Caster.toString(obj, "").trim().toLowerCase();
	    c.dbCreate = dbCreateAsInt(str);
	}

	// dialect
	c.dialect = StringUtil.trim(Caster.toString(settings.get(DIALECT, dc.getDialect()), dc.getDialect()), dc.getDialect());

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
	c.schema = StringUtil.trim(Caster.toString(settings.get(SCHEMA, dc.getSchema()), dc.getSchema()), dc.getSchema());

	// secondarycacheenabled
	c.secondaryCacheEnabled = Caster.toBooleanValue(settings.get(SECONDARY_CACHE_ENABLED, dc.secondaryCacheEnabled()), dc.secondaryCacheEnabled());

	// sqlscript
	obj = settings.get(SQL_SCRIPT, null);
	if (!StringUtil.isEmpty(obj)) {
	    try {
		c.sqlScript = toRes(config, obj, true);
	    }
	    catch (ExpressionException e) {
		// print.e(e);
	    }
	}

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

    private static Resource toRes(Config config, Object obj, boolean existing) throws ExpressionException {
	PageContext pc = ThreadLocalPageContext.get();
	if (pc != null) return Caster.toResource(pc, obj, existing);
	return Caster.toResource(config, obj, existing);
    }

    private ORMConfigurationImpl duplicate() {
	ORMConfigurationImpl other = new ORMConfigurationImpl();
	other.autogenmap = autogenmap;
	other.catalog = catalog;
	other.cfcLocations = cfcLocations;
	other.isDefaultCfcLocation = isDefaultCfcLocation;
	other.dbCreate = dbCreate;
	other.dialect = dialect;
	other.eventHandler = eventHandler;
	other.namingStrategy = namingStrategy;
	other.eventHandling = eventHandling;
	other.flushAtRequestEnd = flushAtRequestEnd;
	other.logSQL = logSQL;
	other.saveMapping = saveMapping;
	other.schema = schema;
	other.secondaryCacheEnabled = secondaryCacheEnabled;
	other.sqlScript = sqlScript;
	other.useDBForMapping = useDBForMapping;
	other.cacheConfig = cacheConfig;
	other.cacheProvider = cacheProvider;
	other.ormConfig = ormConfig;
	other.autoManageSession = autoManageSession;
	other.skipCFCWithError = skipCFCWithError;
	return other;
    }

    @Override
    public String hash() { // no longer used in Hibernate 3.5.5.72 and above
	ApplicationContext _ac = ac;
	if (_ac == null) _ac = ThreadLocalPageContext.get().getApplicationContext();
	Object ds = _ac.getORMDataSource();
	ORMConfiguration ormConf = _ac.getORMConfiguration();

	StringBuilder data = new StringBuilder().append(ormConf.autogenmap()).append(':').append(ormConf.getCatalog()).append(':').append(ormConf.isDefaultCfcLocation())
		.append(':').append(ormConf.getDbCreate()).append(':').append(ormConf.getDialect()).append(':').append(ormConf.eventHandling()).append(':')
		.append(ormConf.namingStrategy()).append(':').append(ormConf.eventHandler()).append(':').append(ormConf.flushAtRequestEnd()).append(':').append(ormConf.logSQL())
		.append(':').append(ormConf.autoManageSession()).append(':').append(ormConf.skipCFCWithError()).append(':').append(ormConf.saveMapping()).append(':')
		.append(ormConf.getSchema()).append(':').append(ormConf.secondaryCacheEnabled()).append(':').append(ormConf.useDBForMapping()).append(':')
		.append(ormConf.getCacheProvider()).append(':').append(ds).append(':');

	append(data, ormConf.getCfcLocations());
	append(data, ormConf.getSqlScript());
	append(data, ormConf.getCacheConfig());
	append(data, ormConf.getOrmConfig());

	return CFMLEngineFactory.getInstance().getSystemUtil().hash64b(data.toString());
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
	    catch (IOException e) {}
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
     * @return the catalog
     */
    @Override
    public String getCatalog() {
	return catalog;
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

    /**
     * @return the dbCreate
     */
    @Override
    public int getDbCreate() {
	return dbCreate;
    }

    /**
     * @return the dialect
     */
    @Override
    public String getDialect() {
	return dialect;
    }

    /**
     * @return the eventHandling
     */
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

    /**
     * @return the flushAtRequestEnd
     */
    @Override
    public boolean flushAtRequestEnd() {
	return flushAtRequestEnd;
    }

    /**
     * @return the logSQL
     */
    @Override
    public boolean logSQL() {
	return logSQL;
    }

    /**
     * @return the saveMapping
     */
    @Override
    public boolean saveMapping() {
	return saveMapping;
    }

    /**
     * @return the schema
     */
    @Override
    public String getSchema() {
	return schema;
    }

    /**
     * @return the secondaryCacheEnabled
     */
    @Override
    public boolean secondaryCacheEnabled() {
	return secondaryCacheEnabled;
    }

    /**
     * @return the sqlScript
     */
    @Override
    public Resource getSqlScript() {
	return sqlScript;
    }

    /**
     * @return the useDBForMapping
     */
    @Override
    public boolean useDBForMapping() {
	return useDBForMapping;
    }

    /**
     * @return the cacheConfig
     */
    @Override
    public Resource getCacheConfig() {
	return cacheConfig;
    }

    /**
     * @return the cacheProvider
     */
    @Override
    public String getCacheProvider() {
	return cacheProvider;
    }

    /**
     * @return the ormConfig
     */
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
	sct.setEL(CATALOG, StringUtil.emptyIfNull(getCatalog()));
	sct.setEL(CFC_LOCATION, arrLocs);
	sct.setEL(IS_DEFAULT_CFC_LOCATION, isDefaultCfcLocation());
	sct.setEL(DB_CREATE, dbCreateAsString(getDbCreate()));
	sct.setEL(DIALECT, StringUtil.emptyIfNull(getDialect()));
	sct.setEL(EVENT_HANDLING, eventHandling());
	sct.setEL(EVENT_HANDLER, eventHandler());
	sct.setEL(NAMING_STRATEGY, namingStrategy());
	sct.setEL(FLUSH_AT_REQUEST_END, flushAtRequestEnd());
	sct.setEL(LOG_SQL, logSQL());
	sct.setEL(SAVE_MAPPING, saveMapping());
	sct.setEL(SCHEMA, StringUtil.emptyIfNull(getSchema()));
	sct.setEL(SECONDARY_CACHE_ENABLED, secondaryCacheEnabled());
	sct.setEL(SQL_SCRIPT, StringUtil.toStringEmptyIfNull(getSqlScript()));
	sct.setEL(USE_DB_FOR_MAPPING, useDBForMapping());
	sct.setEL(CACHE_CONFIG, getAbsolutePath(getCacheConfig()));
	sct.setEL(CACHE_PROVIDER, StringUtil.emptyIfNull(getCacheProvider()));
	sct.setEL(ORM_CONFIG, getAbsolutePath(getOrmConfig()));
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