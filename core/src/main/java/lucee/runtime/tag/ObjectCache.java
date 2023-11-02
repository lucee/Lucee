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
package lucee.runtime.tag;

import java.io.IOException;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.type.cache.CacheResourceProvider;
import lucee.commons.lang.StringUtil;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerFilter;
import lucee.runtime.cache.tag.query.QueryCacheHandlerFilter;
import lucee.runtime.cache.tag.query.QueryCacheHandlerFilterUDF;
import lucee.runtime.cache.util.CacheKeyFilterAll;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;

/**
 * Flushes the query cache
 *
 *
 *
 **/
public final class ObjectCache extends TagImpl {

	private static final int TYPE_QUERY = 1;
	private static final int TYPE_OBJECT = 2;
	private static final int TYPE_TEMPLATE = 3;
	private static final int TYPE_RESOURCE = 4;
	private static final int TYPE_FUNCTION = 5;
	private static final int TYPE_INCLUDE = 6;

	/** Clears queries from the cache in the Application scope. */
	private String action = "clear";
	private CacheHandlerFilter filter;
	private String result = "cfObjectCache";
	private int type = TYPE_QUERY;

	/**
	 * set the value action Clears queries from the cache in the Application scope.
	 * 
	 * @param action value to set
	 **/
	public void setAction(String action) {
		this.action = action;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setType(String strType) throws ApplicationException {
		if (StringUtil.isEmpty(strType, true)) return;
		strType = strType.trim().toLowerCase();
		if ("function".equals(strType)) type = TYPE_FUNCTION;
		else if ("include".equals(strType)) type = TYPE_INCLUDE;
		else if ("object".equals(strType)) type = TYPE_OBJECT;
		else if ("query".equals(strType)) type = TYPE_QUERY;
		else if ("resource".equals(strType)) type = TYPE_RESOURCE;
		else if ("template".equals(strType)) type = TYPE_TEMPLATE;
		else throw new ApplicationException("invalid type [" + strType + "], valid types are [function, include, object, query, resource, template]");

	}

	public void setFilter(Object filter) throws PageException {
		this.filter = createFilter(filter, false);
	}

	public void setFilter(String filter) throws PageException {
		this.filter = createFilter(filter, false);
	}

	public void setFilterignorecase(String filter) throws PageException {
		this.filter = createFilter(filter, true);
	}

	public static CacheHandlerFilter createFilter(Object filter, boolean ignoreCase) throws PageException {
		if (filter instanceof UDF) return new QueryCacheHandlerFilterUDF((UDF) filter);
		String sql = Caster.toString(filter, null);
		if (!StringUtil.isEmpty(sql, true)) {
			return new QueryCacheHandlerFilter(sql, ignoreCase);
		}
		return null;
	}

	/*
	 * public static CacheHandlerFilter createFilterx(String sql) { if(!StringUtil.isEmpty(sql,true)) {
	 * return new QueryCacheHandlerFilter(sql); } return null; }
	 */

	@Override
	public int doStartTag() throws PageException {
		try {
			_doStartTag();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return SKIP_BODY;
	}

	public void _doStartTag() throws PageException, IOException {
		CacheHandlerCollection factory = null;
		Cache cache = null;

		if (type == TYPE_FUNCTION) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null);
		else if (type == TYPE_INCLUDE) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null);
		else if (type == TYPE_QUERY) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null);
		else if (type == TYPE_RESOURCE) {
			cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_RESOURCE, null);

			// no specific cache is defined, get default default cache
			if (cache == null) {
				// get cache resource provider
				CacheResourceProvider crp = null;
				ResourceProvider[] providers = ResourcesImpl.getGlobal().getResourceProviders();
				for (int i = 0; i < providers.length; i++) {
					if (providers[i].getScheme().equals("ram") && providers[i] instanceof CacheResourceProvider) {
						crp = (CacheResourceProvider) providers[i];
					}
				}
				if (crp == null) throw new ApplicationException(Constants.NAME + " was not able to load the Ram Resource Provider");

				// get cache from resource provider
				cache = crp.getCache();
			}
		}
		else if (type == TYPE_OBJECT) {
			// throws an exception if not explicitly defined
			cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_OBJECT);
		}
		else if (type == TYPE_TEMPLATE) {
			// throws an exception if not explicitly defined
			cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_TEMPLATE);
		}

		// Clear
		if (action.equalsIgnoreCase("clear")) {
			if (filter == null) {
				if (cache != null) cache.remove(CacheKeyFilterAll.getInstance());
				else factory.clear(pageContext);
			}
			else {
				if (cache != null) CacheHandlerCollectionImpl.clear(pageContext, cache, filter);
				else factory.clear(pageContext, filter);
			}
		}

		// Size
		else if (action.equalsIgnoreCase("size")) {
			int size = 0;
			if (cache != null) size = cache.keys().size();
			else size = factory.size(pageContext);

			pageContext.setVariable(result, Caster.toDouble(size));
		}
		else throw new ApplicationException("attribute action has an invalid value [" + action + "], valid is only [clear,size]");

	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		action = "clear";
		result = "cfObjectCache";
		filter = null;
		type = TYPE_QUERY;
	}
}