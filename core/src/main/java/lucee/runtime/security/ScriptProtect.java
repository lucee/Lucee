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
import java.util.regex.Pattern;

import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

/**
 * Script-protect to remove cross-attacks from strings
 */
public final class ScriptProtect {

    public static final String INVALID_TAG_REGEX_DEFAULT = "<s*(object|embed|script|applet|meta|iframe)";

	/**
	 * translate all string values of the struct in a script-protected form
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
        ConfigWebPro config = (ConfigWebPro) ThreadLocalPageContext.get().getConfig();
        for(Pattern regexFilterPattern : config.getScriptProtectRegexList()){
            str = invalidateMaliciousInput(str, regexFilterPattern);
        }
        return str;
	}

    private static String invalidateMaliciousInput(String str, Pattern regexPattern) {
        return regexPattern.matcher(str).replaceAll("invalid");
    }
}