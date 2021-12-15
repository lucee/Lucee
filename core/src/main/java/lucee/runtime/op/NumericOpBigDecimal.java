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

import java.math.BigDecimal;

import lucee.commons.math.MathUtil;
import lucee.runtime.exp.PageException;

public class NumericOpBigDecimal implements NumericOp {

	@Override
	public BigDecimal divideRef(Object left, Object right) throws PageException {
		double r = Caster.toDoubleValue(right);
		if (r == 0d) throw new ArithmeticException("Division by zero is not possible");
		return Caster.toBigDecimal(left).divide(Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal exponentRef(Object left, Object right) throws PageException {
		return MathUtil.divide(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal intdivRef(Object left, Object right) throws PageException {
		return MathUtil.divide(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal plusRef(Object left, Object right) throws PageException {
		return MathUtil.add(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal minusRef(Object left, Object right) throws PageException {
		return MathUtil.subtract(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal modulusRef(Object left, Object right) throws PageException {
		return MathUtil.multiply(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}

	@Override
	public BigDecimal multiplyRef(Object left, Object right) throws PageException {
		return MathUtil.multiply(Caster.toBigDecimal(left), Caster.toBigDecimal(right));
	}
}