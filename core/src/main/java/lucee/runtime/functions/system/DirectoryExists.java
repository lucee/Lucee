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
 * Implements the CFML Function directoryexists
 */
package lucee.runtime.functions.system;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class DirectoryExists extends BIF {

	private static final long serialVersionUID = 4375183479006129959L;

	public static boolean call(PageContext pc, String path) throws PageException {
		return call(pc, path, pc.getConfig().allowRealPath());
	}

	public static boolean call(PageContext pc, String path, Object oAllowRealPath) throws PageException {
		if (StringUtil.isEmpty(path, true)) return false;

		Resource file;
		boolean allowRealPath = (oAllowRealPath == null) ? pc.getConfig().allowRealPath() : Caster.toBooleanValue(oAllowRealPath);
		if (allowRealPath) {
			file = ResourceUtil.toResourceNotExisting(pc, path, allowRealPath, false);
			// TODO das else braucht es eigentlich nicht mehr
		}
		else {
			// ARP
			file = pc.getConfig().getResource(path);
			if (file != null && !file.isAbsolute()) return false;
		}
		pc.getConfig().getSecurityManager().checkFileLocation(file);
		return file.isDirectory();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), args[1]);
		throw new FunctionException(pc, "DirectoryExists", 1, 2, args.length);
	}
}