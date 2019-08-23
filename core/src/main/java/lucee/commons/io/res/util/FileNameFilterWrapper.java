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

import java.io.File;
import java.io.FilenameFilter;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileNameResourceFilter;

public final class FileNameFilterWrapper implements FileNameResourceFilter {
	private final FilenameFilter filter;

	public FileNameFilterWrapper(FilenameFilter filter) {
		this.filter = filter;
	}

	@Override
	public boolean accept(Resource dir, String name) {
		if (dir instanceof File) return accept(((File) dir), name);
		return accept(FileWrapper.toFile(dir), name);
	}

	@Override
	public boolean accept(File dir, String name) {
		return filter.accept(dir, name);
	}
}