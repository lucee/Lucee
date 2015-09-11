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
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class DirectoryExists implements Function {
	public static boolean call(PageContext pc , String path) throws PageException {
		return call(pc, path,pc.getConfig().allowRealPath());
	}
	public static boolean call(PageContext pc , String path,Object oAllowRealPath) throws PageException {
		Resource file;
		if(oAllowRealPath==null) return call(pc, path);
		boolean allowRealPath = Caster.toBooleanValue(oAllowRealPath);
		if(allowRealPath) {
			file=ResourceUtil.toResourceNotExisting(pc, path,allowRealPath,false);
			// TODO das else braucht es eigentlich nicht mehr
		}
		else {
			// ARP
			file=pc.getConfig().getResource(path);
			if(file!=null && !file.isAbsolute()) return false;
		}
		 
	    pc.getConfig().getSecurityManager().checkFileLocation(file);
	    return file.isDirectory() && file.exists();
	}
}