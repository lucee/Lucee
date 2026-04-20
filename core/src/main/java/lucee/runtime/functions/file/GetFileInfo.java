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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;

public final class GetFileInfo {

	public static Struct call(PageContext pc, Object oSrc) throws PageException {
		Resource src = Caster.toResource(pc, oSrc, true);
		File file = new File(Caster.toString(oSrc));
		BasicFileAttributes attr;
		SecurityManagerImpl.checkFileLocation(pc, src);

		Struct sct = new StructImpl();

		sct.set(KeyConstants._canRead, Caster.toBoolean(src.isReadable()));
		sct.set(KeyConstants._canWrite, Caster.toBoolean(src.isWriteable()));
		sct.set(KeyConstants._isHidden, Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_HIDDEN)));
		sct.set(KeyConstants._lastmodified, new DateTimeImpl(src.lastModified()));
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			sct.set(KeyConstants._fileCreated, new DateTimeImpl(attr.creationTime().toMillis()));
		}
		catch (Exception e) {}
		sct.set(KeyConstants._name, src.getName());
		sct.set(KeyConstants._parent, src.getParent());
		sct.set(KeyConstants._path, src.getAbsolutePath());
		sct.set(KeyConstants._size, Long.valueOf(src.length()));

		if (src.isDirectory()) sct.set(KeyConstants._type, "directory");
		else if (src.isFile()) sct.set(KeyConstants._type, "file");
		else sct.set(KeyConstants._type, "");

		// supported only by lucee
		sct.set(KeyConstants._isArchive, Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_ARCHIVE)));
		sct.set(KeyConstants._isSystem, Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_SYSTEM)));
		sct.set(KeyConstants._scheme, src.getResourceProvider().getScheme());
		sct.set(KeyConstants._isCaseSensitive, Caster.toBoolean(src.getResourceProvider().isCaseSensitive()));
		sct.set(KeyConstants._isAttributesSupported, Caster.toBoolean(src.getResourceProvider().isAttributesSupported()));
		sct.set(KeyConstants._isModeSupported, Caster.toBoolean(src.getResourceProvider().isModeSupported()));

		return sct;
	}
}