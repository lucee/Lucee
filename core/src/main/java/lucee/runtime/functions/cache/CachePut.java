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

import lucee.commons.io.cache.Cache;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.TimeSpan;

/**
 * 
 */
public final class CachePut implements Function {

	private static final long serialVersionUID = -8636947330333269874L;

	public static String call(PageContext pc, String key,Object value) throws PageException {
		return _call(pc,key, value, null, null,null);
	}
	public static String call(PageContext pc, String key,Object value,TimeSpan timeSpan) throws PageException {
		return _call(pc,key, value, valueOf(timeSpan), null,null);
	}
	public static String call(PageContext pc, String key,Object value,TimeSpan timeSpan, TimeSpan idleTime) throws PageException {
		return _call(pc,key, value, valueOf(timeSpan), valueOf(idleTime),null);
	}
	public static String call(PageContext pc, String key,Object value,TimeSpan timeSpan, TimeSpan idleTime,String cacheName) throws PageException {
		return _call(pc,key, value, valueOf(timeSpan), valueOf(idleTime),cacheName);
	}
	
	private static String _call(PageContext pc, String key,Object value,Long timeSpan, Long idleTime,String cacheName) throws PageException {
		//if(timeSpan!=null && timeSpan.longValue()==0L) return "";
		//if(idleTime!=null && idleTime.longValue()==0L) return "";
		try {
			Cache cache = CacheUtil.getCache(pc,cacheName,Config.CACHE_TYPE_OBJECT);
			cache.put(CacheUtil.key(key), value, idleTime, timeSpan);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
		
		return "";
	}
	
	private static Long valueOf(TimeSpan timeSpan) {
		if(timeSpan==null) return null;
		return Long.valueOf(timeSpan.getMillis());
	}
}