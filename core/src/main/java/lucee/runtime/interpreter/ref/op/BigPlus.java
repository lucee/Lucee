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

/**
 * Plus operation
 */
public final class BigPlus extends Big {

	/**
	 * constructor of the class
	 * 
	 * @param left
	 * @param right
	 */
	public BigPlus(Ref left, Ref right, boolean limited) {
		super(left, right, limited);
	}

	@Override
	public Object getValue(PageContext pc) throws PageException {
		if (limited) throw new InterpreterException("invalid syntax, math operations are not supported in a json string.");
		return getLeft(pc).add(getRight(pc)).toString();
	}

}