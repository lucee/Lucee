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
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;

public class GetFileInfo {

	public static Struct call(PageContext pc, Object oSrc) throws PageException {
		Resource src = Caster.toResource(pc, oSrc, true);
		pc.getConfig().getSecurityManager().checkFileLocation(src);

		Struct sct = new StructImpl();

		sct.set("canRead", Caster.toBoolean(src.isReadable()));
		sct.set("canWrite", Caster.toBoolean(src.isWriteable()));
		sct.set("isHidden", Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_HIDDEN)));
		sct.set("lastmodified", new DateTimeImpl(pc, src.lastModified(), false));
		sct.set(KeyConstants._name, src.getName());
		sct.set(KeyConstants._parent, src.getParent());
		sct.set(KeyConstants._path, src.getAbsolutePath());
		sct.set(KeyConstants._size, Long.valueOf(src.length()));

		if (src.isDirectory()) sct.set(KeyConstants._type, "directory");
		else if (src.isFile()) sct.set(KeyConstants._type, "file");
		else sct.set(KeyConstants._type, "");

		// supported only by lucee
		sct.set("isArchive", Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_ARCHIVE)));
		sct.set("isSystem", Caster.toBoolean(src.getAttribute(Resource.ATTRIBUTE_SYSTEM)));
		sct.set("scheme", src.getResourceProvider().getScheme());
		sct.set("isCaseSensitive", Caster.toBoolean(src.getResourceProvider().isCaseSensitive()));
		sct.set("isAttributesSupported", Caster.toBoolean(src.getResourceProvider().isAttributesSupported()));
		sct.set("isModeSupported", Caster.toBoolean(src.getResourceProvider().isModeSupported()));

		return sct;
	}
}