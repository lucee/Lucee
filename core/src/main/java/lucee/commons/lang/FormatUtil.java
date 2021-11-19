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

public class FormatUtil {

	/**
	 * convert given time in nanoseconds to a flaoting point number in milliseconds reduced to max 3
	 * digits on the right site
	 * 
	 * @param ns
	 * @return
	 */
	public static double formatNSAsMSDouble(long ns) {
		if (ns >= 100000000L) return (ns / 1000000L);
		if (ns >= 10000000L) return ((ns / 100000L)) / 10D;
		if (ns >= 1000000L) return ((ns / 10000L)) / 100D;
		return ((ns / 1000L)) / 1000D;
	}
}