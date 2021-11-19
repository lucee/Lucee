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

/**
 * Literal Number
 */
public final class LBigDecimal implements Ref {

	public static final LBigDecimal ZERO = new LBigDecimal(BigDecimal.ZERO);
	public static final LBigDecimal ONE = new LBigDecimal(BigDecimal.ONE);

	private BigDecimal literal;

	/**
	 * constructor of the class
	 * 
	 * @param literal
	 */
	public LBigDecimal(BigDecimal literal) {
		this.literal = literal;
	}

	/**
	 * constructor of the class
	 * 
	 * @param literal
	 * @throws PageException
	 */
	public LBigDecimal(String literal) throws PageException {
		this.literal = new BigDecimal(literal);
	}

	public BigDecimal getBigDecimal() {
		return literal;
	}

	@Override
	public Object getValue(PageContext pc) {
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
	public String toString() {
		return literal.toString();
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		return RefUtil.eeq(pc, this, other);
	}
}