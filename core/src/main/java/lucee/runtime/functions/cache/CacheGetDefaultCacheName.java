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
package lucee.runtime.functions.cache;

import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

/**
 * 
 */
public final class CacheGetDefaultCacheName implements Function {

	private static final long serialVersionUID = 6115589794465960484L;

	public static String call(PageContext pc, String strType) throws PageException {
		int type = CacheUtil.toType(strType,Config.CACHE_TYPE_NONE);
		if(type==Config.CACHE_TYPE_NONE)
			throw new FunctionException(pc,"CacheGetDefaultCacheName",1,"type","invalid type defintion ["+strType+"], valid types are [object,resource,template,query]");
		
		ConfigImpl config=(ConfigImpl) pc.getConfig();
		CacheConnection conn = config.getCacheDefaultConnection(type);
		if(conn==null)
			throw new ExpressionException("there is no default cache defined for type ["+strType+"]");
		
		return conn.getName();
	}
}