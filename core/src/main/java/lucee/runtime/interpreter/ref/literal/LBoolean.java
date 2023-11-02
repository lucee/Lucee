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
import lucee.runtime.interpreter.ref.RefSupport;
import lucee.runtime.interpreter.ref.util.RefUtil;
import lucee.runtime.op.Caster;

/**
 * constructor of the class
 */
public final class LBoolean extends RefSupport implements Literal {

	/**
	 * Field <code>TRUE</code>
	 */
	public static final Ref TRUE = new LBoolean(Boolean.TRUE);
	/**
	 * Field <code>FALSE</code>
	 */
	public static final Ref FALSE = new LBoolean(Boolean.FALSE);

	private Boolean literal;

	/**
	 * constructor with Boolean
	 * 
	 * @param literal
	 */
	public LBoolean(Boolean literal) {
		this.literal = literal;
	}

	/**
	 * constructor with boolean
	 * 
	 * @param b
	 */
	public LBoolean(boolean b) {
		this.literal = b ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * constructor with boolean
	 * 
	 * @param str
	 * @throws PageException
	 */
	public LBoolean(String str) throws PageException {
		this.literal = Caster.toBoolean(str);
	}

	@Override
	public Object getValue(PageContext pc) {
		return literal;
	}

	@Override
	public String getTypeName() {
		return "literal";
	}

	@Override
	public String getString(PageContext pc) {
		return toString();
	}

	@Override
	public String toString() {
		return Caster.toString(literal.booleanValue());
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		if (other instanceof LNumber) {
			return literal.booleanValue() == ((LBoolean) other).literal.booleanValue();
		}
		return RefUtil.eeq(pc, this, other);
	}

}