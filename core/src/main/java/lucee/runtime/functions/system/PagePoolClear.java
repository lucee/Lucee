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
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContext;

public final class PagePoolClear extends BIF implements Function {

	private static final long serialVersionUID = -2777306151061026079L;

	public static boolean call(PageContext pc) {
		clear(pc, null, false);
		return true;
	}

	public static void clear(PageContext pc, Config c, boolean unused) {
		ConfigWebPro config;
		pc = ThreadLocalPageContext.get(pc);
		if (c == null) config = (ConfigWebPro) ThreadLocalPageContext.getConfig(pc);
		else config = (ConfigWebPro) c;

		// application context
		if (pc != null) {
			ApplicationContext ac = pc.getApplicationContext();
			if (ac != null) {
				clear(config, ac.getMappings(), unused);
				clear(config, ac.getComponentMappings(), unused);
				clear(config, ac.getCustomTagMappings(), unused);
			}
		}

		// config
		clear(config, config.getMappings(), unused);
		clear(config, config.getCustomTagMappings(), unused);
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
		MappingImpl mi = (MappingImpl) mapping;
		if (unused) {
			mi.clearUnused(config);
		}
		else {
			mi.clearPages(null);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "PagePoolClear", 0, 0, args.length);
	}
}