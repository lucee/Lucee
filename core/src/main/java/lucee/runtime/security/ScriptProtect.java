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
package lucee.runtime.security;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

/**
 * Script-protect to remove cross-attacks from strings
 */
public final class ScriptProtect {

	public static final String[] invalids = new String[] { "object", "embed", "script", "applet", "meta", "iframe" };

	/**
	 * translate all strig values of the struct i script-protected form
	 * 
	 * @param sct Struct to translate its values
	 */
	public static void translate(Struct sct) {
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		Object value;
		while (it.hasNext()) {
			e = it.next();
			value = e.getValue();
			if (value instanceof String) {
				sct.setEL(e.getKey(), translate((String) value));
			}
		}
	}

	/**
	 * translate string to script-protected form
	 * 
	 * @param str
	 * @return translated String
	 */
	public static String translate(String str) {
		if (str == null) return "";
		// TODO do-while machen
		int index, last = 0, endIndex;
		StringBuilder sb = null;
		String tagName;
		while ((index = str.indexOf('<', last)) != -1) {
			// read tagname
			int len = str.length();
			char c;
			for (endIndex = index + 1; endIndex < len; endIndex++) {
				c = str.charAt(endIndex);
				if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) break;
			}
			tagName = str.substring(index + 1, endIndex);

			if (compareTagName(tagName)) {
				if (sb == null) {
					sb = new StringBuilder();
					last = 0;
				}
				sb.append(str.substring(last, index + 1));
				sb.append("invalidTag");
				last = endIndex;
			}
			else if (sb != null) {
				sb.append(str.substring(last, index + 1));
				last = index + 1;
			}
			else last = index + 1;

		}
		if (sb != null) {
			if (last != str.length()) sb.append(str.substring(last));
			return sb.toString();
		}
		return str;
	}

	private static boolean compareTagName(String tagName) {
		for (int i = 0; i < invalids.length; i++) {
			if (invalids[i].equalsIgnoreCase(tagName)) return true;
		}
		return false;
	}
}