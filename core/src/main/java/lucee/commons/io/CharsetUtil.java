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
package lucee.commons.io;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.SortedMap;

import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;

public class CharsetUtil {
	public static final Charset UTF8;
	public static final Charset ISO88591;
	public static final Charset UTF16BE;
	public static final Charset UTF16LE;
	public static final Charset UTF32BE;
	public static final Charset UTF32LE;

	static {
		UTF8 = toCharset("utf-8", null);
		ISO88591 = toCharset("iso-8859-1", null);

		UTF16BE = toCharset("utf-16BE", null);
		UTF16LE = toCharset("utf-16LE", null);

		UTF32BE = toCharset("utf-32BE", null);
		UTF32LE = toCharset("utf-32LE", null);
	}

	public static Charset toCharset(String charset) {
		if (StringUtil.isEmpty(charset, true)) return null;
		return Charset.forName(charset.trim());
	}

	public static Charset toCharset(String charset, Charset defaultValue) {
		if (StringUtil.isEmpty(charset)) return defaultValue;
		try {
			return Charset.forName(charset);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static CharSet toCharSet(String charset) {
		if (StringUtil.isEmpty(charset, true)) return null;
		return new CharSet(Charset.forName(charset.trim()));
	}

	public static CharSet toCharSet(String charset, CharSet defaultValue) {
		if (StringUtil.isEmpty(charset)) return defaultValue;
		try {
			return new CharSet(Charset.forName(charset));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static CharSet toCharSet(Charset charset) {
		if (charset == null) return null;
		return new CharSet(charset);
	}

	public static Charset toCharset(CharSet charset) {
		if (charset == null) return null;
		return charset.toCharset();
	}

	public static Charset getWebCharset() {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) return pc.getWebCharset();
		Config config = ThreadLocalPageContext.getConfig();
		if (config != null) return config.getWebCharset();

		return CharsetUtil.ISO88591;
	}

	public static String[] getAvailableCharsets() {
		SortedMap<String, Charset> map = java.nio.charset.Charset.availableCharsets();
		String[] keys = map.keySet().toArray(new String[map.size()]);
		Arrays.sort(keys);
		return keys;
	}

	/**
	 * is given charset supported or not
	 * 
	 * @param charset
	 * @return
	 */
	public static boolean isSupported(String charset) {
		return java.nio.charset.Charset.isSupported(charset);
	}

}