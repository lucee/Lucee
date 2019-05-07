/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.op;

import lucee.runtime.exp.PageException;

public class NumericOpDouble implements NumericOp {

	@Override
	public Double divideRef(Object left, Object right) throws PageException {
		double r = Caster.toDoubleValue(right);
		if (r == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Caster.toDouble(Caster.toDoubleValue(left) / r);
	}

	@Override
	public Double exponentRef(Object left, Object right) throws PageException {
		return Caster.toDouble(StrictMath.pow(Caster.toDoubleValue(left), Caster.toDoubleValue(right)));
	}

	@Override
	public Double intdivRef(Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toIntValue(left) / Caster.toIntValue(right));
	}

	@Override
	public Double plusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toDoubleValue(left) + Caster.toDoubleValue(right));
	}

	@Override
	public Double minusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toDoubleValue(left) - Caster.toDoubleValue(right));
	}

	@Override
	public Double modulusRef(Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toDoubleValue(left) % Caster.toDoubleValue(right));
	}

	@Override
	public Double multiplyRef(Object left, Object right) throws PageException {
		return Caster.toDouble(Caster.toDoubleValue(left) * Caster.toDoubleValue(right));
	}
}