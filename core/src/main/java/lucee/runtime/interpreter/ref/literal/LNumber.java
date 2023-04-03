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

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;

/**
 * Literal Number
 */
public final class LNumber implements Literal {

	public static final LNumber ZERO = new LNumber(BigDecimal.ZERO);
	public static final LNumber ONE = new LNumber(BigDecimal.ONE);
	public static final LNumber MINUS_ONE = new LNumber(BigDecimal.valueOf(Double.valueOf(-1)));

	private BigDecimal literal;

	public LNumber(BigDecimal literal) {
		this.literal = literal;
	}

	/**
	 * constructor of the class
	 * 
	 * @param literal
	 * @throws PageException
	 */
	public LNumber(String literal) throws PageException {
		this.literal = Caster.toBigDecimal(literal);
	}

	@Override
	public Object getValue(PageContext pc) {
		if (!AppListenerUtil.getPreciseMath(pc, null)) return Double.valueOf(literal.doubleValue());
		return literal;
	}

	@Override
	public Object getCollection(PageContext pc) {
		return getValue(pc);
	}

	@Override
	public String getTypeName() {
		return "number";
	}

	@Override
	public Object touchValue(PageContext pc) {
		return getValue(pc);
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
			return getValue(pc).equals(((LNumber) other).getValue(pc)); // doing the methods here to have precisemath flag
		}
		return RefUtil.eeq(pc, this, other);
	}
}