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
 * Implements the CFML Function GetLocalHostIP
 */
package lucee.runtime.functions.system;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lucee.commons.net.IPUtil;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class GetLocalHostIP implements Function {

	public static Object call(PageContext pc) {
		return callLegacy();
	}

	public static Object call(PageContext pc, boolean all, boolean refresh) {

		if (all) return IPUtil.getLocalIPs(refresh);

		return callLegacy();
	}

	public static Object call(PageContext pc, boolean all) {

		return call(pc, all, false);
	}

	static String callLegacy() {

		try {
			if (InetAddress.getLocalHost() instanceof Inet6Address) return "::1";
		}
		catch (UnknownHostException e) {
		}
		return "127.0.0.1";
	}
}