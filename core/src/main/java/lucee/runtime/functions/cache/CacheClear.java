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
import lucee.commons.lang.StringUtil;
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
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class CacheClear extends BIF implements Function, CacheKeyFilter {

	private static final long serialVersionUID = 6080620551371620016L;

	public static CacheKeyFilter FILTER = new CacheClear();

	public static double call(PageContext pc) throws PageException {
		return _call(pc, null, null);
	}

	public static double call(PageContext pc, Object filterOrTags) throws PageException {
		return _call(pc, filterOrTags, null);
	}

	public static double call(PageContext pc, Object filterOrTags, String cacheName) throws PageException {
		return _call(pc, filterOrTags, cacheName);
	}

	private static double _call(PageContext pc, Object filterOrTags, String cacheName) throws PageException {
		try {
			Object filter = FILTER;
			// tags
			boolean isArray = false;
			String dsn = null;
			if ((isArray = Decision.isArray(filterOrTags)) || Decision.isStruct(filterOrTags)) {

				// read tags from collection and datasource (optional)
				String[] tags;
				if (!isArray) {
					Struct sct = Caster.toStruct(filterOrTags);
					Array arr = Caster.toArray(sct.get("tags", null), null);
					if (arr == null) throw new FunctionException(pc, "CacheClear", 1, "tags",
							"if you pass the tags within a struct, that struct need to have a key [tags] containing the tags in an array.");
					tags = ListUtil.toStringArray(arr);
					dsn = Caster.toString(sct.get(KeyConstants._datasource, null), null);
				}
				else {
					tags = ListUtil.toStringArray(Caster.toArray(filterOrTags));
				}

				// get default datasource
				if (StringUtil.isEmpty(dsn)) {
					Object tmp = pc.getApplicationContext().getDefDataSource();
					dsn = tmp instanceof CharSequence ? Caster.toString(tmp, null) : null;
				}

				filter = new QueryTagFilter(tags, StringUtil.isEmpty(dsn) ? null : dsn);
			}
			// filter
			else {
				String strFilter = Caster.toString(filterOrTags);
				if (CacheGetAllIds.isFilter(strFilter)) filter = new WildCardFilter(strFilter, true);

			}

			Cache cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT);
			if (filter instanceof CacheKeyFilter) return cache.remove((CacheKeyFilter) filter);
			return cache.remove((CacheEntryFilter) filter);

		}
		catch (Exception e) {
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
		if (args.length == 0) return call(pc);
		if (args.length == 1) return call(pc, args[0]);
		if (args.length == 2) return call(pc, args[0], Caster.toString(args[1]));
		throw new FunctionException(pc, "CacheClear", 0, 2, args.length);
	}

}