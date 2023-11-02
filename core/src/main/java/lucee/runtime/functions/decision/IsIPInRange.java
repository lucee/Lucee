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

import java.io.IOException;

import lucee.commons.net.IPRange;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public class IsIPInRange {
	public static boolean call(PageContext pc, Object ips, String ip) throws PageException {
		try {
			if (ips instanceof String) return IPRange.getInstance((String) ips).inRange(ip);

			Array arr = Caster.toArray(ips, null);
			if (arr == null) throw new FunctionException(pc, "IsIpRange", 1, "ips", "ips must be a string list or a string array");

			String[] _ips = new String[arr.size()];
			for (int i = 0; i < _ips.length; i++) {
				_ips[i] = Caster.toString(arr.getE(i + 1), null);
				if (_ips[i] == null) throw new FunctionException(pc, "IsIpRange", 1, "ips", "element number " + (i + 1) + " in ips array is not a string");
			}
			return IPRange.getInstance(_ips).inRange(ip);

		}
		catch (IOException e) {
			e.printStackTrace();
			throw Caster.toPageException(e);
		}
	}
}