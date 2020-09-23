/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.functions.system;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.SizeAndCount;
import lucee.commons.lang.SizeAndCount.Size;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.debug.ActiveLock;
import lucee.runtime.debug.ActiveQuery;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.lock.LockManager;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.util.KeyConstants;

public final class GetUsageData implements Function {

	private static final Key START_TIME = KeyImpl.getInstance("starttime");
	private static final Key CACHED_QUERIES = KeyImpl.getInstance("cachedqueries");
	private static final Key OPEN_CONNECTIONS = KeyImpl.getInstance("openconnections");
	private static final Key ELEMENTS = KeyImpl.getInstance("elements");
	private static final Key USERS = KeyImpl.getInstance("users");
	private static final Key QUERIES = KeyImpl.getInstance("queries");
	private static final Key LOCKS = KeyImpl.getInstance("locks");

	public static Struct call(PageContext pc) throws PageException {
		ConfigWeb cw = pc.getConfig();
		ConfigServer cs = cw.getConfigServer("server");
		ConfigWeb[] webs = cs.getConfigWebs();
		CFMLEngineFactory.getInstance();
		CFMLEngineImpl engine = (CFMLEngineImpl) cs.getCFMLEngine();

		Struct sct = new StructImpl();

		// Locks
		/*
		 * LockManager manager = pc.getConfig().getLockManager(); String[] locks =
		 * manager.getOpenLockNames(); for(int i=0;i<locks.length;i++){ locks[i]. }
		 * if(!ArrayUtil.isEmpty(locks)) strLocks=" open locks at this time ("+List.arrayToList(locks,
		 * ", ")+").";
		 */

		// Requests
		Query req = new QueryImpl(new Collection.Key[] { KeyConstants._web, KeyConstants._uri, START_TIME, KeyConstants._timeout }, 0, "requests");
		sct.setEL(KeyConstants._requests, req);

		// Template Cache
		Query tc = new QueryImpl(new Collection.Key[] { KeyConstants._web, ELEMENTS, KeyConstants._size }, 0, "templateCache");
		sct.setEL(KeyImpl.getInstance("templateCache"), tc);

		// Scopes
		Struct scopes = new StructImpl();
		sct.setEL(KeyConstants._scopes, scopes);
		Query app = new QueryImpl(new Collection.Key[] { KeyConstants._web, KeyConstants._application, ELEMENTS, KeyConstants._size }, 0, "templateCache");
		scopes.setEL(KeyConstants._application, app);
		Query sess = new QueryImpl(new Collection.Key[] { KeyConstants._web, KeyConstants._application, USERS, ELEMENTS, KeyConstants._size }, 0, "templateCache");
		scopes.setEL(KeyConstants._session, sess);

		// Query
		Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._web, KeyConstants._application, START_TIME, KeyConstants._sql }, 0, "requests");
		sct.setEL(QUERIES, qry);

		// Locks
		Query lck = new QueryImpl(new Collection.Key[] { KeyConstants._web, KeyConstants._application, KeyConstants._name, START_TIME, KeyConstants._timeout, KeyConstants._type },
				0, "requests");
		sct.setEL(LOCKS, lck);

		// Loop webs
		ConfigWebImpl web;
		Map<Integer, PageContextImpl> pcs;
		PageContextImpl _pc;
		int row, openConnections = 0;
		CFMLFactoryImpl factory;
		ActiveQuery[] queries;
		ActiveQuery aq;
		ActiveLock[] locks;
		ActiveLock al;
		for (int i = 0; i < webs.length; i++) {

			// Loop requests
			web = (ConfigWebImpl) webs[i];
			factory = (CFMLFactoryImpl) web.getFactory();
			pcs = factory.getActivePageContexts();
			Iterator<PageContextImpl> it = pcs.values().iterator();
			while (it.hasNext()) {
				_pc = it.next();
				if (_pc.isGatewayContext()) continue;

				// Request
				row = req.addRow();
				req.setAt(KeyConstants._web, row, web.getLabel());
				req.setAt(KeyConstants._uri, row, getPath(_pc.getHttpServletRequest()));
				req.setAt(START_TIME, row, new DateTimeImpl(pc.getStartTime(), false));
				req.setAt(KeyConstants._timeout, row, new Double(pc.getRequestTimeout()));

				// Query
				queries = _pc.getActiveQueries();
				if (queries != null) {
					for (int y = 0; y < queries.length; y++) {
						aq = queries[y];
						row = qry.addRow();
						qry.setAt(KeyConstants._web, row, web.getLabel());
						qry.setAt(KeyConstants._application, row, _pc.getApplicationContext().getName());
						qry.setAt(START_TIME, row, new DateTimeImpl(web, aq.startTime, true));
						qry.setAt(KeyConstants._sql, row, aq.sql);
					}
				}

				// Lock
				locks = _pc.getActiveLocks();
				if (locks != null) {
					for (int y = 0; y < locks.length; y++) {
						al = locks[y];
						row = lck.addRow();
						lck.setAt(KeyConstants._web, row, web.getLabel());
						lck.setAt(KeyConstants._application, row, _pc.getApplicationContext().getName());
						lck.setAt(KeyConstants._name, row, al.name);
						lck.setAt(START_TIME, row, new DateTimeImpl(web, al.startTime, true));
						lck.setAt(KeyConstants._timeout, row, Caster.toDouble(al.timeoutInMillis / 1000));
						lck.setAt(KeyConstants._type, row, al.type == LockManager.TYPE_EXCLUSIVE ? "exclusive" : "readonly");
					}
				}
			}

			Iterator<Integer> _it = web.getDatasourceConnectionPool().openConnections().values().iterator();
			while (_it.hasNext()) {
				openConnections += _it.next().intValue();
			}

			// Template Cache
			Mapping[] mappings = ConfigWebUtil.getAllMappings(web);
			long[] tce = templateCacheElements(mappings);
			row = tc.addRow();
			tc.setAt(KeyConstants._web, row, web.getLabel());
			tc.setAt(KeyConstants._size, row, new Double(tce[1]));
			tc.setAt(ELEMENTS, row, new Double(tce[0]));

			// Scope Application
			getAllApplicationScopes(web, factory.getScopeContext(), app);
			getAllCFSessionScopes(web, factory.getScopeContext(), sess);

		}

		// Datasource
		Struct ds = new StructImpl();
		sct.setEL(KeyConstants._datasources, ds);

		ds.setEL(CACHED_QUERIES, Caster.toDouble(pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).size(pc))); // there is only one cache for all contexts
		// ds.setEL(CACHED_QUERIES, Caster.toDouble(pc.getQueryCache().size(pc))); // there is only one
		// cache for all contexts
		ds.setEL(OPEN_CONNECTIONS, Caster.toDouble(openConnections));

		// Memory
		Struct mem = new StructImpl();
		sct.setEL(KeyConstants._memory, mem);
		mem.setEL("heap", SystemUtil.getMemoryUsageAsStruct(SystemUtil.MEMORY_TYPE_HEAP));
		mem.setEL("nonheap", SystemUtil.getMemoryUsageAsStruct(SystemUtil.MEMORY_TYPE_NON_HEAP));

		// uptime
		sct.set("uptime", new DateTimeImpl(engine.uptime(), true));

		// now
		sct.set("now", new DateTimeImpl(pc));

		// SizeAndCount.Size size = SizeAndCount.sizeOf(pc.serverScope());

		return sct;
	}

	private static void getAllApplicationScopes(ConfigWebImpl web, ScopeContext sc, Query app) throws PageException {
		Struct all = sc.getAllApplicationScopes();
		Iterator<Entry<Key, Object>> it = all.entryIterator();
		Entry<Key, Object> e;
		int row;
		Size sac;
		while (it.hasNext()) {
			e = it.next();
			row = app.addRow();
			sac = SizeAndCount.sizeOf(e.getValue());
			app.setAt(KeyConstants._web, row, web.getLabel());
			app.setAt(KeyConstants._application, row, e.getKey().getString());
			app.setAt(KeyConstants._size, row, new Double(sac.size));
			app.setAt(ELEMENTS, row, new Double(sac.count));

		}
	}

	private static void getAllCFSessionScopes(ConfigWebImpl web, ScopeContext sc, Query sess) throws PageException {
		Struct all = sc.getAllCFSessionScopes();
		Iterator it = all.entryIterator(), itt;
		Entry e, ee;
		int row, size, count, users;
		Size sac;
		// applications
		while (it.hasNext()) {
			e = (Entry) it.next();
			itt = ((Map) e.getValue()).entrySet().iterator();
			size = 0;
			count = 0;
			users = 0;
			while (itt.hasNext()) {
				ee = (Entry) itt.next();
				sac = SizeAndCount.sizeOf(ee.getValue());
				size += sac.size;
				count += sac.count;
				users++;
			}
			row = sess.addRow();

			sess.setAt(KeyConstants._web, row, web.getLabel());
			sess.setAt(USERS, row, new Double(users));
			sess.setAt(KeyConstants._application, row, e.getKey().toString());
			sess.setAt(KeyConstants._size, row, new Double(size));
			sess.setAt(ELEMENTS, row, new Double(count));
		}
	}

	private static long[] templateCacheElements(Mapping[] mappings) {
		long elements = 0, size = 0;
		Resource res;
		MappingImpl mapping;
		for (int i = 0; i < mappings.length; i++) {
			mapping = (MappingImpl) mappings[i];
			for (PageSource ps: mapping.getPageSources(true)) {
				elements++;
				res = mapping.getClassRootDirectory().getRealResource(ps.getClassName().replace('.', '/') + ".class");
				size += res.length();
			}
		}
		return new long[] { elements, size };
	}

	public static String getScriptName(HttpServletRequest req) {
		return emptyIfNull(req.getContextPath()) + emptyIfNull(req.getServletPath());
	}

	public static String getPath(HttpServletRequest req) {
		String qs = emptyIfNull(req.getQueryString());
		if (qs.length() > 0) qs = "?" + qs;

		return emptyIfNull(req.getContextPath()) + emptyIfNull(req.getServletPath()) + qs;
	}

	private static String emptyIfNull(String str) {
		if (str == null) return "";
		return str;
	}
}