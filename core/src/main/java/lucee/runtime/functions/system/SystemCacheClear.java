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

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.component.ComponentCacheClear;
import lucee.runtime.functions.other.CTCacheClear;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.KeyConstants;

public final class SystemCacheClear implements Function {

	private static final long serialVersionUID = 2151674703665027213L;

	public static String call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static String call(PageContext pc, String cacheName) throws PageException {

		if (StringUtil.isEmpty(cacheName, true) || "all".equals(cacheName = cacheName.trim().toLowerCase())) {
			PagePoolClear.call(pc);
			ComponentCacheClear.call(pc);
			CTCacheClear.call(pc);
			queryCache(pc);
			tagCache(pc);
			functionCache(pc);
		}
		else if ("template".equals(cacheName) || "page".equals(cacheName)) {
			PagePoolClear.call(pc);
		}
		else if ("component".equals(cacheName) || "cfc".equals(cacheName) || "class".equals(cacheName)) {
			ComponentCacheClear.call(pc);
		}
		else if ("customtag".equals(cacheName) || "ct".equals(cacheName)) {
			CTCacheClear.call(pc);
		}
		else if ("query".equals(cacheName) || "object".equals(cacheName)) {
			queryCache(pc);
		}
		else if ("tag".equals(cacheName)) {
			tagCache(pc);
		}
		else if ("function".equals(cacheName)) {
			functionCache(pc);
		}
		else throw new FunctionException(pc, "cacheClear", 1, "cacheName",
				ExceptionUtil.similarKeyMessage(new Collection.Key[] { KeyConstants._all, KeyConstants._template, KeyConstants._component, KeyImpl.getInstance("customtag"),
						KeyConstants._query, KeyConstants._tag, KeyConstants._function }, cacheName, "cache name", "cache names", null)+ExceptionUtil.similarKeyMessage(new Collection.Key[] { KeyConstants._all, KeyConstants._template, KeyConstants._component, KeyImpl.getInstance("customtag"),
						KeyConstants._query, KeyConstants._tag, KeyConstants._function }, cacheName, "cache name", "cache names", null,true));

		return null;
	}

	private static void queryCache(PageContext pc) throws PageException {
		pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clear(pc);
		// pc.getQueryCache().clear(pc);
	}

	private static void tagCache(PageContext pc) {
		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		PagePoolClear.clear(config, config.getServerTagMappings(), false);
		PagePoolClear.clear(config, config.getTagMappings(), false);
	}

	private static void functionCache(PageContext pc) {
		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		config.clearFunctionCache();
		PagePoolClear.clear(config, config.getServerFunctionMappings(), false);
		PagePoolClear.clear(config, config.getFunctionMappings(), false);

	}
}