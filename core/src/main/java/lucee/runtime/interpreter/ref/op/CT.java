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
package lucee.runtime.interpreter.ref.op;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.InterpreterException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.op.OpUtil;

/**
 * imp operation
 */
public final class CT extends RefSupport implements Ref {

	private Ref right;
	private Ref left;
	private boolean limited;

	/**
	 * constructor of the class
	 * 
	 * @param left
	 * @param right
	 */
	public CT(Ref left, Ref right, boolean limited) {
		this.left = left;
		this.right = right;
		this.limited = limited;
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, boolean operations are not supported in a json string.");
		return OpUtil.ct(pc, left.getValue(pc), right.getValue(pc)) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getTypeName() {
		return "operation";
	}
}