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

	public static final LNumber ZERO = new LNumber(new Double(0));
	public static final LNumber ONE = new LNumber(new Double(1));

	private Object literal;

	/**
	 * constructor of the class
	 * 
	 * @param literal
	 */
	public LNumber(Double literal) {
		this.literal = literal;
	}

	/**
	 * constructor of the class
	 * 
	 * @param literal
	 * @throws PageException
	 */
	public LNumber(String literal) throws PageException {
		this.literal = Caster.toDouble(literal);
		// in theory this filter (>10) makes not really sense, just better for performance!!!
		if (literal.length() > 10) {
			if (!Caster.toString(this.literal).equals(literal)) this.literal = literal;
		}
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
	public String getString(PageContext pc) {
		return toString();
	}

	@Override
	public String toString() {
		return literal instanceof String ? (String) literal : Caster.toString((Double) literal);
	}

	@Override
	public boolean eeq(PageContext pc, Ref other) throws PageException {
		if (other instanceof LNumber) {
			if (literal instanceof Double) {
				// Double|Double
				if (((LNumber) other).literal instanceof Double) {
					return ((Double) literal).doubleValue() == ((Double) ((LNumber) other).literal).doubleValue();
				}
				// Double|String
				return Caster.toString(((Double) literal).doubleValue()).equals(((LNumber) other).literal);
			}
			// String|Double
			if (((LNumber) other).literal instanceof Double) {
				return ((String) literal).equals(Caster.toString(((Double) ((LNumber) other).literal).doubleValue()));
			}
			// String|String
			return ((String) literal).equals((((LNumber) other).literal));

		}
		return RefUtil.eeq(pc, this, other);
	}
}