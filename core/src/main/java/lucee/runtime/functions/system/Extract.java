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
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Implements the CFML Function compress
 */
public final class Extract implements Function {

	public static boolean call(PageContext pc, String strFormat, String strSource, String srcTarget) throws PageException {

		boolean singleFileFormat = false;
		strFormat = strFormat.trim().toLowerCase();
		int format = CompressUtil.FORMAT_ZIP;
		if (strFormat.equals("bzip")) {
			format = CompressUtil.FORMAT_BZIP;
			singleFileFormat = true;
		}
		else if (strFormat.equals("bzip2")) {
			format = CompressUtil.FORMAT_BZIP2;
			singleFileFormat = true;
		}
		else if (strFormat.equals("gzip")) {
			format = CompressUtil.FORMAT_GZIP;
			singleFileFormat = true;
		}
		else if (strFormat.equals("tar")) format = CompressUtil.FORMAT_TAR;
		else if (strFormat.equals("tbz")) format = CompressUtil.FORMAT_TBZ;
		else if (strFormat.startsWith("tar.bz")) format = CompressUtil.FORMAT_TBZ;
		else if (strFormat.equals("tbz2")) format = CompressUtil.FORMAT_TBZ2;
		else if (strFormat.startsWith("tar.gz")) format = CompressUtil.FORMAT_TGZ;
		else if (strFormat.equals("tgz")) format = CompressUtil.FORMAT_TGZ;
		else if (strFormat.equals("zip")) format = CompressUtil.FORMAT_ZIP;
		else throw new FunctionException(pc, "extract", 1, "format",
				"invalid format definition [" + strFormat + "]," + " valid formats are [bzip,gzip,tar,tbz (tar bzip),tgz (tar gzip) and zip]");

		String[] arrSources = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(strSource, ","));

		Resource[] sources = new Resource[arrSources.length];
		for (int i = 0; i < sources.length; i++) {
			sources[i] = ResourceUtil.toResourceExisting(pc, arrSources[i]);
			// FileUtil.toFileExisting(pc,arrSources[i]);
			pc.getConfig().getSecurityManager().checkFileLocation(sources[i]);
		}

		Resource target = singleFileFormat ? ResourceUtil.toResourceNotExisting(pc, srcTarget) : ResourceUtil.toResourceExisting(pc, srcTarget);
		pc.getConfig().getSecurityManager().checkFileLocation(target);

		try {
			CompressUtil.extract(format, sources, target);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return true;
	}

}