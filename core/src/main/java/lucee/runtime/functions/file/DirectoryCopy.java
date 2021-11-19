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
package lucee.runtime.functions.file;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceAndResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.UDFFilter;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.tag.Directory;

public class DirectoryCopy implements Function {

	private static final long serialVersionUID = -8591512197642527401L;

	public static String call(PageContext pc, String source, String destination) throws PageException {
		return call(pc, source, destination, false, null, true);
	}

	public static String call(PageContext pc, String source, String destination, boolean recurse) throws PageException {
		return call(pc, source, destination, recurse, null, true);
	}

	public static String call(PageContext pc, String source, String destination, boolean recurse, Object filter) throws PageException {
		return call(pc, source, destination, recurse, filter, true);
	}

	public static String call(PageContext pc, String source, String destination, boolean recurse, Object filter, boolean createPath) throws PageException {
		Resource src = ResourceUtil.toResourceNotExisting(pc, source);
		ResourceAndResourceNameFilter fi = filter == null ? null : UDFFilter.createResourceAndResourceNameFilter(filter);
		Directory.actionCopy(pc, src, destination, null, createPath, null, null, fi, recurse, Directory.NAMECONFLICT_DEFAULT);
		return null;
	}

}