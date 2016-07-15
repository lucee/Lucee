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
package lucee.runtime.functions.cache;

import lucee.commons.io.cache.CacheKeyFilter;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.util.WildCardFilter;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * 
 */
public final class CacheClear implements Function,CacheKeyFilter {
	
	public static CacheKeyFilter FILTER=new CacheClear();

	public static double call(PageContext pc) throws PageException {
		return call(pc,null,null);
		
	}
	public static double call(PageContext pc,String strFilter) throws PageException {
		return call(pc,strFilter,null);
		
	}
	public static double call(PageContext pc,String strFilter, String cacheName) throws PageException {
		try {
			CacheKeyFilter f=FILTER;
			if(CacheGetAllIds.isFilter(strFilter))
				f=new WildCardFilter(strFilter,true);
			return CacheUtil.getCache(pc,cacheName,Config.CACHE_TYPE_OBJECT).remove(f);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public boolean accept(String key) {
		return true;
	}

	@Override
	public String toPattern() {
		return "*";
	}
	
}