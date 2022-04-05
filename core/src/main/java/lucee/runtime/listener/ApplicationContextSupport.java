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
package lucee.runtime.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Version;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.ftp.FTPConnectionData;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.regex.Regex;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;

/**
 * This is a base class for ModernApplicationSupport and ClassicApplicationSupport. It contains code
 * that is shared between the subclasses.
 */
public abstract class ApplicationContextSupport implements ApplicationContext {

	private static final long serialVersionUID = 1384678713928757744L;

	private static Map<Collection.Key, LoggerAndSourceData> _loggers = new ConcurrentHashMap<Collection.Key, LoggerAndSourceData>();

	protected int idletimeout = 1800;
	protected String cookiedomain;
	protected String applicationtoken;

	private Map<Collection.Key, Map<Collection.Key, Object>> tagDefaultAttributeValues = null;
	protected Object cachedWithinFunction;
	protected Object cachedWithinInclude;
	protected Object cachedWithinQuery;
	protected Object cachedWithinResource;
	protected Object cachedWithinHTTP;
	protected Object cachedWithinFile;
	protected Object cachedWithinWS;

	protected ConfigWeb config;

	public ApplicationContextSupport(ConfigWeb config) {
		this.config = config;
		tagDefaultAttributeValues = ((ConfigPro) config).getTagDefaultAttributeValues();

		cachedWithinFunction = config.getCachedWithin(Config.CACHEDWITHIN_FUNCTION);
		cachedWithinInclude = config.getCachedWithin(Config.CACHEDWITHIN_INCLUDE);
		cachedWithinQuery = config.getCachedWithin(Config.CACHEDWITHIN_QUERY);
		cachedWithinResource = config.getCachedWithin(Config.CACHEDWITHIN_RESOURCE);
		cachedWithinHTTP = config.getCachedWithin(Config.CACHEDWITHIN_HTTP);
		cachedWithinFile = config.getCachedWithin(Config.CACHEDWITHIN_FILE);
		cachedWithinWS = config.getCachedWithin(Config.CACHEDWITHIN_WEBSERVICE);
	}

	protected void _duplicate(ApplicationContextSupport other) {
		idletimeout = other.idletimeout;
		cookiedomain = other.cookiedomain;
		applicationtoken = other.applicationtoken;

		if (other.tagDefaultAttributeValues != null) {
			tagDefaultAttributeValues = new HashMap<Collection.Key, Map<Collection.Key, Object>>();
			Iterator<Entry<Collection.Key, Map<Collection.Key, Object>>> it = other.tagDefaultAttributeValues.entrySet().iterator();
			Entry<Collection.Key, Map<Collection.Key, Object>> e;
			Iterator<Entry<Collection.Key, Object>> iit;
			Entry<Collection.Key, Object> ee;
			Map<Collection.Key, Object> map;
			while (it.hasNext()) {
				e = it.next();
				iit = e.getValue().entrySet().iterator();
				map = new HashMap<Collection.Key, Object>();
				while (iit.hasNext()) {
					ee = iit.next();
					map.put(ee.getKey(), ee.getValue());
				}
				tagDefaultAttributeValues.put(e.getKey(), map);
			}
		}
		other.cachedWithinFile = Duplicator.duplicate(cachedWithinFile, true);
		other.cachedWithinFunction = Duplicator.duplicate(cachedWithinFunction, true);
		other.cachedWithinHTTP = Duplicator.duplicate(cachedWithinHTTP, true);
		other.cachedWithinInclude = Duplicator.duplicate(cachedWithinInclude, true);
		other.cachedWithinQuery = Duplicator.duplicate(cachedWithinQuery, true);
		other.cachedWithinResource = Duplicator.duplicate(cachedWithinResource, true);
		other.cachedWithinWS = Duplicator.duplicate(cachedWithinWS, true);
	}

	@Override
	public void setSecuritySettings(String applicationtoken, String cookiedomain, int idletimeout) {
		this.applicationtoken = applicationtoken;
		this.cookiedomain = cookiedomain;
		this.idletimeout = idletimeout;

	}

	@Override
	public String getSecurityApplicationToken() {
		if (StringUtil.isEmpty(applicationtoken, true)) return getName();
		return applicationtoken;
	}

	@Override
	public String getSecurityCookieDomain() {
		if (StringUtil.isEmpty(cookiedomain, true)) return null;
		return cookiedomain;
	}

	@Override
	public int getSecurityIdleTimeout() {
		if (idletimeout < 1) return 1800;
		return idletimeout;
	}

	@Override
	public DataSource getDataSource(String dataSourceName, DataSource defaultValue) {
		if (dataSourceName == null) return defaultValue;

		dataSourceName = dataSourceName.trim();
		DataSource[] sources = getDataSources();
		if (!ArrayUtil.isEmpty(sources)) {
			for (int i = 0; i < sources.length; i++) {
				if (sources[i].getName().equalsIgnoreCase(dataSourceName)) return sources[i];
			}
		}
		return defaultValue;
	}

	@Override
	public DataSource getDataSource(String dataSourceName) throws ApplicationException {
		DataSource source = getDataSource(dataSourceName, null);
		if (source == null) throw new ApplicationException("there is no datasource with name [" + dataSourceName + "]");
		return source;
	}

	@Override
	public Map<Collection.Key, Map<Collection.Key, Object>> getTagAttributeDefaultValues(PageContext pc) {
		return tagDefaultAttributeValues;
	}

	@Override
	public Map<Collection.Key, Object> getTagAttributeDefaultValues(PageContext pc, String fullname) {
		if (tagDefaultAttributeValues == null) return null;
		return tagDefaultAttributeValues.get(KeyImpl.init(fullname));
	}

	@Override
	public void setTagAttributeDefaultValues(PageContext pc, Struct sct) {
		if (tagDefaultAttributeValues == null) tagDefaultAttributeValues = new HashMap<Collection.Key, Map<Collection.Key, Object>>();
		initTagDefaultAttributeValues(config, tagDefaultAttributeValues, sct, pc.getCurrentTemplateDialect());
	}

	public static void initTagDefaultAttributeValues(Config config, Map<Collection.Key, Map<Collection.Key, Object>> tagDefaultAttributeValues, Struct sct, int dialect) {
		if (sct.size() == 0) return;
		ConfigPro ci = ((ConfigPro) config);

		// first check the core lib without namespace
		TagLib lib = ci.getCoreTagLib(dialect);
		_initTagDefaultAttributeValues(config, lib, tagDefaultAttributeValues, sct, false);
		if (sct.size() == 0) return;

		// then all the other libs including the namespace
		TagLib[] tlds = ci.getTLDs(dialect);
		for (int i = 0; i < tlds.length; i++) {
			_initTagDefaultAttributeValues(config, tlds[i], tagDefaultAttributeValues, sct, true);
			if (sct.size() == 0) return;
		}
	}

	private static void _initTagDefaultAttributeValues(Config config, TagLib lib, Map<Collection.Key, Map<Collection.Key, Object>> tagDefaultAttributeValues, Struct sct,
			boolean checkNameSpace) {
		if (sct == null) return;
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		// loop tags
		Struct attrs;
		TagLibTag tag;
		Iterator<Entry<Key, Object>> iit;
		Entry<Key, Object> e;
		Map<Collection.Key, Object> map;
		TagLibTagAttr attr;
		String name;
		while (it.hasNext()) {
			e = it.next();
			attrs = Caster.toStruct(e.getValue(), null);
			if (attrs != null) {
				tag = null;
				if (checkNameSpace) {
					name = e.getKey().getLowerString();
					if (StringUtil.startsWithIgnoreCase(name, lib.getNameSpaceAndSeparator())) {
						name = name.substring(lib.getNameSpaceAndSeparator().length());
						tag = lib.getTag(name);
					}
				}
				else tag = lib.getTag(e.getKey().getLowerString());

				if (tag != null) {
					sct.removeEL(e.getKey());
					map = new HashMap<Collection.Key, Object>();
					iit = attrs.entryIterator();
					while (iit.hasNext()) {
						e = iit.next();
						map.put(KeyImpl.init(e.getKey().getLowerString()), e.getValue());
					}
					tagDefaultAttributeValues.put(KeyImpl.init(tag.getFullName()), map);
				}
			}
		}
	}

	@Override
	public final void setCachedWithin(int type, Object value) {
		if (StringUtil.isEmpty(value)) return;

		switch (type) {
		case Config.CACHEDWITHIN_FUNCTION:
			cachedWithinFunction = value;
			break;
		case Config.CACHEDWITHIN_INCLUDE:
			cachedWithinInclude = value;
			break;
		case Config.CACHEDWITHIN_QUERY:
			cachedWithinQuery = value;
			break;
		case Config.CACHEDWITHIN_RESOURCE:
			cachedWithinResource = value;
			break;
		case Config.CACHEDWITHIN_HTTP:
			cachedWithinHTTP = value;
			break;
		case Config.CACHEDWITHIN_FILE:
			cachedWithinFile = value;
			break;
		case Config.CACHEDWITHIN_WEBSERVICE:
			cachedWithinWS = value;
			break;
		}
	}

	@Override
	public Object getCachedWithin(int type) {

		switch (type) {
		case Config.CACHEDWITHIN_FUNCTION:
			return cachedWithinFunction;
		case Config.CACHEDWITHIN_INCLUDE:
			return cachedWithinInclude;
		case Config.CACHEDWITHIN_QUERY:
			return cachedWithinQuery;
		case Config.CACHEDWITHIN_RESOURCE:
			return cachedWithinResource;
		case Config.CACHEDWITHIN_HTTP:
			return cachedWithinHTTP;
		case Config.CACHEDWITHIN_FILE:
			return cachedWithinFile;
		case Config.CACHEDWITHIN_WEBSERVICE:
			return cachedWithinWS;
		}
		return null;
	}

	public static Map<Collection.Key, Pair<Log, Struct>> initLog(Config config, Struct sct) throws PageException {
		Map<Collection.Key, Pair<Log, Struct>> rtn = new ConcurrentHashMap<Collection.Key, Pair<Log, Struct>>();
		if (sct == null) return rtn;

		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		Struct v;
		int k;
		Collection.Key name;
		LoggerAndSourceData las;
		while (it.hasNext()) {
			e = it.next();
			name = e.getKey();
			v = Caster.toStruct(e.getValue(), null);
			if (v == null) continue;

			// appender
			ClassDefinition cdApp;
			Struct sctApp = Caster.toStruct(v.get("appender", null), null);
			String ac = AppListenerUtil.toClassName(sctApp);
			String abn = AppListenerUtil.toBundleName(sctApp);
			Version abv = AppListenerUtil.toBundleVersion(sctApp);
			if (StringUtil.isEmpty(abn)) cdApp = ((ConfigPro) config).getLogEngine().appenderClassDefintion(ac);
			else cdApp = new ClassDefinitionImpl<>(config.getIdentification(), ac, abn, abv);

			// layout
			ClassDefinition cdLay;
			Struct sctLay = Caster.toStruct(v.get("layout", null), null);
			String lc = AppListenerUtil.toClassName(sctLay);
			String lbn = AppListenerUtil.toBundleName(sctLay);
			Version lbv = AppListenerUtil.toBundleVersion(sctLay);
			if (StringUtil.isEmpty(lbn)) cdLay = ((ConfigPro) config).getLogEngine().layoutClassDefintion(lc);
			else cdLay = new ClassDefinitionImpl<>(config.getIdentification(), lc, lbn, lbv);

			if (cdApp != null && cdApp.hasClass()) {
				// level

				String strLevel = Caster.toString(v.get("level", null), null);
				if (StringUtil.isEmpty(strLevel, true)) Caster.toString(v.get("loglevel", null), null);
				int level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);

				Struct sctAppArgs = Caster.toStruct(sctApp.get("arguments", null), null);
				Struct sctLayArgs = Caster.toStruct(sctLay.get("arguments", null), null);

				boolean readOnly = Caster.toBooleanValue(v.get("readonly", null), false);

				// ignore when no appender/name is defined
				if (!StringUtil.isEmpty(name)) {
					Map<String, String> appArgs = toMap(sctAppArgs);
					if (cdLay != null && cdLay.hasClass()) {
						Map<String, String> layArgs = toMap(sctLayArgs);
						las = addLogger(name, level, cdApp, appArgs, cdLay, layArgs, readOnly);
					}
					else las = addLogger(name, level, cdApp, appArgs, null, null, readOnly);
					rtn.put(name, new Pair<Log, Struct>(las.getLog(false), v));
				}
			}
		}
		return rtn;
	}

	private static Map<String, String> toMap(Struct sct) {
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Map<String, String> map = new HashMap<String, String>();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey().getLowerString(), Caster.toString(e.getValue(), null));
		}
		return map;
	}

	private static LoggerAndSourceData addLogger(Collection.Key name, int level, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
			Map<String, String> layoutArgs, boolean readOnly) throws PageException {
		LoggerAndSourceData existing = _loggers.get(name);
		String id = LoggerAndSourceData.id(name.getLowerString(), appender, appenderArgs, layout, layoutArgs, level, readOnly);

		if (existing != null) {
			if (existing.id().equals(id)) {
				return existing;
			}
			existing.close();
		}

		LoggerAndSourceData las = new LoggerAndSourceData(null, id, name.getLowerString(), appender, appenderArgs, layout, layoutArgs, level, readOnly, true);
		_loggers.put(name, las);
		return las;
	}

	// FUTURE add to interface
	public abstract Resource getAntiSamyPolicyResource();

	public abstract void setAntiSamyPolicyResource(Resource res);

	public abstract CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue);

	public abstract Key[] getCacheConnectionNames();

	public abstract void setCacheConnection(String cacheName, CacheConnection value);

	public abstract SessionCookieData getSessionCookie();

	public abstract void setSessionCookie(SessionCookieData data);

	public abstract AuthCookieData getAuthCookie();

	public abstract void setAuthCookie(AuthCookieData data);

	public abstract lucee.runtime.net.mail.Server[] getMailServers();

	public abstract void setMailServers(lucee.runtime.net.mail.Server[] servers);

	public abstract void setLoggers(Map<Key, Pair<Log, Struct>> logs);

	public abstract java.util.Collection<Collection.Key> getLogNames();

	public abstract Log getLog(String name);

	public abstract Struct getLogMetaData(String string);

	public abstract Object getMailListener();

	public abstract void setMailListener(Object mailListener);

	public abstract boolean getWSMaintainSession(); // used in extension Axis1

	public abstract void setWSMaintainSession(boolean maintainSession);

	public abstract FTPConnectionData getFTP();

	public abstract void setFTP(FTPConnectionData ftp);

	public abstract boolean getFullNullSupport();

	public abstract void setFullNullSupport(boolean fullNullSupport);

	public abstract TagListener getQueryListener();

	public abstract void setQueryListener(TagListener listener);

	public abstract SerializationSettings getSerializationSettings();

	public abstract void setSerializationSettings(SerializationSettings settings);

	public abstract List<Resource> getFunctionDirectories();

	public abstract void setFunctionDirectories(List<Resource> resources);

	public abstract boolean getQueryPSQ();

	public abstract void setQueryPSQ(boolean psq);

	public abstract int getQueryVarUsage();

	public abstract void setQueryVarUsage(int varUsage);

	public abstract TimeSpan getQueryCachedAfter();

	public abstract void setQueryCachedAfter(TimeSpan ts);

	public abstract ProxyData getProxyData();

	public abstract void setProxyData(ProxyData data);

	public abstract String getBlockedExtForFileUpload();

	public abstract void setJavaSettings(JavaSettings javaSettings);

	public abstract Struct getXmlFeatures();

	public abstract void setXmlFeatures(Struct xmlFeatures);

	public abstract boolean getAllowImplicidQueryCall();

	public abstract void setAllowImplicidQueryCall(boolean allowImplicidQueryCall);

	public abstract Regex getRegex();

	public abstract void setRegex(Regex regex);

}