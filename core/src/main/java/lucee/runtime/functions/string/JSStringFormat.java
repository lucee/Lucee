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
 * Implements the CFML Function jsstringformat
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class JSStringFormat implements Function {

	private static final long serialVersionUID = -4188516789835855021L;

	public static String call(PageContext pc, String str) {
		return invoke(str);
	}

	public static String invoke(String str) {
		int len = str.length();
		StringBuilder rtn = new StringBuilder(len + 10);
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			switch (c) {
			case '\\':
				rtn.append("\\\\");
				break;
			case '\n':
				rtn.append("\\n");
				break;
			case '\r':
				rtn.append("\\r");
				break;
			case '\f':
				rtn.append("\\f");
				break;
			case '\b':
				rtn.append("\\b");
				break;
			case '\t':
				rtn.append("\\t");
				break;
			case '"':
				rtn.append("\\\"");
				break;
			case '\'':
				rtn.append("\\\'");
				break;
			default:
				rtn.append(c);
				break;
			}
		}
		return rtn.toString();
	}

	public static String callx(PageContext pc, String jsString) {// MUST ????
		int len = jsString.length();
		int plus = 0;

		for (int pos = 0; pos < len; pos++) {
			char chr = jsString.charAt(pos);

			switch (chr) {
			case '\\':
			case '\n':
			case '\r':
			case '\f':
			case '\b':
			case '\t':
			case '"':
			case '\'':
				plus++;
				break;
			}
		}
		if (plus == 0) return jsString;

		char[] chars = new char[len + plus];
		int count = 0;

		for (int pos = 0; pos < len; pos++) {
			char chr = jsString.charAt(pos);
			switch (chr) {
			case '\\':
				chars[count++] = '\\';
				chars[count++] = '\\';
				break;
			case '\'':
				chars[count++] = '\\';
				chars[count++] = '\'';
				break;
			case '"':
				chars[count++] = '\\';
				chars[count++] = '"';
				break;
			case '\n':
				chars[count++] = '\\';
				chars[count++] = 'n';
				break;
			case '\r':
				chars[count++] = '\\';
				chars[count++] = 'r';
				break;
			case '\f':
				chars[count++] = '\\';
				chars[count++] = 'f';
				break;
			case '\b':
				chars[count++] = '\\';
				chars[count++] = 'b';
				break;
			case '\t':
				chars[count++] = '\\';
				chars[count++] = 't';
				break;
			default:
				chars[count++] = chr;
				break;
			}
		}
		return new String(chars);
	}
}