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
 * Implements the CFML Function paragraphformat
 */
package lucee.runtime.functions.displayFormatting;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class ParagraphFormat implements Function {
	public static String call(PageContext pc, String str) {
		StringBuilder sb = new StringBuilder(str.length());
		char[] chars = str.toCharArray();
		boolean flag = false;

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case '\r':
				if (i + 1 < chars.length && chars[i + 1] == '\r') flag = false;
				sb.append(' ');
				break;

			case '\n':
				if (flag) {
					sb.append(" <P>\r\n");
					flag = false;
				}
				else {
					sb.append(' ');
					flag = true;
				}
				break;
			default:
				sb.append(c);
				flag = false;
				break;
			}
		}
		sb.append(" <P>");
		return sb.toString();
	}
}