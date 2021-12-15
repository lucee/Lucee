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
 * Implements the CFML Function duplicate
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;

public final class Duplicate extends BIF {

	private static final long serialVersionUID = 74899451528656931L;

	public static Object call(PageContext pc, Object object) {
		return Duplicator.duplicate(object, true);
	}

	public static Object call(PageContext pc, Object object, boolean deepCopy) {
		return Duplicator.duplicate(object, deepCopy);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, args[0], Caster.toBooleanValue(args[1]));
		if (args.length == 1) return call(pc, args[0]);
		throw new FunctionException(pc, "Duplicate", 1, 2, args.length);
	}
}