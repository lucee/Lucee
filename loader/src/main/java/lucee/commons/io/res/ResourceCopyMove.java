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
package lucee.commons.io.res;

import java.io.IOException;

import lucee.commons.io.res.filter.ResourceFilter;

/**
 * Optional interface for a {@link Resource} that can copy or move a whole directory tree in a more
 * efficient way than Lucee can by walking the tree one resource at a time, for example a remote
 * resource that is able to do this with a handful of requests instead of several requests per file.
 * <p>
 * Lucee checks if a resource implements this interface when it executes <code>cfdirectory</code>
 * with the actions "copy" or "rename", or the functions <code>directoryCopy</code> and
 * <code>directoryRename</code>. All the pre-checks (source exists, is a directory and readable,
 * destination already exists, security) have already been done by Lucee when these methods are
 * called, the implementation does not have to repeat them.
 * <p>
 * Every method returns <code>false</code> when the implementation cannot handle the given
 * arguments (for example a target that belongs to a different resource provider, or a filter it
 * can't apply). In that case nothing must have been changed, and Lucee falls back to the default
 * behaviour, so an implementation only has to care about the cases it can do better.
 */
public interface ResourceCopyMove {

	/**
	 * Copy the directory this resource points to, including all its content, to the target. The
	 * target and missing parent directories are created if necessary, existing files in the target
	 * are overwritten and existing directories are merged, the same as the default behaviour.
	 * 
	 * @param target the new location of the copy, belonging to the same type of resource
	 * @param filter filter limiting what gets copied (applied to every level, as with
	 *            {@link Resource#listResources(ResourceFilter)}), can be
	 *            null for no filtering
	 * @return true if the directory was copied, false if the implementation does not handle this case
	 *         and nothing was done
	 * @throws IOException
	 */
	public boolean copyDirectory(Resource target, ResourceFilter filter) throws IOException;

	/**
	 * Move the directory this resource points to, including all its content, to the target. The
	 * source directory no longer exists afterwards.
	 * 
	 * @param target the new location of the directory, belonging to the same type of resource
	 * @return true if the directory was moved, false if the implementation does not handle this case
	 *         and nothing was done
	 * @throws IOException
	 */
	public boolean moveDirectory(Resource target) throws IOException;
}
