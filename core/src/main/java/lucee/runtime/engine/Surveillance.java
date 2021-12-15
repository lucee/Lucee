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
package lucee.runtime.engine;

import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.DoubleStruct;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.util.KeyConstants;

class Surveillance {

	private static final Collection.Key PAGE_POOL = KeyImpl.getInstance("pagePool");
	private static final Collection.Key CLASS_LOADER = KeyImpl.getInstance("classLoader");
	private static final Collection.Key QUERY_CACHE = KeyImpl.getInstance("queryCache");
	private static final Collection.Key PAGE_CONTEXT_STACK = KeyImpl.getInstance("pageContextStack");

	public static Struct getInfo(Config config) throws PageException {

		Struct sct = new StructImpl();

		// memory
		DoubleStruct mem = new DoubleStruct();
		sct.set(KeyConstants._memory, mem);
		getInfoMemory(mem, config);

		// count
		// ScopeContext sc = ((CFMLFactoryImpl)config.getFactory()).getScopeContext();
		// sc.getSessionCount(pc)

		return sct;
	}

	private static void getInfoMemory(Struct parent, Config config) throws PageException {
		DoubleStruct server = new DoubleStruct();
		DoubleStruct web = new DoubleStruct();
		parent.set(KeyConstants._server, server);
		parent.set(KeyConstants._web, web);

		boolean isConfigWeb = config instanceof ConfigWeb;

		// server
		/*
		 * ConfigServer cs=isConfigWeb? config.getConfigServerImpl(): ((ConfigServer)config);
		 */

		// infoResources(server,cs);
		// web
		if (isConfigWeb) {
			_getInfoMemory(web, server, (ConfigWeb) config);
		}
		else {
			ConfigWeb[] configs = ((ConfigServer) config).getConfigWebs();
			for (int i = 0; i < configs.length; i++) {
				_getInfoMemory(web, server, configs[i]);
			}
		}
	}

	private static void _getInfoMemory(Struct web, Struct server, ConfigWeb config) throws PageException {
		DoubleStruct sct = new DoubleStruct();
		infoMapping(sct, config);
		// infoResources(sct,config);

		infoScopes(sct, server, config);
		infoPageContextStack(sct, config.getFactory());
		// infoQueryCache(sct,config.getFactory());
		// size+=infoResources(sct,cs);

		web.set(config.getConfigDir().getPath(), sct);
	}

	private static void infoMapping(Struct parent, Config config) throws PageException {
		DoubleStruct map = new DoubleStruct();
		infoMapping(map, config.getMappings(), false);
		infoMapping(map, config.getCustomTagMappings(), true);
		parent.set(KeyConstants._mappings, map);
	}

	private static void infoMapping(Struct map, Mapping[] mappings, boolean isCustomTagMapping) throws PageException {
		if (mappings == null) return;

		DoubleStruct sct = new DoubleStruct();

		long size;
		MappingImpl mapping;
		for (int i = 0; i < mappings.length; i++) {
			mapping = (MappingImpl) mappings[i];

			// archive classloader
			size = mapping.getArchive() != null ? mapping.getArchive().length() : 0;
			sct.set("archiveClassLoader", Caster.toDouble(size));

			// physical classloader
			size = mapping.getPhysical() != null ? mapping.getPhysical().length() : 0;
			sct.set("physicalClassLoader", Caster.toDouble(size));

			// pagepool
			// size = SizeOf.size(mapping.getPageSourcePool());
			// sct.set(PAGE_POOL, Caster.toDouble(size));

			map.set(!isCustomTagMapping ? mapping.getVirtual() : mapping.getStrPhysical(), sct);
		}
	}

	private static void infoScopes(Struct web, Struct server, ConfigWeb config) throws PageException {
		ScopeContext sc = ((CFMLFactoryImpl) config.getFactory()).getScopeContext();
		DoubleStruct webScopes = new DoubleStruct();
		DoubleStruct srvScopes = new DoubleStruct();

		long s;
		s = sc.getScopesSize(Scope.SCOPE_SESSION);
		webScopes.set("session", Caster.toDouble(s));

		s = sc.getScopesSize(Scope.SCOPE_APPLICATION);
		webScopes.set("application", Caster.toDouble(s));

		s = sc.getScopesSize(Scope.SCOPE_CLUSTER);
		srvScopes.set("cluster", Caster.toDouble(s));

		s = sc.getScopesSize(Scope.SCOPE_SERVER);
		srvScopes.set("server", Caster.toDouble(s));

		s = sc.getScopesSize(Scope.SCOPE_CLIENT);
		webScopes.set("client", Caster.toDouble(s));

		web.set(KeyConstants._scopes, webScopes);
		server.set(KeyConstants._scopes, srvScopes);
	}

	private static void infoPageContextStack(Struct parent, CFMLFactory factory) throws PageException {
		long size = ((CFMLFactoryImpl) factory).getPageContextsSize();
		parent.set(PAGE_CONTEXT_STACK, Caster.toDouble(size));
	}

}