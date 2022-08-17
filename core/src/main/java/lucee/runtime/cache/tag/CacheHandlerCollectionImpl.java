/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.cache.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.KeyGenerator;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.tag.query.QueryCacheItem;
import lucee.runtime.cache.tag.timespan.TimespanCacheHandler;
import lucee.runtime.cache.tag.udf.UDFArgConverter;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.db.SQL;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.HttpParamBean;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDFImpl;
import lucee.runtime.type.util.KeyConstants;

// DO NOT CHANGE interface used by extension axis
public class CacheHandlerCollectionImpl implements CacheHandlerCollection {

	public static final char CACHE_DEL = ';';
	public static final char CACHE_DEL2 = ':';

	private ConfigWeb cw;
	Map<String, CacheHandler> handlers = new HashMap<String, CacheHandler>();
	// private final CacheHandler rch;
	// private final CacheHandler sch;
	// private final CacheHandler tch;

	/**
	 * 
	 * @param cw config object this Factory is related
	 * @param cacheType type of the cache, see Config.CACHE_TYPE_XXX
	 * @throws PageException
	 */
	protected CacheHandlerCollectionImpl(ConfigWeb cw, int cacheType) {
		this.cw = cw;

		Iterator<Entry<String, Class<CacheHandler>>> it = ((ConfigWebPro) cw).getCacheHandlers();
		Entry<String, Class<CacheHandler>> e;
		CacheHandler ch;
		while (it.hasNext()) {
			e = it.next();
			try {
				ch = e.getValue().newInstance();
				ch.init(cw, e.getKey(), cacheType);
				handlers.put(e.getKey(), ch);
			}
			catch (Exception pe) {
				ThreadLocalPageContext.getLog(cw, "application").error("cache-handler:" + e.getKey(), pe);
				throw new PageRuntimeException(Caster.toPageException(pe));
			}
		}
	}

	@Override
	public CacheHandler getInstanceMatchingObject(Object cachedWithin, CacheHandler defaultValue) {
		Iterator<CacheHandler> it = handlers.values().iterator();
		CacheHandler ch;
		while (it.hasNext()) {
			ch = it.next();
			if (ch.acceptCachedWithin(cachedWithin)) return ch;
		}
		return defaultValue;
	}

	public CacheHandler getTimespanInstance(CacheHandler defaultValue) {
		Iterator<CacheHandler> it = handlers.values().iterator();
		CacheHandler ch;
		while (it.hasNext()) {
			ch = it.next();
			if (ch instanceof TimespanCacheHandler) return ch;
		}
		return defaultValue;
	}

	@Override
	public CacheHandler getInstance(String cacheHandlerId, CacheHandler defaultValue) {

		// return cache handler matching
		CacheHandler ch = handlers.get(cacheHandlerId);
		if (ch != null) return ch;

		ch = handlers.get(cacheHandlerId.toLowerCase().trim());
		if (ch != null) return ch;

		return defaultValue;
	}

	@Override
	public int size(PageContext pc) throws PageException {
		int size = 0;
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			size += it.next().size(pc);
		}
		return size;
	}

	@Override
	public void clear(PageContext pc) throws PageException {
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			it.next().clear(pc);
		}
	}

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) throws PageException {
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			it.next().clear(pc, filter);
		}
	}

	@Override
	public void clean(PageContext pc) throws PageException {
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			it.next().clean(pc);
		}
	}

	@Override
	public void remove(PageContext pc, String id) throws PageException {
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			it.next().remove(pc, id);
		}
	}

	@Override
	public List<String> getPatterns() {
		List<String> patterns = new ArrayList<String>();
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			patterns.add(it.next().pattern());
		}
		return patterns;
	}

	public static String createId(PageSource[] sources) throws PageException {
		String str;
		if (sources.length == 1) {
			str = sources[0].getDisplayPath();
		}
		else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < sources.length; i++) {
				if (i > 0) sb.append(";");
				sb.append(sources[i].getDisplayPath());
			}
			str = sb.toString();
		}
		try {
			return CacheUtil.key(KeyGenerator.createKey(str));
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	public static String createId(SQL sql, String datasource, String username, String password, int returnType) throws PageException {
		try {
			return CacheUtil.key(KeyGenerator.createKey(sql.toHashString() + datasource + username + password + returnType));
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	public static String createId(Resource res, boolean binary) {
		StringBuilder sb = new StringBuilder().append(res.getAbsolutePath()).append(CACHE_DEL).append(binary).append(CACHE_DEL);

		return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX);
	}

	public static String createId(String wsdlUrl, String username, String password, ProxyData proxyData, String methodName, Object[] arguments, Struct namedArguments) {
		StringBuilder sb = new StringBuilder().append(wsdlUrl).append(CACHE_DEL).append(username).append(CACHE_DEL).append(password).append(CACHE_DEL).append(proxyData)
				.append(CACHE_DEL).append(methodName);

		createIdArgs(null, sb, arguments, namedArguments);

		return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX);
	}

	public static String createId(UDFImpl udf, Object[] args, Struct values) {
		String src = udf.getSource();
		StringBuilder sb = new StringBuilder().append(src == null ? "" : src).append(CACHE_DEL).append(udf.properties.getStartLine()).append(CACHE_DEL)
				.append(udf.getFunctionName()).append(CACHE_DEL);
		createIdArgs(udf, sb, args, values);
		return HashUtil.create64BitHashAsString(sb, Character.MAX_RADIX);
	}

	private static void createIdArgs(UDFImpl udf, StringBuilder sb, Object[] args, Struct namedArgs) {
		if (namedArgs != null) {
			// argumentCollection
			Struct sct;
			if (namedArgs.size() == 1 && (sct = Caster.toStruct(namedArgs.get(KeyConstants._argumentCollection, null), null)) != null) {
				_create(sct, sb);
			}
			else _create(namedArgs, sb);
		}
		else if (args != null) {
			FunctionArgument[] _args = udf == null ? null : udf.getFunctionArguments();
			sb.append('{');
			for (int i = 0; i < args.length; i++) {
				if (_args != null && _args.length > i) sb.append(_args[i].getName().getLowerString()).append(':');
				sb.append(_createId(args[i])).append(',');
			}
			sb.append('}');
		}
	}

	private static void _create(Struct sct, StringBuilder sb) {
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		sb.append('{');
		while (it.hasNext()) {
			e = it.next();
			sb.append(e.getKey().getLowerString()).append(':').append(_createId(e.getValue())).append(',');
		}
		sb.append('}');
	}

	private static String _createId(Object values) {
		return UDFArgConverter.serialize(values);
	}

	public static String createId(String url, String urlToken, short method, ArrayList<HttpParamBean> params, String username, String password, int port, String proxyserver,
			int proxyport, String proxyuser, String proxypassword, String useragent) {
		StringBuilder sb = new StringBuilder().append(url).append(CACHE_DEL).append(urlToken).append(CACHE_DEL).append(method).append(CACHE_DEL).append(username).append(CACHE_DEL)
				.append(password).append(CACHE_DEL).append(port).append(CACHE_DEL).append(proxyserver).append(CACHE_DEL).append(proxyport).append(CACHE_DEL).append(proxyuser)
				.append(CACHE_DEL).append(proxypassword).append(CACHE_DEL).append(proxypassword).append(CACHE_DEL).append(useragent).append(CACHE_DEL);

		HttpParamBean hpb;
		Iterator<HttpParamBean> it = params.iterator();
		while (it.hasNext()) {
			hpb = it.next();
			sb.append(hpb.getEncoded()).append(CACHE_DEL).append(hpb.getMimeType()).append(CACHE_DEL).append(hpb.getName()).append(CACHE_DEL).append(hpb.getType())
					.append(CACHE_DEL).append(toString(hpb.getValue())).append(CACHE_DEL).append(toString(hpb.getFile())).append(CACHE_DEL);
		}
		return HashUtil.create64BitHashAsString(sb.toString());
	}

	private static Object toString(Object value) {
		return Caster.toString(value, null);
	}

	private static Object toString(Resource file) {
		return file == null ? "" : file.getAbsolutePath();
	}

	public static String toStringCacheName(int type, String defaultValue) {
		switch (type) {
		case ConfigPro.CACHE_TYPE_FUNCTION:
			return "function";
		case ConfigPro.CACHE_TYPE_INCLUDE:
			return "include";
		case ConfigPro.CACHE_TYPE_OBJECT:
			return "object";
		case ConfigPro.CACHE_TYPE_QUERY:
			return "query";
		case ConfigPro.CACHE_TYPE_RESOURCE:
			return "resource";
		case ConfigPro.CACHE_TYPE_TEMPLATE:
			return "template";
		case ConfigPro.CACHE_TYPE_HTTP:
			return "http";
		case ConfigPro.CACHE_TYPE_FILE:
			return "file";
		case ConfigPro.CACHE_TYPE_WEBSERVICE:
			return "webservice";
		}
		return defaultValue;
	}

	public static CacheItem toCacheItem(Object value, CacheItem defaultValue) {
		if (value instanceof CacheItem) return (CacheItem) value;
		return defaultValue;
	}

	@Override
	public void release(PageContext pc) throws PageException {
		Iterator<CacheHandler> it = handlers.values().iterator();
		while (it.hasNext()) {
			it.next().release(pc);
		}
	}

	public static void clear(PageContext pc, Cache cache, CacheHandlerFilter filter) {
		try {
			Iterator<CacheEntry> it = cache.entries().iterator();
			CacheEntry ce;
			Object obj;
			while (it.hasNext()) {
				ce = it.next();
				if (filter == null) {
					cache.remove(ce.getKey());
					continue;
				}

				obj = ce.getValue();
				if (obj instanceof QueryCacheItem) obj = ((QueryCacheItem) obj).getQuery();
				if (filter.accept(obj)) cache.remove(ce.getKey());
			}
		}
		catch (IOException e) {
		}
	}
}