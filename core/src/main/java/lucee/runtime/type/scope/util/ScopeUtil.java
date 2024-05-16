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
package lucee.runtime.type.scope.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.RandomUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLItem;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

public class ScopeUtil {

	public static Map<String, String[]> getParameterMap(URLItem[][] itemsArr, String[] encodings) {
		String n, v;
		String[] arr;
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		URLItem[] items;
		String encoding;
		for (int x = 0; x < itemsArr.length; x++) {
			items = itemsArr[x];
			encoding = encodings[x];
			for (int i = 0; i < items.length; i++) {
				n = items[i].getName();
				v = items[i].getValue();
				if (items[i].isUrlEncoded()) {
					try {
						n = URLDecoder.decode(n, encoding, true);
						v = URLDecoder.decode(v, encoding, true);
					}
					catch (UnsupportedEncodingException e) {
					}
				}
				arr = parameters.get(n);
				if (arr == null) parameters.put(n, new String[] { v });
				else {
					String[] tmp = new String[arr.length + 1];
					System.arraycopy(arr, 0, tmp, 0, arr.length);
					tmp[arr.length] = v;
					parameters.put(n, tmp);
				}
			}
		}
		return parameters;
	}

	public static String[] getParameterValues(URLItem[][] itemsArr, String[] encodings, String name) {
		String n, v;
		String encName;

		String[] arr = null;
		URLItem[] items;
		String encoding;
		for (int x = 0; x < itemsArr.length; x++) {
			items = itemsArr[x];
			encoding = encodings[x];
			if (ReqRspUtil.needEncoding(name, false)) encName = ReqRspUtil.encode(name, encoding);
			else encName = null;
			for (int i = 0; i < items.length; i++) {
				n = items[i].getName();
				if (!name.equals(n) && (encName == null || !encName.equals(n))) {
					continue;
				}
				v = items[i].getValue();
				if (items[i].isUrlEncoded()) {
					try {
						n = URLDecoder.decode(n, encoding, true);
						v = URLDecoder.decode(v, encoding, true);
					}
					catch (UnsupportedEncodingException e) {
					}
				}
				if (arr == null) arr = new String[] { v };
				else {
					String[] tmp = new String[arr.length + 1];
					System.arraycopy(arr, 0, tmp, 0, arr.length);
					tmp[arr.length] = v;
					arr = tmp;
				}
			}
		}
		return arr;
	}

	public static String generateCsrfToken(Map mapTokens, String strKey, boolean forceNew) {
		Collection.Key key = KeyImpl.init(strKey == null ? "" : strKey.trim());
		if (mapTokens instanceof Struct) {
			Struct tokens = Caster.toStruct(mapTokens, null, false);
			String token;
			if (!forceNew) {
				Object tmp = tokens.get(key, null);
				token = tmp == null ? null : Caster.toString(tmp, null);
				if (!StringUtil.isEmpty(token)) return token;
			}
			token = RandomUtil.createRandomStringLC(40);
			tokens.setEL(key, token);
			return token;
		}
		String token;
		if (!forceNew) {
			Object tmp = mapTokens.get(key);
			token = tmp == null ? null : Caster.toString(tmp, null);
			if (!StringUtil.isEmpty(token)) return token;
		}

		token = RandomUtil.createRandomStringLC(40);
		mapTokens.put(key, token);
		return token;
	}

	public static boolean verifyCsrfToken(Map mapTokens, String token, String strKey) {
		Collection.Key key = KeyImpl.init(strKey == null ? "" : strKey.trim());
		if (mapTokens instanceof Struct) {
			Struct tokens = (Struct) mapTokens;
			String _token = Caster.toString(tokens.get(key, null), null);
			return (_token != null) && _token.equalsIgnoreCase(token);
		}

		String _token = Caster.toString(mapTokens.get(key), null);
		return (_token != null) && _token.equalsIgnoreCase(token);
	}
}