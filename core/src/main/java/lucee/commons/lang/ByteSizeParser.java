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
package lucee.commons.lang;

import lucee.runtime.op.Caster;

public final class ByteSizeParser {

	private static final long B = 1;
	private static final long KB = 1024;
	private static final long MB = KB * 1024;
	private static final long GB = MB * 1024;
	private static final long TB = GB * 1024;

	public static long parseByteSizeDefinition(String value, long defaultValue) {
		value = value.trim().toLowerCase();

		long factor = B;
		String num = value;
		if (value.endsWith("kb")) {
			factor = KB;
			num = value.substring(0, value.length() - 2).trim();
		}
		else if (value.endsWith("k")) {
			factor = KB;
			num = value.substring(0, value.length() - 1).trim();
		}
		else if (value.endsWith("mb")) {
			factor = MB;
			num = value.substring(0, value.length() - 2).trim();
		}
		else if (value.endsWith("m")) {
			factor = MB;
			num = value.substring(0, value.length() - 1).trim();
		}
		else if (value.endsWith("gb")) {
			factor = GB;
			num = value.substring(0, value.length() - 2).trim();
		}
		else if (value.endsWith("g")) {
			factor = GB;
			num = value.substring(0, value.length() - 1).trim();
		}
		else if (value.endsWith("tb")) {
			factor = TB;
			num = value.substring(0, value.length() - 2).trim();
		}
		else if (value.endsWith("t")) {
			factor = TB;
			num = value.substring(0, value.length() - 1).trim();
		}
		else if (value.endsWith("b")) {
			factor = B;
			num = value.substring(0, value.length() - 1).trim();
		}

		long tmp = Caster.toLongValue(num, Long.MIN_VALUE);
		if (tmp == Long.MIN_VALUE) return defaultValue;
		return tmp * factor;
	}
}