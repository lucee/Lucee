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
/**
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system;

import java.util.Collection;
import java.util.Iterator;

import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.ext.function.Function;

public final class PagePoolClear implements Function {

	public static boolean call(PageContext pc) {
		clear(pc, null, false);
		return true;
	}

	public static void clear(PageContext pc, Config c, boolean unused) {
		ConfigWebImpl config;
		if (c == null) config = (ConfigWebImpl) ThreadLocalPageContext.getConfig(pc);
		else config = (ConfigWebImpl) c;

		clear(config, config.getMappings(), unused);
		clear(config, config.getCustomTagMappings(), unused);
		if (pc != null) clear(config, pc.getApplicationContext().getMappings(), unused);
		clear(config, config.getComponentMappings(), unused);
		clear(config, config.getFunctionMappings(), unused);
		clear(config, config.getServerFunctionMappings(), unused);
		clear(config, config.getTagMappings(), unused);
		clear(config, config.getServerTagMappings(), unused);
	}

	public static void clear(Config config, Collection<Mapping> mappings, boolean unused) {
		if (mappings == null) return;
		Iterator<Mapping> it = mappings.iterator();
		while (it.hasNext()) {
			clear(config, it.next(), unused);
		}
	}

	public static void clear(Config config, Mapping[] mappings, boolean unused) {
		if (mappings == null) return;
		for (int i = 0; i < mappings.length; i++) {
			clear(config, mappings[i], unused);
		}
	}

	public static void clear(Config config, Mapping mapping, boolean unused) {
		if (mapping == null) return;
		PageSourcePool pool = ((MappingImpl) mapping).getPageSourcePool();
		if (unused) pool.clearUnused((ConfigImpl) config);
		else pool.clearPages(null);

	}
}