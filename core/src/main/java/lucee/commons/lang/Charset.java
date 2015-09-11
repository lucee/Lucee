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

import java.util.Arrays;
import java.util.SortedMap;

public final class Charset {

	public static String UTF8 = "UTF-8";

	public static String[] getAvailableCharsets() {
		 SortedMap map = java.nio.charset.Charset.availableCharsets();
		 String[] keys=(String[]) map.keySet().toArray(new String[map.size()]);
		 Arrays.sort(keys);
		 return keys;
	}
	
	/**
	 * is given charset supported or not
	 * @param charset
	 * @return
	 */
	public static boolean isSupported(String charset) {
		return java.nio.charset.Charset.isSupported(charset);
	}
	
}