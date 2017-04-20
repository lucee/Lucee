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

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntryFilter;
import lucee.commons.io.cache.CacheKeyFilter;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.util.QueryTagFilter;
import lucee.runtime.cache.util.WildCardFilter;
import lucee.runtime.config.Config;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class CacheClear extends BIF implements Function,CacheKeyFilter {
	
	private static final long serialVersionUID = 6080620551371620016L;
	
	public static CacheKeyFilter FILTER=new CacheClear();
	
	public static double call(PageContext pc) throws PageException {
		return _call(pc,null,null);
	}
	
	// FUTURE remove, only exist for code in Lucee archives using that function
	public static double call(PageContext pc,String strFilter) throws PageException {
		return _call(pc,strFilter,null);
	}
	
	public static double call(PageContext pc,Object filterOrTags) throws PageException {
		return _call(pc,filterOrTags,null);
	}
	
	// FUTURE remove, only exist for code in Lucee archives using that function
	public static double call(PageContext pc,String filter, String cacheName) throws PageException {
		return _call(pc, filter, cacheName);
	}
	public static double call(PageContext pc,Object filterOrTags, String cacheName) throws PageException {
		return _call(pc, filterOrTags, cacheName);
	}
	
	private static double _call(PageContext pc,Object filterOrTags, String cacheName) throws PageException {
		try {
			Object filter=FILTER;
			// tags
			if(Decision.isArray(filterOrTags)) {
				String[] arr = ListUtil.toStringArray(Caster.toArray(filterOrTags));
				filter=new QueryTagFilter(arr);
			}
			// filter
			else {
				String strFilter=Caster.toString(filterOrTags);
				if(CacheGetAllIds.isFilter(strFilter))
					filter=new WildCardFilter(strFilter,true);
				
			}
			
			Cache cache = CacheUtil.getCache(pc,cacheName,Config.CACHE_TYPE_OBJECT);
			if(filter instanceof CacheKeyFilter)
				return cache.remove((CacheKeyFilter)filter);
			return cache.remove((CacheEntryFilter)filter);
			
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

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==0)
			return call(pc);
		if(args.length==1)
			return call(pc, args[0]);
		if(args.length==2)
			return call(pc, args[0], Caster.toString(args[1]));
		throw new FunctionException(pc, "CacheClear", 0, 2, args.length);
	}
	
}