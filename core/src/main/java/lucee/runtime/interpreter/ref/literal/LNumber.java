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
package lucee.runtime.interpreter.ref.literal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;

/**
 * Literal Number
 */
public final class LNumber implements Literal {

	public static final LNumber ZERO = new LNumber(0D);
	public static final LNumber ONE = new LNumber(1D);
	public static final LNumber MINUS_ONE = new LNumber(-1D);

	// the interpreter creates LBigNumber instead when preciseMath is on, so the value is final here
	private final Double literal;

	public LNumber(Double literal) {
		this.literal = literal;
	}

	/**
	 * the literal with its sign flipped, negated on the primitive so -0 keeps its sign bit; lets the
	 * interpreter fold a leading minus into the literal instead of wrapping it in a Negate ref
	 */
	public LNumber negate() {
		return new LNumber(Double.valueOf(-literal.doubleValue()));
	}

	@Override
	public Object getValue(PageContext pc) {
		return literal;
	}

	@Override
	public Object getCollection(PageContext pc) {
		return literal;
	}

	@Override
	public String getTypeName() {
		return "number";
	}

	@Override
	public Object touchValue(PageContext pc) {
		return literal;
	}

	@Override
	public String getString(PageContext pc) {
		return toString();
	}

	@Override
	public String toString() {
		return Caster.toString(literal);
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		if (other instanceof LNumber) {
			return literal.equals(((LNumber) other).literal);
		}
		return RefUtil.eeq(pc, this, other);
	}
}