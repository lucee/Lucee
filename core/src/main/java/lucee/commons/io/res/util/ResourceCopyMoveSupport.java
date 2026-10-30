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
package lucee.commons.io.res.util;

import java.io.IOException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceCopyMove;
import lucee.commons.io.res.filter.ResourceFilter;

/**
 * Copies or moves a directory tree. If the source resource implements {@link ResourceCopyMove} it
 * gets the chance to do this in its own, more efficient way, if it does not implement the interface
 * or declines the request, the default behaviour is used (walking the tree one resource at a time),
 * which is exactly what Lucee did before the interface existed.
 */
public final class ResourceCopyMoveSupport {

	private ResourceCopyMoveSupport() {
	}

	/**
	 * copy a directory with its content to the target, same as
	 * {@link ResourceUtil#copyRecursive(Resource, Resource, ResourceFilter)}
	 */
	public static void copyDirectory(Resource src, Resource trg, ResourceFilter filter) throws IOException {
		if (src instanceof ResourceCopyMove && ((ResourceCopyMove) src).copyDirectory(trg, filter)) return;
		ResourceUtil.copyRecursive(src, trg, filter);
	}

	/**
	 * move a directory with its content to the target, same as {@link Resource#moveTo(Resource)}
	 */
	public static void moveDirectory(Resource src, Resource trg) throws IOException {
		if (src instanceof ResourceCopyMove && ((ResourceCopyMove) src).moveDirectory(trg)) return;
		src.moveTo(trg);
	}
}
