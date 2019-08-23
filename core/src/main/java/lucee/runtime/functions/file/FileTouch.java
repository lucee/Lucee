/**
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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.FileTag;

public class FileTouch extends BIF {

	private static final long serialVersionUID = -7478227658810128723L;

	public static String call(PageContext pc, Object file, boolean createPath) throws PageException {
		Resource res = Caster.toResource(pc, file, false);

		FileTag.actionTouch(pc, pc.getConfig().getSecurityManager(), res, null, createPath, null, -1, null);

		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, args[0], true);
		else if (args.length == 2) return call(pc, args[0], Caster.toBooleanValue(args[1]));
		throw new FunctionException(pc, "FileTouch", 1, 2, args.length);
	}
}