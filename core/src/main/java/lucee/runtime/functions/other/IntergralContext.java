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
 * Implements the CFML Function gettickcount
 */
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class IntergralContext implements Function {

	private static final long serialVersionUID = -330160528570830717L;

	public static Struct call(PageContext pc) throws PageException {
		Struct sct = new StructImpl();
		sct.setEL(KeyImpl.getInstance("scopeNames"), Caster.toArray(pc.undefinedScope().getScopeNames()));
		// sct.setEL("stack", FDThreadImpl.getStack((PageContextImpl)pc));

		return sct;
	}
}