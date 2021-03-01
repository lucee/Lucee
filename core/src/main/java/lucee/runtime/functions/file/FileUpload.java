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

import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.tag.FileTag;
import lucee.runtime.tag.util.FileUtil;
import lucee.runtime.type.Struct;

public class FileUpload extends BIF implements Function {

	private static final long serialVersionUID = 8289325119924649321L;

	public static Struct call(PageContext pc, String destination) throws PageException {
		return call(pc, destination, null, null, null, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField) throws PageException {
		return call(pc, destination, fileField, null, null, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept) throws PageException {
		return call(pc, destination, fileField, accept, null, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict) throws PageException {
		return call(pc, destination, fileField, accept, nameConflict, null, null, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict, Object allowedExtensions) throws PageException {
		return call(pc, destination, fileField, accept, nameConflict, allowedExtensions, null, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict, Object allowedExtensions, Object blockedExtensions)
			throws PageException {
		return call(pc, destination, fileField, accept, nameConflict, allowedExtensions, blockedExtensions, null, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict, Object allowedExtensions, Object blockedExtensions,
			String mode) throws PageException {
		return call(pc, destination, fileField, accept, nameConflict, allowedExtensions, blockedExtensions, mode, null, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict, Object allowedExtensions, Object blockedExtensions,
			String mode, String attributes) throws PageException {
		return call(pc, destination, fileField, accept, nameConflict, allowedExtensions, blockedExtensions, mode, attributes, null);
	}

	public static Struct call(PageContext pc, String destination, String fileField, String accept, String nameConflict, Object allowedExtensions, Object blockedExtensions,
			String mode, String attributes, Object acl) throws PageException {
		SecurityManager securityManager = pc.getConfig().getSecurityManager();

		int nc = FileUtil.toNameConflict(nameConflict);

		// mode
		int m = FileTag.toMode(mode);

		// allowed extensions
		ExtensionResourceFilter allowedFilter = null;
		if (!StringUtil.isEmpty(allowedExtensions)) {
			allowedFilter = FileUtil.toExtensionFilter(allowedExtensions);
		}

		// blocked extensions
		ExtensionResourceFilter blockedFilter = null;
		if (!StringUtil.isEmpty(blockedFilter)) {
			blockedFilter = FileUtil.toExtensionFilter(blockedExtensions);
		}

		return FileTag.actionUpload(pc, securityManager, fileField, destination, nc, accept, allowedFilter, blockedFilter, true, m, attributes, acl, null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		else if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		else if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		else if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		else if (args.length == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), args[4]);
		else if (args.length == 6) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), args[4], args[5]);
		else if (args.length == 7)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), args[4], args[5], Caster.toString(args[6]));
		else if (args.length == 8) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), args[4], args[5],
				Caster.toString(args[6]), Caster.toString(args[7]));
		else if (args.length == 9) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), args[4], args[5],
				Caster.toString(args[6]), Caster.toString(args[7]), args[8]);
		else throw new FunctionException(pc, "FileUpload", 1, 9, args.length);
	}
}