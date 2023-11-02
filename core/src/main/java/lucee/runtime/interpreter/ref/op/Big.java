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

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.literal.LBigDecimal;
import lucee.runtime.op.Caster;

/**
 * Plus operation
 */
public abstract class Big extends RefSupport implements Ref {

	private Ref right;
	private Ref left;
	protected boolean limited;

	/**
	 * constructor of the class
	 * 
	 * @param left
	 * @param right
	 */
	public Big(Ref left, Ref right, boolean limited) {
		this.left = left;
		this.right = right;
		this.limited = limited;
	}

	protected static BigDecimal toBigDecimal(PageContext pc, Ref ref) throws PageException {
		if (ref instanceof LBigDecimal) return ((LBigDecimal) ref).getBigDecimal();
		return new BigDecimal(Caster.toString(ref.getValue(pc)));
	}

	protected final BigDecimal getLeft(PageContext pc) throws PageException {
		return toBigDecimal(pc, left);
	}

	protected final BigDecimal getRight(PageContext pc) throws PageException {
		return toBigDecimal(pc, right);
	}

	@Override
	public final String getTypeName() {
		return "operation";
	}

}