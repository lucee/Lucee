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
package lucee.runtime.functions.system;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.DataSourcePro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.ClassicApplicationContext;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.ModernApplicationContext;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class GetApplicationSettings extends BIF {

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Struct call(PageContext pc, boolean suppressFunctions) throws PageException {
		ApplicationContext ac = pc.getApplicationContext();
		ApplicationContextSupport acs = (ApplicationContextSupport) ac;
		Component cfc = null;
		if (ac instanceof ModernApplicationContext) cfc = ((ModernApplicationContext) ac).getComponent();

		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL("applicationTimeout", ac.getApplicationTimeout());
		sct.setEL("blockedExtForFileUpload", acs.getBlockedExtForFileUpload());
		sct.setEL("clientManagement", Caster.toBoolean(ac.isSetClientManagement()));
		sct.setEL("clientStorage", ac.getClientstorage());
		sct.setEL("sessionStorage", ac.getSessionstorage());
		sct.setEL("customTagPaths", toArray(ac.getCustomTagMappings()));
		sct.setEL("componentPaths", toArray(ac.getComponentMappings()));
		sct.setEL("loginStorage", AppListenerUtil.translateLoginStorage(ac.getLoginStorage()));
		sct.setEL(KeyConstants._mappings, toStruct(ac.getMappings()));
		sct.setEL(KeyConstants._name, ac.getName());
		sct.setEL("scriptProtect", AppListenerUtil.translateScriptProtect(ac.getScriptProtect()));
		sct.setEL("secureJson", Caster.toBoolean(ac.getSecureJson()));
		sct.setEL("CGIReadOnly", Caster.toBoolean(ac.getCGIScopeReadonly()));
		sct.setEL("typeChecking", Caster.toBoolean(ac.getTypeChecking()));
		sct.setEL("secureJsonPrefix", ac.getSecureJsonPrefix());
		sct.setEL("sessionManagement", Caster.toBoolean(ac.isSetSessionManagement()));
		sct.setEL("sessionTimeout", ac.getSessionTimeout());
		sct.setEL("clientTimeout", ac.getClientTimeout());
		sct.setEL("setClientCookies", Caster.toBoolean(ac.isSetClientCookies()));
		sct.setEL("setDomainCookies", Caster.toBoolean(ac.isSetDomainCookies()));
		sct.setEL(KeyConstants._name, ac.getName());
		sct.setEL("localMode", ac.getLocalMode() == Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS ? Boolean.TRUE : Boolean.FALSE);
		sct.setEL(KeyConstants._locale, LocaleFactory.toString(pc.getLocale()));
		sct.setEL(KeyConstants._timezone, TimeZoneUtil.toString(pc.getTimeZone()));
		// sct.setEL(KeyConstants._timeout,TimeZoneUtil.toString(pc.getRequestTimeout()));

		sct.setEL("nullSupport", ((ApplicationContextSupport) ac).getFullNullSupport());
		sct.setEL("enableNullSupport", ((ApplicationContextSupport) ac).getFullNullSupport());

		// scope cascading
		sct.setEL("scopeCascading", ConfigWebUtil.toScopeCascading(ac.getScopeCascading(), null));

		if (ac.getScopeCascading() != Config.SCOPE_SMALL) {
			sct.setEL("searchImplicitScopes", ac.getScopeCascading() == Config.SCOPE_STANDARD);
		}

		Struct cs = new StructImpl(Struct.TYPE_LINKED);
		cs.setEL("web", pc.getWebCharset().name());
		cs.setEL("resource", ((PageContextImpl) pc).getResourceCharset().name());
		sct.setEL("charset", cs);

		sct.setEL("sessionType", AppListenerUtil.toSessionType(((PageContextImpl) pc).getSessionType(), "application"));
		sct.setEL("serverSideFormValidation", Boolean.FALSE); // TODO impl

		sct.setEL("clientCluster", Caster.toBoolean(ac.getClientCluster()));
		sct.setEL("sessionCluster", Caster.toBoolean(ac.getSessionCluster()));

		sct.setEL("invokeImplicitAccessor", Caster.toBoolean(ac.getTriggerComponentDataMember()));
		sct.setEL("triggerDataMember", Caster.toBoolean(ac.getTriggerComponentDataMember()));
		sct.setEL("sameformfieldsasarray", Caster.toBoolean(ac.getSameFieldAsArray(Scope.SCOPE_FORM)));
		sct.setEL("sameurlfieldsasarray", Caster.toBoolean(ac.getSameFieldAsArray(Scope.SCOPE_URL)));

		Object ds = ac.getDefDataSource();
		if (ds instanceof DataSource) ds = _call((DataSource) ds);
		else ds = Caster.toString(ds, null);
		sct.setEL(KeyConstants._datasource, ds);
		sct.setEL("defaultDatasource", ds);

		Resource src = ac.getSource();
		if (src != null) sct.setEL(KeyConstants._source, src.getAbsolutePath());

		// orm
		if (ac.isORMEnabled()) {
			ORMConfiguration conf = ac.getORMConfiguration();
			if (conf != null) sct.setEL(KeyConstants._orm, conf.toStruct());
		}
		// s3
		Properties props = ac.getS3();
		if (props != null) {
			sct.setEL(KeyConstants._s3, props.toStruct());
		}

		// ws settings
		try {
			Struct wssettings = new StructImpl(Struct.TYPE_LINKED);
			wssettings.setEL(KeyConstants._type, AppListenerUtil.toWSType(ac.getWSType(), ((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getTypeAsString()));
			sct.setEL("wssettings", wssettings);
		}
		catch (Exception e) {} // in case the extension is not loaded this will fail // TODO check if the extension is installed
		// query
		{
			Struct query = new StructImpl(Struct.TYPE_LINKED);
			query.setEL("varusage", AppListenerUtil.toVariableUsage(acs.getQueryVarUsage(), "ignore"));
			query.setEL("psq", acs.getQueryPSQ());
			sct.setEL("query", query);
		}

		// datasources
		Struct _sources = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._datasources, _sources);
		DataSource[] sources = ac.getDataSources();
		if (!ArrayUtil.isEmpty(sources)) {
			for (int i = 0; i < sources.length; i++) {
				_sources.setEL(KeyImpl.init(sources[i].getName()), _call(sources[i]));
			}

		}

		// logs
		Struct _logs = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL("logs", _logs);
		if (ac instanceof ApplicationContextSupport) {
			Iterator<Key> it = acs.getLogNames().iterator();
			Key name;
			while (it.hasNext()) {
				name = it.next();
				_logs.setEL(name, acs.getLogMetaData(name.getString()));
			}
		}

		// mails
		Array _mails = new ArrayImpl();
		sct.setEL("mails", _mails);
		if (ac instanceof ApplicationContextSupport) {
			Server[] servers = acs.getMailServers();
			Struct s;
			Server srv;
			if (servers != null) {
				for (int i = 0; i < servers.length; i++) {
					srv = servers[i];
					s = new StructImpl(Struct.TYPE_LINKED);
					_mails.appendEL(s);
					s.setEL(KeyConstants._host, srv.getHostName());
					s.setEL(KeyConstants._port, srv.getPort());
					if (!StringUtil.isEmpty(srv.getUsername())) s.setEL(KeyConstants._username, srv.getUsername());
					if (!StringUtil.isEmpty(srv.getPassword())) s.setEL(KeyConstants._password, srv.getPassword());
					s.setEL(KeyConstants._readonly, srv.isReadOnly());
					s.setEL("ssl", srv.isSSL());
					s.setEL("tls", srv.isTLS());

					if (srv instanceof ServerImpl) {
						ServerImpl srvi = (ServerImpl) srv;
						s.setEL("lifeTimespan", TimeSpanImpl.fromMillis(srvi.getLifeTimeSpan()));
						s.setEL("idleTimespan", TimeSpanImpl.fromMillis(srvi.getIdleTimeSpan()));
					}
				}
			}
		}

		// serialization
		if (ac instanceof ApplicationContextSupport) {
			Struct ser = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL("serialization", acs.getSerializationSettings().toStruct());
		}

		// tag
		Map<Key, Map<Collection.Key, Object>> tags = ac.getTagAttributeDefaultValues(pc);
		if (tags != null) {
			Struct tag = new StructImpl(Struct.TYPE_LINKED);
			Iterator<Entry<Key, Map<Collection.Key, Object>>> it = tags.entrySet().iterator();
			Entry<Collection.Key, Map<Collection.Key, Object>> e;
			Iterator<Entry<Collection.Key, Object>> iit;
			Entry<Collection.Key, Object> ee;
			Struct tmp;
			while (it.hasNext()) {
				e = it.next();
				iit = e.getValue().entrySet().iterator();
				tmp = new StructImpl(Struct.TYPE_LINKED);
				while (iit.hasNext()) {
					ee = iit.next();
					// lib.getTagByClassName(ee.getKey());
					tmp.setEL(ee.getKey(), ee.getValue());
				}
				tag.setEL(e.getKey(), tmp);

			}
			sct.setEL(KeyConstants._tag, tag);
		}

		// cache
		String fun = ac.getDefaultCacheName(Config.CACHE_TYPE_FUNCTION);
		String obj = ac.getDefaultCacheName(Config.CACHE_TYPE_OBJECT);
		String qry = ac.getDefaultCacheName(Config.CACHE_TYPE_QUERY);
		String res = ac.getDefaultCacheName(Config.CACHE_TYPE_RESOURCE);
		String tmp = ac.getDefaultCacheName(Config.CACHE_TYPE_TEMPLATE);
		String inc = ac.getDefaultCacheName(Config.CACHE_TYPE_INCLUDE);
		String htt = ac.getDefaultCacheName(Config.CACHE_TYPE_HTTP);
		String fil = ac.getDefaultCacheName(Config.CACHE_TYPE_FILE);
		String wse = ac.getDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE);

		// cache connections
		Struct conns = new StructImpl(Struct.TYPE_LINKED);
		if (ac instanceof ApplicationContextSupport) {
			Key[] names = acs.getCacheConnectionNames();
			for (Key name: names) {
				CacheConnection data = acs.getCacheConnection(name.getString(), null);
				Struct _sct = new StructImpl(Struct.TYPE_LINKED);
				conns.setEL(name, _sct);
				_sct.setEL(KeyConstants._custom, data.getCustom());
				_sct.setEL(KeyConstants._storage, data.isStorage());
				ClassDefinition cd = data.getClassDefinition();
				if (cd != null) {
					_sct.setEL(KeyConstants._class, cd.getClassName());
					if (!StringUtil.isEmpty(cd.getName())) _sct.setEL(KeyConstants._bundleName, cd.getClassName());
					if (cd.getVersion() != null) _sct.setEL(KeyConstants._bundleVersion, cd.getVersionAsString());
				}
			}
		}

		if (!conns.isEmpty() || fun != null || obj != null || qry != null || res != null || tmp != null || inc != null || htt != null || fil != null || wse != null) {
			Struct cache = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._cache, cache);
			if (fun != null) cache.setEL(KeyConstants._function, fun);
			if (obj != null) cache.setEL(KeyConstants._object, obj);
			if (qry != null) cache.setEL(KeyConstants._query, qry);
			if (res != null) cache.setEL(KeyConstants._resource, res);
			if (tmp != null) cache.setEL(KeyConstants._template, tmp);
			if (inc != null) cache.setEL(KeyConstants._include, inc);
			if (htt != null) cache.setEL(KeyConstants._http, htt);
			if (fil != null) cache.setEL(KeyConstants._file, fil);
			if (wse != null) cache.setEL(KeyConstants._webservice, wse);
			if (conns != null) cache.setEL(KeyConstants._connections, conns);
		}

		// java settings
		JavaSettings js = ac.getJavaSettings();
		StructImpl jsSct = new StructImpl(Struct.TYPE_LINKED);
		jsSct.put("loadCFMLClassPath", js.loadCFMLClassPath());
		jsSct.put("reloadOnChange", js.reloadOnChange());
		jsSct.put("watchInterval", new Double(js.watchInterval()));
		jsSct.put("watchExtensions", ListUtil.arrayToList(js.watchedExtensions(), ","));
		Resource[] reses = js.getResources();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < reses.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(reses[i].getAbsolutePath());
		}
		jsSct.put("loadCFMLClassPath", sb.toString());
		sct.put("javaSettings", jsSct);
		// REST Settings
		// MUST

		if (cfc != null) {
			sct.setEL(KeyConstants._component, cfc.getPageSource().getDisplayPath());

			try {
				ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, cfc);
				Iterator<Key> it = cw.keyIterator();
				Collection.Key key;
				Object value;
				while (it.hasNext()) {
					key = it.next();
					value = cw.get(key);
					if (suppressFunctions && value instanceof UDF) continue;
					if (!sct.containsKey(key)) sct.setEL(key, value);
				}
			}
			catch (PageException e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(pc), GetApplicationSettings.class.getName(), e);
			}
		}
		// application tag custom attributes
		if (ac instanceof ClassicApplicationContext) {
			Map<Key, Object> attrs = ((ClassicApplicationContext) ac).getCustomAttributes();
			if (attrs != null) {
				Iterator<Entry<Key, Object>> it = attrs.entrySet().iterator();
				Entry<Key, Object> e;
				while (it.hasNext()) {
					e = it.next();
					if (suppressFunctions && e.getValue() instanceof UDF) continue;
					if (!sct.containsKey(e.getKey())) sct.setEL(e.getKey(), e.getValue());
				}
			}
		}
		return sct;
	}

	private static Struct _call(DataSource source) {
		Struct s = new StructImpl(Struct.TYPE_LINKED);
		s.setEL(KeyConstants._class, source.getClassDefinition().getClassName());
		s.setEL(KeyConstants._bundleName, source.getClassDefinition().getName());
		s.setEL(KeyConstants._bundleVersion, source.getClassDefinition().getVersionAsString());

		if (source.getConnectionLimit() >= 0) s.setEL(AppListenerUtil.CONNECTION_LIMIT, Caster.toDouble(source.getConnectionLimit()));
		if (source.getConnectionTimeout() != 1) s.setEL(AppListenerUtil.CONNECTION_TIMEOUT, Caster.toDouble(source.getConnectionTimeout()));

		s.setEL(AppListenerUtil.CONNECTION_STRING, source.getDsnTranslated());
		if (source.getMetaCacheTimeout() != 60000) s.setEL(AppListenerUtil.META_CACHE_TIMEOUT, Caster.toDouble(source.getMetaCacheTimeout()));
		s.setEL(KeyConstants._username, source.getUsername());
		s.setEL(KeyConstants._password, source.getPassword());
		if (source.getTimeZone() != null) s.setEL(KeyConstants._timezone, source.getTimeZone().getID());
		if (source.isBlob()) s.setEL(AppListenerUtil.BLOB, source.isBlob());
		if (source.isClob()) s.setEL(AppListenerUtil.CLOB, source.isClob());
		if (source.isReadOnly()) s.setEL(KeyConstants._readonly, source.isReadOnly());
		if (source.isStorage()) s.setEL(KeyConstants._storage, source.isStorage());
		s.setEL(KeyConstants._validate, source.validate());
		if (source instanceof DataSourcePro) {
			DataSourcePro dsp = (DataSourcePro) source;
			if (dsp.isRequestExclusive()) s.setEL("requestExclusive", dsp.isRequestExclusive());
			if (dsp.isRequestExclusive()) s.setEL("alwaysResetConnections", dsp.isAlwaysResetConnections());
			Object res = TagListener.toCFML(dsp.getListener(), null);
			if (res != null) s.setEL("listener", res);
			if (dsp.getLiveTimeout() != 1) s.setEL(AppListenerUtil.LIVE_TIMEOUT, Caster.toDouble(dsp.getLiveTimeout()));
		}
		if (source instanceof DataSourceImpl) {
			DataSourceImpl di = ((DataSourceImpl) source);
			s.setEL("literalTimestampWithTSOffset", Boolean.valueOf(di.getLiteralTimestampWithTSOffset()));
			s.setEL("alwaysSetTimeout", Boolean.valueOf(di.getAlwaysSetTimeout()));
			s.setEL("dbdriver", Caster.toString(di.getDbDriver(), ""));
		}
		return s;
	}

	private static Array toArray(Mapping[] mappings) {
		Array arr = new ArrayImpl();
		if (mappings != null) {
			String str;
			Struct sct;
			Mapping m;
			for (int i = 0; i < mappings.length; i++) {
				m = mappings[i];
				sct = new StructImpl();
				// physical
				str = m.getStrPhysical();
				if (!StringUtil.isEmpty(str, true)) sct.setEL("physical", str.trim());
				// archive
				str = m.getStrArchive();
				if (!StringUtil.isEmpty(str, true)) sct.setEL("archive", str.trim());
				// primary
				sct.setEL("primary", m.isPhysicalFirst() ? "physical" : "archive");

				arr.appendEL(sct);
			}
		}
		return arr;
	}

	private static Struct toStruct(Mapping[] mappings) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		if (mappings != null) for (int i = 0; i < mappings.length; i++) {
			sct.setEL(KeyImpl.init(mappings[i].getVirtual()), mappings[i].getStrPhysical());
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toBooleanValue(args[0]));
		if (args.length == 0) return call(pc);
		throw new FunctionException(pc, "GetApplicationSettings", 0, 1, args.length);
	}
}