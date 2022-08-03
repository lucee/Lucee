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
 * Implements the CFML Function getpagecontext
 */
package lucee.runtime.functions.other;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.lang.Pair;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;

public final class CreatePageContext implements Function {

	public static Object call(PageContext pc, String serverName, String scriptName) throws PageException {
		return call(pc, serverName, scriptName, "", new StructImpl(), new StructImpl(), new StructImpl(), new StructImpl());
	}

	public static Object call(PageContext pc, String serverName, String scriptName, String queryString) throws PageException {
		return call(pc, serverName, scriptName, queryString, new StructImpl(), new StructImpl(), new StructImpl(), new StructImpl());
	}

	public static Object call(PageContext pc, String serverName, String scriptName, String queryString, Struct cookies) throws PageException {
		return call(pc, serverName, scriptName, queryString, cookies, new StructImpl(), new StructImpl(), new StructImpl());
	}

	public static Object call(PageContext pc, String serverName, String scriptName, String queryString, Struct cookies, Struct headers) throws PageException {
		return call(pc, serverName, scriptName, queryString, cookies, headers, new StructImpl(), new StructImpl());
	}

	public static Object call(PageContext pc, String serverName, String scriptName, String queryString, Struct cookies, Struct headers, Struct parameters) throws PageException {
		return call(pc, serverName, scriptName, queryString, cookies, headers, parameters, new StructImpl());
	}

	public static Object call(PageContext pc, String serverName, String scriptName, String queryString, Struct cookies, Struct headers, Struct parameters, Struct attributes)
			throws PageException {

		HttpSession session = pc != null && pc.getSessionType() == Config.SESSION_TYPE_JEE ? pc.getSession() : null;
		return ThreadUtil.createPageContext(pc.getConfig(), DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, serverName, scriptName, queryString, toCookies(cookies),
				toPair(headers, true), null, toPair(parameters, true), castValuesToString(attributes), true, -1, session);
	}

	public static Struct castValuesToString(Struct sct) throws PageException {
		Key[] keys = CollectionUtil.keys(sct);
		for (int i = 0; i < keys.length; i++) {
			sct.set(keys[i], Caster.toString(sct.get(keys[i])));
		}
		return sct;
	}

	public static Pair<String, Object>[] toPair(Struct sct, boolean doStringCast) throws PageException {
		if (sct == null) return new Pair[0];
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		Object value;
		List<Pair<String, Object>> pairs = new ArrayList<Pair<String, Object>>();
		while (it.hasNext()) {
			e = it.next();
			value = e.getValue();
			if (doStringCast) value = Caster.toString(value);
			pairs.add(new Pair<String, Object>(e.getKey().getString(), value));
		}
		return pairs.toArray(new Pair[pairs.size()]);
	}

	public static Cookie[] toCookies(Struct sct) throws PageException {
		if (sct == null) return new Cookie[0];
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		List<Cookie> cookies = new ArrayList<Cookie>();
		Cookie c;
		while (it.hasNext()) {
			e = it.next();
			c = ReqRspUtil.toCookie(e.getKey().getString(), Caster.toString(e.getValue()), null);
			if (c != null) cookies.add(c);
			else throw new ApplicationException("Cookie name [" + e.getKey().getString() + "] is invalid");

		}
		return cookies.toArray(new Cookie[cookies.size()]);
	}
}