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
package lucee.runtime.functions.decision;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class IsIPv6 {
	public static boolean call(PageContext pc) throws PageException {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			InetAddress[] ias = InetAddress.getAllByName(ia.getHostName());
			return _call(ias);
		}
		catch (UnknownHostException e) {
			throw Caster.toPageException(e);
		}
	}

	public static boolean call(PageContext pc, String hostName) throws PageException {
		if (StringUtil.isEmpty(hostName)) return call(pc);
		try {
			InetAddress[] ias = InetAddress.getAllByName(hostName);
			return _call(ias);
		}
		catch (UnknownHostException e) {
			if (hostName.equalsIgnoreCase("localhost") || hostName.equals("127.0.0.1") || hostName.equalsIgnoreCase("0:0:0:0:0:0:0:1") || hostName.equalsIgnoreCase("::1"))
				return call(pc);
			throw Caster.toPageException(e);
		}
	}

	private static boolean _call(InetAddress[] ias) {
		for (int i = 0; i < ias.length; i++) {
			if (ias[i] instanceof Inet6Address) return true;
		}
		return false;
	}
}