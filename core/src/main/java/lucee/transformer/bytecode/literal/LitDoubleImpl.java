/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.color.ConstantsDouble;
import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.literal.LitDouble;

/**
 * Literal Double Value
 */
public final class LitDoubleImpl extends ExpressionBase implements LitDouble, ExprDouble {

	// public static final LitDouble ZERO=new LitDouble(0,null,null);

	private double d;

	/**
	 * constructor of the class
	 * 
	 * @param d
	 * @param line
	 */
	public LitDoubleImpl(Factory f, double d, Position start, Position end) {
		super(f, start, end);

		this.d = d;
	}

	/**
	 * @return return value as double value
	 */
	@Override
	public double getDoubleValue() {
		return d;
	}

	/**
	 * @return return value as Double Object
	 */
	public Double getDouble() {
		return new Double(d);
	}

	/**
	 * @see lucee.transformer.expression.literal.Literal#getString()
	 */
	@Override
	public String getString() {
		return Caster.toString(d);
	}

	/**
	 * @return return value as a Boolean Object
	 */
	public Boolean getBoolean() {
		return Caster.toBoolean(d);
	}

	/**
	 * @return return value as a boolean value
	 */
	public boolean getBooleanValue() {
		return Caster.toBooleanValue(d);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) {
		GeneratorAdapter adapter = bc.getAdapter();
		if (mode == MODE_REF) {
			String str = ConstantsDouble.getFieldName(d);
			if (str != null) {
				bc.getAdapter().getStatic(Types.CONSTANTS_DOUBLE, str, Types.DOUBLE);
			}
			else {
				adapter.push(d);
				adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
			}
			return Types.DOUBLE;
		}
		adapter.push(d);

		return Types.DOUBLE_VALUE;
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return getDouble();
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return getBoolean();
	}
}