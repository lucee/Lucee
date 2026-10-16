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
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageSourcePool;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class PagePoolClear extends BIF {

	private static final long serialVersionUID = -2777306151061026079L;

	public static boolean call(PageContext pc) {
		return InspectTemplates.call(pc);
	}

	public static boolean call(PageContext pc, boolean force) {
		if (!force) {
			return InspectTemplates.call(pc);
		}
		PageSourcePool.clearPages(pc.getConfig(), null, false);
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc, false);
		else if (args.length == 1) return call(pc, Caster.toBooleanValue(args[0]));
		else throw new FunctionException(pc, "PagePoolClear", 0, 1, args.length);
	}
}